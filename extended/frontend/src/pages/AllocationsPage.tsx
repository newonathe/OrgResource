import { useEffect, useMemo, useState, type FormEvent } from "react";
import { useSearchParams } from "react-router-dom";
import { api, ApiError } from "../api/client";
import { inventoryApi, newOperationIdentity, type OperationIdentity } from "../api/extended";
import { useAuth } from "../auth/AuthContext";
import type { Allocation, AllocationStatus, Project, Resource, ResourceRequirement } from "../api/types";
import { allocationActions, explainError, formatManila, isFictionalTestPhone, isUncertain, reminderLabel, type AllocationAction } from "../domain";
import { Button, DetailSheet, GlassPanel, LoadState, Modal, MutationReviewNotice, Notice, PageHeader, PanelHeading, StatusBadge } from "../ui";

type ActionName = AllocationAction;
interface PendingAction { allocation: Allocation; action: ActionName }
const statusOptions: AllocationStatus[] = ["PENDING", "APPROVED", "ISSUED", "RETURNED", "CONSUMED", "CANCELLED"];

function actionLabel(action: ActionName): string {
  return ({ approve: "Approve request", issue: "Issue batch", return: "Record usable return", consume: "Consume batch", cancel: "Cancel request", delete: "Delete request", revoke: "Withdraw reminder consent" })[action];
}

function confirmationLabel(action: ActionName): string {
  return action === "consume" ? "Confirm consumption" : `Confirm ${actionLabel(action).toLowerCase()}`;
}

function actionDescription(action: ActionName, allocation: Allocation, resourceName: string): string {
  switch (action) {
    case "approve": return `Approve ${allocation.quantity} ${resourceName} for ${allocation.borrowerName}. The server will verify current stock and Requirement coverage.`;
    case "issue": return `Record that the full batch of ${allocation.quantity} ${resourceName} has been handed to ${allocation.borrowerName}. Availability should not be deducted twice.`;
    case "return": return `Confirm that the entire batch of ${allocation.quantity} ${resourceName} was physically returned in usable condition. Partial, damaged, or lost returns are not represented by this workflow.`;
    case "consume": return `Confirm permanent consumption of the full batch of ${allocation.quantity} ${resourceName}. This reduces physical stock and cannot be undone.`;
    case "cancel": return `Cancel the ${allocation.status.toLowerCase()} request for ${allocation.borrowerName}. The server will decide whether the cancellation is allowed.`;
    case "delete": return `Delete Allocation A${allocation.id}? Only never-approved records may be removed; the server preserves protected history.`;
    case "revoke": return `Withdraw SMS reminder consent for ${allocation.borrowerName} and clear the stored contact. Inventory status will not change.`;
  }
}

