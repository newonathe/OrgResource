// Contained renderer preflight: attach, new context, new page, about:blank. No HTTP request and no download.
const { createRequire } = require("node:module");
const { join } = require("node:path");
const { chromium } = createRequire(join(__dirname, "..", "extended", "frontend", "package.json"))("@playwright/test");
(async () => {
  let browser;
  try {
    browser = await chromium.connectOverCDP(process.env.C9_WINDOWS_CDP);
    const page = await (await browser.newContext()).newPage();
    await page.goto("about:blank");
    if (await page.evaluate(() => 6 * 7) !== 42) throw new Error("renderer");
    console.log("C9W_BROWSER_PREFLIGHT_OK");
  } finally { if (browser) await browser.close(); }
})().catch(() => { process.exitCode = 1; });
