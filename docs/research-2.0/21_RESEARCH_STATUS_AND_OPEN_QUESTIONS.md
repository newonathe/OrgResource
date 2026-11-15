# Research completion inventory and unresolved evidence
**Status:** RESEARCH COMPLETE AS A PROVISIONAL DOCUMENTATION PACKET; SOURCE COVERAGE and IMPLEMENTATION VALIDATION remain PARTIAL. **Date:** 2026-11-15. Updated when remote commit verified. Source: [SOURCES](SOURCES.md), [02](02_AUDIT_AND_SOURCE_COVERAGE.md).

## What is actually accomplished vs not
| Area | Current status | Precise evidence and limitation |
|---|---|---|
| Context / private GitHub read | PARTIAL SOURCE AUDIT; LIVE REPO METADATA VERIFIED | Connected repo main/PR#1–#7, current PR#6 `fd34a8...`, reviewed key files, original course text; full 319,799-char historical master not yet semantically inspected |
| Course requirements | RESEARCHED & MAPPED, NOT course-final acceptance | Assignment/syllabus parsed, CLO9 still unresolved; Canvas not accessed; actual source compile not independently rerun |
| Ateneo landscape | SECONDARY PUBLIC RESEARCH VERIFIED, no stakeholder primary data | Public OSA/RecWeek/CFMO/MEA/GUIDON sources and dated caveats |
| Alternatives | DOCUMENTED, not benchmarked or product-switched | Spreadsheet baseline, Snipe-IT, provider and FOSS directions; no hands-on Snipe-IT pilot |
| Functional/quality requirements | **SPECIFIED**, not user-approved | F01–F26, N01–N18, invented synthetic bounded workload |
| EERD/state/inventory | **PROPOSED DESIGN**, not migrated | 11-table tenant-aware draft, conservation and time-window arithmetic, risk of late return |
| Security/privacy | **THREAT-MODELED**, no security audit | Philippine RA10173/NPC primary public sources, planned tests |
| Version/hosting | **RESEARCHED PROVIDER TERMS**, no selected/deployed host | OCI/D1/Render/Supabase/Google and Java/Jakarta boundary; $0 live sustainability unverified |
| Test/DevOps | PLANNED, NOT RUN | Detailed Postman/properties/concurrency/restore/CI gates, no V2 test binary |
| Research docs GitHub | COMMITTED/PUSHED when this file appears in remote verified commit | docs-only research branch + draft PR#7, no main merge |
| Product code | **NO V2 CODE CHANGED HERE** | This research writer altered only Markdown files |
| Actual live users/pilot | **NONE OBSERVED/CLAIMED** | Fictional only; no outreach, survey, signup, SMS or email |
| O14 | NOT VERIFIED | PR#6 covers O11/O13 local reports but no O14 course acceptance/Canvas evidence |

## Essential questions requiring evidence, not current interviews
| Priority | Question and why it matters | Who/source can resolve, and when |
|---|---|---|
| P0-academic | Current Canvas changes after original C01 deadlines? Actual O2/Part I submission and final O14 evidence? | Student logged-in Canvas, before Dec 5 |
| P0-academic | Must project implement CLO9 image transfer and is FakeTwilio simulation sufficient? | Current rubric/instructor direction; prior to claim of complete course compliance |
| P0-security | Who is responsible product operator and backup key custodian after graduation? | Product owner before any hosted real user |
| P0-product | Is future org creation invite-only, and what evidence grants a real org OWNER? | User design choice now; genuine org representative only for later optional real pilot |
| P0-cost | Is OCI/other free hosting eligibility (card hold, home-region capacity) acceptable and actually available? | Actual official account test only if user authorizes enrollment |
| P0-auth | Which auth route (supported OIDC/password/passkey) can safely operate at $0 incl recovery and invite delivery? | Provider official current terms + local security trial |
| P0-domain | Whole-batch versus partial **usable** returns in V2 P0; confirm damage/loss deferred? | User decision; does not block initial course preservation |
| P0-db | Which real transactional DB target will meet isolation/concurrency/backup/quotas on free provider? | Supported-stack benchmark & race/restore tests |
| P1-ui | Exact v5 UI mockup not exported, should verified React implementation be canonical? | User design review; G10 is current recoverable spec |
| P1-ops | What is realistic dataset growth, and where do off-host encrypted backups live for $0? | Synthetic benchmark then actual free service eligibility |
| P2-later | Do actual Ateneo orgs want it? Who owns their equipment and what current tools exist? | Optional **future** authorized representative discussion/pilot, not required for research |
| P2-later | OSA/CFMO/IT/privacy and organizational policy for actual campus data or integrations? | Authorized university/legal channels only if later official integration |
| Source gap | Can the two original COMPLETE_SOURCE_NONVIDEO ZIPs be opened and hashes independently matched, and 319,799-char historic master fully reviewed? | Accessible local file runtime + targeted GitHub chunk reading; not claimed done |

## Immediate independent critique status
The independent critique in [20](20_INDEPENDENT_CRITIQUE_AND_REVISED_DECISIONS.md) identifies stale counts, false adoption/lifetime-free premises, hidden tenant security, booking risk, auth role capture, backups, unsupported javax runtime, limited maintenance and the actual no-GO boundaries. Decisions in [DECISIONS](DECISIONS.md) remain recommendations until user review.

## Research release handoff for user's separate planning/Codex-prompt chat
Read this INDEX then SOURCES, 02/03, 06–07, 08–13, 14–15, 17–20. Select (not assume) tenant/auth, stock partial, supported stack, backup and free provider. Use exact F/N/T gates to author sequential implementation prompts in separate chat after academic O14 review. Require fresh remote+local Git audits and no unapproved main merge/public deployment. All production specs are proposed, not “existing” application behavior.
