#!/usr/bin/env python3
"""Render the tracked Course Final A as-built Markdown into a reviewable DOCX."""

from __future__ import annotations

import argparse
import re
import subprocess
from pathlib import Path

from docx import Document
from docx.enum.table import WD_CELL_VERTICAL_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Inches, Pt, RGBColor


def shade(cell, fill: str) -> None:
    properties = cell._tc.get_or_add_tcPr()
    shading = properties.find(qn("w:shd"))
    if shading is None:
        shading = OxmlElement("w:shd")
        properties.append(shading)
    shading.set(qn("w:fill"), fill)


def repeat_table_header(row) -> None:
    properties = row._tr.get_or_add_trPr()
    repeat = OxmlElement("w:tblHeader")
    repeat.set(qn("w:val"), "true")
    properties.append(repeat)


def add_page_number(paragraph) -> None:
    paragraph.alignment = WD_ALIGN_PARAGRAPH.RIGHT
    run = paragraph.add_run("Page ")
    begin = OxmlElement("w:fldChar")
    begin.set(qn("w:fldCharType"), "begin")
    instruction = OxmlElement("w:instrText")
    instruction.set(qn("xml:space"), "preserve")
    instruction.text = " PAGE "
    separate = OxmlElement("w:fldChar")
    separate.set(qn("w:fldCharType"), "separate")
    text = OxmlElement("w:t")
    text.text = "1"
    end = OxmlElement("w:fldChar")
    end.set(qn("w:fldCharType"), "end")
    run._r.extend([begin, instruction, separate, text, end])


def add_inline(paragraph, text: str) -> None:
    parts = re.split(r"(\*\*.*?\*\*|`[^`]+`)", text)
    for part in parts:
        if not part:
            continue
        if part.startswith("**") and part.endswith("**"):
            paragraph.add_run(part[2:-2]).bold = True
        elif part.startswith("`") and part.endswith("`"):
            run = paragraph.add_run(part[1:-1])
            run.font.name = "Consolas"
            run.font.size = Pt(8.5)
            run.font.color.rgb = RGBColor(36, 87, 120)
        else:
            paragraph.add_run(part)


def parse_table(lines: list[str]) -> list[list[str]]:
    rows: list[list[str]] = []
    for line in lines:
        cells = [cell.strip() for cell in line.strip().strip("|").split("|")]
        if cells and all(re.fullmatch(r":?-{3,}:?", cell) for cell in cells):
            continue
        rows.append(cells)
    return rows


def style_table(table, rows: list[list[str]]) -> None:
    table.style = "Table Grid"
    table.autofit = False
    width_map = {
        3: (1.50, 2.40, 3.36),
        4: (1.20, 1.25, 1.50, 3.31),
        5: (0.65, 1.35, 1.55, 1.65, 2.06),
    }
    widths = width_map.get(len(table.columns), tuple(7.26 / len(table.columns) for _ in table.columns))
    for column, width in zip(table.columns, widths):
        column.width = Inches(width)
    repeat_table_header(table.rows[0])
    for row_index, row in enumerate(table.rows):
        row._tr.get_or_add_trPr().append(OxmlElement("w:cantSplit"))
        for cell_index, cell in enumerate(row.cells):
            cell.width = Inches(widths[cell_index])
            cell.vertical_alignment = WD_CELL_VERTICAL_ALIGNMENT.TOP
            cell.text = ""
            paragraph = cell.paragraphs[0]
            paragraph.paragraph_format.space_after = Pt(1.5)
            paragraph.paragraph_format.space_before = Pt(0)
            paragraph.paragraph_format.line_spacing = 1.0
            add_inline(paragraph, rows[row_index][cell_index] if cell_index < len(rows[row_index]) else "")
            for run in paragraph.runs:
                run.font.name = "Aptos"
                run.font.size = Pt(7.6 if len(row.cells) >= 5 else 8.3)
                if row_index == 0:
                    run.bold = True
                    run.font.color.rgb = RGBColor(255, 255, 255)
            if row_index == 0:
                shade(cell, "17324D")
            elif row_index % 2 == 0:
                shade(cell, "F2F6F8")


def add_table(document: Document, lines: list[str]) -> None:
    rows = parse_table(lines)
    if not rows:
        return
    if document.paragraphs:
        document.paragraphs[-1].paragraph_format.keep_with_next = True
    columns = max(len(row) for row in rows)
    table = document.add_table(rows=len(rows), cols=columns)
    style_table(table, rows)
    document.add_paragraph().paragraph_format.space_after = Pt(1)


