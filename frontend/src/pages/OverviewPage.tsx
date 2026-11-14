import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api/client";
import type { Allocation, Department, Project, Resource, ResourceRequirement } from "../api/types";
import { explainError, formatManila, manilaWallTime, projectPeriod } from "../domain";
import { Button, GlassPanel, MetricCard, Notice, PageHeader, PanelHeading, StatusBadge } from "../ui";

type Slice<T> = { status: "loading" | "ready" | "error"; data: T[]; error: string };
interface OverviewData {
  departments: Slice<Department>;
  projects: Slice<Project>;
  resources: Slice<Resource>;
  requirements: Slice<ResourceRequirement>;
  allocations: Slice<Allocation>;
}

const blankSlice = <T,>(): Slice<T> => ({ status: "loading", data: [], error: "" });
const blankData = (): OverviewData => ({ departments: blankSlice(), projects: blankSlice(), resources: blankSlice(), requirements: blankSlice(), allocations: blankSlice() });

function Metric<T>({ label, data, count, detail, tone }: {
  label: string; data: Slice<T>; count: (rows: T[]) => number; detail: string; tone: "blue" | "gold" | "green" | "rose";
}) {
  const value = data.status === "ready" ? count(data.data) : "—";
  const note = data.status === "error" ? "Unavailable · retry to refresh" : data.status === "loading" ? "Loading current records" : detail;
  return <MetricCard label={label} value={value} detail={note} tone={tone} />;
}

