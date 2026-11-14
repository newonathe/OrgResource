from __future__ import annotations

import re
import sys
from pathlib import Path

from faster_whisper import BatchedInferencePipeline, WhisperModel

PACK = Path(__file__).resolve().parent
ROOT = PACK.parents[1]
UPLOAD = PACK / "UPLOAD"
TEMP_TRANSCRIPT = Path(sys.argv[1]) if len(sys.argv) > 1 else None

VIDEOS = [
    ("V01", "1 GMT20210831-060130_Recording_1920x1080 - spring boot annotations.mp4"),
    ("V02", "2 GMT20210902-060219_Recording_1920x1080 -- Jersey.mp4"),
    ("V03", "2025-09-23 18-19-19-- validation.mov"),
    ("V04", "2025-09-23 18-51-05- json.mov"),
    ("V05", "2025-09-23 19-19-38-many to one.mov"),
    ("V06", "3 GMT20210921-060535_Recording_1920x1080 -- JPA Hibernate MySQL.mp4"),
    ("V07", "4 GMT20210923-060538_Recording_1920x1080 -- JPA Hibernate MySQL 2.mp4"),
    ("V08", "6 GMT20210907-060522_Recording_1920x1080 - Retrofit 1.mp4"),
    ("V09", "8 GMT20210916-060126_Recording_1920x1080 -- JSON.mp4"),
    ("V10", "9 GMT20211007-060438_Recording_1920x1080 -- Image Server.mp4"),
    ("V11", "changing mysql port.mkv"),
    ("V12", "Eclipse starter primer.mkv"),
]


def cached_transcript(path: Path) -> str | None:
    if not TEMP_TRANSCRIPT or not path.name.startswith("1 GMT"):
        return None
    if not TEMP_TRANSCRIPT.exists():
        return None
    text = TEMP_TRANSCRIPT.read_text(encoding="utf-8", errors="replace")
    matches = list(re.finditer(r"\[(\d+\.\d+)-(\d+\.\d+)\]", text))
    duration_match = re.search(r"audio seconds: ([\d.]+)", text[:500])
    if matches and duration_match:
        last_end = float(matches[-1].group(2))
        duration = float(duration_match.group(1))
        if last_end >= duration - 180:
            return text
    return None


