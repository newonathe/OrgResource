import { useEffect, useMemo, useState, type FormEvent } from "react";
import { api, ApiError } from "../api/client";
import type { Resource, ResourceAvailability, ResourceType } from "../api/types";
import { explainError, isUncertain } from "../domain";
import { Button, DetailSheet, GlassPanel, LoadState, Modal, MutationReviewNotice, Notice, PageHeader, PanelHeading, StatusBadge } from "../ui";

type AvailabilityResult =
  | { resourceId: number; status: "ready"; value: ResourceAvailability }
  | { resourceId: number; status: "error"; message: string };

export function ResourcesPage() {
  const [resources, setResources] = useState<Resource[]>([]);
  const [loadState, setLoadState] = useState<"loading" | "error" | "ready">("loading");
  const [loadError, setLoadError] = useState("");
  const [filter, setFilter] = useState<"ALL" | ResourceType>("ALL");
  const [search, setSearch] = useState("");
  const [reload, setReload] = useState(0);
  const [newName, setNewName] = useState("");
  const [newType, setNewType] = useState<ResourceType>("REUSABLE");
  const [newQuantity, setNewQuantity] = useState("0");
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editName, setEditName] = useState("");
  const [editQuantity, setEditQuantity] = useState("");
  const [reviewRequiredId, setReviewRequiredId] = useState<number | null>(null);
  const [selected, setSelected] = useState<Resource | null>(null);
  const [availabilityResult, setAvailabilityResult] = useState<AvailabilityResult | null>(null);
  const [availabilityReload, setAvailabilityReload] = useState(0);
  const [deleting, setDeleting] = useState<Resource | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [postReviewRequired, setPostReviewRequired] = useState(false);

  useEffect(() => {
    const controller = new AbortController();
    api.resources(filter === "ALL" ? undefined : filter, controller.signal).then((rows) => {
      setResources(rows); setLoadState("ready"); setLoadError("");
    }).catch((reason: unknown) => {
      if (reason instanceof DOMException && reason.name === "AbortError") return;
      setLoadError(explainError(reason)); setLoadState("error");
    });
    return () => controller.abort();
  }, [filter, reload]);

  const selectedId = selected?.id;
  useEffect(() => {
    if (selectedId === undefined) return;
    const controller = new AbortController();
    let active = true;
    api.resourceAvailability(selectedId, controller.signal).then((value) => {
      if (active) setAvailabilityResult({ resourceId: selectedId, status: "ready", value });
    }).catch((reason: unknown) => {
      if (reason instanceof DOMException && reason.name === "AbortError") return;
      if (active) setAvailabilityResult({ resourceId: selectedId, status: "error", message: explainError(reason) });
    });
    return () => { active = false; controller.abort(); };
  }, [selectedId, availabilityReload]);

  const ordered = useMemo(() => resources.filter((item) => item.name.toLowerCase().includes(search.trim().toLowerCase()))
    .sort((a, b) => a.name.localeCompare(b.name) || a.id - b.id), [resources, search]);
  const currentAvailability = selected && availabilityResult?.resourceId === selected.id ? availabilityResult : null;
  const availabilityState = currentAvailability?.status ?? "loading";
  const refresh = () => { setLoadState("loading"); setReload((value) => value + 1); };

  async function createResource(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const quantity = Number(newQuantity);
    if (busy || postReviewRequired || !newName.trim() || !Number.isInteger(quantity) || quantity < 0 || quantity > 1_000_000) return;
    setBusy(true); setError(""); setNotice("");
    try {
      await api.createResource({ name: newName.trim(), type: newType, totalQuantity: quantity });
      setNewName(""); setNewQuantity("0"); setNotice("Resource saved."); refresh();
    } catch (reason) { if (isUncertain(reason)) { setPostReviewRequired(true); refresh(); } else setError(explainError(reason)); }
    finally { setBusy(false); }
  }

  function beginEdit(resource: Resource) {
    setEditingId(resource.id); setEditName(resource.name); setEditQuantity(String(resource.totalQuantity));
    setReviewRequiredId(null); setError(""); setNotice("");
  }

  async function saveEdit(event: FormEvent<HTMLFormElement>, resource: Resource) {
    event.preventDefault();
    const quantity = Number(editQuantity);
    if (busy || postReviewRequired || !editName.trim() || !Number.isInteger(quantity) || quantity < 0 || quantity > 1_000_000 || reviewRequiredId === resource.id) return;
    setBusy(true); setError(""); setNotice("");
    try {
      await api.updateResource(resource.id, { name: editName.trim(), totalQuantity: quantity, expectedTotalQuantity: resource.totalQuantity });
      setEditingId(null); setReviewRequiredId(null); setNotice("Resource updated."); refresh();
    } catch (reason) {
      if (isUncertain(reason)) { setPostReviewRequired(true); refresh(); }
      else setError(explainError(reason));
      if (!isUncertain(reason) && reason instanceof ApiError && reason.code === "STALE_RESOURCE_QUANTITY") {
        setNotice("The server quantity changed after this row loaded. Fresh values were fetched; review them before retrying.");
        setReviewRequiredId(resource.id); refresh();
      }
    } finally { setBusy(false); }
  }

  async function confirmDelete() {
    if (busy || postReviewRequired || !deleting || deleting.totalQuantity > 0) return;
    setBusy(true); setError(""); setNotice("");
    try { await api.deleteResource(deleting.id); setDeleting(null); setNotice("Resource deleted."); refresh(); }
    catch (reason) {
      if (isUncertain(reason)) { setPostReviewRequired(true); refresh(); }
      else { setError(explainError(reason)); setDeleting(null); }
    }
    finally { setBusy(false); }
  }

  return (
    <section className="page-shell" aria-labelledby="resource-library-title">
      <PageHeader eyebrow="Workspace · shared inventory" title="Resource Library" description="Read physical totals and server-calculated availability for reusable and consumable stock."
        action={<div className="header-stat"><span>{loadState === "ready" ? resources.length : "—"}</span><small>resources in view</small></div>} />
      {notice && <Notice tone="success">{notice}</Notice>}
      {error && <Notice tone="danger">{error}</Notice>}
      <MutationReviewNotice required={postReviewRequired} ready={loadState === "ready"} onReviewed={() => setPostReviewRequired(false)} />
      <div className="content-grid resource-grid">
        <GlassPanel className="form-panel" tilt={false}>
          <PanelHeading kicker="Add to inventory" title="New resource" />
          <p className="panel-copy">Availability is calculated from saved Allocations by the backend. Quantities are whole units.</p>
          <form className="stack-form" onSubmit={createResource}>
            <label htmlFor="resource-name">Resource name</label><input id="resource-name" maxLength={100} value={newName} onChange={(event) => setNewName(event.target.value)} required />
            <label htmlFor="resource-type">Resource type</label><select id="resource-type" value={newType} onChange={(event) => setNewType(event.target.value as ResourceType)}>
              <option value="REUSABLE">Reusable · returned after use</option><option value="CONSUMABLE">Consumable · used up</option>
            </select>
            <label htmlFor="resource-quantity">Initial total quantity</label><input id="resource-quantity" type="number" min="0" max="1000000" step="1" value={newQuantity} onChange={(event) => setNewQuantity(event.target.value)} required />
            <Button variant="primary" magnetic type="submit" disabled={busy || postReviewRequired || !newName.trim()}>{busy ? "Saving…" : "Add resource"}</Button>
          </form>
          <div className="inventory-legend"><p><StatusBadge tone="blue">Reusable</StatusBadge> Borrowed items return to stock only after a usable return.</p><p><StatusBadge tone="gold">Consumable</StatusBadge> Consumption reduces the physical total.</p></div>
        </GlassPanel>

        <GlassPanel className="list-panel" tilt={false}>
          <PanelHeading kicker="Server inventory" title="All resources">
            <Button variant="quiet" onClick={refresh} disabled={loadState === "loading"}>Refresh</Button>
          </PanelHeading>
          <div className="search-filter-bar resource-filters">
            <label htmlFor="resource-search">Search resources</label><input id="resource-search" type="search" value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search by name" />
            <label htmlFor="resource-filter">Type</label><select id="resource-filter" value={filter} onChange={(event) => { setLoadState("loading"); setFilter(event.target.value as "ALL" | ResourceType); }}>
              <option value="ALL">All types</option><option value="REUSABLE">Reusable</option><option value="CONSUMABLE">Consumable</option>
            </select>
          </div>
          <LoadState state={loadState} error={loadError} onRetry={refresh}>
            {resources.length === 0 ? <div className="state-block state-empty"><span className="empty-symbol" aria-hidden="true">◈</span><h3>No resources in this view</h3><p>Add an item or change the current type filter.</p></div>
              : ordered.length === 0 ? <div className="state-block state-empty"><h3>No matching resources</h3><p>Try another search term.</p></div>
                : <div className="table-scroll"><table className="data-table resource-table"><thead><tr><th scope="col">Resource</th><th scope="col">Type</th><th scope="col">Physical total</th><th scope="col">Available</th><th scope="col"><span className="sr-only">Actions</span></th></tr></thead>
                  <tbody>{ordered.map((resource) => <tr key={resource.id}>
                    <td>{editingId === resource.id ? <form className="inline-edit resource-inline-edit" onSubmit={(event) => void saveEdit(event, resource)}>
                      <label htmlFor={`edit-resource-name-${resource.id}`}>Name</label><input id={`edit-resource-name-${resource.id}`} maxLength={100} value={editName} onChange={(event) => setEditName(event.target.value)} autoFocus required />
                      <label htmlFor={`edit-resource-quantity-${resource.id}`}>Total quantity</label><input id={`edit-resource-quantity-${resource.id}`} type="number" min="0" max="1000000" step="1" value={editQuantity} onChange={(event) => setEditQuantity(event.target.value)} required />
                      <p className="edit-context">Expected current total: {resource.totalQuantity}. Type stays {resource.type.toLowerCase()}.</p>
                      {reviewRequiredId === resource.id && <div className="review-guard" role="status"><p>Current server total: <strong>{resource.totalQuantity}</strong> · Available: <strong>{resource.availableQuantity}</strong></p>
                        <Button variant="secondary" disabled={busy} type="button" onClick={() => setReviewRequiredId(null)}>I reviewed these current values</Button></div>}
                      <div className="row-actions"><Button variant="primary" type="submit" disabled={busy || postReviewRequired || !editName.trim() || reviewRequiredId === resource.id}>Save changes</Button>
                        <Button variant="quiet" disabled={busy} type="button" onClick={() => { setEditingId(null); setReviewRequiredId(null); }}>Cancel</Button></div>
                    </form> : <button className="record-cell record-detail-button" type="button" onClick={() => setSelected(resource)}>
                      <span className="record-symbol" aria-hidden="true">{resource.type === "REUSABLE" ? "↻" : "◇"}</span><span><strong>{resource.name}</strong><small>R{String(resource.id).padStart(3, "0")}</small></span>
                    </button>}</td>
                    {editingId !== resource.id && <><td><StatusBadge tone={resource.type === "REUSABLE" ? "blue" : "gold"}>{resource.type === "REUSABLE" ? "Reusable" : "Consumable"}</StatusBadge></td>
                      <td><strong className="numeric-value">{resource.totalQuantity.toLocaleString()}</strong><small className="cell-unit">units</small></td>
                      <td><strong className="numeric-value available-value">{resource.availableQuantity.toLocaleString()}</strong><small className="cell-unit">available</small></td>
                      <td><div className="row-actions"><Button variant="quiet" disabled={busy || postReviewRequired} aria-label={`Edit ${resource.name}`} onClick={() => beginEdit(resource)}>Edit</Button>
                        <Button variant="quiet" className="button-text-danger" disabled={busy || postReviewRequired || resource.totalQuantity > 0} title={resource.totalQuantity > 0 ? "Set physical stock to zero before deletion." : undefined} aria-label={`Delete ${resource.name}`} onClick={() => setDeleting(resource)}>Delete</Button></div></td>
                    </>}
                  </tr>)}</tbody></table></div>}
          </LoadState>
          <p className="list-footnote">Each quantity describes one Resource. Different kinds of units are never added together.</p>
        </GlassPanel>
      </div>

      {selected && <DetailSheet title={selected.name} onClose={() => setSelected(null)}>
        <div className="detail-grid"><div><span>Type</span><strong>{selected.type}</strong></div><div><span>Physical total</span><strong>{selected.totalQuantity.toLocaleString()} units</strong></div><div><span>Available now</span><strong>{currentAvailability?.status === "ready" && currentAvailability.value.resourceId === selected.id ? `${currentAvailability.value.availableQuantity.toLocaleString()} units` : availabilityState === "error" ? "Unavailable" : "Loading…"}</strong></div></div>
        {availabilityState === "loading" && <p className="panel-copy" role="status">Reading current availability from the server…</p>}
        {currentAvailability?.status === "error" && <Notice tone="danger"><span>{currentAvailability.message}</span><Button variant="quiet" onClick={() => { setAvailabilityResult(null); setAvailabilityReload((value) => value + 1); }}>Retry availability</Button></Notice>}
        {availabilityState === "ready" && <p className="panel-copy">Availability is the server’s current inventory calculation, including applicable reservations and custody.</p>}
        <div className="sheet-actions"><Button onClick={() => { beginEdit(selected); setSelected(null); }}>Edit resource</Button></div>
      </DetailSheet>}
      {deleting && <Modal title="Delete resource?" onClose={() => setDeleting(null)}>
        <p className="modal-copy">Delete <strong>{deleting.name}</strong>? It must have zero physical stock and no Requirement references. The server checks both conditions.</p>
        <div className="modal-actions"><Button variant="quiet" disabled={busy} onClick={() => setDeleting(null)}>Keep resource</Button><Button variant="danger" disabled={busy || postReviewRequired || deleting.totalQuantity > 0} onClick={() => void confirmDelete()}>{busy ? "Checking…" : "Confirm delete"}</Button></div>
      </Modal>}
    </section>
  );
}
