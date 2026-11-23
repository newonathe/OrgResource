---
name: orgresource-superpowers-review
description: Review an integrated OrgResource change against its contract and evidence, or reconcile concrete review findings before acceptance.
---

# Superpowers review for OrgResource

1. Supply the approved contract, exact base and head, complete cumulative diff, critical callers and test receipts. Avoid an isolated last-commit diff when earlier commits affect correctness.
2. Ask the reviewer to challenge actual tenant boundaries, denied-write side effects, archive and migration safety, stock conservation, replay, failure handling and scope as applicable. Record exact locations, observed facts, source-only findings and missing checks.
3. Verify findings independently against source and observed behavior. Fix confirmed issues in bounded batches, rerun the affected tests and recheck the final SHA.
4. A self-review and a CI rerun have value on their own. If a separate reviewer is required and unavailable, leave that gate open.

Adapted from upstream [requesting-code-review](https://github.com/obra/superpowers/tree/22f0b463e7a5e579904dd6211a520321035526f7/skills/requesting-code-review), [receiving-code-review](https://github.com/obra/superpowers/tree/22f0b463e7a5e579904dd6211a520321035526f7/skills/receiving-code-review), and [subagent-driven-development](https://github.com/obra/superpowers/tree/22f0b463e7a5e579904dd6211a520321035526f7/skills/subagent-driven-development).
