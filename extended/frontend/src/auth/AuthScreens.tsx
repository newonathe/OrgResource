import { FormEvent, useCallback, useEffect, useRef, useState } from "react";
import { Link, Navigate, useSearchParams } from "react-router-dom";
import { setCsrfToken } from "../api/client";
import { identityApi, type GovernanceMember, type Role } from "../api/extended";
import { useAuth, errorText } from "./AuthContext";
import { Button, GlassPanel, Notice, PageHeader, PageNavigation, PanelHeading, StatusBadge, usePageCursors, useSettings } from "../ui";

function AuthFrame({ children }: { children: React.ReactNode }) {
  const { theme, motionEnabled } = useSettings();
  return <div className="app-shell auth-shell" data-theme={theme} data-motion={motionEnabled ? "on" : "off"}><main className="auth-main"><div className="auth-brand"><span aria-hidden="true">◈</span><div><strong>OrgResource</strong><small>Extended organization workspace</small></div></div>{children}</main></div>;
}

export function LoginPage() {
  const { ready, session, signIn } = useAuth();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  if (!ready) return <AuthFrame><p role="status">Checking the saved server session…</p></AuthFrame>;
  if (session) return <Navigate replace to="/workspace" />;
  const submit = async (event: FormEvent) => {
    event.preventDefault(); setBusy(true); setError(null);
    try { await signIn(username, password); setPassword(""); }
    catch (cause) { setError(errorText(cause)); }
    finally { setBusy(false); }
  };
  return <AuthFrame><GlassPanel className="auth-panel" tilt={false}>
    <PageHeader eyebrow="Secure sign in" title="Welcome back" description="Sign in to an organization account provisioned by its owner." />
    {error && <Notice tone="danger">{error}</Notice>}
    <form className="auth-form" onSubmit={submit}>
      <label>Username<input autoComplete="username" required maxLength={64} value={username} onChange={event => setUsername(event.target.value)} /></label>
      <label>Password<input autoComplete="current-password" type="password" required maxLength={1024} value={password} onChange={event => setPassword(event.target.value)} /></label>
      <Button variant="primary" type="submit" disabled={busy}>{busy ? "Signing in…" : "Sign in"}</Button>
    </form>
    <p className="auth-links"><Link to="/recover">Use a one-time recovery code</Link><Link to="/invite">Redeem an invitation</Link></p>
    <p className="quiet-note">The session cookie is HttpOnly. This browser keeps the CSRF token in memory only.</p>
  </GlassPanel></AuthFrame>;
}

export function RecoveryPage() {
  const { ready, session } = useAuth();
  const [username, setUsername] = useState("");
  const [code, setCode] = useState("");
  const [password, setPassword] = useState("");
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const submit = async (event: FormEvent) => {
    event.preventDefault(); setBusy(true); setError(null); setMessage(null);
    try {
      setCsrfToken((await identityApi.csrf()).csrfToken);
      const result = await identityApi.recover(username, code, password);
      setMessage(result.message); setCode(""); setPassword("");
    }
    catch (cause) { setError(errorText(cause)); }
    finally { setBusy(false); }
  };
  if (!ready) return <AuthFrame><p role="status">Preparing secure account recovery…</p></AuthFrame>;
  if (session) return <Navigate replace to="/workspace" />;
  return <AuthFrame><GlassPanel className="auth-panel" tilt={false}>
    <PageHeader eyebrow="Account recovery" title="Reset access" description="Submit a one-time recovery code. The server uses the same response for valid and invalid codes." />
    {error && <Notice tone="danger">{error}</Notice>}{message && <Notice>{message}</Notice>}
    <form className="auth-form" onSubmit={submit}>
      <label>Username<input autoComplete="username" required maxLength={64} value={username} onChange={event => setUsername(event.target.value)} /></label>
      <label>One-time recovery code<input autoComplete="one-time-code" required maxLength={128} value={code} onChange={event => setCode(event.target.value)} /></label>
      <label>New password<input autoComplete="new-password" type="password" required minLength={15} maxLength={1024} value={password} onChange={event => setPassword(event.target.value)} /></label>
      <Button variant="primary" type="submit" disabled={busy}>{busy ? "Submitting…" : "Reset password"}</Button>
    </form><p className="auth-links"><Link to="/login">Return to sign in</Link></p>
  </GlassPanel></AuthFrame>;
}