export function AllocationsPage() {
  const { session, organizations } = useAuth();
  const activeOrgId = session?.activeOrgId ?? "";
  const activeRole = organizations.find(org => org.id === activeOrgId)?.role;
  const canCreateAllocation = activeRole === "OWNER" || activeRole === "ADMIN";
  const canOperateStock = activeRole === "OWNER" || activeRole === "CUSTODIAN";
  const [searchParams, setSearchParams] = useSearchParams();
  const [allocations, setAllocations] = useState<Allocation[]>([]);
  const [requirements, setRequirements] = useState<ResourceRequirement[]>([]);
  const [projects, setProjects] = useState<Project[]>([]);
  const [resources, setResources] = useState<Resource[]>([]);
  const [loadState, setLoadState] = useState<"loading" | "error" | "ready">("loading");
  const [loadError, setLoadError] = useState("");
  const statusFilter = (() => {
    const requested = searchParams.get("status");
    return statusOptions.includes(requested as AllocationStatus) ? requested as AllocationStatus : "ALL";
  })();
  const [requirementFilter, setRequirementFilter] = useState("ALL");
  const [reload, setReload] = useState(0);
  const [requirementId, setRequirementId] = useState("");
  const [quantity, setQuantity] = useState("1");
  const [borrowerName, setBorrowerName] = useState("");
  const [smsConsent, setSmsConsent] = useState(false);
  const [phoneNumber, setPhoneNumber] = useState("");
  const [pendingAction, setPendingAction] = useState<PendingAction | null>(null);
  const [selected, setSelected] = useState<Allocation | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [postReviewRequired, setPostReviewRequired] = useState(false);
  const [uncertainOperation, setUncertainOperation] = useState<{ allocation: Allocation; action: ActionName; identity: OperationIdentity; etag: string } | null>(null);
  const [receiptRetryAllowed, setReceiptRetryAllowed] = useState(false);
  const [receiptRecoveryMessage, setReceiptRecoveryMessage] = useState("");
  const [receiptRecoveryBusy, setReceiptRecoveryBusy] = useState(false);

  useEffect(() => {
    const controller = new AbortController();
    const filters = {
      status: statusFilter === "ALL" ? undefined : statusFilter as AllocationStatus,
      requirementId: requirementFilter === "ALL" ? undefined : Number(requirementFilter),
    };
    Promise.all([
      api.allocations(filters, controller.signal), api.requirements(undefined, controller.signal),
      api.projects(undefined, controller.signal), api.resources(undefined, controller.signal),
    ]).then(([allocationRows, requirementRows, projectRows, resourceRows]) => {
      setAllocations(allocationRows); setRequirements(requirementRows); setProjects(projectRows); setResources(resourceRows); setLoadState("ready"); setLoadError("");
    }).catch((reason: unknown) => {
      if (reason instanceof DOMException && reason.name === "AbortError") return;
      setLoadError(explainError(reason)); setLoadState("error");
    });
    return () => controller.abort();
  }, [statusFilter, requirementFilter, reload]);

  const requirementMap = useMemo(() => new Map(requirements.map((item) => [item.id, item])), [requirements]);
  const projectMap = useMemo(() => new Map(projects.map((item) => [item.id, item])), [projects]);
  const resourceMap = useMemo(() => new Map(resources.map((item) => [item.id, item])), [resources]);
  const selectedRequirementId = requirementId || String(requirements[0]?.id ?? "");
  const selectedRequirement = requirementMap.get(Number(selectedRequirementId));
  const resourceNameFor = (allocation: Allocation) => {
    const requirement = requirementMap.get(allocation.requirementId);
    return resourceMap.get(requirement?.resourceId ?? -1)?.name ?? `Resource ${requirement?.resourceId ?? "unknown"}`;
  };
  const projectFor = (allocation: Allocation) => {
    const requirement = requirementMap.get(allocation.requirementId);
    return projectMap.get(requirement?.projectId ?? -1);
  };
  const refresh = () => { setLoadState("loading"); setReload((value) => value + 1); };

  async function createAllocation(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const amount = Number(quantity);
    const validPhone = isFictionalTestPhone(phoneNumber);
    if (busy || postReviewRequired || !selectedRequirementId || !borrowerName.trim() || !Number.isInteger(amount) || amount < 1 || amount > 1_000_000 || (smsConsent && !validPhone)) return;
    setBusy(true); setError(""); setNotice("");
    try {
      await api.createAllocation({ requirementId: Number(selectedRequirementId), quantity: amount, borrowerName: borrowerName.trim(), phoneNumber: smsConsent ? phoneNumber.trim() : null, smsConsent });
      setBorrowerName(""); setQuantity("1"); setSmsConsent(false); setPhoneNumber("");
      setNotice("Allocation request saved as PENDING. Stock and coverage were refreshed from the server."); refresh();
    } catch (reason) {
      if (isUncertain(reason)) { setPostReviewRequired(true); refresh(); }
      else setError(explainError(reason));
    }
    finally { setBusy(false); }
  }

  async function recoverUncertainOperation() {
    if (!uncertainOperation || !activeOrgId) return;
    setReceiptRecoveryBusy(true); setReceiptRecoveryMessage("");
    try {
      const result = await inventoryApi.receipt(activeOrgId, uncertainOperation.identity.operationId);
      const receipt = result.data;
      setReceiptRecoveryMessage("kind" in receipt
        ? `Permanent movement receipt recovered: ${receipt.kind}, ${receipt.quantity} units.`
        : `Temporary operation receipt recovered: ${receipt.action}, ${receipt.allocationStatus ?? "confirmed"}.`);
      setUncertainOperation(null); setReceiptRetryAllowed(false); refresh();
    } catch (reason) {
      if (reason instanceof ApiError && reason.status === 404) {
        setReceiptRecoveryMessage("No receipt is visible yet. The original request may still commit. Review the refreshed Allocation before retrying the original request.");
        setReceiptRetryAllowed(true); refresh();
      } else if (reason instanceof ApiError && reason.status === 409 && reason.code === "IDEMPOTENCY_KEY_EXPIRED") {
        setReceiptRecoveryMessage("The operation receipt expired. Permanent stock movements remain, but this transition cannot be safely retried from the page. Reconcile current state with an authorized custodian.");
      } else setReceiptRecoveryMessage(explainError(reason));
    } finally { setReceiptRecoveryBusy(false); }
  }

  async function performAction() {
    if (busy || postReviewRequired || !pendingAction) return;
    const { allocation, action } = pendingAction;
    setBusy(true); setError(""); setNotice("");
    let sentAttempt: typeof uncertainOperation = null;
    try {
      if (["approve", "issue", "return", "consume", "cancel"].includes(action)) {
        if (!canOperateStock || !allocation.publicId || !activeOrgId) throw new Error("Your current role cannot perform this lifecycle action.");
        const retry = uncertainOperation?.allocation.id === allocation.id && uncertainOperation.action === action && receiptRetryAllowed;
        if (uncertainOperation && !retry) throw new Error("Recover the uncertain operation receipt before another lifecycle request.");
        const identity = retry ? uncertainOperation.identity : newOperationIdentity();
        const etag = retry ? uncertainOperation.etag : (await inventoryApi.allocation(activeOrgId, allocation.publicId)).etag;
        if (!etag) throw new Error("The current Allocation ETag is missing. Reload and review this record.");
        sentAttempt = { allocation, action, identity, etag };
        await inventoryApi.transition(activeOrgId, allocation.publicId, action as "approve" | "issue" | "return" | "consume" | "cancel", etag, identity);
        setUncertainOperation(null); setReceiptRetryAllowed(false); setReceiptRecoveryMessage("");
      } else if (action === "delete") await api.deleteAllocation(allocation.id);
      else await api.revokeSmsConsent(allocation.id);
      setNotice(`${actionLabel(action)} confirmed by the server.`); setPendingAction(null);
      if (selected?.id === allocation.id) setSelected(null);
      refresh();
    } catch (reason) {
      if (isUncertain(reason)) {
        setPostReviewRequired(true);
        if (sentAttempt) {
          setUncertainOperation(sentAttempt);
          setReceiptRetryAllowed(false);
          setReceiptRecoveryMessage("The response was uncertain. Recover the saved receipt before any retry; the page did not automatically repeat the transition.");
        }
        setPendingAction(null);
      } else {
        setError(explainError(reason));
        if (reason instanceof ApiError && reason.status === 412) {
          setNotice("This Allocation changed. Fresh records are being loaded; review them before requesting another transition.");
          setUncertainOperation(null); setReceiptRetryAllowed(false);
        }
      }
      refresh();
    } finally { setBusy(false); }
  }

  const phoneInvalid = smsConsent && phoneNumber.length > 0 && !isFictionalTestPhone(phoneNumber);

  return (
    <section className="page-shell" aria-labelledby="allocations-title">
      <PageHeader eyebrow="Workspace · custody and requests" title="Allocations" description="Review whole-batch requests and record each server-validated inventory transition."
        action={<div className="header-stat"><span>{loadState === "ready" ? allocations.length : "—"}</span><small>batches in view</small></div>} />
      {notice && <Notice tone="success">{notice}</Notice>}{error && <Notice tone="danger">{error}</Notice>}
      <MutationReviewNotice required={postReviewRequired} ready={loadState === "ready"} onReviewed={() => setPostReviewRequired(false)} />
      {uncertainOperation && <Notice tone="warning"><div className="operation-recovery">
        <span>{receiptRecoveryMessage || `Operation ${uncertainOperation.identity.operationId} may have committed. Recover its server receipt before retrying.`}</span>
        <Button disabled={receiptRecoveryBusy} onClick={() => void recoverUncertainOperation()}>{receiptRecoveryBusy ? "Checking…" : "Recover operation receipt"}</Button>
        {receiptRetryAllowed && !postReviewRequired && <Button variant="secondary" disabled={busy} onClick={() => setPendingAction(uncertainOperation)}>Retry same operation safely</Button>}
      </div></Notice>}
      <div className="allocation-layout">
        {canCreateAllocation ? <GlassPanel className="form-panel allocation-create-panel" tilt={false}>
          <PanelHeading kicker="New request" title="Request a resource" />
          <form className="stack-form" onSubmit={createAllocation}>
            <label htmlFor="allocation-requirement">Project Requirement</label><select id="allocation-requirement" value={selectedRequirementId} onChange={(event) => setRequirementId(event.target.value)} required>
              {requirements.length === 0 && <option value="">No Requirements available</option>}{requirements.map((item) => {
                const project = projectMap.get(item.projectId); const resource = resourceMap.get(item.resourceId);
                return <option key={item.id} value={item.id}>{project?.name ?? `Project ${item.projectId}`} · {resource?.name ?? `Resource ${item.resourceId}`} · {item.shortageQuantity} short</option>;
              })}
            </select>
            {selectedRequirement && <div className="selection-summary"><span>Current server shortage</span><strong>{selectedRequirement.shortageQuantity.toLocaleString()} units</strong></div>}
            <label htmlFor="allocation-quantity">Quantity requested</label><input id="allocation-quantity" type="number" min="1" max="1000000" step="1" value={quantity} onChange={(event) => setQuantity(event.target.value)} required />
            <label htmlFor="allocation-borrower">Borrower name</label><input id="allocation-borrower" maxLength={100} value={borrowerName} onChange={(event) => setBorrowerName(event.target.value)} required />
            <label className="checkbox-field"><input type="checkbox" checked={smsConsent} onChange={(event) => setSmsConsent(event.target.checked)} /> <span>Give consent for a local reminder simulation</span></label>
            {smsConsent && <><label htmlFor="allocation-phone">Fictional simulator test contact</label><input id="allocation-phone" type="tel" inputMode="tel" autoComplete="off" placeholder="+12025550100" value={phoneNumber} onChange={(event) => setPhoneNumber(event.target.value)} aria-invalid={phoneInvalid} required />
              <p className="field-hint">Only the fictional reserved range +12025550100 through +12025550199 is accepted. Never enter a real student contact. This local academic setup does not deliver SMS.</p>{phoneInvalid && <p className="field-error" role="alert">Enter a fictional test number from +12025550100 through +12025550199.</p>}</>}
            <Button variant="primary" magnetic type="submit" disabled={busy || postReviewRequired || !selectedRequirementId || !borrowerName.trim() || (smsConsent && !isFictionalTestPhone(phoneNumber))}>{busy ? "Saving…" : "Submit request"}</Button>
            <p className="field-hint">A new request is PENDING and does not reserve stock until approved.</p>
          </form>
        </GlassPanel> : <GlassPanel className="form-panel allocation-create-panel" tilt={false}>
          <PanelHeading kicker="Allocation review" title="Review existing requests" />
          <p>Owners and administrators create borrower requests. Custodians can review existing requests and record stock transitions.</p>
        </GlassPanel>}

        <GlassPanel className="list-panel allocation-list-panel" tilt={false}>
          <PanelHeading kicker="Current state" title="Allocation batches"><Button variant="quiet" disabled={loadState === "loading"} onClick={refresh}>Refresh all data</Button></PanelHeading>
          <div className="search-filter-bar allocation-filters"><label htmlFor="allocation-status-filter">Status</label><select id="allocation-status-filter" value={statusFilter} onChange={(event) => {
            const value = event.target.value as AllocationStatus | "ALL";
            setLoadState("loading");
            const params = new URLSearchParams(searchParams);
            if (value === "ALL") params.delete("status"); else params.set("status", value);
            setSearchParams(params);
          }}><option value="ALL">All statuses</option>{statusOptions.map((status) => <option key={status} value={status}>{status}</option>)}</select>
            <label htmlFor="allocation-requirement-filter">Requirement</label><select id="allocation-requirement-filter" value={requirementFilter} onChange={(event) => { setLoadState("loading"); setRequirementFilter(event.target.value); }}><option value="ALL">All requirements</option>{requirements.map((item) => <option key={item.id} value={item.id}>Q{item.id} · {resourceMap.get(item.resourceId)?.name ?? `Resource ${item.resourceId}`}</option>)}</select></div>
          <LoadState state={loadState} error={loadError} onRetry={refresh}>
            {allocations.length === 0 ? <div className="state-block state-empty"><span className="empty-symbol" aria-hidden="true">⇄</span><h3>No Allocation batches</h3><p>Requests will appear here after the server confirms them.</p></div>
              : <div className="allocation-list">{allocations.map((allocation) => {
                const requirement = requirementMap.get(allocation.requirementId); const resource = resourceMap.get(requirement?.resourceId ?? -1);
                const project = projectFor(allocation); const relationshipUnavailable = !requirement || !resource;
                const allowed = !canOperateStock || relationshipUnavailable ? [] : allocationActions(allocation, resource.type);
                return <article className="allocation-card" key={allocation.id}>
                  {allocation.expiredPendingCleanup && <div className="expired-banner">Project ended · expiry cleanup is pending</div>}
                  {relationshipUnavailable && <Notice tone="warning">Requirement or Resource details are unavailable. Refresh the page before taking an Allocation action.</Notice>}
                  <div className="allocation-card-heading"><div className="allocation-card-mark" aria-hidden="true">⇄</div><div className="allocation-card-title"><p className="section-kicker">{project?.name ?? `Project ${requirement?.projectId ?? "unknown"}`}</p><h3>{allocation.borrowerName}</h3><span>{resource?.name ?? `Resource ${requirement?.resourceId ?? "unknown"}`} · {allocation.quantity.toLocaleString()} units</span></div><StatusBadge tone={allocation.status === "PENDING" ? "warning" : allocation.status === "APPROVED" || allocation.status === "ISSUED" ? "blue" : allocation.status === "RETURNED" || allocation.status === "CONSUMED" ? "success" : "neutral"}>{allocation.status.toLowerCase()}</StatusBadge></div>
                  <div className="allocation-meta"><span>Project period <strong>{project ? `${formatManila(project.startAt)} — ${formatManila(project.endAt)}` : "Details unavailable"}</strong></span><span>Requirement <strong>{requirement ? `${requirement.quantity} required · ${requirement.allocatedQuantity} covered` : `Q${allocation.requirementId}`}</strong></span><span>Created <strong>{formatManila(allocation.createdAt)}</strong></span></div>
                  <div className="allocation-privacy"><span>Reminder contact: <strong>{allocation.contactPresent ? "Present (redacted)" : "Not recorded"}</strong></span><span>Consent: <strong>{allocation.smsConsent ? "Granted" : "Not granted"}</strong></span><span>Reminder: <strong>{reminderLabel(allocation.reminderOutcome)}</strong></span></div>
                  <div className="allocation-card-footer"><Button variant="quiet" aria-label={`Details for Allocation ${allocation.id}`} onClick={() => setSelected(allocation)}>Details</Button>
                    {canOperateStock && allocation.smsConsent && allocation.contactPresent && <Button variant="quiet" aria-label={`Withdraw reminder consent for Allocation ${allocation.id}`} disabled={busy || postReviewRequired || relationshipUnavailable} onClick={() => setPendingAction({ allocation, action: "revoke" })}>Withdraw consent</Button>}
                    <div className="row-actions">{allowed.map((action) => <Button key={action} variant={action === "consume" || action === "delete" ? "danger" : action === "approve" ? "primary" : "secondary"} disabled={busy || postReviewRequired}
                      aria-label={`${actionLabel(action)} for Allocation ${allocation.id}`} onClick={() => setPendingAction({ allocation, action })}>{actionLabel(action)}</Button>)}</div>
                  </div>
                </article>;
              })}</div>}
          </LoadState>
        </GlassPanel>
      </div>

      {pendingAction && <Modal title={actionLabel(pendingAction.action)} onClose={() => { if (!busy) setPendingAction(null); }}>
        <p className="modal-copy">{actionDescription(pendingAction.action, pendingAction.allocation, resourceNameFor(pendingAction.allocation))}</p>
        <div className="modal-actions"><Button variant="quiet" disabled={busy} onClick={() => setPendingAction(null)}>Go back</Button><Button variant={pendingAction.action === "consume" || pendingAction.action === "delete" || pendingAction.action === "cancel" ? "danger" : "primary"} disabled={busy || postReviewRequired} onClick={() => void performAction()}>{busy ? "Waiting for server…" : confirmationLabel(pendingAction.action)}</Button></div>
      </Modal>}
      {selected && <DetailSheet title={`Allocation A${selected.id}`} onClose={() => setSelected(null)}>
        <div className="detail-grid"><div><span>Borrower</span><strong>{selected.borrowerName}</strong></div><div><span>Resource</span><strong>{resourceNameFor(selected)}</strong></div><div><span>Quantity</span><strong>{selected.quantity.toLocaleString()} whole units</strong></div><div><span>Status</span><strong>{selected.status}</strong></div><div><span>Consent</span><strong>{selected.smsConsent ? "Granted" : "Not granted"} · {selected.contactPresent ? "contact redacted" : "no contact"}</strong></div><div><span>Reminder outcome</span><strong>{reminderLabel(selected.reminderOutcome)}</strong></div></div>
        {selected.expiredPendingCleanup && <Notice tone="warning">The Project has ended. Cleanup is pending; the backend remains authoritative for the next transition.</Notice>}
        <section className="sheet-section"><h3>Lifecycle history</h3><dl className="timeline-list">{[["Created", selected.createdAt], ["Approved", selected.approvedAt], ["Issued", selected.issuedAt], ["Returned", selected.returnedAt], ["Consumed", selected.consumedAt], ["Cancelled", selected.cancelledAt]].map(([label, time]) => <div key={label}><dt>{label}</dt><dd>{formatManila(time)}</dd></div>)}</dl>
          {selected.cancellationReason && <p className="quiet-note">Cancellation reason: {selected.cancellationReason}</p>}
        </section>
        <p className="quiet-note">The read API never returns the raw phone number. No reminder status shown here means “Delivered” to a handset.</p>
      </DetailSheet>}
    </section>
  );
}
