import type {
  Allocation,
  ApiErrorBody,
  DeleteResponse,
  Department,
  Project,
  Resource,
  ResourceAvailability,
  ResourceRequirement,
  ResourceType,
  WorkspaceOverview,
} from "./types";

export class ApiError extends Error {
  constructor(readonly status: number, readonly code: string, message: string) {
    super(message);
    this.name = "ApiError";
  }
}

let csrfToken: string | null = null;
let unauthenticatedHandler: (() => void) | null = null;
let missingOrganizationHandler: (() => void) | null = null;
const activeRequests = new Set<AbortController>();

export function setCsrfToken(value: string | null): void { csrfToken = value; }
export function setUnauthenticatedHandler(handler: (() => void) | null): void { unauthenticatedHandler = handler; }
export function setMissingOrganizationHandler(handler: (() => void) | null): void { missingOrganizationHandler = handler; }
export function abortTenantRequests(): void {
  for (const controller of activeRequests) controller.abort();
  activeRequests.clear();
}

export interface WorkspacePage<T> { items: T[]; nextCursor: string | null; }

export interface RequestResult<T> { data: T; etag: string | null; status: number; }

export async function request<T>(
  path: string,
  method: "GET" | "POST",
  body?: unknown,
  options: { signal?: AbortSignal; headers?: Record<string, string>; multipart?: boolean;
    download?: (response: Response) => Promise<T> } = {},
): Promise<RequestResult<T>> {
  const controller = new AbortController();
  if (options.signal?.aborted) controller.abort();
  activeRequests.add(controller);
  const onAbort = () => controller.abort();
  options.signal?.addEventListener("abort", onAbort, { once: true });
  const headers: Record<string, string> = { Accept: "application/json", ...options.headers };
  if (method === "POST") {
    if (csrfToken) headers["X-CSRF-TOKEN"] = csrfToken;
    if (body !== undefined && !options.multipart) headers["Content-Type"] = "application/json";
  }
  let sessionEnded = false;
  let missingOrganization = false;
  const mutation = method === "POST" && !options.download;
  try {
    let response: Response;
    try {
      response = await fetch(path.startsWith("/api/") ? path : `/api${path}`, {
        method,
        credentials: "same-origin",
        cache: "no-store",
        signal: controller.signal,
        headers,
        body: body === undefined ? undefined : options.multipart ? body as BodyInit : JSON.stringify(body),
      });
    } catch (error) {
      if (controller.signal.aborted || (error instanceof DOMException && error.name === "AbortError")) throw error;
      throw mutation
        ? new ApiError(0, "POST_OUTCOME_UNCERTAIN", "The server may have completed this change, but its response was not received. Review current records or recover the operation receipt before trying again.")
        : new ApiError(0, "NETWORK_ERROR", "The OrgResource API could not be reached. Check that the backend is running, then retry.");
    }

  controller.signal.throwIfAborted();
  sessionEnded = response.status === 401 && !path.includes("/auth/login") && !path.includes("/auth/recover");
  const etag = response.headers.get("ETag");
  if (response.ok && options.download) {
    const downloaded = await options.download(response);
    controller.signal.throwIfAborted();
    return { data: downloaded, etag, status: response.status };
  }
  let text: string;
  try { text = await response.text(); }
  catch {
    controller.signal.throwIfAborted();
    throw mutation
      ? new ApiError(response.status, "POST_OUTCOME_UNCERTAIN", "The server may have completed this change, but the response could not be read. Review current records before deciding what to do next.")
      : new ApiError(response.status, "INVALID_SERVER_RESPONSE", "The server response could not be read.");
  }
  controller.signal.throwIfAborted();
  let parsed: unknown = null;
  if (text) {
    try { parsed = JSON.parse(text) as unknown; }
    catch {
      throw mutation
        ? new ApiError(response.status, "POST_OUTCOME_UNCERTAIN", "The server may have completed this change, but returned invalid confirmation data. Review current records before retrying.")
        : new ApiError(response.status, "INVALID_SERVER_RESPONSE", "The server returned a response that was not valid JSON.");
    }
  }
  if (!response.ok) {
    const error = parsed && typeof parsed === "object" ? parsed as Partial<ApiErrorBody> & { data?: ApiErrorBody } : {};
    const detail = error.data ?? error;
    missingOrganization = response.status === 409 && detail.code === "ORG_CONTEXT_REQUIRED";
    if (mutation && (response.status === 408 || response.status === 425 || response.status === 429 || response.status >= 500)) {
      throw new ApiError(response.status, "POST_OUTCOME_UNCERTAIN", "The server could not confirm the outcome. Review records or recover the operation receipt before trying again.");
    }
    throw new ApiError(
      response.status,
      typeof detail.code === "string" ? detail.code : response.status === 412 ? "STALE_VERSION" : "REQUEST_FAILED",
      typeof detail.message === "string" ? detail.message : messageForStatus(response.status),
    );
  }
  if (response.status === 204) return { data: undefined as T, etag, status: response.status };
  const envelope = parsed && typeof parsed === "object" && !Array.isArray(parsed) && "data" in parsed
    ? (parsed as { data: unknown }).data : parsed;
  if (method === "POST" && (envelope === null || envelope === undefined || (typeof envelope === "object" && Object.keys(envelope).length === 0))) {
    throw new ApiError(response.status, "POST_OUTCOME_UNCERTAIN", "The server may have completed this change, but returned no usable confirmation.");
  }
    return { data: envelope as T, etag, status: response.status };
  } finally {
    activeRequests.delete(controller);
    options.signal?.removeEventListener("abort", onAbort);
    if (sessionEnded && !controller.signal.aborted) unauthenticatedHandler?.();
    if (missingOrganization && !controller.signal.aborted) missingOrganizationHandler?.();
  }
}

