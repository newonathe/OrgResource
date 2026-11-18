import { defineConfig } from "@playwright/test";

const externalBaseURL = process.env.EXT_E2E_BASE_URL;

export default defineConfig({
  testDir: "./e2e",
  timeout: 30_000,
  fullyParallel: false,
  reporter: "list",
  use: {
    baseURL: externalBaseURL ?? "http://127.0.0.1:8085",
    headless: true,
    browserName: "chromium",
    timezoneId: "Asia/Manila",
    trace: "retain-on-failure",
  },
  ...(externalBaseURL ? {} : { webServer: {
    command: "npm run dev -- --host 127.0.0.1",
    url: "http://127.0.0.1:5173",
    reuseExistingServer: !process.env.CI,
    timeout: 30_000,
  } }),
});
