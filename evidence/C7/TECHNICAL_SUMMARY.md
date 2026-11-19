# C7 technical acceptance summary

The application under test was commit `85f6c1e0314797244784bc846a13089cfdeec271`, based on C6a `856f73baeb76cab09f880ac1a3ca64c649eb481e`. [Draft PR #22](https://github.com/newonathe/OrgResource/pull/22).

C7 tightened transactional recovery, tenant and governance authorization, and reminder-key handling. It added regression coverage for the export size boundary. The protected Course Final A backend and frontend trees matched their frozen revision. The first five Extended Flyway migrations were unchanged.

## Observed results

- Hosted CI passed all three jobs. Course A passed 61 Java 8/MySQL tests, 100 Newman requests with 151 assertions, and its packaged browser smoke test. Extended passed 51 Java 21 tests, including 50 MySQL integration tests, plus 60 Newman requests with 110 assertions and 12 packaged browser tests. The frontend verification job also passed. [Exact-source CI run](https://github.com/newonathe/OrgResource/actions/runs/44404787878).
- The Windows verification passed 51 Extended Java tests, 24 frontend tests, and both Extended Newman suites. Four browser checks failed on the first run. All 12 passed on an unchanged retry. The cause of the initial failures remains unconfirmed.
- The Windows operator verifier passed restricted-account bootstrap, backup, restore and restart checks against disposable MySQL instances. The local source archive was checked against 185 Git blobs. The packaged JAR was checked for React assets and the five embedded migrations.

The full audit, raw logs and local artifacts are retained offline. Some role and tenant matrix combinations have source review only. Hosted operation and academic sign-off remain outside this receipt. C8 implementation has not started.
