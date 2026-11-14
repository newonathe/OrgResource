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
} from "./types";

export class ApiError extends Error {
  constructor(
    readonly status: number,
    readonly code: string,
    message: string,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

async function request<T>(
  path: string,
  method: "GET" | "POST",
  body?: unknown,
  signal?: AbortSignal,
): Promise<T> {
  let response: Response;
  try {
    response = await fetch(`/api${path}`, {
      method,
      signal,
      headers: body === undefined
        ? { Accept: "application/json" }
        : { Accept: "application/json", "Content-Type": "application/json" },
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch (error) {
    if (error instanceof DOMException && error.name === "AbortError") throw error;
    throw new ApiError(0, "NETWORK_ERROR", "The OrgResource API could not be reached. Check that the backend is running, then retry.");
  }

  const text = await response.text();
  let parsed: unknown = null;
  if (text.length > 0) {
    try {
      parsed = JSON.parse(text) as unknown;
    } catch {
      throw new ApiError(response.status, "INVALID_SERVER_RESPONSE", "The server returned a response that was not valid JSON.");
    }
  }

  if (!response.ok) {
    const error = parsed && typeof parsed === "object" ? parsed as Partial<ApiErrorBody> : {};
    throw new ApiError(
      response.status,
      typeof error.code === "string" ? error.code : "REQUEST_FAILED",
      typeof error.message === "string" ? error.message : "The request could not be completed.",
    );
  }
  return parsed as T;
}

function get<T>(path: string, signal?: AbortSignal): Promise<T> {
  return request<T>(path, "GET", undefined, signal);
}

function post<T>(path: string, body?: unknown): Promise<T> {
  return request<T>(path, "POST", body);
}

export const api = {
  departments: (signal?: AbortSignal) => get<Department[]>("/departments", signal),
  createDepartment: (name: string) => post<Department>("/departments", { name }),
  updateDepartment: (id: number, name: string) => post<Department>(`/departments/${id}/update`, { name }),
  deleteDepartment: (id: number) => post<DeleteResponse>(`/departments/${id}/delete`),

  projects: (departmentId?: number, signal?: AbortSignal) =>
    get<Project[]>(`/projects${departmentId ? `?departmentId=${encodeURIComponent(departmentId)}` : ""}`, signal),
  createProject: (body: { departmentId: number; name: string; startAt: string; endAt: string }) =>
    post<Project>("/projects", body),
  updateProject: (id: number, body: { name: string; startAt: string; endAt: string }) =>
    post<Project>(`/projects/${id}/update`, body),
  deleteProject: (id: number) => post<DeleteResponse>(`/projects/${id}/delete`),
  resources: (type?: ResourceType, signal?: AbortSignal) =>
    get<Resource[]>(`/resources${type ? `?type=${encodeURIComponent(type)}` : ""}`, signal),
  createResource: (body: { name: string; type: ResourceType; totalQuantity: number }) =>
    post<Resource>("/resources", body),
  updateResource: (id: number, body: { name: string; totalQuantity: number; expectedTotalQuantity: number }) =>
    post<Resource>(`/resources/${id}/update`, body),
  deleteResource: (id: number) => post<DeleteResponse>(`/resources/${id}/delete`),
  resourceAvailability: (id: number, signal?: AbortSignal) =>
    get<ResourceAvailability>(`/resources/${id}/availability`, signal),

  requirements: (projectId?: number, signal?: AbortSignal) =>
    get<ResourceRequirement[]>(`/requirements${projectId ? `?projectId=${encodeURIComponent(projectId)}` : ""}`, signal),
  createRequirement: (body: { projectId: number; resourceId: number; quantity: number }) =>
    post<ResourceRequirement>("/requirements", body),
  updateRequirement: (id: number, quantity: number) =>
    post<ResourceRequirement>(`/requirements/${id}/update`, { quantity }),
  deleteRequirement: (id: number) => post<DeleteResponse>(`/requirements/${id}/delete`),

  allocations: (filters: { requirementId?: number; status?: Allocation["status"] } = {}, signal?: AbortSignal) => {
    const query = new URLSearchParams();
    if (filters.requirementId) query.set("requirementId", String(filters.requirementId));
    if (filters.status) query.set("status", filters.status);
    const suffix = query.size ? `?${query.toString()}` : "";
    return get<Allocation[]>(`/allocations${suffix}`, signal);
  },
  createAllocation: (body: {
    requirementId: number;
    quantity: number;
    borrowerName: string;
    phoneNumber?: string | null;
    smsConsent?: boolean;
  }) => post<Allocation>("/allocations", body),
  approveAllocation: (id: number) => post<Allocation>(`/allocations/${id}/approve`),
  issueAllocation: (id: number) => post<Allocation>(`/allocations/${id}/issue`),
  returnAllocation: (id: number) => post<Allocation>(`/allocations/${id}/return`),
  consumeAllocation: (id: number) => post<Allocation>(`/allocations/${id}/consume`),
  cancelAllocation: (id: number) => post<Allocation>(`/allocations/${id}/cancel`),
  deleteAllocation: (id: number) => post<DeleteResponse>(`/allocations/${id}/delete`),
  revokeSmsConsent: (id: number) => post<Allocation>(`/allocations/${id}/revoke-sms-consent`),
};
