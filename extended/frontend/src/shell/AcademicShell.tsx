import { useEffect, useRef, useState } from "react";
import { NavLink, useLocation } from "react-router-dom";
import { formatManila } from "../domain";
import { Button, useSettings } from "../ui";
import { useAuth } from "../auth/AuthContext";

type Role = "OWNER" | "ADMIN" | "CUSTODIAN" | "LEAD" | "MEMBER";
type LinkItem = { to: string; label: string; icon: string };
function groupsFor(role: Role): { title: string; links: LinkItem[] }[] {
  const activity = role === "OWNER" || role === "ADMIN" || role === "CUSTODIAN";
  const governance = role === "OWNER" || role === "ADMIN";
  return [
    { title: "Workspace", links: [{ to: "/", label: "Overview", icon: "⌂" }, ...(activity ? [{ to: "/allocations", label: "Allocations", icon: "⇄" }] : []), { to: "/resources", label: "Resource Library", icon: "◈" }, { to: "/projects", label: "Projects", icon: "▤" }] },
    { title: "Organization", links: [{ to: "/requirements", label: "Requirements", icon: "☷" }, { to: "/departments", label: "Departments", icon: "⌘" }, ...(governance ? [{ to: "/governance", label: "Members and access", icon: "♙" }] : [])] },
    { title: "System", links: [...(activity ? [{ to: "/reminders", label: "Reminder Activity", icon: "◷" }] : []), { to: "/account", label: "Account and security", icon: "◉" }, { to: "/about", label: "About", icon: "ⓘ" }] },
  ];
}

function AcademicBackdrop() {
  return (
    <svg className="academic-architecture" viewBox="0 0 1100 720" aria-hidden="true" focusable="false">
      <g fill="none" stroke="currentColor" strokeWidth="1.2">
        <path d="M45 210 550 60l505 150M90 220h920M120 230v270m100-270v270m660-270v270m100-270v270M80 510h940M65 525h970M150 560h800" />
        <path d="M145 195 550 86l405 109M165 195v28m770-28v28M265 525v100m570-100v100M245 625h610M280 625v24m540-24v24M310 649h480" />
        <path d="M468 600q82-72 164 0v58q-82-56-164 0zM550 600v54M468 600q23 12 0 58m164-58q-23 12 0 58" />
        <path d="M120 670h860M96 680h908M550 44v-24m-10 18h20M70 190h18m924 0h18" strokeDasharray="3 8" />
        <path d="M60 120h980M60 130h980M100 110v90m900-90v90" strokeDasharray="1 8" />
      </g>
    </svg>
  );
}

function Navigation({ groups, onNavigate }: { groups: { title: string; links: LinkItem[] }[]; onNavigate?: () => void }) {
  return <nav aria-label="Main navigation" className="sidebar-nav">
    {groups.map((group) => <div className="nav-group" key={group.title}>
      <p className="nav-group-title">{group.title}</p>
      {group.links.map((link) => <NavLink key={link.to} to={link.to} end={link.to === "/"} className="nav-link" onClick={onNavigate}>
        <span className="nav-icon" aria-hidden="true">{link.icon}</span><span>{link.label}</span>
      </NavLink>)}
    </div>)}
  </nav>;
}