export function InvitationPage() {
  const { ready, session, adoptSession, reload, organizations } = useAuth();
  const [params, setParams] = useSearchParams();
  const [token, setToken] = useState("");
  useEffect(() => {
    const url = new URL(window.location.href);
    if (url.searchParams.has("token")) {
      url.searchParams.delete("token");
      window.history.replaceState(window.history.state, "", url);
    }
    if (params.has("token")) {
      const sanitized = new URLSearchParams(params);
      sanitized.delete("token");
      setParams(sanitized, { replace: true });
    }
  }, [params, setParams]);
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [displayName, setDisplayName] = useState("");
  const [codes, setCodes] = useState<string[] | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const needsAccount = !session;
  const submit = async (event: FormEvent) => {
    event.preventDefault(); setBusy(true); setError(null); setMessage(null);
    try {
      setCsrfToken((await identityApi.csrf()).csrfToken);
      const result = await identityApi.redeemInvitation({ bearerToken: token, ...(needsAccount ? { username, password, displayName } : {}) });
      setToken(""); setPassword("");
      if (result.newAccount && result.session && result.csrfToken) {
        await adoptSession(result.session, result.csrfToken);
        setCodes(result.recoveryCodes);
        setMessage("Invitation accepted and account created. Save these recovery codes now; they are shown once and are not stored by the app.");
      } else {
        await reload();
        setMessage(`Invitation accepted for ${organizations.find(org => org.id === result.organizationId)?.displayName ?? "the organization"}.`);
      }
    } catch (cause) { setError(errorText(cause)); }
    finally { setBusy(false); }
  };
  if (!ready) return <AuthFrame><p role="status">Preparing secure invitation access…</p></AuthFrame>;
  return <AuthFrame><GlassPanel className="auth-panel" tilt={false}>
    <PageHeader eyebrow="Organization access" title="Redeem invitation" description={session ? "Join the organization using the invitation bearer token." : "An invitation can create a new account. Only the server grants membership."} />
    {error && <Notice tone="danger">{error}</Notice>}{message && <Notice tone="success">{message}</Notice>}
    {codes && <section className="recovery-code-list" aria-label="One-time recovery codes"><h2>Recovery codes</h2><ul>{codes.map(code => <li key={code}><code>{code}</code></li>)}</ul><Button onClick={() => void navigator.clipboard?.writeText(codes.join("\n"))}>Copy codes</Button><p className="quiet-note">Copied codes are not retained by this app. Store them in your password manager before leaving.</p></section>}
    {!codes && <form className="auth-form" onSubmit={submit}>
      <label>Invitation token<input autoComplete="off" required value={token} onChange={event => setToken(event.target.value)} /></label>
      {needsAccount && <><label>New username<input autoComplete="username" required maxLength={64} value={username} onChange={event => setUsername(event.target.value)} /></label><label>Display name<input required maxLength={120} value={displayName} onChange={event => setDisplayName(event.target.value)} /></label><label>Password<input type="password" autoComplete="new-password" required minLength={15} maxLength={1024} value={password} onChange={event => setPassword(event.target.value)} /></label></>}
      <Button variant="primary" type="submit" disabled={busy}>{busy ? "Redeeming…" : "Redeem invitation"}</Button>
    </form>}
    <p className="auth-links"><Link to={session ? "/workspace" : "/login"}>{session ? "Return to workspace" : "Sign in instead"}</Link></p>
  </GlassPanel></AuthFrame>;
}

export function OrganizationPicker() {
  const { session, organizations, switchOrganization, error, clearError } = useAuth();
  const [busy, setBusy] = useState<string | null>(null);
  const [localError, setLocalError] = useState<string | null>(null);
  const [search, setSearch] = useState("");
  const normalized = search.trim().toLocaleLowerCase();
  const matching = organizations.filter(org =>
    (org.displayName + " " + org.slug).toLocaleLowerCase().includes(normalized));
  if (!session) return <Navigate replace to="/login" />;
  const choose = async (id: string) => {
    setBusy(id); setLocalError(null); clearError();
    try { await switchOrganization(id); }
    catch (cause) { setLocalError(errorText(cause)); }
    finally { setBusy(null); }
  };
  return <AuthFrame><GlassPanel className="auth-panel org-picker" tilt={false}>
    <PageHeader eyebrow="Workspace selection" title={`Choose an organization, ${session.displayName}`} description="Organization data is fetched only after you explicitly select an active membership." />
    {(error || localError) && <Notice tone="danger">{localError ?? error}</Notice>}
    {organizations.length === 0 ? <Notice>No active organization memberships are available. Redeem an invitation or contact an organization owner.</Notice> :
      <>
        {organizations.length > 8 && <label className="organization-search-label">Find organization
          <input type="search" value={search} autoComplete="off" placeholder="Search organization name or slug"
            onChange={event => setSearch(event.target.value)} />
        </label>}
        <p className="quiet-note" role="status">{matching.length} of {organizations.length} organization{organizations.length === 1 ? "" : "s"} shown</p>
        {matching.length === 0
          ? <Notice>No organizations match this search. Try a different name.</Notice>
          : <div className="organization-list">{matching.map(org => <article className="organization-choice" key={org.id}><div><h2>{org.displayName}</h2><p>{org.slug} · <StatusBadge tone="blue">{org.role}</StatusBadge></p></div><Button variant="primary" disabled={busy !== null} onClick={() => void choose(org.id)}>{busy === org.id ? "Selecting…" : "Open workspace"}</Button></article>)}</div>}
      </>}
  </GlassPanel></AuthFrame>;
}

