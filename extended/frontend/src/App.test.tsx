import { afterEach, describe, expect, it, vi } from "vitest";
import { cleanup, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import { App } from "./App";

function jsonResponse(data: unknown, status = 200): Response {
  return new Response(JSON.stringify(data), { status, headers: { "Content-Type": "application/json" } });
}
function envelope(data: unknown, status = 200) {
  return jsonResponse({ data, requestId: "test-request" }, status);
}
function authenticatedFetch(role: "OWNER" | "ADMIN" | "CUSTODIAN" | "LEAD" | "MEMBER" = "OWNER") {
  let activeOrgId: string | null = "org-a";
  let csrf = "csrf-initial";
  let departments: Array<{ id: number; name: string }> = [];
  return vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
    const url = String(input); const method = init?.method ?? "GET";
    if (url === "/api/v2/auth/csrf") return envelope({ csrfToken: csrf });
    if (url === "/api/v2/session") return envelope({ accountId: "account-a", displayName: "Fictional Operator", activeOrgId, memberships: [] });
    if (url === "/api/v2/orgs" && method === "GET") return envelope([{ id: "org-a", slug: "fictional", displayName: "Fictional Organization", role }]);
    if (url === "/api/departments" && method === "GET") return jsonResponse(departments);
    if (url === "/api/departments" && method === "POST") {
      if ((init?.headers as Record<string, string>)["X-CSRF-TOKEN"] !== csrf) return jsonResponse({ code: "CSRF", message: "Bad token." }, 403);
      const body = JSON.parse(String(init?.body)) as { name: string };
      const created = { id: departments.length + 1, name: body.name.trim() }; departments = [...departments, created];
      return jsonResponse(created, 201);
    }
    if (url === "/api/v2/session/active-org" && method === "POST") {
      if ((init?.headers as Record<string, string>)["X-CSRF-TOKEN"] !== csrf) return jsonResponse({ code: "CSRF", message: "Bad token." }, 403);
      activeOrgId = String((JSON.parse(String(init?.body)) as { organizationId: string }).organizationId);
      csrf = "csrf-after-org-switch";
      return envelope({ accountId: "account-a", displayName: "Fictional Operator", activeOrgId, memberships: [] });
    }
    if (method === "GET") return jsonResponse([]);
    if (method === "POST") return envelope({ message: "ok" });
    return jsonResponse({ code: "NOT_FOUND", message: "No matching test route." }, 404);
  });
}

afterEach(() => { cleanup(); vi.unstubAllGlobals(); });

