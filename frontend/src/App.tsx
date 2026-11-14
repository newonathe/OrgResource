import { HashRouter, NavLink, Navigate, Route, Routes } from "react-router-dom";
import { DepartmentsPage } from "./pages/DepartmentsPage";
import { ResourcesPage } from "./pages/ResourcesPage";

export function App() {
  return (
    <HashRouter>
      <a className="skip-link" href="#main">Skip to content</a>
      <div className="app-shell">
        <header className="site-header">
          <div className="brand-block">
            <div className="brand-mark" aria-hidden="true">OR</div>
            <div>
              <p className="eyebrow">Organization planning</p>
              <a className="brand-name" href="#/departments">OrgResource</a>
            </div>
          </div>
          <nav aria-label="Main navigation" className="main-nav">
            <NavLink to="/departments">Departments</NavLink>
            <NavLink to="/resources">Resources</NavLink>
            <NavLink to="/about">About</NavLink>
          </nav>
          <span className="local-pill"><span aria-hidden="true" /> Local prototype</span>
        </header>

        <main id="main" className="main-content" tabIndex={-1}>
          <Routes>
            <Route path="/" element={<Navigate replace to="/departments" />} />
            <Route path="/departments" element={<DepartmentsPage />} />
            <Route path="/resources" element={<ResourcesPage />} />
            <Route path="/about" element={<AboutPage />} />
            <Route path="*" element={<Navigate replace to="/departments" />} />
          </Routes>
        </main>

        <footer className="site-footer">
          <span>OrgResource · CSCI 180.05</span>
          <span>Local data is served by the running Java and MySQL backend.</span>
        </footer>
      </div>
    </HashRouter>
  );
}

function AboutPage() {
  return (
    <section className="page-shell narrow-page" aria-labelledby="about-title">
      <p className="eyebrow">Project notes</p>
      <h1 id="about-title">A clearer view of shared resources.</h1>
      <p className="lede">OrgResource helps one organization coordinate projects, resource needs, and reusable stock from a single source of truth.</p>
      <div className="about-card">
        <h2>What this prototype supports</h2>
        <ul>
          <li>Create, rename, and remove departments while respecting project links.</li>
          <li>Track reusable and consumable resources with availability calculated by the backend.</li>
          <li>Use the API and MySQL database as the authority for saved records and quantities.</li>
        </ul>
        <p>Project, requirement, and allocation workflows will appear in later implementation phases.</p>
      </div>
      <p className="quiet-note">This browser app is a local academic prototype. It does not provide user authentication or production access controls.</p>
    </section>
  );
}
