---
name: orgresource-superpowers-implement
description: Execute an approved OrgResource implementation in coherent batches with isolated changes, behavior-level tests, and precise resume evidence.
---

# Superpowers implementation for OrgResource

1. Check Git status and the exact base. Use the existing correct worktree. Create isolation when it protects concurrent work or the frozen academic source. Preserve all unrelated changes.
2. Reuse the existing architecture and tests. For new or changed behavior requiring a regression, write or select a focused assertion. Observe the intended failure before changing implementation when practical. A setup or compilation failure does not establish the behavior reproduced.
3. Make the smallest coherent production change, rerun the focused case and refactor only within the authorized slice. Verify persisted side effects, denials, retry behavior or concurrency when relevant.
4. Work inline by default. If delegation is authorized and adds value, assign an independent slice with its base, exact model and effort, writable paths, shared contract and acceptance check. Keep one writer for overlapping code and use an independent context for review when the gate requires it.
5. Retain the source, completed checks and next incomplete gate. Finish with the review and verification skills. Commit, push or clean up only within the user's authority.

Adapted from upstream [executing-plans](https://github.com/obra/superpowers/tree/22f0b463e7a5e579904dd6211a520321035526f7/skills/executing-plans), [test-driven-development](https://github.com/obra/superpowers/tree/22f0b463e7a5e579904dd6211a520321035526f7/skills/test-driven-development), and [using-git-worktrees](https://github.com/obra/superpowers/tree/22f0b463e7a5e579904dd6211a520321035526f7/skills/using-git-worktrees).
