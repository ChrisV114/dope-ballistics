# Architecture

## Milestone 4

The first specialised module is now implemented. `:ballistics` is pure Kotlin/JVM and contains stable input/output models, authoritative G1/G7 tables, the point-mass solver, uncertainty and deterministic trace. It has no Android imports. `:app` retains Room, Android adapters and UI, and maps verified persisted records into engine inputs.

```text
Android app (:app)
  -> Compose profile and environment screens
  -> ProfileRepository, weather cache and schema-v1 profile import/export
  -> Room database version 3 and migrations 1 -> 2 -> 3
  -> Android SensorManager and foreground LocationManager adapters
  -> Explicit Open-Meteo adapter behind OPEN_METEO_ENABLED
  -> Pure environmental math, source policy and profile rules
  -> BallisticsInputMapper validation boundary
  -> Pure Kotlin/JVM ballistics engine (:ballistics)
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
ballistics -> Kotlin/JVM standard library only
vision -> deterministic baseline plus optional signed model adapter
```

The module inventory and constraints in the master prompt remain authoritative. Modules will be added when their milestone contains real implementation.
