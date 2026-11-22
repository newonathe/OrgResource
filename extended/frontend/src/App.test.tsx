import { afterEach, describe, expect, it, vi } from "vitest";
import { act, cleanup, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import { App } from "./App";

function jsonResponse(data: unknown, status = 200): Response {
  return new Response(JSON.stringify(data), { status, headers: { "Content-Type": "application/json" } });
}
function envelope(data: unknown, status = 200) {
  return jsonResponse({ data, requestId: "test-request" }, status);
}
const ordinaryMetadata = { synthetic: false, datasetId: null, demonstrationAsOf: null, notice: null };
const syntheticMetadata = { synthetic: true, datasetId: "orgresource-c8-plan-v1-012345678901-abcdefabcdef-small",
  demonstrationAsOf: "2026-11-19", notice: "DEMO ENVIRONMENT — All operational activity is simulated." };

function authenticatedFetch(role: "OWNER" | "ADMIN" | "CUSTODIAN" | "LEAD" | "MEMBER" = "OWNER") {
  let activeOrgId: string | null = "org-a";
  let csrf = "csrf-initial";
  let departments: Array<{ id: number; name: string }> = [];
  return vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
    const url = String(input); const method = init?.method ?? "GET";
    if (url === "/api/v2/demo/metadata") return envelope(ordinaryMetadata);
    if (url === "/api/v2/auth/csrf") return envelope({ csrfToken: csrf });
    if (url === "/api/v2/session") return envelope({ accountId: "account-a", displayName: "Fictional Operator", activeOrgId, memberships: [] });
    if (url === "/api/v2/orgs" && method === "GET") return envelope([{ id: "org-a", slug: "fictional", displayName: "Fictional Organization", role }]);
    if (url === "/api/v2/workspace/overview") return envelope({
      sampledAt: "2026-11-19T10:00:00", departmentCount: departments.length, projectActiveCount: 0,
      resourceTypeCount: 0, requirementShortageCount: 0, resourcePreview: [], calendarPreview: [], allocations: null,
    });
    if (url.startsWith("/api/v2/workspace/") && method === "GET") {
      return envelope({ items: url.startsWith("/api/v2/workspace/departments") ? departments : [], nextCursor: null });
    }
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

afterEach(() => { cleanup(); vi.useRealTimers(); vi.restoreAllMocks(); vi.unstubAllGlobals(); window.location.hash = ""; });

describe("Workspace provenance", () => {
  it.each([
    ["forbidden metadata", () => Promise.resolve(jsonResponse({ code: "FORBIDDEN" }, 403))],
    ["aborted request", () => Promise.reject(new DOMException("Request aborted", "AbortError"))],
    ["malformed JSON", () => Promise.resolve(new Response("{invalid"))],
    ["invalid metadata", () => Promise.resolve(envelope({ ...syntheticMetadata, notice: "" }))],
    ["missing ordinary fields", () => Promise.resolve(envelope({ synthetic: false }))],
    ["array metadata", () => Promise.resolve(envelope([]))],
  ] as const)("holds the shell on %s and requires an explicit retry", async (_failure, respond) => {
    const authenticated = authenticatedFetch();
    let attempts = 0;
    vi.stubGlobal("fetch", vi.fn((url: RequestInfo | URL, init?: RequestInit) =>
      String(url) === "/api/v2/demo/metadata" && ++attempts === 1 ? respond() : authenticated(url, init)));
    render(<App />);
    await screen.findByRole("alert");
    expect(authenticated).not.toHaveBeenCalled();
    expect(screen.queryByRole("navigation", { name: "Main navigation" })).not.toBeInTheDocument();
    fireEvent.click(screen.getByRole("button", { name: "Retry classification" }));
    await screen.findByRole("heading", { name: "Overview" });
    expect(attempts).toBe(2);
    expect(screen.queryByText(/DEMO ENVIRONMENT/)).not.toBeInTheDocument();
  });

  it("aborts a stalled classification and keeps workspace routes hidden", async () => {
    vi.useFakeTimers();
    const pending = vi.fn((_url: RequestInfo | URL, options?: RequestInit) => new Promise<Response>((_, reject) =>
      options?.signal?.addEventListener("abort", () => reject(new DOMException("Aborted", "AbortError")), { once: true })));
    vi.stubGlobal("fetch", pending);
    render(<App />);
    expect(screen.getByRole("status")).toHaveTextContent("Checking workspace classification");
    await act(async () => { await vi.advanceTimersByTimeAsync(140000); });
    expect(screen.getByRole("alert")).toHaveTextContent("Workspace classification unavailable");
    expect(pending.mock.calls.length).toBeGreaterThan(1);
    expect(screen.queryByRole("navigation", { name: "Main navigation" })).not.toBeInTheDocument();
  });

  it("recovers from a cold backend without bypassing provenance verification", async () => {
    const authenticated = authenticatedFetch();
    let attempts = 0;
    vi.stubGlobal("fetch", vi.fn((url: RequestInfo | URL, init?: RequestInit) => {
      if (String(url) === "/api/v2/demo/metadata" && ++attempts < 3)
        return Promise.resolve(jsonResponse({ code: "TEMPORARILY_UNAVAILABLE" }, 503));
      return authenticated(url, init);
    }));
    render(<App />);
    expect(screen.queryByRole("navigation", { name: "Main navigation" })).not.toBeInTheDocument();
    await screen.findByRole("heading", { name: "Overview" }, { timeout: 15_000 });
    expect(attempts).toBe(3);
    expect(screen.queryByRole("heading", { name: "Workspace verification unavailable" })).not.toBeInTheDocument();
  });

  it("renders a readable standalone boot panel before metadata arrives", () => {
    vi.stubGlobal("fetch", vi.fn(() => new Promise<Response>(() => undefined)));
    render(<App />);
    expect(screen.getByRole("heading", { name: "Connecting to your workspace" })).toBeVisible();
    expect(screen.getByRole("region", { name: "Secure workspace startup" })).toHaveClass("workspace-gate-panel");
    expect(screen.getByRole("status")).toHaveTextContent("Checking workspace classification");
    expect(screen.queryByRole("navigation", { name: "Main navigation" })).not.toBeInTheDocument();
  });

  it("waits for delayed synthetic metadata before accessing the login", async () => {
    window.location.hash = "#/login";
    let deliver!: (response: Response) => void;
    const delayed = new Promise<Response>(resolve => { deliver = resolve; });
    const loginRead = vi.fn((url: RequestInfo | URL) => Promise.resolve(String(url) === "/api/v2/auth/csrf"
      ? envelope({ csrfToken: "pre-auth" }) : jsonResponse({ code: "AUTH_REQUIRED" }, 401)));
    vi.stubGlobal("fetch", vi.fn((url: RequestInfo | URL) =>
      String(url) === "/api/v2/demo/metadata" ? delayed : loginRead(url)));
    render(<App />);
    expect(screen.getByRole("status")).toHaveTextContent("Checking workspace classification");
    expect(screen.queryByRole("button", { name: "Sign in" })).not.toBeInTheDocument();
    expect(loginRead).not.toHaveBeenCalled();
    deliver(envelope(syntheticMetadata));
    await screen.findByRole("heading", { name: "Welcome back" });
    expect(screen.getByText(syntheticMetadata.notice)).toBeVisible();
  });
});

describe("Borrower reminder consent", () => {
  it("allows withdrawal only when the server reports active consent", async () => {
    window.location.hash = "#/borrowed";
    const authenticated = authenticatedFetch("MEMBER");
    let active = true;
    let withdrawals = 0;
    vi.spyOn(window, "confirm").mockReturnValue(true);
    vi.stubGlobal("fetch", vi.fn((input: RequestInfo | URL, init?: RequestInit) => {
      const url = String(input);
      if (url.startsWith("/api/v2/orgs/org-a/borrowed-allocations?")) return Promise.resolve(envelope({
        items: [{ id: "batch-a", version: active ? 1 : 2, resourceName: "Fictional projector", projectName: "Fictional workshop", quantity: 1,
          createdAt: "2026-11-19T10:00:00", status: "PENDING", smsConsent: active }], nextCursor: null,
      }));
      if (url.endsWith("/borrowed-allocations/batch-a/revoke-sms-consent")) {
        expect(init?.method).toBe("POST");
        withdrawals++;
        active = false;
        return Promise.resolve(envelope({ id: "batch-a", version: 2, smsConsent: false }));
      }
      return authenticated(input, init);
    }));
    render(<App />);
    await screen.findByText("SMS reminder consent is active.");
    fireEvent.click(screen.getByRole("button", { name: "Withdraw SMS consent" }));
    await screen.findByText("No active SMS reminder consent.");
    expect(screen.queryByRole("button", { name: "Withdraw SMS consent" })).not.toBeInTheDocument();
    expect(withdrawals).toBe(1);
  });
});

describe("Extended C authentication and organization shell", () => {
  it("logs in with CSRF, explicitly selects an organization and restores the server session", async () => {
    let authenticated = false;
    let activeOrgId: string | null = null;
    let csrf = "pre-auth";
    const controlled = vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
      const url = String(input); const method = init?.method ?? "GET";
      if (url === "/api/v2/demo/metadata") return envelope(ordinaryMetadata);
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
      String(input).startsWith("/api/v2/workspace/departments")
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

  it.each(["ADMIN", "CUSTODIAN"] as const)("gates allocation creation for the %s role", async role => {
    window.location.hash = "#/allocations";
    vi.stubGlobal("fetch", authenticatedFetch(role));
    render(<App />);
    await screen.findByRole("heading", { name: "Allocations", level: 1 });
    expect(screen.queryByRole("button", { name: "Submit request" }) !== null).toBe(role === "ADMIN");
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
