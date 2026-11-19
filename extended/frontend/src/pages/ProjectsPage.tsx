import { useEffect, useState, type FormEvent } from "react";
import { workspaceApi, api } from "../api/client";
import { useAuth } from "../auth/AuthContext";
import { RelatedSelect } from "../RelatedSelect";
import type { Department, Project, WorkspaceAllocation, WorkspaceProject, WorkspaceRequirement } from "../api/types";
import { explainError, formatManila, inputDateTime, isUncertain, projectPeriod, toDateTimeInput } from "../domain";
import { Button, DetailSheet, GlassPanel, LoadState, Modal, MutationReviewNotice, Notice, PageHeader, PageNavigation, PanelHeading, usePageCursors, StatusBadge } from "../ui";

function defaultStart(): string {
  const d = new Date(Date.now() + 48 * 60 * 60 * 1000);
  const parts = Object.fromEntries(new Intl.DateTimeFormat("en-CA", { timeZone: "Asia/Manila", year: "numeric", month: "2-digit", day: "2-digit", hour: "2-digit", minute: "2-digit", hourCycle: "h23" }).formatToParts(d).map((p) => [p.type, p.value]));
  return `${parts.year}-${parts.month}-${parts.day}T${parts.hour}:${parts.minute}`;
}

export function ProjectsPage() {
  const { session, organizations } = useAuth();
  const role = organizations.find(org => org.id === session?.activeOrgId)?.role;
  const canManage = role === "OWNER" || role === "ADMIN";
  const canReadAllocations = canManage || role === "CUSTODIAN";
  const [projects, setProjects] = useState<WorkspaceProject[]>([]);
  const [loadState, setLoadState] = useState<"loading" | "error" | "ready">("loading");
  const [loadError, setLoadError] = useState("");
  const [departmentFilter, setDepartmentFilter] = useState<Department | null>(null);
  const departmentFilterId = departmentFilter?.id;
  const [periodFilter, setPeriodFilter] = useState("ALL");
  const [search, setSearch] = useState("");
  const [reload, setReload] = useState(0);
  const { cursor, page, move } = usePageCursors(JSON.stringify([departmentFilterId, periodFilter, search]));
  const [nextCursor, setNextCursor] = useState<string | null>(null);
  const goToPage = (next: string | null) => { setLoadState("loading"); move(next); };
  const [department, setDepartment] = useState<Department | null>(null);
  const [name, setName] = useState("");
  const [startAt, setStartAt] = useState(defaultStart);
  const [endAt, setEndAt] = useState("");
  const [editing, setEditing] = useState<Project | null>(null);
  const [editName, setEditName] = useState("");
  const [editStart, setEditStart] = useState("");
  const [editEnd, setEditEnd] = useState("");
  const [deleting, setDeleting] = useState<Project | null>(null);
  const [selected, setSelected] = useState<WorkspaceProject | null>(null);
  const selectedId = selected?.id;
  const [detailRequirements, setDetailRequirements] = useState<WorkspaceRequirement[]>([]);
  const [detailAllocations, setDetailAllocations] = useState<WorkspaceAllocation[]>([]);
  const requirementPage = usePageCursors(String(selectedId ?? ""));
  const historyPage = usePageCursors(String(selectedId ?? ""));
  const requirementCursor = requirementPage.cursor;
  const historyCursor = historyPage.cursor;
  const [nextRequirementCursor, setNextRequirementCursor] = useState<string | null>(null);
  const [nextHistoryCursor, setNextHistoryCursor] = useState<string | null>(null);
  const [historyState, setHistoryState] = useState<"loading" | "error" | "ready">("loading");
  const [historyReload, setHistoryReload] = useState(0);
  const [detailState, setDetailState] = useState<"loading" | "error" | "ready">("loading");
  const [detailError, setDetailError] = useState("");
  const [historyError, setHistoryError] = useState("");
  const [detailReload, setDetailReload] = useState(0);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [postReviewRequired, setPostReviewRequired] = useState(false);

  useEffect(() => {
    const controller = new AbortController();
    workspaceApi.projects(cursor, { departmentId: departmentFilterId, search, period: periodFilter === "ALL" ? undefined : periodFilter },
      controller.signal).then(result => {
      setProjects(result.items); setNextCursor(result.nextCursor); setLoadState("ready"); setLoadError("");
    }).catch((reason: unknown) => {
      if (reason instanceof DOMException && reason.name === "AbortError") return;
      setLoadError(explainError(reason)); setLoadState("error");
    });
    return () => controller.abort();
  }, [cursor, departmentFilterId, periodFilter, search, reload]);

  useEffect(() => {
    if (selectedId === undefined) return;
    const controller = new AbortController();
    workspaceApi.requirements(requirementCursor, { projectId: selectedId }, controller.signal).then(result => {
      setDetailRequirements(result.items); setNextRequirementCursor(result.nextCursor);
      setDetailState("ready"); setDetailError("");
    }).catch((failure: unknown) => {
      if (!controller.signal.aborted) { setDetailError(explainError(failure)); setDetailState("error"); }
    });
    return () => controller.abort();
  }, [selectedId, detailReload, requirementCursor]);

  useEffect(() => {
    if (selectedId === undefined || !canReadAllocations) return;
    const controller = new AbortController();
    workspaceApi.allocations(historyCursor, { projectId: selectedId }, controller.signal).then(result => {
      setDetailAllocations(result.items); setNextHistoryCursor(result.nextCursor);
      setHistoryState("ready"); setHistoryError("");
    }).catch((failure: unknown) => {
      if (!controller.signal.aborted) { setHistoryError(explainError(failure)); setHistoryState("error"); }
    });
    return () => controller.abort();
  }, [selectedId, historyReload, canReadAllocations, historyCursor]);

  const ordered = projects;
  const selectedDepartmentId = department?.id;
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

  return (
    <section className="page-shell" aria-labelledby="projects-title">
      <PageHeader eyebrow="Workspace · activity planning" title="Projects" description="Plan each activity in Philippine time and keep its linked needs and history together."
        action={<div className="header-stat"><span>{loadState === "ready" ? projects.length : "—"}</span><small>projects in view</small></div>} />
      {notice && <Notice tone="success">{notice}</Notice>}{error && <Notice tone="danger">{error}</Notice>}
      <MutationReviewNotice required={postReviewRequired} ready={loadState === "ready"} onReviewed={() => setPostReviewRequired(false)} />
      <div className="content-grid project-grid">
        {canManage ? <GlassPanel className="form-panel" tilt={false}>
          <PanelHeading kicker="Planning desk" title="Create a project" />
          <form className="stack-form" onSubmit={createProject}>
            <RelatedSelect id="project-department" label="Department" selection={department}
              onChange={setDepartment} load={workspaceApi.departments} optionLabel={row => row.name} reload={reload} />
            <label htmlFor="project-name">Project name</label><input id="project-name" maxLength={100} value={name} onChange={(event) => setName(event.target.value)} required />
            <label htmlFor="project-start">Starts · Philippine time</label><input id="project-start" type="datetime-local" step="1" value={startAt} onChange={(event) => setStartAt(event.target.value)} required />
            <label htmlFor="project-end">Ends · Philippine time</label><input id="project-end" type="datetime-local" step="1" value={endAt} onChange={(event) => setEndAt(event.target.value)} required />
            <p className="field-hint">Date-times are sent as Asia/Manila wall time (YYYY-MM-DDTHH:mm:ss).</p>
            <Button variant="primary" magnetic type="submit" disabled={busy || postReviewRequired || !selectedDepartmentId || !name.trim() || !startAt || !endAt}>{busy ? "Saving…" : "Create project"}</Button>
          </form>
        </GlassPanel> : <GlassPanel className="form-panel" tilt={false}><PanelHeading kicker="Read only" title="Project access" /><p>Your role can read project schedules but cannot create, edit, or delete Projects.</p></GlassPanel>}

        <GlassPanel className="list-panel" tilt={false}>
          <PanelHeading kicker="Activity calendar" title="Project schedule"><Button variant="quiet" disabled={loadState === "loading"} onClick={refresh}>Refresh</Button></PanelHeading>
          <div className="search-filter-bar project-filters"><label htmlFor="project-search">Search projects</label><input id="project-search" type="search" value={search} maxLength={100} onChange={(event) => { setLoadState("loading"); setSearch(event.target.value); }} placeholder="Name or department" />
            <RelatedSelect id="project-department-filter" label="Filter by Department" selection={departmentFilter}
              onChange={row => { setLoadState("loading"); setDepartmentFilter(row); }}
              load={workspaceApi.departments} optionLabel={row => row.name} allLabel="All departments" reload={reload} />
            <label htmlFor="project-period-filter">Period</label><select id="project-period-filter" value={periodFilter} onChange={(event) => { setLoadState("loading"); setPeriodFilter(event.target.value); }}><option value="ALL">All periods</option><option value="UPCOMING">Upcoming</option><option value="ACTIVE">Active</option><option value="ENDED">Ended</option></select></div>
          <LoadState state={loadState} error={loadError} onRetry={refresh}>
            {projects.length === 0 ? <div className="state-block state-empty"><span className="empty-symbol" aria-hidden="true">▤</span><h3>No projects yet</h3><p>Choose a department and create the first scheduled activity.</p></div>
              : ordered.length === 0 ? <div className="state-block state-empty"><h3>No matching projects</h3><p>Change the search or filters to view other records.</p></div>
                : <div className="project-list">{ordered.map((project) => {
                  const status = projectPeriod(project);
                  const department = project.departmentName;
                  return <article className="project-card" key={project.id}>
                    <div className="project-card-mark" aria-hidden="true">▤</div>
                    <div className="project-card-main"><div className="project-card-heading"><h3>{project.name}</h3><StatusBadge tone={status === "ACTIVE" ? "success" : status === "UPCOMING" ? "blue" : "neutral"}>{status.toLowerCase()}</StatusBadge></div>
                      <p>{department}</p><div className="project-dates"><span><small>Starts</small>{formatManila(project.startAt)}</span><span><small>Ends</small>{formatManila(project.endAt)}</span></div></div>
                    <div className="project-actions"><Button variant="quiet" aria-label={`View ${project.name}`} onClick={() => { setSelected(project); setDetailState("loading"); setHistoryState("loading"); }}>Details</Button>{canManage && <><Button variant="quiet" disabled={postReviewRequired} aria-label={`Edit ${project.name}`} onClick={() => beginEdit(project)}>Edit</Button><Button variant="quiet" disabled={postReviewRequired} className="button-text-danger" aria-label={`Delete ${project.name}`} onClick={() => setDeleting(project)}>Delete</Button></>}</div>
                  </article>;
                })}</div>}
          </LoadState>
          <PageNavigation label="Project pages" page={page} count={loadState === "ready" ? projects.length : 0}
            hasNext={loadState === "ready" && nextCursor !== null} loading={loadState === "loading"} unavailable={loadState === "error"}
            onPrevious={() => goToPage(null)} onNext={() => { if (nextCursor) goToPage(nextCursor); }} />
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
        <div className="detail-grid"><div><span>Department</span><strong>{selected.departmentName}</strong></div><div><span>Period</span><strong>{projectPeriod(selected).toLowerCase()}</strong></div><div><span>Starts</span><strong>{formatManila(selected.startAt)}</strong></div><div><span>Ends</span><strong>{formatManila(selected.endAt)}</strong></div></div>
        <section className="sheet-section"><h3>{canReadAllocations ? "Requirements and allocation history" : "Requirements"}</h3>
          <LoadState state={detailState} error={detailError} onRetry={refreshDetails}>
            {detailRequirements.length === 0 ? <p className="quiet-note">This Project has no more Requirements.</p>
              : <div className="sheet-record-list">{detailRequirements.map(requirement => <article key={requirement.id}>
                <div><strong>{requirement.resourceName}</strong><small>{requirement.quantity} required · {requirement.allocatedQuantity} covered · {requirement.shortageQuantity} shortage</small></div>
              </article>)}</div>}
          </LoadState>
          <PageNavigation label="Project Requirement pages" page={requirementPage.page}
            count={detailState === "ready" ? detailRequirements.length : 0} loading={detailState === "loading"}
            unavailable={detailState === "error"} hasNext={detailState === "ready" && !!nextRequirementCursor}
            onPrevious={() => { setDetailState("loading"); requirementPage.move(null); }}
            onNext={() => { if (nextRequirementCursor) { setDetailState("loading"); requirementPage.move(nextRequirementCursor); } }} />
          {canReadAllocations && <section className="sheet-section" aria-label="Allocation history">
            <h4>Allocation batches</h4>
            <LoadState state={historyState} error={historyError} onRetry={() => {
              setHistoryState("loading"); setHistoryReload(value => value + 1);
            }}>
              <div className="sheet-batches">{detailAllocations.length ? detailAllocations.map(batch =>
                <span key={batch.id}>{batch.requirement.resourceName} · A{batch.id} · {batch.quantity} · {batch.status.toLowerCase()}</span>)
                : <span>No Allocation batches on this page</span>}</div>
            </LoadState>
            <PageNavigation label="Project Allocation history pages" page={historyPage.page}
              count={historyState === "ready" ? detailAllocations.length : 0}
              loading={historyState === "loading"} unavailable={historyState === "error"}
              hasNext={historyState === "ready" && !!nextHistoryCursor}
              onPrevious={() => { setHistoryState("loading"); historyPage.move(null); }}
              onNext={() => { if (nextHistoryCursor) { setHistoryState("loading"); historyPage.move(nextHistoryCursor); } }} />
          </section>}
        </section>
      </DetailSheet>}
    </section>
  );
}
