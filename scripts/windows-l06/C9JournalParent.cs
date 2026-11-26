using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Security.Cryptography;
using System.Text;
using System.Text.Json;
using System.Threading;
using System.Threading.Tasks;

// H03 parent-only journal orchestration, source candidate.
// Pure Python child receives a snapshot and computes a proposed transition.
// Only the lock-owning parent publishes bytes; child exit after parent death cannot publish.
// Trusted Python installation and exact source provenance are still required.
public sealed class C9JournalParent {
    readonly C9ControlLock owner;
    readonly string python, bridge, journal, pythonSha, bridgeSha, journalSha;
    readonly bool isV2;
    readonly string journalV2, journalV2Sha;
    readonly C9LogonContext initialContext;
    const int MaxEnvelope = 65536;
    const string RequestSchema = "orgresource.windows-l06-ownership-bridge.v1";
    const string TransportSchema = "orgresource.windows-l06-ownership-transform.v1";

    public C9JournalParent(C9ControlLock owner, string python, string pythonSha,
        string bridge, string bridgeSha, string journalSha)
        : this(owner, python, pythonSha, bridge, bridgeSha, journalSha, null, false) {}

    public C9JournalParent(C9ControlLock owner, string python, string pythonSha,
        string bridge, string bridgeSha, string journalSha, string journalV2Sha)
        : this(owner, python, pythonSha, bridge, bridgeSha, journalSha, journalV2Sha, true) {}

    C9JournalParent(C9ControlLock owner, string python, string pythonSha,
        string bridge, string bridgeSha, string journalSha, string journalV2Sha, bool isV2) {
        this.owner = owner ?? throw new ArgumentNullException(nameof(owner));
        this.python = python; this.pythonSha = pythonSha;
        this.bridge = bridge; this.bridgeSha = bridgeSha;
        this.journal = Path.Combine(Path.GetDirectoryName(bridge), "ownership_journal.py");
        this.journalSha = journalSha;
        owner.AssertHeld();
        this.isV2 = isV2;
        if (Path.GetFileName(bridge) != (isV2 ? "ownership_bridge_v2.py" : "ownership_bridge.py"))
            throw new InvalidOperationException("C9W_JOURNAL_TOOL");
        if (isV2) {
            if (!Digest(journalV2Sha)) throw new InvalidOperationException("C9W_JOURNAL_TOOL");
            journalV2 = Path.Combine(Path.GetDirectoryName(bridge), "ownership_journal_v2.py");
            this.journalV2Sha = journalV2Sha;
            initialContext = C9LogonContext.Read();
        }
    }

    internal void AssertCurrentContext() {
        owner.AssertHeld();
        if (isV2 && !initialContext.SameContext(C9LogonContext.Read()))
            throw new InvalidOperationException("C9W_JOURNAL_CONTEXT_CHANGED");
    }

    object ContextRecord() {
        return new { userSid = initialContext.UserSid, authenticationId = initialContext.AuthenticationId,
            sessionId = initialContext.SessionId, logonTime = initialContext.LogonTime, bootId = initialContext.BootId };
    }

