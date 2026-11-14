import { FormEvent, useEffect, useMemo, useState } from "react";
import { ApiError, api } from "../api/client";
import type { Resource, ResourceType } from "../api/types";

export function ResourcesPage() {
  const [resources, setResources] = useState<Resource[]>([]);
  const [loadState, setLoadState] = useState<"loading" | "ready" | "error">("loading");
  const [loadError, setLoadError] = useState("");
  const [reloadToken, setReloadToken] = useState(0);
  const [filter, setFilter] = useState<"ALL" | ResourceType>("ALL");
  const [newName, setNewName] = useState("");
  const [newType, setNewType] = useState<ResourceType>("REUSABLE");
  const [newQuantity, setNewQuantity] = useState("0");
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editName, setEditName] = useState("");
  const [editQuantity, setEditQuantity] = useState("");
  const [reviewRequiredId, setReviewRequiredId] = useState<number | null>(null);
  const [formError, setFormError] = useState("");
  const [notice, setNotice] = useState("");
  const [busy, setBusy] = useState(false);

  function refreshResources() {
    setLoadState("loading");
    setReloadToken((value) => value + 1);
  }

  useEffect(() => {
    const controller = new AbortController();
    api.resources(filter === "ALL" ? undefined : filter, controller.signal)
      .then((rows) => {
        setResources(rows);
        setLoadState("ready");
      })
      .catch((error: unknown) => {
        if (error instanceof DOMException && error.name === "AbortError") return;
        setLoadError(errorMessage(error));
        setLoadState("error");
      });
    return () => controller.abort();
  }, [filter, reloadToken]);

  const orderedResources = useMemo(() => [...resources]
    .sort((left, right) => left.name.localeCompare(right.name) || left.id - right.id), [resources]);

  async function createResource(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const quantity = Number(newQuantity);
    if (!Number.isInteger(quantity) || quantity < 0 || quantity > 1_000_000) {
      setFormError("Enter a whole-unit quantity from 0 to 1,000,000.");
      return;
    }
    setBusy(true);
    setFormError("");
    setNotice("");
    try {
      await api.createResource({ name: newName, type: newType, totalQuantity: quantity });
      setNewName("");
      setNewQuantity("0");
      setNotice("Resource saved.");
      refreshResources();
    } catch (error) {
      setFormError(errorMessage(error));
    } finally {
      setBusy(false);
    }
  }

  function beginEdit(resource: Resource) {
    setEditingId(resource.id);
    setEditName(resource.name);
    setEditQuantity(String(resource.totalQuantity));
    setReviewRequiredId(null);
    setFormError("");
    setNotice("");
  }

  async function saveEdit(event: FormEvent<HTMLFormElement>, resource: Resource) {
    event.preventDefault();
    const quantity = Number(editQuantity);
    if (!Number.isInteger(quantity) || quantity < 0 || quantity > 1_000_000) {
      setFormError("Enter a whole-unit quantity from 0 to 1,000,000.");
      return;
    }
    setBusy(true);
    setFormError("");
    setNotice("");
    try {
      await api.updateResource(resource.id, {
        name: editName,
        totalQuantity: quantity,
        expectedTotalQuantity: resource.totalQuantity,
      });
      setEditingId(null);
      setNotice("Resource updated.");
      refreshResources();
    } catch (error) {
      setFormError(errorMessage(error));
      if (error instanceof ApiError && error.code === "STALE_RESOURCE_QUANTITY") {
        setNotice("The server quantity changed after this row loaded. Fresh server values have been fetched; review the current record before retrying.");
        setReviewRequiredId(resource.id);
        refreshResources();
      }
    } finally {
      setBusy(false);
    }
  }

  async function removeResource(resource: Resource) {
    const prompt = resource.totalQuantity > 0
      ? `“${resource.name}” has ${resource.totalQuantity} units. The server will refuse deletion until its stock is zero. Continue to check?`
      : `Delete “${resource.name}”? Resources referenced by requirements cannot be deleted.`;
    if (!window.confirm(prompt)) return;
    setBusy(true);
    setFormError("");
    setNotice("");
    try {
      await api.deleteResource(resource.id);
      setNotice("Resource deleted.");
      refreshResources();
    } catch (error) {
      setFormError(errorMessage(error));
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="page-shell" aria-labelledby="resources-title">
      <div className="page-heading">
        <div>
          <p className="eyebrow">Shared inventory</p>
          <h1 id="resources-title">Resources</h1>
          <p className="lede">Track what your organization owns and what remains available.</p>
        </div>
        <div className="count-card"><span>{loadState === "ready" ? resources.length : "—"}</span><small>resources</small></div>
      </div>

      {notice && <p className="notice success" role="status">{notice}</p>}
      {formError && <p className="notice error" role="alert">{formError}</p>}

      <div className="two-column-layout resource-layout">
        <section className="panel create-panel" aria-labelledby="new-resource-title">
          <p className="section-kicker">Add to inventory</p>
          <h2 id="new-resource-title">New resource</h2>
          <p className="panel-copy">Availability is calculated from saved allocations by the backend.</p>
          <form onSubmit={createResource} className="stack-form">
            <label htmlFor="resource-name">Resource name</label>
            <input id="resource-name" maxLength={100} value={newName}
              onChange={(event) => setNewName(event.target.value)} required />
            <label htmlFor="resource-type">Resource type</label>
            <select id="resource-type" value={newType} onChange={(event) => setNewType(event.target.value as ResourceType)}>
              <option value="REUSABLE">Reusable · returned after use</option>
              <option value="CONSUMABLE">Consumable · used up</option>
            </select>
            <label htmlFor="resource-quantity">Initial total quantity</label>
            <input id="resource-quantity" type="number" min="0" max="1000000" step="1"
              value={newQuantity} onChange={(event) => setNewQuantity(event.target.value)} required />
            <button className="button primary" disabled={busy || !newName.trim()} type="submit">
              {busy ? "Saving…" : "Add resource"}
            </button>
          </form>
        </section>

        <section className="panel list-panel resource-list-panel" aria-labelledby="resource-list-title">
          <div className="list-heading">
            <div><p className="section-kicker">Stock overview</p><h2 id="resource-list-title">All resources</h2></div>
            <button className="button quiet-button" type="button" onClick={refreshResources}
              disabled={loadState === "loading"} aria-label="Refresh resources">Refresh</button>
          </div>
          <div className="filter-row">
            <label htmlFor="resource-filter">Show</label>
            <select id="resource-filter" value={filter} onChange={(event) => {
              setLoadState("loading");
              setFilter(event.target.value as "ALL" | ResourceType);
            }}>
              <option value="ALL">All types</option><option value="REUSABLE">Reusable</option><option value="CONSUMABLE">Consumable</option>
            </select>
          </div>
          {loadState === "loading" && <p className="state-message" role="status">Loading resources…</p>}
          {loadState === "error" && <div className="empty-state error-state" role="alert">
            <h3>Could not load resources</h3><p>{loadError}</p>
            <button className="button secondary" type="button" onClick={refreshResources}>Retry</button>
          </div>}
          {loadState === "ready" && resources.length === 0 && <div className="empty-state">
            <span className="empty-icon" aria-hidden="true">◇</span><h3>No resources yet</h3>
            <p>Add the first item your organization plans to share.</p>
          </div>}
          {loadState === "ready" && orderedResources.length > 0 && <div className="resource-table-wrap">
            <table className="resource-table">
              <thead><tr><th scope="col">Resource</th><th scope="col">Type</th><th scope="col">Available</th><th scope="col"><span className="sr-only">Actions</span></th></tr></thead>
              <tbody>{orderedResources.map((resource) => <tr key={resource.id}>
                <td colSpan={editingId === resource.id ? 4 : undefined}>
                  {editingId === resource.id ? <form className="resource-edit-form" onSubmit={(event) => saveEdit(event, resource)}>
                    <label htmlFor={`edit-resource-name-${resource.id}`}>Name</label>
                    <input id={`edit-resource-name-${resource.id}`} maxLength={100} value={editName}
                      onChange={(event) => setEditName(event.target.value)} required autoFocus />
                    <label htmlFor={`edit-resource-quantity-${resource.id}`}>Total quantity</label>
                    <input id={`edit-resource-quantity-${resource.id}`} type="number" min="0" max="1000000" step="1"
                      value={editQuantity} onChange={(event) => setEditQuantity(event.target.value)} required />
                    <p className="edit-context">Expected current total: {resource.totalQuantity}. Type stays {resource.type.toLowerCase()}.</p>
                    {reviewRequiredId === resource.id && <div className="review-guard" role="status">
                      <p>Current server total: <strong>{resource.totalQuantity}</strong> · Available: <strong>{resource.availableQuantity}</strong></p>
                      <button className="button secondary small-button" disabled={busy} type="button"
                        onClick={() => setReviewRequiredId(null)}>I reviewed these current values</button>
                    </div>}
                    <div className="row-actions">
                      <button className="button primary small-button" disabled={busy || !editName.trim() || reviewRequiredId === resource.id} type="submit">Save changes</button>
                      <button className="button quiet-button small-button" disabled={busy} type="button" onClick={() => { setEditingId(null); setReviewRequiredId(null); }}>Cancel</button>
                    </div>
                  </form> : <div className="resource-name-cell">
                    <div className="record-icon resource-icon" aria-hidden="true">{resource.type === "REUSABLE" ? "↻" : "◇"}</div>
                    <div className="record-main"><strong>{resource.name}</strong><span>{resource.totalQuantity} total units</span></div>
                  </div>}
                </td>
                {editingId !== resource.id && <>
                  <td><span className={`type-badge ${resource.type.toLowerCase()}`}>{resource.type === "REUSABLE" ? "Reusable" : "Consumable"}</span></td>
                  <td><strong className="availability-number">{resource.availableQuantity}</strong><span className="availability-label"> available</span></td>
                  <td><div className="row-actions">
                    <button className="text-button" disabled={busy} type="button" onClick={() => beginEdit(resource)}>Edit <span className="sr-only">{resource.name}</span></button>
                    <button className="text-button destructive" disabled={busy} type="button" onClick={() => void removeResource(resource)}>Delete <span className="sr-only">{resource.name}</span></button>
                  </div></td>
                </>}
              </tr>)}</tbody>
            </table>
          </div>}
          <p className="list-footnote">Availability comes from the saved backend records. It may be lower than the total when items are reserved or in custody.</p>
        </section>
      </div>
    </section>
  );
}

function errorMessage(error: unknown): string {
  if (error instanceof ApiError) return `${error.message} (${error.code})`;
  return "Something went wrong. Review the form and try again.";
}
