import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { workspaceApi } from "../api/client";
import type { WorkspaceOverview } from "../api/types";
import { useAuth } from "../auth/AuthContext";
import { explainError, formatManila } from "../domain";
import { Button, GlassPanel, LoadState, MetricCard, Notice, PageHeader, PanelHeading, StatusBadge } from "../ui";

export function OverviewPage() {
  const { session, organizations } = useAuth();
  const role = organizations.find(org => org.id === session?.activeOrgId)?.role;
  const canViewActivity = role === "OWNER" || role === "ADMIN" || role === "CUSTODIAN";
  const [snapshot, setSnapshot] = useState<WorkspaceOverview | null>(null);
  const [state, setState] = useState<"loading" | "ready" | "error">("loading");
  const [error, setError] = useState("");
  const [reload, setReload] = useState(0);

  useEffect(() => {
    const controller = new AbortController();
    workspaceApi.overview(controller.signal).then(result => {
      setSnapshot(result); setState("ready"); setError("");
    }).catch((reason: unknown) => {
      if (controller.signal.aborted) return;
      setSnapshot(null); setState("error"); setError(explainError(reason));
    });
    return () => controller.abort();
  }, [reload]);

  function refresh() { setState("loading"); setReload(value => value + 1); }
  const summary = snapshot?.allocations;
  const timeLabel = state === "loading" ? "Loading" : state === "error" ? "Unavailable" : formatManila(snapshot?.sampledAt);

  return (
    <section className="page-shell overview-page" aria-labelledby="overview-title">
      <PageHeader eyebrow="Workspace · current snapshot" title="Overview" description="A live view of project activity and shared resources, refreshed from the Java service."
        action={canViewActivity ? <Link className="button button-primary button-magnetic" to="/allocations?status=PENDING">Review requests <span aria-hidden="true">↗</span></Link> : undefined} />
      <GlassPanel as="div" className="overview-hero" tilt={false}>
        <div><p className="hero-kicker">Planning with clarity</p><h2>Shared resources, thoughtfully coordinated.</h2><p>Project needs, inventory availability, and whole-batch Allocation history from one academic workspace.</p></div>
        <div className="hero-illustration" aria-hidden="true"><span className="hero-arc" /><span className="hero-pillar pillar-one" /><span className="hero-pillar pillar-two" /><span className="hero-book">⌑</span></div>
      </GlassPanel>
      <div className="overview-toolbar"><span>Snapshot time: <strong>{timeLabel}</strong> · Philippine time</span>
        <Button variant="quiet" disabled={state === "loading"} onClick={refresh}>{state === "loading" ? "Refreshing…" : "Refresh overview"}</Button>
      </div>
      <LoadState state={state} error={error} onRetry={refresh}>
        {snapshot && <>
          {!summary && <Notice>Allocation counts and borrower activity are unavailable for your current role.</Notice>}
          <div className="metric-grid">
            {summary && <MetricCard label="Pending requests" value={summary.pending} detail="Awaiting review" tone="gold" />}
            <MetricCard label="Active Projects" value={snapshot.projectActiveCount} detail="Current Philippine time" tone="blue" />
            <MetricCard label="Requirements with shortage" value={snapshot.requirementShortageCount} detail="Server-calculated coverage" tone="rose" />
            {summary && <MetricCard label="Issued batches" value={summary.issued} detail="Reusable items in custody" tone="green" />}
            <MetricCard label="Departments" value={snapshot.departmentCount} detail="Organization groups" tone="blue" />
            <MetricCard label="Resource types" value={snapshot.resourceTypeCount} detail="Reusable and consumable categories" tone="gold" />
            {summary && <>
              <MetricCard label="Overdue issued batches" value={summary.overdue} detail="Project period has ended" tone="rose" />
              <MetricCard label="Consumed batches" value={summary.consumed} detail="Whole batches consumed" tone="green" />
            </>}
          </div>
          <div className="overview-panels">
            <GlassPanel className="overview-section" tilt={false}>
              <PanelHeading kicker="Inventory · first five records" title="Resource capacity"><Link className="inline-link" to="/resources">Open Resource Library</Link></PanelHeading>
              {snapshot.resourcePreview.length === 0 ? <div className="state-block state-empty"><h3>No Resources yet</h3><p>Resource capacity will appear after the first record is saved.</p></div>
                : <div className="capacity-list">{snapshot.resourcePreview.map(resource => <article className="capacity-row" key={resource.id}>
                  <div className="capacity-name"><span className="record-symbol" aria-hidden="true">{resource.type === "REUSABLE" ? "↻" : "◇"}</span><span><strong>{resource.name}</strong><small>{resource.type === "REUSABLE" ? "Reusable" : "Consumable"}</small></span></div>
                  <div className="capacity-numbers"><span><small>Available</small><strong>{resource.availableQuantity.toLocaleString()}</strong></span><span><small>Physical total</small><strong>{resource.totalQuantity.toLocaleString()}</strong></span></div>
                  <div className="capacity-track" aria-hidden="true"><span style={{ width: (resource.totalQuantity ? resource.availableQuantity / resource.totalQuantity * 100 : 0) + "%" }} /></div>
                </article>)}</div>}
              <p className="list-footnote">Open Resource Library to browse every Resource.</p>
            </GlassPanel>
            <GlassPanel className="overview-section calendar-section" tilt={false}>
              <PanelHeading kicker="Philippine time · up to ten Projects" title="Active and upcoming Projects"><Link className="inline-link" to="/projects">View schedule</Link></PanelHeading>
              {snapshot.calendarPreview.length === 0 ? <div className="state-block state-empty"><h3>No active or upcoming Projects</h3><p>Ended work remains available in the Projects schedule.</p></div>
                : <div className="calendar-list">{snapshot.calendarPreview.map(({ project, departmentName, active }) => {
                  const [date, ...time] = formatManila(project.startAt).split(",");
                  return <article className="calendar-item" key={project.id}>
                    <div className="calendar-date"><span>{date}</span><small>{time.join(",")}</small></div>
                    <div className="calendar-main"><strong>{project.name}</strong><small>{departmentName}</small></div>
                    <StatusBadge tone={active ? "success" : "blue"}>{active ? "active" : "upcoming"}</StatusBadge>
                  </article>;
                })}</div>}
            </GlassPanel>
            {summary && <GlassPanel className="overview-section recent-section" tilt={false}>
              <PanelHeading kicker="Request history · latest five batches" title="Recent Allocation batches"><Link className="inline-link" to="/allocations">Review all</Link></PanelHeading>
              {summary.recent.length === 0 ? <div className="state-block state-empty"><h3>No Allocation history</h3><p>Confirmed server requests will be listed here.</p></div>
                : <div className="recent-list">{summary.recent.map(allocation => <article key={allocation.id}>
                  <div className="recent-symbol" aria-hidden="true">⇄</div>
                  <div className="recent-main"><strong>{allocation.borrowerName}</strong><small>{allocation.quantity} · {allocation.resourceName} · {formatManila(allocation.createdAt)}</small></div>
                  <StatusBadge tone={allocation.status === "PENDING" ? "warning" : allocation.status === "RETURNED" || allocation.status === "CONSUMED" ? "success" : "blue"}>{allocation.status.toLowerCase()}</StatusBadge>
                </article>)}</div>}
            </GlassPanel>}
          </div>
        </>}
      </LoadState>
    </section>
  );
}
