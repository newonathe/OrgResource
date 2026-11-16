import { mkdirSync } from "node:fs";
import { resolve } from "node:path";
import { expect, test, type Page } from "@playwright/test";

const screenshotDir = resolve(process.env.ORGRESOURCE_E2E_SCREENSHOT_DIR ?? "../evidence/O8-O10/screenshots/raw");
mkdirSync(screenshotDir, { recursive: true });

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
  return await response.json() as T;
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
  await expect(resourceDialog.locator(".detail-grid")).toContainText("8 units");
  await page.getByRole("button", { name: "Close dialog" }).click();
  const concurrentChange = await page.request.post(`/api/resources/${reusable!.id}/update`, {
    data: { name: reusableName, totalQuantity: 9, expectedTotalQuantity: 8 },
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
  await page.locator("#requirement-project").selectOption(String(updatedProject.id));
  await page.locator("#requirement-resource").selectOption(String(reusableId));
  await page.getByLabel("Quantity required").fill("4");
  await page.getByRole("button", { name: "Add requirement" }).click();
  await expect(page.getByText("Requirement saved.")).toBeVisible();
  await page.locator("#requirement-project").selectOption(String(updatedProject.id));
  await page.locator("#requirement-resource").selectOption(String(consumableId));
  await page.getByLabel("Quantity required").fill("3");
  await page.getByRole("button", { name: "Add requirement" }).click();
  await expect(page.getByText("Requirement saved.")).toBeVisible();
  const requirements = await getJson<Array<{ id: number; projectId: number; resourceId: number; quantity: number; allocatedQuantity: number; shortageQuantity: number }>>(page, "/api/requirements");
  const reusableRequirement = requirements.find((item) => item.projectId === updatedProject.id && item.resourceId === reusableId)!;
  const consumableRequirement = requirements.find((item) => item.projectId === updatedProject.id && item.resourceId === consumableId)!;

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
  resourceRows = await getJson(page, "/api/resources");
  const temporaryResource = resourceRows.find((item) => item.name === `${run} temporary requirement resource`)!;
  await page.getByRole("link", { name: "Requirements", exact: true }).click();
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

  const finalAllocations = await getJson<Array<{ id: number; borrowerName: string; status: string; phoneNumber?: string }>>(page, "/api/allocations");
  expect(finalAllocations.find((item) => item.id === reusableAllocation.id)?.status).toBe("RETURNED");
  expect(finalAllocations.find((item) => item.id === consumableAllocation.id)?.status).toBe("CONSUMED");
  expect(finalAllocations.some((item) => item.id === cancelledAllocation.id)).toBe(false);
  expect(finalAllocations.every((item) => !("phoneNumber" in item))).toBe(true);
});

test("captures the all-page responsive light/dark screenshot matrix", async ({ page }) => {
  test.setTimeout(180_000);
  const routes = [
    ["overview", "/"], ["allocations", "/allocations"], ["resources", "/resources"], ["projects", "/projects"],
    ["requirements", "/requirements"], ["departments", "/departments"], ["reminders", "/reminders"], ["about", "/about"],
  ] as const;
  await page.emulateMedia({ reducedMotion: "reduce" });
  await page.goto("/");
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
    for (const viewport of [{ name: "desktop", width: 1440, height: 900 }, { name: "mobile", width: 390, height: 844 }]) {
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

  await page.route("**/api/departments", async (route) => {
    await page.waitForTimeout(1_000);
    await route.fulfill({ status: 200, contentType: "application/json", body: "[]" });
  });
  await page.goto("/#/departments");
  await expect(page.getByRole("status")).toContainText("Loading current records");
  await page.screenshot({ path: resolve(screenshotDir, "state-departments-loading.png"), fullPage: false });
  await expect(page.getByText("No departments yet")).toBeVisible();
  await page.screenshot({ path: resolve(screenshotDir, "state-departments-empty.png"), fullPage: true });
  await page.unroute("**/api/departments");

  await page.route("**/api/departments", async (route) => {
    await route.fulfill({ status: 503, contentType: "application/json", body: JSON.stringify({ code: "TEST_UNAVAILABLE", message: "Test-only unavailable response." }) });
  });
  await page.goto("/#/about");
  await page.goto("/#/departments");
  await expect(page.getByRole("alert")).toContainText("Test-only unavailable response");
  await page.screenshot({ path: resolve(screenshotDir, "state-departments-error.png"), fullPage: false });
  await page.unroute("**/api/departments");
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

test("the packaged Java application serves React assets, hash routes, and the five JSON APIs", async ({ page }) => {
  const assetResponses: Array<{ path: string; status: number }> = [];
  page.on("response", (response) => {
    const path = new URL(response.url()).pathname;
    if (path.startsWith("/assets/") && /\.(?:js|css)$/.test(path)) {
      assetResponses.push({ path, status: response.status() });
    }
  });

  const home = await page.goto("http://127.0.0.1:8080/");
  expect(home?.status()).toBe(200);
  await expect(page.getByRole("heading", { name: "Overview", level: 1 })).toBeVisible();
  await expect.poll(() => assetResponses.filter((item) => item.path.endsWith(".js")).length).toBeGreaterThan(0);
  await expect.poll(() => assetResponses.filter((item) => item.path.endsWith(".css")).length).toBeGreaterThan(0);
  expect(assetResponses.every((item) => item.status === 200)).toBe(true);

  await page.goto("http://127.0.0.1:8080/#/projects");
  await expect(page.getByRole("heading", { name: "Projects", level: 1 })).toBeVisible();
  await page.reload();
  await expect(page.getByRole("heading", { name: "Projects", level: 1 })).toBeVisible();

  for (const route of ["departments", "projects", "resources", "requirements", "allocations"]) {
    const rows = await getJson<unknown[]>(page, `/api/${route}`);
    expect(Array.isArray(rows), `/api/${route} returns a JSON array`).toBe(true);
  }
});
