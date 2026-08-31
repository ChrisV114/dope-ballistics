# Dependency Versions

Selected through 2026-08-31 from stable upstream releases.

| Component | Version | Purpose |
|---|---:|---|
| Android Gradle Plugin | 9.3.2 | Android build; verified in Google Maven metadata and resolved through its explicit module mapping |
| Gradle wrapper | 9.7.1 | Current stable build runtime; compatible with AGP 9.3 |
| JDK | 17 | AGP toolchain |
| compileSdk / targetSdk | 37 | Current stable Android API |
| minSdk | 28 | Product minimum |
| Kotlin / Compose compiler plugin | 2.4.10 | Kotlin and Compose compilation |
| Compose BOM | 2026.08.00 | Compose dependency alignment |
| Activity Compose | 1.13.0 | Compose activity integration |
| Navigation Compose | 2.10.0 | Stable Compose navigation |
| Room | 2.8.4 | Stable Android Room runtime, KTX, compiler and migration testing |
| KSP | 2.3.10 | KSP2 code generation compatible with AGP 9 and Kotlin 2.4 source projects |
| kotlinx.serialization JSON | 1.11.0 | Strict profile import/export schema-v1 JSON |
| Compose Screenshot Testing | 0.0.1-alpha16 | Required host-side golden testing; documented exception because Android's compatible tool remains experimental |
| AndroidX Test Core | 1.7.0 | Host-side Android test support |
| AndroidX Test Ext JUnit | 1.3.0 | Instrumentation JUnit integration |
| AndroidX Test Runner | 1.7.0 | Instrumentation runner |
| JUnit 4 | 4.13.2 | JVM unit tests |
| Detekt | 1.23.8 | Kotlin static analysis |
| Spotless | 8.10.1 | Formatting gate |
| ktlint | 1.8.0 | Kotlin formatter |

Milestone 3 adds no third-party runtime dependency. Sensors, foreground location and HTTPS use Android/JDK platform APIs; JSON continues to use the pinned kotlinx.serialization runtime. The production weather seam uses Open-Meteo's documented current `surface_pressure`, `pressure_msl`, temperature, humidity and wind fields and requires no API key.

All versions are exact pins. The screenshot plugin is the sole pre-release exception: the master prompt requires golden tests and Android's AGP 9-compatible Compose screenshot tool is still alpha. It is isolated to screenshot-test build logic and does not ship in the APK. Future required stack dependencies will be added only with their implementing milestone and recorded here.
