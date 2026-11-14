# 08 — Complete React/TypeScript UI specification (real functional application)

USER DECISION: Actual working UI is mandatory as a final outcome even though course calls UI a bonus. It cannot replace backend Jersey requirements and should not delay imminent skeleton ZIP/video. No fake dashboard/placeholder database presented as complete. React must use real HTTP/JSON and server-authored stock/coverage.

## Framework architecture

Vite + React + TypeScript (strict mode) with pinned npm lockfile. HashRouter to make deployed routes '/#/projects' possible while backend owns '/api/*'. Dev Vite proxy '/api' -> http://127.0.0.1:8080 (preserve prefix). Bundled production assets into Spring Boot static classpath, Jersey @ApplicationPath("/api") and individual controller registration. Verify real packaged JAR serves '/' and '/api/departments', CSS/JS and refresh. If legacy servlet mapping fails, documented two-process local run fallback until corrected, not fake bundled success.

Suggested frontend dirs: src/api (typed client, mutations), src/types (DTO), src/components (layout, cards, table, modal, form), src/features/dashboard,departments,projects,resources,requirements,allocations,reminders, src/styles, src/tests, e2e.

No separate Node backend, Next.js server, Supabase, Firebase, DB or auth provider. No manual send SMS endpoint; reminder is scheduled Java backend.

## Design system

Professional restrained academic operations app:
- Light neutral background; white cards; primary deep blue/indigo; restrained green success, amber warning, red destructive.
- Accessible sans-serif body 14–16px; visible heading hierarchy; 4/8/12/16/24/32px spacing.
- Consistent border radius ~10–14px, subtle borders, little shadow.
- Desktop 240–260px sidebar; mobile collapsible drawer and no page overflow; responsive grids and table horizontal scroll if necessary.
- Persistent app name "OrgResource", contextual "Local academic prototype" notice, accessible nav and keyboard focus.
- Buttons descriptive, no generic unlabeled icons; non-color status text; WCAG-minded contrast, logical labels/aria where appropriate.
- Loading skeletons only when actually loading; empty state with next action; error with Retry; success after real response; no fabricated sample stats.

## Screen architecture and functions

Dashboard:
- Load GET /departments,/projects,/resources,/requirements,/allocations (parallel where useful).
- Count Departments, Resource TYPES, active Projects, upcoming Projects, pending Allocation REQUESTS, outstanding ISSUED BATCHES, overdue ISSUED BATCHES, Requirements with positive shortage, CONSUMED Allocation records. Do NOT sum 10 cords + 100 sheets as one meaningful "110 resources available" metric.
- Show recent Allocation rows, upcoming/overdue lists, actions linking to real workflows.
- Active Project derived startAt<=now<endAt in Asia/Manila; upcoming start>now, ended end<=now.
- Partial failure must show affected metrics as unavailable, not fake zero/false fresh success. Cross-endpoint reads may not form atomic snapshot; label refresh time and provide refresh.

Departments:
- GET list, create POST, rename POST, delete POST only after user confirms.
- Search/filter locally by display name, stable sort.
- Duplicate normalized name 409, dependency 409, missing ID 404, empty state, responsive form and table.
- Do not remove row until server returns success.

Projects:
- GET list filter departmentId, creation form with Department picker, name, PHT start/end.
- HTML datetime-local input sends YYYY-MM-DDTHH:mm:ss *local wall time* directly, not toISOString converting UTC unexpectedly.
- UI Upcoming/Active/Ended derived labels; not stored enum.
- Edit name and dates; name-only update preserves unchanged old dates; date changes may be 409 if historic approval.
- Project detail view composes Requirements/Allocations data using existing list APIs (no invented GET-by-ID).
- Delete only when no Requirements; explain refusal.

Resources:
- GET list/filter type, create Resource name,type,total; display totalQuantity vs availableQuantity and meaning by REUSABLE/CONSUMABLE.
- Do not display unit column unless deriving from name; no required 'unit' field.
- Update name/total using expectedTotalQuantity set from last server observation; if 409 stale, re-fetch current, explain conflict, force user to review (never automatic overwrite).
- Safe delete total=0 and no Requirements, confirm identity, error feedback.

Requirements:
- GET list/project filter, create with Project and Resource picker; unique pair; show required, allocated, shortage. PENDING not counted.
- Update quantity with server enforcing >= coverage and max individual pending; show explanatory 409, allow cancel pending first.
- Delete only no Allocations; no erasing history.

