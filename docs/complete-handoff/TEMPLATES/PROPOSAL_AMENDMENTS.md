# Revised-proposal amendment log (not evidence of submission or approval)

Baseline: 10_ORGRESOURCE_CONCISE_PROPOSAL.docx, 12 pages, 5 entities, 25 unique HTTP routes.

1. Keep five entity names and 25 original method/path pairs unchanged.
2. Resource creation does NOT add mandatory unit.
3. Resource availability response remains exactly resourceId + availableQuantity.
4. Resource update adds expectedTotalQuantity and stale-state 409 behavior; align component method and Postman.
5. Resource delete stricter: require totalQuantity=0 in addition to no Requirements.
6. Department/Resource use normalized nameKey unique DB index.
7. ResourceRequirement update lower bound includes largest live PENDING Allocation in addition to current effective coverage.
8. Allocation persistence adds lifecycle timestamps, approvedAt history, cancellation reason, durable SMS claim/outcome and provider response metadata.
9. Allocation output redacts phone; adds contactPresent, expiredPendingCleanup, reminderOutcome; reminderAttempted derived.
10. Allocation delete protects any historical approvedAt, even after cancellation.
11. Approval holds expire at Project end (behavioral change to original proposal's indefinitely reserved stock), expiry cleanup both PENDING and APPROVED.
12. Reminder stays in upcoming 24-hour window only, no overdue extension.
13. SMS modes disabled/fake/conditional virtual; no delivery claim; no actual provider token in simulator.
14. Add proposed A09 consent-withdrawal route only if retained; original 25 remain.
15. Public errors keep original message property and add stable code.
16. Maven build, VS Code workspace and React UI are user implementation decisions; still export Eclipse-compatible source.
17. Optional image upload/download only if final course interpretation requires; update routes to 28 if added.
18. Explain factual evidence limits: fictional use case, no interviews/adoption, user-approved outside assistance acknowledgment.
19. Update controller method descriptions, request/response JSON, errors, components, entity fields/repositories, Postman to one coherent contract. Do not claim proposal submitted/approved unless actual receipt exists.