export function AccountPage() {
  const { signOut, reload } = useAuth();
  const { motionEnabled, setMotionEnabled } = useSettings();
  const [account, setAccount] = useState<{ username: string; displayName: string; status: string } | null>(null);
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [codes, setCodes] = useState<string[] | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  useEffect(() => { let active = true; void identityApi.account().then(value => { if (active) setAccount(value); }).catch(cause => { if (active) setError(errorText(cause)); }); return () => { active = false; }; }, []);
  const perform = async (action: () => Promise<unknown>, success: string) => {
    setBusy(true); setError(null); setMessage(null);
    try { await action(); setMessage(success); }
    catch (cause) { setError(errorText(cause)); }
    finally { setBusy(false); }
  };
  const changePassword = (event: FormEvent) => { event.preventDefault(); void perform(async () => { await identityApi.changePassword(currentPassword, newPassword); setCurrentPassword(""); setNewPassword(""); await reload(); }, "Password changed; previous sessions were revoked."); };
  const rotate = () => void perform(async () => { const result = await identityApi.rotateRecoveryCodes(currentPassword); setCodes(result.codes); setCurrentPassword(""); }, "New one-time codes are shown below. Previous codes have been replaced.");
  const disable = () => { if (!window.confirm("Disable this account and sign out all sessions? This cannot be undone from this screen.")) return; void perform(async () => { await identityApi.disableAccount(currentPassword); await signOut(); }, "Account disabled."); };
  return <section className="page-shell"><PageHeader eyebrow="Account · session" title="Account and security" description="Manage your own password, one-time recovery codes, and server sessions." />
    {error && <Notice tone="danger">{error}</Notice>}{message && <Notice tone="success">{message}</Notice>}
    <GlassPanel className="form-panel account-preferences" tilt={false}>
      <PanelHeading kicker="Display preferences" title="Interface motion" />
      <div className="account-preference-control">
        <div><p>Animations and pointer effects</p><p className="quiet-note">Changes apply immediately and are saved in this browser. Your device's reduced-motion preference still takes priority.</p></div>
        <Button type="button" variant="secondary" role="switch" aria-label="Interface motion" aria-checked={motionEnabled}
          onClick={() => setMotionEnabled(!motionEnabled)}>Motion {motionEnabled ? "On" : "Off"}</Button>
      </div>
    </GlassPanel>
    <div className="account-grid"><GlassPanel className="form-panel" tilt={false}><PanelHeading kicker="Server account" title={account?.displayName ?? "Account"} /><dl className="account-details"><div><dt>Username</dt><dd>{account?.username ?? "Loading…"}</dd></div><div><dt>Status</dt><dd>{account?.status ?? "—"}</dd></div><div><dt>Session</dt><dd>HttpOnly cookie · 12-hour absolute lifetime</dd></div></dl><Button onClick={() => void signOut()}>Sign out</Button></GlassPanel>
      <GlassPanel className="form-panel" tilt={false}><PanelHeading kicker="Credentials" title="Change password" /><form className="auth-form" onSubmit={changePassword}><label>Current password<input type="password" autoComplete="current-password" required value={currentPassword} onChange={event => setCurrentPassword(event.target.value)} /></label><label>New password<input type="password" autoComplete="new-password" minLength={15} maxLength={1024} required value={newPassword} onChange={event => setNewPassword(event.target.value)} /></label><Button variant="primary" disabled={busy}>{busy ? "Saving…" : "Change password"}</Button></form></GlassPanel>
      <GlassPanel className="form-panel" tilt={false}><PanelHeading kicker="Recovery" title="Rotate recovery codes" /><p>Rotation replaces all previous codes. New values are shown once in this browser session.</p><label>Confirm current password<input type="password" autoComplete="current-password" value={currentPassword} onChange={event => setCurrentPassword(event.target.value)} /></label><Button disabled={busy || !currentPassword} onClick={rotate}>Generate new codes</Button>{codes && <ol className="recovery-code-list">{codes.map(code => <li key={code}><code>{code}</code></li>)}</ol>}</GlassPanel>
      <GlassPanel className="form-panel danger-zone" tilt={false}><PanelHeading kicker="Irreversible account action" title="Disable account" /><p>Disabling revokes all active sessions. Use only if you intend to stop using this account.</p><Button variant="danger" disabled={busy || !currentPassword} onClick={disable}>Disable my account</Button></GlassPanel>
    </div>
  </section>;
}

