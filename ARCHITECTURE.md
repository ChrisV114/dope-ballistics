# Architecture

## Milestone 2

Only `:app` exists because the current profile/database implementation remains cohesive and does not yet justify empty feature modules. Domain rules are pure Kotlin, Room persistence is isolated under `data/db`, import/export orchestration is under `data`, and Compose consumes a repository rather than SQL APIs.

```text
Android app (:app)
  -> Compose profile screens
  -> ProfileRepository and schema-v1 import/export
  -> Room database version 2 and migration 1 -> 2
  -> Pure profile, verification, chronograph and BDC rules
  -> locked design tokens and reusable components
  -> Android platform
```

Room stores physical quantities in SI units and UUIDs as strings. Mutable records carry creation/modification timestamps, revision, archive and favourite state. Referenced records are archived instead of silently hard-deleted. Built-in scope families and variants are immutable rows; customised scopes are separate user-owned profiles.

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
