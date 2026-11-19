import { HashRouter, Navigate, Route, Routes } from "react-router-dom";
import { useEffect, useState, type ReactNode } from "react";
import { request } from "./api/client";
import { AuthProvider, useAuth } from "./auth/AuthContext";
import { AccountPage, GovernancePage, InvitationPage, LoginPage, OrganizationPicker, RecoveryPage } from "./auth/AuthScreens";
import { AboutPage } from "./pages/AboutPage";
import { AllocationsPage } from "./pages/AllocationsPage";
import { DepartmentsPage } from "./pages/DepartmentsPage";
import { OverviewPage } from "./pages/OverviewPage";
import { ProjectsPage } from "./pages/ProjectsPage";
import { BorrowedAllocationsPage, ProjectWorkPage } from "./pages/ProjectWorkPages";
import { RemindersPage } from "./pages/RemindersPage";
import { RequirementsPage } from "./pages/RequirementsPage";
import { ResourcesPage } from "./pages/ResourcesPage";
import { ActivityPage, ExportsPage } from "./pages/ReportingPages";
import { AcademicShell } from "./shell/AcademicShell";
import { Button, SettingsProvider } from "./ui";

function WorkspaceRoutes() {
  const { ready, session, organizations, error } = useAuth();
  if (!ready) return <div className="boot-state" role="status">Restoring the secure server session…</div>;
  if (!session) return <Navigate replace to="/login" />;
  const organization = organizations.find(row => row.id === session.activeOrgId);
  if (!organization) return <OrganizationPicker />;
  const canViewActivity = ["OWNER", "ADMIN", "CUSTODIAN"].includes(organization.role);
  const canGovern = organization.role === "OWNER" || organization.role === "ADMIN";
  return <AcademicShell role={organization.role} organizationName={organization.displayName} sessionError={error}>
    <div key={organization.id} className="tenant-workspace">
      <Routes>
        <Route path="/" element={<OverviewPage />} />
        <Route path="/allocations" element={canViewActivity ? <AllocationsPage /> : <ForbiddenPage />} />
        <Route path="/projects" element={<ProjectsPage />} />
        <Route path="/project-work" element={canGovern || organization.role === "LEAD"
          ? <ProjectWorkPage orgId={organization.id} role={organization.role} /> : <ForbiddenPage />} />
        <Route path="/borrowed" element={<BorrowedAllocationsPage orgId={organization.id} />} />
        <Route path="/requirements" element={<RequirementsPage />} />
        <Route path="/departments" element={<DepartmentsPage />} />
        <Route path="/resources" element={<ResourcesPage />} />
        <Route path="/reminders" element={canViewActivity ? <RemindersPage /> : <ForbiddenPage />} />
        <Route path="/about" element={<AboutPage />} />
        <Route path="/account" element={<AccountPage />} />
        <Route path="/governance" element={canGovern ? <GovernancePage orgId={organization.id} role={organization.role} /> : <ForbiddenPage />} />
        <Route path="/activity" element={canGovern ? <ActivityPage orgId={organization.id} /> : <ForbiddenPage />} />
        <Route path="/exports" element={canGovern ? <ExportsPage orgId={organization.id} /> : <ForbiddenPage />} />
        <Route path="/invite" element={<InvitationPage />} />
        <Route path="*" element={<Navigate replace to="/" />} />
      </Routes>
    </div>
  </AcademicShell>;
}

function ForbiddenPage() {
  return <section className="page-shell"><h1>Not available for this role</h1><p>The server does not permit this membership to view these activity records.</p></section>;
}

function AppRoutes() {
  const { ready, session } = useAuth();
  return <Routes>
    <Route path="/login" element={session ? <Navigate replace to="/" /> : <LoginPage />} />
    <Route path="/recover" element={<RecoveryPage />} />
    <Route path="/invite" element={<InvitationPage />} />
    <Route path="*" element={ready ? <WorkspaceRoutes /> : <div className="boot-state" role="status">Restoring the secure server session…</div>} />
  </Routes>;
}

export function App() {
  return <SettingsProvider><DemoDisclosure><AuthProvider><HashRouter><AppRoutes /></HashRouter></AuthProvider></DemoDisclosure></SettingsProvider>;
}

type Environment = { status: "checking" } | { status: "unavailable" } | { status: "ready"; notice: string | null };

function classifyEnvironment(data: unknown): Environment {
  if (typeof data !== "object" || data === null || !("synthetic" in data)) return { status: "unavailable" };
  const payload = data as Record<string, unknown>;
  if (payload.synthetic === false && payload.notice == null && payload.datasetId == null && payload.demonstrationAsOf == null)
    return { status: "ready", notice: null };
  if (payload.synthetic === true && typeof payload.notice === "string" && payload.notice.startsWith("DEMO ENVIRONMENT")
      && typeof payload.datasetId === "string" && payload.datasetId.trim()
      && typeof payload.demonstrationAsOf === "string" && /^\d{4}-\d{2}-\d{2}$/.test(payload.demonstrationAsOf))
    return { status: "ready", notice: payload.notice };
  return { status: "unavailable" };
}

function DemoDisclosure({ children }: { children: ReactNode }) {
  const [attempt, setAttempt] = useState(0);
  const [environment, setEnvironment] = useState<Environment>({ status: "checking" });
  useEffect(() => {
    const controller = new AbortController();
    const timeout = window.setTimeout(() => {
      controller.abort();
      setEnvironment({ status: "unavailable" });
    }, 10000);
    void request<unknown>("/v2/demo/metadata", "GET", undefined, { signal: controller.signal })
      .then(result => {
        if (!controller.signal.aborted) setEnvironment(classifyEnvironment(result.data));
      }).catch(() => {
        if (!controller.signal.aborted) setEnvironment({ status: "unavailable" });
      }).finally(() => window.clearTimeout(timeout));
    return () => {
      controller.abort();
      window.clearTimeout(timeout);
    };
  }, [attempt]);
  if (environment.status === "checking") return <main className="boot-state" role="status">Checking workspace classification…</main>;
  if (environment.status === "unavailable") return <main className="boot-state">
    <section role="alert"><h1>Workspace classification unavailable</h1>
      <p>Workspace access will resume after the server confirms whether this environment contains simulated data.</p></section>
    <Button onClick={() => { setEnvironment({ status: "checking" }); setAttempt(value => value + 1); }}>Retry classification</Button>
  </main>;
  return <>{environment.notice && <div className="demo-disclosure" role="status">{environment.notice}</div>}{children}</>;
}
