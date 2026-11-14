# OrgResource UI contract amendments

**Recorded:** 2026-11-15 (Asia/Manila)

**Applies to:** O8–O10 frontend against the current O6 backend

**Purpose:** reconcile the proposal examples and UI prompt with the implemented API without rewriting historical submissions.

| Topic | Earlier example or request | Current verified contract and UI behavior | Reason / boundary |
| --- | --- | --- | --- |
| Allocation contact privacy | The concise proposal's Allocation JSON includes `phoneNumber`. | Allocation read DTOs omit `phoneNumber`; they expose `contactPresent`, `smsConsent`, and reminder metadata. UI reads never render the raw contact. | Retains privacy while allowing the contact to be withdrawn. No read route or DTO was expanded. |
| Resource stale-write check | Resource update example only describes name and total quantity. | `POST /api/resources/{id}/update` requires `name`, `totalQuantity`, and `expectedTotalQuantity`; UI sends the version observed in the loaded row, then requires an explicit review after `STALE_RESOURCE_QUANTITY`. | Prevents silent overwrites. Current backend DTO/controller is authoritative. |
| Resource availability detail | The Resource list includes `availableQuantity`; the UI also has a dedicated availability route. | The Resource detail sheet reads `GET /api/resources/{id}/availability` and shows loading/error/retry separately from its list snapshot. | Keeps the dedicated availability route visibly server-authoritative without adding a client inventory calculation. |
| Error shape | Proposal examples show message-only failures. | Current failures use JSON `{"code":"...","message":"..."}` with the relevant 400, 404, or 409 status. UI maps known domain codes to plain-language guidance and re-reads after conflicts. | Mirrors the current backend and preserves machine-readable codes. |
| Reminder Activity relationship | The O8–O10 screen request says to read only Allocations, Projects, and Resources. | The current Allocation DTO has `requirementId`, not `projectId`; the UI also reads `GET /api/requirements` to resolve `Allocation.requirementId → Requirement.projectId → Project.endAt`. | This fourth existing-list read is required to show real due/overdue periods. No route, entity, or client-side data relationship was invented. |
| Consent test number | The screen request asks for a fictional E.164-like contact. | When consent is selected, the UI accepts only the reserved fictional NANP range `+12025550100` through `+12025550199`; the value is never shown in read views. | Prevents a local academic demo form from accepting an ordinary personal number. Backend reminders remain disabled by default in the test setup; no real SMS is sent. |
| Project time input | Proposal/request dates are Philippine wall times. | `datetime-local` values are sent as `YYYY-MM-DDTHH:mm:ss` with no UTC conversion; display, active-window labels, and reminder windows use `Asia/Manila`. | Preserves the entered local time and current backend `LocalDateTime` contract. |

## Unresolved course question

The syllabus associates CLO9 with receiving and serving images over HTTP, while the current project route contract and proposal do not define image endpoints. This UI phase does not add image storage, a sixth entity, an endpoint, or a claim of CLO9 completion. Canvas verification and instructor clarification remain outstanding; no approval is implied here.
