# Implementation Plan

## Current milestone

Milestone 6 — camera capability and calibration review.

- [x] Record owner acceptance of the final Milestone 5 review build.
- [x] Add stable CameraX preview and still capture with runtime camera permission.
- [x] Inspect rear-camera IDs, focal lengths, sensor/active-array dimensions, JPEG sizes, zoom range, OIS, distortion metadata and logical physical IDs where Android exposes them.
- [x] Select an explicit CameraX camera ID and request a 1.0× zoom ratio without marketing-zoom assumptions.
- [x] Add runtime-reported zoom control with 1×/3×/5×/10×/Max shortcuts and invalidate only unsaved camera evidence when zoom changes.
- [x] Capture an app-private still and provide draggable horizontal calibration anchors.
- [x] Fit and persist multi-distance calibration evidence with mean, median and 95th-percentile errors and a valid distance range.
- [x] Warn when camera ID, capture resolution, zoom or device configuration differs from the saved calibration.
- [x] Add a Galaxy S25 landscape physical-acceptance checklist and a deterministic phone golden.
- [x] Run the complete local quality/build gate for `0.6.0-m6-review`.
- [x] Run the protected GitHub quality and emulator jobs for `0.6.0-m6-review`.
- [x] Run the complete local quality/build gate for the S25 Ultra zoom revision `0.6.1-m6-review`.
- [x] Run the protected GitHub quality and emulator jobs for `0.6.1-m6-review`.
- [x] Record owner physical confirmation that CameraX zoom works through the reported 10× maximum.
- [x] Complete owner physical Galaxy S25 Ultra camera/lens/resolution/zoom acceptance, including the observed 10× CameraX maximum.
- [x] Remove calibration from Target Range and retain it only as clearly labelled advanced camera diagnostics that do not produce a range.
- [x] Run the complete local gate for the final `0.6.2-m6-review` cleanup.
- [x] Run the protected GitHub quality and emulator jobs for `0.6.2-m6-review`.

Camera-derived target-distance calculation and automatic ballistic handoff are intentionally not implemented. Target distance remains a separately confirmed manual or laser measurement. Milestone 7 AR/training-video work, match plans, Watch7 shot progression, full-screen fixed-camera sequencing and pistol-drill cues remain future work.