export function GovernancePage({ orgId, role }: { orgId: string; role: Role }) {
  const { session, reload } = useAuth();
  const [members, setMembers] = useState<GovernanceMember[]>([]);
  const [nextCursor, setNextCursor] = useState<string | null>(null);
  const [membersLoading, setMembersLoading] = useState(true);
  const [membersError, setMembersError] = useState<string | null>(null);
  const { cursor, page, move } = usePageCursors(orgId);
  const [inviteRole, setInviteRole] = useState<Exclude<Role, "OWNER">>("MEMBER");
  const [inviteToken, setInviteToken] = useState<string | null>(null);
  const [transferId, setTransferId] = useState("");
  const [transferEtag, setTransferEtag] = useState("");
  const [currentPassword, setCurrentPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const membersRequest = useRef<AbortController | null>(null);
  const ownMembershipId = session?.memberships.find(member => member.organizationId === orgId)?.id;
  const grantableRoles = role === "OWNER"
    ? (["ADMIN", "CUSTODIAN", "LEAD", "MEMBER"] as const)
    : (["CUSTODIAN", "LEAD", "MEMBER"] as const);
  const load = useCallback(async () => {
    membersRequest.current?.abort();
    const controller = new AbortController();
    membersRequest.current = controller;
    setMembersLoading(true);
    setMembersError(null);
    setNextCursor(null);
    try {
      const result = await identityApi.members(orgId, cursor ?? undefined, controller.signal);
      if (!controller.signal.aborted) {
        setMembers(result.items);
        setNextCursor(result.nextCursor);
      }
    } catch (cause) {
      if (!controller.signal.aborted) {
        setMembers([]);
        setMembersError(errorText(cause));
      }
    } finally {
      if (!controller.signal.aborted) setMembersLoading(false);
    }
  }, [orgId, cursor]);
  useEffect(() => {
    const timer = window.setTimeout(() => { void load(); }, 0);
    return () => { window.clearTimeout(timer); membersRequest.current?.abort(); };
  }, [load]);
  const goToPage = (next: string | null) => {
    setMembersLoading(true);
    move(next);
  };
  const recipients = members.filter(member => member.status === "ACTIVE" && member.role === "ADMIN");
  const run = async (action: () => Promise<unknown>, success: string) => { setBusy(true); setError(null); setMessage(null); try { await action(); setMessage(success); await load(); } catch (cause) { setError(errorText(cause)); } finally { setBusy(false); } };
  const createInvite = () => void run(async () => { const result = await identityApi.invite(orgId, inviteRole); setInviteToken(result.data.bearerToken); }, "Invitation created. Copy the bearer token now; it is not stored in browser storage.");
  const changeRole = (member: GovernanceMember, roleValue: Exclude<Role, "OWNER">) => void run(() => identityApi.changeRole(orgId, member, roleValue), "Membership role updated.");
  const suspend = (member: GovernanceMember) => { if (window.confirm(`Suspend ${member.displayName}?`)) void run(() => identityApi.suspendMember(orgId, member), "Membership suspended."); };
  const startTransfer = async (event: FormEvent<HTMLFormElement>) => { event.preventDefault(); const target = new FormData(event.currentTarget).get("target"); if (typeof target !== "string") return; void run(async () => { const result = await identityApi.startOwnershipTransfer(orgId, target, currentPassword); if (!result.data.id || !result.etag) throw new Error("The server did not return a transfer ID and ETag."); setTransferId(result.data.id); setTransferEtag(result.etag); setCurrentPassword(""); }, "Ownership transfer started. Share the transfer ID and ETag with its recipient through a private channel."); };
  const acceptTransfer = (event: FormEvent) => {
    event.preventDefault();
    void run(async () => {
      await identityApi.acceptOwnershipTransfer(orgId, transferId, transferEtag);
      setTransferId(""); setTransferEtag("");
      await reload();
    }, "Ownership transfer accepted.");
  };
  return <section className="page-shell"><PageHeader eyebrow="Organization · governance" title="Members and access" description="Roles and membership state are enforced by the server. This page never grants access from browser state." />
    {error && <Notice tone="danger">{error}</Notice>}{message && <Notice tone="success">{message}</Notice>}
    <div className="governance-grid"><GlassPanel className="list-panel" tilt={false}><PanelHeading kicker="Active and suspended members" title="Memberships"><Button disabled={busy || membersLoading} onClick={() => void load()}>Refresh</Button></PanelHeading>
      {!membersLoading && membersError && <><Notice tone="danger">{membersError}</Notice><Button disabled={busy} onClick={() => void load()}>Retry member page</Button></>}
      {!membersLoading && !membersError && (members.length === 0 ? <p>No memberships on this page.</p> : <div className="member-list">{members.map(member => <article className="member-row" key={member.id}><div><strong>{member.displayName}</strong><p><StatusBadge tone={member.status === "ACTIVE" ? "success" : "warning"}>{member.status}</StatusBadge> <StatusBadge tone="blue">{member.role}</StatusBadge></p></div>
        {member.id !== ownMembershipId && member.role !== "OWNER" && member.status === "ACTIVE" &&
          (role === "OWNER" || (role === "ADMIN" && member.role !== "ADMIN")) &&
          <div className="row-actions"><label className="compact-label">Role<select value={member.role} disabled={busy} onChange={event => changeRole(member, event.target.value as Exclude<Role, "OWNER">)}>{grantableRoles.map(option => <option key={option}>{option}</option>)}</select></label><Button variant="danger" disabled={busy} onClick={() => suspend(member)}>Suspend</Button></div>}</article>)}</div>)}
      <PageNavigation label="Membership pages" page={page} count={members.length} hasNext={nextCursor !== null}
        loading={membersLoading || busy} unavailable={!!membersError} onPrevious={() => goToPage(null)}
        onNext={() => { if (nextCursor) goToPage(nextCursor); }} />
    </GlassPanel>
    <GlassPanel className="form-panel" tilt={false}><PanelHeading kicker="Single-use credential" title="Create invitation" /><label>Grant role<select value={inviteRole} disabled={busy} onChange={event => setInviteRole(event.target.value as Exclude<Role, "OWNER">)}>{grantableRoles.map(option => <option key={option}>{option}</option>)}</select></label><Button variant="primary" disabled={busy} onClick={createInvite}>{busy ? "Working…" : "Create invitation"}</Button>{inviteToken && <div className="token-reveal"><strong>Bearer token — shown once</strong><code>{inviteToken}</code><Button onClick={() => void navigator.clipboard?.writeText(inviteToken)}>Copy token</Button><p>Send it using your approved private method. It is held only in current component memory.</p></div>}</GlassPanel>
    {role === "OWNER" && <GlassPanel className="form-panel" tilt={false}><PanelHeading kicker="Two-person governance flow" title="Transfer ownership" /><form className="auth-form" onSubmit={startTransfer}><label>Recipient membership<select key={cursor ?? "first"} name="target" required disabled={busy || membersLoading}><option value="">Choose an active administrator</option>{recipients.map(member => <option key={member.id} value={member.id}>{member.displayName}</option>)}</select></label><p className="quiet-note">The recipient must be on the current membership page.</p><label>Current password<input type="password" autoComplete="current-password" required value={currentPassword} onChange={event => setCurrentPassword(event.target.value)} /></label><Button disabled={busy || membersLoading || recipients.length === 0}>Start transfer</Button></form><p className="quiet-note">No pending-transfer list API exists. Keep the transfer ID returned by the server and give it to the recipient through a private channel.</p></GlassPanel>}
    {role === "OWNER" && transferId && transferEtag && <GlassPanel className="form-panel" tilt={false}>
      <PanelHeading kicker="Private handoff" title="Transfer details" />
      <dl className="account-details"><div><dt>Transfer ID</dt><dd><code>{transferId}</code></dd></div><div><dt>Current ETag</dt><dd><code>{transferEtag}</code></dd></div></dl>
    </GlassPanel>}
    {role === "ADMIN" && <GlassPanel className="form-panel" tilt={false}><PanelHeading kicker="Recipient action" title="Accept ownership transfer" /><form className="auth-form" onSubmit={acceptTransfer}><label>Transfer ID<input required value={transferId} onChange={event => setTransferId(event.target.value)} /></label><label>Current transfer ETag<input required value={transferEtag} onChange={event => setTransferEtag(event.target.value)} placeholder={'"v1"'} /></label><Button disabled={busy || !transferId || !transferEtag}>Accept transfer</Button></form><p className="quiet-note">The server requires both the transfer ID and matching ETag. No pending-transfer lookup API is available, so these values must be handed to the recipient privately.</p></GlassPanel>}
    </div>
  </section>;
}