function messageForStatus(status: number): string {
  if (status === 401) return "Your session ended. Sign in again.";
  if (status === 403) return "Your current organization role cannot perform this action.";
  if (status === 409) return "The organization context or current record state needs review.";
  if (status === 412) return "This record changed. Reload it and review before making another change.";
  if (status === 428) return "Reload the current record before submitting this operation.";
  if (status === 429 || status === 503) return "The service is temporarily limiting requests. Wait and retry after reviewing the current state.";
  return "The request could not be completed.";
}

function get<T>(path: string, signal?: AbortSignal): Promise<T> { return request<T>(path, "GET", undefined, { signal }).then(result => result.data); }
function post<T>(path: string, body?: unknown): Promise<T> { return request<T>(path, "POST", body).then(result => result.data); }

/** Original numeric-ID academic API. These routes remain isolated from the Extended UUID inventory API. */
export const api = {
  departments: (signal?: AbortSignal) => get<Department[]>("/departments", signal),
  createDepartment: (name: string) => post<Department>("/departments", { name }),
  updateDepartment: (id: number, name: string) => post<Department>(`/departments/${id}/update`, { name }),
  deleteDepartment: (id: number) => post<DeleteResponse>(`/departments/${id}/delete`),
  projects: (departmentId?: number, signal?: AbortSignal) => get<Project[]>(`/projects${departmentId ? `?departmentId=${encodeURIComponent(departmentId)}` : ""}`, signal),
  createProject: (body: { departmentId: number; name: string; startAt: string; endAt: string }) => post<Project>("/projects", body),
  updateProject: (id: number, body: { name: string; startAt: string; endAt: string }) => post<Project>(`/projects/${id}/update`, body),
  deleteProject: (id: number) => post<DeleteResponse>(`/projects/${id}/delete`),
  resources: (type?: ResourceType, signal?: AbortSignal) => get<Resource[]>(`/resources${type ? `?type=${encodeURIComponent(type)}` : ""}`, signal),
  createResource: (body: { name: string; type: ResourceType; totalQuantity: number }) => post<Resource>("/resources", body),
  updateResource: (id: number, body: { name: string; totalQuantity: number; expectedTotalQuantity: number }) => post<Resource>(`/resources/${id}/update`, body),
  deleteResource: (id: number) => post<DeleteResponse>(`/resources/${id}/delete`),
  resourceAvailability: (id: number, signal?: AbortSignal) => get<ResourceAvailability>(`/resources/${id}/availability`, signal),
  requirements: (projectId?: number, signal?: AbortSignal) => get<ResourceRequirement[]>(`/requirements${projectId ? `?projectId=${encodeURIComponent(projectId)}` : ""}`, signal),
  createRequirement: (body: { projectId: number; resourceId: number; quantity: number }) => post<ResourceRequirement>("/requirements", body),
  updateRequirement: (id: number, quantity: number) => post<ResourceRequirement>(`/requirements/${id}/update`, { quantity }),
  deleteRequirement: (id: number) => post<DeleteResponse>(`/requirements/${id}/delete`),
  allocations: (filters: { requirementId?: number; status?: Allocation["status"] } = {}, signal?: AbortSignal) => {
    const query = new URLSearchParams();
    if (filters.requirementId) query.set("requirementId", String(filters.requirementId));
    if (filters.status) query.set("status", filters.status);
    return get<Allocation[]>(`/allocations${query.size ? `?${query}` : ""}`, signal);
  },
  createAllocation: (body: { requirementId: number; quantity: number; borrowerName: string; phoneNumber?: string | null; smsConsent?: boolean }) => post<Allocation>("/allocations", body),
  approveAllocation: (id: number) => post<Allocation>(`/allocations/${id}/approve`),
  issueAllocation: (id: number) => post<Allocation>(`/allocations/${id}/issue`),
  returnAllocation: (id: number) => post<Allocation>(`/allocations/${id}/return`),
  consumeAllocation: (id: number) => post<Allocation>(`/allocations/${id}/consume`),
  cancelAllocation: (id: number) => post<Allocation>(`/allocations/${id}/cancel`),
  deleteAllocation: (id: number) => post<DeleteResponse>(`/allocations/${id}/delete`),
  revokeSmsConsent: (id: number) => post<Allocation>(`/allocations/${id}/revoke-sms-consent`),
};