def configure(document: Document, source_commit: str) -> None:
    section = document.sections[0]
    section.top_margin = Inches(0.62)
    section.bottom_margin = Inches(0.62)
    section.left_margin = Inches(0.62)
    section.right_margin = Inches(0.62)

    normal = document.styles["Normal"]
    normal.font.name = "Aptos"
    normal.font.size = Pt(9.5)
    normal.font.color.rgb = RGBColor(38, 51, 64)
    normal.paragraph_format.space_after = Pt(5)
    normal.paragraph_format.line_spacing = 1.05

    for name, size, color in (
        ("Title", 25, "17324D"),
        ("Heading 1", 18, "17324D"),
        ("Heading 2", 13, "245778"),
        ("Heading 3", 10.5, "8B6B2E"),
    ):
        style = document.styles[name]
        style.font.name = "Aptos Display"
        style.font.size = Pt(size)
        style.font.bold = True
        style.font.color.rgb = RGBColor.from_string(color)
        style.paragraph_format.keep_with_next = True

    header = section.header.paragraphs[0]
    header.text = "ORGRESOURCE  /  COURSE FINAL A  /  AS-BUILT"
    header.style = document.styles["Caption"]
    header.alignment = WD_ALIGN_PARAGRAPH.RIGHT
    for run in header.runs:
        run.font.color.rgb = RGBColor(77, 103, 124)
        run.font.size = Pt(8)

    footer = section.footer.paragraphs[0]
    footer.text = "Technical companion • Source commit " + source_commit[:12] + "   "
    footer.style = document.styles["Caption"]
    footer.runs[0].font.size = Pt(8)
    footer.runs[0].font.color.rgb = RGBColor(77, 103, 124)
    add_page_number(footer)

    document.core_properties.title = "OrgResource Course Final A — As-Built Proposal"
    document.core_properties.subject = "Current technical implementation and acceptance reference"
    document.core_properties.author = "OrgResource Course A technical build"
    document.core_properties.keywords = "CSCI 180.05, OrgResource, as-built, Course Final A"
    document.core_properties.comments = "Generated from the tracked Markdown source at " + source_commit


def build(markdown_path: Path, output_path: Path, source_commit: str) -> None:
    source = markdown_path.read_text(encoding="utf-8-sig").splitlines()
    document = Document()
    configure(document, source_commit)
    index = 0
    while index < len(source):
        line = source[index].strip()
        if not line:
            index += 1
            continue
        if line.startswith("|"):
            table_lines: list[str] = []
            while index < len(source) and source[index].strip().startswith("|"):
                table_lines.append(source[index].strip())
                index += 1
            add_table(document, table_lines)
            continue
        if line.startswith("# "):
            paragraph = document.add_paragraph(style="Title")
            add_inline(paragraph, line[2:].strip())
            source_note = document.add_paragraph("Generated from committed source " + source_commit, style="Subtitle")
            source_note.paragraph_format.space_after = Pt(12)
        elif line.startswith("## "):
            paragraph = document.add_paragraph(style="Heading 1")
            add_inline(paragraph, line[3:].strip())
        elif line.startswith("### "):
            paragraph = document.add_paragraph(style="Heading 2")
            add_inline(paragraph, line[4:].strip())
        elif line.startswith("#### "):
            paragraph = document.add_paragraph(style="Heading 3")
            add_inline(paragraph, line[5:].strip())
        elif line.startswith("- "):
            paragraph = document.add_paragraph(style="List Bullet")
            add_inline(paragraph, line[2:].strip())
        else:
            paragraph = document.add_paragraph()
            add_inline(paragraph, line)
        index += 1

    output_path.parent.mkdir(parents=True, exist_ok=True)
    document.save(output_path)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--source-commit", required=True, help="Full Git source SHA used to generate this document.")
    parser.add_argument("--source", default="docs/academic-final/COURSE_FINAL_A_AS_BUILT.md")
    parser.add_argument("--output", required=True)
    args = parser.parse_args()
    if not re.fullmatch(r"[0-9a-f]{40}", args.source_commit):
        parser.error("--source-commit must be a full 40-character lowercase Git SHA.")
    repository_root = Path(__file__).resolve().parent.parent
    current_commit = subprocess.check_output(
        ["git", "-C", str(repository_root), "rev-parse", "HEAD"], text=True
    ).strip()
    working_changes = subprocess.check_output(
        ["git", "-C", str(repository_root), "status", "--porcelain"], text=True
    ).strip()
    if current_commit != args.source_commit or working_changes:
        parser.error("Generate the DOCX only from the exact clean committed source SHA.")
    build(Path(args.source), Path(args.output), args.source_commit)
    print("DOCX created: " + str(Path(args.output).resolve()))
    print("Source commit: " + args.source_commit)


if __name__ == "__main__":
    main()
