# Changelog

## Unreleased

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
