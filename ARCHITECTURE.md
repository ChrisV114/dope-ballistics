# Architecture

## Milestone 3

Only `:app` exists because the implemented profile and environment slices remain cohesive and do not justify empty modules. Environmental equations/source selection are pure Kotlin. Android sensors, one-shot location and network providers implement interfaces under `data/environment`; Compose does not call platform services through domain code.

```text
Android app (:app)
  -> Compose profile and environment screens
  -> ProfileRepository, weather cache and schema-v1 profile import/export
  -> Room database version 3 and migrations 1 -> 2 -> 3
  -> Android SensorManager and foreground LocationManager adapters
  -> Explicit Open-Meteo adapter behind OPEN_METEO_ENABLED
  -> Pure environmental math, source policy and profile rules
  -> locked design tokens and reusable components
  -> Android platform
```

Room stores physical quantities in SI units and UUIDs as strings. Environmental snapshots retain per-field provenance, quality and timestamps plus derived values. Weather cache entries are coordinate-bucketed and timestamped; stale cached data is explicitly labelled. Precise coordinates are stored in a snapshot only when its opt-in checkbox is selected.

## Planned dependency direction

```text
feature modules -> domain -> core:model/core:common
feature modules -> specialised core adapters
app -> feature modules and core:designsystem
core adapters -> domain interfaces
ballistics -> pure Kotlin/JVM only
vision -> deterministic baseline plus optional signed model adapter
```

The module inventory and constraints in the master prompt remain authoritative. Modules will be added when their milestone contains real implementation.
