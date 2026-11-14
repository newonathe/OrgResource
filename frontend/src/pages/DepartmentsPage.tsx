import { FormEvent, useEffect, useMemo, useState } from "react";
import { ApiError, api } from "../api/client";
import type { Department } from "../api/types";

export function DepartmentsPage() {
  const [departments, setDepartments] = useState<Department[]>([]);
  const [loadState, setLoadState] = useState<"loading" | "ready" | "error">("loading");
  const [loadError, setLoadError] = useState("");
  const [reloadToken, setReloadToken] = useState(0);
  const [search, setSearch] = useState("");
  const [newName, setNewName] = useState("");
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editName, setEditName] = useState("");
  const [formError, setFormError] = useState("");
  const [busy, setBusy] = useState(false);
  const [notice, setNotice] = useState("");

  function refreshDepartments() {
    setLoadState("loading");
    setReloadToken((value) => value + 1);
  }

  useEffect(() => {
    const controller = new AbortController();
    api.departments(controller.signal)
      .then((rows) => {
        setDepartments(rows);
        setLoadState("ready");
      })
      .catch((error: unknown) => {
        if (error instanceof DOMException && error.name === "AbortError") return;
        setLoadError(errorMessage(error));
        setLoadState("error");
      });
    return () => controller.abort();
  }, [reloadToken]);

  const visibleDepartments = useMemo(() => departments
    .filter((department) => department.name.toLocaleLowerCase().includes(search.trim().toLocaleLowerCase()))
    .sort((left, right) => left.name.localeCompare(right.name) || left.id - right.id), [departments, search]);

  async function createDepartment(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setBusy(true);
    setFormError("");
    setNotice("");
    try {
      await api.createDepartment(newName);
      setNewName("");
      setNotice("Department saved.");
      refreshDepartments();
    } catch (error) {
      setFormError(errorMessage(error));
    } finally {
      setBusy(false);
    }
  }

  async function saveEdit(event: FormEvent<HTMLFormElement>, id: number) {
    event.preventDefault();
    setBusy(true);
    setFormError("");
    setNotice("");
    try {
      await api.updateDepartment(id, editName);
      setEditingId(null);
      setNotice("Department updated.");
      refreshDepartments();
    } catch (error) {
      setFormError(errorMessage(error));
    } finally {
      setBusy(false);
    }
  }

  async function removeDepartment(department: Department) {
    if (!window.confirm(`Delete “${department.name}”? Departments with linked projects cannot be deleted.`)) return;
    setBusy(true);
    setFormError("");
    setNotice("");
    try {
      await api.deleteDepartment(department.id);
      setNotice("Department deleted.");
      refreshDepartments();
    } catch (error) {
      setFormError(errorMessage(error));
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="page-shell" aria-labelledby="departments-title">
      <div className="page-heading">
        <div>
          <p className="eyebrow">Your organization</p>
          <h1 id="departments-title">Departments</h1>
          <p className="lede">Set up the groups that plan and share resources.</p>
        </div>
        <div className="count-card"><span>{loadState === "ready" ? departments.length : "—"}</span><small>departments</small></div>
      </div>

      {notice && <p className="notice success" role="status">{notice}</p>}
      {formError && <p className="notice error" role="alert">{formError}</p>}

      <div className="two-column-layout">
        <section className="panel create-panel" aria-labelledby="new-department-title">
          <p className="section-kicker">Add a group</p>
          <h2 id="new-department-title">New department</h2>
          <p className="panel-copy">Use a name people in your organization will recognize.</p>
          <form onSubmit={createDepartment} className="stack-form">
            <label htmlFor="department-name">Department name</label>
            <input id="department-name" maxLength={100} value={newName}
              onChange={(event) => setNewName(event.target.value)} required />
            <button className="button primary" disabled={busy || !newName.trim()} type="submit">
              {busy ? "Saving…" : "Add department"}
            </button>
          </form>
        </section>

        <section className="panel list-panel" aria-labelledby="department-list-title">
          <div className="list-heading">
            <div>
              <p className="section-kicker">Organization list</p>
              <h2 id="department-list-title">All departments</h2>
            </div>
            <button className="button quiet-button" type="button" onClick={refreshDepartments}
              disabled={loadState === "loading"} aria-label="Refresh departments">Refresh</button>
          </div>
          <label className="search-label" htmlFor="department-search">Find a department</label>
          <input id="department-search" className="search-input" type="search" placeholder="Search by name"
            value={search} onChange={(event) => setSearch(event.target.value)} />
          {loadState === "loading" && <p className="state-message" role="status">Loading departments…</p>}
          {loadState === "error" && <div className="empty-state error-state" role="alert">
            <h3>Could not load departments</h3><p>{loadError}</p>
            <button className="button secondary" type="button" onClick={refreshDepartments}>Retry</button>
          </div>}
          {loadState === "ready" && departments.length === 0 && <div className="empty-state">
            <span className="empty-icon" aria-hidden="true">＋</span><h3>No departments yet</h3>
            <p>Add the first department to begin organizing project work.</p>
          </div>}
          {loadState === "ready" && departments.length > 0 && visibleDepartments.length === 0 &&
            <p className="state-message">No departments match “{search}”.</p>}
          {loadState === "ready" && visibleDepartments.length > 0 && <ul className="record-list">
            {visibleDepartments.map((department) => <li className="record-row" key={department.id}>
              {editingId === department.id ? <form className="edit-form" onSubmit={(event) => saveEdit(event, department.id)}>
                <label htmlFor={`edit-department-${department.id}`}>Department name</label>
                <input id={`edit-department-${department.id}`} maxLength={100} value={editName}
                  onChange={(event) => setEditName(event.target.value)} required autoFocus />
                <div className="row-actions">
                  <button className="button primary small-button" disabled={busy || !editName.trim()} type="submit">Save</button>
                  <button className="button quiet-button small-button" disabled={busy} type="button" onClick={() => setEditingId(null)}>Cancel</button>
                </div>
              </form> : <>
                <div className="record-icon" aria-hidden="true">{department.name.slice(0, 1).toLocaleUpperCase()}</div>
                <div className="record-main"><strong>{department.name}</strong><span>Department #{department.id}</span></div>
                <div className="row-actions">
                  <button className="text-button" disabled={busy} type="button" onClick={() => {
                    setEditingId(department.id); setEditName(department.name); setFormError(""); setNotice("");
                  }}>Edit <span className="sr-only">{department.name}</span></button>
                  <button className="text-button destructive" disabled={busy} type="button" onClick={() => void removeDepartment(department)}>
                    Delete <span className="sr-only">{department.name}</span>
                  </button>
                </div>
              </>}
            </li>)}
          </ul>}
          <p className="list-footnote">A department with one or more projects must stay in place.</p>
        </section>
      </div>
    </section>
  );
}

function errorMessage(error: unknown): string {
  if (error instanceof ApiError) return `${error.message} (${error.code})`;
  return "Something went wrong. Review the form and try again.";
}
