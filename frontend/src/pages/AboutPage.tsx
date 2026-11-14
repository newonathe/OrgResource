import { Link } from "react-router-dom";
import { GlassPanel, PageHeader, PanelHeading, StatusBadge } from "../ui";

const domainRows = [
  ["Department", "An organization group that owns Projects."],
  ["Project", "A scheduled activity with a Philippine-time start and end."],
  ["Resource", "A named stock record classified as REUSABLE or CONSUMABLE."],
  ["Requirement", "The quantity of one Resource planned for one Project."],
  ["Allocation", "A whole-batch request, approval, custody/consumption history, and reminder-consent state."],
];

export function AboutPage() {
  return (
    <section className="page-shell about-page" aria-labelledby="about-orgresource-title">
      <PageHeader eyebrow="System · project guide" title="About OrgResource" description="A local academic prototype for planning fictional organization projects and coordinating shared resources." />
      <div className="about-grid">
        <GlassPanel className="about-intro" tilt={false}><p className="hero-kicker">Academic Commons</p><h2>Make shared work easier to see.</h2><p>OrgResource models how one student organization might connect its departments, project plans, actual stock records, requirements, and whole-batch allocation history. This is a prototype and does not claim institutional deployment or measured outcomes.</p>
          <div className="about-links"><Link className="button button-primary" to="/projects">View Projects</Link><Link className="button button-secondary" to="/resources">Open Resources</Link></div>
        </GlassPanel>
        <GlassPanel className="about-panel" tilt={false}><PanelHeading kicker="Domain model" title="Five connected records" />
          <dl className="domain-list">{domainRows.map(([name, description], index) => <div key={name}><dt><span className="domain-index">0{index + 1}</span>{name}</dt><dd>{description}</dd></div>)}</dl>
        </GlassPanel>
        <GlassPanel className="about-panel"><PanelHeading kicker="Whole-batch lifecycle" title="How an Allocation moves" />
          <div className="lifecycle-steps"><div><StatusBadge tone="warning">PENDING</StatusBadge><p>Submitted; no stock is reserved yet.</p></div><div className="lifecycle-arrow" aria-hidden="true">→</div><div><StatusBadge tone="blue">APPROVED</StatusBadge><p>Server checks the Project, Requirement, and stock.</p></div></div>
          <div className="lifecycle-branches"><div><strong>Reusable</strong><p>Issue the batch, then record its full usable return. Issuing does not deduct stock a second time.</p><span className="lifecycle-flow">APPROVED → ISSUED → RETURNED</span></div><div><strong>Consumable</strong><p>Confirm use of the full batch. Consumption permanently reduces the physical total once.</p><span className="lifecycle-flow">APPROVED → CONSUMED</span></div></div>
          <p className="quiet-note">Partial, damaged, and lost-item workflows are not represented. Past approved activity is retained as history.</p>
        </GlassPanel>
        <GlassPanel className="about-panel"><PanelHeading kicker="Inventory meanings" title="How to read quantities" />
          <ul className="about-list"><li><strong>Physical total</strong> is the Resource total returned by the server.</li><li><strong>Available quantity</strong> is the server-calculated amount available under current reservations and custody.</li><li><strong>Requirement coverage and shortage</strong> are calculated and returned by the backend.</li><li>Units are tracked per Resource. The app never combines unlike unit counts into one stock total.</li></ul>
        </GlassPanel>
        <GlassPanel className="about-panel prototype-notice"><PanelHeading kicker="Prototype boundaries" title="Local academic use" />
          <ul className="about-list"><li>Use fictional records only; no real campus data or private student contact information.</li><li>This app has no production authentication, campus integration, or institutional endorsement.</li><li>Reminder mode is disabled by default. A local FakeTwilio response means the simulator accepted a request; it does not mean a handset received a message.</li><li>No adoption, savings, or operational impact has been measured.</li></ul>
        </GlassPanel>
        <GlassPanel className="about-panel"><PanelHeading kicker="Short demo route" title="Explore the workflows" />
          <ol className="demo-steps"><li>Create or choose a Department, then schedule a future Project in Philippine time.</li><li>Add a REUSABLE or CONSUMABLE Resource and set its physical total.</li><li>Connect a Resource Requirement to the Project and observe server coverage/shortage.</li><li>Submit a fictional Allocation, then approve it and follow only the actions shown for its current status.</li><li>Refresh the browser and verify the same server records remain.</li></ol>
          <p className="quiet-note">For local startup and test commands, see frontend/README.md and the repository backend instructions. Keep database credentials outside this app and its Git repository.</p>
        </GlassPanel>
      </div>
    </section>
  );
}
