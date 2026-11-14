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

  projects: (signal?: AbortSignal) => get<Project[]>("/projects", signal),
  resources: (type?: ResourceType, signal?: AbortSignal) =>
    get<Resource[]>(`/resources${type ? `?type=${encodeURIComponent(type)}` : ""}`, signal),
  createResource: (body: { name: string; type: ResourceType; totalQuantity: number }) =>
    post<Resource>("/resources", body),
  updateResource: (id: number, body: { name: string; totalQuantity: number; expectedTotalQuantity: number }) =>
    post<Resource>(`/resources/${id}/update`, body),
  deleteResource: (id: number) => post<DeleteResponse>(`/resources/${id}/delete`),
  resourceAvailability: (id: number, signal?: AbortSignal) =>
    get<ResourceAvailability>(`/resources/${id}/availability`, signal),

  requirements: (signal?: AbortSignal) => get<ResourceRequirement[]>("/requirements", signal),
  allocations: (signal?: AbortSignal) => get<Allocation[]>("/allocations", signal),
};
