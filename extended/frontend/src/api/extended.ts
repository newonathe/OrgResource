import { request, type RequestResult } from "./client";

export type ReportingDataset = "RESOURCES" | "MOVEMENTS" | "AUDIT";
export type ReportingFormat = "CSV" | "TENANT_JSON";
export interface ReportingEvent {
  id: string; source: "AUDIT" | "MOVEMENT"; action: string; targetType: string; outcome: string | null;
  resourceId: string | null; quantity: number | null; deltaTotal: number | null; deltaIssued: number | null;
  occurredAt: string;
}
interface ReportingPage { items: ReportingEvent[]; nextCursor: string | null; }
interface ExportDownload { filename: string; file: Blob; }

export const reportingApi = {
  events: (orgId: string, feed: "activity" | "audit", cursor: string | null, signal: AbortSignal) =>
    request<ReportingPage>(`/api/v2/orgs/${encodeURIComponent(orgId)}/${feed}${cursor ? `?cursor=${encodeURIComponent(cursor)}` : ""}`,
      "GET", undefined, { signal }).then(result => result.data),
  download: (orgId: string, dataset: ReportingDataset, format: ReportingFormat, signal: AbortSignal) =>
    request<ExportDownload>(`/api/v2/orgs/${encodeURIComponent(orgId)}/exports`, "POST", { dataset, format }, {
      signal,
      headers: { Accept: "text/csv, application/json" },
      download: async response => {
        const filename = response.headers.get("Content-Disposition")?.match(/^attachment; filename="([a-zA-Z0-9.-]+)"$/)?.[1];
        const contentType = response.headers.get("Content-Type")?.split(";")[0];
        const expectedType = format === "CSV" ? "text/csv" : "application/json";
        if (!filename?.match(/^orgresource-(resources|movements|audit)-\d{8}T\d{6}Z\.(csv|json)$/) || contentType !== expectedType ||
            !filename.endsWith(format === "CSV" ? ".csv" : ".json")) {
          throw new Error("The server did not return the requested export file.");
        }
        return { filename, file: await response.blob() };
      },
    }).then(result => result.data),
};

export type Role = "OWNER" | "ADMIN" | "CUSTODIAN" | "LEAD" | "MEMBER";
export interface SessionView { accountId: string; displayName: string; activeOrgId: string | null; memberships: MemberView[]; }
export interface OrganizationView { id: string; slug: string; displayName: string; role: Role; }
export interface MemberView { id: string; organizationId: string; username: string; displayName: string; role: Role; status: string; version: number; }
export interface GovernanceMember { id: string; displayName: string; role: Role; status: string; version: number; }
export interface MemberPage { items: GovernanceMember[]; nextCursor: string | null; }
export interface AccountView { id: string; username: string; displayName: string; status: string; }
export interface LoginResult { session: SessionView; csrfToken: string; }
export interface RecoveryCodes { codes: string[]; }
export interface Invitation { id: string; organizationId: string; role: Role; status: string; expiresAt: string; version: number; bearerToken: string | null; }
export interface InventoryResource { id: string; name: string; type: "REUSABLE" | "CONSUMABLE"; totalQuantity: number; availableQuantity: number; version: number; }
export interface ResourcePage { items: InventoryResource[]; nextCursor: string | null; }
export interface AllocationState { id: string; resourceId: string; status: string; quantity: number; version: number; }
export interface AllocationPage { items: AllocationState[]; nextCursor: string | null; }
export interface Movement { id: string; operationId: string; allocationId: string | null; kind: string; quantity: number; deltaTotal: number; deltaIssued: number; occurredAt: string; }
export interface MovementPage { items: Movement[]; nextCursor: string | null; }
export interface OperationReceipt { operationId: string; resourceId: string; allocationId: string | null; kind: string; quantity: number; deltaTotal: number; deltaIssued: number; occurredAt: string; }
export interface OperationView { operationId: string; action: string; resourceId: string; allocationId: string | null; allocationStatus: string | null; quantity: number | null; totalQuantity: number | null; availableQuantity: number | null; version: number | null; }
export interface OperationIdentity { operationId: string; idempotencyKey: string; }

const data = <T>(path: string, method: "GET" | "POST" = "GET", body?: unknown) =>
  request<T>(path, method, body).then(result => result.data);

