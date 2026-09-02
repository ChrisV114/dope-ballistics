# Changelog

## Unreleased

### Milestone 6

- Added CameraX 1.6.2 preview and app-private still capture with explicit runtime permission.
- Added rear-camera capability inspection for camera IDs, focal lengths, sensor/active-array geometry, capture sizes, zoom range, OIS, distortion metadata and logical physical-camera IDs where Android exposes them.
- Added runtime-reported camera zoom with active/range readouts, a continuous slider and supported 1×/3×/5×/10×/Max shortcuts for the owner's Galaxy S25 Ultra review.
- Changing camera zoom now clears the transient still and unsaved calibration samples without affecting rifle, DOPE or session data.
- Recorded physical S25 Ultra confirmation of the CameraX 10× maximum and the future requirement for a system-safe full-screen landscape stage-marking surface.
- Added explicit camera-ID selection, a requested 1.0× zoom baseline, draggable still-image calibration anchors and tape-measured calibration sample entry.
- Added local multi-distance calibration fitting with effective focal length, mean absolute error, median percentage error, 95th-percentile error and valid-distance range.
- Added persistent device/configuration fingerprints and visible wrong-camera, wrong-resolution and changed-zoom warnings.
- Added a Galaxy S25 landscape physical-acceptance checklist and a locked phone-sized camera-calibration golden.
- Kept camera calibration isolated from target-distance calculation and ballistic handoff; target distance remains separately measured and confirmed manually.
- Completed owner physical S25 Ultra review, removed calibration from the normal Target Range workflow and retained it only as clearly labelled advanced camera diagnostics with no range result.

### Milestone 5

- Replaced the non-interactive elevation gauge with the shared touch/drag wind-direction wheel and added unambiguous wind turret output such as `6 clicks LEFT` or `3 clicks RIGHT` from the selected scope's click value.
- Added direct verified-setup switching on the calculation screen and a bottom actual-setting handoff into the immutable session/DOPE log; observations remain separate and never silently tune a profile.
- Added editable rifle and ammunition/load forms, optional cartridge overall length, owner-selected persistent local photos for rifle/load/scope profiles and a non-destructive Room `8 -> 9` migration.
- Made foreground location collection listen to GPS and network providers together, added a recent-fix fallback, corrected environment-number formatting, and updates altitude from GPS or the weather model with visible provenance.
- Made top-level bottom navigation deterministic from nested screens, exposed Free shoot/Match/Shot timer session structure, and replaced non-functional camera buttons with an honest Milestone 6 deferred state.
- Updated the private signed review APK to version code 10 / `0.5.5-m5-review` so it installs over the prior signed review build.
- Reworked the calculation screen to the supplied original-UI target with direct one-screen range, direction-of-fire, wind-direction and wind-speed entry; intermediate blank/invalid edits retain the last valid result instead of replacing the screen.
- Moved rifle zero distance and sight height into the rifle profile, seeded the owner test rifles with their confirmed values, and added a non-destructive Room `7 -> 8` migration.
- Added a clearly labelled estimated-reference path for users who do not know the historical zero-day atmosphere, while keeping exact source provenance and an approximation warning in results.
- Kept unsaved rifle/load/scope choices when navigating through Home, surfaced the active/draft setup and current conditions on the dashboard, and tightened interior headers to match the original compact layout.
- Added local rifle, ammunition and scope illustrations to saved profile cards, with profile-specific owner photographs still replaceable later.
- Added a Room `6 -> 7` repair migration for devices that opened an early review build but did not receive the owner test-profile rows.
- Added the owner-requested Howa 6.5 Creedmoor/Lapua 139 gr and M&P15 Sport III/Hornady 53 gr editable test profiles, plus DNT TheOne MIL and Arken EP-8 MOA/KLBOX scope copies that remain explicitly unverified.
- Added Room version 6 idempotent starter-profile insertion so an installed `0.5.1` database receives the profiles without duplicating or replacing user-created records.
- Added visible saved-rifle and saved-ammunition cards above their entry forms.
- Added review fixes for a complete fresh-install path: explicit rifle/load/scope selection, G1 or G7 ammunition, physically confirmed dial directions, reference-atmosphere and zero creation, and a persistent active setup.
- Replaced the blocked calculation preview with the offline engine result, exact missing-input guidance, confirmed-target distance shortcuts, and live wind speed/direction controls.
- Restored the approved original calculation-results and wind-panel hierarchy as the production visual target: compact result grids, compass-first wind wheel, component tiles and speed tiles, with editors below the primary panels.
- Added Room version 5 active-setup selection with a non-destructive `4 -> 5` migration and active multi-setup switching.
- Added tested true/magnetic wind-from conventions, relative direction, signed cross/head components and min/average/max/gust brackets.
- Added an interactive manual wind wheel with tap/drag, clock/numeric entry, fine adjustment, reset, source, timestamp, notes and lock state.
- Added verified-profile range cards with confirmed saved-target distance inclusion and offline CSV, PDF and PNG sharing.
- Added Room version 4 append-only session snapshots and verified range records with complete calculation/profile/environment evidence and SHA-256 hashes.
- Kept theoretical and actual settings separate with explicit confidence and `CALCULATED`, `VERIFIED`, `BLENDED` and `DO_NOT_USE` states.
- Added non-destructive baseline/alternative comparison and new phone/landscape visual baselines.

### Maintenance

- Enabled the Android emulator runner's documented KVM permissions on Ubuntu so instrumentation uses hardware acceleration instead of an unstable unaccelerated boot.
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