    void CheckRecord(JsonElement record) {
        Strict(record);
        JsonElement bound = record;
        if (isV2) {
            Shape(record, "schema","context","ownership","profileStorage","mappingBinding","removalEvidence");
            if (Text(record,"schema") != "orgresource.windows-l06-ownership.v2")
                throw new InvalidOperationException("C9W_JOURNAL_BINDING");
            var ctx = record.GetProperty("context");
            Shape(ctx,"userSid","authenticationId","sessionId","logonTime","bootId");
            if (Text(ctx,"userSid") != initialContext.UserSid ||
                Text(ctx,"authenticationId") != initialContext.AuthenticationId ||
                !ctx.GetProperty("sessionId").TryGetUInt32(out uint session) || session != initialContext.SessionId ||
                Text(ctx,"logonTime") != initialContext.LogonTime ||
                ctx.GetProperty("bootId").ValueKind != JsonValueKind.Null)
                throw new InvalidOperationException("C9W_JOURNAL_BINDING");
            bound = record.GetProperty("ownership");
        }
        Shape(bound,"schema","token","root","rootId","controlId","work","profiles","mapping");
        if (Text(bound,"schema") != "orgresource.windows-l06-ownership.v1" ||
            Text(bound,"token") != owner.Token || Text(bound,"root") != owner.Root ||
            Text(bound,"rootId") != owner.RootId || Text(bound,"controlId") != owner.ControlId)
            throw new InvalidOperationException("C9W_JOURNAL_BINDING");
        if (isV2) {
            // This parent is intentionally limited to the present no-mapping/no-work-removal slice.
            if (Text(bound.GetProperty("mapping"),"state") != "none" ||
                record.GetProperty("mappingBinding").ValueKind != JsonValueKind.Null ||
                Text(bound.GetProperty("work"),"state") == "removing" ||
                Text(bound.GetProperty("work"),"state") == "removed")
                throw new InvalidOperationException("C9W_JOURNAL_EFFECT_REFUSED");
        }
    }

    static string Sha(byte[] bytes) { return Convert.ToHexString(SHA256.HashData(bytes)).ToLowerInvariant(); }
    static bool Digest(string value) {
        return value != null && System.Text.RegularExpressions.Regex.IsMatch(value, @"\A[a-f0-9]{64}\z");
    }

    static FileStream Pin(string path, string expected) {
        if (!Digest(expected) || !Path.IsPathFullyQualified(path) ||
            !String.Equals(Path.GetFullPath(path), path, StringComparison.Ordinal) ||
            (File.GetAttributes(path) & (FileAttributes.ReparsePoint | FileAttributes.Directory | FileAttributes.Offline)) != 0)
            throw new InvalidOperationException("C9W_JOURNAL_TOOL");
        var s = new FileStream(path, FileMode.Open, FileAccess.Read, FileShare.Read);
        try {
            if (Convert.ToHexString(SHA256.HashData(s)).ToLowerInvariant() != expected)
                throw new InvalidOperationException("C9W_JOURNAL_TOOL");
            return s;
        } catch { s.Dispose(); throw; }
    }

    static void Strict(JsonElement v) {
        if (v.ValueKind == JsonValueKind.Object) {
            var seen = new HashSet<string>(StringComparer.OrdinalIgnoreCase);
            foreach (var p in v.EnumerateObject()) {
                if (!seen.Add(p.Name)) throw new InvalidOperationException("C9W_JOURNAL_JSON");
                Strict(p.Value);
            }
        } else if (v.ValueKind == JsonValueKind.Array) {
            foreach (var item in v.EnumerateArray()) Strict(item);
        }
    }

    static void Shape(JsonElement v, params string[] names) {
        if (v.ValueKind != JsonValueKind.Object) throw new InvalidOperationException("C9W_JOURNAL_JSON");
        var expected = new HashSet<string>(names, StringComparer.Ordinal);
        foreach (var p in v.EnumerateObject())
            if (!expected.Remove(p.Name)) throw new InvalidOperationException("C9W_JOURNAL_JSON");
        if (expected.Count != 0) throw new InvalidOperationException("C9W_JOURNAL_JSON");
    }

    static string Text(JsonElement v, string key) {
        var item = v.GetProperty(key);
        if (item.ValueKind != JsonValueKind.String) throw new InvalidOperationException("C9W_JOURNAL_JSON");
        return item.GetString();
    }

