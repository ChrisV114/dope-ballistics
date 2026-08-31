# DOPE Ballistics

DOPE (Data On Previous Engagements) is an offline-first Android application for lawful sport-shooting range data, calculation, static-target measurement, and completed-string review.

## Milestone status

Milestone 3 adds runtime sensor diagnostics, barometer/orientation capture, one-shot foreground location, complete manual/offline environmental entry, atmospheric calculations, timestamped snapshots and an explicit user-triggered Open-Meteo adapter with cache/staleness labels. Built-in DNT and Arken templates remain immutable and unverified. Ballistic trajectory calculations and camera ranging remain deliberately deferred.

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

The owner-approved application ID is `za.co.bdstudio.dope`. Initial distribution is private sideloading; release signing and public distribution remain deferred.

See `docs/DOPE_CODEX_MASTER_BUILD_PROMPT.md` for the authoritative specification.
