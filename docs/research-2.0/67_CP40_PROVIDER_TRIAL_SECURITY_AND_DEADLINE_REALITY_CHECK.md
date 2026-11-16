# CP40 — Independent provider-trial, invitation-security, zero-cost and deadline reality check

> **P43 course-scope user decision (2026-11-16):** The previous optional instructor-clarification draft in §6 remains historical/UNSENT; it is **not necessary** to choose product scope. The supplied final-project specification and syllabus now govern. A **real Twilio Messages REST adapter** is a planned code requirement alongside FakeTwilio (no live SMS, unauthorized trial, fee or borrowed credentials), and **real CLO9 image receive/serve** is included. See [CP43](70_CP43_FIXED_WRITTEN_COURSE_SCOPE_AND_HANDOFF_GO_NO_GO.md). Existing CP40 external trial/cost constraints still apply. No user/instructor approval or actual provider test inferred.


> **New source-manifest recount (2026-11-16):** [Machine-readable historical count reconciliation](contracts/ORIGINAL_MANIFEST_COUNT_RECONCILIATION_2026-11-16.json) was created by reading the entire embedded CSV record set in `04_ORGRESOURCE_PRIOR_WORK_RESEARCH_AND_MANIFEST.md`: **1,241 original filesystem entries, 15 video file paths, 1,226 historical non-video files, and 1,370 nested archive members (2,611 rows)**. The previously recorded 1,227 actual outer nonvideo ZIP entries are one more. The historical text states that its generated CSV is stored inside Part 1, a **plausible** one-added-file explanation, but exact ZIP path/hash remains **NOT RECHECKED** because original archive bytes could not be opened in this execution environment. Do not say the discrepancy is resolved.


**Research date:** 2026-11-16 PHT. **Status:** primary-source-supported **DESIGN RECOMMENDATION** and targeted correction, **NOT** implementation, actual vendor-account verification, instructor waiver, paid account, live SMS, new runtime test or Codex handoff. Read [CP31](58_CP31_PRE_CODEX_FINALIZATION_PACKET.md) then [CP32–CP39](66_CP39_PINNED_CI_TRIAGE_AND_LAST_CONTRACT_CORRECTIONS.md); CP40 controls only the explicit topics below.

## 1. Fresh current external constraints and actual O14 code

### Twilio trial is not a no-cost permanent custom-reminder service

**VERIFIED EXTERNAL FACT, 2026-11-16:** Official Twilio trial docs currently say: **30-day trial; 100 included SMS trial units; only up to five verified recipient numbers; sign-up-country geographic scope; predefined trial SMS templates, not arbitrary custom message bodies; no automatic upgrade**. These are published terms, not proof the owner's particular account can send even one message. Source: [official trial overview](https://www.twilio.com/docs/usage/trials) and [trial setup](https://www.twilio.com/docs/usage/tutorials/how-to-use-your-free-trial-account). Check live Console and applicable country regulatory restrictions before considering real send. **Misleading claim prohibited:** “the free trial permanently sends scheduled customized OrgResource reminders to any student.”

**VERIFIED O14 SOURCE:** `ReminderComponent.prepareForDispatch` constructs a **custom date-specific SMS body** ("OrgResource reminder: your reusable allocation is due by ..."). `FakeTwilioTransport` uses Retrofit/Gson to POST this payload to a **separate loopback course FakeTwilio server**, returning `SIMULATED` for validated fake acceptance. `ReminderConfiguration` mode `TWILIO_VIRTUAL` deliberately throws `IllegalStateException` (provider adapter not authorized/implemented); O14 doesn't connect to live Twilio. This is honest classroom source behavior and cannot be marked actual provider proof.

**Course requirement interpretation still OPEN:** assignment says “RetroJit” and “at the very least Twilio” while syllabus names Retrofit/Gson and course source includes FakeTwilio. Only current Canvas or instructor may decide whether a local provided simulator satisfies provider-specific grading. Do not label it waived.

### Free hosting is not guaranteed continuous availability

