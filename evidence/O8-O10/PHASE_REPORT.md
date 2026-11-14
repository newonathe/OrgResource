# OrgResource O8–O10 phase report — Academic Glass Commons

Captured: 2026-11-15 (Asia/Manila)

Branch: `feat/o8-o10-academic-glass-ui-2026-11-15`

Base: `83b02ba01b367b1b6df537aa1652d93922faed48` (`feat/o6-o7-reminders-react-2026-11-15`)

## Delivered

Completed the React O8–O10 UI as eight real routed pages: Overview, Allocations, Resource Library, Projects, Requirements, Departments, Reminders Activity, and About. The screens use the existing Java API and the five-entity domain. They do not keep authoritative records in browser storage or calculate stock independently.

The UI provides Department, Project, Resource and Requirement CRUD; Allocation request, review, custody/consumption and consent-revocation workflows; resource availability details; filters and refresh; server-confirmed success and conflict states; deletion confirmation; and server-backed overview and reminder activity. Reminder dates are derived from issued allocations and Project periods; no manual SMS action or invented reminder endpoint was added. Public allocation data does not display a phone number. Reminder mode remains simulator-only per the existing backend configuration.

The typed API client covers all 26 current operations: the original 25-route contract plus the separately tracked A09 consent-revocation route. The connected browser flow exercises the CRUD and allocation lifecycle operations against the running backend; non-mutating list/filter calls run as part of those UI screens.

The Resources detail view now fetches `GET /api/resources/{id}/availability` so the detail sheet displays the current server result instead of a potentially stale list snapshot. Its request is tied to the selected Resource and safely refreshed when selection changes.

## Visual and interaction refinement

The visual system is named **Academic Glass Commons**: a cool academic blue, book-gold and paper palette; restrained architectural line art; Georgia display headings; and a system sans-serif for navigation, forms and records. Light-mode text and separator colors were darkened/strengthened, and small labels/metadata were enlarged. Frosted panels, translucent layered surfaces and blurred backgrounds carry the glass treatment without reducing form readability.

Mouse feedback includes a cursor-following highlight on glass surfaces, restrained card tilt, subtle navigation indicators and primary-action movement. A visible Motion setting controls these effects. The interface honors `prefers-reduced-motion`, provides a solid-surface `prefers-reduced-transparency` fallback, and strengthens colors/borders for `prefers-contrast: more`. Keyboard focus, skip navigation, dialog focus management, labels and narrow-screen navigation remain supported.

Review references: the third-party [apple-design-skill](https://github.com/dickwu/apple-design-skill), Apple [design principles](https://developer.apple.com/design/human-interface-guidelines/design-principles/) and [readability guidance](https://developer.apple.com/design/tips/), and W3C [WCAG 2.2 contrast minimum](https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum). The GitHub skill was treated as a design-review checklist, not as official Apple material or an app dependency. Light-token calculations are documented in `docs/design/ACADEMIC_GLASS_COMMONS_V1.md`; those calculations are not a formal certification of every rendered text/background pair.

## Verification

| Area | Command / observation | Result |
| --- | --- | --- |
| TypeScript | `npm run typecheck` from `frontend/` | PASS |
| Lint | `npm run lint` from `frontend/` | PASS |
| Unit tests | `npm test` from `frontend/` | PASS — 2 files, 8 tests |
| Production frontend | `npm run build` from `frontend/` | PASS — Vite production build |
| Browser and backend | `npm run e2e` from `frontend/` | PASS — 3 Playwright tests, including connected Java/MySQL workflows, persistence after reload, CRUD/integrity and Allocation lifecycle/privacy checks |
| Responsive visual review | Same Playwright run at 1440px desktop, 768px tablet and 390px mobile, light and dark | PASS — all eight pages captured; no horizontal viewport overflow; detail, loading, empty, unavailable and confirmation states captured |
| Pointer behavior | Dedicated Playwright test with reduced motion disabled, then app Motion switched off | PASS — cursor variables and hover feedback responded; Motion-off prevented subsequent magnetic movement |
| Backend runtime | Java 21.0.3 local test-profile service; GET `/api/departments`, `/projects`, `/resources`, `/requirements`, `/allocations` | PASS — all returned HTTP 200 during preflight; browser tests exercised persisted fictional data against isolated MySQL |
| Java compatibility reference | Existing O6–O7 report and PR CI | PASS in prior gate — Java 8 + MySQL, 55 tests; backend source was unchanged in this frontend batch |
| Dependency audit | `npm audit --omit=dev` | PASS — zero production vulnerabilities |
| Full npm audit | `npm audit --json` | FINDING — 7 moderate development-only advisories in the TypeScript ESLint/minimatch dependency chain; zero high/critical and zero production findings. No dependency upgrade was made in this UI-only batch. |

The backend used here runs locally on Java 21.0.3; this batch does not claim a local Java 8 build. Java 8 compatibility remains covered by the previously recorded backend CI gate.

Playwright emitted a harmless Node environment warning that `NO_COLOR` is ignored when `FORCE_COLOR` is set. The final full run passed 3/3 tests. Fixed dialog captures use viewport screenshots to avoid full-page Chromium stitching artifacts. The committed evidence pack contains six compressed JPEG sheets under `evidence/O8-O10/screenshots/`; raw PNG intermediates are disposable test output.

## Boundaries and remaining gates

- CLO9 image receiving/serving remains **UNRESOLVED**: the syllabus mentions the outcome, while the current proposal/API scope does not define image behavior. No image storage, route or sixth entity was added.
- This is a fictional-data local academic prototype, not a hosted, authenticated or institutionally approved service. The local reminder simulator accepting a request is not proof that a handset received an SMS.
- Canvas submission, deployment and merge were not performed.
- O11 system acceptance, O12 conditional image handling, O13 packaging and O14 academic finalization remain future stages.

## Changed areas

- `frontend/src/App.tsx`, `frontend/src/shell/`, `frontend/src/ui.tsx`: routed academic shell, responsive navigation, theme and motion controls, reusable glass/dialog/form states.
- `frontend/src/pages/`: functional dashboard, CRUD, allocation lifecycle, reminder activity and project guide pages.
- `frontend/src/api/client.ts`, `frontend/src/domain.ts`: typed API boundary and client-side display/date/decision helpers; the server remains authoritative.
- `frontend/src/styles.css`: Academic Glass Commons palette, typography, layout, accessibility fallbacks and pointer feedback.
- `frontend/e2e/orgresource.spec.ts`, `frontend/src/*.test.tsx`, `frontend/src/domain.test.ts`: unit and live browser verification.
- `frontend/README.md`, `docs/design/`, `evidence/O8-O10/`: run instructions, UI contract notes and verification evidence.
- `.gitignore`: keeps full-resolution Playwright screenshot intermediates local while retaining compressed review sheets.
