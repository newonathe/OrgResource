from __future__ import annotations

import csv
import hashlib
import json
import zipfile
from datetime import datetime
from pathlib import Path

from docx import Document
from pypdf import PdfReader

PACK = Path(__file__).resolve().parent
ROOT = PACK.parents[1]
UPLOAD = PACK / "UPLOAD"


def read_text(path: Path) -> str:
    return path.read_text(encoding="utf-8-sig", errors="replace")


def add_block(out, title: str, body: str) -> None:
    out.write(f"\n\n{'=' * 88}\nSOURCE: {title}\n{'=' * 88}\n\n")
    out.write(body.rstrip())
    out.write("\n")


def docx_text(path: Path) -> str:
    doc = Document(path)
    chunks = []
    for p in doc.paragraphs:
        if p.text.strip():
            chunks.append(p.text)
    for ti, table in enumerate(doc.tables, 1):
        chunks.append(f"\n[TABLE {ti}]")
        for row in table.rows:
            chunks.append(" | ".join(cell.text.replace("\n", " / ") for cell in row.cells))
    return "\n".join(chunks)


def pdf_text(path: Path) -> str:
    reader = PdfReader(str(path))
    chunks = []
    for page_no, page in enumerate(reader.pages, 1):
        chunks.append(f"\n--- PAGE {page_no} ---\n")
        chunks.append(page.extract_text() or "[No extractable text on this page.]")
    return "\n".join(chunks)


def combine_corpora() -> None:
    course = [
        "project-research/COURSE-AND-SOURCE-AUDIT.md",
        "project-research/evidence/course-primary.txt",
        "project-research/evidence/course-review.txt",
        "project-research/evidence/course-corpus.txt",
        "project-research/evidence/decks-and-labs.txt",
        "project-research/evidence/code-review.txt",
        "project-research/evidence/reference-review.txt",
        "project-research/evidence/course-manifest.json",
        "project-research/evidence/ESP Project Proposal 2026.pdf.txt",
        "project-research/evidence/CS-DISCS-CSCI180.05-JONGKO_J-O-2026-1.pdf.txt",
    ]
    path = UPLOAD / "03_COURSE_REFERENCE_CORPUS.txt"
    with path.open("w", encoding="utf-8", newline="\n") as out:
        out.write("CSCI 180.05 course documents, lecture material, labs, and code corpus.\n")
        out.write("This compilation preserves source labels and extracted text; it is not a substitute for the attached original assignment and syllabus PDFs.\n")
        for relative in course:
            source = ROOT / relative
            if source.exists():
                add_block(out, relative, read_text(source))
            else:
                add_block(out, relative, f"[SOURCE FILE MISSING: {relative}]")

    project_files = [
        "OrgResource/README.md",
        *[f"OrgResource/{i:02d}-{name}.md" for i, name in [
            (1, "SOURCE-AND-REQUIREMENTS"),
            (2, "COMPLETE-PROJECT-SPECIFICATION"),
            (3, "TECHNICAL-ARCHITECTURE"),
            (4, "DATABASE-SPECIFICATION"),
            (5, "REST-API-CATALOGUE"),
            (6, "COMPONENT-SPECIFICATION"),
            (7, "INTEGRATION-AND-SCHEDULING"),
            (8, "POSTMAN-TESTING-AND-DEMONSTRATION"),
            (9, "FORMAL-PROPOSAL-DRAFT"),
        ]] + ["OrgResource/10-IMPLEMENTATION-HANDOFF.md"],
        "OrgResource/OrgResource-Concise-Submission-Proposal.md",
        "OrgResource/qa/QUALITY-REVIEW.md",
        "OrgResource/qa/READABLE-PART1-REVIEW.md",
        "OrgResource/qa/consistency-check.json",
        "OrgResource/qa/delivery-check.json",
        "OrgResource/qa/layout-check.json",
        "OrgResource/qa/diagram-check.json",
        "OrgResource/qa/concise-layout-check.json",
        "OrgResource/qa/before-readable-revision/OrgResource-Concise-Submission-Proposal.md",
        "OrgResource/DELIVERY-MANIFEST.json",
        "OrgResource/evidence/source-inventory.json",
        "OrgResource/contracts/domain-model.json",
        "OrgResource/contracts/operations.json",
        "OrgResource/contracts/response-examples.json",
        "OrgResource/postman/OrgResource-API.postman_collection.json",
        "OrgResource/postman/OrgResource-Demo.postman_collection.json",
        "OrgResource/postman/OrgResource-Negative.postman_collection.json",
        "OrgResource/postman/OrgResource-Local.postman_environment.json",
    ]
    path = UPLOAD / "04_ORGRESOURCE_PRIOR_WORK.md"
    with path.open("w", encoding="utf-8", newline="\n") as out:
        out.write("# Existing OrgResource Work: Complete Text and Contracts\n")
        out.write("Prior drafts and QA are supplied for inspection, not as binding or implementation-verified decisions. Original proposal PDFs/DOCX files are also indexed in the manifest.\n")
        for relative in project_files:
            source = ROOT / relative
            if source.exists():
                add_block(out, relative, read_text(source))
            else:
                add_block(out, relative, f"[SOURCE FILE MISSING: {relative}]")
        for relative in [
            "OrgResource/COA_ProjectProposal_EthanOwenTaruc.pdf",
            "OrgResource/OrgResource-Concise-Submission-Proposal.docx",
            "OrgResource/OrgResource-Formal-Proposal.docx",
            "OrgResource/OrgResource-Taruc-Proposal.docx",
            "OrgResource/qa/before-readable-revision/OrgResource-Concise-Submission-Proposal.docx",
        ]:
            source = ROOT / relative
            if source.exists():
                body = pdf_text(source) if source.suffix.lower() == ".pdf" else docx_text(source)
                add_block(out, relative, body)

    research = [
        "project-research/RESEARCH-AND-RECOMMENDATION.md",
        "project-research/SHIFT-COVER-PRELIMINARY-SPEC.md",
        "project-research/erp-focus/ERP-RESEARCH-AND-DECISION.md",
        "project-research/decision-data.json",
        "project-research/erp-focus/research-data.json",
        "project-research/erp-focus/evidence/source-manifest.json",
        "project-research/erp-focus/evidence/options-qa.json",
        "project-research/erp-focus/evidence/check-options.cjs",
        "project-research/evidence/reference-corpus.txt",
        "project-research/evidence/research-qa.json",
        "project-research/evidence/course-manifest.json",
    ]
    research += [
        f"project-research/erp-focus/evidence/{p.name}"
        for p in sorted((ROOT / "project-research/erp-focus/evidence").glob("E*.txt"))
    ]
    path = UPLOAD / "05_PRIOR_RESEARCH_EVIDENCE.md"
    with path.open("w", encoding="utf-8", newline="\n") as out:
        out.write("# Prior Research and Evidence Archive\n")
        out.write("Historical sources and decisions are leads requiring current verification where facts may have changed. Reference-pack content is not Ateneo evidence.\n")
        for relative in research:
            source = ROOT / relative
            if source.exists():
                add_block(out, relative, read_text(source))
            else:
                add_block(out, relative, f"[SOURCE FILE MISSING: {relative}]")


