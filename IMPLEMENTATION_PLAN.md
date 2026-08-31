# Implementation Plan

## Current milestone

Milestone 4 — Ballistics engine.

- [x] Add a pure Kotlin/JVM `:ballistics` module with stable solver interfaces and no Android imports.
- [x] Add provenance-labelled BRL/JBM G1 and G7 Cd-versus-Mach tables with interpolation.
- [x] Add air-relative drag, gravity, wind, inclination, sight height and line-of-sight geometry.
- [x] Root-solve the bore angle for the verified zero under the reference atmosphere.
- [x] Add reference/current comparison and explicit environmental/inclination contributions.
- [x] Add time of flight, velocity, energy, Mach and flight-state output.
- [x] Add MIL/MOA clicks, rounding residual, travel and revolution checks.
- [x] Add sensitivity uncertainty v1 and deterministic versioned traces.
- [x] Add the verified-profile mapping boundary and blocked invalid/unverified inputs.
- [x] Add deterministic fixtures, drag-table values, convergence and invalid-input tests.
- [x] Run the complete local quality/build gate.
- [x] Run the protected GitHub quality and emulator jobs.
- [x] Stop for owner review.

Milestone 5 — wind UI, engagement-level multi-rifle match plans, full immutable DOPE log, range-card presentation/export, sessions and explicit reversible truing — remains blocked until Milestone 4 review. It must capture actual settings per target and improve only through accepted equipment-specific true profiles with before/after validation. The Watch7 companion and automatic shot-count progression remain future work, governed by `MATCH_PLAN.md`; camera ranging remains Milestone 6, and fixed-camera sequencing/pistol drill cues remain later requirements.
