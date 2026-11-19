> **LATEST STATUS CORRECTION — 2026-11-19 PHT:** A later direct GitHub repository API read returned **private=false, visibility=public**, and [run #44431604781](https://github.com/newonathe/OrgResource/actions/runs/44431604781) subsequently executed all three CI jobs successfully. All statements below saying “private=true” or anticipating “before changing visibility” describe the earlier snapshot and are now **STALE**. This audit did **not** change repository visibility. Publication is an **ACTIVE P0 privacy/copyright/history exposure under owner review**, regardless of successful CI; review GitHub Settings to restrict access pending verified rights/privacy/secret/history clearance. Details: [CI receipt](CI_SUCCESS_RECEIPT_2026_11_19.md). No student identifiers or actual sensitive strings are repeated in this correction.

# C6 PUBLICATION_SAFETY_GATE — publication BLOCKED pending private-data and rights audit

Date: 2026-11-19 PHT. Stage: read-only C6 source review. Status: PUBLICATION_RISK_P0_BLOCKED, NOT a completed secret scan. No visibility change made.

## GitHub Actions computation and the user's plan

USER-REPORTED: Private repository GitHub Actions quota exhausted; recent runs ended before any test steps. VERIFIED EXTERNAL FACT from current official GitHub docs: standard GitHub-hosted runners are free and unlimited on public repos; larger runner classes and artifact/cache storage retain separate prices/limits:
https://docs.github.com/en/actions/concepts/billing-and-usage
https://docs.github.com/en/actions/reference/runners/github-hosted-runners
https://docs.github.com/en/billing/concepts/product-billing/github-actions

CURRENT VERIFIED GITHUB FACT: GitHub API reported newonathe/OrgResource private=true, visibility=private, default_branch=main, size=70,621 KiB. No change of visibility occurred here. Public repository visibility exposes code, reachable history, branch/PR refs, forks, and Actions logs. Changing files in the latest commit alone cannot purge historical content. Source: https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/managing-repository-settings/setting-repository-visibility

## Directly observed disclosure concerns

| Source / exact Git blob | Evidence | Decision |
| --- | --- | --- |
| ChatGPT-Project-Pack/UPLOAD/02_MASTER_CONTEXT_DOSSIER.md, f012176ec27d057402f80b8757057b516b6183e0 | Pattern-class scan found student name/identifier-like text; actual values withheld here | P0: keep private until owner approves necessity, redaction, history treatment |
| ChatGPT-Project-Pack/UPLOAD/06_VIDEO_TRANSCRIPTS.md, 2af61e5c6dc68e523631eec790fa297db9c43c1d | Approximately 470 KB of recorded course audio transcripts; distribution rights not checked, frames unseen | P0: keep private until lecture permission |
| ChatGPT-Project-Pack/UPLOAD/03_COURSE_REFERENCE_CORPUS.txt and original class PDFs in UPLOAD/ and sources/ | Full teaching references, instructor slides/examples/syllabus/assignment may not be licensed for public republication | P0: verify copyright and permission |
| Root DOCX/PDF/ZIP, OrgResource-Specification-Package.zip, deliverables and historical artifacts | Some binary/metadata content uninspected; student identification and course exports possible | P0: full bytewise archive+history review |
| Postman local environment JSON and backend/extended application.properties | Selected file scan found credentials-looking field labels or placeholders; no live secret independently proved, no full history audit | P0: full secret history scan, not prematurely cleared |
| .github/workflows/verify.yml | Disposable CI credentials observed in course/Extended test runner YAML; Actions history/logs still require review | P1: public fork/PR security and workflow token permissions |
| data/reference and synthetic demo plans | Public source names are factual references, simulated activity is not real Ateneo adoption | P1: source attribution, no-endorsement and trademark/privacy review |

Observed scan scope: exact 699-file Git tree metadata; selected specific text sources scanned by pattern categories (email, student ID, credential labels, phone, real name), not every byte of every file. Binary, history and all branches/PRs NOT_FULLY_SCANNED. A pattern hit is not a verified credential; absence of a hit does not certify no secret.

## Actions required BEFORE any public visibility change

1. P0 privacy: inspect/remove private student IDs, confidential academic material, contact information, private correspondence and hidden DOCX/PDF metadata from the proposed PUBLIC content and all reachable history.
2. P0 copyright: explicitly verify sharing permission for class videos/transcripts, slides, examples, assignment/syllabus, and bundled third-party code. Canvas visibility does not confer open-source redistribution rights.
3. P0 credentials: full Git history and all refs, binary archives, Postman variables, workflow logs/artifacts and orphaned credentials scan; rotate any exposed real keys BEFORE publishing. If history rewrite is necessary, require owner authority and account for forks/cached copies; no automatic force-push.
4. P0 repository separation: PREFERRED safe route is a NEW SANITIZED PUBLIC CI MIRROR containing only explicitly approved original code, tests and fictional fixtures; retain private OrgResource project with academic source corpus, student dossier and protected evidence. Creating the mirror requires separate user permission.
5. P1 CI: verify workflow uses ordinary standard ubuntu-latest runners (observed), public fork PR permissions, token scopes, secrets exposure, safety of pull_request_target and artifact retention. GitHub warns that changing private repo to public can disable push rulesets.
6. P1 after authorization: run ACTUAL public CI at final SHA and verify all Java8/MySQL, Java21/MySQL, Newman, React and browser test steps started and finished. Avoid claiming code checks passed when runner never started.

Alternative: stay private and use local disposable tests or next quota window. User can select public mirror vs full-public repo AFTER review. This audit does not authorize GitHub visibility change, deleting files, rewriting history, paid services, real SMS, public hosting or academic submission.

Verdict: PUBLICATION BLOCKED for the existing repo, pending owner-facing risk and source permission audit. This is not a claim that the code cannot safely be open-sourced via a sanitized path.
