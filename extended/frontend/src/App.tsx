import { HashRouter, Navigate, Route, Routes } from "react-router-dom";
import { AuthProvider, useAuth } from "./auth/AuthContext";
import { AccountPage, GovernancePage, InvitationPage, LoginPage, OrganizationPicker, RecoveryPage } from "./auth/AuthScreens";
import { AboutPage } from "./pages/AboutPage";
import { AllocationsPage } from "./pages/AllocationsPage";
import { DepartmentsPage } from "./pages/DepartmentsPage";
import { OverviewPage } from "./pages/OverviewPage";
import { ProjectsPage } from "./pages/ProjectsPage";
import { RemindersPage } from "./pages/RemindersPage";
import { RequirementsPage } from "./pages/RequirementsPage";
import { ResourcesPage } from "./pages/ResourcesPage";
import { ActivityPage, ExportsPage } from "./pages/ReportingPages";
import { AcademicShell } from "./shell/AcademicShell";
import { SettingsProvider } from "./ui";

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
  return <SettingsProvider><AuthProvider><HashRouter><AppRoutes /></HashRouter></AuthProvider></SettingsProvider>;
}
