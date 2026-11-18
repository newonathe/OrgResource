import { useEffect, useMemo, useState, type FormEvent } from "react";
import { api } from "../api/client";
import { useAuth } from "../auth/AuthContext";
import type { Project, Resource, ResourceRequirement } from "../api/types";
import { explainError, formatManila, isUncertain, projectPeriod } from "../domain";
import { Button, GlassPanel, LoadState, Modal, MutationReviewNotice, Notice, PageHeader, PanelHeading, StatusBadge } from "../ui";

export function RequirementsPage() {
  const { session, organizations } = useAuth();
  const role = organizations.find(org => org.id === session?.activeOrgId)?.role;
  const canManage = role === "OWNER" || role === "ADMIN";
  const [requirements, setRequirements] = useState<ResourceRequirement[]>([]);
  const [projects, setProjects] = useState<Project[]>([]);
  const [resources, setResources] = useState<Resource[]>([]);
  const [loadState, setLoadState] = useState<"loading" | "error" | "ready">("loading");
  const [loadError, setLoadError] = useState("");
  const [projectFilter, setProjectFilter] = useState("ALL");
  const [search, setSearch] = useState("");
  const [reload, setReload] = useState(0);
  const [projectId, setProjectId] = useState("");
  const [resourceId, setResourceId] = useState("");
  const [quantity, setQuantity] = useState("1");
  const [editing, setEditing] = useState<ResourceRequirement | null>(null);
  const [editQuantity, setEditQuantity] = useState("");
  const [deleting, setDeleting] = useState<ResourceRequirement | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [postReviewRequired, setPostReviewRequired] = useState(false);

  useEffect(() => {
    const controller = new AbortController();
    const filterId = projectFilter === "ALL" ? undefined : Number(projectFilter);
    Promise.all([api.requirements(filterId, controller.signal), api.projects(undefined, controller.signal), api.resources(undefined, controller.signal)])
      .then(([requirementRows, projectRows, resourceRows]) => {
        setRequirements(requirementRows); setProjects(projectRows); setResources(resourceRows); setLoadState("ready"); setLoadError("");
      }).catch((reason: unknown) => {
        if (reason instanceof DOMException && reason.name === "AbortError") return;
        setLoadError(explainError(reason)); setLoadState("error");
      });
    return () => controller.abort();
  }, [projectFilter, reload]);

  const projectMap = useMemo(() => new Map(projects.map((item) => [item.id, item])), [projects]);
  const resourceMap = useMemo(() => new Map(resources.map((item) => [item.id, item])), [resources]);
  const ordered = useMemo(() => requirements.filter((item) => {
    const project = projectMap.get(item.projectId);
    const resource = resourceMap.get(item.resourceId);
    return `${project?.name ?? ""} ${resource?.name ?? ""}`.toLowerCase().includes(search.trim().toLowerCase());
  }).sort((a, b) => (projectMap.get(a.projectId)?.name ?? "").localeCompare(projectMap.get(b.projectId)?.name ?? "") || a.id - b.id),
  [requirements, projectMap, resourceMap, search]);
  const selectedProjectId = projectId || String(projects[0]?.id ?? "");
  const selectedResourceId = resourceId || String(resources[0]?.id ?? "");
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
            <label htmlFor="requirement-project">Project</label><select id="requirement-project" value={selectedProjectId} onChange={(event) => setProjectId(event.target.value)} required>
              {projects.length === 0 && <option value="">No Projects available</option>}{projects.map((item) => <option key={item.id} value={item.id}>{item.name} · {projectPeriod(item).toLowerCase()}</option>)}
            </select>
            <label htmlFor="requirement-resource">Resource</label><select id="requirement-resource" value={selectedResourceId} onChange={(event) => setResourceId(event.target.value)} required>
              {resources.length === 0 && <option value="">No Resources available</option>}{resources.map((item) => <option key={item.id} value={item.id}>{item.name} · {item.type.toLowerCase()}</option>)}
            </select>
            <label htmlFor="requirement-quantity">Quantity required</label><input id="requirement-quantity" type="number" min="1" max="1000000" step="1" value={quantity} onChange={(event) => setQuantity(event.target.value)} required />
            <p className="field-hint">Each Project and Resource pair can have one Requirement. The server enforces that rule.</p>
            <Button variant="primary" magnetic type="submit" disabled={busy || postReviewRequired || !selectedProjectId || !selectedResourceId}>{busy ? "Saving…" : "Add requirement"}</Button>
          </form>
        </GlassPanel> : <GlassPanel className="form-panel" tilt={false}><PanelHeading kicker="Read only" title="Requirement access" /><p>Your role can inspect current coverage and shortage but cannot change project requirements.</p></GlassPanel>}
        <GlassPanel className="list-panel" tilt={false}>
          <PanelHeading kicker="Coverage ledger" title="Project requirements"><Button variant="quiet" disabled={loadState === "loading"} onClick={refresh}>Refresh</Button></PanelHeading>
          <div className="search-filter-bar requirement-filters"><label htmlFor="requirement-search">Search</label><input id="requirement-search" type="search" value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Project or Resource" />
            <label htmlFor="requirement-project-filter">Project</label><select id="requirement-project-filter" value={projectFilter} onChange={(event) => { setLoadState("loading"); setProjectFilter(event.target.value); }}><option value="ALL">All projects</option>{projects.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</select></div>
          <LoadState state={loadState} error={loadError} onRetry={refresh}>
            {requirements.length === 0 ? <div className="state-block state-empty"><span className="empty-symbol" aria-hidden="true">☷</span><h3>No requirements yet</h3><p>Choose a Project and Resource to record planned demand.</p></div>
              : ordered.length === 0 ? <div className="state-block state-empty"><h3>No matching requirements</h3><p>Try a different search or Project filter.</p></div>
                : <div className="requirement-list">{ordered.map((requirement) => {
                  const project = projectMap.get(requirement.projectId);
                  const resource = resourceMap.get(requirement.resourceId);
                  return <article className="requirement-card" key={requirement.id}>
                    <div className="requirement-title"><div><p className="section-kicker">{project?.name ?? `Project ${requirement.projectId}`}</p><h3>{resource?.name ?? `Resource ${requirement.resourceId}`}</h3></div><StatusBadge tone={requirement.shortageQuantity > 0 ? "warning" : "success"}>{requirement.shortageQuantity > 0 ? "Shortage" : "Covered"}</StatusBadge></div>
                    <div className="coverage-metrics"><div><span>Required</span><strong>{requirement.quantity.toLocaleString()}</strong></div><div><span>Covered</span><strong>{requirement.allocatedQuantity.toLocaleString()}</strong></div><div><span>Shortage</span><strong className={requirement.shortageQuantity > 0 ? "warning-value" : "success-value"}>{requirement.shortageQuantity.toLocaleString()}</strong></div></div>
                    <div className="coverage-bar" role="img" aria-label={`${requirement.allocatedQuantity} of ${requirement.quantity} covered`}><span style={{ width: `${requirement.quantity ? Math.min(100, requirement.allocatedQuantity / requirement.quantity * 100) : 0}%` }} /></div>
                    <p className="requirement-meta">{project ? `${projectPeriod(project).toLowerCase()} · ends ${formatManila(project.endAt)}` : "Project details unavailable"}</p>
                    {canManage && <div className="row-actions"><Button variant="quiet" disabled={postReviewRequired} aria-label={`Edit requirement for ${resource?.name ?? requirement.resourceId}`} onClick={() => { setEditing(requirement); setEditQuantity(String(requirement.quantity)); setError(""); }}>Change quantity</Button>
                      <Button variant="quiet" disabled={postReviewRequired} className="button-text-danger" aria-label={`Delete requirement for ${resource?.name ?? requirement.resourceId}`} onClick={() => setDeleting(requirement)}>Delete</Button></div>
                    }
                  </article>;
                })}</div>}
          </LoadState>
          <p className="list-footnote">Coverage and shortage are the backend’s current calculation. Pending requests do not count as fulfilled coverage.</p>
        </GlassPanel>
      </div>
      {editing && <Modal title="Change Requirement quantity" onClose={() => setEditing(null)}><p className="modal-copy">Project and Resource links stay fixed. The server will refuse a quantity below covered stock or the largest live pending request.</p>
        <form className="stack-form" onSubmit={updateRequirement}><label htmlFor="edit-requirement-quantity">New quantity</label><input id="edit-requirement-quantity" type="number" min="1" max="1000000" step="1" value={editQuantity} onChange={(event) => setEditQuantity(event.target.value)} required />
          <div className="modal-actions"><Button variant="quiet" type="button" disabled={busy} onClick={() => setEditing(null)}>Cancel</Button><Button variant="primary" type="submit" disabled={busy || postReviewRequired}>{busy ? "Saving…" : "Save quantity"}</Button></div></form></Modal>}
      {deleting && <Modal title="Delete requirement?" onClose={() => setDeleting(null)}><p className="modal-copy">Delete the requirement for <strong>{resourceMap.get(deleting.resourceId)?.name ?? `Resource ${deleting.resourceId}`}</strong>? Any Allocation history blocks deletion.</p>
        <div className="modal-actions"><Button variant="quiet" disabled={busy} onClick={() => setDeleting(null)}>Keep requirement</Button><Button variant="danger" disabled={busy || postReviewRequired} onClick={() => void confirmDelete()}>{busy ? "Checking…" : "Confirm delete"}</Button></div></Modal>}
    </section>
  );
}
