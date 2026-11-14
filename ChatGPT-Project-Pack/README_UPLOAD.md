# OrgResource ChatGPT Project Pack — Transcript Only

## What is included

The `UPLOAD` folder contains exactly ten files and no video files: eight planning/reference documents plus two ZIP archives holding every original non-video filesystem file. The source ZIP archives preserve all 1,226 original non-video files byte-for-byte with their original relative paths. The complete source manifest is included in the first archive and appended to the consolidated planning/evidence document. Both ZIPs are independently readable and under the per-file upload limit.

`06_VIDEO_TRANSCRIPTS.md` contains locally generated, timestamped audio transcripts for all 12 unique course recordings. **Do not upload the recordings or the generated playable video parts.** The playable parts are retained locally under `REFERENCE/VIDEO_PARTS_NOT_FOR_UPLOAD` only; the unchanged originals are in the course folder. The three duplicate recording paths and their hashes are recorded in the manifest.

This pack totals about 669 MB; its largest file is about 332 MB. ChatGPT's published 512 MB limit is per file, and Plus supports up to 25 files per Project. The pack deliberately uses only ten slots so the videos do not need to be uploaded or processed again. The Project will not be able to inspect video frames or screen contents from the transcripts. Use the course corpus, slides, labs, code, and source ZIPs for visual/technical corroboration; if a claim depends on a screen detail found only in a recording, ask Ethan to check that timestamp in the local original.

`PROJECT_INSTRUCTIONS.md` is meant to be pasted into the ChatGPT Project's Instructions field; it is outside `UPLOAD` and does not use a source-file slot. `01_KICKOFF_PROMPT.md` is the first prompt to send in a new chat inside the Project. `REFERENCE` contains local copies of the prior-work document, prior research/evidence document, standalone manifest, and generated playable parts; do not upload the playable parts.

## Set up the Project

1. In ChatGPT, select **New project** and name it **OrgResource Project Planning**.
2. Open the Project instructions and paste the full contents of `PROJECT_INSTRUCTIONS.md`.
3. Upload the ten files from `UPLOAD` in numeric order. The two ZIP archives preserve original non-video files; the text documents are the searchable reading layer.
4. Start a new chat in the Project and paste the full contents of `01_KICKOFF_PROMPT.md`.

## Upload order

1. `01_KICKOFF_PROMPT.md`
2. `02_MASTER_CONTEXT_DOSSIER.md`
3. `03_COURSE_REFERENCE_CORPUS.txt`
4. `04_ORGRESOURCE_PRIOR_WORK_RESEARCH_AND_MANIFEST.md`
5. `06_VIDEO_TRANSCRIPTS.md`
6. `08_CSCI180_SYLLABUS.pdf`
7. `09_ESP_PROJECT_ASSIGNMENT.pdf`
8. `10_ORGRESOURCE_CONCISE_PROPOSAL.docx`
9. `11_COMPLETE_SOURCE_NONVIDEO_PART1.zip`
10. `12_COMPLETE_SOURCE_NONVIDEO_PART2.zip`

## How to use the transcript and source archives

The transcripts cover audio only. Speech recognition may mishear code, identifiers, and names, and the web Project cannot verify screen contents without the actual recordings. The transcript marks seconds from each original recording and maps oversized recordings to local playable parts. Use transcript timestamps to locate a topic, then rely on uploaded slides, labs, code, and the course corpus for confirmation. If those sources do not settle a visual detail, ask Ethan to inspect the local recording at the cited time. Never claim the raw videos were uploaded or reviewed.

The complete-source ZIPs contain all original non-video files, including nested ZIP files, source code, JARs, PDFs, slides, images, and previous OrgResource materials. Entries retain the original folder path under `source/`. Consult an archive when the reading corpus does not expose an exact original file. Do not treat an inventory row or filename as proof that file contents were reviewed.

All original filesystem paths and all files inside source ZIP archives are inventoried with sizes and SHA-256 hashes. Exact duplicate files are identified in `duplicate_of`. The source folder contains old drafts and historical research; the master context identifies their authority limits and conflicts. The planning chat should reconcile them rather than treat them as settled facts. The unchanged course folder remains the authoritative full-fidelity source collection.

## Current platform references

Official OpenAI Help Center pages checked 11 November 2026:

- [Uploading files and audio to ChatGPT](https://help.openai.com/en/articles/8555545-uploading-files-and-audio-to-chatgpt)
- [Projects in ChatGPT](https://help.openai.com/en/articles/10169521-projects-in-chatgpt)
- [ChatGPT Image Inputs FAQ, including video attachments](https://help.openai.com/en/articles/8400551-chatgpt-image-inputs-faq)
