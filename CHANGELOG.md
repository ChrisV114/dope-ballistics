# Changelog

## Unreleased

### Milestone 5

- Added review fixes for a complete fresh-install path: explicit rifle/load/scope selection, G1 or G7 ammunition, physically confirmed dial directions, reference-atmosphere and zero creation, and a persistent active setup.
- Replaced the blocked calculation preview with the offline engine result, exact missing-input guidance, confirmed-target distance shortcuts, and live wind speed/direction controls.
- Restored the approved original calculation-results and wind-panel hierarchy as the production visual target: compact result grids, elevation dial, compass-first wind wheel, component tiles and speed tiles, with editors below the primary panels.
- Added Room version 5 active-setup selection with a non-destructive `4 -> 5` migration and active multi-setup switching.
- Added tested true/magnetic wind-from conventions, relative direction, signed cross/head components and min/average/max/gust brackets.
- Added an interactive manual wind wheel with tap/drag, clock/numeric entry, fine adjustment, reset, source, timestamp, notes and lock state.
- Added verified-profile range cards with confirmed saved-target distance inclusion and offline CSV, PDF and PNG sharing.
- Added Room version 4 append-only session snapshots and verified range records with complete calculation/profile/environment evidence and SHA-256 hashes.
- Kept theoretical and actual settings separate with explicit confidence and `CALCULATED`, `VERIFIED`, `BLENDED` and `DO_NOT_USE` states.
- Added non-destructive baseline/alternative comparison and new phone/landscape visual baselines.

### Maintenance

- Updated the GitHub-hosted CI actions to their current Node-runtime-compatible major releases: checkout v7, setup-java v6, setup-gradle v6 and upload-artifact v7.

### Milestone 4

- Added the pure Kotlin/JVM `:ballistics` module and stable solve/range-card contracts.
- Added BRL/JBM G1 and G7 drag tables, linear interpolation and air-relative fixed-step RK4 integration.
- Added root-solved zero, inclined line-of-sight geometry, reference/current atmosphere comparison and environmental deviation.
- Added velocity, energy, Mach/state, MIL/MOA click rounding, residual, travel/revolution checks and sensitivity uncertainty v1.
- Added deterministic calculation traces, invalid-input blocking and a verified Android profile-to-engine mapping boundary.
- Added owner-provided Howa 6.5 Creedmoor/Lapua 139 gr and M&P15 Sport III/Hornady 53 gr regression fixtures without seeding private data as factory defaults.
- Added source-labelled 500/800/1,000 m owner field-DOPE comparison coverage and recorded a future explicit, reversible truing workflow.
- Specified the Milestone 5 per-target actual-setting log and evidence-gated equipment-specific learning rules.
- Specified engagement-level multi-rifle stages and explicit Watch7 equipment-change confirmation.
- Separated body-carried phone/watch match operation from rigid-stand fixed-camera mode.

### Milestone 3

- Added optional-sensor diagnostics, bounded barometer statistics and stable rotation-vector orientation capture.
- Added one-shot foreground location with permission-denied and approximate-location fallbacks.
- Added offline manual environmental entry, per-field provenance/quality/time and persisted atmospheric snapshots.
- Added Buck vapour pressure, moist-air density, dew point, pressure altitude, numerically solved density altitude and speed of sound.
- Added provider-neutral weather/terrain seams, an explicitly invoked Open-Meteo adapter and timestamped weather cache.
- Added Room migration `2 -> 3`, environment JVM/instrumentation coverage and a locked environment screen golden.

### Post-Milestone-2 owner review

- Approved `za.co.bdstudio.dope` as the Android application ID for initial private sideloading.
- Recorded the Galaxy Watch7 44 mm, EARMOR/phone cue playback, fixed wide-frame target highlighting and closed pistol-drill vocabulary baselines.
- Recorded owner-specific DNT TheOne and Arken EP-8 physical verification values without changing immutable built-in templates to globally verified.

### Milestone 2

- Added Room version-2 storage, schema export and a tested `1 -> 2` bootstrap migration.
- Added UUID/revision/archive-aware rifle, ammunition, chronograph, scope, zero, atmosphere, saved-range and static-target records.
- Added immutable DNT TheOne and Arken EP-8 templates, physical verification and critical-field invalidation.
- Enforced the KLBOX BDC no-generic-hold rule.
- Added schema-v1 profile JSON with privacy-safe export, strict validation and explicit duplicate policies.
- Connected rifle, ammunition, scope verification and confirmed target-distance creation to Room-backed screens.
- Added confirmed marked-target distance fields for future camera/range-finder to DOPE/match-list handoff.

### Milestone 1

- Implemented locked colours, typography, spacing, wordmark and topographic treatment.
- Added edge-to-edge navigation that keeps app controls clear of gesture and three-button system controls.
- Added reusable cards, chips, buttons, fields and result panels.
- Added splash, dashboard, profile, environment, wind, result, range-card, session, camera-calibration and target-range shells.
- Added manual, IDPA, A4/A3 and nominal gong target-size choices with explicit confirmation.
- Added token/safe-area tests, navigation/accessibility instrumentation coverage and six visual golden baselines.
- Documented future match-plan, Wear OS shot-count/timer, fixed-camera sequence and pistol-drill cue requirements without implementing later milestones.

### Milestone 0

- Established Android/Gradle repository and version catalogue.
- Added minimal Compose app shell and JVM foundation test.
- Added lint, Detekt, Spotless, dependency locking and CI baselines.
- Added authoritative specification/design assets and documentation skeleton.
- Recorded package-ID and release-signing decision gates.
