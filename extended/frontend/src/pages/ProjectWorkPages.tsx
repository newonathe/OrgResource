import { useCallback, useEffect, useState, type FormEvent, type ReactNode } from "react";
import { workspaceApi, ApiError, type WorkspacePage } from "../api/client";
import { projectWorkApi, type MembershipOption, type ProjectRequirement, type ProjectWork } from "../api/project-work";
import type { Resource } from "../api/types";
import type { Role } from "../api/extended";
import { explainError, formatManila, inputDateTime, isUncertain, toDateTimeInput } from "../domain";
import { RelatedSelect } from "../RelatedSelect";
import { Button, GlassPanel, MutationReviewNotice, Notice, PageHeader, PageNavigation, PanelHeading, StatusBadge, usePageCursors } from "../ui";

const loadResources = (cursor: string | null, search: string, signal: AbortSignal) =>
  workspaceApi.resources(cursor, { search }, signal);

function useWorkPage<T>(scope: string, load: (cursor: string | null, signal: AbortSignal) => Promise<WorkspacePage<T>>, revision: number) {
  const nav = usePageCursors(scope);
  const [retry, setRetry] = useState(0);
  const [read, setRead] = useState<{ key: string; data?: WorkspacePage<T>; error?: string } | null>(null);
  const cursor = nav.cursor;
  const key = JSON.stringify([scope, cursor, revision, retry]);
  useEffect(() => {
    const controller = new AbortController();
    void load(cursor, controller.signal).then(data => {
      if (!controller.signal.aborted) setRead({ key, data });
    }).catch((reason: unknown) => {
      if (!controller.signal.aborted) setRead({ key, error: explainError(reason) });
    });
    return () => controller.abort();
  }, [cursor, key, load]);
  const current = read?.key === key ? read : null;
  return { rows: current?.data?.items ?? [], next: current?.data?.nextCursor ?? null, error: current?.error,
    loading: current === null, page: nav.page, move: nav.move, retry: () => setRetry(value => value + 1) };
}

function WorkRows<T>({ view, label, empty, children }: {
  view: ReturnType<typeof useWorkPage<T>>; label: string; empty: string; children: ReactNode;
}) {
  return <>
    {view.error && <><Notice tone="danger">{view.error}</Notice><Button onClick={view.retry}>Retry page</Button></>}
    {!view.loading && !view.error && (view.rows.length ? children : <p>{empty}</p>)}
    <PageNavigation label={label} page={view.page} count={view.rows.length} hasNext={view.next !== null}
      loading={view.loading} unavailable={!!view.error} onPrevious={() => view.move(null)}
      onNext={() => { if (view.next) view.move(view.next); }} />
  </>;
}

function useWorkActions(orgId: string, refresh: () => void) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [review, setReview] = useState(false);
  const [receiptId, setReceiptId] = useState<string | null>(null);
  async function run(action: () => Promise<unknown>, confirmation: string, operationId?: string) {
    if (busy || review) return;
    setBusy(true); setError(""); setMessage("");
    try { await action(); setMessage(confirmation); refresh(); }
    catch (reason) {
      if (isUncertain(reason)) { setReview(true); setReceiptId(operationId ?? null); refresh(); }
      else { setError(explainError(reason)); if (reason instanceof ApiError && reason.status === 412) refresh(); }
    } finally { setBusy(false); }
  }
  async function recover() {
    if (!receiptId || busy) return;
    setBusy(true); setError("");
    try {
      await projectWorkApi.receipt(orgId, receiptId);
      setMessage("The server confirmed the saved change.");
      setReview(false); setReceiptId(null); refresh();
    } catch (reason) { setError(explainError(reason)); }
    finally { setBusy(false); }
  }
  return { run, disabled: busy || review, feedback: (ready: boolean) => <>
    {error && <Notice tone="danger">{error}</Notice>}
    {message && <Notice tone="success">{message}</Notice>}
    <MutationReviewNotice required={review} ready={ready && !busy} onReviewed={() => { setReview(false); setReceiptId(null); }} />
    {receiptId && <Button disabled={busy} onClick={() => void recover()}>Check saved change receipt</Button>}
  </> };
}

