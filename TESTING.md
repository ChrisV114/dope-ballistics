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
- Local instrumentation execution was not claimed because no local emulator or physical device is installed. The protected GitHub Actions `instrumentation` job is the Milestone 2 execution gate.
