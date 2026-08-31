# Testing

Milestone 0 gates:

```text
./gradlew spotlessCheck
./gradlew lint
./gradlew detekt
./gradlew test
./gradlew assembleDebug
```

CI also defines a separate emulator instrumentation job. Feature-specific unit, safety, instrumentation and golden tests are added with their milestones. A command is recorded as passed only when actually executed.


## Milestone 0 results — 2026-08-30

- `spotlessCheck`: passed.
- `lint`: passed with zero lint findings.
- `detekt`: passed with the app production and test sources explicitly included.
- `test`: passed; 1 test, 0 failures, 0 errors, 0 skipped.
- `assembleDebug`: passed.
- Final combined gate set also passed with `--offline`, proving the dependency locks and local cache are sufficient.

Instrumentation and physical-device tests were not run because Milestone 0 adds no device feature and no emulator/device acceptance is claimed.

## Milestone 1 results — 2026-08-30

- Final combined offline gate passed: Spotless, lint, Detekt, JVM tests, screenshot validation, debug APK assembly and debug instrumentation-test APK assembly.
- JVM tests: 4 run, 0 failures, 0 errors, 0 skipped.
- Visual goldens: 6 generated and validated for S25-equivalent portrait/system UI, compact portrait, landscape, 1.3 font scale, splash and target-range choices.
- Accessibility basics covered by 48/52 dp component minimums, semantic labels/headings, icon-plus-text statuses, a 1.3 font-scale golden and navigation instrumentation assertions.
- Safe-area unit coverage proves 72/80 dp common behavior and expansion to 104 dp for a 48 dp three-button inset.
- The instrumentation test APK compiled successfully.

The instrumentation test was not executed locally because no emulator image or physical device is installed. The separate GitHub Actions emulator job remains the execution gate. No Samsung Galaxy S25 physical-device acceptance is claimed.

GitHub Actions run #2 subsequently passed both `build-and-test` and `instrumentation` for Milestone 1 on commit `488175e`.

## Milestone 2 results — 2026-08-31

- Final combined offline local gate passed: Spotless, lint, Detekt, JVM tests, screenshot validation, debug APK assembly and debug instrumentation-test APK assembly.
- JVM tests: 11 run, 0 failures, 0 errors, 0 skipped.
- Visual goldens: 6 validated; the reviewed Profiles large-font baseline was updated for persisted-count wording and physical-verification guidance.
- Unit coverage includes chronograph statistics, scope verification invalidation, KLBOX BDC blocking, target-class safety, schema-v1 JSON round-trip, unconfirmed-distance blocking and duplicate import UUID remapping.
- Instrumentation coverage adds Room CRUD/archive behavior, complete `1 -> 2` migration validation, immutable built-in template seeding and existing navigation/accessibility checks.
- Local instrumentation execution was not claimed because no local emulator or physical device is installed. No Samsung Galaxy S25 physical-device acceptance is claimed.
- GitHub Actions run `33371064157` passed `build-and-test` in 4m55s and API 35 `instrumentation` in 14m19s on commit `c1f57b4`.
- The GitHub emulator completed 3 instrumentation tests with 0 skipped and 0 failed, covering Room CRUD/archive, the complete `1 -> 2` migration with built-in scope templates, and navigation/accessibility behavior.

## Post-Milestone-2 owner-review verification — 2026-08-31

- The complete offline local gate passed after changing the application ID to `za.co.bdstudio.dope`: Spotless, lint, Detekt, JVM tests, screenshot validation, debug APK assembly and debug instrumentation-test APK assembly.
- JVM tests: 11 run, 0 failures, 0 errors, 0 skipped; the foundation test asserts the approved application ID.
- Six screenshot goldens remain unchanged and passed validation.
- `apkanalyzer` confirmed that the packaged debug APK application ID is `za.co.bdstudio.dope`.
- No local emulator or physical-device execution is claimed; the protected GitHub API 35 emulator job remains the external execution gate for this review update.

## Milestone 3 results — 2026-08-31

- Final combined offline local gate passed: Spotless, lint, Detekt, 16 JVM tests, seven screenshot validations, debug APK assembly and debug instrumentation-test APK assembly.
- Environmental JVM coverage includes source priority/staleness, Buck vapour pressure, moist-air density, dew point, pressure altitude, numerical density altitude, pressure-sample stability and strict separation of Open-Meteo surface and mean-sea-level pressure.
- Seven instrumentation tests compile for manual/offline calculation, permission-denied location, optional sensor diagnostics, environment/cache persistence, Room CRUD/archive, complete `1 -> 2 -> 3` migration and navigation/accessibility.
- The environment golden uses a 360 × 780 dp S25-equivalent viewport with system UI and confirms the scrollable controls remain above the app bottom navigation/system-control region.
- Local instrumentation execution is not claimed because no emulator or physical device is installed. Physical Galaxy S25 sensor acceptance remains pending.
- GitHub Actions run `33386061965` passed `build-and-test` in 2m47s and API 35 `instrumentation` in 21m02s on commit `18b426e`.
- The GitHub emulator executed seven instrumentation tests with 0 skipped and 0 failed.

## Milestone 4 results — 2026-08-31

- Final combined local gate passed: Spotless, lint, Detekt, JVM tests, screenshot validation, debug APK assembly and debug instrumentation-test APK assembly.
- JVM tests: 27 run, 0 failures, 0 errors, 0 skipped; 11 are pure `:ballistics` tests and 16 are app tests.
- Ballistics coverage includes exact BRL/JBM table values and interpolation, deterministic owner rifle/load fixtures, root-solved zero, RK4 step-halving convergence, G1/G7 paths, reference/current deviation, wind sign, MIL/MOA clicks/residual/travel/revolutions, uncertainty ordering, range ordering and invalid-input blocking.
- The standard-atmosphere 6.5 Creedmoor fixture predicts 3.497, 7.716 and 11.503 MIL at 500, 800 and 1,000 m; all remain within 0.35 MIL of the owner's independent observed 3.6, 8.0 and 11.5 MIL settings without solver truing.
- Seven existing screenshot goldens passed unchanged, preserving the reviewed phone system-control clearance.
- Seven existing instrumentation tests compile into `app-debug-androidTest.apk`; no database schema or device behaviour changed in this milestone.
- Local instrumentation execution is not claimed because no emulator or physical device is installed.
- GitHub Actions run `33405639742` passed `build-and-test` in 1m32s and API 35 `instrumentation` in 19m17s on commit `96f9ae0`.
- The GitHub emulator executed seven instrumentation tests with 0 skipped and 0 failed.
