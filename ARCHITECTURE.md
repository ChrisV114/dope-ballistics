# Architecture

## Milestone 1

Only :app exists because the current implementation is a cohesive design/navigation shell and no domain implementation exists yet. This avoids empty modules while preserving the planned boundaries.

```text
Android app (:app)
  -> Compose navigation and screen shells
  -> locked design tokens and reusable components
  -> Android platform
```

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
