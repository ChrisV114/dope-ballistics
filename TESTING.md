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

## Milestone 5 local results — 2026-08-31

- Final combined offline local gate passed: Spotless, Android lint, Detekt, JVM tests, screenshot validation, debug APK assembly and debug instrumentation-test APK assembly.
- JVM tests: 37 run, 0 failures, 0 errors, 0 skipped; 17 are pure `:ballistics` tests and 20 are app tests.
- Wind coverage tests true/magnetic conversion, missing-declination behaviour, clock-relative direction, wind-from cross/head signs, selected speed and min/max/gust brackets.
- Range-card coverage tests confirmed-distance inclusion, unconfirmed-distance rejection, reference/current selection, metric/imperial output, MIL/MOA display, column selection, offline CSV metadata and non-mutating comparison.
- Nine visual goldens passed, including new wind/session portrait baselines and the updated landscape range-card controls. Reviewed content remains scrollable behind a fixed app navigation bar that is clear of the Android system-control inset.
- Nine instrumentation tests compile, including complete `1 -> 2 -> 3 -> 4` migration, append-only session/verified-record hashing and offline CSV/PDF/PNG generation with a content-URI share intent.
- Local instrumentation execution is not claimed because no local emulator or physical device is installed.
- GitHub Actions run `33423772232` passed `build-and-test` in 3m44s and API 35 `instrumentation` in 22m14s on commit `21a01cb`.
- The GitHub emulator executed nine instrumentation tests with 0 skipped and 0 failed, including Room `1 -> 4`, append-only session evidence and offline range-card file export.

## Milestone 5 review-fix local results — 2026-08-31

- The complete local gate passed: Spotless, Android lint, Detekt, all JVM tests, screenshot validation, debug APK assembly and debug instrumentation-test APK assembly.
- JVM tests: 39 run, 0 failures, 0 errors and 0 skipped.
- Ten screenshot goldens passed. The calculation and wind goldens follow the approved original panel hierarchy while retaining scrollable live editors below the primary display; all visible controls remain above the fixed navigation/system-control inset.
- Ten instrumentation tests compile. New coverage validates the complete Room `1 -> 5` migration and explicit active-zero switching while preserving linked profiles, reference atmosphere and current environment.
- Local instrumentation execution is not claimed because no local emulator or physical device is installed.
- GitHub Actions run `33434913641` passed `build-and-test` in 3m31s and API 35 `instrumentation` in 21m58s on commit `601731a`.
- The GitHub emulator executed all ten instrumentation tests with 0 skipped and 0 failed, including Room `1 -> 5` migration and active-zero switching.

## Milestone 5 test-profile review results — 2026-09-01

- The complete local gate passed: Spotless, Android lint, Detekt, all JVM tests, screenshot validation, debug APK assembly and debug instrumentation-test APK assembly.
- JVM tests: 39 run, 0 failures, 0 errors and 0 skipped.
- Ten screenshot goldens passed unchanged.
- Eleven instrumentation tests compile. Updated migration coverage validates Room `1 -> 6`, two rifles, two linked loads, two unverified scope copies and idempotent stable-ID insertion.
- The owner test profiles are visible on the Rifle and Ammunition screens. No zero/reference atmosphere is fabricated.
- Local instrumentation execution is not claimed because no local emulator or physical device is installed. The protected GitHub emulator remains the execution gate.
- GitHub Actions run `33484557392` passed `build-and-test` in 3m33s and API 35 `instrumentation` in 21m47s on commit `d38739c`. The emulator executed all eleven tests with 0 skipped and 0 failed. Two earlier attempts were not counted as passes: one exposed and led to the idempotency fix; the other ran zero tests after Android package/activity services disappeared.

## Milestone 5 starter-profile repair results — 2026-09-01

- The complete local gate passed: Spotless, Android lint, Detekt, 39 JVM tests, ten screenshot validations, debug APK assembly and debug instrumentation-test APK assembly.
- Twelve instrumentation tests compile. New coverage creates a schema-6 database with missing starter rows, migrates it through `6 -> 7`, and confirms that both rifles, both linked loads and both unverified scope copies are restored.
- The migration uses stable IDs and conflict-ignore insertion, so it does not replace matching records or create a zero/reference atmosphere.
- Local instrumentation execution is not claimed because no local emulator or physical device is installed. The protected GitHub API 35 emulator job remains the execution gate.

## Milestone 5 calculator-usability review results — 2026-09-01

