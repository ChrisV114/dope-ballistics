# DOPE Ballistics

DOPE (Data On Previous Engagements) is an offline-first Android application for lawful sport-shooting range data, calculation, static-target measurement, and completed-string review.

## Milestone status

Milestone 5 adds a manual wind wheel with explicit wind-from/true/magnetic conventions, selected and bracketed wind, offline range cards with PDF/CSV/PNG sharing, immutable calculation sessions, verified range observations and non-destructive comparison. Confirmed saved-target distances are added to range cards automatically. Profile truing, match planning, Watch7, and camera ranging remain deliberately deferred.

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
