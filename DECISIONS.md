# Decisions

## M0-001 — Repository root

The Android repository is rooted at `D:\1Dope`. The supplied ZIP and extracted build pack are retained unchanged as source evidence.

## M0-002 — Initial module set

Create only `:app` during Milestone 0. Add logical modules when they gain real code, avoiding dozens of empty modules.

## M0-003 — Toolchain baseline

Use stable AGP 9.3.2, Gradle 9.7.1, JDK 17, compile/target SDK 37, and min SDK 28. Exact dependency pins are in `DEPENDENCY_VERSIONS.md` and the version catalogue. Gradle plugin resolution maps the Android plugin ID to its published Google Maven module explicitly.

## M0-004 — Formatting

Use Spotless with ktlint. CI treats format, lint, static-analysis, unit-test, and assembly failures as blocking.

## M1-001 — System-control clearance

Use edge-to-edge rendering with Scaffold-managed safe drawing and a bottom navigation container that owns the Android navigation-bar inset. Preserve the locked 72–80 dp band for common insets and expand for larger three-button controls rather than risk overlap.

## M1-002 — Golden-test exception

Use Android Compose Screenshot Testing 0.0.1-alpha16 for the required AGP 9-compatible host-side goldens. Keep the experimental plugin isolated to the screenshot source set and record it as the only pre-release dependency exception.

## M1-003 — Target-size presets

Provide design-shell choices for manual dimensions, official IDPA cardboard dimensions, ISO A-series paper and nominal circular/custom gongs. Presets are never silent assumptions and require user confirmation.

## M1-004 — Future pistol drill cues

Treat pistol drill prompts as a future, separate user-authored allowlist. They may cover movement, position, reload and timer events, but may not provide generated ballistic corrections, live aim instructions, target selection or spoken fire commands.

## M1-005 — Future fixed-camera target sequence

Allow only user-pre-marked stationary target regions and deterministic digital crop changes between configured shot groups. No camera panning, live impact analysis, hit inference, correction, aim point or automatic low-confidence advance.

## M2-001 — Database and schema baseline

Use stable Room 2.8.4 with KSP2. The production database is version 2 and carries an explicit bootstrap migration from the pre-database milestone state. Export schema versioning is independent and starts at profile schema v1.

## M2-002 — Built-in scope ownership

Store DNT and Arken families/variants as immutable built-in rows. Any customisation creates a user-owned scope profile. All variants start unverified, and critical edits invalidate a previous verification.

## M2-003 — Import duplicate policy

Never overwrite silently. Import requires duplicate-with-new-UUIDs, safe non-conflicting merge, confirmed replace, or cancel.

## M2-004 — Confirmed target distance handoff

Store each marked static target's measured distance, source, quality, timestamp, uncertainty and confirmation state. A confirmed manual or future camera/range-finder distance may automatically populate the target's DOPE/match distance list. Unconfirmed or low-confidence measurements remain inactive.
