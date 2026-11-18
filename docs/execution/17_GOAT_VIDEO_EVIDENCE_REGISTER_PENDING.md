# Daniyar / GOAT video — honest evidence register and pending frame/audio inspection

**2026-11-19 PHT, status: DIRECT VIDEO NOT_INSPECTED / LOCAL_TOOL_BLOCKED.** This file is an audit obligation, NOT a claim the uploaded video was watched. It is associated with the user's explicitly uploaded asset at exact provided sandbox path:
`/mnt/data/Daniyar_AI_Engineer_Big_Tech_video_no_watermark (1).mp4`

## A. Tools attempted and actual observation

- Container/CaaS `ls`, `ffprobe`, and even a minimal `echo` process: tool returned `ClientError` / execution infrastructure failure.
- Python local kernel attempts to check `os.path.exists` and call `ffprobe`: returned `ClientError` before any output; a trivial `print(1+1)` also failed.
- Python-user-visible attempted `os.path.exists`, `os.path.getsize`, `ffprobe`: also returned `ClientError`.
- Project Files search for the video filename: empty; it is not an alternative decoded-frame source.
- **Therefore file existence, actual MP4 codec/duration/number of frames/scene count/audio content and any specific timestamp were not independently established in this pass.** Developer upload metadata supplies the exact sandbox path and identifies a video upload, but that alone is not visual inspection.
- No screenshot image or original frame was shown to the model, no OCR, transcript or ASR was run. Do not invent visual descriptions or frame counts.

## B. Public source that may be related, independent from video bytes

Retrieved public caption https://imginn.com/p/DeFS0Fes8yu/ attributed to Daniyar Kazi / @donimas, dated 2026-10-04. Describes a fictional six-task comparison of agent model/effort routing, “JUNIOR”, “MIDDLE”, “SENIOR”, “GOAT”. Claims US$5.10, $0.75, $0.45, $0.32 under its own **“Simulated workload, real prices. Not a benchmark.”** qualifier. This is **PUBLIC CAPTION EVIDENCE**, not verified identity of user's actual uploaded MP4, nor independently replicated experiment. Caption words cannot establish which slides/tables/images were actually visible in the uploaded video.

## C. Required future source-level frame/audio verification (only when local video runtime works)

1. **Source integrity:** confirm exact original MP4 path, nonzero bytes, file SHA256, ffprobe duration, codec, frame rate (constant vs variable), stream count, audio codec, size, orientation/time base; record command exit and metadata. Keep all processing local; never upload an unknown source to a third-party service without user consent.
2. **Frame completeness:** compute exact presentation timestamps for ALL decoded frames with `ffprobe -show_frames` or FFmpeg reader. For variable frame rates use presentation timestamps, not `index / nominal_fps`. Record expected vs actual decoded frame count and errors.
3. **Visual pass:** decode video to a temporary folder or stream, compare every consecutive frame for scene/overlay/slide changes (perceptual hash/scene cut), extract **all visually distinct states** and representative transition/interval frames. Manually inspect full-resolution frames in chronological order, zoom in on GOAT price/model/effort captions, charts, footnotes and on-screen source references. A computational sweep of all frames is not proof the model visually understood every individual frame; report exact frames visually inspected and sampling policy.
4. **Text/time alignment:** annotate visible on-screen text manually from the full-resolution frames and timestamp; use native vision preferentially to OCR. Do not infer unreadable or cropped words. Extract the entire audio locally for transcription if an ASR model is available, preserve speaker wording and timestamp, and check disagreements with on-screen text.
5. **GOAT falsification:** identify actual claimed routing inputs, whether an LLM call or deterministic rules perform routing, whether effort is chosen per task, fallback/escalation rules, how token usage/caching/retries were counted, model names and actual time/pricing. Confirm sample size, success oracle, output quality, counterfactual baseline, independence of tasks, confidence interval and total user-plan cost if the video discusses them.
6. **Independent source verification:** compare each factual claim against model and Codex primary documentation and actual supported 2026 model availability; reproduce cost only when exact prompts/usage/task results are public. Test in OrgResource only through a pre-registered authorized small evaluation, never an uncontrolled costly swarm.
7. **Output:** `evidence/research/GOAT_VIDEO_FRAME_REGISTER.csv` with columns asset_sha256, frame_index, pts_seconds, visually_reviewed_yes_no, visual_text, scene/overlay changes, audio_timestamp, claim_id, source_confidence and contradiction; `GOAT_VIDEO_CLAIM_EVIDENCE.md` with timestamp/independent source link and verified vs not verified. No frame claim without frame identity and screenshot/evidence. Use local temp outputs excluded from Git.

## D. Frugal local example commands (future reproducible instructions; NOT EXECUTED)

```bash
# Use the exact uploaded path on the actual workstation or file mount.
ffprobe -v error -show_entries format=duration,size:stream=index,codec_name,codec_type,width,height,r_frame_rate,avg_frame_rate -of json "$VIDEO"
# Inspect presentation timestamps and frame list before selecting representative scenes:
ffprobe -v error -select_streams v:0 -show_frames -show_entries frame=best_effort_timestamp_time,pict_type,key_frame -of csv "$VIDEO"
# Extract separate audio only when present and needed:
ffmpeg -nostdin -i "$VIDEO" -vn -ac 1 -ar 16000 "$WORKDIR/audio.wav"
```

No large extraction should be committed into OrgResource or used as pretense of completed analysis; workdir must be a safe temporary location, available disk checked, and original MP4 preserved unchanged.

## E. Decision while video is inaccessible

The GOAT concept is being treated as an **externally sourced, unproven optimization hypothesis**; the cost figures are **NOT_BENCHMARK**. The source-grounded Superpowers design merits a **policy-only** OrgResource adaptation now, which can be accepted/rejected independently of proving the video frames. All video-specific visual assertions are explicitly **OPEN / NOT_RUN** until source inspection becomes possible. No false frame-by-frame statement is allowed in PR, phase report or user reply.
