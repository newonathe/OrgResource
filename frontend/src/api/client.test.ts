import { afterEach, describe, expect, it, vi } from "vitest";
import { api } from "./client";

afterEach(() => vi.unstubAllGlobals());

function jsonResponse(status: number, body: unknown): Response {
  return new Response(JSON.stringify(body), { status, headers: { "Content-Type": "application/json" } });
}

describe("POST response certainty", () => {
  it("preserves a well-formed 409 rejection as definitive", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(jsonResponse(409, { code: "DEPENDENCY_EXISTS", message: "Still referenced." })));
    await expect(api.createDepartment("Fictional group")).rejects.toMatchObject({ status: 409, code: "DEPENDENCY_EXISTS" });
  });

  it.each([500, 422])("classifies HTTP %i as uncertain because it is not a definitive rejection status", async (status) => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(jsonResponse(status, { code: "FAILED", message: "No reliable outcome." })));
    await expect(api.createDepartment("Fictional group")).rejects.toMatchObject({ code: "POST_OUTCOME_UNCERTAIN" });
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
});
