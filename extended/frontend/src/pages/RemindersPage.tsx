import { useEffect, useState } from "react";
import { workspaceApi } from "../api/client";
import type { WorkspaceAllocation } from "../api/types";
import { explainError, formatManila, manilaWallTime, reminderLabel, reminderWindow } from "../domain";
import { Button, GlassPanel, LoadState, PageHeader, PageNavigation, PanelHeading, StatusBadge, usePageCursors } from "../ui";

export function RemindersPage() {
  const [allocations, setAllocations] = useState<WorkspaceAllocation[]>([]);
  const [state, setState] = useState<"loading" | "error" | "ready">("loading");
  const [error, setError] = useState("");
  const [reload, setReload] = useState(0);
  const { cursor, page, move } = usePageCursors("issued-reminders");
  const [nextCursor, setNextCursor] = useState<string | null>(null);
  const openPage = (next: string | null) => { setState("loading"); move(next); };
  const [now, setNow] = useState(manilaWallTime());
  const [updatedAt, setUpdatedAt] = useState("");

  useEffect(() => {
    const interval = window.setInterval(() => setNow(manilaWallTime()), 60_000);
    return () => window.clearInterval(interval);
  }, []);
  useEffect(() => {
    const controller = new AbortController();
    workspaceApi.allocations(cursor, { status: "ISSUED" }, controller.signal)
      .then(result => {
        setAllocations(result.items); setNextCursor(result.nextCursor);
        setUpdatedAt(manilaWallTime()); setError(""); setState("ready");
      }).catch((reason: unknown) => {
        if (reason instanceof DOMException && reason.name === "AbortError") return;
        setError(explainError(reason)); setState("error");
      });
    return () => controller.abort();
  }, [cursor, reload]);
  const refresh = () => { setState("loading"); setReload((value) => value + 1); };

  const plus24h = manilaWallTime(new Date(Date.parse(now + "+08:00") + 24 * 60 * 60 * 1000));
  const issued = allocations.filter(allocation => allocation.requirement.resourceType === "REUSABLE");
  const dueSoon = issued.filter(allocation => reminderWindow(allocation.requirement.projectEndAt, now, plus24h) === "DUE_SOON");
  const overdue = issued.filter(allocation => reminderWindow(allocation.requirement.projectEndAt, now, plus24h) === "OVERDUE");

  return (
    <section className="page-shell" aria-labelledby="reminder-activity-title">
      <PageHeader eyebrow="System · scheduled activity" title="Reminder Activity" description="See upcoming return windows and outcomes recorded by the backend reminder scheduler."
        action={<div className="header-stat"><span>{state === "ready" ? issued.length : "—"}</span><small>issued batches in view</small></div>} />
      <div className="overview-toolbar"><span>Updated: <strong>{updatedAt ? formatManila(updatedAt) : "Loading"}</strong></span><Button variant="quiet" disabled={state === "loading"} onClick={refresh}>Refresh</Button></div>
      <LoadState state={state} error={error} onRetry={refresh}>
        {issued.length === 0 ? <GlassPanel className="state-block state-empty"><span className="empty-symbol" aria-hidden="true">◷</span><h3>No issued reusable batches on this page</h3><p>When an Allocation is issued, the backend determines whether a scheduled reminder is eligible.</p></GlassPanel>
          : <div className="reminder-groups">
            <GlassPanel className="reminder-group" tilt={false}><PanelHeading kicker="Next 24 hours" title="Return window approaching"><span className="group-count">{dueSoon.length}</span></PanelHeading>
              {dueSoon.length === 0 ? <p className="panel-copy">No batch on this page is due within 24 hours.</p> : <ReminderList rows={dueSoon} />}</GlassPanel>
            <GlassPanel className="reminder-group" tilt={false}><PanelHeading kicker="Project end passed" title="Overdue issued batches"><span className="group-count overdue-count">{overdue.length}</span></PanelHeading>
              {overdue.length === 0 ? <p className="panel-copy">No batch on this page has passed its Project end.</p> : <ReminderList rows={overdue} overdue />}</GlassPanel>
            <GlassPanel className="reminder-group reminder-all" tilt={false}><PanelHeading kicker="Backend records" title="Issued reusable batches on this page" />
              <ReminderList rows={issued} /></GlassPanel>
          </div>}
      </LoadState>
      <PageNavigation label="Reminder pages" page={page} count={state === "ready" ? issued.length : 0}
        hasNext={state === "ready" && nextCursor !== null} loading={state === "loading"} unavailable={state === "error"}
        onPrevious={() => openPage(null)} onNext={() => { if (nextCursor) openPage(nextCursor); }} />
      <p className="list-footnote">Reminder outcomes describe request processing. “Accepted by local simulator” and “Provider accepted request” do not prove handset delivery. This screen has no manual send action.</p>
    </section>
  );
}

function ReminderList({ rows, overdue = false }: {
  rows: WorkspaceAllocation[];
  overdue?: boolean;
}) {
  return <div className="reminder-list">{rows.map(allocation => <article className="reminder-row" key={allocation.id}>
    <div className="reminder-date"><span aria-hidden="true">◷</span><strong>{formatManila(allocation.requirement.projectEndAt)}</strong></div>
    <div className="reminder-main"><strong>{allocation.requirement.resourceName} · {allocation.quantity} units</strong><small>Allocation A{allocation.id} · {allocation.borrowerName} · {allocation.requirement.projectName}</small></div>
    <div className="reminder-status"><StatusBadge tone={overdue ? "danger" : allocation.reminderOutcome === "SIMULATED" || allocation.reminderOutcome === "PROVIDER_ACCEPTED" ? "success" : allocation.reminderOutcome === "UNKNOWN" ? "warning" : "neutral"}>{reminderLabel(allocation.reminderOutcome)}</StatusBadge>
      <small>{allocation.reminderAttempted ? "Attempt recorded" : "No attempt recorded"}</small></div>
    <div className="reminder-consent"><span>Consent</span><strong>{allocation.smsConsent ? "Granted" : "Not granted"}</strong><span>Contact</span><strong>{allocation.contactPresent ? "Present · redacted" : "None"}</strong></div>
  </article>)}</div>;
}