- The complete local gate passed: Spotless, Android lint, Detekt, all JVM tests, screenshot validation, debug APK assembly and debug instrumentation-test APK assembly.
- JVM tests: 42 run, 0 failures, 0 errors and 0 skipped; 17 are pure `:ballistics` tests and 25 are app tests.
- Eleven screenshot goldens passed. The S25-equivalent calculation golden keeps direct range, fire direction, wind direction and wind speed entry together with the complete original result hierarchy above the fixed bottom-navigation/system-control region.
- Twelve instrumentation tests compile. Migration coverage now validates Room `1 -> 8` and `6 -> 8`, including rifle-owned zero-distance/sight-height defaults and preservation of explicit estimated-reference provenance.
- Local instrumentation execution is not claimed because no local emulator or physical device is installed. The protected GitHub API 35 emulator job remains the execution gate.
- The build retains the known experimental screenshot-testing, Gradle 10 deprecation and unstripped debug native-library warnings.
- GitHub Actions run `33509481413` passed `build-and-test` in 4m10s and API 35 `instrumentation` in 21m27s on commit `62dbc57`.
- The GitHub emulator started and finished all twelve instrumentation tests with 0 skipped and 0 failed.

## Milestone 5 usability round-two local results — 2026-09-01

- The complete local gate passed: Spotless, Android lint, Detekt, all JVM tests, screenshot validation, signed debug APK assembly and debug instrumentation-test APK assembly.
- JVM tests: 43 run, 0 failures, 0 errors and 0 skipped; focused coverage confirms blank-safe direct input and explicit left/right wind-click wording.
- Eleven screenshot goldens passed after intentionally updating the calculation and session references. The calculation golden replaces the non-interactive elevation gauge with the shared wind wheel while retaining the original result-card hierarchy and bottom system-control clearance.
- Room schema 9 is exported. Instrumentation migration coverage compiles for `1 -> 9` and `6 -> 9`, including preserved starter profiles, rifle-owned zero geometry, nullable owner-photo URIs and nullable cartridge overall length.
- `apkanalyzer` confirmed application ID `za.co.bdstudio.dope`, version code `10` and version name `0.5.5-m5-review`.
- `apksigner` confirmed the APK uses the approved private review certificate SHA-256 `5AD535D1D96C0A79A6D77BE223BCB6D659CCE53180C3910C48DD456D664232A3`.
- Local emulator and physical Galaxy S25 execution are not claimed. The protected GitHub API 35 emulator job remains the external execution gate.
- The build retains the known experimental screenshot-testing, Gradle 10 deprecation and unstripped debug native-library warnings.
- GitHub Actions run `33538503906` passed `build-and-test` in 3m54s and API 35 `instrumentation` in 17m14s on commit `5fae2c6`. The signed review APK artifact was downloaded and independently checked for package, version and certificate identity.

## Milestone 5 previous-DOPE review results — 2026-09-01

- The calculation layout and existing result hierarchy remain unchanged. A read-only Previous DOPE card now appears immediately after Wind correction.
- Matching is deliberately strict: the newest `VERIFIED` manual record must use the active zero/setup identifier and match the entered distance to within 0.01 m. Other setups, distances and non-verified records are excluded.
- The full local gate passed: Spotless, Android lint, Detekt, all JVM tests, screenshot validation, signed debug APK assembly and debug instrumentation-test APK assembly.
- JVM tests: 45 run, 0 failures, 0 errors and 0 skipped; 17 are pure `:ballistics` tests and 28 are app tests. Two focused tests cover newest-record selection and rejection of wrong-distance, wrong-setup and unverified records.
- Eleven existing screenshot goldens passed unchanged; the added card is below the retained result hierarchy and does not change the locked calculation reference viewport.
- `apkanalyzer` confirmed application ID `za.co.bdstudio.dope`, version code `11` and version name `0.5.6-m5-review`.
- `apksigner` confirmed the approved private review certificate SHA-256 `5AD535D1D96C0A79A6D77BE223BCB6D659CCE53180C3910C48DD456D664232A3`.
- Local emulator and physical Galaxy S25 execution are not claimed. The protected GitHub API 35 emulator job remains the external execution gate.
- GitHub Actions run `33546118928` passed `build-and-test` in 3m58s on commit `8a4ee73`. Its first API 35 emulator attempt crashed before discovering any tests; the unchanged failed-job retry passed all twelve instrumentation tests with 0 skipped and 0 failed in 19m43s.
