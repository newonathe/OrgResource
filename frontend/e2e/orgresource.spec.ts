import { expect, test } from "@playwright/test";

test("Department and Resource workflows persist through the running backend", async ({ page, request }) => {
  const unique = `E2E ${Date.now()}`;
  const departmentName = `${unique} department`;
  const resourceName = `${unique} reusable`;

  await page.goto("/");
  await expect(page.getByRole("heading", { name: "Departments", exact: true })).toBeVisible();
  await page.getByLabel("Department name").fill(departmentName);
  await page.getByRole("button", { name: "Add department" }).click();
  await expect(page.getByRole("button", { name: `Edit ${departmentName}` })).toBeVisible();
  page.once("dialog", (dialog) => dialog.accept());
  await page.getByRole("button", { name: `Delete ${departmentName}` }).click();
  await expect(page.getByText("Department deleted.")).toBeVisible();

  await page.getByRole("link", { name: "Resources" }).click();
  await page.getByLabel("Resource name").fill(resourceName);
  await page.getByLabel("Resource type").selectOption("REUSABLE");
  await page.getByLabel("Initial total quantity").fill("5");
  await page.getByRole("button", { name: "Add resource" }).click();
  const row = page.getByRole("row", { name: new RegExp(resourceName) });
  await expect(row).toBeVisible();

  const response = await request.get("/api/resources");
  expect(response.ok()).toBeTruthy();
  const resources = await response.json() as Array<{ id: number; name: string; totalQuantity: number }>;
  const saved = resources.find((resource) => resource.name === resourceName);
  expect(saved).toBeDefined();
  const changed = await request.post(`/api/resources/${saved!.id}/update`, {
    data: { name: resourceName, totalQuantity: 6, expectedTotalQuantity: 5 },
  });
  expect(changed.status()).toBe(200);

  await row.getByRole("button", { name: `Edit ${resourceName}` }).click();
  await page.getByLabel("Name", { exact: true }).fill(`${resourceName} edited`);
  await page.getByLabel("Total quantity", { exact: true }).fill("4");
  await page.getByRole("button", { name: "Save changes" }).click();
  await expect(page.getByText(/server quantity changed after this row loaded/i)).toBeVisible();
  await expect(page.getByText(/Current server total: 6/)).toBeVisible();
  await expect(page.getByRole("button", { name: "Save changes" })).toBeDisabled();
  await page.getByRole("button", { name: "I reviewed these current values" }).click();
  await page.getByRole("button", { name: "Save changes" }).click();
  await expect(page.getByText("Resource updated.")).toBeVisible();
  await expect(page.getByRole("row", { name: new RegExp(`${resourceName} edited`) })).toBeVisible();

  const refreshed = await request.get("/api/resources");
  const afterUpdate = (await refreshed.json() as Array<{ id: number; name: string; totalQuantity: number }>)
    .find((resource) => resource.id === saved!.id);
  expect(afterUpdate?.totalQuantity).toBe(4);

  await page.getByRole("button", { name: `Edit ${resourceName} edited` }).click();
  await page.getByLabel("Total quantity", { exact: true }).fill("0");
  await page.getByRole("button", { name: "Save changes" }).click();
  await expect(page.getByText("Resource updated.")).toBeVisible();
  page.once("dialog", (dialog) => dialog.accept());
  await page.getByRole("button", { name: `Delete ${resourceName} edited` }).click();
  await expect(page.getByText("Resource deleted.")).toBeVisible();
});