export function OverviewPage() {
  const [data, setData] = useState<OverviewData>(blankData);
  const [updatedAt, setUpdatedAt] = useState("");
  const [reload, setReload] = useState(0);
  const [now, setNow] = useState(manilaWallTime());
  const [refreshing, setRefreshing] = useState(false);

  useEffect(() => {
    const interval = window.setInterval(() => setNow(manilaWallTime()), 60_000);
    return () => window.clearInterval(interval);
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    const load = async <T,>(promise: Promise<T[]>): Promise<Slice<T>> => {
      try { return { status: "ready", data: await promise, error: "" }; }
      catch (reason) {
        if (reason instanceof DOMException && reason.name === "AbortError") throw reason;
        return { status: "error", data: [], error: explainError(reason) };
      }
    };
    Promise.all([
      load(api.departments(controller.signal)), load(api.projects(undefined, controller.signal)),
      load(api.resources(undefined, controller.signal)), load(api.requirements(undefined, controller.signal)),
      load(api.allocations({}, controller.signal)),
    ]).then(([departments, projects, resources, requirements, allocations]) => {
      setData({ departments, projects, resources, requirements, allocations });
      setUpdatedAt(manilaWallTime()); setRefreshing(false);
    }).catch((reason: unknown) => {
      if (reason instanceof DOMException && reason.name === "AbortError") return;
      setRefreshing(false);
    });
    return () => controller.abort();
  }, [reload]);

  const refreshOverview = () => {
    setRefreshing(true);
    setData((previous) => Object.fromEntries(Object.entries(previous).map(([key, slice]) => [key, { ...slice, status: "loading", error: "" }])) as OverviewData);
    setReload((value) => value + 1);
  };

  const currentProjects = data.projects.status === "ready" ? data.projects.data : [];
  const activeProjects = currentProjects.filter((project) => project.startAt <= now && project.endAt > now);
  const upcomingProjects = currentProjects.filter((project) => project.startAt > now).sort((a, b) => a.startAt.localeCompare(b.startAt)).slice(0, 5);
  const currentAllocations = data.allocations.status === "ready" ? data.allocations.data : [];
  const unavail = [data.departments, data.projects, data.resources, data.requirements, data.allocations].some((slice) => slice.status === "error");
  const overdueData: Slice<Allocation> = data.allocations.status !== "ready" || data.requirements.status !== "ready" || data.projects.status !== "ready"
    ? { status: "error", data: [], error: "Project periods could not be completely refreshed." }
    : data.allocations;
  const departmentMap = useMemo(() => new Map(data.departments.data.map((item) => [item.id, item.name])), [data.departments.data]);
  const resourceMap = useMemo(() => new Map(data.resources.data.map((item) => [item.id, item])), [data.resources.data]);
  const requirementMap = useMemo(() => new Map(data.requirements.data.map((item) => [item.id, item])), [data.requirements.data]);
  const recentAllocations = [...currentAllocations].sort((a, b) => b.createdAt.localeCompare(a.createdAt)).slice(0, 5);

  const statusFor = (slice: Slice<unknown>) => slice.status;
  return (
    <section className="page-shell overview-page" aria-labelledby="overview-title">
      <PageHeader eyebrow="Workspace · current snapshot" title="Overview" description="A live view of project activity and shared resources, refreshed from the Java service."
        action={<Link className="button button-primary button-magnetic" to="/allocations?status=PENDING">Review requests <span aria-hidden="true">↗</span></Link>} />
      <GlassPanel as="div" className="overview-hero" tilt={false}>
        <div><p className="hero-kicker">Planning with clarity</p><h2>Shared resources, thoughtfully coordinated.</h2><p>Project needs, inventory availability, and whole-batch Allocation history from one academic workspace.</p></div>
        <div className="hero-illustration" aria-hidden="true"><span className="hero-arc" /><span className="hero-pillar pillar-one" /><span className="hero-pillar pillar-two" /><span className="hero-book">⌑</span></div>
      </GlassPanel>

      <div className="overview-toolbar"><span>Snapshot time: <strong>{updatedAt ? formatManila(updatedAt) : "Loading"}</strong> · Philippine time</span><Button variant="quiet" disabled={refreshing} onClick={refreshOverview}>{refreshing ? "Refreshing…" : "Refresh overview"}</Button></div>
      {unavail && <Notice tone="warning">Some lists could not be refreshed. Metrics that depend on them are shown as unavailable rather than zero.</Notice>}
      <div className="metric-grid">
        <Metric label="Pending requests" data={data.allocations} count={(rows) => rows.filter((item) => item.status === "PENDING").length} detail="Awaiting review" tone="gold" />
        <Metric label="Active Projects" data={data.projects} count={(rows) => rows.filter((item) => item.startAt <= now && item.endAt > now).length} detail="Current Philippine time" tone="blue" />
        <Metric label="Requirements with shortage" data={data.requirements} count={(rows) => rows.filter((item) => item.shortageQuantity > 0).length} detail="Server-calculated coverage" tone="rose" />
        <Metric label="Issued batches" data={data.allocations} count={(rows) => rows.filter((item) => item.status === "ISSUED").length} detail="Reusable items in custody" tone="green" />
        <Metric label="Departments" data={data.departments} count={(rows) => rows.length} detail="Organization groups" tone="blue" />
        <Metric label="Resource types" data={data.resources} count={(rows) => new Set(rows.map((item) => item.type)).size} detail="Reusable and consumable categories" tone="gold" />
        <Metric label="Overdue issued batches" data={overdueData} count={(rows) => rows.filter((item) => item.status === "ISSUED" && (() => { const req = requirementMap.get(item.requirementId); const project = req ? currentProjects.find((candidate) => candidate.id === req.projectId) : undefined; return !!project && project.endAt <= now; })()).length} detail="Project period has ended" tone="rose" />
        <Metric label="Consumed batches" data={data.allocations} count={(rows) => rows.filter((item) => item.status === "CONSUMED").length} detail="Whole batches consumed" tone="green" />
      </div>

      <div className="overview-panels">
        <GlassPanel className="overview-section" tilt={false}>
          <PanelHeading kicker="Inventory · by record" title="Resource capacity"><Link className="inline-link" to="/resources">Open Resource Library</Link></PanelHeading>
          {statusFor(data.resources) === "loading" ? <p className="panel-copy" role="status">Loading server inventory…</p> : statusFor(data.resources) === "error" ? <div className="state-block state-error"><p>{data.resources.error}</p><Button onClick={refreshOverview}>Retry</Button></div>
            : data.resources.data.length === 0 ? <div className="state-block state-empty"><h3>No Resources yet</h3><p>Resource capacity will appear after the first record is saved.</p></div>
              : <div className="capacity-list">{data.resources.data.slice().sort((a, b) => a.name.localeCompare(b.name)).map((resource) => <article className="capacity-row" key={resource.id}>
                <div className="capacity-name"><span className="record-symbol" aria-hidden="true">{resource.type === "REUSABLE" ? "↻" : "◇"}</span><span><strong>{resource.name}</strong><small>{resource.type === "REUSABLE" ? "Reusable" : "Consumable"}</small></span></div>
                <div className="capacity-numbers"><span><small>Available</small><strong>{resource.availableQuantity.toLocaleString()}</strong></span><span><small>Physical total</small><strong>{resource.totalQuantity.toLocaleString()}</strong></span></div>
                <div className="capacity-track" aria-hidden="true"><span style={{ width: `${resource.totalQuantity > 0 ? Math.min(100, resource.availableQuantity / resource.totalQuantity * 100) : 0}%` }} /></div>
              </article>)}</div>}
          <p className="list-footnote">Quantities remain separate by Resource; they are never combined across unlike items.</p>
        </GlassPanel>

        <GlassPanel className="overview-section calendar-section" tilt={false}>
          <PanelHeading kicker="Philippine time" title="Active and upcoming Projects"><Link className="inline-link" to="/projects">View schedule</Link></PanelHeading>
          {data.projects.status === "loading" ? <p className="panel-copy" role="status">Loading Project periods…</p> : data.projects.status === "error" ? <div className="state-block state-error"><p>{data.projects.error}</p><Button onClick={() => setReload((value) => value + 1)}>Retry</Button></div>
            : activeProjects.length + upcomingProjects.length === 0 ? <div className="state-block state-empty"><h3>No active or upcoming Projects</h3><p>Ended work remains available in the Projects schedule.</p></div>
              : <div className="calendar-list">{[...activeProjects, ...upcomingProjects].map((project) => <article className="calendar-item" key={project.id}>
                <div className="calendar-date"><span>{formatManila(project.startAt).split(",")[0]}</span><small>{formatManila(project.startAt).split(",").slice(1).join(",")}</small></div>
                <div className="calendar-main"><strong>{project.name}</strong><small>{departmentMap.get(project.departmentId) ?? `Department ${project.departmentId}`}</small></div>
                <StatusBadge tone={projectPeriod(project) === "ACTIVE" ? "success" : "blue"}>{projectPeriod(project).toLowerCase()}</StatusBadge>
              </article>)}</div>}
        </GlassPanel>

        <GlassPanel className="overview-section recent-section" tilt={false}>
          <PanelHeading kicker="Request history" title="Recent Allocation batches"><Link className="inline-link" to="/allocations">Review all</Link></PanelHeading>
          {data.allocations.status === "loading" ? <p className="panel-copy" role="status">Loading Allocation history…</p> : data.allocations.status === "error" ? <div className="state-block state-error"><p>{data.allocations.error}</p><Button onClick={() => setReload((value) => value + 1)}>Retry</Button></div>
            : recentAllocations.length === 0 ? <div className="state-block state-empty"><h3>No Allocation history</h3><p>Confirmed server requests will be listed here.</p></div>
              : <div className="recent-list">{recentAllocations.map((allocation) => {
                const requirement = requirementMap.get(allocation.requirementId); const resource = resourceMap.get(requirement?.resourceId ?? -1);
                return <article key={allocation.id}><div className="recent-symbol" aria-hidden="true">⇄</div><div className="recent-main"><strong>{allocation.borrowerName}</strong><small>{allocation.quantity} · {resource?.name ?? "Resource"} · {formatManila(allocation.createdAt)}</small></div><StatusBadge tone={allocation.status === "PENDING" ? "warning" : allocation.status === "RETURNED" || allocation.status === "CONSUMED" ? "success" : "blue"}>{allocation.status.toLowerCase()}</StatusBadge></article>;
              })}</div>}
        </GlassPanel>
      </div>
    </section>
  );
}
