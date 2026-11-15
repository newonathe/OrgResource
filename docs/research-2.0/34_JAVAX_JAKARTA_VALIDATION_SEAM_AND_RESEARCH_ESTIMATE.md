# CP7 — genuine javax validation in supported Java/Jersey and remaining research estimate
**Research date: November 15, 2026 (PHT). Status: ARCHITECTURE BLOCKED; RESEARCH NOT ACCEPTED. No code, Java/Maven integration tests, database migration, provider account, payment, hosting, live institutional evidence or deployment performed.** Dedicated research chat only; separate planning chat writes master Codex prompts, Codex executes software.

## I. Estimated remaining research to research acceptance
Estimate based on [pre-build R0–R10](29_PRE_BUILD_RESEARCH_ACCEPTANCE_GATE.md), [current decisions](DECISIONS.md), source-coverage register and CP0–CP6 reports. One pass = one focused, primary-source-based inquiry with actionable conflict/acceptance changes and a verified research-only commit, **not** a calendar day or a guarantee of no new findings.

| Workstream remaining after CP6 | Estimated passes |
|---|---:|
| Literal Javax + supported Spring Boot/Jersey JPA/MySQL compatibility and provider security | 2 |
| Exact Canvas/CLO9/Twilio instruction check (student-provided official evidence if LMS inaccessible) | 1 |
| Original Department/26 route, global booking, state/API and scheduling compatibility finalization | 1 |
| Identity, owner recovery, security/privacy and institutional authority policies | 1 |
| MySQL target physical schema/constraints/locking/inventory state final review | 1 |
| Actual cost/eligibility research for qualifying JVM/MySQL public VM and HTTPS | 1–2 |
| Off-failure-domain backups, key custody, restoration operations | 1 |
| DevOps/CI/test traceability, demo/proposal/checklists | 1 |
| Systematic original ZIP/source-path coverage and conflict re-audit | 1 |
| Independent adversarial final critique and user review | 1–2 |
| **Planning envelope** | **approximately 10–14 focused passes** |

**CP7 is the first in that envelope; after the present investigation approximately 9–13 passes remain**, assuming source access and no unexpected blockers. This is subjective workload planning, not a commitment or forecast of dates. The printed course date is December 5, 2026; current Canvas amendments unverified and schedule high-risk.

**Research "complete" means zero unresolved *material paper-resolvable design ambiguity*, evidence-accountable requirements, versioned compatibility candidates, owner decisions and precise executable acceptance tests.** It does **not** mean actual JVM/Jersey/Javax coexistence, live MySQL race/restore, or public HTTPS/cost have passed. They cannot be verified end-to-end without actually running code and using an authorized service account. Those must remain named NOT RUN and subject to separate approval for a small feasibility proof before product-scale implementation. Do not label paper evidence "E2E proven" or silently replace required technologies.

