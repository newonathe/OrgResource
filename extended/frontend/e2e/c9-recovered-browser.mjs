// Actual recovered packaged assets and real mixed-cohort metadata, no interception.
import { chromium } from "@playwright/test";
if (process.env.C9_RUNTIME_ISOLATED !== "true") throw new Error("C9_RUNTIME_BROWSER_GUARD");
const browser = await chromium.launch({headless: true});
try {
  const page = await browser.newPage();
  let metadata503 = 0;
  let metadataUnexpected = 0;
  let protectedRequests = 0;
  page.on("response", response => {
    if (new URL(response.url()).pathname === "/api/v2/demo/metadata") {
      if (response.status() === 503) metadata503++;
      else metadataUnexpected++;
    }
  });
  page.on("request", request => {
    const path = new URL(request.url()).pathname;
    if (path.startsWith("/api/") && path !== "/api/v2/demo/metadata") protectedRequests++;
  });
  const html = await page.goto("http://127.0.0.1:8085/#/login");
  if (!html || html.status() !== 200) throw new Error("C9_RUNTIME_BROWSER_HTML");
  await page.locator(".workspace-gate").waitFor({timeout: 15000});
  // Real application retry budget is 120 s. Do not mock or alter it.
  await page.getByRole("alert").filter({hasText: "The server could not confirm the workspace classification"}).waitFor({timeout: 135000});
  if (metadata503 < 2 || metadataUnexpected !== 0 || protectedRequests !== 0 ||
      await page.getByRole("navigation", {name: "Main navigation"}).count() !== 0 ||
      await page.getByRole("heading", {name: "Welcome back"}).count() !== 0) {
    throw new Error("C9_RUNTIME_BROWSER_FAIL_CLOSED");
  }
  console.log("C9_RUNTIME_BROWSER_FAIL_CLOSED_OK: actual_metadata_503=" + metadata503 + " protected_api_requests=0");
} finally {
  await browser.close();
}
