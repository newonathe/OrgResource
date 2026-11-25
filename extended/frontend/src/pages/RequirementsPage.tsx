import { useEffect, useState, type FormEvent } from "react";
import { workspaceApi, api } from "../api/client";
import { useAuth } from "../auth/AuthContext";
import { RelatedSelect } from "../RelatedSelect";
import type { Resource, WorkspaceProject, WorkspaceRequirement } from "../api/types";
import { explainError, formatManila, isUncertain, projectPeriod } from "../domain";
import { Button, GlassPanel, LoadState, Modal, MutationReviewNotice, Notice, PageHeader, PageNavigation, PanelHeading, usePageCursors, StatusBadge } from "../ui";

const loadProjects = (cursor: string | null, search: string, signal: AbortSignal) => workspaceApi.projects(cursor, { search }, signal);
const loadResources = (cursor: string | null, search: string, signal: AbortSignal) => workspaceApi.resources(cursor, { search }, signal);

export function RequirementsPage() {
  const { session, organizations } = useAuth();
  const role = organizations.find(org => org.id === session?.activeOrgId)?.role;
  const canManage = role === "OWNER" || role === "ADMIN";
  const [requirements, setRequirements] = useState<WorkspaceRequirement[]>([]);
  const [loadState, setLoadState] = useState<"loading" | "error" | "ready">("loading");
  const [loadError, setLoadError] = useState("");
  const [projectFilter, setProjectFilter] = useState<WorkspaceProject | null>(null);
  const projectFilterId = projectFilter?.id;
  const [search, setSearch] = useState("");
  const [reload, setReload] = useState(0);
  const { cursor, page, move } = usePageCursors(JSON.stringify([projectFilterId, search]));
  const [nextCursor, setNextCursor] = useState<string | null>(null);
  const goToPage = (next: string | null) => { setLoadState("loading"); move(next); };
  const [project, setProject] = useState<WorkspaceProject | null>(null);
  const [resource, setResource] = useState<Resource | null>(null);
  const [quantity, setQuantity] = useState("1");
  const [editing, setEditing] = useState<WorkspaceRequirement | null>(null);
  const [editQuantity, setEditQuantity] = useState("");
  const [deleting, setDeleting] = useState<WorkspaceRequirement | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [postReviewRequired, setPostReviewRequired] = useState(false);

  useEffect(() => {
    const controller = new AbortController();
    workspaceApi.requirements(cursor, { projectId: projectFilterId, search }, controller.signal)
      .then(result => {
        setRequirements(result.items); setNextCursor(result.nextCursor); setLoadState("ready"); setLoadError("");
      }).catch((reason: unknown) => {
        if (reason instanceof DOMException && reason.name === "AbortError") return;
        setLoadError(explainError(reason)); setLoadState("error");
      });
    return () => controller.abort();
  }, [cursor, projectFilterId, search, reload]);

  const ordered = requirements;
  const selectedProjectId = project?.id;
  const selectedResourceId = resource?.id;
  const refresh = () => { setLoadState("loading"); setReload((value) => value + 1); };

  async function createRequirement(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const amount = Number(quantity);
    if (busy || postReviewRequired || !selectedProjectId || !selectedResourceId || !Number.isInteger(amount) || amount < 1 || amount > 1_000_000) return;
    setBusy(true); setError(""); setNotice("");
    try {
      await api.createRequirement({ projectId: Number(selectedProjectId), resourceId: Number(selectedResourceId), quantity: amount });
      setQuantity("1"); setNotice("Requirement saved. Coverage and shortage were read from the server."); refresh();
    } catch (reason) { if (isUncertain(reason)) { setPostReviewRequired(true); refresh(); } else setError(explainError(reason)); }
    finally { setBusy(false); }
  }

  async function updateRequirement(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const amount = Number(editQuantity);
    if (busy || postReviewRequired || !editing || !Number.isInteger(amount) || amount < 1 || amount > 1_000_000) return;
    setBusy(true); setError(""); setNotice("");
    try { await api.updateRequirement(editing.id, amount); setEditing(null); setNotice("Requirement quantity updated."); refresh(); }
    catch (reason) { if (isUncertain(reason)) { setPostReviewRequired(true); refresh(); } else setError(explainError(reason)); }
    finally { setBusy(false); }
  }

  async function confirmDelete() {
    if (busy || postReviewRequired || !deleting) return;
    setBusy(true); setError(""); setNotice("");
    try { await api.deleteRequirement(deleting.id); setDeleting(null); setNotice("Requirement deleted."); refresh(); }
    catch (reason) {
      if (isUncertain(reason)) { setPostReviewRequired(true); refresh(); }
      else { setError(explainError(reason)); setDeleting(null); }
    }
    finally { setBusy(false); }
  }

  return (
    <section className="page-shell" aria-labelledby="requirements-title">
      <PageHeader eyebrow="Organization · project needs" title="Requirements" description="Connect each Project to one Resource and follow server-calculated coverage and shortage."
        action={<div className="header-stat"><span>{loadState === "ready" ? requirements.length : "—"}</span><small>requirements in view</small></div>} />
      {notice && <Notice tone="success">{notice}</Notice>}{error && <Notice tone="danger">{error}</Notice>}
      <MutationReviewNotice required={postReviewRequired} ready={loadState === "ready"} onReviewed={() => setPostReviewRequired(false)} />
      <div className="content-grid requirement-grid">
        {canManage ? <GlassPanel className="form-panel" tilt={false}>
          <PanelHeading kicker="Demand planning" title="Add a requirement" />
          <form className="stack-form" onSubmit={createRequirement}>
            <RelatedSelect id="requirement-project" label="Project" selection={project} onChange={setProject}
              load={loadProjects} optionLabel={row => row.name + " · " + projectPeriod(row).toLowerCase()} reload={reload} />
            <RelatedSelect id="requirement-resource" label="Resource" selection={resource} onChange={setResource}
              load={loadResources} optionLabel={row => row.name + " · " + row.type.toLowerCase()} reload={reload} />
            <label htmlFor="requirement-quantity">Quantity required</label><input id="requirement-quantity" type="number" min="1" max="1000000" step="1" value={quantity} onChange={(event) => setQuantity(event.target.value)} required />
            <p className="field-hint">Each Project and Resource pair can have one Requirement. The server enforces that rule.</p>
            <Button variant="primary" magnetic type="submit" disabled={busy || postReviewRequired || !selectedProjectId || !selectedResourceId}>{busy ? "Saving…" : "Add requirement"}</Button>
          </form>
        </GlassPanel> : <GlassPanel className="form-panel" tilt={false}><PanelHeading kicker="Read only" title="Requirement access" /><p>Your role can inspect current coverage and shortage but cannot change project requirements.</p></GlassPanel>}
        <GlassPanel className="list-panel" tilt={false}>
          <PanelHeading kicker="Coverage ledger" title="Project requirements"><Button variant="quiet" disabled={loadState === "loading"} onClick={refresh}>Refresh</Button></PanelHeading>
          <div className="search-filter-bar requirement-filters"><label htmlFor="requirement-search">Search</label><input id="requirement-search" type="search" value={search} maxLength={100} onChange={(event) => { setLoadState("loading"); setSearch(event.target.value); }} placeholder="Project or Resource" />
            <RelatedSelect id="requirement-project-filter" label="Filter by Project" selection={projectFilter}
              onChange={row => { setLoadState("loading"); setProjectFilter(row); }} load={loadProjects}
              optionLabel={row => row.name} allLabel="All projects" reload={reload} /></div>
          <LoadState state={loadState} error={loadError} onRetry={refresh}>
            {requirements.length === 0 ? <div className="state-block state-empty"><span className="empty-symbol" aria-hidden="true">☷</span><h3>No requirements yet</h3><p>Choose a Project and Resource to record planned demand.</p></div>
              : ordered.length === 0 ? <div className="state-block state-empty"><h3>No matching requirements</h3><p>Try a different search or Project filter.</p></div>
                : <div className="requirement-list">{ordered.map((requirement) => {
                  const period = { startAt: requirement.projectStartAt, endAt: requirement.projectEndAt };
                  return <article className="requirement-card" key={requirement.id}>
                    <div className="requirement-title"><div><p className="section-kicker">{requirement.projectName}</p><h3>{requirement.resourceName}</h3></div><StatusBadge tone={requirement.shortageQuantity > 0 ? "warning" : "success"}>{requirement.shortageQuantity > 0 ? "Shortage" : "Covered"}</StatusBadge></div>
                    <div className="coverage-metrics"><div><span>Required</span><strong>{requirement.quantity.toLocaleString()}</strong></div><div><span>Covered</span><strong>{requirement.allocatedQuantity.toLocaleString()}</strong></div><div><span>Shortage</span><strong className={requirement.shortageQuantity > 0 ? "warning-value" : "success-value"}>{requirement.shortageQuantity.toLocaleString()}</strong></div></div>
                    <div className="coverage-bar" role="img" aria-label={`${requirement.allocatedQuantity} of ${requirement.quantity} covered`}><span style={{ width: `${requirement.quantity ? Math.min(100, requirement.allocatedQuantity / requirement.quantity * 100) : 0}%` }} /></div>
                    <p className="requirement-meta">{projectPeriod(period).toLowerCase() + " · ends " + formatManila(period.endAt)}</p>
                    {canManage && <div className="row-actions"><Button variant="quiet" disabled={postReviewRequired} aria-label={`Edit requirement for ${requirement.resourceName}`} onClick={() => { setEditing(requirement); setEditQuantity(String(requirement.quantity)); setError(""); }}>Change quantity</Button>
                      <Button variant="quiet" disabled={postReviewRequired} className="button-text-danger" aria-label={`Delete requirement for ${requirement.resourceName}`} onClick={() => setDeleting(requirement)}>Delete</Button></div>
                    }
                  </article>;
                })}</div>}
          </LoadState>
          <PageNavigation label="Requirement pages" page={page} count={loadState === "ready" ? requirements.length : 0}
            hasNext={loadState === "ready" && nextCursor !== null} loading={loadState === "loading"} unavailable={loadState === "error"}
            onPrevious={() => goToPage(null)} onNext={() => { if (nextCursor) goToPage(nextCursor); }} />
          <p className="list-footnote">Coverage and shortage are the backend’s current calculation. Pending requests do not count as fulfilled coverage.</p>
        </GlassPanel>
      </div>
      {editing && <Modal title="Change Requirement quantity" onClose={() => setEditing(null)}><p className="modal-copy">Project and Resource links stay fixed. The server will refuse a quantity below covered stock or the largest live pending request.</p>
        <form className="stack-form" onSubmit={updateRequirement}><label htmlFor="edit-requirement-quantity">New quantity</label><input id="edit-requirement-quantity" type="number" min="1" max="1000000" step="1" value={editQuantity} onChange={(event) => setEditQuantity(event.target.value)} required />
          <div className="modal-actions"><Button variant="quiet" type="button" disabled={busy} onClick={() => setEditing(null)}>Cancel</Button><Button variant="primary" type="submit" disabled={busy || postReviewRequired}>{busy ? "Saving…" : "Save quantity"}</Button></div></form></Modal>}
      {deleting && <Modal title="Delete requirement?" onClose={() => setDeleting(null)}><p className="modal-copy">Delete the requirement for <strong>{deleting.resourceName}</strong>? Any Allocation history blocks deletion.</p>
        <div className="modal-actions"><Button variant="quiet" disabled={busy} onClick={() => setDeleting(null)}>Keep requirement</Button><Button variant="danger" disabled={busy || postReviewRequired} onClick={() => void confirmDelete()}>{busy ? "Checking…" : "Confirm delete"}</Button></div></Modal>}
    </section>
  );
}
