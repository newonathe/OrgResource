import { createHash } from "node:crypto";
import { readFileSync, mkdirSync } from "node:fs";
import { resolve } from "node:path";
import { expect, test, type APIRequest, type Page } from "@playwright/test";

const screenshotDir = resolve(process.env.ORGRESOURCE_E2E_SCREENSHOT_DIR ?? "../../evidence/C4/screenshots");
mkdirSync(screenshotDir, { recursive: true });

async function signIn(page: Page) {
  const username = process.env.EXT_E2E_USERNAME;
  const password = process.env.EXT_E2E_PASSWORD;
  if (!username || !password) throw new Error("Set EXT_E2E_USERNAME and EXT_E2E_PASSWORD for authenticated C4 acceptance.");
  await page.goto("/#/login");
  await expect(page.getByRole("heading", { name: "Welcome back", level: 1 })).toBeVisible();
  await page.getByLabel("Username", { exact: true }).fill(username);
  await page.getByLabel("Password", { exact: true }).fill(password);
  await page.getByRole("button", { name: "Sign in", exact: true }).click();
  const picker = page.getByRole("heading", { name: /Choose an organization/ });
  await expect.poll(async () => (await picker.count()) + (await page.getByRole("heading", { name: "Overview", level: 1 }).count())).toBe(1);
  if (await picker.count()) {
    const primaryWorkspace = page.locator(".organization-choice").filter({ hasText: "Fictional C1 Workspace" });
    const chosenWorkspace = await primaryWorkspace.count() ? primaryWorkspace : page.locator(".organization-choice").first();
    await chosenWorkspace.getByRole("button", { name: "Open workspace", exact: true }).click();
  }
  await expect(page.getByRole("heading", { name: "Overview", level: 1 })).toBeVisible();
}

test.beforeEach(async ({ page }) => signIn(page));

function manilaWallTime(offsetMs: number): string {
  const date = new Date(Date.now() + offsetMs);
  const parts = Object.fromEntries(new Intl.DateTimeFormat("en-CA", {
    timeZone: "Asia/Manila", year: "numeric", month: "2-digit", day: "2-digit",
    hour: "2-digit", minute: "2-digit", second: "2-digit", hourCycle: "h23",
  }).formatToParts(date).map((part) => [part.type, part.value]));
  return `${parts.year}-${parts.month}-${parts.day}T${parts.hour}:${parts.minute}:${parts.second}`;
}

async function confirm(page: Page, actionButtonName: RegExp, confirmationName: string) {
  const dialog = page.getByRole("dialog");
  const confirmation = page.getByRole("button", { name: confirmationName, exact: true });
  if (!(await confirmation.isVisible())) {
    await expect(dialog).toHaveCount(0);
    await page.getByRole("button", { name: actionButtonName }).click();
  }
  await expect(dialog).toBeVisible();
  const dialogConfirmation = dialog.getByRole("button", { name: confirmationName, exact: true });
  await expect(dialogConfirmation).toBeVisible();
  await dialogConfirmation.click();
  await expect(dialog).toHaveCount(0);
}

async function getJson<T>(page: Page, route: string): Promise<T> {
  const response = await page.request.get(route);
  expect(response.ok(), `${route} returned ${response.status()}`).toBeTruthy();
  const payload = await response.json() as unknown;
  return payload && typeof payload === "object" && !Array.isArray(payload) && "data" in payload
    ? (payload as { data: T }).data : payload as T;
}

async function postJson(page: Page, route: string, body: unknown) {
  const csrfResponse = await page.request.get("/api/v2/auth/csrf");
  expect(csrfResponse.ok()).toBeTruthy();
  const csrfPayload = await csrfResponse.json() as { data?: { csrfToken?: string }; csrfToken?: string };
  const csrfToken = csrfPayload.data?.csrfToken ?? csrfPayload.csrfToken;
  expect(csrfToken).toBeTruthy();
  return page.request.post(route, { data: body, headers: { "X-CSRF-TOKEN": csrfToken!, Origin: new URL(page.url()).origin } });
}

async function inviteFictionalAccount(page: Page, requests: APIRequest, orgPath: string, role: "MEMBER" | "LEAD",
  username: string, displayName: string) {
  const invited = await postJson(page, orgPath + "/invitations", { role });
  expect(invited.status(), "create a fictional invitation").toBe(201);
  const { data } = await invited.json() as { data: { bearerToken: string } };
  const origin = new URL(page.url()).origin;
  const isolated = await requests.newContext({ baseURL: origin });
  try {
    const csrf = await isolated.get("/api/v2/auth/csrf");
    expect(csrf.status()).toBe(200);
    const { data: { csrfToken } } = await csrf.json() as { data: { csrfToken: string } };
    const redeemed = await isolated.post("/api/v2/invitations/redeem", {
      data: { bearerToken: data.bearerToken, username, displayName, password: username + "-Local!25" },
      headers: { "X-CSRF-TOKEN": csrfToken, Origin: origin },
    });
    expect(redeemed.status(), "redeem a fictional invitation").toBe(201);
    return await isolated.storageState();
  } finally {
    await isolated.dispose();
  }
}

test("a lost create confirmation keeps its draft, refreshes the saved row, and never retries automatically", async ({ page }) => {
  const name = `O11 uncertain ${Date.now().toString(36)}`;
  let postCount = 0;
  await page.route("**/api/departments", async (route) => {
    if (route.request().method() !== "POST") return route.continue();
    postCount += 1;
    const committedResponse = await route.fetch();
    expect(committedResponse.ok()).toBeTruthy();
    await route.fulfill({ status: committedResponse.status(), contentType: "application/json", body: "not-json" });
  });

  await page.goto("/#/departments");
  await page.getByLabel("Department name", { exact: true }).fill(name);
  await page.getByRole("button", { name: "Add department" }).click();
  await expect(page.getByText("Change outcome uncertain.")).toBeVisible();
  await expect(page.getByRole("button", { name: `Edit ${name}` })).toBeVisible();
  await expect(page.getByLabel("Department name", { exact: true })).toHaveValue(name);
  await expect(page.getByRole("button", { name: "Add department" })).toBeDisabled();
  await expect(page.getByRole("button", { name: "I reviewed the current records" })).toBeEnabled();
  expect(postCount).toBe(1);
  await page.getByRole("button", { name: "I reviewed the current records" }).click();
  await expect(page.getByRole("button", { name: "Add department" })).toBeEnabled();
  expect(postCount).toBe(1);
});

test("refresh recovers automatically after temporary classification endpoint failures", async ({ page }) => {
  const metadataPath = "**/api/v2/demo/metadata";
  let calls = 0;
  await page.route(metadataPath, async route => {
    calls += 1;
    if (calls <= 2) await route.fulfill({ status: 503, json: { code: "STARTING_UP" } });
    else await route.continue();
  });
  try {
    await page.reload();
    await expect(page.getByRole("heading", { name: "Overview", level: 1 })).toBeVisible({ timeout: 30_000 });
    expect(calls).toBeGreaterThanOrEqual(3);
    await expect(page.getByRole("alert")).toHaveCount(0);
  } finally {
    await page.unroute(metadataPath);
  }
});

test("a packaged workspace withholds activity until provenance is verified", async ({ page }) => {
  const metadataPath = "**/api/v2/demo/metadata";
  await page.route(metadataPath, route => route.fulfill({ status: 200, json: {
    data: { synthetic: true, datasetId: null, demonstrationAsOf: null, notice: "" },
  } }));
  await page.reload();
  await expect(page.getByRole("alert")).toContainText("The server could not confirm the workspace classification");
  await expect(page.getByRole("navigation", { name: "Main navigation" })).toHaveCount(0);
  await page.unroute(metadataPath);
  await page.getByRole("button", { name: "Retry classification" }).click();
  await expect(page.getByRole("heading", { name: "Overview", level: 1 })).toBeVisible();
  await expect(page.getByText(/^DEMO ENVIRONMENT/)).toHaveCount(0);
});

test("Allocation status filters follow browser back and forward navigation", async ({ page }) => {
  await page.goto("/#/allocations");
  const status = page.getByLabel("Status", { exact: true });
  await expect(status).toHaveValue("ALL");
  await status.selectOption("PENDING");
  await expect(status).toHaveValue("PENDING");
  await expect.poll(() => page.evaluate(() => window.location.hash)).toContain("status=PENDING");
  await status.selectOption("APPROVED");
  await expect(status).toHaveValue("APPROVED");
  await page.goBack();
  await expect(status).toHaveValue("PENDING");
  await page.goForward();
  await expect(status).toHaveValue("APPROVED");
});

