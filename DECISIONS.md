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

Treat pistol drill prompts as a future, separate user-authored allowlist. The approved phrases are `Move left`, `Move right`, `Move forward`, `Move back`, `Reload`, `Kneel`, `Stand`, `Get down`, `Hold`, `Stop` and `Drill complete`. Live-fire audio plays from the phone through EARMOR Bluetooth hearing protection; dry-fire audio may use the phone speaker. The watch may mirror the cue and vibrate. The app may randomise only phrases explicitly enabled for the drill and may not provide generated ballistic corrections, live aim instructions, target selection or spoken fire commands.

## M1-005 — Future fixed-camera target sequence

Allow only user-pre-marked stationary target regions in a fixed landscape wide frame. After a configured and confirmed shot group, move a border and sequence-number highlight to the next pre-marked target; crop/zoom is optional and not required. No camera panning, crosshair, live impact analysis, hit inference, correction, aim point or automatic low-confidence advance.

## M2-001 — Database and schema baseline

Use stable Room 2.8.4 with KSP2. The production database is version 2 and carries an explicit bootstrap migration from the pre-database milestone state. Export schema versioning is independent and starts at profile schema v1.

## M2-002 — Built-in scope ownership

Store DNT and Arken families/variants as immutable built-in rows. Any customisation creates a user-owned scope profile. All variants start unverified, and critical edits invalidate a previous verification.

## M2-003 — Import duplicate policy

Never overwrite silently. Import requires duplicate-with-new-UUIDs, safe non-conflicting merge, confirmed replace, or cancel.

## M2-004 — Confirmed target distance handoff

Store each marked static target's measured distance, source, quality, timestamp, uncertainty and confirmation state. A confirmed manual or future camera/range-finder distance may automatically populate the target's DOPE/match distance list. Unconfirmed or low-confidence measurements remain inactive.

## R2-001 — Owner-approved application identity

Use `za.co.bdstudio.dope` as the Android application ID. Keep the existing Kotlin namespace to avoid a non-functional source-tree rename. Initial distribution is private sideloading; keystore custody, release signing and public distribution remain deferred.

## R2-002 — Wear OS hardware baseline

Use the Samsung Galaxy Watch7 44 mm paired with the Samsung Galaxy S25 as the primary future Wear OS baseline. The watch experience must expose shot-count confidence and manual correction, and must not silently advance an unconfirmed match stage.

## R2-003 — Owner scope verification baselines

Record owner-specific physical verification for a DNT TheOne 7–35×56 FFP MIL/TOR-MIL scope and an Arken EP-8 1–8×28 FFP MOA/KLBOX scope. Do not change immutable built-in templates to globally verified: owner measurements belong to user-owned profiles.
