import type { Allocation, Project, ReminderOutcome } from "./api/types";
import { ApiError } from "./api/client";

const manilaFormatter = new Intl.DateTimeFormat("en-CA", {
  timeZone: "Asia/Manila",
  year: "numeric",
  month: "2-digit",
  day: "2-digit",
  hour: "2-digit",
  minute: "2-digit",
  second: "2-digit",
  hourCycle: "h23",
});

export function manilaWallTime(date = new Date()): string {
  const parts = Object.fromEntries(manilaFormatter.formatToParts(date).map((part) => [part.type, part.value]));
  return `${parts.year}-${parts.month}-${parts.day}T${parts.hour}:${parts.minute}:${parts.second}`;
}

export function inputDateTime(value: string): string {
  return value.length === 16 ? `${value}:00` : value;
}

export function toDateTimeInput(value: string): string {
  return value.slice(0, 19);
}

function parseManila(value: string): Date {
  return new Date(/[zZ]|[+-]\d\d:\d\d$/.test(value) ? value : `${value}+08:00`);
}

export function formatManila(value: string | null | undefined): string {
  if (!value) return "Not recorded";
  const date = parseManila(value);
  if (!Number.isFinite(date.getTime())) return value;
  return new Intl.DateTimeFormat("en-PH", {
    timeZone: "Asia/Manila",
    dateStyle: "medium",
    timeStyle: "short",
  }).format(date);
}

export function projectPeriod(project: Project, now = manilaWallTime()): "UPCOMING" | "ACTIVE" | "ENDED" {
  if (project.startAt > now) return "UPCOMING";
  if (project.endAt > now) return "ACTIVE";
  return "ENDED";
}

export type AllocationAction = "approve" | "issue" | "return" | "consume" | "cancel" | "delete" | "revoke";

export function allocationActions(allocation: Allocation, type: "REUSABLE" | "CONSUMABLE" | undefined): AllocationAction[] {
  if (allocation.status === "PENDING") return ["approve", "cancel", ...(allocation.approvedAt === null ? ["delete"] : [])] as AllocationAction[];
  if (allocation.status === "APPROVED") return type === "REUSABLE" ? ["issue", "cancel"] : ["consume", "cancel"];
  if (allocation.status === "ISSUED" && type === "REUSABLE") return ["return"];
  if (allocation.status === "CANCELLED" && allocation.approvedAt === null) return ["delete"];
  return [];
}

export function reminderLabel(outcome: ReminderOutcome): string {
  switch (outcome) {
    case "SIMULATED": return "Accepted by local simulator";
    case "PROVIDER_ACCEPTED": return "Provider accepted request";
    case "UNKNOWN": return "Outcome uncertain";
    case "SUPPRESSED": return "Reminder suppressed";
    case "REJECTED": return "Provider rejected request";
    case "CLAIMED": return "Reminder claimed";
    case "NONE": return "No reminder attempt";
  }
}

export function isFictionalTestPhone(value: string): boolean {
  return /^\+120255501\d{2}$/.test(value.trim());
}

const knownMessages: Record<string, string> = {
  DUPLICATE_NAME: "A record with that normalized name already exists.",
  DUPLICATE_REQUIREMENT: "This Project already has a Requirement for that Resource.",
  DEPENDENCY_EXISTS: "This record is still referenced by another saved record.",
  RESOURCE_NOT_EMPTY: "The Resource still has physical stock. Set its total to zero before deleting it.",
  STALE_RESOURCE_QUANTITY: "The server quantity changed. Review the refreshed values before saving again.",
  QUANTITY_BELOW_COMMITMENTS: "The requested quantity would fall below covered work or the largest live pending Allocation.",
  PROJECT_DATES_LOCKED: "Project dates cannot change after an Allocation has been approved. You can still edit the name.",
  PROJECT_ENDED: "This Project has ended, so the requested change is no longer allowed.",
  INSUFFICIENT_STOCK: "There is not enough available stock for this Allocation.",
  REQUIREMENT_ALREADY_COVERED: "This Requirement is already covered by current Allocations.",
  INVALID_ALLOCATION_STATE: "The Allocation changed or this action is not allowed for its current status.",
  OUTSIDE_PROJECT_PERIOD: "This action is only allowed while the Project is active.",
  HISTORY_PROTECTED: "This Allocation has protected history and cannot be deleted.",
  NOT_FOUND: "This record no longer exists. Refresh the list to see current data.",
  NETWORK_ERROR: "The Java API could not be reached. Check the backend, then retry the read.",
};

export function explainError(error: unknown): string {
  if (error instanceof ApiError) return knownMessages[error.code] ?? `${error.message} (${error.code}).`;
  if (error instanceof DOMException && error.name === "AbortError") return "";
  return "The request could not be completed. Refresh the current records and review the server response.";
}

export function isUncertain(error: unknown): boolean {
  return error instanceof ApiError && error.status === 0;
}
