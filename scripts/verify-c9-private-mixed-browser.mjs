// Private mixed recovered packaged browser oracle. No mocks, traces or screenshots.
// Only the future owner-gated same-lifetime runner may invoke this.
import { createRequire } from "node:module";
import { lstatSync, realpathSync, readFileSync } from "node:fs";
import { dirname, isAbsolute } from "node:path";

const BASE = "http://127.0.0.1:8085";
const NOTICE = "DEMO ENVIRONMENT — Workspaces identified as demo organizations contain simulated accounts, projects, inventory and activity. Existing untagged workspaces retain their own data. Public directory labels do not imply participation, adoption or endorsement.";
let phase = "INTAKE", browser;
function requireTrue(ok) { if (!ok) throw new Error("oracle mismatch"); }
function step(value) { phase = value; console.log("R06B3B_MIXED_BROWSER_PHASE:" + value); }
function equal(a, b) { return JSON.stringify(a) === JSON.stringify(b); }
function privateExpected() {
  requireTrue(process.platform === "linux" && process.getuid() > 0 && process.env.GITHUB_ACTIONS !== "true");
  requireTrue(process.env.C9_RUNTIME_ISOLATED === "true" && process.env.C9_CONTROL_PRIVATE_MIXED === "true");
  requireTrue(!process.env.DEBUG && !process.env.PWDEBUG && !process.env.PLAYWRIGHT_HTML_REPORT);
  const path = process.env.C9_MIXED_EXPECTED;
  requireTrue(isAbsolute(path) && realpathSync(path) === path);
  const file = lstatSync(path), parent = lstatSync(dirname(path));
  requireTrue(file.isFile() && file.uid === process.getuid() && (file.mode & 0o777) === 0o600 && file.size > 0 && file.size <= 1024 * 1024);
  requireTrue(parent.isDirectory() && parent.uid === process.getuid() && (parent.mode & 0o777) === 0o700);
  const doc = JSON.parse(readFileSync(path, "utf8"));
  requireTrue(doc.metadata.synthetic === true && doc.metadata.notice === NOTICE);
  requireTrue(/^orgresource-c8-plan-v1-[0-9a-f]{12}-[0-9a-f]{12}$/.test(doc.metadata.datasetId));
  requireTrue(/^\d{4}-\d{2}-\d{2}$/.test(doc.metadata.demonstrationAsOf));
  const ids = doc.ownerOrgIds;
  const uuid = x => typeof x === "string" && /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/.test(x);
  requireTrue(Array.isArray(ids) && ids.length === 97 && new Set(ids).size === 97 && ids.every(uuid));
  requireTrue(uuid(doc.foreignOrgId) && !ids.includes(doc.foreignOrgId));
  requireTrue(doc.original.id !== doc.demo.id && doc.original.displayName === "Isolated Original Workspace");
  for (const org of [doc.original, doc.demo]) {
    requireTrue(ids.includes(org.id) && typeof org.slug === "string" && org.slug.length > 0);
    requireTrue(Array.isArray(org.workspaceIds) && org.workspaceIds.length > 0 && org.workspaceIds.length <= 100);
    requireTrue(org.workspaceIds.every(x => Number.isSafeInteger(x) && x > 0) && new Set(org.workspaceIds).size === org.workspaceIds.length);
    requireTrue(Array.isArray(org.modernIds) && org.modernIds.length === org.workspaceIds.length && org.modernIds.every(uuid) && new Set(org.modernIds).size === org.modernIds.length);
  }
  requireTrue(doc.original.workspaceIds.length === 1 && doc.original.detail.name === "Existing fictional projector");
  return doc;
}
try {
  const expected = privateExpected();
  const require = createRequire(new URL("../extended/frontend/package.json", import.meta.url));
  const { chromium } = require("@playwright/test");
  requireTrue(typeof process.env.C9_RUNTIME_OWNER_PASSWORD === "string" && process.env.C9_RUNTIME_OWNER_PASSWORD.length > 0);
  browser = await chromium.launch({headless: true});
  const context = await browser.newContext();
  const page = await context.newPage();
  page.setDefaultTimeout(30000);
  let classified = false, early = false, badMetadata = false, unexpectedOrigin = false, unexpectedWrite = false;
  const metadataResponses = [];
  page.on("response", res => {
    const url = new URL(res.url());
    if (url.origin === BASE && url.pathname === "/api/v2/demo/metadata") {
      metadataResponses.push(res);
      if (res.status() === 200) classified = true;
      else badMetadata = true;
    }
  });
  page.on("request", req => {
    const url = new URL(req.url());
    if (["http:", "https:"].includes(url.protocol) && url.origin !== BASE) unexpectedOrigin = true;
    if (url.pathname.startsWith("/api/") && url.pathname !== "/api/v2/demo/metadata" && !classified) early = true;
    if (url.pathname.startsWith("/api/") && !["GET","HEAD"].includes(req.method()) &&
        !(req.method() === "POST" && ["/api/v2/auth/login","/api/v2/session/active-org","/api/v2/auth/logout"].includes(url.pathname))) unexpectedWrite = true;
  });
  function clean() { requireTrue(classified && !early && !badMetadata && !unexpectedOrigin && !unexpectedWrite); }
  async function notice() {
    clean();
    const banner = page.locator(".demo-disclosure");
    requireTrue(await banner.count() === 1 && await banner.isVisible() && await banner.innerText() === NOTICE);
  }
  const waitResponse = (path, method) => page.waitForResponse(res => {
    const url = new URL(res.url());
    return url.origin === BASE && url.pathname === path && res.request().method() === method;
  });
  const nav = () => page.getByRole("navigation", {name:"Main navigation"});
  const selector = () => page.locator("header select[aria-label='Switch organization']");
  async function selectAction(action, org) {
    const [res] = await Promise.all([waitResponse("/api/v2/session/active-org", "POST"), action()]);
    requireTrue(res.status() === 200 && (await res.json()).data.activeOrgId === org.id);
    await nav().waitFor();
    await page.waitForFunction(id => document.querySelector("header select[aria-label='Switch organization']")?.value === id, org.id);
    const ids = await selector().locator("option").evaluateAll(options => options.map(o => o.value).sort());
    requireTrue(equal(ids, [...expected.ownerOrgIds].sort()));
    await notice();
  }
  async function verifyResources(org, foreign) {
    const [res] = await Promise.all([
      page.waitForResponse(r => {
        const url = new URL(r.url());
        return url.origin === BASE && url.pathname === "/api/v2/workspace/resources" && r.request().method() === "GET" && !url.search;
      }),
      nav().getByRole("link",{name:"Resource Library",exact:true}).click()
    ]);
    requireTrue(res.status() === 200);
    const data = (await res.json()).data;
    const ids = org.workspaceIds.slice(0,50);
    requireTrue(equal(data.items.map(x => x.id), ids));
    requireTrue(equal(data.items.map(x => x.publicId), org.modernIds.slice(0,50)));
    requireTrue(ids.every(x => !foreign.workspaceIds.includes(x)));
    requireTrue(org.workspaceIds.length <= 50 ? data.nextCursor === null : typeof data.nextCursor === "string" && data.nextCursor.length > 0);
    await page.getByRole("heading",{name:"Resource Library",exact:true}).waitFor();
    // Numeric displayed IDs are independent SQL expectations, not merely
    // text copied from the API. Require exact visible rows, not only one match.
    const renderedIds = ids.map(id => "R" + String(id).padStart(3,"0"));
    await page.waitForFunction(wanted => {
      const actual = [...document.querySelectorAll(".resource-table tbody .record-detail-button small")].map(x => x.textContent);
      return JSON.stringify(actual) === JSON.stringify(wanted);
    }, renderedIds);
    const rows = page.locator(".resource-table tbody tr");
    requireTrue(await rows.count() === ids.length);
    const names = await rows.locator(".record-detail-button strong").allTextContents();
    requireTrue(equal(names, data.items.map(x => x.name)));
    if (org.id === expected.original.id) {
      const item = data.items[0];
      requireTrue(item.name === expected.original.detail.name && item.totalQuantity === 0 && item.availableQuantity === 0);
      requireTrue(names[0] === "Existing fictional projector");
    }
    await notice();
  }
  step("METADATA");
  const html = await page.goto(BASE + "/#/login");
  requireTrue(html && html.status() === 200);
  await page.getByRole("heading",{name:"Welcome back"}).waitFor();
  requireTrue(metadataResponses.length === 1);
  const metadata = (await metadataResponses[0].json()).data;
  requireTrue(metadata.synthetic === true && metadata.datasetId === expected.metadata.datasetId &&
    metadata.demonstrationAsOf === expected.metadata.demonstrationAsOf && metadata.notice === NOTICE);
  await notice();
  step("OWNER_LOGIN");
  await page.getByLabel("Username").fill("additive-original-owner");
  await page.getByLabel("Password").fill(process.env.C9_RUNTIME_OWNER_PASSWORD);
  const [login] = await Promise.all([waitResponse("/api/v2/auth/login","POST"),
    page.getByRole("button",{name:"Sign in",exact:true}).click()]);
  requireTrue(login.status() === 200 && (await login.json()).data.session.activeOrgId === null);
  const search = page.getByLabel("Find organization");
  await search.waitFor();
  await search.fill(expected.original.slug);
  const originalChoice = page.locator(".organization-choice").filter({has:page.getByRole("heading",{name:expected.original.displayName,exact:true})});
  step("ORIGINAL_WORKSPACE");
  await selectAction(() => originalChoice.getByRole("button",{name:"Open workspace",exact:true}).click(), expected.original);
  await verifyResources(expected.original, expected.demo);
  // Move off the resource screen first so a new resource request cannot
  // accidentally resolve the next wait before the tenant switch completes.
  await nav().getByRole("link",{name:"Overview",exact:true}).click();
  await page.waitForURL("**/#/");
  step("DEMO_SWITCH");
  await selectAction(() => selector().selectOption(expected.demo.id), expected.demo);
  await verifyResources(expected.demo, expected.original);
  step("LOGOUT");
  const [logout] = await Promise.all([waitResponse("/api/v2/auth/logout","POST"),
    page.locator(".sign-out-button").click()]);
  requireTrue(logout.status() === 204);
  await page.getByRole("heading",{name:"Welcome back"}).waitFor();
  const unauthenticated = await page.evaluate(async () => (await fetch("/api/v2/session",{cache:"no-store",credentials:"same-origin"})).status);
  requireTrue(unauthenticated === 401);
  await notice();
  await context.close();
  console.log("R06B3B_MIXED_BROWSER_OK:exact_metadata=true no_early_api=true exact_owner_options=true original_demo_rows=true signout=true");
} catch {
  // Playwright errors can embed names/URLs/locator text. Never print them.
  console.error("R06B3B_MIXED_BROWSER_FAILED:" + phase);
  process.exitCode = 1;
} finally {
  if (browser) {
    try { await browser.close(); }
    catch { console.error("R06B3B_MIXED_BROWSER_CLOSE_FAILED"); process.exitCode = 1; }
  }
}
