# Design System

## Calculation and wind reference panels

The approved original calculation-results and wind-direction panels are layout targets, not loose inspiration. Production screens preserve their information hierarchy and compact field-panel proportions while using real engine state, SI field entry and the shared safe-area-aware navigation shell. Additional editable controls follow below the primary reference layout so they do not displace the field result or overlap phone system controls.

The design authority is docs/design/DOPE_UI_REFERENCE_4K.png, supported by the original board and wordmark reference.

## Milestone 1 implementation

- DopeDesignTokens.kt is the single source for the locked colour, spacing, size and border values.
- Dynamic colour is disabled. Dark, high-contrast and red-light modes use controlled palettes.
- The approved wordmark PNG is rendered as an image asset, not recreated as text.
- The topographic canvas uses #1D4ED8 lines at 0.14/0.25 opacity with a 1 dp stroke.
- Cards, state chips, primary/secondary buttons, fields and result panels share the locked geometry and typography.
- Status chips combine icon, label and colour; colour is never the only status signal.
- Controls use at least a 48 dp touch target; primary controls use at least 52 dp.

## System controls and safe areas

The app draws edge to edge, consumes top/horizontal safe-drawing insets, and gives the bottom navigation exclusive ownership of the Android navigation-bar inset. The bottom container is 72 dp with no inset, reaches 80 dp with a common 24 dp gesture inset, and expands beyond the visual band when a larger three-button navigation inset requires it. Scrollable screen content receives the measured Scaffold bottom padding, so app buttons cannot sit under phone controls.

## Golden coverage

Host-side Compose screenshot baselines cover an S25-equivalent 360 × 780 dp portrait with system UI, compact portrait, 780 × 360 dp landscape, 1.3 font scale, splash, and the target-range preset shell. Intentional baseline changes require explicit visual review.
