---
name: orgresource-superpowers-verify
description: Verify an OrgResource fix, push, artifact, or acceptance claim with current command results, exact source identity, and the required real runtime.
---

# Superpowers verification for OrgResource

1. Match each claim to a direct check before reporting it. Identify the exact source, worktree, command and environment. Read the exit status and relevant results, including failures and skips.
2. Use the required Java and isolated MySQL editions for runtime proof. Use command-line Playwright on Windows. When accepting a package or push, verify the corresponding artifact digest or remote SHA and the checks that actually ran there.
3. Distinguish focused tests, full regression, independent review and formal acceptance. Record historical or unrun evidence with explicit status. Preserve failure receipts and show what changed before a successful rerun.
4. Report the verified outcome, material remaining gates and separate owner actions. Keep the current phase and publication boundary.

Adapted from upstream [verification-before-completion](https://github.com/obra/superpowers/tree/22f0b463e7a5e579904dd6211a520321035526f7/skills/verification-before-completion) and [finishing-a-development-branch](https://github.com/obra/superpowers/tree/22f0b463e7a5e579904dd6211a520321035526f7/skills/finishing-a-development-branch).
