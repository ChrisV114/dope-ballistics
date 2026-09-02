# Architecture

## Milestone 7

`:ballistics` remains pure Kotlin/JVM and unchanged. Milestone 7 adds app-layer, informational orientation and local training-video components without connecting the camera/sensor stream to ballistic calculation.

```text
Android app (:app)
  -> Compose profile and environment screens
  -> ProfileRepository, SessionRepository and versioned export codecs
  -> Room database version 9 and migrations 1 -> ... -> 9
  -> Android SensorManager and foreground LocationManager adapters
  -> continuous orientation tracker with rotation-vector and accelerometer/magnetometer paths
  -> optional ARCore capability/Depth adapter with sensor-only fallback
  -> CameraX video recorder, local recording metadata and synchronized sensor CSV
  -> Explicit Open-Meteo adapter behind OPEN_METEO_ENABLED
  -> Pure environmental math, source policy and profile rules
  -> BallisticsInputMapper validation boundary
  -> range-card generator and offline CSV/PDF/PNG renderer
  -> immutable session and verified-range evidence with SHA-256 content hashes
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
