# R09 integrated restart DTO correction

Parent c68252db41f562d0230269844794048471dac0cf; carry exact two blobs from PR55 5afd326f4bd4e03c6027a9a639de682a1996d5e1: 
- `scripts/verify-c9-recovered-api.py` `c0371dfe7f7190ffd34da3800cf7f05ab5dd4b81`
- `evidence/C9/R07_RESTART_RECEIPT_DTO_CORRECTION_2026-11-25.md` `3f8f3d70b635d2abf1001a8b9e45d00be8a68a7c`

Previous combined API blob d468cb6fd75cd27bb56e883c7aa9b43fcc10b541 matches previous PR55 source. Actual earlier full run failed KeyError after successful deadlock, response drop and new JVM readiness; source identifies temporary OperationView.action vs durable MovementReceipt.kind. Correct exact receipt and ETag oracle; pending combined CI, no merge/deploy/Claude launch. Prior integrated manifest historical.
