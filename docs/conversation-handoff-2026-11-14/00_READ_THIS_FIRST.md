# OrgResource: new-conversation continuity pack (2026-11-14, Asia/Manila)

## Purpose and how to use
This repository directory preserves the substantive project decisions, engineering instructions, evidence, scope, current build-in-progress, user requirements, and continuation procedure from the ChatGPT conversation ending with the request to transfer everything to a new conversation in the same OrgResource ChatGPT Project.

**This is a continuity overlay, not a substitute for the current repository, its source code, the primary course assignment/syllabus, or the original proposals and course files.** The complete prior 188,934-byte implementation planning packet already exists at \`docs/ORGRESOURCE_MASTER_HANDOFF.md\` and a byte-equivalent copy at \`docs/complete-handoff/ORGRESOURCE_COMPLETE_MASTER_HANDOFF.md\`. This overlay refers to that full packet instead of copying the same material redundantly. The new conversation must read the source-of-truth files and re-audit the current branch/commits, especially since Codex O3 implementation was explicitly **still running** when this continuity pack was requested.

Suggested reading order for the new conversation:
1. \`00_READ_THIS_FIRST.md\` — authority and use.
2. \`01_CONVERSATION_DECISIONS_AND_HISTORY.md\` — exact sequence, user priorities, delivery context, and explicit authorizations.
3. \`02_GITHUB_SNAPSHOT_AND_EVIDENCE.md\` — independently checked remote snapshot and known code/evidence limits.
4. \`03_COURSE_DOMAIN_API_CONTRACT.md\` — course authority, five-entity design, all APIs, domain calculations, error handling, and academic integrity.
5. \`04_PHASES_TESTS_RELEASE.md\` — roadmap, acceptance, SMS/scheduling, UI, DevOps and final deliverables.
6. \`05_O3_ACTIVE_BUILD_COMPLETION_GATE.md\` — current Codex job, exact acceptance conditions, what to inspect immediately after completion.
7. \`06_TOKEN_PERFORMANCE_PUSH_POLICY.md\` — standing instructions that take priority for planning the NEXT Codex prompt.
8. \`07_NEW_CHAT_COPY_READY_PROMPT.md\` — first message to copy into a new conversation.
9. \`08_SOURCE_DIRECTORY_EVIDENCE_REGISTER.md\` — paths, provenance, historical material and availability.
10. \`09_CONTINUATION_CHECKLIST_AND_DECISION_LOG.md\` — audit template and open questions.

## Snapshot, not timeless status
At the last successful connected GitHub audit during the handoff request:
- Private repository: \`newonathe/OrgResource\`.
- Remote \`main\`: \`16e781c837dd7e1f0481fb7a01e49c27d80a91e8\`; last committed 2026-11-14 13:56:55 UTC.
- On \`main\`, 13/25 original proposal routes implemented, plus proposed A09 NOT implemented.
- One older draft PR #1 from \`demo/o2-recording-kit-2026-11-14\` exists and should not be blindly merged; no GitHub Actions runs.
- Codex was executing the O3 completion prompt at the user's report; **do not presume its success, push, tests, or route count before auditing remote and local evidence**.
- User explicitly reported that they had **finished recording the O2 skeleton video**. Actual video file content, uploaded Canvas receipt, sharing access and submission status were NOT independently verified by this chat. The earlier checked-in \`evidence/O1/VIDEO_NOT_CREATED.md\` is older than this user report and is a **stale report**, not proof that a recording still does not exist.

## Authority hierarchy
COURSE REQUIREMENT: actual current \`09_ESP_PROJECT_ASSIGNMENT.pdf\`; SYLLABUS FACT: \`08_CSCI180_SYLLABUS.pdf\`; current Canvas updates may supersede printed dates but have not been verified. USER DECISION: the user's live instructions in the conversation. The 12-page five-entity \`10_ORGRESOURCE_CONCISE_PROPOSAL.docx\` defines original 25 operations. The 26th consent-withdrawal route is a reviewed amendment. The earlier nine-entity/68-operation package is historical research, not the selected current scope.

Project: one fictional student organization's resource planning system, NOT a verified official Ateneo app, not a deployed/piloted campus system, not evidence of measured demand, adoption, or integration. Preserve honest academic assistance disclosures. Source corpus may include extracted audio transcripts but **no raw video screen content was analyzed**.

## Very important role distinction
This ChatGPT Project = research, review, continuity, audit, and Codex prompt authoring. Codex = actual implementation. Do not claim ChatGPT implemented Java or ran the user's local MySQL unless a tool actually did so. Do not send Codex another full project plan when a scoped, actionable larger-cycle prompt is needed.

## New conversation behavior
Immediately after this pack is supplied, perform a new read-only connected GitHub audit: current main and relevant feature heads, open PRs, recent commits, changed files, route parity, evidence and tests. Don't reuse the fixed snapshot as current truth. Then provide a precise completion review of the ongoing O3 build and, when warranted, the **combined O4+O5 large coherent coding prompt** using the rules in file 06. If O3 is still not complete, offer a targeted repair/finish prompt and preserve work. Never automatically merge to main or submit to Canvas.

## What “complete/lossless” means and does NOT mean
The pack attempts comprehensive transfer of all MATERIAL instructions, design decisions, history, conflicts and continuity state visible in the conversation. It does **not** claim to be a byte-for-byte transcript of every private thought, an exhaustive reread of every original ZIP entry, a substitute for all original course files, a current Git checkout, a runtime test, or a video. For source-level losslessness, the original committed source and docs MUST remain canonical and be reopened at the precise version currently on GitHub. No secrets or password data are replicated into this handoff.