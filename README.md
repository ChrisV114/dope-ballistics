# DOPE Ballistics

DOPE (Data On Previous Engagements) is an offline-first Android application for lawful sport-shooting range data, calculation, static-target measurement, and completed-string review.

## Milestone status

Milestone 4 adds a pure Kotlin/JVM deterministic point-mass engine with BRL/JBM G1/G7 drag tables, root-solved zero, reference/current atmosphere comparison, MIL/MOA scope output, uncertainty v1 and calculation traces. Android maps only physically verified profile data into the engine. Built-in DNT and Arken templates remain immutable; owner verification belongs to user-owned profiles. Camera ranging remains deliberately deferred.

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