Allocations:
- List/filter Requirement,status; show Project name and Resource through related read-only maps; borrower label, quantity, status, lifecycle times, consent presence and reminder result.
- Create PENDING with borrower label, quantity and optional fictional test contact/consent. No real student numbers.
- On PENDING approve/cancel and delete if never approved.
- On APPROVED REUSABLE issue/cancel; on APPROVED CONSUMABLE consume/cancel; on ISSUED reusable return; RETURNED/CONSUMED read-only; CANCELLED previously approved read-only; CANCELLED never approved deletable.
- Consent revocation action independent of lifecycle (if consent currently true); POST /revoke-sms-consent clears saved phone; don't show full phone in UI.
- Return confirmation explicitly declares entire batch physically received usable. Consume confirmation clearly states full batch deduction and no undo.
- Post state-changing command must await actual result and re-fetch Allocations, Resources, Requirements, Project when relevant. No optimistic guessed balance.
- HTTP timeout or lost result: show uncertain outcome, re-fetch row/list, don't automatically retry non-idempotent POST.
- Show 409 state/race conflicts accurately, no fabricated success.

Reminders / Activity:
- Use Allocation and Project lists, not invented notification-log API. ISSUED batches due soon (end within 24h), overdue issued; show recorded reminderClaimed/Outcome and consent.
- SIMULATED label "Accepted by local simulator", PROVIDER_ACCEPTED "Provider accepted request", UNKNOWN "Outcome uncertain", not "Delivered".
- No manual Send SMS UI unless new authorized backend contract explicitly agreed.

About/Demo:
- Explain hypothetical org, five entities, stock formulas, limitations: whole-batch returns, no damage/loss, no auth, fictional local data, no campus endorsement or live SMS claim.
- Provide documentation/dev version and accessible usage help, not fake Admin account.

## Shared states every screen must handle

INITIAL_LOADING, EMPTY, READY, VALIDATION_ERROR, HTTP_404, HTTP_409, NETWORK_ERROR, SERVER_ERROR, REFRESHING, MUTATING, SUCCESS_CONFIRMED, STALE_DATA. Retry GET safely; no automatic POST retry. Modal focus trap and restore, keyboard tab order, visible focus, accessible labels, screen-reader status; test at 375px and desktop widths. Do not use disabled action as security; server revalidates.

## Typed API contract

Department {id:number,name:string}.
Project {id,departmentId,name,startAt,endAt} date strings PHT.
Resource {id,name,type:"REUSABLE"|"CONSUMABLE",totalQuantity,availableQuantity}.
ResourceRequirement {id,projectId,resourceId,quantity,allocatedQuantity,shortageQuantity}.
Allocation {id,requirementId,quantity,borrowerName,contactPresent,smsConsent,status,createdAt,approvedAt,issuedAt,returnedAt,consumedAt,cancelledAt,cancellationReason,expiredPendingCleanup,reminderAttempted,reminderOutcome} nullable timestamps and enums. Never phoneNumber in read DTO.
Error {code,message}. Availability {resourceId,availableQuantity}. Delete {message:"Record deleted."}.

HTTP client:
- relative /api paths, JSON Accept/Content-Type, AbortSignal support for GET, parse JSON failures separately, throw typed ApiError(status,code,message) for non-2xx, do not use null "success".
- Prevent two simultaneous mutation submissions from same UI form where possible, still backend is authoritative.
- No localStorage as inventory database, no mock intercepts in final browser E2E.
- Handle 200 [] from valid unmatched list filters vs 404 required FK.

## Visual quality acceptance

UI01 dashboard empty DB shows no fabricated stats.
UI02 Department create persists.
UI03 Project dates round-trip PHT.
UI04 Resource create/list and availability.
UI05 Requirement shortage.
UI06 Allocation PENDING stock unchanged.
UI07 Approve reserves.
UI08 ISSUE no second deduction.
UI09 RETURN releases custody preserves coverage.
UI10 CONSUME once.
UI11 Stale expected total 409.
UI12 Unsafe delete refused with informative message.
UI13 Consent revoke no contact leak.
UI14 browser refresh persistence.
UI15 backend restart persistence.
UI16 backend unreachable shows correct offline/error, not success.
UI17 keyboard-only core workflows.
UI18 narrow viewport all controls usable.
UI19 real backend browser E2E not all mocks.
UI20 final bundled assets work from Java JAR.

## Testing

Vitest and React Testing Library (unit/component), Playwright browser E2E after actual API live. Frontend build, lint, unit tests, E2E. Mock fetch is allowed for isolated component tests but must NOT replace real-backend acceptance. Mobile layout and accessibility manual checks plus automated semantic queries. No actual screen or performance measurement claim until tested.
