// Proposed public DTO types, not implemented/tested React source.
export type ResourceType = "REUSABLE" | "CONSUMABLE";
export type AllocationStatus = "PENDING" | "APPROVED" | "ISSUED" | "RETURNED" | "CONSUMED" | "CANCELLED";
export type ReminderOutcome = "NONE" | "CLAIMED" | "SUPPRESSED" | "SIMULATED" | "PROVIDER_ACCEPTED" | "REJECTED" | "UNKNOWN";
export type CancellationReason = "USER_REQUEST" | "EXPIRED";
export interface Department { id:number; name:string }
export interface Project { id:number; departmentId:number; name:string; startAt:string; endAt:string }
export interface Resource { id:number; name:string; type:ResourceType; totalQuantity:number; availableQuantity:number }
export interface ResourceAvailability { resourceId:number; availableQuantity:number }
export interface ResourceRequirement { id:number; projectId:number; resourceId:number; quantity:number; allocatedQuantity:number; shortageQuantity:number }
export interface Allocation {
  id:number; requirementId:number; quantity:number; borrowerName:string;
  contactPresent:boolean; smsConsent:boolean; status:AllocationStatus;
  createdAt:string; approvedAt:string|null; issuedAt:string|null;
  returnedAt:string|null; consumedAt:string|null; cancelledAt:string|null;
  cancellationReason:CancellationReason|null; expiredPendingCleanup:boolean;
  reminderAttempted:boolean; reminderOutcome:ReminderOutcome;
}
export interface ApiErrorBody { code:string; message:string }
export interface DeleteResponse { message:"Record deleted." }
