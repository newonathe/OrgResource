import { request, type WorkspacePage } from "./client";

type Versioned = { id: string; version: number };
export type ProjectWork = Versioned & { name: string; startAt: string; endAt: string; leadMembershipId: string | null };
export type ProjectRequirement = Versioned & {
  projectId: string; resourceId: string; projectName: string; resourceName: string; resourceType: "REUSABLE" | "CONSUMABLE";
  quantity: number; allocatedQuantity: number; shortageQuantity: number;
};
export type ProjectAllocation = Versioned & {
  requirementId: string; resourceName: string; borrowerNameSnapshot: string; quantity: number; status: string;
  canCancel: boolean; canDelete: boolean;
};
export type BorrowedAllocation = Versioned & {
  projectName: string; resourceName: string; quantity: number; status: string; createdAt: string; smsConsent: boolean;
};
export type MembershipOption = { id: string; displayName: string };

function orgPath(org: string, route: string) {
  return "/v2/orgs/" + encodeURIComponent(org) + route;
}

function projectPath(project: string) {
  return "/projects/" + encodeURIComponent(project);
}

function page<T>(org: string, route: string, cursor: string | null, signal: AbortSignal, match: Record<string, string> = {}) {
  const query = new URLSearchParams(match);
  if (cursor) query.set("cursor", cursor);
  return request<WorkspacePage<T>>(orgPath(org, route + "?" + query), "GET", undefined, { signal }).then(result => result.data);
}

function write<T>(org: string, path: string, body?: unknown, version?: number, key?: string) {
  const headers: Record<string, string> = {};
  if (version !== undefined) headers["If-Match"] = `"v${version}"`;
  if (key) headers["Idempotency-Key"] = key;
  return request<T>(orgPath(org, path), "POST", body, { headers }).then(result => result.data);
}

function people(org: string, route: string, cursor: string | null, search: string, signal: AbortSignal, projectId?: string) {
  return page<{ membershipId: string; displayName: string }>(org, route, cursor, signal,
    projectId ? { projectId, search } : { search }).then(result => ({
    items: result.items.map(row => ({ id: row.membershipId, displayName: row.displayName })),
    nextCursor: result.nextCursor,
  }));
}

export const projectWorkApi = {
  projects: (org: string, cursor: string | null, signal: AbortSignal) => page<ProjectWork>(org, "/projects", cursor, signal),
  project: (org: string, id: string) => request<ProjectWork>(orgPath(org, projectPath(id)), "GET").then(result => result.data),
  candidates: (org: string, project: string, cursor: string | null, search: string, signal: AbortSignal) =>
    people(org, projectPath(project) + "/lead-options", cursor, search, signal),
  assign: (org: string, project: ProjectWork, leadMembershipId: string | null, operationId: string, key: string) =>
    write<ProjectWork>(org, projectPath(project.id) + "/lead-assignment", { operationId, leadMembershipId }, project.version, key),
  update: (org: string, project: ProjectWork, details: { name: string; startAt: string; endAt: string }) =>
    write<ProjectWork>(org, projectPath(project.id) + "/update", details, project.version),
  deleteProject: (org: string, project: ProjectWork) =>
    write(org, projectPath(project.id) + "/delete", undefined, project.version),
  requirements: (org: string, projectId: string, cursor: string | null, signal: AbortSignal) =>
    page<ProjectRequirement>(org, "/requirements", cursor, signal, { projectId }),
  createRequirement: (org: string, projectId: string, resourceId: string, quantity: number) =>
    write<ProjectRequirement>(org, "/requirements", { projectId, resourceId, quantity }),
  updateRequirement: (org: string, row: ProjectRequirement, quantity: number) =>
    write<ProjectRequirement>(org, "/requirements/" + encodeURIComponent(row.id) + "/update", { quantity }, row.version),
  deleteRequirement: (org: string, row: ProjectRequirement) =>
    write(org, "/requirements/" + encodeURIComponent(row.id) + "/delete", undefined, row.version),
  allocations: (org: string, project: string, cursor: string | null, signal: AbortSignal) =>
    page<ProjectAllocation>(org, projectPath(project) + "/allocations", cursor, signal),
  borrowers: (org: string, project: string, cursor: string | null, search: string, signal: AbortSignal) =>
    people(org, "/borrowers", cursor, search, signal, project),
  createAllocation: (org: string, requirementId: string, borrowerMembershipId: string | null, quantity: number,
    operationId: string, key: string) =>
    write<Versioned>(org, "/allocations", { requirementId, borrowerMembershipId, quantity, operationId }, undefined, key),
  cancelAllocation: (org: string, row: ProjectAllocation, operationId: string, key: string) =>
    write(org, "/allocations/" + encodeURIComponent(row.id) + "/cancel", { operationId }, row.version, key),
  deleteAllocation: (org: string, row: ProjectAllocation) =>
    write(org, "/allocations/" + encodeURIComponent(row.id) + "/delete", undefined, row.version),
  borrowed: (org: string, cursor: string | null, signal: AbortSignal) => page<BorrowedAllocation>(org, "/borrowed-allocations", cursor, signal),
  revokeConsent: (org: string, row: BorrowedAllocation, operationId: string, key: string) =>
    write(org, "/borrowed-allocations/" + encodeURIComponent(row.id) + "/revoke-sms-consent", { operationId }, row.version, key),
  receipt: (org: string, operationId: string) => request(orgPath(org, "/operations/" + encodeURIComponent(operationId)), "GET"),
};
