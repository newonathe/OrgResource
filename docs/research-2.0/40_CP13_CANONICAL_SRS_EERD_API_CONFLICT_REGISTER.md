# CP13 — Canonical source-first SRS, EERD, API and course preservation conflict register

**Dated 2026-11-15 (Asia/Manila).** Conflict audit COMPLETE at research level; architecture BLOCKED, not user-approved or implemented.

## Authority and evidence

**COURSE REQUIREMENT:** Attached `09_ESP_PROJECT_ASSIGNMENT.pdf` pp1–3 and `08_CSCI180_SYLLABUS.pdf` pp2–4 were reopened. Actual Spring Boot/Java, Jersey REST JSON, `@Autowired` controller→component, Spring Data JPA/Hibernate/**MySQL**, JPQL, **literal `javax.validation`**, Retrofit/GSON external service at least Twilio, scheduling, five justified entities, GET/POST runtime create/read/delete, full Postman. Syllabus CLO9 image scope and current Canvas instructions remain unresolved.

**ORIGINAL SOURCE:** `10_ORGRESOURCE_CONCISE_PROPOSAL.docx` pp1–12 reopened: Department/Project/Resource/ResourceRequirement/Allocation; numeric IDs; Department owns Project; original **global reservation** and **whole-batch return**; 25 proposed operations. O14 PR#8 at `2dd336ed39cecd417a8bce3388b303e688e95dee` adds A09 SMS-consent revocation = 26 Jersey handlers. Original actual app tests NOT rerun in CP13.

**USER DECISION:** All required course technologies must remain fully executable (not archival-only); this conversation researches/verifies, other chat drafts Codex master prompts, Codex builds; P-ACCEPT before A-FINAL and student Canvas submission.

**PRECEDENCE:** Course and user requirements > original source behavior > dated external facts > explicit user-approved design > research recommendations > historical V2 draft. Earlier SRS F01–F26, 11-table ERD and `/api/v2` remain **UNAPPROVED HISTORICAL PROPOSALS**, not build instructions.

## 21 concrete discrepancies

| ID | Earlier draft | Source-grounded correction or open design |

|---|---|---|

| MC-29 | F02 open org self-create | Fictional operator-controlled first OWNER bootstrap; public self-claim NOT approved |

| MC-30 | Verified email invitation | Manual confidential single-use token for synthetic account; no verified-email assertion |

| MC-31 | Owner transfer/disabling account | Effective owner must have ACTIVE account+membership across all owned orgs; old owner's final role OPEN |

| MC-32 | Department omitted | Organization != Department; include Department and same-org Project link to preserve original |

| MC-33 | 11-table count | Local credentials/recovery/ownership + Department + two JDBC session tables => 17 physical if selected |

| MC-34 | Time-window bookings | Original GLOBAL holds; temporal overlap SUM wrongly rejects some feasible requests; optional future peak sweep |

| MC-35 | Partial returns | Original whole usable issued batch RETURN; partial/damage/loss unapproved |

| MC-36 | Archive instead of delete | Original protected runtime DELETE routes remain required; archive is not automatically parity |

| MC-37 | Changed public route contract | Original 26: 10 missing, 15 changed, 1 archive substitution in initial V2 crosswalk |

| MC-38 | TIMESTAMP(6) as universal UTC | New V2 propose DATETIME(6) UTC; preserve original PHT-local original behavior |

| MC-39 | Logical UUID as MySQL type | Choose concrete CHAR(36) or BINARY(16), exact JPA conversion/index/serialization |

| MC-40 | Movement-resource mismatch | Require composite tenant+resource+allocation and requirement FK matching source resource |

| MC-41 | FK MATCH clause | MySQL explicit MATCH can cause ON DELETE/UPDATE ignored; omit and verify RESTRICT |

| MC-42 | 24h HTTP idempotency | Add durable caller-reused operation ID/payload fingerprint + unique(org,operationId) |

| MC-43 | Conflicting lock order | Provisional org-root before resource/requirement/allocation for relevant writes; race review OPEN |

| MC-44 | Cookie mistaken for Authorization | Session uses Cookie header; CSRF; Spring Session JDBC two physical tables if selected |

| MC-45 | In-app activity replaces Twilio | Preserve actual Retrofit/GSON/Twilio demonstration; simulator != provider virtual phone |

| MC-46 | Jakarta replaces literal Javax | Original javax DTO and JPA entity-save behavior must be actively executed; supported modern path UNPROVEN |

| MC-47 | D1/SQLite/Workers alternatives | Not course-carrying substitutes for mandatory Java/Jersey/JPA/Hibernate/MySQL |

| MC-48 | Synthetic load as observed demand | 50 org/500k event synthetic load is not real Ateneo usage or hosting entitlement |

| MC-49 | Checksum-only immutable ledger | Need DB grants, row constraints, durable business ID, reconciliation and off-provider restore |

## Recommended minimal P0 — NOT approved

Use fictional operator-controlled organizations, distinct Department, original five entity/domain concepts, **global no-future-reuse inventory holds** and whole-batch usable returns. Reusable availability `T-H-I`, consumable `T-H`; issue transfers H to I; return reduces I without changing T; consume reduces T and H equally. O14 excludes already-ended APPROVED from effective H before scheduler cleanup; whether to preserve exact expiry detail in new public API remains OPEN. No real institutional status or pilot.

If local identity/JDBC sessions are chosen, provisional **17 physical tables** = initial 11 V2 entities + Department + local_credential + account_recovery_code + ownership_transfer + `SPRING_SESSION` and `SPRING_SESSION_ATTRIBUTES`; not a database migration. UUID physical type `CHAR(36)` vs `BINARY(16)` OPEN; use new UTC `DATETIME(6)` candidate and explicit nonnull composite same-resource FKs, omit `MATCH` in MySQL DDL. Security/auth/host/runtime remain blocked.

An unauthenticated old `/api` must remain restricted; new `/api/v2` with cookie, CSRF, UUID and different errors is NOT drop-in parity to 26 original operations. Actual modern `jakarta.validation` does not automatically prove literal `javax.validation` for DTOs AND entity saves. Pure D1/Workers backend is not compliant with mandatory MySQL Java Jersey stack.

## Future checks — ALL NOT RUN

- **CX-01** original five-entity and all 26 GET/POST/MySQL/Postman routes on pinned O14, incl errors and CRUD

- **CX-02** genuine supported course stack incl Javax validation on DTO AND invalid entity save

- **CX-03** Department, project link, original-to-secured-public compatibility map or owner-signed exception

- **CX-04** approved global T-H-I reusable/T-H consumable with original endAt hold rule clarified

- **CX-05** whole-batch usable return/consume and invalid repeat requests

- **CX-06** operator-controlled fictional bootstrap, no unauthorized campus org assertions

- **CX-07** token invitation one-use, revocation, expiry, race, no fake verified identity

- **CX-08** effective last owner under transfer, suspension, disable across two orgs

- **CX-09** concrete MySQL UUID type, UTC DATETIME6/year2040, correct FK and no MATCH

- **CX-10** raw SQL cross-tenant / wrong-resource composite FK rejection

- **CX-11** 100-way stock approvals and transactional deadlock rollback

- **CX-12** durable RECEIVE operation dedupe after 48h and restart

- **CX-13** complete authorized public route/DTO/error positive and negative matrix

- **CX-14** secure Cookie+CSRF/session invalidation; Spring Session two-table test if chosen

- **CX-15** distinguish FakeTwilio, real Virtual Phone API, actual phone; Canvas/CLO9 check

- **CX-16** verified $0 host, TLS auto-renewal and clean off-provider restore

- **CX-17** synthetic NFR assumptions vs measured MySQL load/cost

- **CX-18** signed owner review of material choices and R0–R10; runtime checks separately performed

## Open owner / evidence questions

Actual supported public Javax provider path, exact 26-route compatibility map, local vs verified identity, old OWNER transfer role, P0 whole/global/Department choice, Twilio/CLO9 Canvas scope, MySQL UUID storage, conditional OCI host/no-charge/DR, and per-path raw ZIP inventory discrepancy. These are NOT authorized by writing this report.

## Remaining estimate and next gate

**~4–8 further focused research passes**, not days, after CP13. Runtime tests (JVM, MySQL races, TLS, real restore) cannot be proven by paper research. Architecture and R0–R10 remain BLOCKED.

## Direct evidence

Attached three original course/proposal files above, GitHub research `08`, `10`, `11`, `12`, [26-operation crosswalk](30_ORIGINAL_26_ROUTE_CROSSWALK_AND_RUNTIME_CORRECTIONS.md), [identity study](32_IDENTITY_STATE_MACHINE_MYSQL_SCHEMA_AND_SECURITY_TESTS.md), [stock/locking study](33_TENANT_MYSQL_RESERVATIONS_LEDGER_AND_GLOBAL_COMPATIBILITY.md), official [MySQL FK MATCH warning](https://dev.mysql.com/doc/refman/8.4/en/constraint-foreign-key.html), [Spring Session JDBC](https://docs.spring.io/spring-session/reference/configuration/jdbc.html), [MySQL UUID functions](https://dev.mysql.com/doc/refman/8.4/en/miscellaneous-functions.html).