for (const role of ["MEMBER", "LEAD", "ADMIN"] as const) {
test(`connected invitation grants only ${role} Project and inventory access`, async ({ page, browser }) => {
  test.setTimeout(90_000);
  await page.goto("/#/governance");
  await expect(page.getByRole("heading", { name: "Members and access", level: 1 })).toBeVisible();
  await page.locator(".governance-grid > .form-panel").first().locator("select").selectOption(role);
  await page.getByRole("button", { name: "Create invitation", exact: true }).click();
  await expect(page.getByText("Invitation created.")).toBeVisible();
  const bearerToken = (await page.locator(".token-reveal code").textContent())?.trim();
  expect(bearerToken).toBeTruthy();

  const context = await browser.newContext();
  try {
    const memberPage = await context.newPage();
    const username = `c4-member-${Date.now().toString(36)}`;
    const password = `C4-Member-${Date.now().toString(36)}-Secure!`;
    const csrfInitialized = memberPage.waitForResponse(response => new URL(response.url()).pathname === "/api/v2/auth/csrf" && response.ok());
    await memberPage.goto("/?token=fictional-query-placeholder#/invite?token=fictional-hash-placeholder");
    await expect(memberPage).toHaveURL(/\/#\/invite$/);
    await expect(memberPage.getByLabel("Invitation token", { exact: true })).toHaveValue("");
    await csrfInitialized;
    await memberPage.getByLabel("Invitation token", { exact: true }).fill(bearerToken!);
    await memberPage.getByLabel("New username", { exact: true }).fill(username);
    await memberPage.getByLabel("Display name", { exact: true }).fill("C4 Fictional Member");
    await memberPage.getByLabel("Password", { exact: true }).fill(password);
    await memberPage.getByRole("button", { name: "Redeem invitation", exact: true }).click();
    await expect(memberPage.getByRole("heading", { name: "Recovery codes", level: 2 })).toBeVisible();
    const storageKeys = await memberPage.evaluate(() => Object.keys(localStorage));
    expect(storageKeys.some(key => /token|password|session|csrf/i.test(key))).toBe(false);
    const storageValues = await memberPage.evaluate(() => [...Object.values(sessionStorage), ...Object.values(localStorage)]);
    expect(storageValues.some(value => value.includes(bearerToken!))).toBe(false);

    await memberPage.goto("/#/resources");
    await expect(memberPage.getByRole("heading", { name: /Choose an organization/ })).toBeVisible();
    await memberPage.getByRole("button", { name: "Open workspace", exact: true }).click();
    await expect(memberPage.getByRole("heading", { name: "Resource Library", level: 1 })).toBeVisible();
    await expect(memberPage.getByRole("link", { name: "Allocations", exact: true })).toHaveCount(role === "ADMIN" ? 1 : 0);
    await expect(memberPage.getByRole("link", { name: "Members and access", exact: true })).toHaveCount(role === "ADMIN" ? 1 : 0);
    await expect(memberPage.getByRole("link", { name: "Activity", exact: true })).toHaveCount(role === "ADMIN" ? 1 : 0);
    await expect(memberPage.getByRole("link", { name: "Exports", exact: true })).toHaveCount(role === "ADMIN" ? 1 : 0);
    await expect(memberPage.getByText("Inventory access", { exact: true })).toBeVisible();

    const denied = await postJson(memberPage, "/api/resources", { name: `Denied ${username}`, type: "REUSABLE", totalQuantity: 0 });
    expect(denied.status()).toBe(403);
    const requirements = await getJson<Array<{ projectId: number }>>(memberPage, "/api/requirements");
    const projects = await getJson<Array<{ id: number; name: string }>>(memberPage, "/api/projects");
    const withRequirement = projects.find(project => requirements.some(row => row.projectId === project.id));
    expect(withRequirement).toBeDefined();
    const historyReads: URL[] = [];
    memberPage.on("request", request => {
      if (request.method() !== "GET") return;
      const url = new URL(request.url());
      if (url.pathname === "/api/allocations" || url.pathname === "/api/v2/workspace/allocations") historyReads.push(url);
    });
    await memberPage.goto("/#/projects");
    await memberPage.getByRole("button", { name: `View ${withRequirement!.name}`, exact: true }).click();
    const dialog = memberPage.getByRole("dialog");
    await expect(dialog.getByRole("heading", { name: role === "ADMIN" ? "Requirements and allocation history" : "Requirements", exact: true })).toBeVisible();
    await expect(dialog.getByText(/required · .* covered · .* shortage/).first()).toBeVisible();
    expect(historyReads).toHaveLength(role === "ADMIN" ? 1 : 0);
    if (role === "ADMIN") {
      expect(historyReads[0].pathname).toBe("/api/v2/workspace/allocations");
      expect(historyReads[0].searchParams.get("projectId")).toBe(String(withRequirement!.id));
      await expect(dialog.locator(".sheet-batches").first()).toBeVisible();
    }
    await memberPage.goto("/#/resources");

    const savedSession = await getJson<{ activeOrgId: string | null }>(memberPage, "/api/v2/session");
    expect(savedSession.activeOrgId).toBeTruthy();
    await memberPage.goto("/#/invite");
    await expect(memberPage.getByRole("heading", { name: "Redeem invitation" })).toBeVisible();
    await expect(memberPage.getByRole("heading", { name: "Recovery codes" })).toHaveCount(0);
    await memberPage.goto("/#/resources");
    await memberPage.reload();
    await expect(memberPage.getByRole("heading", { name: "Resource Library", level: 1 })).toBeVisible();
  } finally {
    await context.close();
  }
});

}

test("switching organizations hides old tenant data, cancels stale reads, and denies an unmember cross-tenant lookup", async ({ page, browser }) => {
  test.setTimeout(60_000);
  const session = await getJson<{ activeOrgId: string | null }>(page, "/api/v2/session");
  const organizations = await getJson<Array<{ id: string; displayName: string }>>(page, "/api/v2/orgs");
  expect(organizations.length).toBeGreaterThanOrEqual(2);
  expect(session.activeOrgId).toBeTruthy();
  const originalOrg = organizations.find(org => org.id === session.activeOrgId)!;
  const otherOrg = organizations.find(org => org.id !== session.activeOrgId)!;

  const marker = `C4 tenant isolation ${Date.now().toString(36)}`;
  await page.goto("/#/resources");
  await page.getByLabel("Resource name", { exact: true }).fill(marker);
  await page.getByLabel("Resource type", { exact: true }).selectOption("REUSABLE");
  await page.getByLabel("Initial total quantity", { exact: true }).fill("0");
  await page.getByRole("button", { name: "Add resource", exact: true }).click();
  await expect(page.getByText("Resource saved.")).toBeVisible();
  const originalOrgResources = await getJson<{ items: Array<{ id: string; name: string }> }>(
    page, `/api/v2/orgs/${encodeURIComponent(originalOrg.id)}/resources?limit=100`,
  );
  const tenantResource = originalOrgResources.items.find(resource => resource.name === marker);
  expect(tenantResource).toBeDefined();

  let delayedReadStarted = false;
  let releaseDelayedRead!: () => void;
  const delayedRead = new Promise<void>(resolve => { releaseDelayedRead = resolve; });
  const staleRequestFailed = page.waitForEvent("requestfailed", request =>
    new URL(request.url()).pathname === "/api/v2/workspace/resources");
  await page.route("**/api/v2/workspace/resources*", async route => {
    if (!delayedReadStarted && route.request().method() === "GET") {
      delayedReadStarted = true;
      await delayedRead;
      try { await route.continue(); } catch { /* The organization switch should abort this old-tenant request. */ }
      return;
    }
    await route.continue();
  });
  await page.reload();
  await expect.poll(() => delayedReadStarted).toBeTruthy();
  const switchResponse = page.waitForResponse(response =>
    response.request().method() === "POST" && new URL(response.url()).pathname === "/api/v2/session/active-org");
  await page.getByLabel("Switch organization", { exact: true }).selectOption(otherOrg.id);
  expect((await switchResponse).status()).toBe(200);
  releaseDelayedRead();
  const failedRequest = await staleRequestFailed;
  expect(failedRequest.failure()?.errorText ?? "").toMatch(/abort|cancel/i);
  await expect(page.getByRole("heading", { name: "Resource Library", level: 1 })).toBeVisible();
  await expect(page.getByText(marker, { exact: true })).toHaveCount(0);

  const otherOrgResources = await getJson<{ items: Array<{ id: string; name: string }> }>(
    page, `/api/v2/orgs/${encodeURIComponent(otherOrg.id)}/resources?limit=100`,
  );
  expect(otherOrgResources.items.some(resource => resource.name === marker)).toBe(false);

  await page.goto("/#/governance");
  await expect(page.getByRole("heading", { name: "Members and access", level: 1 })).toBeVisible();
  await page.locator(".governance-grid > .form-panel").first().locator("select").selectOption("MEMBER");
  await page.getByRole("button", { name: "Create invitation", exact: true }).click();
  await expect(page.getByText("Invitation created.")).toBeVisible();
  const bearerToken = (await page.locator(".token-reveal code").textContent())?.trim();
  expect(bearerToken).toBeTruthy();

  const context = await browser.newContext();
  try {
    const memberPage = await context.newPage();
    const username = `c4-secondary-member-${Date.now().toString(36)}`;
    const password = `C4-Secondary-${Date.now().toString(36)}-Secure!`;
    const csrfInitialized = memberPage.waitForResponse(response => new URL(response.url()).pathname === "/api/v2/auth/csrf" && response.ok());
    await memberPage.goto("/?token=fictional-query-placeholder#/invite?token=fictional-hash-placeholder");
    await expect(memberPage).toHaveURL(/\/#\/invite$/);
    await expect(memberPage.getByLabel("Invitation token", { exact: true })).toHaveValue("");
    await csrfInitialized;
    await memberPage.getByLabel("Invitation token", { exact: true }).fill(bearerToken!);
    await memberPage.getByLabel("New username", { exact: true }).fill(username);
    await memberPage.getByLabel("Display name", { exact: true }).fill("C4 Secondary Tenant Member");
    await memberPage.getByLabel("Password", { exact: true }).fill(password);
    await memberPage.getByRole("button", { name: "Redeem invitation", exact: true }).click();
    await expect(memberPage.getByRole("heading", { name: "Recovery codes", level: 2 })).toBeVisible();
    await memberPage.goto("/#/resources");
    await expect(memberPage.getByRole("heading", { name: /Choose an organization/ })).toBeVisible();
    await expect(memberPage.locator(".organization-choice")).toHaveCount(1);
    await expect(memberPage.locator(".organization-choice")).toContainText("C4 Tenant Isolation");
    await memberPage.getByRole("button", { name: "Open workspace", exact: true }).click();
    await expect(memberPage.getByRole("heading", { name: "Resource Library", level: 1 })).toBeVisible();
    await expect(memberPage.getByText(marker, { exact: true })).toHaveCount(0);
    const crossTenant = await memberPage.request.get(
      `/api/v2/orgs/${encodeURIComponent(originalOrg.id)}/resources/${encodeURIComponent(tenantResource!.id)}`,
    );
    expect(crossTenant.status()).toBe(404);
    const memberOrganizations = await getJson<Array<{ id: string }>>(memberPage, "/api/v2/orgs");
    expect(memberOrganizations).toHaveLength(1);
    expect(memberOrganizations[0].id).toBe(otherOrg.id);
  } finally {
    await context.close();
  }

  const switchBackResponse = page.waitForResponse(response =>
    response.request().method() === "POST" && new URL(response.url()).pathname === "/api/v2/session/active-org");
  await page.setViewportSize({ width: 360, height: 844 });
  await page.getByRole("button", { name: "More", exact: true }).click();
  await expect(page.getByRole("button", { name: "Sign out", exact: true })).toBeVisible();
  await page.getByRole("combobox", { name: "Switch organization", exact: true }).selectOption(originalOrg.id);
  expect((await switchBackResponse).status()).toBe(200);
  await page.goto("/#/resources");
  await expect(page.getByText(marker, { exact: true })).toBeVisible();
});

test("connected inventory UI recovers an uncertain receipt once and reloads its saved image", async ({ page }) => {
  test.setTimeout(120_000);
  const resourceName = `C4 receipt ${Date.now().toString(36)}`;
  await page.goto("/#/resources");
  await page.getByLabel("Resource name", { exact: true }).fill(resourceName);
  await page.getByLabel("Resource type", { exact: true }).selectOption("REUSABLE");
  await page.getByLabel("Initial total quantity", { exact: true }).fill("0");
  await page.getByRole("button", { name: "Add resource", exact: true }).click();
  await expect(page.getByText("Resource saved.")).toBeVisible();
  const resources = await getJson<Array<{ id: number; name: string }>>(page, "/api/resources");
  const resource = resources.find(row => row.name === resourceName);
  expect(resource).toBeDefined();
  await page.locator(".record-detail-button").filter({ hasText: resourceName }).click();
  const detail = page.getByRole("dialog");
  await expect(detail.getByRole("heading", { name: resourceName })).toBeVisible();

  const fixturePath = resolve(process.cwd(), "../../postman/fixtures/academic-demo-image.png");
  const fixtureBytes = readFileSync(fixturePath);
  const fixtureHash = createHash("sha256").update(fixtureBytes).digest("hex");
  const publicId = (await getJson<Array<{ id: number; name: string; publicId: string }>>(page, "/api/resources"))
    .find(row => row.id === resource!.id)?.publicId;
  expect(publicId).toBeTruthy();
  const etagRefresh = page.waitForResponse(response => {
    const path = new URL(response.url()).pathname;
    return response.request().method() === "GET" && /\/api\/v2\/orgs\/[^/]+\/resources\/[^/]+$/.test(path) && path.endsWith(publicId!);
  });
  await page.locator("#resource-image-file").setInputFiles(fixturePath);
  await page.getByRole("button", { name: "Upload image", exact: true }).click();
  await expect(page.getByText(/Image saved/)).toBeVisible();
  await etagRefresh;
  await expect(detail.getByRole("img", { name: `Stored image for ${resourceName}` })).toBeVisible();

  let receiveCalls = 0;
  let operationId = "";
  await page.route("**/api/v2/orgs/*/resources/*/receive", async route => {
    if (route.request().method() !== "POST") return route.continue();
    receiveCalls += 1;
    operationId = String((route.request().postDataJSON() as { operationId?: string }).operationId ?? "");
    const committed = await route.fetch();
    expect(committed.ok(), `receipt operation returned HTTP ${committed.status()}`).toBeTruthy();
    await route.fulfill({ status: 502, contentType: "application/json", body: JSON.stringify({ code: "TEST_RESPONSE_LOSS", message: "Test-only response loss after server commit." }) });
  });
  await page.getByLabel("Receive additional units", { exact: true }).fill("5");
  await page.getByRole("button", { name: "Record receipt", exact: true }).click();
  await expect(page.getByRole("button", { name: "Recover saved receipt", exact: true })).toBeVisible();
  expect(receiveCalls).toBe(1);
  expect(operationId).toMatch(/^[0-9a-f-]{36}$/i);
  await page.getByRole("button", { name: "Recover saved receipt", exact: true }).click();
  await expect(page.getByText(/Saved RECEIPT receipt/)).toBeVisible();
  expect(receiveCalls).toBe(1);
  const movementRows = page.locator(".stock-management table tbody tr");
  await expect(movementRows).toHaveCount(1);
  await expect(movementRows.first()).toContainText(operationId);
  await expect(page.locator(".stock-management")).toContainText("5");
  await page.unroute("**/api/v2/orgs/*/resources/*/receive");

  // Hold the original upstream dispatch until after the receipt lookup returns 404.
  let completeOriginal: (() => Promise<void>) | undefined;
  let originalRequest: { body: string | null; etag: string; key: string } | undefined;
  await page.route("**/api/v2/orgs/*/resources/*/receive", async route => {
    const request = route.request();
    const headers = await request.allHeaders();
    const body = request.postData();
    if (completeOriginal) {
      expect({ body, etag: headers["if-match"], key: headers["idempotency-key"] }).toEqual(originalRequest);
      return route.continue();
    }
    originalRequest = { body, etag: headers["if-match"], key: headers["idempotency-key"] };
    const url = request.url();
    completeOriginal = async () => {
      const response = await page.request.post(url, { data: body ?? "", headers: {
        "Content-Type": "application/json", "Origin": new URL(url).origin,
        "X-CSRF-TOKEN": headers["x-csrf-token"], "If-Match": headers["if-match"],
        "Idempotency-Key": headers["idempotency-key"],
      } });
      expect(response.ok()).toBeTruthy();
    };
    await route.abort();
  });
  await page.getByLabel("Receive additional units", { exact: true }).fill("3");
  await page.getByRole("button", { name: "Record receipt", exact: true }).click();
  await page.getByRole("button", { name: "Recover saved receipt", exact: true }).click();
  await expect(page.getByText(/original request may still commit/)).toBeVisible();
  expect(completeOriginal).toBeDefined();
  await completeOriginal!();
  await page.getByRole("button", { name: "Retry same operation ID safely", exact: true }).click();
  await expect(page.getByText(/Stock receipt confirmed by the server/)).toBeVisible();
  await expect(movementRows).toHaveCount(2);
  await page.unroute("**/api/v2/orgs/*/resources/*/receive");

  const imageResponse = await page.request.get(`/api/resources/${resource!.id}/image`);
  expect(imageResponse.status()).toBe(200);
  const servedHash = createHash("sha256").update(await imageResponse.body()).digest("hex");
  expect(servedHash).toBe(fixtureHash);
  await page.reload();
  await page.locator(".record-detail-button").filter({ hasText: resourceName }).click();
  await expect(page.getByRole("dialog").getByRole("img", { name: `Stored image for ${resourceName}` })).toBeVisible();
  const reloadedImage = await page.request.get(`/api/resources/${resource!.id}/image`);
  expect(createHash("sha256").update(await reloadedImage.body()).digest("hex")).toBe(fixtureHash);
  await page.getByRole("button", { name: "Remove image", exact: true }).click();
  await page.getByRole("button", { name: "Confirm image removal", exact: true }).click();
  await expect(page.getByText("Resource image removed.")).toBeVisible();
  await expect(page.getByRole("dialog").getByRole("img")).toHaveCount(0);
  expect((await page.request.get(`/api/resources/${resource!.id}/image`)).status()).toBe(404);
  await expect(movementRows).toHaveCount(2);
});

test("connected O8–O10 workflows persist through the isolated Java/MySQL backend", async ({ page }) => {
  test.setTimeout(240_000);
  const run = `O8${Date.now().toString(36)}`;
  const departmentName = `${run} department`;
  const disposableDepartment = `${run} remove department`;
  const reusableName = `${run} reusable`;
  const reusableUpdatedName = `${run} reusable reviewed`;
  const consumableName = `${run} consumable`;
  const emptyResourceName = `${run} empty`;
  const mainProjectName = `${run} workshop`;
  const updatedProjectName = `${run} workshop renamed`;
  const temporaryProjectName = `${run} removable project`;
  const borrowerReusable = `${run} reusable borrower`;
  const borrowerConsumable = `${run} consumable borrower`;
  const borrowerCancelled = `${run} cancelled borrower`;

  await page.goto("/#/departments");
  await expect(page.getByRole("heading", { name: "Departments", level: 1 })).toBeVisible();
  await page.getByLabel("Department name", { exact: true }).fill(departmentName);
  await page.getByRole("button", { name: "Add department" }).click();
  await expect(page.getByRole("button", { name: `Edit ${departmentName}` })).toBeVisible();
  await page.getByRole("button", { name: `Edit ${departmentName}` }).click();
  await page.getByLabel("Department name", { exact: true }).nth(1).fill(`${departmentName} renamed`);
  await page.getByRole("button", { name: "Save changes" }).click();
  await expect(page.getByRole("button", { name: `Edit ${departmentName} renamed` })).toBeVisible();
  await page.getByLabel("Department name", { exact: true }).fill(disposableDepartment);
  await page.getByRole("button", { name: "Add department" }).click();
  await page.getByRole("button", { name: `Delete ${disposableDepartment}` }).click();
  const deleteDialog = page.getByRole("dialog");
  await expect(deleteDialog).toBeVisible();
  await expect(deleteDialog).toHaveCSS("opacity", "1");
  await page.screenshot({ path: resolve(screenshotDir, "confirmation-department-delete.png"), fullPage: false });
  await page.getByRole("button", { name: "Confirm delete" }).click();
  await expect(page.getByText("Department deleted.")).toBeVisible();
  const departments = await getJson<Array<{ id: number; name: string }>>(page, "/api/departments");
  const department = departments.find((item) => item.name === `${departmentName} renamed`);
  expect(department).toBeDefined();

  await page.getByRole("link", { name: "Resource Library", exact: true }).click();
  await page.getByLabel("Resource name").fill(reusableName);
  await page.getByLabel("Resource type").selectOption("REUSABLE");
  await page.getByLabel("Initial total quantity").fill("8");
  await page.getByRole("button", { name: "Add resource" }).click();
  await expect(page.getByText("Resource saved.")).toBeVisible();
  let resourceRows = await getJson<Array<{ id: number; name: string; type: string; totalQuantity: number; availableQuantity: number }>>(page, "/api/resources");
  const reusable = resourceRows.find((item) => item.name === reusableName);
  expect(reusable?.totalQuantity).toBe(8);
  await page.locator(".record-detail-button").filter({ hasText: reusableName }).click();
  const resourceDialog = page.getByRole("dialog");
  await expect(resourceDialog.getByText("Availability is the server’s current inventory calculation, including applicable reservations and custody.")).toBeVisible();
  await expect(resourceDialog.locator(".detail-grid").first()).toContainText("8 units");
  await page.getByRole("button", { name: "Close dialog" }).click();
  const concurrentChange = await postJson(page, `/api/resources/${reusable!.id}/update`, {
    name: reusableName, totalQuantity: 9, expectedTotalQuantity: 8,
  });
  expect(concurrentChange.status()).toBe(200);
  const reusableRow = page.getByRole("row", { name: new RegExp(reusableName) });
  await reusableRow.getByRole("button", { name: `Edit ${reusableName}` }).click();
  await page.getByLabel("Name", { exact: true }).fill(reusableUpdatedName);
  await page.getByLabel("Total quantity", { exact: true }).fill("7");
  await page.getByRole("button", { name: "Save changes" }).click();
  await expect(page.getByText(/server quantity changed after this row loaded/i)).toBeVisible();
  await expect(page.getByText(/Current server total: 9/)).toBeVisible();
  await expect(page.getByRole("button", { name: "Save changes" })).toBeDisabled();
  await page.getByRole("button", { name: "I reviewed these current values" }).click();
  await page.getByRole("button", { name: "Save changes" }).click();
  await expect(page.getByText("Resource updated.")).toBeVisible();
  await expect(page.getByRole("row", { name: new RegExp(reusableUpdatedName) })).toBeVisible();

  await page.getByLabel("Resource name").fill(consumableName);
  await page.getByLabel("Resource type").selectOption("CONSUMABLE");
  await page.getByLabel("Initial total quantity").fill("5");
  await page.getByRole("button", { name: "Add resource" }).click();
  await expect(page.getByRole("row", { name: new RegExp(consumableName) })).toBeVisible();
  await page.getByLabel("Resource name").fill(emptyResourceName);
  await page.getByLabel("Initial total quantity").fill("0");
  await page.getByRole("button", { name: "Add resource" }).click();
  await page.getByRole("button", { name: `Delete ${emptyResourceName}` }).click();
  await confirm(page, new RegExp(`Delete ${emptyResourceName}`), "Confirm delete");
  await expect(page.getByText("Resource deleted.")).toBeVisible();
  resourceRows = await getJson(page, "/api/resources");
  const reusableId = resourceRows.find((item) => item.name === reusableUpdatedName)!.id;
  const consumableId = resourceRows.find((item) => item.name === consumableName)!.id;

  await page.getByRole("link", { name: "Projects", exact: true }).click();
  await page.locator("#project-department").selectOption(String(department!.id));
  const startAt = manilaWallTime(20_000);
  const endAt = manilaWallTime(20_000 + 4 * 60 * 60 * 1000);
  await page.getByLabel("Project name").fill(mainProjectName);
  await page.getByLabel("Starts · Philippine time").fill(startAt);
  await page.getByLabel("Ends · Philippine time").fill(endAt);
  await page.getByRole("button", { name: "Create project" }).click();
  await expect(page.getByText("Project saved.")).toBeVisible();
  let projectRows = await getJson<Array<{ id: number; departmentId: number; name: string; startAt: string; endAt: string }>>(page, "/api/projects");
  const mainProject = projectRows.find((item) => item.name === mainProjectName)!;
  await page.getByRole("button", { name: `Edit ${mainProjectName}` }).click();
  await page.getByLabel("Project name", { exact: true }).nth(1).fill(updatedProjectName);
  await page.getByRole("button", { name: "Save changes" }).click();
  await expect(page.getByText("Project updated.")).toBeVisible();
  projectRows = await getJson(page, "/api/projects");
  const updatedMainProject = projectRows.find((item) => item.name === updatedProjectName)!;
  expect(updatedMainProject.startAt).toBe(mainProject.startAt);
  expect(updatedMainProject.endAt).toBe(mainProject.endAt);
  const extraStart = manilaWallTime(2 * 24 * 60 * 60 * 1000);
  const extraEnd = manilaWallTime(2 * 24 * 60 * 60 * 1000 + 60 * 60 * 1000);
  await page.getByLabel("Project name").fill(temporaryProjectName);
  await page.getByLabel("Starts · Philippine time").fill(extraStart);
  await page.getByLabel("Ends · Philippine time").fill(extraEnd);
  await page.getByRole("button", { name: "Create project" }).click();
  await expect(page.getByText("Project saved.")).toBeVisible();
  await page.getByRole("button", { name: `Delete ${temporaryProjectName}` }).click();
  await confirm(page, /Delete project/, "Confirm delete");
  await expect(page.getByText("Project deleted.")).toBeVisible();

  await page.getByRole("link", { name: "Requirements", exact: true }).click();
  projectRows = await getJson(page, "/api/projects");
  const updatedProject = projectRows.find((item) => item.name === updatedProjectName)!;
  // RelatedSelect loads choices asynchronously and auto-selects the first row.
  // Establish that initial state before changing both controlled selections.
  await expect(page.locator("#requirement-project")).not.toHaveValue("");
  await expect(page.locator("#requirement-resource")).not.toHaveValue("");
  await page.locator("#requirement-project").selectOption(String(updatedProject.id));
  await page.locator("#requirement-resource").selectOption(String(reusableId));
  await expect(page.locator("#requirement-project")).toHaveValue(String(updatedProject.id));
  await expect(page.locator("#requirement-resource")).toHaveValue(String(reusableId));
  await page.getByLabel("Quantity required").fill("4");
  const reusableCreateResponse = page.waitForResponse(response =>
    new URL(response.url()).pathname === "/api/requirements" && response.request().method() === "POST");
  await page.getByRole("button", { name: "Add requirement" }).click();
  const reusableCreated = await reusableCreateResponse;
  expect(reusableCreated.status(), "Reusable Requirement create must confirm a committed row").toBe(201);
  const reusableBody = await reusableCreated.json() as { data?: { projectId: number; resourceId: number; quantity: number }; projectId?: number; resourceId?: number; quantity?: number };
  expect(reusableBody.data ?? reusableBody, "Reusable Requirement confirmation must match the selected Project/Resource").toMatchObject({
    projectId: updatedProject.id, resourceId: reusableId, quantity: 4,
  });
  await expect(page.getByText("Requirement saved.")).toBeVisible();
  // The toast can temporarily reflect the prior mutation; wait for the exact
  // committed server record before starting the next UI mutation.
  await expect.poll(async () => (await getJson<Array<{ projectId: number; resourceId: number }>>(page, "/api/requirements"))
    .filter((item) => item.projectId === updatedProject.id && item.resourceId === reusableId).length,
    { message: "Reusable Requirement must be persisted before creating the next one", timeout: 15_000 }).toBe(1);
  await page.locator("#requirement-project").selectOption(String(updatedProject.id));
  await page.locator("#requirement-resource").selectOption(String(consumableId));
  await expect(page.locator("#requirement-project")).toHaveValue(String(updatedProject.id));
  await expect(page.locator("#requirement-resource")).toHaveValue(String(consumableId));
  await page.getByLabel("Quantity required").fill("3");
  const consumableCreateResponse = page.waitForResponse(response =>
    new URL(response.url()).pathname === "/api/requirements" && response.request().method() === "POST");
  await page.getByRole("button", { name: "Add requirement" }).click();
  const consumableCreated = await consumableCreateResponse;
  expect(consumableCreated.status(), "Consumable Requirement create must confirm a committed row").toBe(201);
  const consumableBody = await consumableCreated.json() as { data?: { projectId: number; resourceId: number; quantity: number }; projectId?: number; resourceId?: number; quantity?: number };
  expect(consumableBody.data ?? consumableBody, "Consumable Requirement confirmation must match the selected Project/Resource").toMatchObject({
    projectId: updatedProject.id, resourceId: consumableId, quantity: 3,
  });
  await expect(page.getByText("Requirement saved.")).toBeVisible();
  await expect.poll(async () => (await getJson<Array<{ projectId: number; resourceId: number }>>(page, "/api/requirements"))
    .filter((item) => item.projectId === updatedProject.id && item.resourceId === consumableId).length,
    { message: "Consumable Requirement must be persisted before checking allocations", timeout: 15_000 }).toBe(1);
  const requirements = await getJson<Array<{ id: number; projectId: number; resourceId: number; quantity: number; allocatedQuantity: number; shortageQuantity: number }>>(page, "/api/requirements");
  const reusableRequirement = requirements.find((item) => item.projectId === updatedProject.id && item.resourceId === reusableId);
  const consumableRequirement = requirements.find((item) => item.projectId === updatedProject.id && item.resourceId === consumableId);
  // Assert the actual committed API state before doing unrelated cleanup. A success toast
  // alone is not proof that the specific Project/Resource pair was persisted.
  expect(reusableRequirement, `Missing reusable Requirement for project ${updatedProject.id}, resource ${reusableId}; persisted pairs: ${requirements.map((item) => `${item.projectId}/${item.resourceId}`).join(", ")}`).toBeDefined();
  expect(consumableRequirement, `Missing consumable Requirement for project ${updatedProject.id}, resource ${consumableId}; persisted pairs: ${requirements.map((item) => `${item.projectId}/${item.resourceId}`).join(", ")}`).toBeDefined();
  // Explicit guard also narrows TypeScript beyond Playwright's runtime assertion.
  if (!reusableRequirement || !consumableRequirement) {
    throw new Error("Persisted Requirement identity could not be established for the current Project.");
  }

  const temporaryProjectStart = manilaWallTime(4 * 24 * 60 * 60 * 1000);
  const temporaryProjectEnd = manilaWallTime(4 * 24 * 60 * 60 * 1000 + 60 * 60 * 1000);
  await page.getByRole("link", { name: "Projects", exact: true }).click();
  await page.locator("#project-department").selectOption(String(department!.id));
  await page.getByLabel("Project name").fill(`${run} temporary requirement project`);
  await page.getByLabel("Starts · Philippine time").fill(temporaryProjectStart);
  await page.getByLabel("Ends · Philippine time").fill(temporaryProjectEnd);
  await page.getByRole("button", { name: "Create project" }).click();
  await expect(page.getByText("Project saved.")).toBeVisible();
  projectRows = await getJson(page, "/api/projects");
  const temporaryProject = projectRows.find((item) => item.name === `${run} temporary requirement project`)!;
  await page.getByRole("link", { name: "Resource Library", exact: true }).click();
  await page.getByLabel("Resource name").fill(`${run} temporary requirement resource`);
  await page.getByLabel("Resource type").selectOption("REUSABLE");
  await page.getByLabel("Initial total quantity").fill("0");
  await page.getByRole("button", { name: "Add resource" }).click();
  await expect(page.getByText("Resource saved.")).toBeVisible();
  await expect(page.getByRole("row", { name: new RegExp(`${run} temporary requirement resource`) })).toBeVisible();
  resourceRows = await getJson(page, "/api/resources");
  const temporaryResource = resourceRows.find((item) => item.name === `${run} temporary requirement resource`)!;
  await page.getByRole("link", { name: "Requirements", exact: true }).click();
  await expect(page.locator("#requirement-project")).not.toHaveValue("");
  await expect(page.locator("#requirement-resource")).not.toHaveValue("");
  await page.locator("#requirement-project").selectOption(String(temporaryProject.id));
  await page.locator("#requirement-resource").selectOption(String(temporaryResource.id));
  await page.getByLabel("Quantity required").fill("1");
  await page.getByRole("button", { name: "Add requirement" }).click();
  await expect(page.getByText("Requirement saved.")).toBeVisible();
  await page.getByRole("button", { name: new RegExp(`Edit requirement for ${run} temporary requirement resource`) }).click();
  await page.getByLabel("New quantity").fill("2");
  await page.getByRole("button", { name: "Save quantity" }).click();
  await expect(page.getByText("Requirement quantity updated.")).toBeVisible();
  await page.getByRole("button", { name: new RegExp(`Delete requirement for ${run} temporary requirement resource`) }).click();
  await confirm(page, /Delete requirement/, "Confirm delete");
  await expect(page.getByText("Requirement deleted.")).toBeVisible();
  await page.getByRole("link", { name: "Projects", exact: true }).click();
  await page.getByRole("button", { name: `Delete ${temporaryProject.name}` }).click();
  await confirm(page, /Delete project/, "Confirm delete");
  await page.getByRole("link", { name: "Resource Library", exact: true }).click();
  await page.getByRole("button", { name: `Delete ${temporaryResource.name}` }).click();
  await confirm(page, /Delete resource/, "Confirm delete");

  // Deleting a separate temporary Requirement must not erase either main Requirement.
  const retainedRequirements = await getJson<Array<{ id: number; projectId: number; resourceId: number }>>(page, "/api/requirements");
  expect(retainedRequirements.map((item) => item.id)).toEqual(expect.arrayContaining([
    reusableRequirement!.id, consumableRequirement!.id,
  ]));

  // Let the server's real Asia/Manila active-window check turn true before allocation approval.
  await page.getByRole("link", { name: "Allocations", exact: true }).click();
  await page.waitForTimeout(Math.max(0, Date.parse(`${startAt}+08:00`) - Date.now() + 1_000));
  await page.locator("#allocation-requirement").selectOption(String(reusableRequirement.id));
  await page.getByLabel("Quantity requested").fill("2");
  await page.getByLabel("Borrower name").fill(borrowerReusable);
  await page.getByRole("button", { name: "Submit request" }).click();
  await expect(page.getByText("Allocation request saved as PENDING.")).toBeVisible();
  let allocationRows = await getJson<Array<{ id: number; requirementId: number; borrowerName: string; status: string; phoneNumber?: string; contactPresent: boolean; smsConsent: boolean }>>(page, "/api/allocations");
  const reusableAllocation = allocationRows.find((item) => item.borrowerName === borrowerReusable)!;
  expect(reusableAllocation.status).toBe("PENDING");
  expect("phoneNumber" in reusableAllocation).toBe(false);
  await confirm(page, new RegExp(`Approve request for Allocation ${reusableAllocation.id}`), "Confirm approve request");
  await expect(page.getByText("Approve request confirmed by the server.")).toBeVisible();
  await confirm(page, new RegExp(`Issue batch for Allocation ${reusableAllocation.id}`), "Confirm issue batch");
  await expect(page.getByText("Issue batch confirmed by the server.")).toBeVisible();
  const issueDialog = page.getByRole("dialog");
  expect(await issueDialog.count()).toBe(0);
  await page.getByRole("button", { name: new RegExp(`Details for Allocation ${reusableAllocation.id}`) }).click();
  await expect(page.getByRole("dialog")).toContainText("Lifecycle history");
  await page.screenshot({ path: resolve(screenshotDir, "detail-allocation.png"), fullPage: false });
  await page.getByRole("button", { name: "Close dialog" }).click();
  await confirm(page, new RegExp(`Record usable return for Allocation ${reusableAllocation.id}`), "Confirm record usable return");
  await expect(page.getByText("Record usable return confirmed by the server.")).toBeVisible();

  await page.locator("#allocation-requirement").selectOption(String(consumableRequirement.id));
  await page.getByLabel("Quantity requested").fill("2");
  await page.getByLabel("Borrower name").fill(borrowerConsumable);
  await page.getByLabel("Give consent for a local reminder simulation").check();
  await page.getByLabel("Fictional simulator test contact").fill("+12025550100");
  await page.getByRole("button", { name: "Submit request" }).click();
  await expect(page.getByText("Allocation request saved as PENDING.")).toBeVisible();
  allocationRows = await getJson(page, "/api/allocations");
  const consumableAllocation = allocationRows.find((item) => item.borrowerName === borrowerConsumable)!;
  expect(consumableAllocation.contactPresent).toBe(true);
  expect(consumableAllocation.smsConsent).toBe(true);
  expect("phoneNumber" in consumableAllocation).toBe(false);
  expect(await page.getByText("+12025550100", { exact: true }).count()).toBe(0);
  await confirm(page, new RegExp(`Withdraw reminder consent for Allocation ${consumableAllocation.id}`), "Confirm withdraw reminder consent");
  await expect(page.getByText("Withdraw reminder consent confirmed by the server.")).toBeVisible();
  allocationRows = await getJson(page, "/api/allocations");
  const revoked = allocationRows.find((item) => item.id === consumableAllocation.id)!;
  expect(revoked.contactPresent).toBe(false);
  expect(revoked.smsConsent).toBe(false);
  expect("phoneNumber" in revoked).toBe(false);
  await confirm(page, new RegExp(`Approve request for Allocation ${consumableAllocation.id}`), "Confirm approve request");
  await confirm(page, new RegExp(`Consume batch for Allocation ${consumableAllocation.id}`), "Confirm consumption");
  await expect(page.getByText("Consume batch confirmed by the server.")).toBeVisible();

  await page.locator("#allocation-requirement").selectOption(String(reusableRequirement.id));
  await page.getByLabel("Quantity requested").fill("1");
  await page.getByLabel("Borrower name").fill(borrowerCancelled);
  await page.getByRole("button", { name: "Submit request" }).click();
  await expect(page.getByText("Allocation request saved as PENDING.")).toBeVisible();
  allocationRows = await getJson(page, "/api/allocations");
  const cancelledAllocation = allocationRows.find((item) => item.borrowerName === borrowerCancelled)!;
  await confirm(page, new RegExp(`Cancel request for Allocation ${cancelledAllocation.id}`), "Confirm cancel request");
  await expect(page.getByText("Cancel request confirmed by the server.")).toBeVisible();
  await confirm(page, new RegExp(`Delete request for Allocation ${cancelledAllocation.id}`), "Confirm delete request");
  await expect(page.getByText("Delete request confirmed by the server.")).toBeVisible();

  const routes = [
    ["/", "Overview"], ["/allocations", "Allocations"], ["/resources", "Resource Library"],
    ["/projects", "Projects"], ["/requirements", "Requirements"], ["/departments", "Departments"],
    ["/reminders", "Reminder Activity"], ["/about", "About OrgResource"],
  ] as const;
  for (const [path, heading] of routes) {
    await page.goto(`/#${path}`);
    await expect(page.getByRole("heading", { name: heading, level: 1 })).toBeVisible();
  }

  const finalAllocations = await getJson<Array<{
    id: number; status: string; returnedAt: string | null; consumedAt: string | null;
    reminderOutcome: string; reminderAttempted: boolean; expiredPendingCleanup: boolean;
  }>>(page, "/api/allocations");
  expect(finalAllocations.find((item) => item.id === reusableAllocation.id)).toMatchObject({
    status: "RETURNED", returnedAt: expect.any(String), consumedAt: null,
    reminderOutcome: "NONE", reminderAttempted: false, expiredPendingCleanup: false,
  });
  expect(finalAllocations.find((item) => item.id === consumableAllocation.id)).toMatchObject({
    status: "CONSUMED", consumedAt: expect.any(String), returnedAt: null,
  });
  expect(finalAllocations.some((item) => item.id === cancelledAllocation.id)).toBe(false);
  expect(finalAllocations.every((item) => !("phoneNumber" in item))).toBe(true);
  await page.goto("/#/allocations");
  const returned = page.locator(".allocation-card").filter({ hasText: borrowerReusable });
  await expect(returned.getByText("No reminder attempt", { exact: true })).toBeVisible();
});

test("captures the all-page responsive light/dark screenshot matrix", async ({ page }) => {
  test.setTimeout(180_000);
  const routes = [
    ["overview", "/"], ["allocations", "/allocations"], ["resources", "/resources"], ["projects", "/projects"],
    ["requirements", "/requirements"], ["departments", "/departments"], ["reminders", "/reminders"], ["about", "/about"],
  ] as const;
  await page.emulateMedia({ reducedMotion: "reduce" });
  await page.goto("/");
  await page.reload();
  await expect(page.getByRole("heading", { name: "Overview", level: 1 })).toBeVisible();
  await expect(page.getByText("Local academic prototype", { exact: true })).toHaveCount(0);
  await expect(page.getByText("CSCI 180.05", { exact: true })).toHaveCount(0);
  await page.keyboard.press("Tab");
  await expect(page.getByRole("link", { name: "Skip to main content" })).toBeFocused();
  await page.keyboard.press("Enter");
  await expect(page.locator("#main")).toBeFocused();
  await expect(page.locator(".app-shell")).toHaveAttribute("data-motion", "off");
  for (const theme of ["light", "dark"] as const) {
    const current = await page.locator(".app-shell").getAttribute("data-theme");
    if (current !== theme) await page.getByRole("button", { name: `Switch to ${theme} theme` }).click();
    for (const viewport of [{ name: "desktop", width: 1440, height: 900 }, { name: "mobile", width: 360, height: 844 }]) {
      await page.setViewportSize({ width: viewport.width, height: viewport.height });
      for (const [name, path] of routes) {
        await page.goto(`/#${path}`);
        await expect(page.getByRole("heading", { level: 1 })).toBeVisible();
        await expect(page.locator('.state-block[role="status"]')).toHaveCount(0);
        if (name === "overview") await expect(page.locator(".overview-toolbar strong")).not.toHaveText("Loading");
        const width = await page.evaluate(() => ({ content: document.documentElement.scrollWidth, viewport: window.innerWidth }));
        expect(width.content, `${name} ${viewport.name} ${theme} horizontal overflow`).toBeLessThanOrEqual(width.viewport + 1);
        await page.screenshot({ path: resolve(screenshotDir, `${name}-${viewport.name}-${theme}.png`), fullPage: true });
      }
    }
  }
  await page.getByRole("button", { name: "Switch to light theme" }).click();
  await page.setViewportSize({ width: 768, height: 1024 });
  await page.goto("/#/allocations");
  await expect(page.getByRole("heading", { name: "Allocations", level: 1 })).toBeVisible();
  const tabletToolbar = page.locator(".top-toolbar");
  for (const control of [
    tabletToolbar.getByRole("combobox", { name: "Switch organization" }),
    tabletToolbar.getByRole("button", { name: "Sign out", exact: true }),
  ]) {
    await expect(control).toBeInViewport({ ratio: 1 });
    const bounds = await control.boundingBox();
    const toolbarBounds = await tabletToolbar.boundingBox();
    expect(bounds!.x + bounds!.width).toBeLessThanOrEqual(toolbarBounds!.x + toolbarBounds!.width);
  }
  await expect(page.locator('.state-block[role="status"]')).toHaveCount(0);
  await page.screenshot({ path: resolve(screenshotDir, "allocations-tablet-light.png"), fullPage: true });

  await page.goto("/#/projects");
  const projectButton = page.locator(".project-actions button").first();
  await expect(projectButton).toBeVisible();
  await projectButton.click();
  const projectDetail = page.getByRole("dialog");
  await expect(projectDetail).toBeVisible();
  await expect(projectDetail).toHaveCSS("opacity", "1");
  await expect(page.locator('.state-block[role="status"]')).toHaveCount(0);
  await page.screenshot({ path: resolve(screenshotDir, "detail-project-light.png"), fullPage: false });
  await page.getByRole("button", { name: "Close dialog" }).click();
  await page.goto("/#/resources");
  const resourceDetail = page.locator(".record-detail-button").first();
  await expect(resourceDetail).toBeVisible();
  await resourceDetail.click();
  const resourceDetailSheet = page.getByRole("dialog");
  await expect(resourceDetailSheet).toBeVisible();
  await expect(resourceDetailSheet).toHaveCSS("opacity", "1");
  await expect(resourceDetailSheet.getByText("Availability is the server’s current inventory calculation, including applicable reservations and custody.")).toBeVisible();
  await page.screenshot({ path: resolve(screenshotDir, "detail-resource-light.png"), fullPage: false });
  await page.getByRole("button", { name: "Close dialog" }).click();

  const departmentPage = "**/api/v2/workspace/departments*";
  let completeEmptyPage!: () => void;
  const waitForScreenshot = new Promise<void>(finish => { completeEmptyPage = finish; });
  await page.route(departmentPage, async route => {
    await waitForScreenshot;
    await route.fulfill({ status: 200, json: { data: { items: [], nextCursor: null } } });
  });
  try {
    await page.goto("/#/departments");
    await expect(page.locator('.state-block[role="status"]')).toContainText("Loading current records");
    await page.screenshot({ path: resolve(screenshotDir, "state-departments-loading.png"), fullPage: false });
  } finally {
    completeEmptyPage();
  }
  await expect(page.getByText("No departments yet")).toBeVisible();
  await page.screenshot({ path: resolve(screenshotDir, "state-departments-empty.png"), fullPage: true });
  await page.unroute(departmentPage);

  await page.route(departmentPage, async route => {
    await route.fulfill({ status: 503, json: { code: "TEST_UNAVAILABLE", message: "Test-only unavailable response." } });
  });
  await page.goto("/#/about");
  await page.goto("/#/departments");
  await expect(page.getByRole("alert")).toContainText("Test-only unavailable response");
  await page.screenshot({ path: resolve(screenshotDir, "state-departments-error.png"), fullPage: false });
  await page.unroute(departmentPage);
});

test("desktop pointer highlights follow the mouse and motion can be switched off", async ({ page }) => {
  await page.emulateMedia({ reducedMotion: "no-preference" });
  await page.goto("/");
  const shell = page.locator(".app-shell");
  await expect(shell).toHaveAttribute("data-motion", "on");
  const hero = page.locator(".overview-hero");
  await expect(hero).toBeVisible();
  const bounds = await hero.boundingBox();
  expect(bounds).not.toBeNull();
  await page.mouse.move(bounds!.x + bounds!.width * 0.32, bounds!.y + bounds!.height * 0.55);
  await expect.poll(() => hero.evaluate((node) => node.style.getPropertyValue("--pointer-x"))).toMatch(/px$/);
  expect(await hero.evaluate((node) => node.matches(".glass-panel:hover"))).toBe(true);

  const activeIcon = page.locator('.nav-link[aria-current="page"] .nav-icon');
  await activeIcon.hover();
  await expect.poll(() => activeIcon.evaluate((node) => getComputedStyle(node).transform)).not.toBe("none");

  await page.goto("/#/departments");
  await page.getByLabel("Department name", { exact: true }).fill("Pointer feedback check");
  const magneticButton = page.getByRole("button", { name: "Add department" });
  const buttonBounds = await magneticButton.boundingBox();
  expect(buttonBounds).not.toBeNull();
  await page.mouse.move(buttonBounds!.x + buttonBounds!.width * 0.8, buttonBounds!.y + buttonBounds!.height * 0.8);
  await expect.poll(() => magneticButton.evaluate((node) => (node as HTMLElement).style.getPropertyValue("--mag-x"))).not.toBe("0px");

  await page.getByRole("button", { name: "Motion on" }).click();
  await expect(shell).toHaveAttribute("data-motion", "off");
  await page.mouse.move(buttonBounds!.x + buttonBounds!.width * 0.3, buttonBounds!.y + buttonBounds!.height * 0.3);
  await expect.poll(() => magneticButton.evaluate((node) => (node as HTMLElement).style.getPropertyValue("--mag-x"))).toBe("0px");
});

test("C6a owner opens recorded events and downloads real redacted CSV and JSON files", async ({ page }) => {
  await page.goto("/#/activity");
  await expect(page.getByRole("heading", { name: "Activity", level: 1 })).toBeVisible();
  await expect(page.getByText(/Recorded audit action|Stock movement/).first()).toBeVisible();
  await page.getByLabel("Show").selectOption("audit");
  await expect(page.getByText(/Recorded audit action/).first()).toBeVisible();
  await expect(page.getByText(/^Stock movement ·/)).toHaveCount(0);
  await page.getByRole("link", { name: "Exports", exact: true }).click();
  const csvDownloaded = page.waitForEvent("download");
  await page.getByRole("button", { name: "Download export" }).click();
  const csv = await csvDownloaded;
  expect(csv.suggestedFilename()).toMatch(/^orgresource-resources-\d{8}T\d{6}Z\.csv$/);
  expect(readFileSync((await csv.path())!, "utf8")).toContain("schemaVersion,exportedAt,id,name,type,totalQuantity\r\n");
  await page.getByLabel("Dataset").selectOption("AUDIT");
  await page.getByLabel("File format").selectOption("TENANT_JSON");
  const jsonDownloaded = page.waitForEvent("download");
  await page.getByRole("button", { name: "Download export" }).click();
  const json = await jsonDownloaded;
  expect(json.suggestedFilename()).toMatch(/^orgresource-audit-\d{8}T\d{6}Z\.json$/);
  const exported = JSON.parse(readFileSync((await json.path())!, "utf8")) as { schemaVersion: number; dataset: string; rows: object[] };
  expect(exported.schemaVersion).toBe(1);
  expect(exported.dataset).toBe("AUDIT");
  expect(exported.rows.length).toBeGreaterThan(0);
  expect(exported.rows.every(row => !("actorUserId" in row) && !("targetId" in row))).toBe(true);
  await page.emulateMedia({ reducedMotion: "reduce" });
  for (const width of [768, 375]) {
    await page.setViewportSize({ width, height: 800 });
    await page.goto("/#/activity");
    const newest = page.getByRole("button", { name: "Newest events" });
    await expect(newest).toBeEnabled();
    await newest.focus();
    await newest.press("Enter");
    await expect(page.getByText(/Recorded audit action|Stock movement/).first()).toBeVisible();
    expect(await page.evaluate(() => document.documentElement.scrollWidth)).toBeLessThanOrEqual(width + 1);
    await page.goto("/#/exports");
    await expect(page.getByRole("button", { name: "Download export" })).toBeEnabled();
    expect(await page.evaluate(() => document.documentElement.scrollWidth)).toBeLessThanOrEqual(width + 1);
  }
});

test("C8 traverses memberships and recorded activity using bounded pages", async ({ page, playwright }) => {
  test.setTimeout(90_000);
  const { activeOrgId } = await getJson<{ activeOrgId: string }>(page, "/api/v2/session");
  expect(activeOrgId).toBeTruthy();
  const orgPath = "/api/v2/orgs/" + activeOrgId;
  const prefix = "c8-roster-" + Date.now().toString(36);
  const fixtures: string[] = [];
  for (let index = 1; index <= 26; index++) {
    const displayName = "Fictional " + prefix + " member " + index;
    await inviteFictionalAccount(page, playwright.request, orgPath, "MEMBER", prefix + "-" + index, displayName);
    fixtures.push(displayName);
  }
  const seen: string[] = [];
  const memberRead = new Set<string>();
  page.on("request", request => {
    const url = new URL(request.url());
    if (request.method() === "GET" && url.pathname === orgPath + "/members") {
      memberRead.add(url.searchParams.get("limit") ?? "unbounded");
    }
  });

  await page.goto("/#/governance");
  const members = page.getByRole("navigation", { name: "Membership pages" });
  const nextMember = members.getByRole("button", { name: "Next page" });
  const previousMember = members.getByRole("button", { name: "Previous page" });
  const memberNames = page.locator(".governance-grid .member-list article strong");
  await expect(members.getByRole("status")).toHaveText("Page 1 · 25 shown");
  await expect(previousMember).toBeDisabled();
  seen.push(...await memberNames.allTextContents());
  expect(seen).not.toContain(fixtures.at(-1));
  await nextMember.focus();
  await page.keyboard.press("Enter");
  await expect(members.getByRole("status")).toContainText("Page 2");
  await expect(members.getByRole("status")).toContainText("End of results");
  seen.push(...await memberNames.allTextContents());
  expect(seen.filter(name => name.startsWith("Fictional " + prefix))).toEqual(fixtures);
  expect(memberRead).toEqual(new Set(["25"]));
  await expect(nextMember).toBeDisabled();

  const lastMember = page.locator(".member-row").filter({ has: page.getByText(fixtures.at(-1)!, { exact: true }) });
  await lastMember.getByLabel("Role").selectOption("ADMIN");
  await expect(page.getByText("Membership role updated.")).toBeVisible();
  const recipients = page.getByLabel("Recipient membership");
  await expect(recipients.getByRole("option", { name: fixtures.at(-1), exact: true })).toHaveCount(1);
  await expect(members.getByRole("status")).toContainText("Page 2");
  await previousMember.click();
  await expect(members.getByRole("status")).toHaveText("Page 1 · 25 shown");
  await expect(recipients.getByRole("option", { name: fixtures.at(-1), exact: true })).toHaveCount(0);

  await page.goto("/#/activity");
  const activity = page.getByRole("navigation", { name: "Activity pages" });
  const older = activity.getByRole("button", { name: "Next page" });
  const newer = activity.getByRole("button", { name: "Previous page" });
  const events = page.locator(".member-list > li");
  await expect(activity.getByRole("status")).toHaveText("Page 1 · 50 shown");
  await expect(events).toHaveCount(50);
  const firstEvents = await events.allTextContents();
  await older.click();
  await expect(activity.getByRole("status")).toContainText("Page 2 ·");
  expect(await events.allTextContents()).not.toEqual(firstEvents);
  await newer.click();
  await expect(activity.getByRole("status")).toHaveText("Page 1 · 50 shown");
  expect(await events.allTextContents()).toEqual(firstEvents);
  await older.click();
  await expect(activity.getByRole("status")).toContainText("Page 2 ·");
  await page.getByLabel("Show").selectOption("audit");
  await expect(activity.getByRole("status")).toHaveText("Page 1 · 50 shown");
  await expect(newer).toBeDisabled();
  await expect(page.getByText(/^Stock movement ·/)).toHaveCount(0);

  for (let pageNumber = 2; pageNumber <= 15 && await older.isEnabled(); pageNumber++) {
    await older.click();
    await expect(activity.getByRole("status")).toContainText("Page " + pageNumber + " ·");
  }
  await expect(activity.getByRole("status")).toContainText("End of results");
  await expect(older).toBeDisabled();
  await page.getByRole("button", { name: "Newest events" }).click();
  await expect(activity.getByRole("status")).toHaveText("Page 1 · 50 shown");
});

test("C8 linked borrower and assigned LEAD complete the packaged Project work journey", async ({ page, browser, playwright }) => {
  test.setTimeout(90_000);
  const { activeOrgId } = await getJson<{ activeOrgId: string }>(page, "/api/v2/session");
  const orgPath = "/api/v2/orgs/" + activeOrgId;
  const prefix = "c8-scoped-" + Date.now().toString(36);
  const leadName = "Fictional " + prefix + " lead";
  const borrowerName = "Fictional " + prefix + " borrower";
  const leadState = await inviteFictionalAccount(page, playwright.request, orgPath, "LEAD", prefix + "-lead", leadName);
  const borrowerState = await inviteFictionalAccount(page, playwright.request, orgPath, "MEMBER", prefix + "-borrower", borrowerName);
  const department = await postJson(page, "/api/departments", { name: prefix + " department" });
  expect(department.status()).toBe(201);
  const { id: departmentId } = await department.json() as { id: number };
  const projectName = prefix + " Project", unassignedName = prefix + " private scope";
  for (const name of [projectName, unassignedName]) {
    const project = await postJson(page, "/api/projects", { departmentId, name,
      startAt: "2040-11-20T10:00:00", endAt: "2040-11-20T13:00:00" });
    expect(project.status()).toBe(201);
  }
  const resourceName = prefix + " projector";
  expect((await postJson(page, "/api/resources", { name: resourceName, type: "REUSABLE", totalQuantity: 6 })).status()).toBe(201);
  await page.goto("/#/project-work");
  await page.getByRole("button", { name: "Open " + projectName, exact: true }).click();
  await page.getByRole("searchbox", { name: "Find eligible project lead" }).fill(prefix);
  await page.getByLabel("Eligible Project lead", { exact: true }).selectOption({ label: leadName });
  await page.getByRole("button", { name: "Assign Project lead" }).click();
  await expect(page.getByText("Project lead assigned.", { exact: true })).toBeVisible();

  const leadContext = await browser.newContext({ storageState: leadState });
  const borrowerContext = await browser.newContext({ storageState: borrowerState });
  try {
    const lead = await leadContext.newPage();
    await lead.goto("/#/project-work");
    await lead.getByRole("button", { name: "Open workspace", exact: true }).click();
    await expect(lead.getByRole("navigation", { name: "Project work pages" }).getByRole("status"))
      .toHaveText("Page 1 · 1 shown · End of results");
    await expect(lead.getByRole("button", { name: "Open " + unassignedName, exact: true })).toHaveCount(0);
    await lead.getByRole("button", { name: "Open " + projectName, exact: true }).click();
    await lead.getByRole("searchbox", { name: "Find resource for requirement" }).fill(prefix);
    await lead.getByLabel("Resource for Requirement", { exact: true }).selectOption({ label: resourceName });
    await lead.getByLabel("Required quantity", { exact: true }).fill("3");
    await lead.getByRole("button", { name: "Add Project Requirement" }).click();
    await expect(lead.getByText("Requirement saved.", { exact: true })).toBeVisible();
    await lead.getByRole("button", { name: "Request " + resourceName, exact: true }).click();
    await lead.getByRole("searchbox", { name: "Find borrower membership" }).fill(prefix);
    await expect(lead.getByRole("button", { name: "Save linked request" })).toBeDisabled();
    await lead.getByLabel("Borrower membership", { exact: true }).selectOption({ label: borrowerName });
    await lead.getByRole("button", { name: "Save linked request" }).click();
    await expect(lead.getByText("Allocation request saved.", { exact: true })).toBeVisible();
    await expect(lead.getByRole("button", { name: "Cancel pending request" })).toBeEnabled();
    await expect(lead.getByRole("button", { name: /approve|issue/i })).toHaveCount(0);

    const borrowerPage = await borrowerContext.newPage();
    await borrowerPage.goto("/#/borrowed");
    await borrowerPage.getByRole("button", { name: "Open workspace", exact: true }).click();
    await expect(borrowerPage.getByRole("heading", { name: "My borrowed resources", level: 1 })).toBeVisible();
    await expect(borrowerPage.getByText(resourceName, { exact: true })).toBeVisible();
    await expect(borrowerPage.getByRole("link", { name: "Project work", exact: true })).toHaveCount(0);
    await expect(borrowerPage.getByText("No active SMS reminder consent.", { exact: true })).toBeVisible();
    await expect(borrowerPage.getByRole("button", { name: "Withdraw SMS consent" })).toHaveCount(0);

    page.once("dialog", dialog => void dialog.accept());
    await page.getByRole("button", { name: "Clear Project lead" }).click();
    await expect(page.getByText("Project lead cleared.", { exact: true })).toBeVisible();
    await lead.reload();
    await expect(lead.getByText("No Projects are available for this membership.", { exact: true })).toBeVisible();
    await borrowerPage.reload();
    await expect(borrowerPage.getByText(resourceName, { exact: true })).toBeVisible();
  } finally {
    await leadContext.close();
    await borrowerContext.close();
  }
});

test("the packaged Java application serves React assets, hash routes, and the five JSON APIs", async ({ page }) => {
  test.skip(!process.env.EXT_E2E_BASE_URL, "Packaged Java entry assets require an explicit packaged origin.");
  const assetResponses: Array<{ path: string; status: number }> = [];
  page.on("response", (response) => {
    const path = new URL(response.url()).pathname;
    if (path.startsWith("/assets/") && /\.(?:js|css)$/.test(path)) {
      assetResponses.push({ path, status: response.status() });
    }
  });

  const home = await page.goto("/");
  expect(home?.status()).toBe(200);
  await expect(page.getByRole("heading", { name: "Overview", level: 1 })).toBeVisible();
  await expect.poll(() => assetResponses.filter((item) => item.path.endsWith(".js")).length).toBeGreaterThan(0);
  await expect.poll(() => assetResponses.filter((item) => item.path.endsWith(".css")).length).toBeGreaterThan(0);
  expect(assetResponses.every((item) => item.status === 200)).toBe(true);

  await page.goto("/#/projects");
  await expect(page.getByRole("heading", { name: "Projects", level: 1 })).toBeVisible();
  await page.reload();
  await expect(page.getByRole("heading", { name: "Projects", level: 1 })).toBeVisible();

  const restoredProject = process.env.EXT_E2E_RESTORED_PROJECT;
  if (restoredProject) {
    await page.getByRole("button", { name: `View ${restoredProject}`, exact: true }).click();
    await expect(page.getByRole("dialog").getByRole("heading", { name: restoredProject, exact: true })).toBeVisible();
  }

  for (const route of ["departments", "projects", "resources", "requirements", "allocations"]) {
    const rows = await getJson<unknown[]>(page, `/api/${route}`);
    expect(Array.isArray(rows), `/api/${route} returns a JSON array`).toBe(true);
  }
});

test("Resource Library traverses 101 matching resources with bounded server pages", async ({ page }) => {
  test.setTimeout(90_000);
  const prefix = "C8 page fixture " + Date.now().toString(36);
  const expected = Array.from({ length: 101 }, (_, index) => prefix + " item " + String(index + 1).padStart(3, "0"));
  const resourceIds: number[] = [];
  for (const name of expected) {
    const created = await postJson(page, "/api/resources", { name, type: "CONSUMABLE", totalQuantity: 0 });
    expect(created.status()).toBe(201);
    resourceIds.push((await created.json() as { id: number }).id);
  }

  const legacyReads: string[] = [];
  const usedCursors = new Set<string>();
  page.on("request", request => {
    if (request.method() !== "GET") return;
    const url = new URL(request.url());
    if (/^\/api\/(departments|projects|resources|requirements|allocations)$/.test(url.pathname)) legacyReads.push(url.pathname);
    const cursor = url.searchParams.get("cursor");
    if (url.pathname === "/api/v2/workspace/resources" && cursor) usedCursors.add(cursor);
  });
  await page.goto("/#/resources");
  await page.getByRole("searchbox", { name: "Search resources" }).fill(prefix);
  const table = page.getByRole("table");
  const nav = page.getByRole("navigation", { name: "Resource pages" });
  const next = nav.getByRole("button", { name: "Next page" });
  const previous = nav.getByRole("button", { name: "Previous page" });
  await expect(nav.getByRole("status")).toHaveText("Page 1 · 50 shown");
  await expect(previous).toBeDisabled();
  const seen: string[] = [];
  for (const [pageNumber, size] of [[1, 50], [2, 50], [3, 1]]) {
    await expect(nav.getByRole("status")).toContainText("Page " + pageNumber + " · " + size + " shown");
    const names = await table.locator("tbody tr td:first-child strong").allTextContents();
    expect(names).toHaveLength(size);
    seen.push(...names);
    if (pageNumber !== 3) {
      await next.focus();
      await page.keyboard.press("Enter");
    }
  }
  expect(seen).toEqual(expected);
  expect(usedCursors.size).toBe(2);
  expect(legacyReads).toEqual([]);
  await expect(next).toBeDisabled();
  await expect(nav.getByRole("status")).toContainText("End of results");
  await previous.click();
  await expect(nav.getByRole("status")).toHaveText("Page 2 · 50 shown");

  const department = await postJson(page, "/api/departments", { name: prefix + " department" });
  expect(department.status()).toBe(201);
  const departmentId = (await department.json() as { id: number }).id;
  const projectName = prefix + " project";
  const project = await postJson(page, "/api/projects", {
    departmentId, name: projectName, startAt: "2040-12-10T10:00:00", endAt: "2040-12-10T11:00:00",
  });
  expect(project.status()).toBe(201);
  const projectId = (await project.json() as { id: number }).id;
  for (const resourceId of resourceIds.slice(0, 51)) {
    const requirement = await postJson(page, "/api/requirements", { projectId, resourceId, quantity: 2 });
    expect(requirement.status()).toBe(201);
  }

  await page.goto("/#/requirements");
  await page.getByRole("searchbox", { name: "Find resource", exact: true }).fill(prefix);
  const resourceChoices = page.getByRole("navigation", { name: "Resource choices" });
  await expect(resourceChoices.getByRole("status")).toHaveText("Page 1 · 50 shown");
  await resourceChoices.getByRole("button", { name: "Next page" }).click();
  await expect(resourceChoices.getByRole("status")).toHaveText("Page 2 · 50 shown");
  const chosenResource = page.locator("#requirement-resource");
  await chosenResource.selectOption(String(resourceIds[50]));
  await resourceChoices.getByRole("button", { name: "Previous page" }).click();
  await expect(resourceChoices.getByRole("status")).toHaveText("Page 1 · 50 shown");
  await expect(chosenResource).toHaveValue(String(resourceIds[50]));

  await page.goto("/#/projects");
  await page.getByRole("searchbox", { name: "Search projects" }).fill(projectName);
  await page.getByRole("button", { name: "View " + projectName, exact: true }).click();
  const details = page.getByRole("dialog");
  const requirements = details.getByRole("navigation", { name: "Project Requirement pages" });
  const history = details.getByRole("navigation", { name: "Project Allocation history pages" });
  await expect(requirements.getByRole("status")).toHaveText("Page 1 · 50 shown");
  await expect(history.getByRole("status")).toHaveText("Page 1 · 0 shown · End of results");
  await requirements.getByRole("button", { name: "Next page" }).click();
  await expect(requirements.getByRole("status")).toHaveText("Page 2 · 1 shown · End of results");
  await expect(details.getByText(expected[50], { exact: true })).toBeVisible();
  await expect(history.getByRole("status")).toHaveText("Page 1 · 0 shown · End of results");

  await page.goto("/#/allocations");
  const findRequirement = page.getByRole("searchbox", { name: "Find project requirement", exact: true });
  await findRequirement.fill(expected[50]);
  const requirementOption = page.locator("#allocation-requirement option").filter({ hasText: expected[50] });
  await expect(requirementOption).toHaveCount(1);
  const chosenRequirementId = await requirementOption.getAttribute("value");
  expect(chosenRequirementId).toBeTruthy();
  await page.locator("#allocation-requirement").selectOption(chosenRequirementId!);
  await page.getByLabel("Borrower name", { exact: true }).fill("Fictional linked-choice borrower");
  const allocationPost = page.waitForResponse(response => response.request().method() === "POST"
    && new URL(response.url()).pathname === "/api/allocations");
  await page.getByRole("button", { name: "Submit request", exact: true }).click();
  const saved = await allocationPost;
  expect(saved.status()).toBe(201);
  expect((await saved.json() as { requirementId: number }).requirementId).toBe(Number(chosenRequirementId));

  await page.goto("/#/reminders");
  await expect(page.getByRole("heading", { name: "Reminder Activity" })).toBeVisible();
  await expect(page.getByRole("navigation", { name: "Reminder pages" }).getByRole("status")).toContainText("Page 1");
  expect(legacyReads).toEqual([]);
});



test("sidebar wordmark stays on one line and inside its panel at responsive widths", async ({ page }) => {
  test.setTimeout(90_000);
  await page.emulateMedia({ reducedMotion: "reduce" });
  await page.goto("/#/");
  await expect(page.getByRole("heading", { name: "Overview", level: 1 })).toBeVisible();
  for (const width of [1440, 1120, 1024, 900, 768, 681, 680, 360, 320]) {
    await page.setViewportSize({ width, height: 510 });
    if (width <= 680) {
      const toggle = page.getByRole("button", { name: "Open navigation" });
      if (await toggle.getAttribute("aria-expanded") !== "true") await toggle.click();
    }
    const brand = page.getByRole("link", { name: "OrgResource Academic Commons, Overview" });
    await expect(brand).toBeVisible();
    const layout = await brand.evaluate((node) => {
      const title = node.querySelector("strong")!;
      const seal = node.querySelector(".brand-seal")!;
      const text = node.querySelector(".brand-text")!;
      const aside = node.closest("aside.sidebar")!;
      const range = document.createRange();
      range.selectNodeContents(title);
      const wordRects = [...range.getClientRects()];
      const sidebarRect = aside.getBoundingClientRect();
      const brandRect = node.getBoundingClientRect();
      const sealRect = seal.getBoundingClientRect();
      const textRect = text.getBoundingClientRect();
      return {
        lines: wordRects.length,
        lineWidth: wordRects[0]?.width ?? 0,
        titleBoxWidth: title.getBoundingClientRect().width,
        inside: brandRect.left >= sidebarRect.left - 1 && brandRect.right <= sidebarRect.right + 1 &&
          sealRect.right <= textRect.left && wordRects.every(rect => rect.left >= textRect.left - 1 && rect.right <= textRect.right + 1),
        horizontalOverflow: document.documentElement.scrollWidth > window.innerWidth + 1,
      };
    });
    expect(layout.lines, `OrgResource should not split at ${width}px`).toBe(1);
    expect(layout.inside, `brand seal/title clipped at ${width}px: ${JSON.stringify(layout)}`).toBe(true);
    expect(layout.horizontalOverflow, `document overflows at ${width}px`).toBe(false);
  }
});

test("Project work panels contain headings at desktop and mobile sizes", async ({ page }) => {
  for (const viewport of [{ width: 860, height: 510 }, { width: 375, height: 667 }]) {
    await page.setViewportSize(viewport);
    await page.goto("/#/project-work");
    const panel = page.locator(".project-work-panel").first();
    const heading = panel.getByRole("heading", { name: "Available Projects" });
    await expect(heading).toBeVisible();
    const geometry = await panel.evaluate(node => {
      const container = node.getBoundingClientRect();
      const title = node.querySelector("h2")!.getBoundingClientRect();
      return { left: container.left, right: container.right, titleLeft: title.left, titleRight: title.right };
    });
    expect(geometry.titleLeft).toBeGreaterThanOrEqual(geometry.left + 8);
    expect(geometry.titleRight).toBeLessThanOrEqual(geometry.right - 8);
    expect(await page.evaluate(() => document.documentElement.scrollWidth)).toBeLessThanOrEqual(viewport.width);
    if (viewport.width > 680) {
      const sidebar = page.locator("aside.sidebar");
      expect(await sidebar.evaluate(node => getComputedStyle(node).overflowY)).toBe("auto");
      const accountLink = page.getByRole("navigation", { name: "Main navigation" })
        .getByRole("link", { name: "Account and security" });
      await accountLink.scrollIntoViewIfNeeded();
      await expect(accountLink).toBeInViewport();
    }
  }
});
