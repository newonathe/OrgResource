# OrgResource Extended C frontend

This workspace bundles into the Java 21 Extended C JAR. The JAR serves the authenticated React UI and Jersey API from the same origin. Course Final A has its own Java 8 JAR and React workspace at the repository root.

- Workspace screens cover Overview, Departments, Projects, Resource Library, Requirements, Allocations, Reminder Activity, and About.
- Project work exposes current assigned-Lead and governance actions. My borrowed resources shows the signed-in borrower's linked requests and their current reminder consent. Withdrawal is available only for active consent.
- The workspace waits for the server to classify the environment. A dedicated synthetic demo displays a simulation notice. On a classification error the app holds the workspace closed and offers a retry.
- Session and governance screens cover sign in, workspace selection, account recovery and management, member roles, invitations, and ownership transfer.
- Resource details include image upload, preview and versioned removal, stock receive and correction, a bounded movement view, and permanent or temporary operation receipt lookup. Server roles determine which actions appear. The server checks authorization again.
- CSRF values and displayed one-time credentials live in memory. Browser local storage holds theme and motion preferences.

## Build and verify

Use Node 24, Java 21, MySQL 8.0.46, and command-line Playwright Chromium. To install Chromium, run `npx playwright install chromium` in this directory after `npm ci`. From the repository root, run `.\scripts\verify-extended-c1.ps1`. It creates a disposable loopback MySQL instance, builds the packaged app, runs MySQL, Newman, and browser tests with fictional fixtures, and shuts down the test processes.

For a frontend-only check, run `npm ci`, `npm run typecheck`, `npm test`, `npm run lint`, and `npm run build` here. Vite development uses port 5173 and proxies API calls to Extended C on port 8085, or the loopback port in EXT_VITE_BACKEND_PORT. The full acceptance suite requires the packaged JAR and the runner's seeded organizations and credentials. Run browser acceptance through the guarded runner.

See the [Extended C release guide](../../docs/EXTENDED_C_RELEASE.md) for the package, first-run setup, and restore drill.