export function ProjectWorkPage({ orgId, role }: { orgId: string; role: Role }) {
  const [selected, setSelected] = useState<ProjectWork | null>(null);
  const [revision, setRevision] = useState(0);
  const list = useWorkPage(orgId, useCallback((cursor, signal) => projectWorkApi.projects(orgId, cursor, signal), [orgId]), revision);
  const refresh = () => setRevision(value => value + 1);
  return <section className="page-shell">
    <PageHeader eyebrow="Organization" title="Project work"
      description={role === "LEAD" ? "Manage the Projects assigned to your membership." : "Manage Project assignments, Requirements and linked requests."} />
    <GlassPanel tilt={false} className="project-work-panel">
      <PanelHeading title="Available Projects" />
      <WorkRows view={list} label="Project work pages" empty={role === "LEAD" ? "No Projects are available for this membership." : "No Projects are available yet. Create a fictional Project from Projects to begin."}>
        <ul className="member-list">{list.rows.map(project => <li className="member-row" key={project.id}>
          <div><strong>{project.name}</strong><p>{formatManila(project.startAt)} to {formatManila(project.endAt)}</p></div>
          <Button onClick={() => setSelected(project)}>Open {project.name}</Button>
        </li>)}</ul>
      </WorkRows>
    </GlassPanel>
    {selected && <ProjectWorkspace key={selected.id} orgId={orgId} role={role} project={selected}
      onUpdate={next => { setSelected(next); refresh(); }} />}
  </section>;
}

