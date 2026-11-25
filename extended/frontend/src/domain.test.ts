import { describe, expect, it } from "vitest";
import type { Allocation } from "./api/types";
import { allocationActions, formatManila, inputDateTime, isFictionalTestPhone, isUncertain, projectPeriod, reminderLabel, reminderWindow, toDateTimeInput } from "./domain";
import { ApiError } from "./api/client";

const allocation = (status: Allocation["status"], approvedAt: string | null = null): Allocation => ({
  id: 1, publicId: "00000000-0000-4000-8000-000000000001", requirementId: 1, quantity: 2, borrowerName: "Fictional Borrower", contactPresent: false, smsConsent: false,
  status, createdAt: "2026-11-15T09:00:00", approvedAt, issuedAt: null, returnedAt: null, consumedAt: null,
  cancelledAt: null, cancellationReason: null, expiredPendingCleanup: false, reminderAttempted: false, reminderOutcome: "NONE",
});

describe("display and lifecycle rules", () => {
  it("uses Philippine wall time with inclusive start and exclusive end", () => {
    const project = { id: 1, departmentId: 1, name: "Fictional Project", startAt: "2026-11-15T09:00:00", endAt: "2026-11-15T12:00:00" };
    expect(projectPeriod(project, "2026-11-15T08:59:59")).toBe("UPCOMING");
    expect(projectPeriod(project, "2026-11-15T09:00:00")).toBe("ACTIVE");
    expect(projectPeriod(project, "2026-11-15T11:59:59")).toBe("ACTIVE");
    expect(projectPeriod(project, "2026-11-15T12:00:00")).toBe("ENDED");
  });

  it("preserves local datetime input rather than converting through the browser timezone", () => {
    expect(inputDateTime("2026-11-15T09:30")).toBe("2026-11-15T09:30:00");
    expect(toDateTimeInput("2026-11-15T09:30:42")).toBe("2026-11-15T09:30:42");
    expect(formatManila("2026-11-15T09:30:00")).toContain("9:30");
  });

  it("exposes only legal Allocation actions for each lifecycle and Resource type", () => {
    expect(allocationActions(allocation("PENDING"), "REUSABLE")).toEqual(["approve", "cancel", "delete"]);
    expect(allocationActions(allocation("APPROVED", "2026-11-15T09:05:00"), "REUSABLE")).toEqual(["issue", "cancel"]);
    expect(allocationActions(allocation("APPROVED", "2026-11-15T09:05:00"), "CONSUMABLE")).toEqual(["consume", "cancel"]);
    expect(allocationActions(allocation("APPROVED", "2026-11-15T09:05:00"), undefined)).toEqual([]);
    expect(allocationActions(allocation("ISSUED"), "REUSABLE")).toEqual(["return"]);
    expect(allocationActions(allocation("ISSUED"), "CONSUMABLE")).toEqual([]);
    expect(allocationActions(allocation("CANCELLED", "2026-11-15T09:05:00"), "REUSABLE")).toEqual([]);
  });

  it("labels reminder outcomes without claiming handset delivery", () => {
    expect(reminderLabel("SIMULATED")).toBe("Accepted by local simulator");
    expect(reminderLabel("PROVIDER_ACCEPTED")).toBe("Provider accepted request");
    expect(reminderLabel("UNKNOWN")).toBe("Outcome uncertain");
    expect(reminderLabel("SUPPRESSED")).toBe("Reminder suppressed");
  });

  it("uses the backend reminder window lower-inclusive and upper-exclusive boundaries", () => {
    const now = "2026-11-15T09:00:00";
    const upper = "2026-11-16T09:00:00";
    expect(reminderWindow("2026-11-15T08:59:59", now, upper)).toBe("OVERDUE");
    expect(reminderWindow(now, now, upper)).toBe("DUE_SOON");
    expect(reminderWindow("2026-11-16T08:59:59", now, upper)).toBe("DUE_SOON");
    expect(reminderWindow(upper, now, upper)).toBe("LATER");
    expect(reminderWindow("2026-11-16T09:00:01", now, upper)).toBe("LATER");
  });

  it("accepts only the reserved fictional simulator contact range", () => {
    expect(isFictionalTestPhone("+12025550100")).toBe(true);
    expect(isFictionalTestPhone("+12025550199")).toBe(true);
    expect(isFictionalTestPhone("+12025550000")).toBe(false);
    expect(isFictionalTestPhone("+639171234567")).toBe(false);
  });

  it("distinguishes an uncertain POST outcome from a definitive server rejection", () => {
    expect(isUncertain(new ApiError(0, "POST_OUTCOME_UNCERTAIN", "uncertain"))).toBe(true);
    expect(isUncertain(new ApiError(409, "DEPENDENCY_EXISTS", "rejected"))).toBe(false);
  });
});
