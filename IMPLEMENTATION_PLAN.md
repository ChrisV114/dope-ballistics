# Implementation Plan

## Current milestone

Milestone 6 — camera capability and calibration review.

- [x] Record owner acceptance of the final Milestone 5 review build.
- [x] Add stable CameraX preview and still capture with runtime camera permission.
- [x] Inspect rear-camera IDs, focal lengths, sensor/active-array dimensions, JPEG sizes, zoom range, OIS, distortion metadata and logical physical IDs where Android exposes them.
- [x] Select an explicit CameraX camera ID and request a 1.0× zoom ratio without marketing-zoom assumptions.
- [x] Capture an app-private still and provide draggable horizontal calibration anchors.
- [x] Fit and persist multi-distance calibration evidence with mean, median and 95th-percentile errors and a valid distance range.
- [x] Warn when camera ID, capture resolution, zoom or device configuration differs from the saved calibration.
- [x] Add a Galaxy S25 landscape physical-acceptance checklist and a deterministic phone golden.
- [x] Run the complete local quality/build gate for `0.6.0-m6-review`.
- [x] Run the protected GitHub quality and emulator jobs for `0.6.0-m6-review`.
- [ ] Complete physical Galaxy S25 camera/lens/resolution/zoom acceptance.

Camera-derived target-distance calculation and automatic ballistic handoff are intentionally not implemented. Target distance remains a separately confirmed manual measurement. Milestone 7 AR/training-video work, match plans, Watch7 shot progression, fixed-camera sequencing and pistol-drill cues remain future work.