export function AcademicShell({ children, role, organizationName, sessionError }: { children: React.ReactNode; role: Role; organizationName: string; sessionError: string | null }) {
  const { theme, setTheme, motionEnabled, setMotionEnabled } = useSettings();
  const { organizations, switchOrganization, session, signOut } = useAuth();
  const [menuOpen, setMenuOpen] = useState(false);
  const [moreOpen, setMoreOpen] = useState(false);
  const frame = useRef<number | null>(null);
  const location = useLocation();
  const groups = groupsFor(role);
  const mobileLinks = groups.flatMap(group => group.links).filter(link => ["/", "/allocations", "/resources", "/projects"].includes(link.to)).slice(0, 4);
  const title = groups.flatMap((group) => group.links).find((link) => link.to === location.pathname)?.label ?? "Overview";
  const organizationSelector = (
    <label className="organization-switch-label">Organization<select aria-label="Switch organization" value={session?.activeOrgId ?? ""} onChange={event => { if (event.target.value) void switchOrganization(event.target.value).catch(() => undefined); }}>
      {organizations.map(org => <option key={org.id} value={org.id}>{org.displayName} · {org.role}</option>)}
    </select></label>
  );

  useEffect(() => () => { if (frame.current !== null) cancelAnimationFrame(frame.current); }, []);

  const moveLight = (event: React.PointerEvent<HTMLDivElement>) => {
    if (!motionEnabled || !window.matchMedia("(pointer: fine)").matches) return;
    const node = event.currentTarget;
    const x = event.clientX;
    const y = event.clientY;
    if (frame.current !== null) cancelAnimationFrame(frame.current);
    frame.current = requestAnimationFrame(() => {
      node.style.setProperty("--ambient-x", `${x}px`);
      node.style.setProperty("--ambient-y", `${y}px`);
    });
  };

  return (
    <div className="app-shell" data-theme={theme} data-motion={motionEnabled ? "on" : "off"} onPointerMove={moveLight}>
      <AcademicBackdrop />
      <a className="skip-link" href="#main" onClick={(event) => { event.preventDefault(); document.getElementById("main")?.focus(); }}>
        Skip to main content
      </a>
      {menuOpen && <button className="sidebar-scrim" aria-label="Close navigation" onClick={() => setMenuOpen(false)} />}
      <aside className={`sidebar ${menuOpen ? "sidebar-open" : ""}`} aria-label="OrgResource navigation">
        <NavLink to="/" className="brand-lockup" aria-label="OrgResource Academic Commons, Overview" onClick={() => setMenuOpen(false)}>
          <span className="brand-seal" aria-hidden="true"><span>O</span><i>R</i></span>
          <span><strong>OrgResource</strong><small>Academic Commons</small></span>
        </NavLink>
        <Navigation groups={groups} onNavigate={() => setMenuOpen(false)} />
      </aside>

      <div className="workspace-column">
        <header className="top-toolbar glass-panel">
          <div className="toolbar-leading">
            <Button className="menu-toggle" variant="quiet" aria-label="Open navigation" aria-expanded={menuOpen} onClick={() => setMenuOpen((value) => !value)}>☰</Button>
            <div><p className="toolbar-kicker">{organizationName}</p><span className="toolbar-title">{title}</span></div>
          </div>
          <div className="toolbar-actions">
            {organizationSelector}
            <span className="toolbar-date">{formatManila(new Date().toISOString())}</span>
            <Button variant="quiet" className="setting-button" aria-pressed={motionEnabled}
              onClick={() => setMotionEnabled(!motionEnabled)}><span aria-hidden="true">✧</span><span>Motion {motionEnabled ? "on" : "off"}</span></Button>
            <Button variant="quiet" className="setting-button" aria-label={"Switch to " + (theme === "light" ? "dark" : "light") + " theme"}
              onClick={() => setTheme(theme === "light" ? "dark" : "light")}><span aria-hidden="true">{theme === "light" ? "☾" : "☼"}</span><span>{theme === "light" ? "Dark" : "Light"}</span></Button>
            <Button variant="quiet" className="setting-button sign-out-button" onClick={() => void signOut()}>Sign out</Button>
          </div>
        </header>

        {sessionError && <div className="session-banner" role="alert">{sessionError}</div>}

        <main id="main" className="main-content" tabIndex={-1}>{children}</main>
        <footer className="site-footer"><span>Saved records come from the Java API and MySQL.</span></footer>
      </div>

      <nav className="mobile-bottom-nav" aria-label="Mobile navigation">
        {mobileLinks.map((link) => <NavLink key={link.to} to={link.to} end={link.to === "/"} className="mobile-nav-link" onClick={() => setMoreOpen(false)}><span>{link.label}</span></NavLink>)}
        <Button variant="quiet" className={moreOpen ? "mobile-nav-link mobile-more active" : "mobile-nav-link mobile-more"} aria-expanded={moreOpen}
          onClick={() => setMoreOpen((value) => !value)}>More</Button>
      </nav>
      {moreOpen && <div className="mobile-more-menu">
        <div className="mobile-workspace-actions">
          {organizationSelector}
          <Button variant="quiet" onClick={() => void signOut()}>Sign out</Button>
        </div>
        <Navigation groups={groups} onNavigate={() => setMoreOpen(false)} />
      </div>}
    </div>
  );
}
