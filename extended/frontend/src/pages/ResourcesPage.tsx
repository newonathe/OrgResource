import { useEffect, useState, type FormEvent } from "react";
import { api, ApiError, workspaceApi } from "../api/client";
import { inventoryApi, newOperationIdentity, type InventoryResource, type Movement, type OperationIdentity } from "../api/extended";
import { useAuth } from "../auth/AuthContext";
import type { Resource, ResourceAvailability, ResourceType } from "../api/types";
import { explainError, isUncertain } from "../domain";
import { Button, DetailSheet, GlassPanel, LoadState, Modal, MutationReviewNotice, Notice, PageHeader, PageNavigation, PanelHeading, StatusBadge, usePageCursors } from "../ui";

type StockAttempt = { resourceId: string; mode: "receive" | "adjust"; value: number; etag: string; identity: OperationIdentity };

type AvailabilityResult =
  | { resourceId: number; status: "ready"; value: ResourceAvailability }
  | { resourceId: number; status: "error"; message: string };

export function ResourcesPage() {
  const { session, organizations } = useAuth();
  const activeOrgId = session?.activeOrgId ?? "";
  const activeRole = organizations.find(org => org.id === activeOrgId)?.role;
  const canOperateStock = activeRole === "OWNER" || activeRole === "CUSTODIAN";
  const canManageLibrary = canOperateStock;
  const [resources, setResources] = useState<Resource[]>([]);
  const [loadState, setLoadState] = useState<"loading" | "error" | "ready">("loading");
  const [loadError, setLoadError] = useState("");
  const [filter, setFilter] = useState<"ALL" | ResourceType>("ALL");
  const [search, setSearch] = useState("");
  const [cursors, setCursors] = useState<(string | null)[]>([null]);
  const [nextCursor, setNextCursor] = useState<string | null>(null);
  const cursor = cursors.at(-1) ?? null;
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
  const [inventorySnapshot, setInventorySnapshot] = useState<{ resourceId: string; value: InventoryResource; etag: string } | null>(null);
  const [movementSnapshot, setMovementSnapshot] = useState<{ resourceId: string; cursor: string | null; items: Movement[]; nextCursor: string | null } | null>(null);
  const [inventoryErrorState, setInventoryErrorState] = useState<{ resourceId: string | null; message: string } | null>(null);
  const [stockBusy, setStockBusy] = useState(false);
  const [receiveQuantity, setReceiveQuantity] = useState("1");
  const [adjustQuantity, setAdjustQuantity] = useState("");
  const [operationAttempt, setOperationAttempt] = useState<StockAttempt | null>(null);
  const [operationRecoverable, setOperationRecoverable] = useState(false);
  const [receiptId, setReceiptId] = useState("");
  const [receiptMessage, setReceiptMessage] = useState("");
  const [imagePreview, setImagePreview] = useState<{ resourceId: number; url: string } | null>(null);
  const [imageRefresh, setImageRefresh] = useState(0);
  const [imageFile, setImageFile] = useState<File | null>(null);
  const [confirmRemoveImage, setConfirmRemoveImage] = useState(false);

  useEffect(() => {
    const controller = new AbortController();
    workspaceApi.resources(cursor, { type: filter === "ALL" ? undefined : filter, search }, controller.signal).then((page) => {
      setResources(page.items); setNextCursor(page.nextCursor); setLoadState("ready"); setLoadError("");
    }).catch((reason: unknown) => {
      if (reason instanceof DOMException && reason.name === "AbortError") return;
      setLoadError(explainError(reason)); setLoadState("error");
    });
    return () => controller.abort();
  }, [cursor, filter, reload, search]);

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

  const selectedPublicId = selected?.publicId;
  const movementPage = usePageCursors(selectedPublicId ?? "");
  const movementCursor = movementPage.cursor;
  const [movementError, setMovementError] = useState("");
  const [movementReload, setMovementReload] = useState(0);
  const inventoryState = inventorySnapshot?.resourceId === selectedPublicId ? inventorySnapshot : null;
  const movementState = movementSnapshot && movementSnapshot.resourceId === selectedPublicId && movementSnapshot.cursor === movementCursor ? movementSnapshot : null;
  const movements = movementState?.items ?? [];
  const inventoryErrorForSelection = inventoryErrorState && inventoryErrorState.resourceId === selectedPublicId ? inventoryErrorState : null;
  const inventoryError = inventoryErrorForSelection?.message ?? "";
  const imagePreviewForSelection = imagePreview && imagePreview.resourceId === selected?.id ? imagePreview : null;
  const imageUrl = imagePreviewForSelection?.url ?? null;
  const reportInventoryError = (message: string) => setInventoryErrorState({ resourceId: selectedPublicId ?? null, message });
  useEffect(() => {
    if (selectedId === undefined || !selectedPublicId || !activeOrgId || !canOperateStock) {
      return;
    }
    let active = true;
    inventoryApi.resource(activeOrgId, selectedPublicId)
      .then(state => {
        if (!active) return;
        if (!state.etag) throw new Error("The inventory API did not return its current ETag.");
        setInventorySnapshot({ resourceId: selectedPublicId, value: state.data, etag: state.etag });
        setInventoryErrorState(null);
      })
      .catch((reason: unknown) => {
        if (reason instanceof DOMException && reason.name === "AbortError") return;
        if (active) setInventoryErrorState({ resourceId: selectedPublicId, message: explainError(reason) });
      });
    return () => { active = false; };
  }, [selectedId, selectedPublicId, activeOrgId, canOperateStock, availabilityReload]);

  useEffect(() => {
    if (!selectedPublicId || !activeOrgId || !canOperateStock) return;
    const controller = new AbortController();
    inventoryApi.movements(activeOrgId, selectedPublicId, 30, movementCursor, controller.signal)
      .then(page => {
        setMovementSnapshot({ resourceId: selectedPublicId, cursor: movementCursor, ...page });
        setMovementError("");
      })
      .catch((reason: unknown) => {
        if (!controller.signal.aborted) setMovementError(explainError(reason));
      });
    return () => controller.abort();
  }, [selectedPublicId, activeOrgId, canOperateStock, movementCursor, availabilityReload, movementReload]);

  useEffect(() => {
    if (selectedId === undefined) return;
    const controller = new AbortController();
    let objectUrl: string | null = null;
    void fetch(`/api/resources/${selectedId}/image`, { credentials: "same-origin", cache: "no-store", signal: controller.signal })
      .then(async response => {
        if (response.status === 404 && !controller.signal.aborted) setImagePreview(null);
        if (!response.ok) return null;
        const blob = await response.blob();
        if (controller.signal.aborted || !blob.type.startsWith("image/")) return null;
        objectUrl = URL.createObjectURL(blob); setImagePreview({ resourceId: selectedId, url: objectUrl }); return null;
      })
      .catch(() => undefined);
    return () => { controller.abort(); if (objectUrl) URL.revokeObjectURL(objectUrl); };
  }, [selectedId, imageRefresh]);

  function changePage(next: string | null) {
    setLoadState("loading");
    setCursors(previous => next ? [...previous, next] : previous.slice(0, -1));
  }
  const currentAvailability = selected && availabilityResult?.resourceId === selected.id ? availabilityResult : null;
  const availabilityState = currentAvailability?.status ?? "loading";
  const refresh = () => { setLoadState("loading"); setReload((value) => value + 1); };

  async function executeStock(attempt: StockAttempt) {
    if (!selected || selected.publicId !== attempt.resourceId || !activeOrgId || !inventoryState || stockBusy) return;
    setOperationAttempt(attempt); setOperationRecoverable(false); setStockBusy(true); reportInventoryError(""); setReceiptMessage("");
    try {
      const result = attempt.mode === "receive"
        ? await inventoryApi.receive(activeOrgId, attempt.resourceId, attempt.value, attempt.etag, attempt.identity)
        : await inventoryApi.adjust(activeOrgId, attempt.resourceId, attempt.value, attempt.etag, attempt.identity);
      setOperationAttempt(null); setReceiveQuantity("1"); setAdjustQuantity("");
      setNotice(`${attempt.mode === "receive" ? "Stock receipt" : "Stock adjustment"} confirmed by the server (operation ${result.data.operationId}).`);
      refresh(); setAvailabilityReload(current => current + 1);
    } catch (reason) {
      reportInventoryError(explainError(reason));
      if (isUncertain(reason)) {
        setOperationAttempt(attempt);
        setReceiptMessage("The result is uncertain. Recover its receipt before repeating this physical operation.");
      } else {
        setOperationAttempt(null);
        if (reason instanceof ApiError && (reason.status === 412 || reason.status === 409)) {
          setReceiptMessage("The saved state changed. Current values are being refreshed; review them before starting a new operation.");
          setAvailabilityReload(current => current + 1);
        }
      }
    } finally { setStockBusy(false); }
  }

  async function submitStock(mode: "receive" | "adjust", event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selected || !activeOrgId || !inventoryState || stockBusy) return;
    const value = Number(mode === "receive" ? receiveQuantity : adjustQuantity);
    if (!Number.isInteger(value) || value < (mode === "receive" ? 1 : 0) || value > 1_000_000) return;
    if (operationAttempt) return;
    await executeStock({ resourceId: selected.publicId, mode, value, etag: inventoryState.etag, identity: newOperationIdentity() });
  }

  async function recoverOperationReceipt() {
    const id = operationAttempt?.identity.operationId || receiptId.trim();
    if (!activeOrgId || !id) return;
    setStockBusy(true); setOperationRecoverable(false); reportInventoryError(""); setReceiptMessage("");
    try {
      const result = await inventoryApi.receipt(activeOrgId, id);
      const receipt = result.data;
      setReceiptMessage("kind" in receipt
        ? `Saved ${receipt.kind} receipt · permanent movement · quantity ${receipt.quantity} · total change ${receipt.deltaTotal} · operation ${receipt.operationId}.`
        : `Temporary ${receipt.action} receipt · ${receipt.allocationStatus ?? "confirmed"} · operation ${receipt.operationId}.`);
      if (operationAttempt?.identity.operationId === id) { setOperationAttempt(null); setOperationRecoverable(false); }
      setAvailabilityReload(current => current + 1); refresh();
    } catch (reason) {
      if (reason instanceof ApiError && reason.status === 404) {
        setReceiptMessage("No receipt is visible yet. The original request may still commit. Review the refreshed stock. A retry can only reuse the original request.");
        if (operationAttempt?.identity.operationId === id) setOperationRecoverable(true);
        setAvailabilityReload(current => current + 1);
      } else if (reason instanceof ApiError && reason.status === 409 && reason.code === "IDEMPOTENCY_KEY_EXPIRED") {
        setReceiptMessage("The short-lived receipt expired and no permanent movement was found. Do not repeat this operation; reconcile the current stock with an authorized operator.");
      } else reportInventoryError(explainError(reason));
    } finally { setStockBusy(false); }
  }

  async function uploadImage(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selected || !imageFile || busy) return;
    if (imageFile.size < 1 || imageFile.size > 2 * 1024 * 1024 || !["image/png", "image/jpeg"].includes(imageFile.type)) {
      setError("Choose a nonempty PNG or JPEG no larger than 2 MiB. The server performs the final format and dimension checks."); return;
    }
    setBusy(true); setError(""); setNotice("");
    try { const result = await inventoryApi.uploadImage(selected.id, imageFile); setImageFile(null); setImageRefresh(value => value + 1); setAvailabilityReload(value => value + 1); setNotice(`Image saved (${result.data.sizeBytes.toLocaleString()} bytes, ${result.data.contentType}).`); }
    catch (reason) { setError(explainError(reason)); }
    finally { setBusy(false); }
  }

  async function removeImage() {
    if (!inventoryState || !activeOrgId || !canManageLibrary || busy || stockBusy) return;
    setBusy(true); setError(""); setNotice("");
    try {
      await inventoryApi.removeImage(activeOrgId, inventoryState.resourceId, inventoryState.etag);
      setImagePreview(null); setNotice("Resource image removed.");
    } catch (reason) {
      setError(explainError(reason));
    } finally {
      setConfirmRemoveImage(false); setBusy(false);
      setImageRefresh(value => value + 1); setAvailabilityReload(value => value + 1);
    }
  }

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
        {canManageLibrary ? <GlassPanel className="form-panel" tilt={false}>
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
        </GlassPanel> : <GlassPanel className="form-panel" tilt={false}><PanelHeading kicker="Read only" title="Inventory access" /><p>Your current role can read these server records but cannot create or change stock. Ask an organization owner or custodian for an authorized correction.</p></GlassPanel>}

        <GlassPanel className="list-panel" tilt={false}>
          <PanelHeading kicker="Server inventory" title="All resources">
            <Button variant="quiet" onClick={refresh} disabled={loadState === "loading"}>Refresh</Button>
          </PanelHeading>
          <div className="search-filter-bar resource-filters">
            <label htmlFor="resource-search">Search resources</label><input id="resource-search" type="search" value={search} maxLength={100} onChange={(event) => { setCursors([null]); setLoadState("loading"); setSearch(event.target.value); }} placeholder="Search by name" />
            <label htmlFor="resource-filter">Type</label><select id="resource-filter" value={filter} onChange={(event) => { setLoadState("loading"); setCursors([null]); setFilter(event.target.value as "ALL" | ResourceType); }}>
              <option value="ALL">All types</option><option value="REUSABLE">Reusable</option><option value="CONSUMABLE">Consumable</option>
            </select>
          </div>
          <LoadState state={loadState} error={loadError} onRetry={refresh}>
            {resources.length === 0 ? <div className="state-block state-empty"><span className="empty-symbol" aria-hidden="true">◈</span><h3>No resources in this view</h3><p>Try a different search or type filter.</p></div>
                : <div className="table-scroll"><table className="data-table resource-table"><thead><tr><th scope="col">Resource</th><th scope="col">Type</th><th scope="col">Physical total</th><th scope="col">Available</th><th scope="col"><span className="sr-only">Actions</span></th></tr></thead>
                  <tbody>{resources.map((resource) => <tr key={resource.id}>
                    <td>{editingId === resource.id ? <form className="inline-edit resource-inline-edit" onSubmit={(event) => void saveEdit(event, resource)}>
                      <label htmlFor={`edit-resource-name-${resource.id}`}>Name</label><input id={`edit-resource-name-${resource.id}`} maxLength={100} value={editName} onChange={(event) => setEditName(event.target.value)} autoFocus required />
                      <label htmlFor={`edit-resource-quantity-${resource.id}`}>Total quantity</label><input id={`edit-resource-quantity-${resource.id}`} type="number" min="0" max="1000000" step="1" value={editQuantity} onChange={(event) => setEditQuantity(event.target.value)} required />
                      <p className="edit-context">Expected current total: {resource.totalQuantity}. Type stays {resource.type.toLowerCase()}.</p>
                      {reviewRequiredId === resource.id && <div className="review-guard" role="status"><p>Current server total: <strong>{resource.totalQuantity}</strong> · Available: <strong>{resource.availableQuantity}</strong></p>
                        <Button variant="secondary" disabled={busy} type="button" onClick={() => setReviewRequiredId(null)}>I reviewed these current values</Button></div>}
                      <div className="row-actions"><Button variant="primary" type="submit" disabled={busy || postReviewRequired || !editName.trim() || reviewRequiredId === resource.id}>Save changes</Button>
                        <Button variant="quiet" disabled={busy} type="button" onClick={() => { setEditingId(null); setReviewRequiredId(null); }}>Cancel</Button></div>
                    </form> : <button className="record-cell record-detail-button" type="button" onClick={() => { setMovementSnapshot(null); setMovementError(""); setSelected(resource); }}>
                      <span className="record-symbol" aria-hidden="true">{resource.type === "REUSABLE" ? "↻" : "◇"}</span><span><strong>{resource.name}</strong><small>R{String(resource.id).padStart(3, "0")}</small></span>
                    </button>}</td>
                    {editingId !== resource.id && <><td><StatusBadge tone={resource.type === "REUSABLE" ? "blue" : "gold"}>{resource.type === "REUSABLE" ? "Reusable" : "Consumable"}</StatusBadge></td>
                      <td><strong className="numeric-value">{resource.totalQuantity.toLocaleString()}</strong><small className="cell-unit">units</small></td>
                      <td><strong className="numeric-value available-value">{resource.availableQuantity.toLocaleString()}</strong><small className="cell-unit">available</small></td>
                      {canManageLibrary && <td><div className="row-actions"><Button variant="quiet" disabled={busy || postReviewRequired} aria-label={`Edit ${resource.name}`} onClick={() => beginEdit(resource)}>Edit</Button>
                        <Button variant="quiet" className="button-text-danger" disabled={busy || postReviewRequired || resource.totalQuantity > 0} title={resource.totalQuantity > 0 ? "Set physical stock to zero before deletion." : undefined} aria-label={`Delete ${resource.name}`} onClick={() => setDeleting(resource)}>Delete</Button></div></td>
                      }
                    </>}
                  </tr>)}</tbody></table></div>}
          </LoadState>
          {loadState !== "error" && <PageNavigation label="Resource pages" page={cursors.length}
            count={resources.length} hasNext={nextCursor !== null} loading={loadState === "loading"}
            onPrevious={() => changePage(null)} onNext={() => { if (nextCursor) changePage(nextCursor); }} />}
          {loadState === "error" && cursors.length > 1 && <Button onClick={() => changePage(null)}>Previous page</Button>}
          <p className="list-footnote">Each quantity describes one Resource. Different kinds of units are never added together.</p>
        </GlassPanel>
      </div>

      {selected && <DetailSheet title={selected.name} onClose={() => { setSelected(null); setImageFile(null); setConfirmRemoveImage(false); }}>
        <div className="detail-grid"><div><span>Type</span><strong>{selected.type}</strong></div><div><span>Physical total</span><strong>{selected.totalQuantity.toLocaleString()} units</strong></div><div><span>Available now</span><strong>{currentAvailability?.status === "ready" && currentAvailability.value.resourceId === selected.id ? `${currentAvailability.value.availableQuantity.toLocaleString()} units` : availabilityState === "error" ? "Unavailable" : "Loading…"}</strong></div></div>
        {availabilityState === "loading" && <p className="panel-copy" role="status">Reading current availability from the server…</p>}
        {currentAvailability?.status === "error" && <Notice tone="danger"><span>{currentAvailability.message}</span><Button variant="quiet" onClick={() => { setAvailabilityResult(null); setAvailabilityReload((value) => value + 1); }}>Retry availability</Button></Notice>}
        {availabilityState === "ready" && <p className="panel-copy">Availability is the server’s current inventory calculation, including applicable reservations and custody.</p>}
        <section className="sheet-section resource-image-section"><h3>Resource image</h3>
          {imageUrl ? <img className="resource-image-preview" src={imageUrl} alt={"Stored image for " + selected.name} /> : <p className="quiet-note">No image is currently available for this Resource.</p>}
          {canManageLibrary && imageUrl && <Button variant="secondary" disabled={busy || stockBusy || !inventoryState} onClick={() => setConfirmRemoveImage(true)}>Remove image</Button>}
          {confirmRemoveImage && <div className="auth-form">
            <p>Remove the stored image for this Resource?</p>
            <Button disabled={busy} onClick={() => void removeImage()}>Confirm image removal</Button>
            <Button variant="secondary" disabled={busy} onClick={() => setConfirmRemoveImage(false)}>Keep image</Button>
          </div>}
          {canManageLibrary && <form className="auth-form image-upload-form" onSubmit={event => void uploadImage(event)}>
            <label htmlFor="resource-image-file">Upload PNG or JPEG (maximum 2 MiB)</label>
            <input id="resource-image-file" type="file" accept="image/png,image/jpeg" onChange={event => setImageFile(event.target.files?.[0] ?? null)} />
            <Button variant="secondary" type="submit" disabled={busy || !imageFile}>{busy ? "Uploading…" : "Upload image"}</Button>
          </form>}
        </section>
        {canOperateStock && <section className="sheet-section stock-management">
          <h3>Stock controls and permanent history</h3>
          {inventoryError && <Notice tone="danger">{inventoryError}</Notice>}
          {inventoryState ? <>
            <div className="detail-grid"><div><span>Current physical total</span><strong>{inventoryState.value.totalQuantity.toLocaleString()}</strong></div><div><span>Current available quantity</span><strong>{inventoryState.value.availableQuantity.toLocaleString()}</strong></div></div>
            {receiptMessage && <Notice tone={operationAttempt ? "warning" : "info"}>{receiptMessage}</Notice>}
            {operationAttempt && <div className="operation-recovery">
              <p>Operation ID <code>{operationAttempt.identity.operationId}</code></p>
              <Button disabled={stockBusy} onClick={() => void recoverOperationReceipt()}>{stockBusy ? "Checking…" : "Recover saved receipt"}</Button>
              {operationRecoverable && operationAttempt.resourceId === selectedPublicId && <Button variant="secondary" disabled={stockBusy} onClick={() => void executeStock(operationAttempt)}>Retry same operation ID safely</Button>}
            </div>}
            <div className="stock-operation-grid">
              <form className="auth-form" onSubmit={event => void submitStock("receive", event)}>
                <label htmlFor="stock-receive-quantity">Receive additional units</label>
                <input id="stock-receive-quantity" type="number" min="1" max="1000000" step="1" value={receiveQuantity} onChange={event => setReceiveQuantity(event.target.value)} required disabled={stockBusy || !!operationAttempt} />
                <Button variant="primary" type="submit" disabled={stockBusy || !inventoryState.etag || !!operationAttempt}>{stockBusy ? "Saving…" : "Record receipt"}</Button>
              </form>
              <form className="auth-form" onSubmit={event => void submitStock("adjust", event)}>
                <label htmlFor="stock-adjust-total">Correct physical total</label>
                <input id="stock-adjust-total" type="number" min="0" max="1000000" step="1" value={adjustQuantity} onChange={event => setAdjustQuantity(event.target.value)} required disabled={stockBusy || !!operationAttempt} />
                <Button type="submit" disabled={stockBusy || adjustQuantity === "" || !inventoryState.etag || !!operationAttempt}>{stockBusy ? "Saving…" : "Record adjustment"}</Button>
              </form>
            </div>
            <form className="receipt-lookup" onSubmit={event => { event.preventDefault(); void recoverOperationReceipt(); }}>
              <label htmlFor="operation-receipt-id">Recover an operation by ID</label>
              <input id="operation-receipt-id" value={receiptId} onChange={event => setReceiptId(event.target.value)} placeholder="Operation UUID" />
              <Button disabled={stockBusy || (!receiptId.trim() && !operationAttempt)}>{stockBusy ? "Checking…" : "Look up receipt"}</Button>
            </form>
            <h4>Permanent movement timeline</h4>
            {movementError && <Notice tone="danger">{movementError} <Button onClick={() => {
              setMovementError(""); setMovementReload(value => value + 1);
            }}>Retry</Button></Notice>}
            {!movementState && !movementError && <p role="status">Loading movements…</p>}
            {movementState && !movementError && (movements.length === 0 ? <p className="quiet-note">No stock movements have been recorded.</p> : <div className="table-scroll"><table className="data-table"><thead><tr><th>When (UTC from server)</th><th>Movement</th><th>Quantity</th><th>Total change</th><th>Issued change</th><th>Operation</th></tr></thead><tbody>{movements.map(movement => <tr key={movement.id}><td>{movement.occurredAt}</td><td>{movement.kind}</td><td>{movement.quantity}</td><td>{movement.deltaTotal > 0 ? "+" : ""}{movement.deltaTotal}</td><td>{movement.deltaIssued > 0 ? "+" : ""}{movement.deltaIssued}</td><td><code>{movement.operationId}</code></td></tr>)}</tbody></table></div>)}
            <PageNavigation label="Stock movement pages" page={movementPage.page} count={movementError ? 0 : movements.length}
              hasNext={!movementError && !!movementState?.nextCursor} loading={!movementState && !movementError} unavailable={!!movementError}
              onPrevious={() => { setMovementError(""); movementPage.move(null); }}
              onNext={() => {
                const next = movementState?.nextCursor;
                if (next) { setMovementError(""); movementPage.move(next); }
              }} />
          </> : <p className="quiet-note">{inventoryError || "Loading tenant-scoped inventory state and ETag…"}</p>}
        </section>}
        {canManageLibrary && <div className="sheet-actions"><Button onClick={() => { beginEdit(selected); setSelected(null); }}>Edit resource</Button></div>}
      </DetailSheet>}
      {deleting && <Modal title="Delete resource?" onClose={() => setDeleting(null)}>
        <p className="modal-copy">Delete <strong>{deleting.name}</strong>? It must have zero physical stock and no Requirement references. The server checks both conditions.</p>
        <div className="modal-actions"><Button variant="quiet" disabled={busy} onClick={() => setDeleting(null)}>Keep resource</Button><Button variant="danger" disabled={busy || postReviewRequired || deleting.totalQuantity > 0} onClick={() => void confirmDelete()}>{busy ? "Checking…" : "Confirm delete"}</Button></div>
      </Modal>}
    </section>
  );
}
