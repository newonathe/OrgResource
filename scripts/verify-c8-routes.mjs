#!/usr/bin/env node
// Static inventory check. Does not prove runtime Jersey registration or authorization.
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const sourceDir = path.join(root, "extended/backend/src/main/java/edu/orgresource/extended/rest");
const snapshot = JSON.parse(fs.readFileSync(path.join(root, "evidence/C8/C8_ROUTE_INVENTORY.json"), "utf8"));
const routes = [];
for (const file of fs.readdirSync(sourceDir).filter(file => file.endsWith("Resource.java")).sort()) {
  const source = fs.readFileSync(path.join(sourceDir, file), "utf8");
  const classMatch = source.match(/\bpublic\s+class\s+\w+/);
  if (!classMatch) throw new Error("No public Jersey class in " + file);
  const classPath = source.slice(0, classMatch.index).match(/@Path\("([^"]+)"\)/)?.[1];
  if (!classPath) throw new Error("Missing class @Path in " + file);
  const lines = source.split(/\r?\n/);
  for (let i = 0; i < lines.length; i++) {
    const verb = lines[i].match(/@(GET|POST|PUT|DELETE|PATCH)\b/)?.[1];
    if (!verb) continue;
    const fragment = lines.slice(i, i + 16).join("\n");
    const signature = fragment.match(/\bpublic\s+[^\n(]+\s+([A-Za-z_]\w*)\s*\(/);
    if (!signature) throw new Error("Missing route method in " + file + ":" + (i + 1));
    const methodPath = fragment.slice(0, signature.index).match(/@Path\("([^"]+)"\)/)?.[1] ?? "";
    routes.push({verb, path: "/api" + classPath + methodPath, java_class: file.slice(0, -5), java_method: signature[1]});
  }
}
const key = r => [r.path, r.verb, r.java_class, r.java_method].join("\0");
routes.sort((a, b) => key(a).localeCompare(key(b)));
if (snapshot.schema_version !== 1 || snapshot.expected_route_count !== 93 || routes.length !== 93) {
  throw new Error("C8 route count mismatch, found " + routes.length);
}
if (new Set(routes.map(r => r.verb + " " + r.path)).size !== routes.length) {
  throw new Error("Duplicate C8 HTTP method/path");
}
if (JSON.stringify(routes) !== JSON.stringify(snapshot.routes)) {
  const old = snapshot.routes.map(key), current = routes.map(key);
  console.error("C8 route drift: " + JSON.stringify({
    removed: old.filter(k => !current.includes(k)),
    added: current.filter(k => !old.includes(k))
  }, null, 2));
  process.exitCode = 1;
} else {
  console.log("PASS: 93 source-declared C8 Jersey operations match tracked inventory.");
  console.log("LIMIT: No verification of runtime registration, policy, or authorization.");
}
