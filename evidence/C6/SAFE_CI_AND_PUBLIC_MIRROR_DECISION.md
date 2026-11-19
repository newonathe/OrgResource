> **IMPORTANT NEW STATE — November 19, 2026 PHT:** Current direct GitHub API says **repository visibility PUBLIC**, so “MUST stay private,” “no visibility change” and “no CI run occurred” below are **historical descriptions only**, superseded by the new reality. The C6 auditor did not change visibility. GitHub Actions [#44431604781](https://github.com/newonathe/OrgResource/actions/runs/44431604781) has **successfully executed** all three code/runtime jobs on commit 6be8165. The public source/history exposure is now an **active P0 rights/privacy/security review**, not a future choice. Recommended owner-controlled action is to return the repository to private pending clearance; this is not performed here. Details in [runtime receipt](CI_SUCCESS_RECEIPT_2026_11_19.md) and [publication gate](PUBLICATION_SAFETY_GATE.md).

# C6 safe CI choice and public repository publication boundary

**2026-11-19 PHT. OWNER APPROVAL REQUIRED.** Owner reports monthly included GitHub Actions private runner quota exhausted. Official https://docs.github.com/en/actions/concepts/billing-and-usage confirms standard GitHub-hosted runners are free and unlimited on PUBLIC repositories (still subject to concurrency and workflow limits), while larger runners and storage can have charges. The full existing OrgResource repo MUST stay private until course copyright, personally identifying files, secrets and history are vetted. No visibility change, new repo, runner install or workflow execution occurred in C6.

## Options in safe order

1. **Retain private source and use manual local test execution / an owner-approved self-hosted runner** with an isolated disposable MySQL. Self-hosted runner usage is also free under current GitHub billing, but your own machine/security/maintenance resources apply. Never execute untrusted fork PR code on personal host and follow the known in-app Chromium GPU limitation. Needs separate user setup/authorization.
2. **After explicit authorization, create a SANITIZED new PUBLIC CI mirror with CLEAN history** consisting only of approved authored source, test harness, fictional fixtures, lockfiles and a verified workflow. Include exact origin SHA and source manifest without private names. No copy of all branches, historical PRs or full git folder, and no automatic mirroring.
3. **Turning current repo PUBLIC is blocked** because original course files, private planning dossiers, academic documents, lecture audio transcripts, hidden binary metadata, archives, reachable Git history and Action logs have not been fully cleared. Publishing coursework before its printed Dec. 5, 2026 deadline also risks unauthorized reuse and academic integrity complications.

## Candidate source export policy (NOT yet executed)

ALLOW AFTER OWNER AND RIGHTS AUDIT: application Java/React source files personally owned/licensed for public release, clean Maven wrapper, MySQL Flyway definitions, explicitly fictional tests, Postman test collections and environment templates without credentials, minimal README/license/attribution, restrictive GitHub Actions YAML, deterministic test-data manifest and package lockfiles.

DENY BY DEFAULT: ChatGPT-Project-Pack, sources, private evidence and research, syllabus/course PDFs, original lecture slides/recordings/transcripts, student identification/docx, past proposals/specification ZIPs, deliverables, binaries/archives, original private Git history and PR refs, environment secrets or logs, SQL backups/dumps, browser images, personal data, node_modules, target and dist. If a required test relies on denied material, author a new fictional-equivalent test fixture with identical contract. Never delete assertions just to get public free runners.

## Mandatory review before owner chooses either public route

- Verify ownership and permission to publish academic code and class excerpts before Dec. 5. Do not assume Canvas materials can be openly redistributed.
- Run a complete history/branch/PR/secret scan and inspect binary metadata; rotate any leaked credentials before exposure. Latest-commit deletions do NOT sanitize reachable history.
- Review public fork pull request permissions, effective workflow token scopes (minimum contents:read), avoid privileged pull_request_target accepting untrusted code, validate action versions and retention.
- Verify Java8 Course A/MySQL8, Extended Java21/MySQL8, React unit/typecheck/lint/build, real Newman and Playwright actually execute on exact SHA and that all jobs have observed step logs/exit codes; previous zero-step jobs are NOT_RUN.
- Keep OrgResource's full Quality Evidence Continuous Gate and public demo non-affiliation/synthetic labeling requirements separate from mere CI eligibility.

**C6 VERDICT:** Private Actions allowance exhausted is USER-REPORTED, confirmed by owner, consistent with zero-step jobs; publication blocked by rights/privacy/history risk until reviewed. No public mirror, visibility change or CI run was performed.
