import { readFileSync, writeFileSync } from "node:fs";

const [original, destination] = process.argv.slice(2);
if (!original || !destination) throw new Error("Pass the frozen Course A collection and a new output path.");
const collection = JSON.parse(readFileSync(original, "utf8"));
let changes = 0;

function visit(item) {
  for (const event of item.event ?? []) {
    if (event.listen !== "prerequest") continue;
    event.script.exec = event.script.exec.map(line => {
      if (!line.startsWith("function pht(d){return new Intl.DateTimeFormat(")) return line;
      changes++;
      // The frozen collection uses Manila civil time. Newman 6 has no Intl in its sandbox.
      return "function pht(d){return new Date(d.getTime()+8*60*60*1000).toISOString().slice(0,19);}";
    });
  }
  for (const child of item.item ?? []) visit(child);
}

visit(collection);
if (changes !== 1) throw new Error("The exact Course A Manila compatibility point changed.");
writeFileSync(destination, JSON.stringify(collection), { flag: "wx" });