describe("Extended C authentication and organization shell", () => {
  it("logs in with CSRF, explicitly selects an organization and restores the server session", async () => {
    let authenticated = false;
    let activeOrgId: string | null = null;
    let csrf = "pre-auth";
    const controlled = vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
      const url = String(input); const method = init?.method ?? "GET";
      if (url === "/api/v2/auth/csrf") return envelope({ csrfToken: csrf });
      if (url === "/api/v2/session") return authenticated
        ? envelope({ accountId: "a1", displayName: "Fictional Operator", activeOrgId, memberships: [] })
        : jsonResponse({ code: "AUTH_REQUIRED", message: "Sign in." }, 401);
      if (url === "/api/v2/orgs") return envelope([{ id: "org-a", slug: "fictional", displayName: "Fictional Organization", role: "OWNER" }]);
      if (url === "/api/v2/auth/login" && method === "POST") {
        expect((init?.headers as Record<string, string>)["X-CSRF-TOKEN"]).toBe("pre-auth");
        authenticated = true; csrf = "post-login";
        return envelope({ session: { accountId: "a1", displayName: "Fictional Operator", activeOrgId: null, memberships: [] }, csrfToken: csrf });
      }
      if (url === "/api/v2/session/active-org") {
        expect((init?.headers as Record<string, string>)["X-CSRF-TOKEN"]).toBe("post-login");
        activeOrgId = "org-a"; csrf = "post-selection";
        return envelope({ accountId: "a1", displayName: "Fictional Operator", activeOrgId, memberships: [] });
      }
      if (method === "GET") return jsonResponse([]);
      return envelope({ message: "ok" });
    });
    vi.stubGlobal("fetch", controlled);
    render(<App />);
    await screen.findByRole("heading", { name: "Welcome back" });
    fireEvent.change(screen.getByLabelText("Username"), { target: { value: "operator" } });
    fireEvent.change(screen.getByLabelText("Password"), { target: { value: "Correct-Horse-Bookcase-73" } });
    fireEvent.click(screen.getByRole("button", { name: "Sign in" }));
    await screen.findByRole("heading", { name: /Choose an organization/ });
    fireEvent.click(screen.getByRole("button", { name: "Open workspace" }));
    await screen.findByRole("heading", { name: "Overview" });
    expect(controlled).toHaveBeenCalledWith("/api/v2/session/active-org", expect.objectContaining({ method: "POST" }));
    expect(window.localStorage.getItem("operator")).toBeNull();
    expect(window.localStorage.getItem("pre-auth")).toBeNull();
  });

  it("restores the authenticated workspace and displays the eight connected academic screens", async () => {
    vi.stubGlobal("fetch", authenticatedFetch());
    render(<App />);
    const navigation = within(await screen.findByRole("navigation", { name: "Main navigation" }));
    const routes = [
      ["Allocations", "Allocations"], ["Resource Library", "Resource Library"], ["Projects", "Projects"],
      ["Requirements", "Requirements"], ["Departments", "Departments"], ["Reminder Activity", "Reminder Activity"], ["About", "About OrgResource"],
    ] as const;
    expect(await screen.findByRole("heading", { name: "Overview" })).toBeInTheDocument();
    for (const [link, title] of routes) {
      fireEvent.click(navigation.getByRole("link", { name: new RegExp("^" + link + "$") }));
      expect(await screen.findByRole("heading", { name: title, level: 1 })).toBeInTheDocument();
    }
  });

  it("hides allocation activity and governance navigation for MEMBER", async () => {
    vi.stubGlobal("fetch", authenticatedFetch("MEMBER"));
    render(<App />);
    const navigation = within(await screen.findByRole("navigation", { name: "Main navigation" }));
    expect(navigation.queryByRole("link", { name: "Allocations" })).not.toBeInTheDocument();
    expect(navigation.queryByRole("link", { name: "Reminder Activity" })).not.toBeInTheDocument();
    expect(navigation.queryByRole("link", { name: "Members and access" })).not.toBeInTheDocument();
  });

  it("returns to the organization picker when the server has lost the active context", async () => {
    window.location.hash = "#/departments";
    const authenticated = authenticatedFetch();
    vi.stubGlobal("fetch", vi.fn((input: RequestInfo | URL, init?: RequestInit) =>
      String(input) === "/api/departments"
        ? Promise.resolve(jsonResponse({ code: "ORG_CONTEXT_REQUIRED", message: "Select an organization." }, 409))
        : authenticated(input, init)));
    render(<App />);
    expect(await screen.findByRole("heading", { name: /Choose an organization/ })).toBeInTheDocument();
  });

  it("shows an unconfirmed sign-out error and clears the UI after a confirmed retry", async () => {
    const authenticated = authenticatedFetch();
    let logoutAttempts = 0;
    vi.stubGlobal("fetch", vi.fn((input: RequestInfo | URL, init?: RequestInit) => {
      if (String(input) !== "/api/v2/auth/logout") return authenticated(input, init);
      ++logoutAttempts;
      return logoutAttempts === 1
        ? Promise.reject(new TypeError("connection reset"))
        : Promise.resolve(new Response(null, { status: 204 }));
    }));
    render(<App />);
    fireEvent.click(await screen.findByRole("button", { name: "Sign out" }));
    expect(await screen.findByText(/server did not confirm sign out/)).toBeInTheDocument();
    expect(logoutAttempts).toBe(1);
    fireEvent.click(screen.getByRole("button", { name: "Sign out" }));
    expect(await screen.findByRole("heading", { name: "Welcome back" })).toBeInTheDocument();
  });

  it("offers administrators only the membership changes allowed by the server contract", async () => {
    window.location.hash = "#/governance";
    const authenticated = authenticatedFetch("ADMIN");
    vi.stubGlobal("fetch", vi.fn((input: RequestInfo | URL, init?: RequestInit) =>
      String(input).startsWith("/api/v2/orgs/org-a/members?")
        ? Promise.resolve(envelope({ items: [
          { id: "admin", displayName: "Peer Administrator", role: "ADMIN", status: "ACTIVE", version: 0 },
          { id: "member", displayName: "General Member", role: "MEMBER", status: "ACTIVE", version: 0 },
        ], nextCursor: null }))
        : authenticated(input, init)));
    render(<App />);
    await screen.findByText("Peer Administrator");
    expect(screen.getAllByRole("button", { name: "Suspend" })).toHaveLength(1);
    expect(within(screen.getByLabelText("Grant role")).queryByRole("option", { name: "ADMIN" })).toBeNull();
    expect(within(screen.getByLabelText("Role", { exact: true })).queryByRole("option", { name: "ADMIN" })).toBeNull();
    expect(screen.queryByRole("heading", { name: "Transfer ownership" })).toBeNull();
    expect(screen.getByRole("heading", { name: "Accept ownership transfer" })).toBeInTheDocument();
  });

  it("sends the in-memory CSRF value on mutations", async () => {
    const fetchMock = authenticatedFetch();
    vi.stubGlobal("fetch", fetchMock);
    render(<App />);
    fireEvent.click(await screen.findByRole("link", { name: "Departments" }));
    await screen.findByText("No departments yet");
    fireEvent.change(screen.getByLabelText("Department name"), { target: { value: "Field Operations" } });
    fireEvent.click(screen.getByRole("button", { name: "Add department" }));
    expect(await screen.findByRole("button", { name: "Edit Field Operations" })).toBeInTheDocument();
    await waitFor(() => expect(fetchMock).toHaveBeenCalledWith("/api/departments", expect.objectContaining({ method: "POST", headers: expect.objectContaining({ "X-CSRF-TOKEN": "csrf-initial" }) })));
  });
});
