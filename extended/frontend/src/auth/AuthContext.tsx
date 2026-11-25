import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { abortTenantRequests, setCsrfToken, setMissingOrganizationHandler, setUnauthenticatedHandler } from "../api/client";
import { identityApi, type OrganizationView, type SessionView } from "../api/extended";
import { ApiError } from "../api/client";

interface AuthState {
  ready: boolean;
  session: SessionView | null;
  organizations: OrganizationView[];
  error: string | null;
  signIn(username: string, password: string): Promise<void>;
  signOut(): Promise<void>;
  switchOrganization(id: string): Promise<void>;
  adoptSession(session: SessionView, csrf: string): Promise<void>;
  reload(): Promise<void>;
  clearError(): void;
}

const AuthContext = createContext<AuthState | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [ready, setReady] = useState(false);
  const [session, setSession] = useState<SessionView | null>(null);
  const [organizations, setOrganizations] = useState<OrganizationView[]>([]);
  const [error, setError] = useState<string | null>(null);

  const clearAuth = useCallback(() => {
    abortTenantRequests();
    setSession(null);
    setOrganizations([]);
    setCsrfToken(null);
  }, []);

  const reload = useCallback(async () => {
    try {
      const csrf = await identityApi.csrf();
      setCsrfToken(csrf.csrfToken);
      const current = await identityApi.session();
      const orgs = await identityApi.organizations();
      setSession(current);
      setOrganizations(orgs);
      setError(null);
    } catch (cause) {
      if (cause instanceof ApiError && cause.status === 401) {
        clearAuth();
        try {
          const csrf = await identityApi.csrf();
          setCsrfToken(csrf.csrfToken);
        } catch {
          setCsrfToken(null);
        }
        setError(null);
      } else {
        setError(errorText(cause));
      }
    } finally {
      setReady(true);
    }
  }, [clearAuth]);

  useEffect(() => {
    setUnauthenticatedHandler(clearAuth);
    setMissingOrganizationHandler(() => {
      abortTenantRequests();
      setSession(previous => previous ? { ...previous, activeOrgId: null } : previous);
    });
    const timer = window.setTimeout(() => { void reload(); }, 0);
    return () => {
      window.clearTimeout(timer);
      setUnauthenticatedHandler(null);
      setMissingOrganizationHandler(null);
    };
  }, [clearAuth, reload]);

  const signIn = useCallback(async (username: string, password: string) => {
    setError(null);
    try {
      const csrf = await identityApi.csrf();
      setCsrfToken(csrf.csrfToken);
      const result = await identityApi.login(username, password);
      setCsrfToken(result.csrfToken);
      const orgs = await identityApi.organizations();
      setSession(result.session);
      setOrganizations(orgs);
    } catch (cause) {
      setError(errorText(cause));
      throw cause;
    }
  }, []);

  const signOut = useCallback(async () => {
    abortTenantRequests();
    try {
      await identityApi.logout();
      clearAuth();
    } catch (cause) {
      if (cause instanceof ApiError && cause.status === 401) clearAuth();
      else setError("The server did not confirm sign out. Retry before leaving a shared device.");
    }
  }, [clearAuth]);

  const adoptSession = useCallback(async (next: SessionView, csrf: string) => {
    setCsrfToken(csrf);
    setSession(next);
    setOrganizations(await identityApi.organizations());
  }, []);

  const switchOrganization = useCallback(async (id: string) => {
    if (!session) return;
    if (session.activeOrgId === id) return;
    abortTenantRequests();
    setSession(previous => previous ? { ...previous, activeOrgId: null } : previous);
    setError(null);
    try {
      const selected = await identityApi.selectOrganization(id);
      const [csrf, orgs] = await Promise.all([identityApi.csrf(), identityApi.organizations()]);
      setCsrfToken(csrf.csrfToken);
      setSession(selected);
      setOrganizations(orgs);
    } catch (cause) {
      setError(errorText(cause));
      if (cause instanceof ApiError && cause.code === "POST_OUTCOME_UNCERTAIN") {
        await reload();
      }
      throw cause;
    }
  }, [reload, session]);

  const value = useMemo<AuthState>(() => ({
    ready, session, organizations, error, signIn, signOut, switchOrganization, adoptSession, reload,
    clearError: () => setError(null),
  }), [ready, session, organizations, error, signIn, signOut, switchOrganization, adoptSession, reload]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthState {
  const value = useContext(AuthContext);
  if (!value) throw new Error("useAuth must be used within AuthProvider.");
  return value;
}

export function errorText(cause: unknown): string {
  if (cause instanceof ApiError) {
    if (cause.status === 429 || cause.status === 503) return `${cause.message} Please wait briefly before retrying.`;
    if (cause.status === 403) return cause.message || "Your current role cannot perform this action.";
    if (cause.status === 409 && cause.code === "ORG_CONTEXT_REQUIRED") return "Choose an organization before opening this workspace.";
    return cause.message;
  }
  return cause instanceof Error ? cause.message : "The request could not be completed.";
}
