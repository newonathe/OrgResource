// Fictional containment probe. Prints enums only. 192.0.2.1 and 2001:db8::1 are documentation-only addresses. The only real names and
// servers asked for are example.com and the local router's resolver; containment must stop both.
const net = require("node:net"), dns = require("node:dns"), dgram = require("node:dgram"), fs = require("node:fs");
const tcp = (host, port) => new Promise(r => {
  let done = false; const s = net.connect({ host, port });
  const f = x => { if (!done) { done = true; s.destroy(); r(x); } };
  s.on("connect", () => f("CONNECTED")); s.on("error", e => f(e.code || "ERROR")); setTimeout(() => f("TIMEOUT"), 5000);
});
(async () => {
  const [mode, arg] = process.argv.slice(2);
  if (mode === "serve") {
    const s = net.createServer(c => { c.on("error", () => {}); c.end("x"); });
    s.listen(Number(arg), "127.0.0.1", () => console.log("SERVING")); setTimeout(() => process.exit(0), 900000); return;
  }
  const { tcpTargets: a, udpTargets, paths, writePaths, dnsServer } = JSON.parse(Buffer.from(arg, "base64").toString("utf8")), out = {};
  const s = net.createServer(c => { c.on("error", () => {}); c.end("x"); }); await new Promise(r => s.listen(0, "127.0.0.1", r));
  out.self = await tcp("127.0.0.1", s.address().port);
  out.external4 = await tcp("192.0.2.1", 443);
  out.external6 = await tcp("2001:db8::1", 443);
  for (const [k, v] of Object.entries(a)) out[k] = await tcp(v.host, v.port);
  const direct = new dns.Resolver({ timeout: 2000, tries: 1 }); if (dnsServer) direct.setServers([dnsServer]);
  out.dnsDirect = await new Promise(r => direct.resolve4("example.com", e => r(e ? e.code : "RESOLVED")));
  out.dnsSystem = await new Promise(r => dns.lookup("example.com", e => r(e ? e.code : "RESOLVED")));
  for (const p of paths) { try { fs.readdirSync(p); out["path " + p] = "READ"; } catch (e) { out["path " + p] = e.code; } }
  for (const p of writePaths) { try { fs.writeFileSync(p + "\\w.tmp", "x"); out["write " + p] = "WRITTEN"; } catch (e) { out["write " + p] = e.code; } }
  // A dropped datagram still reports a successful send on Windows, so only delivery counts: a reply here, or receipt by the host listener.
  const udp = (host, port) => new Promise(r => {
    const u = dgram.createSocket("udp4"); let seen = "NO_REPLY";
    u.on("message", () => { seen = "REPLY"; }); u.on("error", e => { seen = e.code; });
    u.send(Buffer.from("123401000001000000000000076578616d706c6507696e76616c69640000010001", "hex"), port, host, () => {});
    setTimeout(() => { u.close(); r(seen); }, 2000);
  });
  for (const v of udpTargets) out["udp " + v.name] = await udp(v.host, v.port);
  console.log(JSON.stringify(out)); process.exit(0);
})();
