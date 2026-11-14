# Academic Glass Commons v5

**Implemented:** 2026-11-15

**Status:** approved visual direction; UI source is the implementation

**Scope:** React frontend O8–O10

## Identity

OrgResource is presented as a university library and academic planning commons: architectural linework, a ledger/book motif, editorial serif headings, and legible sans-serif controls and records. The decorative architecture is an inline SVG drawn with ordinary paths. It uses no institution seal, remote font, copied Apple asset, photograph, or runtime design library. The public [`dickwu/apple-design-skill` repository](https://github.com/dickwu/apple-design-skill) is design-review inspiration only; it is not an official Apple resource, an OrgResource dependency, or a source of copied assets.

The approved interactive reference remains in the originating ChatGPT design context and was not exported into this repository. This implementation follows the approved written design direction and has its own rendered-browser QA; it does not claim a pixel-by-pixel comparison to an unavailable exported mockup.

## Palette tokens

The academic blue, book-gold, and architectural palette remains. The light text and weak-glass values were refined after a readability review; the dark palette remains unchanged.

| Token | Light | Dark |
| --- | --- | --- |
| `--bg-1` | `#edf1f0` | `#101d2a` |
| `--bg-2` | `#dbe7f1` | `#182c40` |
| `--bg-3` | `#efe8d9` | `#25313c` |
| `--glass` | `rgba(255,255,255,.70)` | `rgba(25,41,56,.74)` |
| `--glass-strong` | `rgba(255,255,255,.85)` | `rgba(27,42,57,.91)` |
| `--glass-weak` | `rgba(255,255,255,.56)` | `rgba(32,49,66,.56)` |
| `--ink` | `#10263d` | `#f0f5fa` |
| `--body-text` | `#263b50` | `#d4e0ed` |
| `--muted-text` | `#40566b` | `#b9cadd` |
| `--line` | `rgba(91,117,145,.36)` | `rgba(164,190,220,.22)` |
| `--navy` | `#214c78` | `#a5caff` |
| `--gold` | `#765318` | `#f0cd89` |
| `--success` | `#17684e` | `#8ce4b8` |
| `--warning` | `#84571d` | `#f5cf91` |
| `--danger` | `#a0384b` | `#ffacb9` |

## Layout and behavior

- Desktop uses a fixed-width frosted sidebar, floating toolbar, and fluid workspace. Intermediate widths collapse the sidebar; narrow screens use a bottom navigation bar and a separate More drawer.
- Main content uses responsive grids, individually labeled records, horizontally scrollable tables where necessary, and contextual sheets for detail views.
- Glass surfaces use blur and translucent fills over the three-color academic gradient. A reduced-transparency fallback uses solid surfaces.
- Pointer lighting, small card tilt, magnetic button movement, and navigation transitions are controlled by a visible Motion setting. The default honors `prefers-reduced-motion`; CSS disables motion under that preference and in the explicit off state.
- Light/dark theme and the motion preference are the only browser values stored in `localStorage`. Business records always come from the API.
- Semantic headings, skip navigation, visible keyboard focus, labeled controls, modal Escape handling, focus containment/restoration, text status labels, and forced-color borders are part of the shared shell.
- The application makes no adoption, institutional integration, authentication, SMS delivery, or measured-outcome claim.

## Readability and interaction refinement

- Display headings and editorial section titles use Georgia; navigation, forms, tables, and records use one system sans-serif stack. Type sizes for labels, metadata, and data were raised to keep the page readable at ordinary desktop and mobile zoom. Hierarchy comes from size, weight, alignment, and grouping before color.
- Light-mode ink, body, muted, gold, and separator tokens were darkened or strengthened. A calculation over the three light-gradient stops composited with the translucent glass fills gives minimum text-token contrasts of 6.30:1 for gold, 6.89:1 for muted text, 10.44:1 for body text, and 13.93:1 for ink. This is a token-level sRGB calculation, not a claim that every rendered text/background pair has been formally certified.
- Content and navigation keep translucent, blurred surfaces, with a moving cursor highlight on the main glass panels. Pointer-only icon and primary-action feedback is small and brief; it respects the Motion setting and the operating system's reduced-motion preference. Touch layouts keep their larger navigation targets.
- A `prefers-contrast: more` palette uses stronger text and borders, while `prefers-reduced-transparency: reduce` replaces translucent fills with solid surfaces.

The review used the linked [apple-design-skill guidance](https://github.com/dickwu/apple-design-skill), Apple's [design principles](https://developer.apple.com/design/human-interface-guidelines/design-principles/) and [UI readability guidance](https://developer.apple.com/design/tips/), plus the W3C [WCAG 2.2 minimum-contrast criterion](https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum). The third-party skill is used as a review reference only; it is not installed, copied into the app, or represented as an official Apple resource. These principles were translated to this React web application rather than treated as native Apple-platform requirements.

The reference artwork, typography, responsive rules, and motion remain subordinate to readable forms and saved operational data. CLO9 image handling remains out of scope pending an authoritative course requirement.
