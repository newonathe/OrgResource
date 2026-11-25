---
name: orgresource-superpowers-debug
description: Diagnose an OrgResource application failure, flaky acceptance test, or GitHub CI regression from evidence before changing the implementation.
---

# Superpowers debugging for OrgResource

1. Record the failed command, exact revision, fixture, environment and expected versus actual result. Preserve useful failure evidence with credentials redacted.
2. Follow the real path from UI or HTTP through authorization, Java services and database state or provider simulation. Compare the failing path with a working case. Distinguish a fixture, timing or infrastructure failure from a product defect.
3. Test one causal hypothesis at a time. A transient CI download failure can justify a targeted retry. For a reproduced code defect, keep the regression and fix the boundary that produced it.
4. Recheck the original command or equivalent real behavior under its original assertions. For an uncertain write, inspect its durable receipt and persisted state before attempting any replay.

Adapted from upstream [systematic-debugging](https://github.com/obra/superpowers/tree/22f0b463e7a5e579904dd6211a520321035526f7/skills/systematic-debugging).
