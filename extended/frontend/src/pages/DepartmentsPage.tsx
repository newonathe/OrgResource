import { useEffect, useState, type FormEvent } from "react";
import { workspaceApi, api } from "../api/client";
import { useAuth } from "../auth/AuthContext";
import type { Department } from "../api/types";
import { explainError, isUncertain } from "../domain";
import { Button, GlassPanel, LoadState, Modal, MutationReviewNotice, Notice, PageHeader, PageNavigation, PanelHeading, usePageCursors } from "../ui";

export function DepartmentsPage() {
  const { session, organizations } = useAuth();
  const role = organizations.find(org => org.id === session?.activeOrgId)?.role;
  const canManage = role === "OWNER" || role === "ADMIN";
  const [departments, setDepartments] = useState<Department[]>([]);
  const [loadState, setLoadState] = useState<"loading" | "error" | "ready">("loading");
  const [loadError, setLoadError] = useState("");
  const [search, setSearch] = useState("");
  const [name, setName] = useState("");
  const [editing, setEditing] = useState<Department | null>(null);
  const [editName, setEditName] = useState("");
  const [deleting, setDeleting] = useState<Department | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [postReviewRequired, setPostReviewRequired] = useState(false);
  const [reload, setReload] = useState(0);
  const { cursor, page, move } = usePageCursors(search);
  const [nextCursor, setNextCursor] = useState<string | null>(null);
  const goToPage = (next: string | null) => { setLoadState("loading"); move(next); };

  useEffect(() => {
    const controller = new AbortController();
    workspaceApi.departments(cursor, search, controller.signal).then((result) => {
      setDepartments(result.items);
      setNextCursor(result.nextCursor);
      setLoadState("ready");
      setLoadError("");
    }).catch((reason: unknown) => {
      if (reason instanceof DOMException && reason.name === "AbortError") return;
      setLoadError(explainError(reason));
      setLoadState("error");
    });
    return () => controller.abort();
  }, [cursor, search, reload]);

  const refresh = () => { setLoadState("loading"); setReload((value) => value + 1); };

  const ordered = departments;

  async function submitCreate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (busy || postReviewRequired || !name.trim()) return;
    setBusy(true); setError(""); setNotice("");
    try {
      await api.createDepartment(name.trim());
      setName(""); setNotice("Department saved."); refresh();
    } catch (reason) { if (isUncertain(reason)) { setPostReviewRequired(true); refresh(); } else setError(explainError(reason)); }
    finally { setBusy(false); }
  }

  async function submitUpdate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (busy || postReviewRequired || !editing || !editName.trim()) return;
    setBusy(true); setError(""); setNotice("");
    try {
      await api.updateDepartment(editing.id, editName.trim());
      setEditing(null); setNotice("Department updated."); refresh();
    } catch (reason) { if (isUncertain(reason)) { setPostReviewRequired(true); refresh(); } else setError(explainError(reason)); }
    finally { setBusy(false); }
  }

  async function confirmDelete() {
    if (busy || postReviewRequired || !deleting) return;
    setBusy(true); setError(""); setNotice("");
    try {
      await api.deleteDepartment(deleting.id);
      setDeleting(null); setNotice("Department deleted."); refresh();
    } catch (reason) {
      if (isUncertain(reason)) { setPostReviewRequired(true); refresh(); }
      else { setError(explainError(reason)); setDeleting(null); }
    }
    finally { setBusy(false); }
  }

  return (
    <section className="page-shell" aria-labelledby="departments-title">
      <PageHeader eyebrow="Organization · structure" title="Departments" description="Keep the organization’s working groups clear and ready for project planning."
        action={<div className="header-stat"><span>{loadState === "ready" ? departments.length : "—"}</span><small>departments in view</small></div>} />
      {notice && <Notice tone="success">{notice}</Notice>}
      {error && <Notice tone="danger">{error}</Notice>}
      <MutationReviewNotice required={postReviewRequired} ready={loadState === "ready"} onReviewed={() => setPostReviewRequired(false)} />
      <div className="content-grid departments-grid">
        {canManage ? <GlassPanel className="form-panel" tilt={false}>
          <PanelHeading kicker="Organization" title="Add a department" />
          <p className="panel-copy">Use the name your organization recognizes. Duplicate names are checked by the server.</p>
          <form className="stack-form" onSubmit={submitCreate}>
            <label htmlFor="department-name">Department name</label>
            <input id="department-name" value={name} maxLength={100} onChange={(event) => setName(event.target.value)} required />
            <Button type="submit" variant="primary" magnetic disabled={busy || postReviewRequired || !name.trim()}>{busy ? "Saving…" : "Add department"}</Button>
          </form>
          <div className="form-footnote"><span className="small-seal" aria-hidden="true">i</span><span>Projects remain linked to their original department.</span></div>
        </GlassPanel> : <GlassPanel className="form-panel" tilt={false}><PanelHeading kicker="Read only" title="Department access" /><p>Your role can read Departments but cannot create or change them.</p></GlassPanel>}

        <GlassPanel className="list-panel" tilt={false}>
          <PanelHeading kicker="Organization directory" title="Department list">
            <Button variant="quiet" onClick={refresh} disabled={loadState === "loading"}>Refresh</Button>
          </PanelHeading>
          <div className="search-filter-bar"><label htmlFor="department-search">Search departments</label><input id="department-search" type="search" value={search} maxLength={100} onChange={(event) => { setLoadState("loading"); setSearch(event.target.value); }} placeholder="Search by name" /></div>
          <LoadState state={loadState} error={loadError} onRetry={refresh}>
            {departments.length === 0 ? <div className="state-block state-empty"><span className="empty-symbol" aria-hidden="true">⌘</span><h3>No departments yet</h3><p>Add the first group to start organizing project work.</p></div>
              : ordered.length === 0 ? <div className="state-block state-empty"><h3>No matching departments</h3><p>Try a different search term.</p></div>
                : <div className="table-scroll"><table className="data-table"><thead><tr><th scope="col">Department</th><th scope="col">Record</th><th scope="col"><span className="sr-only">Actions</span></th></tr></thead>
                  <tbody>{ordered.map((department) => <tr key={department.id}>
                    <td>{editing?.id === department.id ? <form className="inline-edit" onSubmit={submitUpdate}>
                      <label htmlFor="edit-department-name">Department name</label><input id="edit-department-name" value={editName} maxLength={100} onChange={(event) => setEditName(event.target.value)} autoFocus required />
                      <Button type="submit" variant="primary" disabled={busy || postReviewRequired || !editName.trim()}>Save changes</Button>
                      <Button type="button" variant="quiet" disabled={busy} onClick={() => setEditing(null)}>Cancel</Button>
                    </form> : <div className="record-cell"><span className="record-symbol" aria-hidden="true">⌘</span><span><strong>{department.name}</strong><small>Organization group</small></span></div>}</td>
                    {editing?.id !== department.id && <><td><span className="mono-id">D{String(department.id).padStart(3, "0")}</span></td>{canManage && <td><div className="row-actions">
                      <Button variant="quiet" disabled={busy || postReviewRequired} aria-label={`Edit ${department.name}`} onClick={() => { setEditing(department); setEditName(department.name); setError(""); }}>Edit</Button>
                      <Button variant="quiet" className="button-text-danger" disabled={busy || postReviewRequired} aria-label={`Delete ${department.name}`} onClick={() => { setDeleting(department); setError(""); }}>Delete</Button>
                    </div></td>}</>}
                  </tr>)}</tbody></table></div>}
          </LoadState>
          <PageNavigation label="Departments pages" page={page} count={loadState === "ready" ? departments.length : 0}
            hasNext={loadState === "ready" && nextCursor !== null} loading={loadState === "loading"} unavailable={loadState === "error"}
            onPrevious={() => goToPage(null)} onNext={() => { if (nextCursor) goToPage(nextCursor); }} />
        </GlassPanel>
      </div>

      {deleting && <Modal title="Delete department?" onClose={() => setDeleting(null)}>
        <p className="modal-copy">Delete <strong>{deleting.name}</strong>? The server will refuse if Projects still reference this department.</p>
        <div className="modal-actions"><Button variant="quiet" disabled={busy} onClick={() => setDeleting(null)}>Keep department</Button><Button variant="danger" disabled={busy || postReviewRequired} onClick={() => void confirmDelete()}>{busy ? "Checking…" : "Confirm delete"}</Button></div>
      </Modal>}
    </section>
  );
}
