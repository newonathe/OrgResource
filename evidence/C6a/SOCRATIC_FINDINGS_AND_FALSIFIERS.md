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

This is an internal source critique. Independent test environments and tool observations are listed in the acceptance evidence. An independent human review has not been recorded.