function ProjectWorkspace({ orgId, role, project, onUpdate }: {
  orgId: string; role: Role; project: ProjectWork; onUpdate: (next: ProjectWork | null) => void;
}) {
  const [revision, setRevision] = useState(0);
  const [lead, setLead] = useState<MembershipOption | null>(null);
  const [resource, setResource] = useState<Resource | null>(null);
  const [quantity, setQuantity] = useState("1");
  const [requirement, setRequirement] = useState<ProjectRequirement | null>(null);
  const [borrower, setBorrower] = useState<MembershipOption | null>(null);
  const [requested, setRequested] = useState("1");
  const id = project.id;
  const loadRequirements = useCallback((cursor: string | null, signal: AbortSignal) => projectWorkApi.requirements(orgId, id, cursor, signal), [orgId, id]);
  const loadAllocations = useCallback((cursor: string | null, signal: AbortSignal) => projectWorkApi.allocations(orgId, id, cursor, signal), [orgId, id]);
  const loadBorrowers = useCallback((cursor: string | null, search: string, signal: AbortSignal) => projectWorkApi.borrowers(orgId, id, cursor, search, signal), [orgId, id]);
  const loadLeads = useCallback((cursor: string | null, search: string, signal: AbortSignal) => projectWorkApi.candidates(orgId, id, cursor, search, signal), [orgId, id]);
  const requirements = useWorkPage(id, loadRequirements, revision);
  const allocations = useWorkPage(id, loadAllocations, revision);
  const refresh = () => setRevision(value => value + 1);
  const actions = useWorkActions(orgId, refresh);
  const blocked = actions.disabled;

  function editProject(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const fields = new FormData(event.currentTarget);
    void actions.run(async () => {
      const updated = await projectWorkApi.update(orgId, project, { name: String(fields.get("name")).trim(),
        startAt: inputDateTime(String(fields.get("startAt"))), endAt: inputDateTime(String(fields.get("endAt"))) });
      onUpdate(updated);
    }, "Project details saved.");
  }

  function assign(candidate: string | null) {
    const operationId = crypto.randomUUID();
    void actions.run(async () => {
      onUpdate(await projectWorkApi.assign(orgId, project, candidate, operationId, crypto.randomUUID()));
      setLead(null);
    }, candidate ? "Project lead assigned." : "Project lead cleared.", operationId);
  }

  function createRequest(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!requirement || (!borrower && requirement.resourceType === "REUSABLE")) return;
    const operationId = crypto.randomUUID();
    void actions.run(async () => {
      await projectWorkApi.createAllocation(orgId, requirement.id, borrower?.id ?? null, Number(requested), operationId, crypto.randomUUID());
      setRequirement(null); setBorrower(null); setRequested("1");
    }, "Allocation request saved.", operationId);
  }

  return <div className="page-shell">
    {actions.feedback(!allocations.loading && !requirements.loading)}
    <GlassPanel tilt={false} className="project-work-panel">
      <PanelHeading title={project.name}><Button disabled={blocked}
        onClick={() => void actions.run(async () => onUpdate(await projectWorkApi.project(orgId, id)), "Project refreshed.")}>Refresh Project</Button></PanelHeading>
      <form className="auth-form" onSubmit={editProject} key={project.version}>
        <label>Project name<input name="name" required maxLength={100} defaultValue={project.name} /></label>
        <label>Starts<input name="startAt" type="datetime-local" required defaultValue={toDateTimeInput(project.startAt)} /></label>
        <label>Ends<input name="endAt" type="datetime-local" required defaultValue={toDateTimeInput(project.endAt)} /></label>
        <div className="row-actions"><Button disabled={blocked}>Save Project details</Button>
          <Button type="button" variant="danger" disabled={blocked} onClick={() => {
            if (window.confirm("Delete this Project?")) void actions.run(async () => {
              await projectWorkApi.deleteProject(orgId, project); onUpdate(null);
            }, "Project deleted.");
          }}>Delete Project</Button></div>
      </form>
    </GlassPanel>
    {role !== "LEAD" && <GlassPanel tilt={false} className="project-work-panel">
      <PanelHeading title="Assigned lead" /><p>{project.leadMembershipId ? "This Project has an assigned lead." : "No lead is assigned."}</p>
      <RelatedSelect id="project-lead" label="Eligible Project lead" load={loadLeads} selection={lead} onChange={setLead}
        optionLabel={item => item.displayName} chooseFirst={false} />
      <div className="row-actions"><Button disabled={blocked || !lead} onClick={() => { if (lead) assign(lead.id); }}>Assign Project lead</Button>
        <Button disabled={blocked || !project.leadMembershipId} onClick={() => { if (window.confirm("Clear the assigned Project lead?")) assign(null); }}>
          Clear Project lead
        </Button></div>
    </GlassPanel>}
    <GlassPanel tilt={false} className="project-work-panel">
      <PanelHeading title="Project Requirements" />
      <form className="auth-form" onSubmit={event => {
        event.preventDefault();
        if (resource) void actions.run(() => projectWorkApi.createRequirement(orgId, id, resource.publicId, Number(quantity)), "Requirement saved.");
      }}>
        <RelatedSelect id="project-work-resource" label="Resource for Requirement" selection={resource} onChange={setResource}
          load={loadResources} optionLabel={item => item.name} chooseFirst={false} />
        <label>Required quantity<input type="number" min={1} max={1000000} required value={quantity} onChange={event => setQuantity(event.target.value)} /></label>
        <Button disabled={blocked || !resource}>Add Project Requirement</Button>
      </form>
      <WorkRows view={requirements} label="Project work Requirement pages" empty="No Requirements on this Project page.">
        <ul className="member-list">{requirements.rows.map(row => <li className="member-row" key={row.id + ":" + row.version}>
          <div><strong>{row.resourceName}</strong><p>{row.quantity} required · {row.allocatedQuantity} covered · {row.shortageQuantity} shortage</p></div>
          <form className="row-actions" onSubmit={event => {
            event.preventDefault();
            const next = Number(new FormData(event.currentTarget).get("quantity"));
            void actions.run(() => projectWorkApi.updateRequirement(orgId, row, next), "Requirement updated.");
          }}>
            <label className="compact-label">New quantity<input name="quantity" type="number" min={1} max={1000000} required defaultValue={row.quantity} /></label>
            <Button disabled={blocked}>Update quantity</Button>
            <Button type="button" disabled={blocked} onClick={() => { setRequirement(row); setBorrower(null); }}>Request {row.resourceName}</Button>
            <Button type="button" variant="danger" disabled={blocked} onClick={() => {
              if (window.confirm("Delete this Requirement?"))
                void actions.run(() => projectWorkApi.deleteRequirement(orgId, row), "Requirement deleted.");
            }}>Delete Requirement</Button>
          </form>
        </li>)}</ul>
      </WorkRows>
    </GlassPanel>
    {requirement && <GlassPanel tilt={false} className="project-work-panel"><PanelHeading title={"Request " + requirement.resourceName} />
      <form className="auth-form" onSubmit={createRequest}>
        <RelatedSelect id="linked-borrower" label="Borrower membership" selection={borrower} onChange={setBorrower}
          load={loadBorrowers} optionLabel={item => item.displayName} chooseFirst={false}
          allLabel={requirement.resourceType === "CONSUMABLE" ? "No borrower selected" : undefined} />
        <label>Requested quantity<input type="number" min={1} max={requirement.quantity} required value={requested}
          onChange={event => setRequested(event.target.value)} /></label>
        <Button disabled={blocked || (!borrower && requirement.resourceType === "REUSABLE")}>Save linked request</Button>
      </form>
    </GlassPanel>}
    <GlassPanel tilt={false} className="project-work-panel"><PanelHeading title="Project Allocations" />
      <WorkRows view={allocations} label="Project work Allocation pages" empty="No Allocation requests on this Project page.">
        <ul className="member-list">{allocations.rows.map(row => <li className="member-row" key={row.id}>
          <div><strong>{row.resourceName}</strong><p>{row.borrowerNameSnapshot} · {row.quantity} units</p>
            <StatusBadge>{row.status}</StatusBadge></div>
          <div className="row-actions">
            {row.canCancel && <Button disabled={blocked} onClick={() => {
              if (!window.confirm("Cancel this pending request?")) return;
              const operationId = crypto.randomUUID();
              void actions.run(() => projectWorkApi.cancelAllocation(orgId, row, operationId, crypto.randomUUID()), "Request cancelled.", operationId);
            }}>Cancel pending request</Button>}
            {row.canDelete && <Button variant="danger" disabled={blocked} onClick={() => {
              if (window.confirm("Delete this never-approved request?"))
                void actions.run(() => projectWorkApi.deleteAllocation(orgId, row), "Request deleted.");
            }}>Delete request</Button>}
          </div>
        </li>)}</ul>
      </WorkRows>
    </GlassPanel>
  </div>;
}

