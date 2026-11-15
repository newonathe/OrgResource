import { useEffect, useMemo, useState } from "react";
import { api } from "../api/client";
import type { Allocation, Project, Resource, ResourceRequirement } from "../api/types";
import { explainError, formatManila, manilaWallTime, reminderLabel, reminderWindow } from "../domain";
import { Button, GlassPanel, LoadState, Notice, PageHeader, PanelHeading, StatusBadge } from "../ui";

export function RemindersPage() {
  const [allocations, setAllocations] = useState<Allocation[]>([]);
  const [projects, setProjects] = useState<Project[]>([]);
  const [resources, setResources] = useState<Resource[]>([]);
  const [requirements, setRequirements] = useState<ResourceRequirement[]>([]);
  const [state, setState] = useState<"loading" | "error" | "ready">("loading");
  const [error, setError] = useState("");
  const [reload, setReload] = useState(0);
  const [now, setNow] = useState(manilaWallTime());
  const [updatedAt, setUpdatedAt] = useState("");

  useEffect(() => {
    const interval = window.setInterval(() => setNow(manilaWallTime()), 60_000);
    return () => window.clearInterval(interval);
  }, []);
  useEffect(() => {
    const controller = new AbortController();
    Promise.all([api.allocations({}, controller.signal), api.projects(undefined, controller.signal), api.resources(undefined, controller.signal), api.requirements(undefined, controller.signal)])
      .then(([allocationRows, projectRows, resourceRows, requirementRows]) => {
        setAllocations(allocationRows); setProjects(projectRows); setResources(resourceRows); setRequirements(requirementRows);
        setUpdatedAt(manilaWallTime()); setError(""); setState("ready");
      }).catch((reason: unknown) => {
        if (reason instanceof DOMException && reason.name === "AbortError") return;
        setError(explainError(reason)); setState("error");
      });
    return () => controller.abort();
  }, [reload]);
  const refresh = () => { setState("loading"); setReload((value) => value + 1); };

  const requirementMap = useMemo(() => new Map(requirements.map((item) => [item.id, item])), [requirements]);
  const projectMap = useMemo(() => new Map(projects.map((item) => [item.id, item])), [projects]);
  const resourceMap = useMemo(() => new Map(resources.map((item) => [item.id, item])), [resources]);
  const plus24h = manilaWallTime(new Date(Date.parse(`${now}+08:00`) + 24 * 60 * 60 * 1000));
  const issued = allocations.filter((allocation) => {
    if (allocation.status !== "ISSUED") return false;
    const requirement = requirementMap.get(allocation.requirementId);
    return resourceMap.get(requirement?.resourceId ?? -1)?.type === "REUSABLE";
  }).map((allocation) => {
    const requirement = requirementMap.get(allocation.requirementId);
    const project = projectMap.get(requirement?.projectId ?? -1);
    const resource = resourceMap.get(requirement?.resourceId ?? -1);
    return { allocation, project, resource };
  });
  const dueSoon = issued.filter(({ project }) => !!project && reminderWindow(project.endAt, now, plus24h) === "DUE_SOON");
  const overdue = issued.filter(({ project }) => !!project && reminderWindow(project.endAt, now, plus24h) === "OVERDUE");
  const unavailableCount = issued.filter(({ project }) => !project).length;

  return (
    <section className="page-shell" aria-labelledby="reminder-activity-title">
      <PageHeader eyebrow="System · scheduled activity" title="Reminder Activity" description="See upcoming return windows and outcomes recorded by the backend reminder scheduler."
        action={<div className="header-stat"><span>{state === "ready" ? issued.length : "—"}</span><small>issued reusable batches</small></div>} />
      {unavailableCount > 0 && <Notice tone="warning">The Project period for {unavailableCount} issued batch{unavailableCount === 1 ? "" : "es"} could not be resolved. Those rows are not counted as due or overdue.</Notice>}
      <div className="overview-toolbar"><span>Updated: <strong>{updatedAt ? formatManila(updatedAt) : "Loading"}</strong></span><Button variant="quiet" disabled={state === "loading"} onClick={refresh}>Refresh</Button></div>
      <LoadState state={state} error={error} onRetry={refresh}>
        {issued.length === 0 ? <GlassPanel className="state-block state-empty"><span className="empty-symbol" aria-hidden="true">◷</span><h3>No issued reusable batches</h3><p>When an Allocation is issued, the backend determines whether a scheduled reminder is eligible.</p></GlassPanel>
          : <div className="reminder-groups">
            <GlassPanel className="reminder-group" tilt={false}><PanelHeading kicker="Next 24 hours" title="Return window approaching"><span className="group-count">{dueSoon.length}</span></PanelHeading>
              {dueSoon.length === 0 ? <p className="panel-copy">No issued reusable batch is due within the upcoming 24 hours.</p> : <ReminderList rows={dueSoon} />}</GlassPanel>
            <GlassPanel className="reminder-group" tilt={false}><PanelHeading kicker="Project end passed" title="Overdue issued batches"><span className="group-count overdue-count">{overdue.length}</span></PanelHeading>
              {overdue.length === 0 ? <p className="panel-copy">No currently issued reusable batch has passed its Project end.</p> : <ReminderList rows={overdue} overdue />}</GlassPanel>
            <GlassPanel className="reminder-group reminder-all" tilt={false}><PanelHeading kicker="Backend records" title="All issued reusable batches" />
              <ReminderList rows={issued} /></GlassPanel>
          </div>}
      </LoadState>
      <p className="list-footnote">Reminder outcomes describe request processing. “Accepted by local simulator” and “Provider accepted request” do not prove handset delivery. This screen has no manual send action.</p>
    </section>
  );
}

function ReminderList({ rows, overdue = false }: {
  rows: Array<{ allocation: Allocation; project?: Project; resource?: Resource }>;
  overdue?: boolean;
}) {
  return <div className="reminder-list">{rows.map(({ allocation, project, resource }) => <article className="reminder-row" key={allocation.id}>
    <div className="reminder-date"><span aria-hidden="true">◷</span><strong>{project ? formatManila(project.endAt) : "Project unavailable"}</strong></div>
    <div className="reminder-main"><strong>{resource?.name ?? "Resource unavailable"} · {allocation.quantity} units</strong><small>Allocation A{allocation.id} · {allocation.borrowerName}{project ? ` · ${project.name}` : ""}</small></div>
    <div className="reminder-status"><StatusBadge tone={overdue ? "danger" : allocation.reminderOutcome === "SIMULATED" || allocation.reminderOutcome === "PROVIDER_ACCEPTED" ? "success" : allocation.reminderOutcome === "UNKNOWN" ? "warning" : "neutral"}>{reminderLabel(allocation.reminderOutcome)}</StatusBadge>
      <small>{allocation.reminderAttempted ? "Attempt recorded" : "No attempt recorded"}</small></div>
    <div className="reminder-consent"><span>Consent</span><strong>{allocation.smsConsent ? "Granted" : "Not granted"}</strong><span>Contact</span><strong>{allocation.contactPresent ? "Present · redacted" : "None"}</strong></div>
  </article>)}</div>;
}
