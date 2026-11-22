import { HashRouter, Navigate, Route, Routes } from "react-router-dom";
import { useEffect, useState, type ReactNode } from "react";
import { ApiError, request } from "./api/client";
import { AuthProvider, useAuth } from "./auth/AuthContext";
import { AccountPage, GovernancePage, InvitationPage, LoginPage, OrganizationPicker, RecoveryPage } from "./auth/AuthScreens";
import { AboutPage } from "./pages/AboutPage";
import { AllocationsPage } from "./pages/AllocationsPage";
import { DepartmentsPage } from "./pages/DepartmentsPage";
import { OverviewPage } from "./pages/OverviewPage";
import { ProjectsPage } from "./pages/ProjectsPage";
import { BorrowedAllocationsPage, ProjectWorkPage } from "./pages/ProjectWorkPages";
import { RemindersPage } from "./pages/RemindersPage";
import { RequirementsPage } from "./pages/RequirementsPage";
import { ResourcesPage } from "./pages/ResourcesPage";
import { ActivityPage, ExportsPage } from "./pages/ReportingPages";
import { AcademicShell } from "./shell/AcademicShell";
import { Button, SettingsProvider } from "./ui";

function WorkspaceRoutes() {
  const { ready, session, organizations, error } = useAuth();
  if (!ready) return <div className="boot-state" role="status">Restoring the secure server session…</div>;
  if (!session) return <Navigate replace to="/login" />;
  const organization = organizations.find(row => row.id === session.activeOrgId);
  if (!organization) return <OrganizationPicker />;
  const canViewActivity = ["OWNER", "ADMIN", "CUSTODIAN"].includes(organization.role);
  const canGovern = organization.role === "OWNER" || organization.role === "ADMIN";
  return <AcademicShell role={organization.role} organizationName={organization.displayName} sessionError={error}>
    <div key={organization.id} className="tenant-workspace">
      <Routes>
        <Route path="/" element={<OverviewPage />} />
        <Route path="/allocations" element={canViewActivity ? <AllocationsPage /> : <ForbiddenPage />} />
        <Route path="/projects" element={<ProjectsPage />} />
        <Route path="/project-work" element={canGovern || organization.role === "LEAD"
          ? <ProjectWorkPage orgId={organization.id} role={organization.role} /> : <ForbiddenPage />} />
        <Route path="/borrowed" element={<BorrowedAllocationsPage orgId={organization.id} />} />
        <Route path="/requirements" element={<RequirementsPage />} />
        <Route path="/departments" element={<DepartmentsPage />} />
        <Route path="/resources" element={<ResourcesPage />} />
        <Route path="/reminders" element={canViewActivity ? <RemindersPage /> : <ForbiddenPage />} />
        <Route path="/about" element={<AboutPage />} />
        <Route path="/account" element={<AccountPage />} />
        <Route path="/governance" element={canGovern ? <GovernancePage orgId={organization.id} role={organization.role} /> : <ForbiddenPage />} />
        <Route path="/activity" element={canGovern ? <ActivityPage orgId={organization.id} /> : <ForbiddenPage />} />
        <Route path="/exports" element={canGovern ? <ExportsPage orgId={organization.id} /> : <ForbiddenPage />} />
        <Route path="/invite" element={<InvitationPage />} />
        <Route path="*" element={<Navigate replace to="/" />} />
      </Routes>
    </div>
  </AcademicShell>;
}

function ForbiddenPage() {
  return <section className="page-shell"><h1>Not available for this role</h1><p>The server does not permit this membership to view these activity records.</p></section>;
}

function AppRoutes() {
  const { ready, session } = useAuth();
  return <Routes>
    <Route path="/login" element={session ? <Navigate replace to="/" /> : <LoginPage />} />
    <Route path="/recover" element={<RecoveryPage />} />
    <Route path="/invite" element={<InvitationPage />} />
    <Route path="*" element={ready ? <WorkspaceRoutes /> : <div className="boot-state" role="status">Restoring the secure server session…</div>} />
  </Routes>;
}

export function App() {
  return <SettingsProvider><DemoDisclosure><AuthProvider><HashRouter><AppRoutes /></HashRouter></AuthProvider></DemoDisclosure></SettingsProvider>;
}

type Environment =
  | { status: "checking"; attempt: number }
  | { status: "unavailable" }
  | { status: "ready"; notice: string | null };

