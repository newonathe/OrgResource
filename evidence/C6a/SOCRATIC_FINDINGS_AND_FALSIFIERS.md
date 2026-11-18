# C6a findings and falsifiers

1. **Could an artifact be treated as ready before validation?** C5 created the final directory before tests. C6a stages beside it and moves only after validation. A deliberate verifier failure must leave the final path and readiness marker absent.
2. **Could a restricted backup succeed with partial visibility?** Require the named protected login and the 18-table inventory. Try a SELECT grant on only one table and a missing login path. Neither should create an archive.
3. **Could metadata visibility hide a nonempty target?** The protected restore identity needs schema-wide SELECT. It must authenticate the archive first, reject a populated target and identify partial DDL after import failure as unusable.
4. **Can a cursor grant access?** The cursor must resolve a row in the selected organization and allowed feed after OWNER/ADMIN checks. Try a foreign cursor and tied times past a full page. A newer inserted fact should be visible from the newest page.
5. **Could the activity response silently omit a source?** Sources are visibly labeled and both selects run in one snapshot. A source query failure must fail the whole response. Audit records cover recorded events only.
6. **Can a Resource name alter CSV structure or execute as a formula?** Text fields are quoted, internal quotes doubled and risky cell prefixes neutralized. Check exact bytes with fictional fixture text and preserve JSON text.
7. **Could a workspace switch show stale results or download stale bytes?** Organization-keyed remount and existing transport aborts are necessary. Inspect the actual browser from the packaged app. Role checks are server requirements.
8. **Does suspension or session expiry immediately bar a subsequent request?** Exercise ordinary requests with persisted sessions and role fixtures. Require same-origin and CSRF for the export POST.
9. **Does CI prove first-run permission setup and recovery?** Separate Java 8 CI, Java 21 CI, clean extraction, packaged browser, and restricted operator receipts. Absence of an independent environment result is evidence of an unrun check.
10. **Does a second MySQL process prove off-host recovery?** It proves a separate process and storage directory on this workstation. Off-host storage, production HTTPS and live delivery remain separate gates.

11. **Does a role error disclose a workspace other than the selected one?** Check a dual member who is OWNER in the selected workspace and MEMBER in the requested workspace. The report routes must return the same 404 as any unselected workspace before evaluating the requested role.

12. **Does the refused bootstrap run online maintenance?** Age permanent-operation HTTP receipts only in the disposable operator database. Ordinary scheduling is enabled during servlet startup. The refused offline command must leave both the original organization and expired receipt rows intact.

13. **Can row, body or file limits fail open?** The API reads at most 1,025 request bytes and inspects the 1,001st output row before encoding. Exercise row refusal and strict types. The fixed widths and reduced DTO field set keep current fixture data below the separate 4 MiB cap.

14. **Does a protected restore roll back the whole import?** MySQL DDL can commit before a later statement fails. A partially affected disposable target is unusable, even if some rows or tables appear valid. The runbook uses a separately authorized new empty schema for the next attempt.

15. **Are visibly successful app writes enough to trust inventory?** Use the actual MySQL transaction, reconciliation and replay tests. The lost-response browser scenario issues one inventory command, then checks permanent receipt lookup, safe replay and one corresponding movement.

16. **Did the frozen Course A acceptance runner actually traverse runtime endpoints?** The 28-entry route reference includes mutually exclusive operations. Use independently generated fixtures in the existing Newman acceptance series, compare every route and preserve the reference.

17. **Is the no-advisory result durable?** Two point-in-time npm audits returned zero. Earlier Picomatch 4.0.7 advisory status changed externally, while the selected package stayed identical. Dependency compatibility and a public release decision remain separate.

18. **Does visual verification establish complete accessibility?** Browser checks cover routing, viewport widths, keyboard focus, reduced motion and real downloads. Manual screenshot review samples light and dark modes. Assistive technology and physical device checks remain unrun.

19. **Does encoding an event ID conceal a global sequence?** An encoded source/row pair contains no actor or target contact identifier. It may expose coarse activity growth through gaps. Hardening against that inference requires a different stable event reference contract and separate approval.

20. **Can this phase infer student, provider or owner approval?** No runtime suite can establish a Canvas action, delivery receipt, member identity linkage or permission to host publicly. The release remains local, and the unresolved academic and product decisions stay with their owners.

This is an internal source critique. Independent test environments and tool observations are listed in the acceptance evidence. An independent human review has not been recorded.
