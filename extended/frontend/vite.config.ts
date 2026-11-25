import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react";

const backendPort = process.env.EXT_VITE_BACKEND_PORT ?? "8085";
if (!/^[0-9]+$/.test(backendPort) || Number(backendPort) < 1 || Number(backendPort) > 65535) {
  throw new Error("EXT_VITE_BACKEND_PORT must be a valid loopback port.");
}

export default defineConfig({
  plugins: [react()],
  server: {
    host: "127.0.0.1",
    port: 5173,
    strictPort: true,
    proxy: {
      "/api": {
        target: `http://127.0.0.1:${backendPort}`,
        changeOrigin: false,
      },
    },
  },
  test: {
    include: ["src/**/*.test.{ts,tsx}"],
    environment: "jsdom",
    setupFiles: ["./src/test/setup.ts"],
    clearMocks: true,
  },
});