export const identityApi = {
  csrf: () => data<{ csrfToken: string }>("/api/v2/auth/csrf"),
  session: () => data<SessionView>("/api/v2/session"),
  organizations: () => data<OrganizationView[]>("/api/v2/orgs"),
  selectOrganization: (organizationId: string) => data<SessionView>("/api/v2/session/active-org", "POST", { organizationId }),
  login: (username: string, password: string) => data<LoginResult>("/api/v2/auth/login", "POST", { username, password }),
  logout: () => data<void>("/api/v2/auth/logout", "POST"),
  recover: (username: string, oneTimeCode: string, newPassword: string) => data<{ message: string }>("/api/v2/auth/recover", "POST", { username, oneTimeCode, newPassword }),
  redeemInvitation: (body: { bearerToken: string; username?: string; password?: string; displayName?: string }) =>
    data<{ organizationId: string; membershipId: string; role: Role; newAccount: boolean; recoveryCodes: string[]; session: SessionView | null; csrfToken: string | null }>("/api/v2/invitations/redeem", "POST", body),
  account: () => data<AccountView>("/api/v2/account"),
  changePassword: (currentPassword: string, newPassword: string) => data<{ message: string }>("/api/v2/account/password", "POST", { currentPassword, newPassword }),
  rotateRecoveryCodes: (currentPassword: string) => data<RecoveryCodes>("/api/v2/account/recovery-codes/rotate", "POST", { currentPassword }),
  disableAccount: (currentPassword: string) => data<void>("/api/v2/account/disable", "POST", { currentPassword }),
  members: (orgId: string, after?: string, signal?: AbortSignal) =>
    request<MemberPage>(`/api/v2/orgs/${encodeURIComponent(orgId)}/members?limit=100${after ? `&cursor=${encodeURIComponent(after)}` : ""}`, "GET", undefined, { signal }).then(result => result.data),
  invite: (orgId: string, role: Exclude<Role, "OWNER">) => request<Invitation>(`/api/v2/orgs/${encodeURIComponent(orgId)}/invitations`, "POST", { role }),
  changeRole: (orgId: string, member: GovernanceMember, role: Exclude<Role, "OWNER">) =>
    request<GovernanceMember>(`/api/v2/orgs/${encodeURIComponent(orgId)}/members/${encodeURIComponent(member.id)}/role`, "POST", { role }, { headers: { "If-Match": `"v${member.version}"` } }),
  suspendMember: (orgId: string, member: GovernanceMember) =>
    request<GovernanceMember>(`/api/v2/orgs/${encodeURIComponent(orgId)}/members/${encodeURIComponent(member.id)}/suspend`, "POST", {}, { headers: { "If-Match": `"v${member.version}"` } }),
  startOwnershipTransfer: (orgId: string, toMembershipId: string, currentPassword: string) =>
    request<{ id: string; status: string; version: number }>(`/api/v2/orgs/${encodeURIComponent(orgId)}/transfer-ownership`, "POST", { toMembershipId, currentPassword }),
  acceptOwnershipTransfer: (orgId: string, transferId: string, etag: string) =>
    request<{ id: string; status: string; version: number }>(`/api/v2/orgs/${encodeURIComponent(orgId)}/accept-transfer`, "POST", { transferId }, { headers: { "If-Match": etag } }),
};

export const inventoryApi = {
  resources: (orgId: string, limit = 100) => data<ResourcePage>(`/api/v2/orgs/${encodeURIComponent(orgId)}/resources?limit=${Math.max(1, Math.min(100, limit))}`),
  resource: (orgId: string, resourceId: string) => request<InventoryResource>(`/api/v2/orgs/${encodeURIComponent(orgId)}/resources/${encodeURIComponent(resourceId)}`, "GET"),
  allocations: (orgId: string, limit = 100) => data<AllocationPage>(`/api/v2/orgs/${encodeURIComponent(orgId)}/allocations?limit=${Math.max(1, Math.min(100, limit))}`),
  allocation: (orgId: string, allocationId: string) => request<AllocationState>(`/api/v2/orgs/${encodeURIComponent(orgId)}/allocations/${encodeURIComponent(allocationId)}`, "GET"),
  movements: (orgId: string, resourceId: string, limit = 50, cursor?: string | null, signal?: AbortSignal) => {
    const query = new URLSearchParams({ limit: String(limit) });
    if (cursor) query.set("after", cursor);
    const path = "/api/v2/orgs/" + encodeURIComponent(orgId) + "/resources/" + encodeURIComponent(resourceId) + "/movements?" + query;
    return request<MovementPage>(path, "GET", undefined, { signal }).then(result => result.data);
  },
  receipt: (orgId: string, operationId: string) => request<OperationReceipt | OperationView>(`/api/v2/orgs/${encodeURIComponent(orgId)}/operations/${encodeURIComponent(operationId)}`, "GET"),
  removeImage: (orgId: string, resourceId: string, etag: string) =>
    request<InventoryResource>(`/api/v2/orgs/${encodeURIComponent(orgId)}/resources/${encodeURIComponent(resourceId)}/image/delete`, "POST", undefined, { headers: { "If-Match": etag } }),
  receive: (orgId: string, resourceId: string, quantity: number, etag: string, identity: OperationIdentity) =>
    inventoryPost<OperationView>(orgId, `resources/${resourceId}/receive`, { operationId: identity.operationId, quantity }, etag, identity),
  adjust: (orgId: string, resourceId: string, newTotalQuantity: number, etag: string, identity: OperationIdentity) =>
    inventoryPost<OperationView>(orgId, `resources/${resourceId}/adjust`, { operationId: identity.operationId, newTotalQuantity }, etag, identity),
  transition: (orgId: string, allocationId: string, action: "approve" | "issue" | "return" | "consume" | "cancel", etag: string, identity: OperationIdentity) =>
    inventoryPost<OperationView>(orgId, `allocations/${allocationId}/${action}`, { operationId: identity.operationId }, etag, identity),
  uploadImage: (numericResourceId: number, file: File) => {
    const form = new FormData(); form.append("file", file);
    return request<{ resourceId: number; contentType: string; sizeBytes: number; sha256: string }>(`/api/resources/${numericResourceId}/image`, "POST", form, { multipart: true });
  },
  imageUrl: (numericResourceId: number) => `/api/resources/${numericResourceId}/image`,
};

function inventoryPost<T>(orgId: string, suffix: string, body: unknown, etag: string, identity: OperationIdentity): Promise<RequestResult<T>> {
  return request<T>(`/api/v2/orgs/${encodeURIComponent(orgId)}/${suffix}`, "POST", body, {
    headers: { "If-Match": etag, "Idempotency-Key": identity.idempotencyKey },
  });
}

export function newOperationIdentity(): OperationIdentity {
  const random = new Uint8Array(32);
  crypto.getRandomValues(random);
  return { operationId: crypto.randomUUID(), idempotencyKey: Array.from(random, byte => byte.toString(16).padStart(2, "0")).join("") };
}
