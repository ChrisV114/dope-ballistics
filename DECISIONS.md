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

## M4-001 — Drag data and coefficient ownership

Use the BRL standard-projectile Cd-versus-Mach G1/G7 tables published by JBM Ballistics and linear interpolation. The exact bullet profile owns its manufacturer-declared coefficient and selected drag model. Never infer, convert or substitute a missing coefficient.

## M4-002 — Deterministic numerical baseline

Use SI internal units, fixed-step RK4 with a 0.001 s default, and bracketed bisection for the bore-angle zero. Trace engine version, drag source/model, step and zero iterations. Advanced spin/Coriolis/aerodynamic-jump corrections remain off and outside the core release.

## R4-001 — Owner rifle/load fixtures

Record editable private fixtures for: (1) Howa 6.5 Creedmoor, 26-inch 1:8 barrel, DNT TheOne, 100 m zero, 6 cm sight height, Lapua 139 gr Scenar GB458 G7 0.290 at 809 m/s and 0.1 MIL/click; (2) Smith & Wesson M&P15 Sport III .223 Remington, 16-inch 1:8 barrel, Arken EP-8, 50 m zero, 6 cm sight height, Hornady 53 gr V-MAX G1 0.290 at 920 m/s and 0.25 MOA/click. Do not seed these user values as immutable global defaults.

## R4-002 — Field DOPE observations

Retain the owner's observed 6.5 Creedmoor settings of 3.6 MIL at 500 m, 8.0 MIL at 800 m and 11.5 MIL at 1,000 m as source-labelled comparison points. Do not silently tune the solver to them. Unknown atmosphere and session metadata must remain explicit.

## R5-001 — Future guided truing workflow

A later guided workflow may compare verified observed DOPE with a traceable prediction and propose bounded muzzle-velocity or BC adjustments separately. It must preserve the original profile, observation, residual, environment and calculation trace; require explicit user acceptance into a derived profile; support rollback; and never silently change, jointly overfit or invent G1/G7 data.

## R5-002 — Full DOPE log and learning boundary

Store the calculated setting and actual elevation/windage setting used for every match target with immutable rifle/load/scope/zero revisions, verified range, conditions, engine trace and user confidence. Future calculations may use only an explicitly accepted, versioned true profile derived from matching trusted observations. Never mix equipment fingerprints, equate usage count with accuracy, or train silently from an unconfirmed entry. Detailed fields and acceptance rules are in `DOPE_LOG.md`.

## R5-003 — Multi-rifle match stages

Model rifle/load/scope/zero at the individual engagement level so a single stage can move from the M&P15 Sport III .223 to the Howa 6.5 Creedmoor or any later verified profile. Insert explicit equipment-change steps and require manual readiness confirmation; shot counting may not infer a completed swap. The phone owns plan editing and the future Watch7 companion displays the current identity, setting, count confidence and next step from an offline snapshot. Detailed behaviour is in `MATCH_PLAN.md`.

## R5-004 — Body-carried match phone

During multi-gun match sessions the Galaxy S25 is carried on the user and may remain locked; it is not on the landscape stand. Match plans and the Watch7 must work without camera input. Any future background timing/counting requires an explicit foreground session and user-controlled status. Fixed-camera sequencing remains a separate mode and would require a separate device to operate concurrently.

## M5-001 — Wind convention

Bearings run clockwise from north and describe where wind comes from. Relative direction is normalized to `[-180, 180)`. Positive headwind travels from target toward shooter; positive crosswind moves the projectile right. Magnetic input remains usable for relative components without declination, but true bearings are withheld until an east-positive declination is supplied.

## M5-002 — Immutable range evidence

Completed sessions and verified range observations are append-only and content hashed. A session freezes profile revisions, reference/current environments, per-field sources, distance/uncertainty, direction, inclination, wind, result, trace, engine, rounding and warnings. Calculated and actual values remain distinct; no record changes a ballistic profile.

