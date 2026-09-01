# Implementation Plan

## Current milestone

Milestone 5 — wind, range cards and sessions.

- [x] Implement a manual wind wheel with drag/tap, clock, numeric and fine-adjust controls.
- [x] Define and test true/magnetic, wind-from, relative, headwind and crosswind signs.
- [x] Capture min/average/max/gust values, selected wind and a calculation bracket.
- [x] Generate range cards from verified profiles and confirmed saved-target distances.
- [x] Export and share range cards offline as CSV, PDF and PNG.
- [x] Add immutable complete session snapshots and append-only verified range records.
- [x] Keep calculated and actual settings separate with explicit confidence/status.
- [x] Add comparison/what-if without mutating persisted profiles.
- [x] Add Room `3 -> 4`, export, convention, immutability and screenshot coverage.
- [x] Run the complete local quality/build gate.
- [x] Run the protected GitHub quality and emulator jobs.
- [x] Complete initial owner review of controls, wind and range-card layout.
- [x] Fix the fresh-install profile/zero path and calculation-screen blockers found during review.
- [x] Rebuild and run the protected GitHub quality and emulator jobs for `0.5.1-m5-review`.
- [x] Add the owner-requested editable test profiles, visible saved-profile lists and an idempotent Room `5 -> 6` data migration.
- [x] Run the protected GitHub quality and emulator jobs for `0.5.2-m5-review`.
- [x] Add a non-destructive `6 -> 7` repair migration for an installed review database missing starter profiles.
- [ ] Stop for owner review of the review-fix APK.

Milestone 6 camera ranging and calibration remains blocked until Milestone 5 review. Guided profile truing is a later optional workflow under the master prompt and `DOPE_LOG.md`; engagement-level match plans, Watch7 shot progression, fixed-camera sequencing and pistol-drill cues remain separately governed future work.