def main() -> None:
    output = UPLOAD / "06_VIDEO_TRANSCRIPTS.md"
    large_model = None
    small_pipeline = None
    with output.open("w", encoding="utf-8", newline="\n") as out:
        out.write("# Timestamped Course Video Transcripts\n\n")
        out.write("Speech recognition ran locally: V01 used faster-whisper large-v3-turbo (CPU int8); V02-V12 used the small multilingual model with English forced (CPU int8, batch size 8). The text is machine-generated, minimally edited, and may mishear technical identifiers or names. Each recording's detected language, probability, and model are reported below. Timestamps are seconds from each original recording. Consult the corresponding uploaded playable video part for visual context and verify important details against primary slides, labs, and code.\n")
        out.write("\n## Video and upload-part map\n\n")
        out.write("| ID | Original recording | Project upload file(s) | Notes |\n|---|---|---|---|\n")
        out.write("| V01 | Spring Boot annotations recording | Videos/V01_spring_boot_annotations.mp4 | Full source |\n")
        out.write("| V02 | Jersey recording | Videos/V02_Jersey_REST.mp4 | Full source |\n")
        out.write("| V03 | Validation MOV | Videos/V03_validation_part00.mov (0.000-1,000.233s); part01.mov (1,000.233-1,897.200s) | Full source, stream copied |\n")
        out.write("| V04 | JSON MOV | Videos/V04_JSON_Lab3_session.mov | Full source |\n")
        out.write("| V05 | Many-to-one MOV | Videos/V05_many_to_one_part00.mov (0.000-907.700s); part01.mov (907.700-1,801.233s); part02.mov (1,801.233-2,674.933s) | Full source, stream copied |\n")
        out.write("| V06 | JPA/Hibernate/MySQL recording 1 | Videos/V06_JPA_Hibernate_MySQL_1.mp4 | Full source |\n")
        out.write("| V07 | JPA/Hibernate/MySQL recording 2 | Videos/V07_JPA_Hibernate_MySQL_2.mp4 | Full source |\n")
        out.write("| V08 | Retrofit 1 recording | Videos/V08_Retrofit_1.mp4 | Full source |\n")
        out.write("| V09 | JSON recording | Videos/V09_JSON.mp4 | Full source |\n")
        out.write("| V10 | Image Server recording | Videos/V10_Image_Server.mp4 | Full source |\n")
        out.write("| V11 | Changing MySQL port recording | Videos/V11_changing_mysql_port.mkv | Full source |\n")
        out.write("| V12 | Eclipse starter primer | Videos/V12_Eclipse_starter_primer.mkv | Full source |\n")
        out.write("\n## Exact duplicate recording paths\n\n")
        out.write("| Duplicate original path | Canonical original path | SHA-256 |\n|---|---|---|\n")
        out.write("| Lab 3 Videos and relevant materials/2025-09-23 18-19-19-- validation.mov | 2025-09-23 18-19-19-- validation.mov | b777108288ae3ab935be0012f2af3170696781ea53a989de7caded3839eeb953 |\n")
        out.write("| Lab 3 Videos and relevant materials/2025-09-23 18-51-05- json.mov | 2025-09-23 18-51-05- json.mov | f09d65d1ebb90f3b7c6dcac2057a99767532adb71f7030f18170aa51d9ac4680 |\n")
        out.write("| Lab 3 Videos and relevant materials/2025-09-23 19-19-38-many to one (1).mov | 2025-09-23 19-19-38-many to one.mov | 5b716513210d362d88ad52bdd945e0384fde48c7a5955be17160b54ae9ed7982 |\n")

        for index, (video_id, filename) in enumerate(VIDEOS, 1):
            source = ROOT / filename
            out.write(f"\n\n## {video_id} — {filename}\n\n")
            if not source.exists():
                out.write(f"[SOURCE NOT FOUND: {source}]\n")
                continue
            print(f"Starting transcript {index}/12: {filename}", flush=True)
            cached = cached_transcript(source)
            if cached:
                lines = cached.splitlines()
                for line in lines[2:]:
                    out.write(line + "\n")
                out.write("\nTranscript model: large-v3-turbo, CPU int8.\n")
                print(f"Reused local large-v3-turbo transcript {index}/12", flush=True)
                continue
            if index == 1:
                large_model = WhisperModel("large-v3-turbo", device="cpu", compute_type="int8", cpu_threads=8, num_workers=1)
                segments, info = large_model.transcribe(
                    str(source), language="en", beam_size=5, vad_filter=True,
                    condition_on_previous_text=False,
                )
                used_model = "large-v3-turbo, CPU int8"
            else:
                if small_pipeline is None:
                    local_small = (
                        Path.home()
                        / ".cache" / "huggingface" / "hub"
                        / "models--Systran--faster-whisper-small"
                        / "snapshots" / "536b0662742c02347bc0e980a01041f333bce120"
                    )
                    if not (local_small / "model.bin").exists():
                        raise FileNotFoundError(f"Cached faster-whisper small model is missing: {local_small}")
                    small_model = WhisperModel(str(local_small), device="cpu", compute_type="int8", cpu_threads=8, num_workers=1)
                    small_pipeline = BatchedInferencePipeline(model=small_model)
                    print("Loaded cached small multilingual model for English transcription.", flush=True)
                segments, info = small_pipeline.transcribe(
                    str(source), language="en", beam_size=5, batch_size=8,
                    vad_filter=True, condition_on_previous_text=False,
                )
                used_model = "small multilingual model (English forced), CPU int8, batch size 8"
            out.write(f"Detected language: {info.language} (probability {info.language_probability:.3f}); decoded audio duration: {info.duration:.1f} seconds.\nTranscript model: {used_model}.\n\n")
            segment_count = 0
            last_end = 0.0
            for segment in segments:
                flags = []
                if segment.avg_logprob < -1.0:
                    flags.append("LOW CONFIDENCE")
                if segment.no_speech_prob > 0.7:
                    flags.append("CHECK AUDIO")
                flag_text = f" [{' / '.join(flags)}]" if flags else ""
                out.write(f"[{segment.start:07.2f}-{segment.end:07.2f}] {segment.text.strip()}{flag_text}\n")
                segment_count += 1
                last_end = segment.end
                if segment_count % 20 == 0:
                    out.flush()
                    print(f"{video_id}: {segment_count} segments through {last_end:.1f}s", flush=True)
            out.flush()
            print(f"Completed transcript {index}/12: {segment_count} segments, last timestamp {last_end:.1f}s; {used_model}", flush=True)
    print(f"Transcript file written: {output}", flush=True)


if __name__ == "__main__":
    main()
