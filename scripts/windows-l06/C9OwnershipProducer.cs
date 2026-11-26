using System;
using System.Collections.Generic;
using System.IO;
using System.Security.Cryptography;
using System.Text.Json;

// H03 intent-first producer, not yet wired into the quarantined runner.
// Serial parent operations; failures poison the instance. Never retry ambiguous effects.
// Only this object owns its newly created work and profile handles.
public sealed class C9OwnershipProducer : IDisposable {
    readonly C9ControlLock owner;
    readonly C9JournalParent journal;
    readonly Dictionary<string,C9Container> profiles = new Dictionary<string,C9Container>(StringComparer.Ordinal);
    C9DirectoryIdentity workIdentity;
    string sha;
    bool poisoned, disposed;
    public string WorkPath { get { return Path.Combine(owner.Root, "orgresource-c9w-" + owner.Token); } }
    public string WorkId { get { Check(); return workIdentity?.Id; } }

    public C9OwnershipProducer(C9ControlLock owner, string python, string pythonSha,
        string bridge, string bridgeSha, string journalSha, string journalV2Sha) {
        this.owner = owner ?? throw new ArgumentNullException(nameof(owner));
        journal = new C9JournalParent(owner, python, pythonSha, bridge, bridgeSha, journalSha, journalV2Sha);
        // Only a new journal is accepted; no producer resume or inferred ownership.
        sha = Receipt(journal.Invoke("init"));
    }

    static string Receipt(string response) {
        using var doc = JsonDocument.Parse(response);
        return doc.RootElement.GetProperty("sha256").GetString();
    }

    void Check() {
        if (disposed || poisoned) throw new InvalidOperationException("C9W_PRODUCER_REFUSED");
        journal.AssertCurrentContext();
    }

    void Transition(string resource, string state, object fields) {
        sha = Receipt(journal.Invoke("transition", JsonSerializer.Serialize(new {
            expectedSha256 = sha, resource, state, fields
        })));
    }

    public string CreateWork() {
        Check();
        if (workIdentity != null) throw new InvalidOperationException("C9W_WORK_ALREADY_CREATED");
        try {
            Transition("work", "intent", new {});
            journal.AssertCurrentContext();
            workIdentity = owner.CreateWorkAfterIntent();
            Transition("work", "created", new { id = workIdentity.Id });
            return WorkPath;
        } catch { poisoned = true; throw; }
    }

    static bool Role(string role) {
        return role == "source" || role == "restore" || role == "mixed" || role == "jobtest";
    }

    public C9Container CreateProfile(string role) {
        Check();
        if (!Role(role) || workIdentity == null || profiles.ContainsKey(role))
            throw new InvalidOperationException("C9W_PROFILE_ROLE");
        string name = "orgresource.c9." + Convert.ToHexString(RandomNumberGenerator.GetBytes(6)).ToLowerInvariant();
        try {
            Transition(role, "intent", new { name });
            // CreateAppContainerProfile must return S_OK, not an existing profile.
            // Constructor callback journals the real returned SID before job initialization.
            journal.AssertCurrentContext();
            var profile = new C9Container(name, (sid, storage) =>
                Transition(role, "created", new { sid, storage }));
            try { profiles.Add(role, profile); }
            catch { profile.Dispose(); throw; } // handle-only cleanup, never delete by inference
            return profile;
        } catch { poisoned = true; throw; }
    }

    // Bounded normal cleanup of an in-memory owned profile, not crash recovery.
    // Caller must already have stopped the role and verified its listeners/processes.
    public void RemoveProfile(string role) {
        Check();
        if (!profiles.TryGetValue(role, out var profile))
            throw new InvalidOperationException("C9W_PROFILE_ROLE");
        try {
            if (!profile.KillAll()) throw new InvalidOperationException("C9W_JOB_KILL");
            profile.Dispose(); // closes only owned handles; retains OS profile
            profile.AssertProfileStoragePresent();
            Transition(role, "removing", new {});
            journal.AssertCurrentContext();
            profile.DeleteConfirmedProfile();
            // S_OK alone is not sufficient: independently reobserve captured storage absence.
            // Storage evidence is derived from the captured native observation only.
            var evidence = profile.RemovalEvidence();
            Transition(role, "removed", new { evidence });
            profiles.Remove(role);
        } catch { poisoned = true; throw; }
    }

    public void Dispose() {
        if (disposed) return;
        disposed = true;
        // On failure/crash intent, close only owned handles. Leave resources/journal for reconciliation.
        bool ok = true;
        foreach (var profile in profiles.Values) {
            try { profile.Dispose(); } catch { ok = false; }
        }
        profiles.Clear();
        if (workIdentity != null) { workIdentity.Dispose(); workIdentity = null; }
        if (!ok) throw new InvalidOperationException("C9W_PRODUCER_HANDLE_CLEANUP");
        // Does not release the caller's separate control lock or delete work.
    }
}
