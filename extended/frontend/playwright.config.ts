import { defineConfig } from "@playwright/test";

const externalBaseURL = process.env.EXT_E2E_BASE_URL;
const developmentUrl = "http://127.0.0.1:5173";
if (externalBaseURL && !/^http:\/\/127\.0\.0\.1:[0-9]+\/?$/.test(externalBaseURL)) {
  throw new Error("EXT_E2E_BASE_URL must be a dedicated numeric loopback HTTP origin.");
}

export default defineConfig({
  testDir: "./e2e",
  timeout: 30_000,
  fullyParallel: false,
  reporter: "list",
  use: {
    baseURL: externalBaseURL ?? developmentUrl,
    headless: true,
    browserName: "chromium",
    timezoneId: "Asia/Manila",
    trace: "retain-on-failure",
  },
  ...(externalBaseURL ? {} : { webServer: {
    command: "npm run dev -- --host 127.0.0.1",
    url: developmentUrl,
    reuseExistingServer: false,
    timeout: 30_000,
  } }),
});
