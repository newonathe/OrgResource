import "@testing-library/jest-dom/vitest";
import { beforeEach } from "vitest";

Object.defineProperty(window, "matchMedia", {
  configurable: true,
  value: (media: string): MediaQueryList => ({
    matches: false,
    media,
    onchange: null,
    addListener: () => undefined,
    removeListener: () => undefined,
    addEventListener: () => undefined,
    removeEventListener: () => undefined,
    dispatchEvent: () => false,
  }),
});

beforeEach(() => window.localStorage.clear());
