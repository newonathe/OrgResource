# OrgResource React frontend (O7–O10)

The React, TypeScript, and Vite application is a local same-origin client for the existing Java API. Its eight routes provide the project workspace:

| Route | Screen | Main work |
| --- | --- | --- |
| `#/` | Overview | Server-derived project, requirement, stock, and allocation snapshot |
| `#/allocations` | Allocations | Request, approve, issue, return, consume, cancel, delete, and withdraw reminder consent |
| `#/resources` | Resource Library | Create, list, edit with stale-total review, inspect server availability, and guarded delete |
| `#/projects` | Projects | Create, filter, inspect history, rename, and guarded delete |
| `#/requirements` | Requirements | Create, view server coverage/shortage, update quantity, and guarded delete |
| `#/departments` | Departments | Create, list/search, rename, and guarded delete |
| `#/reminders` | Reminder Activity | Read upcoming/overdue Projects and server-recorded reminder outcomes |
| `#/about` | About | Explain the five records, whole-batch lifecycle, and prototype boundaries |

Business records always come from the API. Only the light/dark theme and motion preference are stored in browser `localStorage`. The UI does not claim production authentication, institutional integration, delivered SMS, or CLO9 image handling. The SMS consent input accepts only the reserved fictional range `+12025550100`–`+12025550199`; the allocation read DTO never exposes the saved phone number.

## Local development

Use Node.js 20.19+ and the same locally configured Java/MySQL backend described in [`../backend/README.md`](../backend/README.md). From this folder:

```powershell
npm ci
npm run dev
```

Vite binds to `http://127.0.0.1:5173` and proxies `/api` to `http://127.0.0.1:8080`. The backend remains the authority for validation, lifecycle transitions, coverage, stock availability, and persistence.

## Checks

```powershell
npm run typecheck
npm test
npm run lint
npm run build
npm run e2e
```

The Playwright suite runs headless Chromium from the command line. Its connected workflow writes uniquely named fictional data through the real API. Run it only while the backend is configured for the isolated `orgresource_test` schema; never point it at the regular `orgresource` database. The suite does not reset schemas. It removes only unreferenced temporary records; Allocation history intentionally persists according to backend deletion rules. If a run is interrupted, inspect only rows beginning with that run's unique `O8...` fixture prefix in `orgresource_test`.

The visual matrix saves full-page light/dark desktop and mobile captures, one tablet allocation view, record detail and confirmation views, and representative loading/empty/error states under [`../evidence/O8-O10/screenshots/`](../evidence/O8-O10/screenshots/). The compact JPEG contact sheets are the review artifacts; raw Playwright captures are generated locally and are not part of the deliverable.
