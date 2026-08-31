# Calculation Methods

All calculation boundaries use SI units and preserve full precision until a user-facing scope setting is rounded. Environmental formulas and provenance are documented in `ENVIRONMENTAL_CALCULATIONS.md`; trajectory methods and constants are documented in `BALLISTICS_ENGINE.md`.

## Angular output

```text
MIL = radians * 1000
MOA = radians * 180 / pi * 60
clicks = round(rawAngularValue / clickValue)
rounded = clicks * clickValue
residual = rawAngularValue - rounded
```

Signed raw and rounded values, click count, residual, travel status and configured revolutions are retained. BDC marks remain blocked unless a separate user-verified calibration exists.

## Uncertainty v1

The first uncertainty method is deterministic one-at-a-time sensitivity analysis. Each supplied positive one-sigma input uncertainty is perturbed once, the trajectory is recalculated, and its angular difference from the raw result becomes that input's contribution. Contributions are sorted from largest to smallest and combined by root-sum-square. Supported inputs are distance (including confirmed camera uncertainty), muzzle velocity, BC, station pressure, temperature, humidity, inclination, headwind and crosswind.

This is a local sensitivity estimate, not a confidence guarantee. Correlation and nonlinear two-sided effects are not yet modelled; a seeded Monte Carlo method may be added after independent validation.
