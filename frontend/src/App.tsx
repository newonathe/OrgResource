import { HashRouter, Navigate, Route, Routes } from "react-router-dom";
import { AllocationsPage } from "./pages/AllocationsPage";
import { AboutPage } from "./pages/AboutPage";
import { DepartmentsPage } from "./pages/DepartmentsPage";
import { OverviewPage } from "./pages/OverviewPage";
import { ProjectsPage } from "./pages/ProjectsPage";
import { RemindersPage } from "./pages/RemindersPage";
import { RequirementsPage } from "./pages/RequirementsPage";
import { ResourcesPage } from "./pages/ResourcesPage";
import { AcademicShell } from "./shell/AcademicShell";
import { SettingsProvider } from "./ui";

export function App() {
  return (
    <SettingsProvider>
      <HashRouter>
        <AcademicShell>
          <Routes>
            <Route path="/" element={<OverviewPage />} />
            <Route path="/allocations" element={<AllocationsPage />} />
            <Route path="/projects" element={<ProjectsPage />} />
            <Route path="/requirements" element={<RequirementsPage />} />
            <Route path="/departments" element={<DepartmentsPage />} />
            <Route path="/resources" element={<ResourcesPage />} />
            <Route path="/reminders" element={<RemindersPage />} />
            <Route path="/about" element={<AboutPage />} />
            <Route path="*" element={<Navigate replace to="/" />} />
          </Routes>
        </AcademicShell>
      </HashRouter>
    </SettingsProvider>
  );
}
