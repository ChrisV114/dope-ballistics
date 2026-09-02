# Implementation Plan

## Current milestone

Milestone 7 — AR orientation and training video.

- [x] Add optional runtime ARCore support and an explicit sensor-only fallback.
- [x] Report optional Depth support without treating Depth as a long-range rangefinder.
- [x] Add rotation-vector orientation with accelerometer-plus-magnetometer fallback.
- [x] Show horizon, magnetic heading, inclination, roll, stability, confidence and recording state.
- [x] Keep all aiming points, target tracking, impact markers, lead indicators and corrected reticles out of the live view.
- [x] Add user-started CameraX training video with audio off by default and point-of-use microphone permission.
- [x] Keep a bounded local training-recording history and synchronize playback to saved sensor samples.
- [x] Export original video through Android sharing and a location-free Sensor CSV.
- [x] Add deterministic unit, instrumentation and phone-sized screenshot coverage.
- [ ] Add burned-in informational-overlay MP4 export after codec validation.
- [ ] Complete Samsung Galaxy S25 Ultra physical-device acceptance.
- [x] Run and record the complete local quality/build gate for `0.7.0-m7-review`.
- [x] Run and record protected GitHub quality and emulator jobs for `0.7.0-m7-review`.

Milestone 7 remains in review until the two pending deliverable/acceptance items and both complete gates pass. Match plans, Watch7 shot progression, full-screen fixed-camera sequencing and pistol-drill cues remain future milestones.
