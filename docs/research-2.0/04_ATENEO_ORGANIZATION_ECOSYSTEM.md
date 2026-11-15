# Ateneo student-organization landscape and institutional boundaries
**Status:** EVIDENCE VERIFIED public descriptions; institutional role interpretations **PROVISIONAL**. **Date:** 2026-11-15. Sources A01–A07, C01.

## Publicly described categories — avoid false hierarchy
A01 OSA describes registered organizations, accredited/unaccredited organizations, Sanggunian and affiliates, and separate student publications; its embedded statistical breakdown is dated **AY 2019–2020**, so it is *not* a reliable 2026 population count. A05 RecWeek 2026 calls COA-M the confederation of fifty-five accredited organizations and presents LIONS as the league of independent organizations; these are that site's published descriptions, **not** independent validation that every group belongs to precisely one branch or that count remains current beyond publication. “Student-led,” “registered,” “accredited,” “independent,” and “affiliated” describe different dimensions, not a single mutually exclusive enumeration. University recognition, accreditation and official resource authority cannot be inferred from an email domain or a self-entered org label.

**Design implication:** Model an organization's public display name and optional claimed affiliation as text/verified-status metadata with source+date, not an authoritative Ateneo registry. Domain user roles are local permission roles, not official COA/OSA ranks. Allow membership in several org workspaces; forbid cross-workspace data by default.

## Official workflows and integrations that OrgResource must NOT impersonate
- A02/A03: OSA publishes a Project Proposal Form process, endorsements from organization president and moderator, and its own document-submission/status portal. OrgResource's internal "Project" is an **internal planning item**, not an OSA-approved project, and must never show "Ateneo approved" merely because its local allocation is APPROVED.
- A04: CFMO runs a venue/facilities/equipment reservation portal and publishes requirements/lead times. OrgResource must exclude university-owned/CFMO-controlled reservations unless a later authorized real integration and ownership contract exists. No scraping authenticated forms.
- A06: The GUIDON reported separate budgeting of comparable Sanggunian materials in 2025; this supports a research lead about coordination **not proof of resource shortfalls, inventory tools absent, or adoption willingness**.
- A07: MEA publicly describes inventory tracking/physical audit; therefore claim "Ateneo orgs have no inventory software" would be false/unsupported.

## Hypothetical user archetypes, not actual Ateneo interviews
A custodian owns an org-managed equipment record, a project coordinator requests a future booking, a member checks out items, a separate workspace uses its own stock, and outgoing leadership transfers administration. These are **synthetic** scenarios; counts, losses, demand, delivery schedules and staffing arrangements are test fixtures only.

## Authority, rights and later validation
OrgResource cannot validate officer titles, membership, university association, departmental allocations, procurement authority, minors or inventory ownership from public listings alone. Initial public demo explicitly states "independent fictional software demo; not affiliated with Ateneo." A future real-organization pilot requires authorized representatives who control records and approve privacy/operational policies; possible OSA/CFMO/IT involvement depends on actual integration and policy, not an automatic assumption.

## Specific evidence gaps
Current authenticated OSA/CFMO workflows, actual roles and turnover policy, asset custody practices, account ownership authority, legal registration status, live project form deadlines, legitimate demand, ability to integrate, and real software use are **unknown**. No outreach now per user. Future pilot gate defines the questions; it is optional.
