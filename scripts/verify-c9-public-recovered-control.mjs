// Public original-only real packaged browser control. No mocks or traces.
import { createRequire } from "node:module";
const require = createRequire(new URL("../extended/frontend/package.json", import.meta.url));
const { chromium } = require("@playwright/test");
if (process.env.C9_RUNTIME_ISOLATED !== "true" || process.env.C9_CONTROL_PUBLIC_ONLY !== "true") throw new Error("R06B3B_BROWSER_GUARD");
let browser;
try {
  browser = await chromium.launch({headless: true});
  const page = await browser.newPage();
  let classified = false, early = false, badMetadata = false;
  page.on("response", res => {
    if (new URL(res.url()).pathname === "/api/v2/demo/metadata") {
      if (res.status() === 200) classified = true;
      else badMetadata = true;
    }
  });
  page.on("request", req => {
    const path = new URL(req.url()).pathname;
    if (path.startsWith("/api/") && path !== "/api/v2/demo/metadata" && !classified) early = true;
  });
  const html = await page.goto("http://127.0.0.1:8085/#/login");
  if (!html || html.status() !== 200) throw new Error("HTML");
  await page.getByRole("heading", {name:"Welcome back"}).waitFor({timeout:30000});
  if (!classified || early || badMetadata || await page.locator(".demo-disclosure").count()) throw new Error("CLASSIFICATION");
  await page.getByLabel("Username").fill("additive-original-owner");
  await page.getByLabel("Password").fill(process.env.C9_RUNTIME_OWNER_PASSWORD);
  await page.getByRole("button", {name:"Sign in",exact:true}).click();
  const choice = page.locator(".organization-choice").filter({hasText:"Isolated Original Workspace"});
  await choice.getByRole("button", {name:"Open workspace"}).click({timeout:30000});
  await page.getByRole("navigation",{name:"Main navigation"}).waitFor({timeout:30000});
  const response = page.waitForResponse(r => new URL(r.url()).pathname === "/api/v2/workspace/resources", {timeout:30000});
  await page.getByRole("navigation",{name:"Main navigation"}).getByRole("link",{name:"Resource Library"}).click();
  const res = await response;
  const data = (await res.json()).data;
  if (res.status() !== 200 || !Array.isArray(data?.items) || data.items.length !== 1 || data.items[0].name !== "Existing fictional projector") throw new Error("WORKSPACE_RESOURCE");
  await page.getByRole("heading",{name:"Resource Library"}).waitFor();
  await page.getByText("Existing fictional projector",{exact:true}).first().waitFor();
  await page.locator(".sign-out-button").click();
  await page.getByRole("heading",{name:"Welcome back"}).waitFor({timeout:30000});
  if (early || badMetadata) throw new Error("GATE");
  console.log("R06B3B_CONTROL_BROWSER_OK:real_metadata_200=true no_early_api=true original_resource=true signout=true");
} catch {
  console.error("R06B3B_CONTROL_BROWSER_FAILED");
  process.exitCode = 1;
} finally {
  if (browser) await browser.close();
}