## II. The exact historical package transition (VERIFIED EXTERNAL FACT)
[Eclipse Jakarta EE FAQ](https://jakarta.ee/about/faq/) and [namespace history](https://jakarta.ee/blogs/javax-jakartaee-namespace-ecosystem-progress/) explicitly state **Jakarta EE 8 used the old `javax.*` packages; Jakarta EE 9 changed them to `jakarta.*`**. Therefore neither Java17 nor Java21 intrinsically bans the `javax.validation` API. The provider, framework integration, classloader and support lifecycle are the actual compatibility questions.

**COURSE REQUIREMENT:** Reopened original attached assignment p1 and syllabus p2 earlier. Explicit required `Javax Validation API`, Jersey REST, Spring Boot/`@Autowired` component delegations, JPA/Hibernate/**MySQL**, JPQL, Retrofit/GSON Twilio pattern, scheduling, five justified entity types and GET/POST CRUD/Postman. No exact Java8 or Jersey2 version mandated by the course text, but the O14 source actually runs Java8/Boot2/Jersey2 and `javax.ws.rs`; its original Department/five entities/26 routes/whole-batch behaviors must remain executable. CLO9 images and real provider Twilio acceptance await current official Canvas interpretation.

**LIVE PRIVATE SOURCE VERIFIED:** O14 PR#8 at `2dd336ed39cecd417a8bce3388b303e688e95dee`: `backend/src/main/java/app/components/RequestValidation.java` contains `@Autowired private javax.validation.Validator validator`, then `validator.validate(request)`, returning HTTP400 on violations. `backend/pom.xml` includes old Boot starter validation/Jersey/Retrofit/Gson and JPA/MySQL; `JerseyConfig.java` uses `javax.ws.rs.ApplicationPath` and genuine Jersey `ResourceConfig`. This is actual **legacy source behavior**, not proof of modern public app integration.

## III. Current supported native validation interfaces — compatibility gap
**[Spring Framework 7 Bean Validation](https://docs.spring.io/spring-framework/reference/core/validation/beanvalidation.html)**: `LocalValidatorFactoryBean` implements native `jakarta.validation.Validator` and `jakarta.validation.ValidatorFactory`, and Spring's `@Autowired` example injects `jakarta.validation.Validator`. It does **not** automatically become a `javax.validation.Validator`.

**[Jersey 4 Bean Validation chapter 20](https://eclipse-ee4j.github.io/jersey.github.io/documentation/latest4x/user-guide.html)**: native extension `jersey-bean-validation` uses `jakarta.validation`. Installing Jersey 4 validation is therefore *not evidence* that `javax.validation.constraints` on an old DTO were checked. Custom explicit Javax validation may still be possible but must be wired and tested, without assuming provider autodiscovery.

**[Spring Boot 4.1.2 managed BOM](https://docs.spring.io/spring-boot/appendix/dependency-versions/coordinates.html)**: current managed Jersey 4.0.3 and Hibernate Validator 9.1-series use Jakarta. **[Hibernate Validator 6.2 series](https://hibernate.org/validator/releases/6.2/)** retains `javax.*` but is **limited-support** and lists Java8/11/17 (not declared Java21). Release list records patch **6.2.6.Final on October 13, 2026**, meaning not absolutely abandoned, but no guaranteed security fixes or full production support. Hibernate Validator6.2 and 9.1 share the Maven coordinate `org.hibernate.validator:hibernate-validator`: Maven dependency management will not load two different versions as distinct isolated providers by simply declaring both.

**[Apache BVal2.0.6](https://bval.apache.org/downloads.html)** is a genuine `javax.validation.spi.ValidationProvider` from June 13, 2022, with separate implementation packages from Hibernate Validator. Its current support/CVE status, use alongside current Boot-managed Jakarta Validator, ServiceLoader selection and annotation coverage are **unverified**.

**[Open Liberty Bean Validation2](https://openliberty.io/docs/latest/reference/feature/beanValidation-2.0.html)** provides actual `javax.validation` features even with modern Java21. Yet [Liberty Spring Boot4 feature](https://openliberty.io/docs/latest/reference/feature/springBoot-4.0.html) explicitly says the Boot application does not auto-integrate with other Liberty REST/security features, and does not promise Jersey is its integrated JAX-RS provider. The EE8 container support fact is **not** integration proof.

## IV. Production candidate decision matrix — no automatic GO
| Candidate | Tech preserved | Material unknown | Status |
|---|---|---|---|
| A: **one Java17 + supported Boot4/Jersey4/Spring Data/JPA/MySQL/Retrofit/Gson/scheduling service** that **explicitly executes real `javax.validation.Validator`** on original-equivalent DTOs | Potentially all named technologies, while public Jersey native validation remains Jakarta; course executable also retained | Javax provider limited support/old releases, ServiceLoader and class conflicts, automatic Jakarta bypass, security/CVE, full 26-route parity. None compiled | **CONDITIONAL RESEARCH CANDIDATE only** |
| B: Open Liberty modern EE8 Javax + Boot4/Jersey4 integration | Maintained EE8 provider + supported Boot if integration works | Container Servlet/Jakarta and JavaEE8 service boundaries, Jersey requirement, classloading and real validation not shown | **UNPROVEN** |
| C: original exact course executable genuinely run privately **plus** modern supported public application | Keeps course Spring/Jersey2/Javax/JPA/MySQL/Retrofit/Gson active, public product supported | User forbids treating archived old code as automatic compliance: requires explicit approval of two-runtime boundary and 26-route/domain contract, not merely archived ZIP | **USER DECISION / NOT APPROVED** |
| D: expose unsupported Boot2 as public product | old types direct | old open-source Spring/Boot maintenance and user security standard | **REJECT** |
| E: pure Jakarta/other-language rewrite with Javax unused | modern public code, old technologies absent | User's hard course preservation violated | **REJECT** |

This is a **support/namespace decision problem**, not a proof that modern Spring Boot plus true Javax is impossible in principle. Current first-party documents provide NO tested single-public-process supported full-stack preservation. **Architecture remains BLOCKED.**

## V. Acceptance gate for an eventual separately approved *minimal compatibility experiment*
- Exact effective dependency graph with Java17 or Java21 **chosen based on the actual Javax provider**; no guessed coexistence or duplicate Hibernate Validator library classes; licence/security/advisory report.
- Prove `javax.validation.Validator.validate` on preserved-style DTO with `@NotNull`, `@Min`, `@Pattern`, nested invalid fields, and correct 400; verify Jersey4 native Jakarta validation does not silently bypass any Javax requirement.
- Run real Jersey REST with Spring `@Autowired` controller→component, Spring Security 401/403, secure JSON serialization, JPA/Hibernate + MySQL transactions, Retrofit/Gson HTTP client, and scheduled jobs on supported JVM/OS.
- Reconcile all **26 original course methods**, Department, original global stock, whole-batch returns and Postman; the existing Java8 academic executable remains independently reproducible for A-FINAL.
- Prove selected JVM/DB on target ARM64 only after externally approved compute account; no automatic paid domains/providers or SMS; no production user data.
- If any actual probe fails, mark BLOCKED and recommend strictly compliant alternative or explicit user decision, never a silent Jakarta-only backend.

**None of these future executable checks was run during CP7.** The separate ChatGPT conversation owns Codex prompts and task sequencing; this chat only maintains research, independent verification and dated decision evidence.

## VI. Source/citation caveats
Official first-party claims above are externally verified; some documentation pages are rolling "latest" versions so exact artifact effective POM is future proof. Javax/Jakarta interoperability or public security is not established by citations alone. Exact current Canvas and Oracle account eligibility are inaccessible without user/authorized provider action. Research [29](29_PRE_BUILD_RESEARCH_ACCEPTANCE_GATE.md) remains authoritative: **NOT RESEARCH ACCEPTED**.
