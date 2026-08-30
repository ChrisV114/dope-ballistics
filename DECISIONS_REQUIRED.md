# Decisions Required

## DR-001 — Release application ID

- Current working value: `za.co.dope.ballistics`
- Required by: before the first signed public release
- Owner decision: confirm or replace the application ID
- Safety state: no signed public release may proceed without confirmation because this identifier is long-lived and difficult to change.

## DR-002 — Release signing and distribution

Select keystore custody, signing process, and intended distribution channel before Milestone 11 release work. No signing secrets belong in Git.

## DR-003 — Wear OS companion scope

- Requested capability: match plan with target/range DOPE, shot-count progression, next-setting wake display and shot timer.
- Required before implementation: select supported watch hardware/Wear OS baseline and decide whether the companion is a separate module or application.
- Safety/reliability gate: acoustic shot counting must expose confidence, support manual correction and never silently advance an unconfirmed match stage.

## DR-004 — Pistol drill cue vocabulary

- Requested capability: user-programmed Bluetooth cues for pistol practice movements and reload/position drills.
- Required before implementation: owner approves the exact closed vocabulary and whether cues play from phone, watch or both.
- Locked boundary: no generated cues, ballistic corrections, aim points, target selection, spoken fire commands or impact-derived instructions.

## DR-005 — Fixed-camera sequencing hardware acceptance

- Requested capability: pre-mark target order and switch digital crop after the configured shots per target.
- Required before implementation: physical-device tests for microphone shot-count confidence, camera thermal limits, screen wake behaviour and supported zoom/crop transitions.
- Locked boundary: no live hit detection, impact analysis, ballistic correction or aim overlay.
