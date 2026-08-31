# Ballistics Engine

## Milestone 4 method v1

`:ballistics` is a pure Kotlin/JVM module with no Android imports. Its stable entry points are `BallisticsEngine.solve(TrajectoryInput)` and `BallisticsEngine.rangeCard(RangeCardInput)`. Android profile/database records are converted at the app boundary by `BallisticsInputMapper`; missing or unverified critical inputs return issues instead of a confident calculation.

### Reference drag data

G1 and G7 use the US Army Ballistics Research Laboratory standard-projectile drag-coefficient tables published by JBM Ballistics. The embedded values are Cd versus Mach from 0.0 through 5.0 and are linearly interpolated between adjacent points. The trace records the source, selected model and retrieval date. The engine requires the bullet profile's manufacturer-declared coefficient for the selected model and never converts, estimates or silently substitutes G1/G7.

Primary provenance:

- <https://jbmballistics.com/downloads.html>
- <https://jbmballistics.com/downloads/mcg1.txt>
- <https://jbmballistics.com/downloads/mcg7.txt>

### Point-mass model

The solver uses SI units internally and fixed-step fourth-order Runge-Kutta integration. Drag is evaluated from air-relative velocity, including along-line head/tail wind and crosswind. Gravity is resolved into the inclined line-of-sight frame. Moist-air density uses station pressure, temperature and relative humidity; speed of sound uses the documented temperature-only method shared with Milestone 3.

For a ballistic coefficient expressed in lb/in² against its selected reference projectile:

```text
dragScale = rho * pi * Cd(Mach) * airSpeed / (8 * 703.06957964 * BC)
dragAccelerationVector = -dragScale * airRelativeVelocityVector
```

The bore angle is not approximated. A bracketed bisection root solver finds the angle that intersects the line of sight at the verified zero distance under the reference atmosphere. Reference and current trajectories then reuse that physical bore angle. The output separates reference elevation, current elevation, environmental deviation, inclination contribution and windage.

### Output and trace

Every confident result includes time of flight, remaining velocity, kinetic energy, Mach, flight state, offsets, raw elevation/windage, rounded MIL or MOA, signed clicks, residual, configured-travel status, optional revolutions, sensitivity uncertainty and a versioned deterministic trace. Spin drift, Coriolis, aerodynamic jump and BDC holds are not part of this validated core.

Default integration step is 0.001 s. Accepted steps are 0.0001–0.01 s. Calculation range is input-bounded and defaults to 3,000 m. Initial velocity must remain within the table's Mach 0–5 validation domain.

### Owner fixtures

The regression suite includes the owner-provided, editable load values:

- Howa 6.5 Creedmoor, 26-inch barrel, 1:8 twist, DNT TheOne, 100 m zero, 6 cm sight height, Lapua 139 gr Scenar GB458, G7 0.290, 809 m/s and 0.1 MIL clicks.
- Smith & Wesson M&P15 Sport III .223 Remington, 16-inch barrel, 1:8 twist, Arken EP-8, 50 m zero, 6 cm sight height, Hornady 53 gr V-MAX, G1 0.290, 920 m/s and 0.25 MOA clicks.

These are private user profiles and test fixtures, not immutable factory defaults. Muzzle velocity remains editable load data. Twist direction remains unspecified and is not used by the point-mass core.
