# C6 — exact versioned stack and current dependency advisory boundaries

**2026-11-19 PHT. Verified source versions; no build/npm audit/Java runtime commands executed.** This is compatibility and release-risk research for **two distinct isolated deliverables**, not authorization to upgrade frozen Course A, replace class APIs, start C7, or assert dependencies safe.

## 1. Source-exact dependency matrix

| Lane | Exact source at frozen/reviewed SHA | Observed declared technology | Source/evidence status |
| --- | --- | --- | --- |
| Academic Course A | `backend/pom.xml` Git blob `65293736b992e4db708b00b3fe6f52060ae2f787`, immutable code `7f8c0746da18fd54b47f430ac1435ac36e605d67` | **Java 8** `java.version=1.8`; **Spring Boot 2.1.0.RELEASE** parent; **Jersey 2.26**; **MySQL Connector/J 8.0.13**; **Retrofit 2.4.0**; **converter-gson 2.1.0**; Spring Data JPA/Hibernate, javax bean validation | VERIFIED DECLARATIONS ONLY. Actual Maven effective tree, Eclipse import, local database/server versions and HTTP integration **NOT_RUN THIS PASS** |
| Extended C5 | `extended/backend/pom.xml` blob `2eb4209e67e974fdc99a3c65d8683c9758b4e294`, packaged code SHA `14576b57fff44c6f0112fd37d8c237ca0122efb4` | **Java 21**, **Spring Boot 4.1.2**, **Retrofit 2.11.0**, **Gson 2.11.0**, **OkHttp 4.12.0**, javax validation 2.0.1.Final and Jakarta validation, **Jersey via Spring Boot** and Spring Data JPA/Spring Security/Spring Session/Flyway/MySQL JDBC | VERIFIED DECLARATIONS; C5 reports historical MySQL 8.0.46/Java21.0.3 local tests, **REPORTED_ONLY** |
| Extended React | `extended/frontend/package.json` blob `98bbadf846495f05b284cac211487336ff059d92` | React **19.3.1**, React DOM 19.3.1, React Router **7.18.5**, Vite **8.3.8**, Vitest **5.1.0**, TypeScript **5.9.3**, Playwright **1.65.1**; Node engine **>=20.19.0** | Package source read, lockfile parsed. No new `npm ci/test/lint/build/e2e` run |
| Extended lockfile | `extended/frontend/package-lock.json` blob `aca7c7a1d094535ceabb9ee01f3536b839d93efe`, lockfileVersion 3 | `node_modules/picomatch@4.0.7` present in dependency tree | Exact locked package version verified; current vulnerability behavior and dependency reachability NOT_PROVEN |

**Source authority:** Uploaded CSCI 180.05 assignment/syllabus and supplied labs/code govern Academic A. The course requires Jersey/JSON/Spring/JPA/Javax/Retrofit/Gson/scheduling/Postman, with Java8-era examples. The modern Java21 Extended lane ADDS functionality and must not replace/rewrite A or its effective dependencies, even if contemporary best practices use newer libraries. Exact current Spring Boot release is external changeable research; frozen POM is the build contract.

## 2. Current 2026-11-19 picomatch advisory state: avoid both false positives and false negatives

- **VERIFIED SOURCE:** The C5 report `evidence/C5/PHASE_REPORT.md` reports **12 high development-only npm audit findings** all rooted in one Picomatch GHSA, and **zero production-only** findings at the **earlier reported audit**. The report says no compatible patched release at that time. That is historical *reported* execution, not the result of a new registry audit.
- **VERIFIED SOURCE:** Current Extended frontend lockfile contains `picomatch@4.0.7` (the package itself can be used transitively in developer tooling).
- **VERIFIED EXTERNAL FACT, checked 2026-11-19:** Official [GitHub Advisory GHSA-fc5q-39xc-vjpf](https://github.com/advisories/GHSA-fc5q-39xc-vjpf) now presents the historical ReDoS issue as a **WITHDRAWN advisory, withdrawn on November 18, 2026**, despite a related published unreviewed record [here](https://github.com/advisories/GHSA-fc5q-39xc-vjpf). Search results and mirrors may show different historical/cached severity labels. Withdrawal does **not** itself prove package behavior safe, nor that no replacement advisory exists. Do not state the high finding remains currently active solely from old C5 output; equally do not claim all vulnerabilities cured.
- **OPEN QUESTION:** Current fresh `npm audit` behavior with public registry, dependency direct/indirect provenance, actual attack surface (patterns from untrusted source vs dev-only controlled patterns), current advisory and any patched compatible version. Must use fresh current registry and repeat independently after Actions runner quota or local environment is available. Any mitigation must preserve class/test/build quality; don't replace `picomatch` with a forced arbitrary override without safe tests.

**Required exact commands** for an authorized, clean disposable checkout with Node/npm versions recorded:
```sh
cd extended/frontend
npm ci
npm audit --json > audit-all.json || true
npm audit --omit=dev --json > audit-production-only.json || true
npm ls picomatch
npm run typecheck
npm test -- --run
npm run lint
npm run build
```
Review the contents of `audit-*.json` and commands' exit codes. The `|| true` on audit capture ensures JSON files are produced; **the underlying npm audit status and severity MUST be inspected separately** before any gate. Avoid publishing raw logs with real local secrets. Playwright should run against the packaged actual Java21/MySQL origin at the accepted SHA after build.

## 3. Academic and Extended compatibility / current risk decisions

1. **Course A SOURCE IMMUTABLE (USER DECISION / COURSE REQUIREMENT):** no Spring Boot 4, Java 21 or Jakarta replacement in A. Keep Java8/Jersey2/Javax and the as-built academic postman and image interfaces. Legacy POM versions may be old, but 2026 internet security concerns do not authorize silent course substitution. Production hosting must use isolated hardened Extended lane, not A.
2. **Extended C5 BOOT VERSION SOURCE VERIFIED:** current POM says Spring Boot 4.1.2 + Java21; actual Maven resolution/jar and MySQL/Java library startup still NOT_RUN this phase. Verify official project system requirements at https://docs.spring.io/spring-boot/system-requirements.html and relevant source test branches before any version bump.
3. **PRODUCTION REQUIREMENT:** track Java21 patched security release, MySQL8 support/runtime SSL grants, frontend dependency vulnerabilities, HTTP cookie/CSRF/Host/proxy behind real HTTPS, and build reproducibility. Exact deploy versions change over time; repeat current authoritative lookup at C9/C10 instead of freezing this research as a perpetual safety guarantee.
4. **Storage/speed constraints:** measure **actual** staged 96-org import, ledger query, history pagination, build cold/warm time, raw SQL backup bytes and 64 MiB restriction before claiming optimized production. No AOT, Redis, message broker or microservices without tested need; no quality gate removal.

## 4. Evidence requirements before claiming any acceptance

**RUN STATUS:** `mvn clean verify` Java8 with real MySQL, `mvn clean verify` Java21 with Flyway, `npm ci/typecheck/test/lint/build/audit`, real packaged Newman, Chromium, off-host restore and TLS ingress **NOT_RUN in this C6 read-only pass**. Prior C5 report remains `REPORTED_ONLY`, subject to exact SHA and independent scrutiny. GitHub private Actions runner allowance exhausted (USER REPORTED) prevented runner job startup; zero steps is **NOT_RUN**, not proof of failing application tests.

**C6 conclusion:** source declaration audit completed for the listed POM/package/lockfile refs; security advisory remains **dated external source plus actual locked version, not a new npm audit**. No version updates should be performed merely to make this document claim GREEN.
