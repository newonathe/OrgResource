import { useEffect, useRef, useState, type FormEvent } from "react";
import { reportingApi, type ReportingDataset, type ReportingEvent, type ReportingFormat } from "../api/extended";
import { explainError } from "../domain";
import { Button, GlassPanel, Notice, PageHeader, PageNavigation, PanelHeading, usePageCursors } from "../ui";

export function ActivityPage({ orgId }: { orgId: string }) {
  const [feed, setFeed] = useState<"activity" | "audit">("activity");
  const [rows, setRows] = useState<ReportingEvent[]>([]);
  const [nextCursor, setNextCursor] = useState<string | null>(null);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(true);
  const [reload, setReload] = useState(0);
  const { cursor, page, move, reset } = usePageCursors(orgId + ":" + feed);

  useEffect(() => {
    const controller = new AbortController();
    void reportingApi.events(orgId, feed, cursor, controller.signal).then(result => {
      if (controller.signal.aborted) return;
      setRows(result.items);
      setNextCursor(result.nextCursor);
    }).catch((reason: unknown) => {
      if (!controller.signal.aborted) setError(explainError(reason));
    }).finally(() => {
      if (!controller.signal.aborted) setBusy(false);
    });
    return () => controller.abort();
  }, [orgId, feed, cursor, reload]);

  function prepareRead() {
    setRows([]); setNextCursor(null); setError(""); setBusy(true);
  }

  function goToPage(next: string | null) {
    prepareRead();
    move(next);
  }

  function refresh() {
    prepareRead();
    setReload(value => value + 1);
  }

  return <section className="page-shell">
    <PageHeader eyebrow="Organization" title="Activity" description="Recorded governance events and permanent inventory movements for this workspace. The audit feed covers recorded actions and can omit unrecorded requests." />
    <GlassPanel className="list-panel" tilt={false}>
      <PanelHeading title="Recorded events">
        <Button variant="secondary" onClick={() => { reset(); refresh(); }} disabled={busy}>Newest events</Button>
      </PanelHeading>
      <label>Show <select value={feed} onChange={event => {
        reset();
        setFeed(event.target.value as "activity" | "audit");
        prepareRead();
      }}>
        <option value="activity">Audit and stock movements</option>
        <option value="audit">Audit only</option>
      </select></label>
      {busy && <p role="status">Loading events…</p>}
      {error && <><Notice tone="danger">{error}</Notice><Button onClick={refresh}>Retry this page</Button></>}
      {!busy && !error && rows.length === 0 && <p>No events were recorded for this page.</p>}
      {rows.length > 0 && <ul className="member-list">{rows.map(row => <li className="member-row" key={row.id}>
        <div><strong>{row.action.replaceAll("_", " ")}</strong><p>{row.source === "AUDIT" ? "Recorded audit action" : "Stock movement"} · {row.targetType}
          {row.outcome && ` · ${row.outcome}`}{row.quantity !== null && ` · ${row.quantity} units`}</p></div>
        <time dateTime={row.occurredAt}>{row.occurredAt.replace("T", " ").replace("Z", " UTC")}</time>
      </li>)}</ul>}
      <PageNavigation label="Activity pages" page={page} count={rows.length} hasNext={nextCursor !== null}
        loading={busy} unavailable={!!error} onPrevious={() => goToPage(null)}
        onNext={() => { if (nextCursor) goToPage(nextCursor); }} />
    </GlassPanel>
  </section>;
}

export function ExportsPage({ orgId }: { orgId: string }) {
  const [dataset, setDataset] = useState<ReportingDataset>("RESOURCES");
  const [format, setFormat] = useState<ReportingFormat>("CSV");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [downloadName, setDownloadName] = useState("");
  const inFlight = useRef<AbortController | null>(null);
  useEffect(() => () => inFlight.current?.abort(), []);

  async function download(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (inFlight.current) return;
    const controller = new AbortController();
    inFlight.current = controller;
    setBusy(true); setError(""); setDownloadName("");
    try {
      const result = await reportingApi.download(orgId, dataset, format, controller.signal);
      if (controller.signal.aborted) return;
      const url = URL.createObjectURL(result.file);
      const link = document.createElement("a");
      link.href = url; link.download = result.filename;
      document.body.appendChild(link); link.click(); link.remove();
      window.setTimeout(() => URL.revokeObjectURL(url), 1000);
      setDownloadName(result.filename);
    } catch (reason) {
      if (!controller.signal.aborted) setError(explainError(reason));
    } finally {
      inFlight.current = null;
      if (!controller.signal.aborted) setBusy(false);
    }
  }

  return <section className="page-shell">
    <PageHeader eyebrow="Organization" title="Exports" description="Download redacted records for this workspace. Each export is limited to 1,000 rows and 4 MiB." />
    <GlassPanel className="form-panel" tilt={false}>
      <PanelHeading title="Download workspace records" />
      <form className="auth-form" onSubmit={event => void download(event)}>
        <label>Dataset<select value={dataset} disabled={busy} onChange={event => setDataset(event.target.value as ReportingDataset)}>
          <option value="RESOURCES">Resources</option><option value="MOVEMENTS">Stock movements</option><option value="AUDIT">Audit records</option>
        </select></label>
        <label>File format<select value={format} disabled={busy} onChange={event => setFormat(event.target.value as ReportingFormat)}>
          <option value="CSV">CSV spreadsheet</option><option value="TENANT_JSON">JSON</option>
        </select></label>
        <Button variant="primary" disabled={busy}>{busy ? "Preparing file…" : "Download export"}</Button>
      </form>
      {error && <Notice tone="danger">{error}</Notice>}
      {downloadName && <Notice tone="success">Download requested for {downloadName}.</Notice>}
    </GlassPanel>
  </section>;
}
