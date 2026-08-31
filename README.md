# DOPE Ballistics

DOPE (Data On Previous Engagements) is an offline-first Android application for lawful sport-shooting range data, calculation, static-target measurement, and completed-string review.

## Milestone status

Milestone 2 implements local Room-backed rifle, ammunition, chronograph, scope, zero, reference-atmosphere, saved-range and static-target profiles. Built-in DNT and Arken templates remain immutable and unverified; edits create user-owned copies. Schema-v1 JSON import/export validates SI values and requires an explicit duplicate policy. Ballistic calculations and camera ranging remain deliberately deferred.

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
