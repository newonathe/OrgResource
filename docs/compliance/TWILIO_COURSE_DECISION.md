# Twilio and external-service course decision

> **Course A status (2026-11-17):** A separate Retrofit/Gson Twilio Messages REST protocol transport is implemented. `TwilioRestTransportTest` exercises its path, form, Basic auth, acceptance/rejection parsing, malformed/lost responses, bounded timeout and no-retry behavior against a fictional loopback server. MySQL lifecycle evidence verifies `PROVIDER_ACCEPTED` persistence without claiming delivery. The adapter remains disabled by default, and no provider account, real SMS or carrier delivery was used. Instructor acceptance of the assignment wording remains **NOT_VERIFIED**; Canvas was not checked. See `evidence/COURSE-A/A1_A2_ACCEPTANCE_REPORT.md`.

**Current technical status: local Twilio protocol verified. Assignment/instructor acceptance: NOT_VERIFIED.** No live Twilio account or SMS was used.

## Evidence

- The assignment, `ChatGPT-Project-Pack/UPLOAD/09_ESP_PROJECT_ASSIGNMENT.pdf` p. 1, lists “RetroJit” for connecting to external services and says “at the very least Twilio.” The print copy does not say whether a local protocol-compatible simulator satisfies this provider-specific wording.
- The syllabus, `ChatGPT-Project-Pack/UPLOAD/08_CSCI180_SYLLABUS.pdf` p. 2, names CLO8 as communicating by JSON over HTTP using Retrofit and GSON. Its p. 3 outline lists that CLO separately from the image outcome and the final project.
- The supplied Lab 4 `FakeTwilio` source implements a loopback `POST /fake_twilio/send` form endpoint using `To`, `MessagingServiceSid`, `Body`, and `Authorization`, and returns an `accepted` response. It is a teaching simulator, not the Twilio provider. Its sample controller prints the Authorization header, destination, and body; those sample logging practices are not copied as an OrgResource credential/privacy policy.
- OrgResource uses Retrofit/Gson with an equivalent loopback-only FakeTwilio transport and dummy authorization. `FakeTwilioTransportTest` and `ReminderLifecycleMySqlTest` exercise the local HTTP response, persistent reminder outcomes, redaction, single claims, and uncertain-result handling. Reminder sending is disabled by default.
- No live provider credential, paid account, real destination, handset, or delivery receipt was used. No real SMS was sent.

## O14 decision (historical)

Count Retrofit/Gson and simulator transport as **verified technical course-pattern evidence**. Do not claim that this settles the assignment’s “at the very least Twilio” requirement. Until the current rubric or instructor direction confirms simulator acceptance, mark the provider-specific academic requirement **UNRESOLVED**. Do not add live Twilio credentials or send test messages in O14; the user has reserved production work for a later reviewed prompt.

## Student clarification

> For the final project, does a Retrofit/Gson integration with the supplied local FakeTwilio protocol satisfy the assignment’s “at the very least Twilio” requirement, or must the application call the actual Twilio service? If actual Twilio is expected, what safe test account, consent, cost and evidence requirements should be used?

No message was sent. The student checks current Canvas or asks the instructor before claiming this requirement is accepted. If actual provider behavior is required, implement it only after approval and with explicit consent/cost controls; never include credentials in Git, recordings, logs, or source exports.
