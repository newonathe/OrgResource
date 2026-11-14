# 07 — Retrofit/Gson, SMS, scheduling, security, privacy, demo-only policy

## Course interface

Printed assignment says "RetroJit", but course examples and syllabus use Retrofit and Gson. Implement actual outbound Retrofit from Java backend; frontend fetch to OrgResource API does not satisfy external-service requirement.

Course FakeTwilioController maps @Path("/fake_twilio"), @POST @Path("/send"), consumes application/x-www-form-urlencoded fields To, MessagingServiceSid, Body; Authorization header required and PRINTED by simulator (never send real auth). Local configured port in original course files 9997, verify actual startup. Simulator returns JSON accepted status typically HTTP 200; original Retrofit tester looked for HTTP 201 for real Twilio API, so do not assume 201 in simulator. Gson converter must be enabled and actual reply parsed. Response key messaging_service_sid needs @SerializedName.

## Modes

DISABLED default: no send, no provider credential requirement; core and expiry work.
FAKE_TWILIO: form-encoded HTTP to local simulator, dummy auth only, outcome SIMULATED when actual HTTP response accepted.
TWILIO_VIRTUAL conditional: actual authorized Twilio Messaging API to user's eligible controlled Virtual Phone destination, provider-accepted result only, NOT handset delivered. Trial/template restrictions are changeable and must be verified against current Twilio primary docs and actual account; never invent provider entitlement.

No real student/borrower phone number or live SMS without explicit safe authorization. A Boolean in demo JSON cannot establish real-world legally valid consent. Prototype uses only fictional/controlled destinations.

## Suggested interface

~~~java
public interface FakeTwilioApi {
 @FormUrlEncoded
 @POST("fake_twilio/send")
 Call<FakeTwilioReply> send(
   @Field("To") String to,
   @Field("MessagingServiceSid") String sid,
   @Field("Body") String body,
   @Header("Authorization") String dummyAuth
 );
}
~~~
Initialize Retrofit with verified baseUrl ending '/' and GsonConverterFactory. DTO fields body,to,status,messaging_service_sid via @SerializedName. Observe actual HTTP status, body and error stream; status acceptance != delivery.

## Return reminder eligibility

Project ends in upcoming 24h: endAt -24h <= now < endAt.
Status ISSUED (reusable only), smsConsent true, valid phoneNumber, reminderClaimedAt null.
Avoid REMINDERS for RETURNED/CANCELLED/CONSUMED or overdue after end. ISSUED after end remains custody but no new upcoming reminder; this is faithful to original proposal and should not silently become overdue SMS.

## Claim/dispatch lifecycle

Scanner @Scheduled query selects bounded IDs.
Transaction claimOne(id): lock Allocation, re-read status/consent and relevant current Project end, check unclaimed, set reminderClaimedAt=now and outcome CLAIMED, COMMIT.
Outside DB tx, last eligibility and consent check; if newly RETURNED/revoked, mark SUPPRESSED. If still eligible, form POST through Retrofit. Rejected definitive response -> REJECTED. Fake accepted -> SIMULATED. Real provider accepts -> PROVIDER_ACCEPTED. Network timeout, crash after claim -> UNKNOWN (don't automatically resend). Result persistence should only update reminder fields while locking Allocation; never save stale Allocation object and overwrite returned status.
Do NOT hold open database transaction while calling remote HTTP. Do NOT guarantee perfectly synchronized revocation/return and external dispatch after it starts; document unavoidable race.
Do NOT automatically retry uncertain send. Stale CLAIMED record can be reconciled to UNKNOWN without another send.

## Expiry job

@Scheduled separately from SMS; actual @EnableScheduling on Spring Boot app; DO NOT copy example's excluded datasource autoconfiguration. At/after Project.endAt, find PENDING or APPROVED, lock Project -> Resource -> Allocation, recheck current status/end, set CANCELLED with reason EXPIRED and cancelledAt. For APPROVED, stock effective hold was already zero at end; cleanup should not release again. For PENDING, no stock effect. For ISSUED overdue, no auto return. Preserve approvedAt historical marker.
Demonstrate actual invocation; a manually called method alone doesn't establish Scheduling requirement.
Configurable interval, ordinary 1h, short local demo profile for observed scan. Inject Clock for deterministic tests; no public time-modification endpoint.

## Security boundary

No authentication/authorization in baseline; one trusted local operator. Server bind 127.0.0.1 by default, no unrestricted public host. No claims of production security. UI About/Demo Guide labels as fictional academic prototype, not official Ateneo service. Do not show fake login/role UI implying real access control.

Data minimization: borrowerName fictional; phone only if consent true; revocation clears destination; API responses never expose raw number; logs never print destination or auth; prevent frontend state echo of persisted contact values.
Secrets via environment, not repo, README, Postman exported environment or CI log. Gitignore .env, test output, dumps, node_modules, target, credentials, user-specific settings. Avoid blanket ignoring required Maven wrapper or Eclipse metadata if needed for reproducible import.
No unsafe CORS wildcard + credentials; same-origin UI with Vite local proxy. Browser cannot override server-controlled IDs/status or computed values; DTOs narrow, server validation and DB constraints still apply.
Handle malformed JSON, invalid enum, invalid LocalDateTime, overflow/out-of-range quantity, duplicate normalized names, unknown FKs, concurrency, invariant failure, SQL errors with sanitized responses.
Do not expose stack traces/SQL in UI; technical logs use non-sensitive IDs. Browser must show errors without falsely claiming success.
Conditional image feature: validate actual PNG/JPEG signature, MIME, size; no filesystem paths or untrusted SVG/scripts, store bytes in Resource if confirmed required.

## Test controls

SEC01 loopback listener; SEC02 credentials excluded; SEC03 redacted Allocation JSON; SEC04 consent=false destination null; SEC05 server-controlled fields protected; SEC06 DB FKs; SEC07 no broad CORS; SEC08 simulator dummy Authorization; SEC09 no SQL leaks; SEC10 stale update refused; SEC11 frontend escapes text; SEC12 no fake success on lost POST; SEC13 demo seed non-destructive; SEC14 no unsolicited provider sends; SEC15 upload safe if added; SEC16 GitHub CI logs sanitized.

SMS01 disabled startup; SMS02 expiry independent; SMS03 eligible claim one; SMS04 second scan none; SMS05 return before claim; SMS06 revocation before claim; SMS07 return between claim and pre-dispatch recheck; SMS08 real simulator accepted JSON; SMS09 error status; SMS10 timeout unknown; SMS11 restart no duplicate; SMS12 window boundaries; SMS13 no physical delivery claim; SMS14 missing destination suppression; SMS15 no real secret in simulator logs.

## Privacy and academic integrity

No real campus inventory, demand measurements, interviews, real user accounts or provider approval asserted. No actual project Java code/DB/SMS tests are claimed by this file. Document outside AI/research assistance truthfully as required by syllabus Certificate of Authorship. Check Canvas for explicit generative-AI policies; cannot infer from syllabus absence.
