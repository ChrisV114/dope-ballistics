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

- Owner baseline resolved 2026-08-31 and clarified 2026-09-02: Samsung Galaxy S25 Ultra on a rigid landscape stand, all targets in one fixed wide frame, with user-marked borders and sequence numbers advancing after confirmed shot groups.
- Owner layout requirement recorded 2026-09-02: stage target marking and sequence display use a dedicated full-screen landscape camera surface with app navigation hidden and all controls clear of Android system controls and the camera cutout.
- Crop/zoom is optional and not required; physical camera panning is unsupported.
- Required before implementation acceptance: physical-device tests for microphone shot-count confidence, camera thermal limits, screen wake behaviour and highlight visibility at target distance.
- Locked boundary: no live hit detection, impact analysis, ballistic correction or aim overlay.

## DR-006 — Milestone 6 Galaxy S25 Ultra camera acceptance

- Status: resolved by owner review on 2026-09-02.
- On the Galaxy S25 Ultra, the owner reported that camera permission, rear-camera selection, stable preview, still capture, anchor dragging, multi-sample calibration, configuration warnings and installed-update behaviour all worked.
- CameraX exposed and applied a maximum of 10× in the reviewed configuration; the slider and supported shortcuts worked. This is not a claim that third-party CameraX can access Samsung Camera's marketed 100× mode.
- Calibration is removed from the normal Target Range workflow and retained only as advanced camera/lens diagnostics.
- This acceptance does not validate camera-derived target range, hit detection, aiming overlays or ballistic handoff; those are not implemented.

## DR-007 — Milestone 7 physical orientation/video acceptance

- Status: owner review required.
- Install version code 15 over the existing signed review build and execute `TRAINING_VIDEO.md` on the Samsung Galaxy S25 Ultra.
- Record ARCore and Depth states, heading/confidence behaviour, portrait/landscape horizon behaviour, video with and without audio, synchronized playback/CSV, 10-minute thermal stability and system-control clearance.
- Burned-in informational-overlay MP4 export remains an implementation requirement before Milestone 7 is complete. It must contain informational orientation only and preserve the locked no-aim-overlay boundary.
