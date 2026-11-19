export type ResourceType = "REUSABLE" | "CONSUMABLE";
export type AllocationStatus = "PENDING" | "APPROVED" | "ISSUED" | "RETURNED" | "CONSUMED" | "CANCELLED";
export type ReminderOutcome = "NONE" | "CLAIMED" | "DISPATCHING" | "KEY_UNAVAILABLE" | "INVALID_CONTACT" | "SUPPRESSED" | "SIMULATED" | "PROVIDER_ACCEPTED" | "REJECTED" | "UNKNOWN";
export type CancellationReason = "USER_REQUEST" | "EXPIRED";

export interface Department {
  id: number;
  name: string;
}

export interface Project {
  id: number;
  departmentId: number;
  name: string;
  startAt: string;
  endAt: string;
}

export interface Resource {
  id: number;
  publicId: string;
  name: string;
  type: ResourceType;
  totalQuantity: number;
  availableQuantity: number;
}

export interface ResourceAvailability {
  resourceId: number;
  availableQuantity: number;
}

export interface ResourceRequirement {
  id: number;
  projectId: number;
  resourceId: number;
  quantity: number;
  allocatedQuantity: number;
  shortageQuantity: number;
}

export interface Allocation {
  id: number;
  publicId: string;
  requirementId: number;
  quantity: number;
  borrowerName: string;
  contactPresent: boolean;
  smsConsent: boolean;
  status: AllocationStatus;
  createdAt: string;
  approvedAt: string | null;
  issuedAt: string | null;
  returnedAt: string | null;
  consumedAt: string | null;
  cancelledAt: string | null;
  cancellationReason: CancellationReason | null;
  expiredPendingCleanup: boolean;
  reminderAttempted: boolean;
  reminderOutcome: ReminderOutcome;
}

export interface ApiErrorBody {
  code: string;
  message: string;
}

export interface DeleteResponse {
  message: "Record deleted.";
}


export interface WorkspaceOverview {
  sampledAt: string;
  departmentCount: number;
  projectActiveCount: number;
  resourceTypeCount: number;
  requirementShortageCount: number;
  resourcePreview: Resource[];
  calendarPreview: { project: Project; departmentName: string; active: boolean }[];
  allocations: {
    pending: number;
    issued: number;
    overdue: number;
    consumed: number;
    recent: {
      id: number;
      borrowerName: string;
      quantity: number;
      status: AllocationStatus;
      resourceName: string;
      createdAt: string;
    }[];
  } | null;
}
