import { afterEach, describe, expect, it, vi } from "vitest";
import { abortTenantRequests, api, setCsrfToken, setUnauthenticatedHandler } from "./client";

afterEach(() => { abortTenantRequests(); setCsrfToken(null); setUnauthenticatedHandler(null); vi.unstubAllGlobals(); });

function jsonResponse(status: number, body: unknown): Response {
  return new Response(JSON.stringify(body), { status, headers: { "Content-Type": "application/json" } });
}

describe("POST response certainty", () => {
  it("preserves the 401 response before session cleanup aborts pending requests", async () => {
    const clearSession = vi.fn(abortTenantRequests);
    setUnauthenticatedHandler(clearSession);
    vi.stubGlobal("fetch", vi.fn((_input: RequestInfo | URL, init?: RequestInit) => Promise.resolve({
      status: 401, ok: false, headers: new Headers(),
      text: async () => {
        init?.signal?.throwIfAborted();
        return JSON.stringify({ code: "AUTH_REQUIRED", message: "Sign in again." });
      },
    })));
    await expect(api.departments()).rejects.toMatchObject({ status: 401, code: "AUTH_REQUIRED" });
    expect(clearSession).toHaveBeenCalledOnce();
  });
  it("preserves a well-formed 409 rejection as definitive", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(jsonResponse(409, { code: "DEPENDENCY_EXISTS", message: "Still referenced." })));
    await expect(api.createDepartment("Fictional group")).rejects.toMatchObject({ status: 409, code: "DEPENDENCY_EXISTS" });
  });

  it.each([500, 503])("classifies HTTP %i as uncertain because the server cannot confirm the mutation outcome", async (status) => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(jsonResponse(status, { code: "FAILED", message: "No reliable outcome." })));
    await expect(api.createDepartment("Fictional group")).rejects.toMatchObject({ code: "POST_OUTCOME_UNCERTAIN" });
  });

  it("preserves a controlled 422 validation response as definitive", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(jsonResponse(422, { code: "VALIDATION_FAILED", message: "Value is invalid." })));
    await expect(api.createDepartment("Fictional group")).rejects.toMatchObject({ status: 422, code: "VALIDATION_FAILED" });
  });

  it("classifies invalid or missing successful confirmation as uncertain", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(new Response("not-json", { status: 200 })));
    await expect(api.createDepartment("Fictional group")).rejects.toMatchObject({ code: "POST_OUTCOME_UNCERTAIN" });
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(new Response(null, { status: 200 })));
    await expect(api.createDepartment("Fictional group")).rejects.toMatchObject({ code: "POST_OUTCOME_UNCERTAIN" });
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(jsonResponse(200, {})));
    await expect(api.createDepartment("Fictional group")).rejects.toMatchObject({ code: "POST_OUTCOME_UNCERTAIN" });
  });

  it("classifies a lost POST response as uncertain but leaves GET network errors distinct", async () => {
    vi.stubGlobal("fetch", vi.fn().mockRejectedValue(new TypeError("connection reset")));
    await expect(api.createDepartment("Fictional group")).rejects.toMatchObject({ code: "POST_OUTCOME_UNCERTAIN" });
    await expect(api.departments()).rejects.toMatchObject({ code: "NETWORK_ERROR" });
  });

  it("discards results that finish decoding after a workspace switch", async () => {
    let finish!: (value: string) => void;
    let bodyStarted!: () => void;
    const decoding = new Promise<void>(resolve => { bodyStarted = resolve; });
    const lateBody = new Promise<string>(resolve => { finish = resolve; });
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue({
      ok: true, status: 200, headers: new Headers(),
      text: () => { bodyStarted(); return lateBody; },
    }));
    const pending = api.departments();
    await decoding;
    abortTenantRequests();
    finish(JSON.stringify([{ id: 1, name: "Prior workspace" }]));
    await expect(pending).rejects.toMatchObject({ name: "AbortError" });
  });

  it("sends only same-origin session requests with in-memory CSRF and aborts pending tenant reads", async () => {
    setCsrfToken("memory-only-csrf");
    let signal: AbortSignal | null = null;
    vi.stubGlobal("fetch", vi.fn((_input: RequestInfo | URL, init?: RequestInit) => {
      signal = init?.signal ?? null;
      return new Promise<Response>((_resolve, reject) => signal?.addEventListener("abort", () => reject(new DOMException("aborted", "AbortError")), { once: true }));
    }));
    const pending = api.departments();
    expect(signal).not.toBeNull();
    abortTenantRequests();
    await expect(pending).rejects.toMatchObject({ name: "AbortError" });
    const post = vi.fn().mockResolvedValue(jsonResponse(201, { id: 1, name: "Fictional" }));
    vi.stubGlobal("fetch", post);
    await api.createDepartment("Fictional");
    expect(post).toHaveBeenCalledWith("/api/departments", expect.objectContaining({
      credentials: "same-origin", cache: "no-store", headers: expect.objectContaining({ "X-CSRF-TOKEN": "memory-only-csrf" }),
    }));
  });
});