def digest_stream(stream) -> str:
    h = hashlib.sha256()
    while True:
        block = stream.read(8 * 1024 * 1024)
        if not block:
            break
        h.update(block)
    return h.hexdigest()


def build_manifest() -> None:
    out_path = UPLOAD / "07_COMPLETE_SOURCE_MANIFEST.csv"
    rows = []
    seen: dict[str, str] = {}
    source_files = sorted(
        p for p in ROOT.rglob("*")
        if p.is_file() and PACK not in p.parents
    )
    for path in source_files:
        rel = path.relative_to(ROOT).as_posix()
        with path.open("rb") as stream:
            sha = digest_stream(stream)
        rows.append({
            "record_type": "filesystem_file",
            "path": rel,
            "parent_archive": "",
            "size_bytes": path.stat().st_size,
            "modified": datetime.fromtimestamp(path.stat().st_mtime).astimezone().isoformat(timespec="seconds"),
            "sha256": sha,
            "duplicate_of": seen.get(sha, ""),
            "status": "hashed",
        })
        seen.setdefault(sha, rel)
        if path.suffix.lower() == ".zip":
            try:
                with zipfile.ZipFile(path) as archive:
                    for info in archive.infolist():
                        if info.is_dir():
                            continue
                        nested = f"{rel}::{info.filename}"
                        with archive.open(info) as stream:
                            member_sha = digest_stream(stream)
                        rows.append({
                            "record_type": "archive_member",
                            "path": nested,
                            "parent_archive": rel,
                            "size_bytes": info.file_size,
                            "modified": datetime(*info.date_time).isoformat(timespec="seconds"),
                            "sha256": member_sha,
                            "duplicate_of": seen.get(member_sha, ""),
                            "status": "hashed",
                        })
                        seen.setdefault(member_sha, nested)
            except Exception as exc:
                rows.append({
                    "record_type": "archive_error",
                    "path": rel,
                    "parent_archive": rel,
                    "size_bytes": "",
                    "modified": "",
                    "sha256": "",
                    "duplicate_of": "",
                    "status": f"ERROR: {type(exc).__name__}: {exc}",
                })
    with out_path.open("w", encoding="utf-8-sig", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=[
            "record_type", "path", "parent_archive", "size_bytes",
            "modified", "sha256", "duplicate_of", "status",
        ])
        writer.writeheader()
        writer.writerows(rows)
    print(f"Manifest: {len(source_files)} filesystem files plus archive members -> {out_path}")


if __name__ == "__main__":
    UPLOAD.mkdir(parents=True, exist_ok=True)
    combine_corpora()
    build_manifest()
