# CP30 — Single-runtime compatibility and zero-mandatory-cost continuous deployment feasibility

**Research checked:** 2026-11-16 (Asia/Manila). **Status: OFFICIAL DOCUMENTS REVIEWED; ZERO UNIFIED PROTOTYPES OR HOSTS/CI PIPELINES BUILT OR TESTED.** Links below are dated verification sources, not proof of OrgResource successful integration, provider eligibility, live SMS, uptime or no charges.

## 1. Highest-risk constraint: one product AND genuine Javax + maintained Jakarta platform

### Verified dated facts

- Spring Boot 4 is based on **Jakarta EE 11**, requiring Java 17+; its documented validation mechanism uses **`jakarta.validation`**. Spring's official [Boot 4 migration guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide) and [validation reference](https://docs.spring.io/spring-boot/reference/io/validation.html), accessed Nov 16.
- Spring Boot 2.x open-source support ended with **2.7.18 in November 2023**, [Spring release announcement 2023-11-23](https://spring.io/blog/2023/11/23/spring-boot-2-7-18-available-now/). "Use Boot2 in production indefinitely at no support cost" is not a safe maintained-stack choice.
- [Hibernate Validator official compatibility/release table](https://hibernate.org/validator/releases/), accessed Nov 16, records 9.1.6 as current stable (Jakarta Validation 3.1) and 6.2.6 as **limited support**, retaining `javax.*` API. Official [6.2 series](https://hibernate.org/validator/releases/6.2/) lists Java8/11/17 and Spring Boot 2.4–2.7 integration, **not Boot4**.
- The older `jakarta.validation-api:2.x` *Maven coordinate* contains **Java `javax.validation` package names**, while Validation3.x contains **Java `jakarta.validation`**. Maven artifact name does not establish actual API compatibility. See [HV6.2](https://hibernate.org/validator/releases/6.2/) and [HV9 reference](https://docs.hibernate.org/stable/validator/migration/en-US/html_single/).
- The O14 source's `RequestValidation` and JPA entities actually use literal `javax.validation`; Spring Boot4's normal Jakarta validation does not automatically apply these constraints. Direct invalid `saveAndFlush` evidence is required, not just JSON 400 or SQL NOT NULL.
- Jersey4/Jakarta REST can still fulfill the assignment's **Jersey REST** technology because the assignment does not pin a `javax.ws.rs` namespace, but this does **not** remove the separate literal Javax Validation conflict; check exact library/provider before claiming.

### Three options and honest trade-off

| Architecture | One user-facing product? | One backend process? | Literal course compliance? | Production support/cost | Current decision |
|---|---|---|---|---|---|
| A. **Preferred experimental** supported Java17+/Spring Boot4/Jersey4/Jakarta ORM plus an explicitly isolated `javax.validation` execution/adapter module | Yes | **Potentially yes** | **NOT PROVEN**: javax DTO and actual JPA entity invalid-save must pass | Legacy provider limited support / CVEs need dependency review; no guarantee | Test in **UNIFIED-COMPAT-01** first |
| B. One deployed product/hostname/UI/database with separately isolated internal course-compatibility process behind same authenticated gateway | Yes | **No** (two internal runtimes) | Potential if legacy lane really runs, but public legacy security/support boundary complicated | Higher RAM/patch load; must not be hidden or called "one backend" | Only owner-reviewed fallback if A fails; not automatic |
| C. Extend original Boot2.1/Jersey2/javax backend into public service | Yes | Yes | Original namespace likely retained | **Unsupported legacy Spring**; security support not free; unacceptable unreviewed public exposure | Not recommended for production |

Other single-runtime technologies (supported Open Liberty or alternate servers) may be evaluated if A fails, but must still really execute the original Spring `@Autowired`/Data/JPA/MySQL/Jersey and exact course validation, not replace them with superficially similar APIs.

**Decision rule:** Preserve the goal **one product**. Do not claim option A compatible without tests, don't silently choose option B, and don't sacrifice literal course features. If all technically supportable $0 one-process solutions fail, provide the factual failure report and ask for an explicit trade-off decision. One product does not magically eliminate a namespace conflict.

## 2. Real Twilio vs unlimited free service

Official [Twilio trial rules](https://www.twilio.com/docs/usage/tutorials/how-to-use-your-free-trial-account), accessed Nov16: **30-day** trial, free-unit limits, verified recipient and predefined-template restrictions; must upgrade for unconstrained usage. [Twilio Virtual Phone](https://www.twilio.com/docs/messaging/guides/guide-to-using-the-twilio-virtual-phone) is a simulated recipient for actual Twilio API messaging; [Twilio test credentials](https://www.twilio.com/docs/messaging/tutorials/automate-testing) simulate API responses without real SMS or charges. A local FakeTwilio HTTP service tests Retrofit/Gson wiring but is **not** Twilio's actual provider.

| Mode | Real HTTP? | Real Twilio API account? | Physical carrier SMS? | Free long-term? | Acceptable statement |
|---|---|---|---|---|---|
| T-A local FakeTwilio via Retrofit/Gson | Yes to loopback | No | No | Local free | "Simulator transport integration passed" |
| T-B official Twilio test credentials | Provider-mode API simulation, not real delivery | Requires eligible test credentials | No | May not incur SMS charge | "Twilio test response accepted" |
| T-C Twilio Virtual Phone via permitted trial | Yes | Yes, account-specific | No (Virtual Phone simulated) | Trial expires | "Actual Twilio API reached Virtual Phone" |
| T-D actual recipient SMS | Yes | Yes | Yes | **No indefinite free guarantee** | "Real provider delivery status observed" only with evidence |

**One-product recommendation:** keep scheduled reminder pipeline and genuine Retrofit+Gson provider adapter *working*, configurable safely; use an always-available **in-app** reminder fallback when a paid/Twilio provider is unavailable, record exact status honestly, never mark simulated or failed provider action as delivered. Whether the course actually demands T-C vs T-D is INSTRUCTOR/CANVAS OWNED and cannot be waived. If user requires permanent unlimited T-D at ₱0, mark this combination **UNSATISFIABLE TO GUARANTEE** absent a new verified provider.

## 3. Hosting free-tier candidate and uptime boundary

- [Oracle official Always Free overview](https://www.oracle.com/cloud/free/) and [Compute eligibility/idle instances](https://docs.oracle.com/en-us/iaas/Content/FreeTier/freetier_topic-Always_Free_Resources.htm), accessed Nov16: free **A1 ARM 1,500 OCPU-hours, 9,000 GB-hours monthly** (roughly two OCPU and 12GB continuously within applicable limits), home-region/shape capacity and service quotas; needs appropriate credit/debit card verification. NO automatic free VM availability.
- Oracle says low-use free compute **may be reclaimed** if its 7-day CPU/network/memory thresholds hold. Therefore **24/7 uptime and unrestricted months of uninterrupted service cannot be guaranteed** by terms.
- One VM can **plausibly** host a modern JVM app + MySQL8.4 + reverse proxy + static UI if measured RAM/storage/network permit. But maintenance, in-place updates, MySQL patching, memory and disk pressure are our responsibility. Oracle's managed MySQL.Free is another *conditional* choice with unpinned DB version and limited backup options; don't assume equivalence to pinned MySQL8.4.
- Current user's GitHub repo is PRIVATE; do not make it public just to obtain free unlimited CI or public environment approval. Historical complete archives and academic submissions require a privacy/material review first.

## 4. Free HTTPS without buying a domain

- [Let's Encrypt GA announcement dated January 15, 2026](https://letsencrypt.org/2026/01/15/6day-and-ip-general-availability) establishes **IP-address certificates**, short-lived **160 hours**. This is a potential no-domain HTTPS route if a stable publicly reachable IP and compatible ACME client exist.
- Require proven automated issuance+renewal, webserver reload, monitoring/alerts and recovery from IP replacement. At least one real successful renewal is insufficient to guarantee perpetual uptime; tests should cover renewal failure and alert delivery.
- [Cloudflare named Tunnel guide](https://developers.cloudflare.com/tunnel/get-started/) requires a Cloudflare-managed domain for publishing; Cloudflare free DNS alone does NOT grant a domain name.
- [Cloudflare Quick Tunnel docs](https://developers.cloudflare.com/tunnel/get-started/quick-tunnels/) explicitly describe temporary `trycloudflare.com` and no uptime guarantee, suitable for development **not** continuous production.

## 5. GitHub Actions CI/CD: genuinely free allowance, not unlimited

Verified [GitHub current Actions billing](https://docs.github.com/en/billing/concepts/product-billing/github-actions), accessed 2026-11-16: standard GitHub-hosted jobs in **public** repos are free; **private** repos draw from plan included compute/storage and may accrue charges beyond allowances if billing permits; self-hosted runners don't draw from hosted compute but have severe security constraints. Exact account allowance must be read from GitHub billing UI; no account-specific entitlements inferred.

Verified [GitHub environment availability](https://docs.github.com/en/actions/how-tos/deploy/configure-and-manage-deployments/manage-environments): GitHub Free **private** repos lack certain environments/environment secrets/protection rules. GitHub repository-level Actions secrets exist but must not be mistaken for environment approval. [GitHub secrets docs](https://docs.github.com/en/actions/how-tos/write-workflows/choose-what-workflows-do/use-secrets).

Verified [GitHub secure use guidance](https://docs.github.com/en/actions/reference/security/secure-use): persistent **self-hosted runner on production** is dangerous, even on private repositories; it executes repository code with access to host and potentially sensitive tokens. Don't install general-purpose runner on the live database VM just to avoid quota.

Verified [GitHub continuous deployment reference](https://docs.github.com/en/actions/get-started/continuous-deployment): workflows can deploy on controlled release triggers with job concurrency. These are **capabilities**, not an existing OrgResource CD workflow or proof of no charges.

### Safest provisional zero-mandatory-cost route
1. Keep private repo, secrets private; use **GitHub-hosted** standard ephemeral runners within account's verified included allowance, budget/usage alerts and stop-on-exhaustion. No public release of archived course files.
2. Test/scan/build immutable source-SHA artifact on trusted authorized branches. Pull requests run no deployment secrets, have read-only repository token, no privileged untrusted workflow code. Do not deploy on every PR or automatically merge.
3. Deploy **only after an approved release ref/merge**, with least-privileged repository secret or short-lived approved key to a restricted SSH deploy principal; compare pinned artifact SHA on host.
4. On host, take verified pre-deploy independent DB backup/snapshot, validate migrations and blue/green or restart strategy, serve health/readiness, smoke API+UI/DB, and rollback app artifact automatically on failed health. Schema migrations may need forward fix; do not pretend blind rollback of data is always safe.
5. Pin deploy concurrency=1, explicit manual emergency stop, known-good release, preserve zero-charge operation if Actions quota exhausted: **stop future CD rather than incur charges**. CDN/domain not assumed.
6. No public environment claim/production uptime PASS before actual free server eligibility, IP HTTPS, automatic renewal, full restoration and real approved deployment executed.

**Tradeoff:** "continuous deployment" can mean repeatable automatic *approved releases*. It cannot promise automatic release while quota exhausted or hosting unavailable. A deliberate FAIL-CLOSED pipeline meets no mandatory payment but may pause deployment.

## 6. Disaster recovery and privacy
The plausible cheapest host still has a **single failure domain**. Same-tenancy storage snapshots, a GitHub repository ZIP or CSV export are not sufficient to prove independent recovery. Need separately controlled encrypted off-provider backup with no required charges and a **real** compatible clean MySQL restore. Include database consistency, stock ledger, users, recovery material/session revocation and eventual restart. No third-party data or credentials exposed during proof. Prior [CP29](53_CP29_MYSQL_IDENTITY_HISTORY_DR_CONTRACT_CANDIDATE.md) covers restore of stale sessions and credential risks.

No provider account was created, card used, external SMS sent, deployment started, VM reserved, certificate obtained, or hosted response measured during this research.

## 7. Dated primary source register (accessed Nov 16, 2026)

| Category | Authoritative page | Factual scope |
|---|---|---|
| Spring4 platform | [Official Spring Boot4 migration](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide) | Java17+, Jakarta EE11 |
| Spring validation | [Official Boot validation reference](https://docs.spring.io/spring-boot/reference/io/validation.html) | Native Jakarta API |
| Legacy Spring support | [Spring Boot2.7.18 Nov23 2023](https://spring.io/blog/2023/11/23/spring-boot-2-7-18-available-now/) | Open-source support ended |
| Validator versions | [Hibernate Validator releases](https://hibernate.org/validator/releases/) | HV9.1 modern Jakarta; HV6.2 limited javax |
| Oracle host | [OCI Always Free docs](https://docs.oracle.com/en-us/iaas/Content/FreeTier/freetier_topic-Always_Free_Resources.htm) | Quotas, cards/capacity/reclaim |
| GitHub free CI | [Actions billing](https://docs.github.com/en/billing/concepts/product-billing/github-actions) | Included allowance vs charges |
| GitHub private protections | [Environments docs](https://docs.github.com/en/actions/how-tos/deploy/configure-and-manage-deployments/manage-environments) | Free private limitations |
| GitHub runner safety | [Secure use](https://docs.github.com/en/actions/reference/security/secure-use) | Persistent self-hosted risk |
| GitHub CD | [Continuous deployment](https://docs.github.com/en/actions/get-started/continuous-deployment) | Workflows, concurrency and deployment |
| Free IP TLS | [Let's Encrypt Jan15 2026](https://letsencrypt.org/2026/01/15/6day-and-ip-general-availability) | 160h IP certificates |
| Cloudflare tunnel | [Named Tunnel](https://developers.cloudflare.com/tunnel/get-started/) and [Quick Tunnel](https://developers.cloudflare.com/tunnel/get-started/quick-tunnels/) | Domain requirement vs temporary testing |
| Twilio free trial | [Twilio trial](https://www.twilio.com/docs/usage/tutorials/how-to-use-your-free-trial-account) | 30 days, verified recipients/templates |
| Twilio virtual/test | [Virtual Phone](https://www.twilio.com/docs/messaging/guides/guide-to-using-the-twilio-virtual-phone), [Test credentials](https://www.twilio.com/docs/messaging/tutorials/automate-testing) | Provider simulation vs carrier delivery |

**Bottom line:** All tools may be free-to-use for a controlled small academic demo under verified allowances; **permanent unlimited free production uptime+carrier SMS+CI volume are not established**. One supported runtime with full literal javax JPA validation is the highest-risk compatibility gate and must be tested FIRST.
