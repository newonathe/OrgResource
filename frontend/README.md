# OrgResource web interface (O7)

The React/TypeScript/Vite app is a local same-origin client for the Java API. It currently implements real Department and Resource list, create, update, and delete workflows. DTOs for the other API areas are defined in the shared typed client for later UI phases; those screens are not presented as complete.

## Local development

Use Node.js 24 or another version supported by Vite 8. From this folder:

```powershell
npm ci
npm run dev
```

The Java API must be running at `http://127.0.0.1:8080`. Vite binds to `127.0.0.1:5173` and proxies `/api` to that backend. The browser does not hold inventory data or use fabricated records.

## Checks

```powershell
npm run typecheck
npm test
npm run lint
npm run build
npm run e2e
```

The Playwright test uses the real API and writes fictional fixtures. Run it only against the isolated disposable `orgresource_test` database/profile. On success it removes the Department and Resource fixtures it creates. If interrupted, inspect and clean only its unique `E2E <timestamp>` fixtures in that test schema.

The E2E runner uses headless Playwright Chromium from the command line and does not open an in-app browser window.