// Shared-CPU hosting can return temporary 5xx while waking. Retry only the
// public, idempotent metadata GET; unverified provenance must never open the app.
const CLASSIFICATION_REQUEST_TIMEOUT_MS = 12_000;
const CLASSIFICATION_STARTUP_BUDGET_MS = 120_000;

function classifyEnvironment(data: unknown): Environment {
  if (typeof data !== "object" || data === null || !("synthetic" in data)) return { status: "unavailable" };
  const payload = data as Record<string, unknown>;
  if (payload.synthetic === false && payload.notice === null && payload.datasetId === null && payload.demonstrationAsOf === null)
    return { status: "ready", notice: null };
  if (payload.synthetic === true && typeof payload.notice === "string" && payload.notice.startsWith("DEMO ENVIRONMENT")
      && typeof payload.datasetId === "string" && payload.datasetId.trim()
      && typeof payload.demonstrationAsOf === "string" && /^\d{4}-\d{2}-\d{2}$/.test(payload.demonstrationAsOf))
    return { status: "ready", notice: payload.notice };
  return { status: "unavailable" };
}

function WorkspaceClassificationGate({ environment, retry }: { environment: Exclude<Environment, { status: "ready" }>; retry(): void }) {
  const checking = environment.status === "checking";
  return <main className="workspace-gate" aria-busy={checking}>
    <section className="workspace-gate-panel" aria-label="Secure workspace startup">
      <div className="workspace-gate-brand"><span aria-hidden="true">◈</span><strong>OrgResource</strong></div>
      <p className="workspace-gate-eyebrow">Secure organization workspace</p>
      <h1>{checking ? "Connecting to your workspace" : "Workspace verification unavailable"}</h1>
      {checking
        ? <>
          <p className="workspace-gate-description">We're verifying the workspace classification before restoring your session. On shared hosting, the server may need time to start.</p>
          <p className="workspace-gate-status" role="status" aria-live="polite">
            <span className="workspace-gate-spinner" aria-hidden="true" />
            Checking workspace classification…{environment.attempt > 1 ? " Reconnecting (attempt " + environment.attempt + ")." : ""}
          </p>
        </>
        : <>
          <p className="workspace-gate-description" role="alert">The server could not confirm the workspace classification. Your workspace remains protected until it can be verified.</p>
          <Button onClick={retry}>Retry classification</Button>
        </>}
    </section>
  </main>;
}

function DemoDisclosure({ children }: { children: ReactNode }) {
  const [attempt, setAttempt] = useState(0);
  const [environment, setEnvironment] = useState<Environment>({ status: "checking", attempt: 1 });

  useEffect(() => {
    let cancelled = false;
    let currentController: AbortController | null = null;
    let retryTimer: number | null = null;
    const startedAt = Date.now();
    let tries = 0;

    const verify = async () => {
      if (cancelled) return;
      tries += 1;
      setEnvironment({ status: "checking", attempt: tries });
      const controller = new AbortController();
      currentController = controller;
      const timeout = window.setTimeout(() => controller.abort(), CLASSIFICATION_REQUEST_TIMEOUT_MS);
      try {
        const result = await request<unknown>("/v2/demo/metadata", "GET", undefined, { signal: controller.signal });
        if (!cancelled) setEnvironment(classifyEnvironment(result.data));
      } catch (cause) {
        if (cancelled) return;
        const retriable = controller.signal.aborted ||
          (cause instanceof ApiError && (cause.status === 0 || cause.status === 408 || cause.status === 429 || cause.status >= 500));
        const delay = Math.min(6_000, 2_000 + tries * 750);
        if (!retriable || Date.now() - startedAt + delay >= CLASSIFICATION_STARTUP_BUDGET_MS) {
          setEnvironment({ status: "unavailable" });
        } else {
          retryTimer = window.setTimeout(() => {
            retryTimer = null;
            void verify();
          }, delay);
        }
      } finally {
        window.clearTimeout(timeout);
        if (currentController === controller) currentController = null;
      }
    };

    void verify();
    return () => {
      cancelled = true;
      currentController?.abort();
      if (retryTimer !== null) window.clearTimeout(retryTimer);
    };
  }, [attempt]);

  if (environment.status !== "ready") return <WorkspaceClassificationGate environment={environment}
    retry={() => { setEnvironment({ status: "checking", attempt: 1 }); setAttempt(value => value + 1); }} />;
  return <>{environment.notice && <div className="demo-disclosure" role="status">{environment.notice}</div>}{children}</>;
}