## R5-005 — Rifle-owned zero geometry

Store the rifle's default zero distance and sight height on its editable rifle profile. Zero setup displays and copies those values into the immutable zero revision rather than asking the user to re-enter them. The owner baselines are 100 m and 6 cm for the Howa, and 50 m and 6 cm for the M&P15 Sport III.

## R5-006 — One-screen live calculator

Treat the supplied original calculation-results image as the production layout target. Keep direct range, direction-of-fire, wind-direction and wind-speed fields with the result hierarchy on one phone screen; do not require repetitive increment/decrement controls. Blank or temporarily invalid editing input must not navigate away or discard the last valid result.

## R5-007 — Unknown zero atmosphere

Historical zero weather is not a hard blocker. The owner may explicitly copy the latest saved conditions as an estimated reference; persist the estimate source and notes, and warn that environmental deviation is approximate. Never present an estimate as measured zero-day weather.

## R5-008 — Profile media ownership

Profile photographs are user-owned local attachments selected through Android's document picker and stored as persistent content URIs. Do not scrape, bundle or redistribute manufacturer product photography without confirmed rights. Keep the neutral local illustration as the deterministic offline fallback.

## R5-009 — Wind correction presentation

The calculator may present the existing model windage as angular correction and rounded turret clicks using the selected verified scope. Always spell out `LEFT` or `RIGHT`; do not expose an unexplained signed click count. Wind observations and actual settings remain logged separately and do not auto-tune future wind predictions.

## M6-001 — Camera calibration boundary

CameraX may inspect camera capabilities, capture an app-private still, place manual anchors, fit multi-distance calibration evidence and warn when the device/camera/resolution/zoom configuration changes. The workflow uses Android camera IDs and physical metadata where exposed and requests 1.0× zoom; it never treats a marketing zoom label as calibration. Camera anchor data does not calculate a target distance or enter a ballistic solution. A target distance remains a separately measured and explicitly confirmed manual value.

## M6-002 — Calibration persistence

Persist one active camera-calibration profile in private application preferences because it is device-local configuration rather than relational profile/session evidence. Store the device/configuration fingerprint, samples, effective focal length, fit errors, valid range, date and app version. Captured JPEG files remain transient app-cache files and are not exported or added to the media library.

## M6-003 — Advanced diagnostics placement

Camera calibration has no ordinary user-facing ranging purpose until a reviewed camera-derived ranging workflow exists. Remove it from Target Range, retain the underlying capability and calibration evidence only under a clearly labelled Advanced diagnostics section, and state on that screen that it does not measure target distance. Confirmed manual or laser distance remains the normal ranging input.

## R6-001 — Galaxy S25 Ultra zoom baseline

The owner confirmed the physical phone is a Samsung Galaxy S25 Ultra. Camera preview controls must use the minimum and maximum zoom ratios reported at runtime by CameraX for the selected logical camera, display the active and available zoom, and provide 1×, 3×, 5×, 10× and maximum shortcuts where supported. Do not hard-code or claim Samsung's marketing 100× mode when Android does not expose it to the app. Changing zoom invalidates the current captured still and unsaved calibration samples, but never changes rifle, DOPE or session data.

Owner physical review on 2026-09-02 confirmed that the selected CameraX camera exposes and applies zoom up to 10× on the Galaxy S25 Ultra. Treat 10× as the verified in-app maximum for the current camera configuration, not as a universal device or firmware constant.

## R6-002 — Full-screen stage target marking

Future fixed-camera stage setup and target-sequence display must use a dedicated full-screen landscape camera surface rather than the standard app shell. Hide the app bottom navigation while that surface is active. Keep exit, target number, marking and sequence controls inside status-bar, camera-cutout and navigation/gesture safe areas. Full-screen display increases usable viewing area but does not increase optical detail or extend the CameraX 10× limit. Continue to allow only user-marked stationary target regions with no automatic target selection, hit detection, aiming overlay or correction.