type WorkspaceFilters = Record<string, string | number | null | undefined>;

function workspacePage<T>(path: string, cursor: string | null, filters: WorkspaceFilters, signal?: AbortSignal) {
  const query = new URLSearchParams();
  for (const [name, value] of Object.entries(filters)) {
    if (value != null && value !== "") query.set(name, String(value));
  }
  if (cursor !== null) query.set("cursor", cursor);
  return request<WorkspacePage<T>>("/v2/workspace/" + path + "?" + query, "GET", undefined, { signal })
    .then(({ data }) => {
      if (!Array.isArray(data?.items) || (data.nextCursor !== null && typeof data.nextCursor !== "string")) {
        throw new ApiError(502, "INVALID_SERVER_RESPONSE", "The server returned an invalid workspace page.");
      }
      return data;
    });
}

export const workspaceApi = {
  overview: (signal?: AbortSignal) => request<WorkspaceOverview>("/v2/workspace/overview", "GET", undefined, { signal })
    .then(({ data }) => {
      if (typeof data?.departmentCount !== "number" || !Array.isArray(data.resourcePreview) || !Array.isArray(data.calendarPreview)) {
        throw new ApiError(502, "INVALID_SERVER_RESPONSE", "The server returned an invalid Overview snapshot.");
      }
      return data;
    }),
  departments: (cursor: string | null, search?: string, signal?: AbortSignal) => workspacePage<Department>("departments", cursor, { search }, signal),
  projects: (cursor: string | null, filters: { departmentId?: number; search?: string; period?: string }, signal?: AbortSignal) =>
    workspacePage<Project>("projects", cursor, filters, signal),
  resources: (cursor: string | null, filters: { type?: ResourceType; search?: string }, signal?: AbortSignal) =>
    workspacePage<Resource>("resources", cursor, filters, signal),
  requirements: (cursor: string | null, filters: { projectId?: number; search?: string }, signal?: AbortSignal) =>
    workspacePage<ResourceRequirement>("requirements", cursor, filters, signal),
  allocations: (cursor: string | null, filters: { requirementId?: number; projectId?: number; status?: Allocation["status"] }, signal?: AbortSignal) =>
    workspacePage<Allocation>("allocations", cursor, filters, signal),
};