**VERIFIED EXTERNAL FACT, 2026-11-16:** OCI Always Free compute is eligible subject to actual home-region supply and allowed resource shapes. Oracle documents capacity errors and potential reclamation of idle VMs based on a 7-day resource-utilization threshold. Source: [Oracle Always Free documentation](https://docs.oracle.com/en-us/iaas/Content/FreeTier/freetier_topic-Always_Free_Resources.htm). A VM's Always Free label does not constitute account-specific entitlement, indefinite uptime or a suitable protected DB, TLS, off-provider backup and sender. If A1 unavailable, do **not** silently buy PAYG/paid cloud.

**VERIFIED EXTERNAL FACT, 2026-11-16:** GitHub Actions on private repositories draws from plan-specific included usage/storage and additional usage may be billed. Source: [GitHub Actions billing](https://docs.github.com/en/actions/concepts/billing-and-usage). Cost/usage alert and hard spending budget controls must be confirmed from the user's actual account. Avoid assuming a $0 full-time private pipeline after quotas.

### Supported Jersey does not settle Javax/Jakarta

**VERIFIED EXTERNAL FACT, 2026-11-16:** Spring Boot current Jersey documentation shows `spring-boot-starter-jersey`, `ResourceConfig` and `jakarta.ws.rs.GET/Path` with injected Spring Component; it also documents configuring Jersey alongside Spring MVC (Jersey filter and forwarding on 404). Source: [Boot Jersey docs](https://docs.spring.io/spring-boot/reference/web/servlet.html), [Jersey alongside MVC](https://docs.spring.io/spring-boot/how-to/jersey.html). Hibernate Validator [9.1](https://hibernate.org/validator/releases/9.1/) is Jakarta-based for Boot4.1; [6.2](https://hibernate.org/validator/releases/6.2/) retains `javax.*` limited support. The S1 isolated feasibility gate must prove not only Jersey GET/POST/JPA `javax.validation` but **that protected JSON API and bundled React static assets resolve together in one JVM**. Official documentation is not an executing integration test.

## 2. Final proposed communications-mode policy and state mapping

The ONE product must remain useful with **no SMS account**. The UI's in-app upcoming/due flags derive from Project.endAt and ISSUED reusable allocations, clearly marked "in-app"; no outgoing sender is necessary. Scheduling and separate Retrofit HTTP FakeTwilio remain technical course requirements.

| Mode | Allowed preconditions and mechanism | What API/UI may say | What it may NOT say |
|---|---|---|---|
| `DISABLED` (default) | No recipient/transport; scheduler may still compute expiry and in-app due displays | `No SMS sent` / `Notifications disabled` | Sent/delivered |
| `FAKE_TWILIO` | Loopback-only, supplied course protocol with dummy authorization and fictional E164, Retrofit/Gson HTTP | `SIMULATED`, accepted by FakeTwilio simulator only; show payload and deterministic logs in local demo (redact phone) | Provider accepted or device delivered |
| `PROVIDER_TRIAL` (OPTIONAL NOT IMPLEMENTED) | Only after owner approves real account, verified consenting recipient and allowed trial message template; must check account live and published restrictions | `PROVIDER_ACCEPTED` if actual API response establishes accepted **template trial** request; `REJECTED` or `UNKNOWN` as observed | Customized due-date content, unrestricted numbers, guaranteed future availability |
| `PROVIDER_LIVE` (OPTIONAL NOT IMPLEMENTED) | Explicit reviewed billing/consent/regulatory approval, provider capability, opt-in and loss controls | A real provider acceptance status and verified handset receipt **only if externally observed** | Zero mandatory cost, delivery guarantees, simulated response equated with carrier delivery |

**No automatic fallback on provider failure** from live send to another sender or repeated SMS; fail CLOSED and record `UNKNOWN` if response may have been accepted without acknowledgement. One-time claim + eligibility recheck precedes HTTP (O14 pattern). Scheduler claims must be transactionally bounded, with a recorded attempt ID for diagnostics. Do not hold SQL locks during HTTP. Treat stale CLAIMED as UNKNOWN; do not auto resend since provider may already have received it. A09 consent revocation clears future sending eligibility but cannot retract already accepted external SMS. No actual real-provider account/test has been performed.

**Required NEW test IDs:**
- `INT40-01`: Local FakeTwilio receives exact personalized body by Retrofit/Gson; persistent outcome SIMULATED, no carrier claim.
- `INT40-02`: Configuration `DISABLED` never makes HTTP request; in-app due list still works.
- `INT40-03`: Trial mode with no authorized/verified account fails CLOSED and **never attempts** an arbitrary custom message.
- `INT40-04`: Actual optional permitted trial template API outcome is classified PROVIDER_ACCEPTED vs REJECTED vs UNKNOWN based on observed HTTP/provider result; no delivered flag without proof.
- `INT40-05`: Two scheduler workers, timeout and delayed A09 revoke cannot duplicate or falsely retract sends; no blind retry.
- `INT40-06`: Bad/expired consent, unverified destination, quota exhaustion and unavailable provider suppress send without exposing phone/secrets.
- `INT40-07`: Real trial ends or terms/recipient eligibility change; product still functions in DISABLED/in-app-only mode and shows a truthful status.

## 3. P0 invite-based identity: remove an unsafe optional ambiguity

The old candidate public Invite DTO offered `intendedUserId? UUID` while anonymous invite redemption could create a new account, without stating how a recipient's global account identity would be verified or bound. **NEW DESIGN RECOMMENDATION:** **omit `intendedUserId` entirely in P0**, because no verified account-directory lookup exists in the proposal. Every P0 invitation is a **high-entropy one-time bearer token** with intended role, organization, creator, expiration and revocation; its possession authorizes an unverified fictional registration/join, **not real-world identity**. Token secret shown once to inviter, delivered via an authorized out-of-band channel manually, never as a URL query string or log. Token hash only stored in DB; expiry max24h/default24h (configurable to shorter), 256-bit random generation; `ACCEPTED` after first transaction commit, all concurrent repeats fail. Enrollment is limited to a named fictional org and role; no public self-create Org. Token theft risk remains: bearer possession is not personal identity verification. Future named-recipient binding would require a separate versioned design/actual verification procedure.

**Changes to source contracts:** [CP34 invitation table](61_CP34_CANONICAL_SCHEMA_AND_CUTOVER_BLUEPRINT.md) no longer has `intended_user_id` in P0; [CP35 Invite DTO](62_CP35_API_AUTH_UI_WORKFLOW_FREEZE_CANDIDATE.md) has only `role,expiresAt` with one-time token, and an anonymous/new-account and authenticated existing-account redemption branch. [CP37 S3](64_CP37_SEALED_STAGE_BY_STAGE_CODEX_PROMPTS.md) must test token interception/replay/simultaneous redeem and correct role. No account is claimed verified as an Ateneo student.

**NEW `SEC40-01` test:** a malicious `intendedUserId` or user/account/status field in POST Invite or Redeem is rejected as unknown writable field (422 or selected 400 grammar); token replay after accepted fails, race exactly one winner, same-org membership UQ holds.

## 4. Optional phone number data protection contract (only with later approved real sending)

**Default P0**: do not collect real phone data. O14 legacy A01 receives fictional E164 for course FakeTwilio; original input semantics preserved, but the unified secured DB should not store raw destination plainly. New public v2 does not expose an unapproved self-service phone edit/consent endpoint.

**RECOMMENDED future-capable schema details** without claiming encryption exists:
- `allocation.phone_ciphertext VARBINARY(256) NULL` stores ciphertext **including the authentication tag**; `phone_nonce BINARY(12) NULL`, `phone_key_id VARCHAR(40) NULL`. All three either NULL or set together. Use a tested authenticated encryption implementation, e.g. AES-256-GCM with fresh 96-bit nonce per encryption, authenticated associated data binding `org public UUID|allocation public UUID|field name|key version`. Key bytes live OUTSIDE DB/backups/code, with independent recovery tested.
- `sms_consent BOOLEAN DEFAULT FALSE`, `sms_consent_at DATETIME(6) NULL`, `sms_consent_source` appropriate bounded enum; **do not treat an officer's unchecked `smsConsent=true` JSON alone as verified recipient consent for external SMS**. Field is a prototype submission assertion until a consent collection/recording workflow exists.
- On A09 revoke, immediately set consent false and ciphertext/nonce/key ID NULL under transaction; preserve a redacted revocation audit event without phone. Already sent provider messages cannot be revoked. `CLAIMED` reminder rechecks consent before dispatch; in-flight external requests are inherently not retractable.
- Encryption key version rotation is an explicit ops procedure with actual backup/restore proof; if key unavailable, fail CLOSED without sending or displaying phone. No phone in application logs, Git, CI, Postman fixtures, errors or export.

These are design-level field specifications, not claims of compliance with any unstated Ateneo policy or of full data-privacy-law evaluation. If real personal information is planned, privacy basis/notice/retention and intended actual operator must be separately approved.

**NEW `SEC40-02` test:** database read lacks plaintext, log/export lacks raw phone, approved decrypt+decrypt failure cases behave safely, key rotation/clean restore works, A09 nulls encrypted fields and a stale reminder cannot send afterwards.

## 5. Academic time risk and evidence—not assumptions about submissions

**COURSE DOCUMENT:** printed original assignment has proposal due October 30, skeleton/partial due November 13 and Canvas skeleton export/video due November 14, and final due **December 5, 2026**. As of Nov16, the earlier dates are past. **UNKNOWN:** whether the student actually submitted any of these artifacts, obtained a Canvas extension, posted peer reviews, or has newer Canvas instructions. Do not claim they did or didn't. Current Canvas can supersede printed dates.

**USER-DECIDED order:** P-ACCEPT full secure product before independent academic A-FINAL. The Nov16–Dec5 span leaves 19 calendar days, while the draft includes 18 candidate tables, two secured HTTP surfaces, identity and audit, original 26 handlers, connected UI, zero-cost cloud, off-provider restore and real course demo. This is a **severe schedule feasibility risk**, not a reason to fake completion. Do not represent the current CP31 Nov20–25 backend schedule as a measured estimate or guaranteed feasible for a solo student. Present Nov19 decision with actual S1 evidence; without S1 GO, broad full-stack build cannot be authorized under existing gates.

**Owner-controlled contingency (NOT preauthorized):** keep same product/one DB and original O14 course behavior; defer optional public production-only functions **only if owner explicitly changes release acceptance requirements**, not by stealth. Default optional deferrals: permanent org close, verified campus SSO, time-window booking, partial damaged returns, analytics, rich animation, payment/procurement, custom free SMS. Never cut literal course requirements, actual stock concurrency, auth for public Internet, truthful test evidence, owner-only authorization or private source licensing review. If free hosted P-ACCEPT blocked, present an explicit decision: keep strict no-spend and report blocked vs accept different owner-authorized path. Never imply an academic extension or bypass production-first rule.

## 6. Explicit instructor check message draft (NOT SENT)

> Subject: CSCI 180.05 final project — clarification on integrations, images and resource deletion
>
> Good day, Sir. I am finalizing the OrgResource project against the printed final-project requirements and want to confirm three points. (1) Does integrating the supplied FakeTwilio service through Retrofit/Gson satisfy the “at the very least Twilio” requirement, or must a live Twilio account be used? (2) Must the final project itself receive and serve an image over HTTP for CLO9, given the syllabus's differing CLO mappings? (3) For a Resource with no remaining stock, would a real delete endpoint that refuses removal when immutable stock history exists meet the create/delete requirement? I will preserve the original GET/POST operations and Postman demonstrations. Please also confirm whether any current Canvas announcements have changed the printed December 5 final due date or submission requirements. Thank you.

**Do not send without owner/student review.** The written assignment and syllabus do not answer these points definitively; no instructor approval or Canvas check has occurred in this chat.

## 7. Decision and evidence register

| Category | Final proposed stance | Evidence needed to mark accepted |
|---|---|---|
| SMS in core P0 | Disabled by default; truthful in-app flags + real loopback FakeTwilio | Local real HTTP and durable claimed result, actual owner review |
| Real-provider trial | Optional contingent experiment; cannot prove custom reminders with current published trial terms | Current account capability/verified recipient/allowed message, permitted API run |
| Literal Javax | Supported current one-JVM hypothesis, explicit javax validation preserved | Actual isolated Java/JPA/Jersey/Spring result, not vendor docs alone |
| Hosting/CD $0 | Conditional on account quotas, host capacity, idle reclaim safeguards, independent restore | Actual $0 entitlement, monitoring, outage/recovery, budget controls |
| Invitation | Bare invite token, no intended user identity binding in fictional P0 | S3 role/token/one-winner tests, owner review |
| Phone security | No real data by default; encrypted optional when explicitly enabled | Key/consent/retention design and actual TEST/DR results |
| Academic obligations | All original dates and tech preserved as printed; earlier submission status UNKNOWN | Current Canvas screenshot/owner actual receipt, instructor answers |
| Codex | SEALED planning prompts only | Separate owner authorization after design review and appropriate gates |

**Stop:** Do not publish a further generic “complete research” packet. Research only remaining when a new primary fact/owner rule materially changes this design. Most unresolved issues are executable proofs, account-specific constraints, current Canvas or owner acceptance; they cannot be erased by more prose. This CP40 is research documentation and does not alter the running app.
