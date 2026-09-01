# Decisions Required

## DR-001 — Release application ID

- Status: resolved 2026-08-31.
- Owner-approved value: `za.co.bdstudio.dope`.
- Initial distribution: private sideloading.
- Kotlin namespace remains independent at `za.co.dope.ballistics`.

## DR-002 — Release signing and distribution

- Status: private review signing resolved 2026-09-01; public release/distribution remains deferred.
- The owner-approved private review key signs local and GitHub review APKs through ignored local properties and encrypted GitHub Actions secrets. The key and passwords do not belong in Git.
- Select long-term release-key custody and the public distribution channel before Milestone 11 release work.

## DR-003 — Wear OS companion scope

- Requested capability: match plan with target/range DOPE, shot-count progression, next-setting wake display and shot timer.
- Hardware resolved 2026-08-31: Samsung Galaxy Watch7 44 mm paired with Samsung Galaxy S25; layouts must tolerate the smaller 40 mm display.
- Packaging still required before implementation: confirm the companion module/application packaging when the Wear OS milestone is scheduled.
- Safety/reliability gate: acoustic shot counting must expose confidence, support manual correction and never silently advance an unconfirmed match stage.

## DR-004 — Pistol drill cue vocabulary

- Requested capability: user-programmed Bluetooth cues for pistol practice movements and reload/position drills.
- Status: resolved 2026-08-31.
- Approved vocabulary: `Move left`, `Move right`, `Move forward`, `Move back`, `Reload`, `Kneel`, `Stand`, `Get down`, `Hold`, `Stop`, `Drill complete`.
- Playback: EARMOR Bluetooth hearing protection for live fire; phone speaker for dry fire; optional watch display/vibration.
- Locked boundary: no generated cues, ballistic corrections, aim points, target selection, spoken fire commands or impact-derived instructions.

## DR-005 — Fixed-camera sequencing hardware acceptance

- Owner baseline resolved 2026-08-31: Samsung S25 on a rigid landscape stand, all targets in one fixed wide frame, with user-marked borders and sequence numbers advancing after confirmed shot groups.
- Crop/zoom is optional and not required; physical camera panning is unsupported.
- Required before implementation acceptance: physical-device tests for microphone shot-count confidence, camera thermal limits, screen wake behaviour and highlight visibility at target distance.
- Locked boundary: no live hit detection, impact analysis, ballistic correction or aim overlay.
