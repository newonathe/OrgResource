#!/usr/bin/env node
// SOURCE-ONLY traceability gate. It verifies documentation/inventory parity,
// never HTTP authorization, SQL effects, runtime registration or reviewer signoff.
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const inventory = JSON.parse(fs.readFileSync(path.join(root, "evidence/C8/C8_ROUTE_INVENTORY.json"), "utf8"));
const oracle = fs.readFileSync(path.join(root, "evidence/C8/C8_LEGACY_WORKSPACE_SOURCE_ORACLE_2026-11-23.md"), "utf8");
const reviewedElsewhere = new Set([
  "C1AuthResource", "C1SessionResource", "C1GovernanceResource", "C1InvitationResource",
  "ProjectWorkResource", "ReportingResource", "InventoryResource", "AllocationResource"
]);
const expected = inventory.routes.filter(route => !reviewedElsewhere.has(route.java_class));
if (inventory.expected_route_count !== 93 || inventory.routes.length !== 93 || expected.length !== 29) {
  throw new Error("Unexpected source-route inventory or 29-route partition");
}

const rows = [...oracle.matchAll(/^\|\s*(N\d{2})\s*\|\s*`((?:GET|POST|PUT|PATCH|DELETE) \/api\/[^\x60]+)`\s*\|\s*`([A-Za-z][A-Za-z0-9]*Resource\.[A-Za-z][A-Za-z0-9]*)`\s*\|/gm)];
if (rows.length !== 29) throw new Error("Expected exactly 29 policy-oracle table rows, got " + rows.length);
const expectedKeys = new Map(expected.map(route => [route.verb + " " + route.path,
  route.java_class + "." + route.java_method]));
const seenKeys = new Set(), seenIds = new Set();
for (const [, id, key, handler] of rows) {
  if (!expectedKeys.has(key)) throw new Error("Undeclared or mispartitioned source-policy route: " + key);
  if (seenKeys.has(key) || seenIds.has(id)) throw new Error("Duplicate source-policy route or ID: " + key);
  if (expectedKeys.get(key) !== handler) throw new Error("Source-policy delegate drift: " + key);
  seenKeys.add(key);
  seenIds.add(id);
}
for (let i = 1; i <= 29; i++) {
  if (!seenIds.has("N" + String(i).padStart(2, "0"))) throw new Error("Missing source-policy ID N" + i);
}
for (const key of expectedKeys.keys()) {
  if (!seenKeys.has(key)) throw new Error("Missing source-policy row: " + key);
}
const alreadyMapped = inventory.routes.length - expected.length;
if (alreadyMapped !== 64) throw new Error("Previously source-oracled route partition drift");
console.log("PASS: all 29 remaining policy rows match the 93-route inventory and exact Jersey handlers.");
console.log("LIMIT: source documentation only; no authorization, MySQL effects or independent review proved.");