export function BorrowedAllocationsPage({ orgId }: { orgId: string }) {
  const [revision, setRevision] = useState(0);
  const load = useCallback((cursor: string | null, signal: AbortSignal) => projectWorkApi.borrowed(orgId, cursor, signal), [orgId]);
  const view = useWorkPage(orgId, load, revision);
  const actions = useWorkActions(orgId, () => setRevision(value => value + 1));
  return <section className="page-shell">
    <PageHeader eyebrow="Account" title="My borrowed resources" description="Allocations linked to your membership in this workspace." />
    {actions.feedback(!view.loading && !view.error)}
    <GlassPanel tilt={false} className="project-work-panel"><PanelHeading title="My Allocations" />
      <WorkRows view={view} label="Borrowed Allocation pages" empty="No Allocations are linked to this membership.">
        <ul className="member-list">{view.rows.map(row => <li className="member-row" key={row.id}>
          <div><strong>{row.resourceName}</strong><p>{row.projectName} · {row.quantity} units · {formatManila(row.createdAt)}</p>
            <StatusBadge>{row.status}</StatusBadge></div>
          <div>
            <p>{row.smsConsent === true ? "SMS reminder consent is active." : "No active SMS reminder consent."}</p>
            {row.smsConsent === true && <Button disabled={actions.disabled} onClick={() => {
              if (!window.confirm("Withdraw SMS consent for this Allocation?")) return;
              const operationId = crypto.randomUUID();
              void actions.run(() => projectWorkApi.revokeConsent(orgId, row, operationId, crypto.randomUUID()), "SMS consent withdrawn.", operationId);
            }}>Withdraw SMS consent</Button>}
          </div>
        </li>)}</ul>
      </WorkRows>
    </GlassPanel>
  </section>;
}
