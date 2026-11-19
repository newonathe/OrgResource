---
name: orgresource-goat
description: Choose an executor, model, reasoning effort, and verification depth for each coherent OrgResource task. Use at task intake and when evidence changes risk.
---

# GOAT routing for OrgResource

The supplied GOAT video illustrates task-specific selection of both model and effort, with verification scaled to risk. Its simulated cost comparison provides no measured OrgResource baseline. The routes below apply that idea to this project's work.

1. Identify what the user authorized, the exact source, uncertainty, consequences of failure, and the proof needed. Apply [the repository rules](../../../AGENTS.md) and [the quality gate](../../../docs/QUALITY_EVIDENCE_CONTINUOUS_GATE.md).
2. Use Git, file tools or a deterministic script for refs, hashes, counts and exact comparisons. Use the current session for coherent work by default. Select a different model only when the available runtime permits it and the task benefits.
3. Choose the family and the lowest supported effort that can satisfy the full acceptance criteria. For work with material consequences, start at the appropriate high-risk route.

| Family | Starting effort | Use |
| --- | --- | --- |
| Luna | Low for bounded routine work, medium for source comparison | Extract exact requirements after tooling has inventoried the sources, reconcile simple documentation, or make a narrow edit with a clear check. |
| Terra | Medium | Implement a defined React or API slice, inspect screenshots, check accessibility, or repair a bounded build or operator workflow. |
| Sol | Medium for cross-file development or independent review, high for high-risk code | Trace Java/API/SQL integration, diagnose regressions and verify authorization, stock, migrations, replay or recovery against their real boundaries. |
| Astra | High for complex design or a critical decision, xhigh when the boundary warrants it | Resolve conflicting authorities, interacting transaction or architecture constraints, and unresolved high-impact release or review findings. |

4. Escalate a bounded failing step after inspecting its actual failure. Raise the effort within the same family if the approach is sound. Switch families if the task needs a different capability. Prefer Astra high or xhigh for hard cross-cutting decisions. Use max or ultra only when the runtime exposes them, the stakes justify them, and a documented evidence gap remains.
5. Verify in proportion to risk. For routine work, inspect the diff and run the direct check. For changed behavior, run focused positive and negative checks and relevant regression. For authorization, data, academic integrity or release, run the required real environment gates and independent review where prescribed. Model choice does not reduce the evidence required.

Resolve family names to the exact selectable model ID and effort at dispatch time. If the desired pair is unavailable, choose a supported peer or continue in the current session and record the actual route. For delegated or high-risk work, record the selected pair, scope, verification and why any escalation was necessary. Compare routes by time to a verified result, including retries and review. Report token use or cost only when measured. Give a separate reviewer the exact integrated diff and evidence. User, permission and runtime limits control whether delegation is available.
