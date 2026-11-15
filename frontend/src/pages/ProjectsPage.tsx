import { useEffect, useMemo, useState, type FormEvent } from "react";
import { api } from "../api/client";
import type { Allocation, Department, Project, Resource, ResourceRequirement } from "../api/types";
import { explainError, formatManila, inputDateTime, isUncertain, projectPeriod, toDateTimeInput } from "../domain";
import { Button, DetailSheet, GlassPanel, LoadState, Modal, MutationReviewNotice, Notice, PageHeader, PanelHeading, StatusBadge } from "../ui";

function defaultStart(): string {
  const d = new Date(Date.now() + 48 * 60 * 60 * 1000);
  const parts = Object.fromEntries(new Intl.DateTimeFormat("en-CA", { timeZone: "Asia/Manila", year: "numeric", month: "2-digit", day: "2-digit", hour: "2-digit", minute: "2-digit", hourCycle: "h23" }).formatToParts(d).map((p) => [p.type, p.value]));
  return `${parts.year}-${parts.month}-${parts.day}T${parts.hour}:${parts.minute}`;
}

export function ProjectsPage() {
  const [projects, setProjects] = useState<Project[]>([]);
  const [departments, setDepartments] = useState<Department[]>([]);
  const [loadState, setLoadState] = useState<"loading" | "error" | "ready">("loading");
  const [loadError, setLoadError] = useState("");
  const [departmentFilter, setDepartmentFilter] = useState("ALL");
  const [periodFilter, setPeriodFilter] = useState("ALL");
  const [search, setSearch] = useState("");
  const [reload, setReload] = useState(0);
  const [departmentId, setDepartmentId] = useState("");
  const [name, setName] = useState("");
  const [startAt, setStartAt] = useState(defaultStart);
  const [endAt, setEndAt] = useState("");
  const [editing, setEditing] = useState<Project | null>(null);
  const [editName, setEditName] = useState("");
  const [editStart, setEditStart] = useState("");
  const [editEnd, setEditEnd] = useState("");
  const [deleting, setDeleting] = useState<Project | null>(null);
  const [selected, setSelected] = useState<Project | null>(null);
  const [detailRequirements, setDetailRequirements] = useState<ResourceRequirement[]>([]);
  const [detailAllocations, setDetailAllocations] = useState<Allocation[]>([]);
  const [detailResources, setDetailResources] = useState<Resource[]>([]);
  const [detailState, setDetailState] = useState<"loading" | "error" | "ready">("loading");
  const [detailError, setDetailError] = useState("");
  const [detailReload, setDetailReload] = useState(0);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [postReviewRequired, setPostReviewRequired] = useState(false);

  useEffect(() => {
    const controller = new AbortController();
    const filterId = departmentFilter === "ALL" ? undefined : Number(departmentFilter);
    Promise.all([api.projects(filterId, controller.signal), api.departments(controller.signal)]).then(([rows, departmentRows]) => {
      setProjects(rows); setDepartments(departmentRows); setLoadState("ready"); setLoadError("");
    }).catch((reason: unknown) => {
      if (reason instanceof DOMException && reason.name === "AbortError") return;
      setLoadError(explainError(reason)); setLoadState("error");
    });
    return () => controller.abort();
  }, [departmentFilter, reload]);

  useEffect(() => {
    if (!selected) return;
    const controller = new AbortController();
    Promise.all([
      api.requirements(selected.id, controller.signal),
      api.allocations({}, controller.signal),
      api.resources(undefined, controller.signal),
    ]).then(([requirements, allocations, resources]) => {
      setDetailRequirements(requirements); setDetailAllocations(allocations.filter((a) => requirements.some((r) => r.id === a.requirementId)));
      setDetailResources(resources); setDetailState("ready"); setDetailError("");
    }).catch((reason: unknown) => {
      if (reason instanceof DOMException && reason.name === "AbortError") return;
      setDetailError(explainError(reason)); setDetailState("error");
    });
    return () => controller.abort();
  }, [selected, detailReload]);

  const ordered = useMemo(() => projects.filter((project) => {
    const department = departments.find((item) => item.id === project.departmentId)?.name ?? "";
    const queryMatch = `${project.name} ${department}`.toLowerCase().includes(search.trim().toLowerCase());
    return queryMatch && (periodFilter === "ALL" || projectPeriod(project) === periodFilter);
  }).sort((a, b) => a.startAt.localeCompare(b.startAt) || a.id - b.id), [projects, departments, search, periodFilter]);
  const selectedDepartmentId = departmentId || String(departments[0]?.id ?? "");
  const refresh = () => { setLoadState("loading"); setReload((value) => value + 1); };
  const refreshDetails = () => { setDetailState("loading"); setDetailReload((value) => value + 1); };

  async function createProject(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (busy || postReviewRequired || !selectedDepartmentId || !name.trim() || !startAt || !endAt) return;
    setBusy(true); setError(""); setNotice("");
    try {
      await api.createProject({ departmentId: Number(selectedDepartmentId), name: name.trim(), startAt: inputDateTime(startAt), endAt: inputDateTime(endAt) });
      setName(""); setEndAt(""); setNotice("Project saved."); refresh();
    } catch (reason) { if (isUncertain(reason)) { setPostReviewRequired(true); refresh(); } else setError(explainError(reason)); }
    finally { setBusy(false); }
  }

  function beginEdit(project: Project) {
    setEditing(project); setEditName(project.name); setEditStart(toDateTimeInput(project.startAt)); setEditEnd(toDateTimeInput(project.endAt)); setError(""); setNotice("");
  }

  async function updateProject(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (busy || postReviewRequired || !editing || !editName.trim() || !editStart || !editEnd) return;
    setBusy(true); setError(""); setNotice("");
    try {
      await api.updateProject(editing.id, { name: editName.trim(), startAt: inputDateTime(editStart), endAt: inputDateTime(editEnd) });
      setEditing(null); setNotice("Project updated."); refresh();
    } catch (reason) { if (isUncertain(reason)) { setPostReviewRequired(true); refresh(); } else setError(explainError(reason)); }
    finally { setBusy(false); }
  }

  async function confirmDelete() {
    if (busy || postReviewRequired || !deleting) return;
    setBusy(true); setError(""); setNotice("");
    try { await api.deleteProject(deleting.id); setDeleting(null); setNotice("Project deleted."); refresh(); }
    catch (reason) {
      if (isUncertain(reason)) { setPostReviewRequired(true); refresh(); }
      else { setError(explainError(reason)); setDeleting(null); }
    }
    finally { setBusy(false); }
  }

  const detailRows = detailRequirements.map((requirement) => {
    const resource = detailResources.find((item) => item.id === requirement.resourceId);
    const batches = detailAllocations.filter((allocation) => allocation.requirementId === requirement.id);
    return { requirement, resource, batches };
  });

  return (
    <section className="page-shell" aria-labelledby="projects-title">
      <PageHeader eyebrow="Workspace · activity planning" title="Projects" description="Plan each activity in Philippine time and keep its linked needs and history together."
        action={<div className="header-stat"><span>{loadState === "ready" ? projects.length : "—"}</span><small>projects in view</small></div>} />
      {notice && <Notice tone="success">{notice}</Notice>}{error && <Notice tone="danger">{error}</Notice>}
      <MutationReviewNotice required={postReviewRequired} ready={loadState === "ready"} onReviewed={() => setPostReviewRequired(false)} />
      <div className="content-grid project-grid">
        <GlassPanel className="form-panel" tilt={false}>
          <PanelHeading kicker="Planning desk" title="Create a project" />
          {departments.length === 0 && loadState === "ready" && <Notice tone="warning">Create a Department before adding a Project.</Notice>}
          <form className="stack-form" onSubmit={createProject}>
            <label htmlFor="project-department">Department</label><select id="project-department" value={selectedDepartmentId} onChange={(event) => setDepartmentId(event.target.value)} required>
              {departments.length === 0 && <option value="">No departments available</option>}{departments.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}
            </select>
            <label htmlFor="project-name">Project name</label><input id="project-name" maxLength={100} value={name} onChange={(event) => setName(event.target.value)} required />
            <label htmlFor="project-start">Starts · Philippine time</label><input id="project-start" type="datetime-local" step="1" value={startAt} onChange={(event) => setStartAt(event.target.value)} required />
            <label htmlFor="project-end">Ends · Philippine time</label><input id="project-end" type="datetime-local" step="1" value={endAt} onChange={(event) => setEndAt(event.target.value)} required />
            <p className="field-hint">Date-times are sent as Asia/Manila wall time (YYYY-MM-DDTHH:mm:ss).</p>
            <Button variant="primary" magnetic type="submit" disabled={busy || postReviewRequired || !selectedDepartmentId || !name.trim() || !startAt || !endAt}>{busy ? "Saving…" : "Create project"}</Button>
          </form>
        </GlassPanel>

        <GlassPanel className="list-panel" tilt={false}>
          <PanelHeading kicker="Activity calendar" title="Project schedule"><Button variant="quiet" disabled={loadState === "loading"} onClick={refresh}>Refresh</Button></PanelHeading>
          <div className="search-filter-bar project-filters"><label htmlFor="project-search">Search projects</label><input id="project-search" type="search" value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Name or department" />
            <label htmlFor="project-department-filter">Department</label><select id="project-department-filter" value={departmentFilter} onChange={(event) => { setLoadState("loading"); setDepartmentFilter(event.target.value); }}><option value="ALL">All departments</option>{departments.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</select>
            <label htmlFor="project-period-filter">Period</label><select id="project-period-filter" value={periodFilter} onChange={(event) => setPeriodFilter(event.target.value)}><option value="ALL">All periods</option><option value="UPCOMING">Upcoming</option><option value="ACTIVE">Active</option><option value="ENDED">Ended</option></select></div>
          <LoadState state={loadState} error={loadError} onRetry={refresh}>
            {projects.length === 0 ? <div className="state-block state-empty"><span className="empty-symbol" aria-hidden="true">▤</span><h3>No projects yet</h3><p>Choose a department and create the first scheduled activity.</p></div>
              : ordered.length === 0 ? <div className="state-block state-empty"><h3>No matching projects</h3><p>Change the search or filters to view other records.</p></div>
                : <div className="project-list">{ordered.map((project) => {
                  const status = projectPeriod(project);
                  const department = departments.find((item) => item.id === project.departmentId)?.name ?? `Department ${project.departmentId}`;
                  return <article className="project-card" key={project.id}>
                    <div className="project-card-mark" aria-hidden="true">▤</div>
                    <div className="project-card-main"><div className="project-card-heading"><h3>{project.name}</h3><StatusBadge tone={status === "ACTIVE" ? "success" : status === "UPCOMING" ? "blue" : "neutral"}>{status.toLowerCase()}</StatusBadge></div>
                      <p>{department}</p><div className="project-dates"><span><small>Starts</small>{formatManila(project.startAt)}</span><span><small>Ends</small>{formatManila(project.endAt)}</span></div></div>
                    <div className="project-actions"><Button variant="quiet" aria-label={`View ${project.name}`} onClick={() => { setSelected(project); setDetailState("loading"); }}>Details</Button><Button variant="quiet" disabled={postReviewRequired} aria-label={`Edit ${project.name}`} onClick={() => beginEdit(project)}>Edit</Button><Button variant="quiet" disabled={postReviewRequired} className="button-text-danger" aria-label={`Delete ${project.name}`} onClick={() => setDeleting(project)}>Delete</Button></div>
                  </article>;
                })}</div>}
          </LoadState>
          <p className="list-footnote">Active means start ≤ current Philippine time &lt; end. Period labels are calculated for display.</p>
        </GlassPanel>
      </div>

      {editing && <Modal title={`Edit ${editing.name}`} onClose={() => setEditing(null)}>
        <form className="stack-form" onSubmit={updateProject}><label htmlFor="edit-project-name">Project name</label><input id="edit-project-name" maxLength={100} value={editName} onChange={(event) => setEditName(event.target.value)} required />
          <label htmlFor="edit-project-start">Starts · Philippine time</label><input id="edit-project-start" type="datetime-local" step="1" value={editStart} onChange={(event) => setEditStart(event.target.value)} required />
          <label htmlFor="edit-project-end">Ends · Philippine time</label><input id="edit-project-end" type="datetime-local" step="1" value={editEnd} onChange={(event) => setEditEnd(event.target.value)} required />
          <p className="field-hint">Department stays linked. If only the name changes, leave the existing dates as shown.</p>
          <div className="modal-actions"><Button variant="quiet" type="button" disabled={busy} onClick={() => setEditing(null)}>Cancel</Button><Button variant="primary" type="submit" disabled={busy || postReviewRequired || !editName.trim()}>{busy ? "Saving…" : "Save changes"}</Button></div>
        </form>
      </Modal>}
      {deleting && <Modal title="Delete project?" onClose={() => setDeleting(null)}><p className="modal-copy">Delete <strong>{deleting.name}</strong>? The server will refuse while any Requirements reference this Project.</p>
        <div className="modal-actions"><Button variant="quiet" disabled={busy} onClick={() => setDeleting(null)}>Keep project</Button><Button variant="danger" disabled={busy || postReviewRequired} onClick={() => void confirmDelete()}>{busy ? "Checking…" : "Confirm delete"}</Button></div></Modal>}
      {selected && <DetailSheet title={selected.name} onClose={() => setSelected(null)}>
        <div className="detail-grid"><div><span>Department</span><strong>{departments.find((item) => item.id === selected.departmentId)?.name ?? `Department ${selected.departmentId}`}</strong></div><div><span>Period</span><strong>{projectPeriod(selected).toLowerCase()}</strong></div><div><span>Starts</span><strong>{formatManila(selected.startAt)}</strong></div><div><span>Ends</span><strong>{formatManila(selected.endAt)}</strong></div></div>
        <section className="sheet-section"><h3>Requirements and allocation history</h3>
          <LoadState state={detailState} error={detailError} onRetry={refreshDetails}>
            {detailRows.length === 0 ? <p className="quiet-note">This Project has no Requirements.</p> : <div className="sheet-record-list">{detailRows.map(({ requirement, resource, batches }) => <article key={requirement.id}>
              <div><strong>{resource?.name ?? `Resource ${requirement.resourceId}`}</strong><small>{requirement.quantity} required · {requirement.allocatedQuantity} covered · {requirement.shortageQuantity} shortage</small></div>
              <div className="sheet-batches">{batches.length ? batches.map((batch) => <span key={batch.id}>A{batch.id} · {batch.quantity} · {batch.status.toLowerCase()}</span>) : <span>No Allocation batches</span>}</div>
            </article>)}</div>}
          </LoadState>
        </section>
      </DetailSheet>}
    </section>
  );
}
