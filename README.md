# DOPE Ballistics

DOPE (Data On Previous Engagements) is an offline-first Android application for lawful sport-shooting range data, calculation, static-target measurement, and completed-string review.

## Milestone status

Milestone 1 implements the locked visual system, edge-to-edge safe-area navigation, reusable Compose components, core screen shells, target-size selection shell and host-side visual goldens. Domain data, calculations and camera ranging remain deliberately unimplemented.

## Build

Requirements: JDK 17 and Android SDK Platform 37 with Build Tools 36.0.0.

```text
./gradlew spotlessCheck
./gradlew lint
./gradlew detekt
./gradlew test
./gradlew :app:validateDebugScreenshotTest
./gradlew assembleDebug
```

The working application ID is `za.co.dope.ballistics`; owner confirmation is required before the first signed public release.

See `docs/DOPE_CODEX_MASTER_BUILD_PROMPT.md` for the authoritative specification.