    static async Task<byte[]> Drain(Stream stream, int max, CancellationToken ct) {
        using var output = new MemoryStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = await stream.ReadAsync(buf.AsMemory(), ct).ConfigureAwait(false)) != 0) {
            if (output.Length + n > max) throw new InvalidOperationException("C9W_JOURNAL_OUTPUT");
            output.Write(buf, 0, n);
        }
        return output.ToArray();
    }

    async Task<byte[]> Compute(byte[] input) {
        using var pinPythonDir = C9DirectoryIdentity.Open(Path.GetDirectoryName(python));
        using var pinSourceDir = C9DirectoryIdentity.Open(Path.GetDirectoryName(bridge));
        using var p1 = Pin(python, pythonSha);
        using var p2 = Pin(bridge, bridgeSha);
        using var p3 = Pin(journal, journalSha);
        using var p4 = isV2 ? Pin(journalV2, journalV2Sha) : null;
        var psi = new ProcessStartInfo(python) {
            UseShellExecute = false, CreateNoWindow = true,
            RedirectStandardInput = true, RedirectStandardOutput = true, RedirectStandardError = true,
            WorkingDirectory = owner.ControlPath
        };
        psi.ArgumentList.Add("-I");
        if (isV2) psi.ArgumentList.Add("-S"); // suppress site initialization on this new transport
        psi.ArgumentList.Add("-B"); psi.ArgumentList.Add(bridge);
        psi.Environment.Clear();
        string system = Environment.GetEnvironmentVariable("SystemRoot");
        string drive = Environment.GetEnvironmentVariable("SystemDrive");
        if (String.IsNullOrEmpty(system) || String.IsNullOrEmpty(drive))
            throw new InvalidOperationException("C9W_JOURNAL_TOOL");
        psi.Environment["SystemRoot"] = system;
        psi.Environment["SystemDrive"] = drive;
        using var child = Process.Start(psi) ?? throw new InvalidOperationException("C9W_JOURNAL_CHILD");
        using var deadline = new CancellationTokenSource(TimeSpan.FromSeconds(20));
        try {
            var output = Drain(child.StandardOutput.BaseStream, MaxEnvelope + 1, deadline.Token);
            var error = Drain(child.StandardError.BaseStream, 256, deadline.Token);
            await child.StandardInput.BaseStream.WriteAsync(input.AsMemory(), deadline.Token).ConfigureAwait(false);
            child.StandardInput.Close();
            await child.WaitForExitAsync(deadline.Token).ConfigureAwait(false);
            byte[] result = await output.ConfigureAwait(false);
            byte[] stderr = await error.ConfigureAwait(false);
            if (child.ExitCode != 0 || stderr.Length != 0 || result.Length == 0)
                throw new InvalidOperationException("C9W_JOURNAL_CHILD");
            return result;
        } catch {
            // Only this owned process is signaled. No PID search or tree-wide kill.
            try { if (!child.HasExited) { child.Kill(); child.WaitForExit(5000); } } catch { }
            throw new InvalidOperationException("C9W_JOURNAL_CHILD");
        }
    }

    // Single-threaded parent. An uncertain result requires refusal, never automatic retry.
    public string Invoke(string operation, string detailsJson = "{}") {
        lock (owner) {
            AssertCurrentContext();
            if (detailsJson == null || Encoding.UTF8.GetByteCount(detailsJson) > 32768)
                throw new InvalidOperationException("C9W_JOURNAL_JSON");
            using var details = JsonDocument.Parse(detailsJson, new JsonDocumentOptions { MaxDepth = 16 });
            Strict(details.RootElement);
            string[] fields;
            switch (operation) {
                case "init": case "read": fields = Array.Empty<string>(); break;
                case "transition": fields = new[] {"expectedSha256","resource","state","fields"}; break;
                case "plan": fields = new[] {"expectedSha256","observed"}; break;
                default: throw new InvalidOperationException("C9W_JOURNAL_JSON");
            }
            Shape(details.RootElement, fields);
            if (isV2 && (operation == "plan" || (operation == "transition" &&
                (Text(details.RootElement,"resource") == "mapping" ||
                 (Text(details.RootElement,"resource") == "work" &&
                  Text(details.RootElement,"state") != "intent" && Text(details.RootElement,"state") != "created")))))
                throw new InvalidOperationException("C9W_JOURNAL_EFFECT_REFUSED");
            var request = new Dictionary<string,object> {
                ["schema"] = isV2 ? "orgresource.windows-l06-ownership-bridge.v2" : RequestSchema, ["operation"] = operation, ["token"] = owner.Token,
                ["root"] = owner.Root, ["rootId"] = owner.RootId, ["controlId"] = owner.ControlId
            };
            if (isV2) request.Add("context", ContextRecord());
            foreach (var p in details.RootElement.EnumerateObject()) request.Add(p.Name, p.Value.Clone());
            byte[] current = owner.ReadSnapshot();
            if (operation == "init" ? current != null : current == null)
                throw new InvalidOperationException("C9W_JOURNAL_STATE");
            if (isV2 && current != null) {
                using var previous = JsonDocument.Parse(current, new JsonDocumentOptions { MaxDepth = 16 });
                CheckRecord(previous.RootElement); // no v1 conversion, before any child launch
            }
            if ((operation == "transition" || operation == "plan") &&
                Text(details.RootElement, "expectedSha256") != Sha(current))
                throw new InvalidOperationException("C9W_JOURNAL_STALE");
            byte[] input = JsonSerializer.SerializeToUtf8Bytes(new {
                schema = isV2 ? "orgresource.windows-l06-ownership-transform.v2" : TransportSchema,
                request, current = current == null ? null : Convert.ToBase64String(current)
            });
            if (input.Length > MaxEnvelope) throw new InvalidOperationException("C9W_JOURNAL_JSON");
            byte[] raw = Compute(input).GetAwaiter().GetResult();
            AssertCurrentContext();
            using var result = JsonDocument.Parse(raw, new JsonDocumentOptions { MaxDepth = 16 });
            var top = result.RootElement;
            Strict(top); Shape(top, "replacement", "response");
            var response = top.GetProperty("response");
            var replacement = top.GetProperty("replacement");
            Shape(response, operation == "read" ? new[] {"sha256","journal"} :
                operation == "plan" ? new[] {"sha256","plan"} : new[] {"sha256"});
            string digest = Text(response, "sha256");
            if (!Digest(digest)) throw new InvalidOperationException("C9W_JOURNAL_JSON");
            if (operation == "init" || operation == "transition") {
                if (replacement.ValueKind != JsonValueKind.String)
                    throw new InvalidOperationException("C9W_JOURNAL_JSON");
                string encoded = replacement.GetString();
                if (encoded.Length > 21848) throw new InvalidOperationException("C9W_JOURNAL_JSON");
                byte[] bytes = Convert.FromBase64String(encoded);
                if (bytes.Length == 0 || bytes.Length > 16384 ||
                    Convert.ToBase64String(bytes) != encoded || Sha(bytes) != digest)
                    throw new InvalidOperationException("C9W_JOURNAL_JSON");
                using var parsed = JsonDocument.Parse(bytes, new JsonDocumentOptions { MaxDepth = 16 });
                var record = parsed.RootElement;
                CheckRecord(record);
                AssertCurrentContext();
                owner.PublishSnapshot(current, bytes);
                AssertCurrentContext();
            } else {
                if (replacement.ValueKind != JsonValueKind.Null || digest != Sha(current) ||
                    !owner.ReadSnapshot().AsSpan().SequenceEqual(current))
                    throw new InvalidOperationException("C9W_JOURNAL_STALE");
                if (isV2 && operation == "read") {
                    var record = response.GetProperty("journal");
                    CheckRecord(record);
                    // Canonical UTF-8 from pure v2 child must equal actual held snapshot.
                    if (!Encoding.UTF8.GetBytes(record.GetRawText()).AsSpan().SequenceEqual(current))
                        throw new InvalidOperationException("C9W_JOURNAL_READBACK");
                }
                AssertCurrentContext();
                // A v1 plan is advisory only; runner/recovery remain quarantined.
                if (operation == "plan") {
                    var plan = response.GetProperty("plan");
                    Shape(plan, "profiles","mapping","workDeletion");
                    if (plan.GetProperty("workDeletion").ValueKind != JsonValueKind.False)
                        throw new InvalidOperationException("C9W_JOURNAL_PLAN");
                }
            }
            return response.GetRawText();
        }
    }
}
