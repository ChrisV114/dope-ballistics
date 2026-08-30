# DOPE — Complete Codex Master Build Prompt

## 0. Instruction to Codex

You are the principal Android architect and implementation engineer for **DOPE**.

Build the application described in this document as a production-quality, offline-first Android project. Treat this document and the supplied approved visual reference as the single source of truth.

Do not attempt to implement the entire application in one uncontrolled pass. Work milestone by milestone, keep the project compiling, add tests from the beginning, and stop for review at each milestone gate.

When a physical specification is unknown or can vary by product version, do not guess. Store it as unverified, expose an editable field, and prevent a falsely confident result until the user confirms it.

When an assumption is not safety-critical and does not block implementation, document it in `ASSUMPTIONS.md` and proceed. When a missing decision would change data integrity, calculation validity, privacy, package identity, or a non-reversible release decision, record it in `DECISIONS_REQUIRED.md` and implement a safe placeholder or feature flag.

Do not silently change the approved branding, screen layout, palette, logo treatment, navigation pattern, product name, scope templates, calculation conventions, or safety boundaries.

---

# 1. Product identity

Application name:

**DOPE**

Meaning:

**Data On Previous Engagements**

Full product name:

**DOPE Ballistics**

Subtitle:

**Ballistic Data & Environmental Calculator**

Working Android application ID:

```text
za.co.dope.ballistics
```

Before the first signed public release, flag the application ID for owner confirmation because an Android application ID is a long-lived release decision.

Primary target device:

**Samsung Galaxy S25**

The app must use runtime capability detection rather than assuming that every Galaxy S25 regional variant, firmware version, or other Android phone exposes identical cameras, sensors, AR features, or Bluetooth behaviour.

Primary locale:

```text
English (South Africa)
```

Default units:

```text
Metric
```

Core purpose:

- Manage rifle, ammunition, zero, scope, target, range, environmental and session profiles.
- Collect available environmental and orientation data from the Android device.
- Retrieve missing current weather values from an optional internet provider.
- Accept manual wind speed and wind-from direction.
- Calculate reference and current trajectories.
- Display environmental deviation and final angular scope settings in MIL or MOA.
- Convert angular results to valid turret clicks.
- Generate range cards and retain verified range data.
- Estimate distance to a known, static paper or steel range target from a calibrated photograph.
- Provide an AR orientation and training-video overlay for heading, horizon, roll, inclination and sensor confidence.
- Provide a Bluetooth-speaking, post-string **DOPE Range Analyst** for target scoring and completed-string review.
- Let the user correct Range Analyst detections and export those corrections to teach and improve a future on-device model.

The core app must work without an account and without internet connectivity.

---

# 2. Non-negotiable operating and safety boundary

The application is for lawful sport-shooting range use, data recording, static target measurement, trajectory study and post-string review.

The app may:

- Calculate a ballistic solution from explicitly selected profiles and entered or measured data.
- Display a pre-session or manually requested scope result in MIL or MOA.
- Display valid click counts for the selected, user-verified scope.
- Show a static range card.
- Record environmental conditions and wind observations.
- Measure device heading, roll and inclination.
- Measure a known static range target from a photograph.
- Analyse a completed string after the user has explicitly ended it.
- Speak completed-string statistics and raw observations over Bluetooth.
- Compare completed strings historically.
- Show raw linear target offsets in millimetres or inches after user confirmation.

The app must not implement:

- Automatic recognition of people, animals or vehicles.
- Live target tracking.
- Automatic target selection.
- A live corrected point of aim.
- A live ballistic impact marker.
- A lead indicator.
- A live camera reticle corrected for elevation or wind.
- Automatic trigger or weapon control.
- “Fire now” instructions.
- Shot-by-shot spoken scope corrections.
- Spoken instructions such as “two clicks down”, “dial left”, “hold high” or equivalent directions derived from observed impacts.
- Automatic conversion of an observed impact into a spoken aiming instruction while a string is active.
- Live AI analysis of impacts during `STRING_ACTIVE`.

The standard calculator and the post-string Range Analyst must be separate subsystems.

During `STRING_ACTIVE`, the Range Analyst may log time, shot count entered by the user, environmental observations and wind observations. It may not analyse impacts, calculate impact-derived angular corrections, display an impact-derived point of aim, or speak corrective instructions.

Do not weaken or bypass these restrictions during implementation.

---

# 3. Approved visual design lock

The supplied reference image is the approved design authority:

```text
docs/design/DOPE_UI_REFERENCE_4K.png
```

Also retain:

```text
docs/design/DOPE_UI_REFERENCE_ORIGINAL.png
docs/design/DOPE_WORDMARK_REFERENCE.png
```

The reference establishes and locks:

- The DOPE wordmark treatment.
- Dark navy and near-black interface.
- Topographic contour-line background.
- Electric-blue primary controls.
- Light-blue information accents.
- Lime-green environmental and confirmation accents.
- Green verified states.
- High-contrast raised cards.
- White primary typography.
- Condensed technical visual character.
- Rounded cards and controls.
- Compact information-dense screen composition.
- Bottom navigation.
- The overall hierarchy and spacing shown on the approved board.

Do not redesign the app into a generic Material demo. Use Material 3 as the component foundation, but override it with the locked DOPE design system.

Disable Android dynamic colour by default. The approved palette must not change based on wallpaper.

## 3.1 Locked design tokens

Create a single source of truth in `DopeDesignTokens.kt`.

Use these exact base colours:

```text
BackgroundDeep       #0B1220
BackgroundBlackBlue  #08111E
SurfaceBase          #12182A
SurfaceRaised        #1E293B
SurfaceStrong        #2E3B4E
BorderSlate          #334155
TextPrimary          #F8FAFC
TextSecondary        #CBD5E1
TextMuted            #94A3B8
PrimaryBlue          #2563EB
PrimaryBlueBright    #60A5FA
AccentLime           #A3E635
SuccessGreen         #22C55E
WarningAmber         #F59E0B
ErrorRed             #EF4444
InfoCyan             #38BDF8
```

Topographic contour lines:

```text
Line colour: #1D4ED8
Normal opacity: 0.14
Highlighted opacity: 0.25
Stroke: 1 dp or less
```

Minimum contrast must remain accessible. Do not communicate status by colour alone.

## 3.2 Shape and spacing tokens

```text
Screen horizontal padding: 16 dp
Card corner radius: 16 dp
Input/control radius: 12 dp
Small chip radius: 8 dp
Card border: 1 dp
Minimum touch target: 48 dp
Primary button minimum height: 52 dp
Bottom navigation height: 72–80 dp including system inset
Grid baseline: 4 dp
```

Use edge-to-edge layout with correct status-bar and navigation-bar insets.

## 3.3 Typography

Use an Android-packaged or system-available condensed sans-serif for UI headings and numeric readouts. Do not depend on downloading a font at runtime.

Use:

- Strong uppercase display treatment for `DOPE`.
- Condensed semibold headings.
- Tabular figures for calculated values.
- Clear unit labels.
- Sentence case for descriptive UI copy.
- Large primary result numbers.
- No decorative script fonts.

The approved DOPE wordmark must be used as an image/vector asset, not replaced by plain default text.

## 3.4 Iconography

Use consistent outlined technical icons with a 2 dp visual stroke. Prefer Material Symbols or custom vector assets styled to match the reference. Do not mix filled cartoon icons with the approved line style.

## 3.5 Required visual modes

Implement:

- Standard dark mode as the default and primary approved mode.
- High-contrast outdoor mode.
- Red-light-preserving mode.
- Optional light mode only if it retains the DOPE visual identity; it is not required for the first release.

The user-approved dark mode must always remain available and must be the screenshot-test baseline.

## 3.6 Visual quality gates

Create screenshot or golden tests for the key screens at:

- Samsung Galaxy S25 portrait-equivalent dimensions.
- A common 360 × 780 dp portrait viewport.
- Landscape mode.
- Font scale 1.0 and 1.3.

The following screens must visually follow the reference before feature work is considered complete:

1. Splash.
2. Home dashboard.
3. Rifle profiles.
4. Ammunition profiles.
5. Scope profiles.
6. Scope detail and verification.
7. Environmental collection.
8. Wind direction wheel.
9. Calculation results.
10. Range card.
11. Session log.
12. Camera measurement and calibration.

New Range Analyst screens must extend the same design system rather than introduce another visual language.

---

# 4. Technical stack

Use stable, non-deprecated releases available when implementation begins.

Required:

- Kotlin.
- Gradle Kotlin DSL.
- Version catalogues.
- Jetpack Compose.
- Material 3.
- Navigation Compose.
- Kotlin Coroutines and Flow.
- Hilt dependency injection.
- Room.
- KSP.
- DataStore.
- Kotlin Serialization.
- CameraX for Preview, ImageCapture, ImageAnalysis and VideoCapture.
- Camera2 interop only where camera intrinsics or controls require it.
- Android SensorManager.
- Google Play services Fused Location Provider.
- ARCore as an optional capability.
- LiteRT for optional on-device vision inference.
- Retrofit and OkHttp for internet provider adapters.
- WorkManager only for explicit, user-enabled deferred work.
- Android TextToSpeech.
- Media audio routing APIs appropriate to the active Android version.
- JUnit.
- MockK or an equivalent Kotlin mocking library.
- Compose UI tests.
- Android instrumentation tests.
- Screenshot/golden testing.
- Static analysis with Detekt.
- Code formatting with ktlint or Spotless.

Avoid alpha, beta and release-candidate dependencies unless a required production capability has no stable equivalent. Document every exception.

Suggested SDK policy:

```text
minSdk: 28 or higher
targetSdk: latest stable available at implementation
compileSdk: latest stable available at implementation
```

Do not hard-code the SDK level in this prompt if current stable tooling requires a different supported value; document the selected values in `DEPENDENCY_VERSIONS.md`.

Use Java/Kotlin toolchains compatible with the selected Android Gradle Plugin.

---

# 5. Repository and module architecture

Use a maintainable multi-module architecture without creating dozens of empty modules.

Required logical modules:

```text
:app
:core:common
:core:model
:core:designsystem
:core:database
:core:network
:core:sensors
:core:location
:core:camera
:core:voice
:core:export
:domain
:ballistics
:vision
:feature:home
:feature:profiles
:feature:environment
:feature:calculator
:feature:range
:feature:analyst
:feature:settings
```

Small feature areas may initially share a feature module when splitting them would add no value. Domain and calculation code must remain independent of Android framework classes.

Use:

- Repository interfaces in domain modules.
- Android implementations in data/core modules.
- Immutable UI state.
- Unidirectional data flow.
- Sealed interfaces for screen state and calculation state.
- Explicit mappers between database entities, domain models and UI models.
- No direct Room entity exposure to Compose.
- No direct Retrofit DTO exposure to domain code.
- No global mutable singleton state.
- No hidden unit conversions.

Create architecture diagrams in `ARCHITECTURE.md`.

---

# 6. Core workflow

Primary workflow:

```text
Select rifle
→ Select ammunition
→ Select scope and verified variant
→ Select zero/reference profile
→ Collect or enter current environment
→ Enter or estimate distance
→ Capture direction and inclination
→ Enter wind speed and wind-from direction
→ Calculate
→ Review final MIL/MOA result and confidence
→ Generate range card
→ Start and save range session
→ Optionally perform post-string Range Analyst review
```

The home screen must always show:

- Active rifle.
- Active ammunition.
- Active scope.
- Scope verification status.
- Zero distance.
- Current environmental summary.
- Data freshness.
- Distance.
- Wind summary.
- Primary Calculate button.
- Range Card button.
- Measure Target button.
- Start Session button.
- Range Analyst button.

Do not show a confident calculation when required profile values are missing or unverified.

---

# 7. Units and internal precision

Store all physical values internally in SI units:

- Metres.
- Seconds.
- Metres per second.
- Pascals.
- Kelvin where required by formula.
- Kilograms.
- Radians.

Accept and display:

Metric:

- Metres.
- Metres per second.
- Kilometres per hour.
- Celsius.
- hPa.
- Millimetres.
- Grains for bullet weight, with SI conversion internally.

Imperial:

- Yards.
- Feet per second.
- Miles per hour.
- Fahrenheit.
- inHg.
- Inches.
- Grains.

Angular definitions:

```text
1 MIL = 0.001 radian
MIL = radians × 1000
MOA = radians × 180 / π × 60
1 MIL ≈ 3.437746770784939 MOA
1 MOA ≈ 0.2908882086657216 MIL
```

Keep full `Double` precision internally.

Never calculate from a rounded UI value.

Retain:

- Raw radians.
- Raw MIL.
- Raw MOA.
- Scope-unit value.
- Rounded valid turret value.
- Click count.
- Rounding residual.
- Reticle display value.

Use tabular numbers in the UI.

---

# 8. Data provenance and quality model

Every measured, imported, inferred or manually entered value must retain provenance.

Create a model equivalent to:

```kotlin
enum class DataSource {
    DEVICE_SENSOR,
    GPS,
    SAVED_RANGE,
    MANUAL,
    WEATHER_SERVICE,
    TERRAIN_SERVICE,
    BLUETOOTH_DEVICE,
    CAMERA_CALIBRATION,
    CAMERA_MEASUREMENT,
    ARCORE,
    CALCULATED,
    IMPORTED
}

enum class ReadingQuality {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR,
    UNAVAILABLE
}

data class MeasuredValue<T>(
    val value: T?,
    val unitId: String,
    val source: DataSource,
    val quality: ReadingQuality,
    val timestamp: Instant,
    val uncertainty: Double?,
    val accuracyDescription: String?,
    val rawValue: T?,
    val manuallyOverridden: Boolean,
    val notes: String?
)
```

Every calculation snapshot must preserve the exact input values, units, sources, timestamps, uncertainties, selected profile revisions, engine version and rounding method.

---

# 9. Domain profiles and storage

Use UUIDs.

All mutable profiles must have:

- `createdAt`.
- `modifiedAt`.
- `revision`.
- `archived`.
- `favourite`.
- Optional notes.

Do not hard-delete profiles that are referenced by a saved session. Archive them instead.

## 9.1 Rifle profile

Required fields:

- Profile name.
- Manufacturer.
- Model.
- Calibre/cartridge label.
- Barrel length.
- Twist rate.
- Twist direction.
- Optional internal reference.
- Notes.
- Favourite.
- Archived.

Serial number is not required. If provided, treat it as sensitive local data and exclude it from exports by default.

## 9.2 Ammunition profile

Required fields:

- Rifle link.
- Profile name.
- Manufacturer.
- Product/load name.
- Bullet manufacturer.
- Bullet name.
- Bullet weight.
- Bullet diameter where known.
- G1 BC.
- G7 BC.
- Selected drag model.
- Muzzle velocity.
- Optional muzzle-velocity standard deviation.
- Optional velocity-temperature coefficient.
- Optional ammunition reference temperature.
- Lot number.
- Chronograph date.
- Chronograph type.
- Notes.

Require one valid selected BC and muzzle velocity before trajectory calculation.

Never fabricate BC, bullet weight or muzzle velocity.

## 9.3 Chronograph strings

Allow entry of individual velocity readings.

Calculate and store:

- Raw readings.
- Average.
- Median.
- Minimum.
- Maximum.
- Extreme spread.
- Sample standard deviation.
- Sample count.
- Ammunition temperature if entered.
- Date.
- Chronograph.
- Notes.

The user selects which chronograph result feeds the active ammunition profile.

## 9.4 Zero profile

Required fields:

- Rifle.
- Ammunition.
- Scope.
- Zero distance.
- Sight height above bore.
- Zero elevation offset.
- Zero windage offset.
- Zero confirmation date.
- Reference environment.
- Verified status.
- Notes.

A change of scope, ammunition, sight height or zero distance must invalidate or duplicate the dependent zero profile rather than silently reusing it.

## 9.5 Reference atmosphere

Required fields:

- Name.
- Temperature.
- Station pressure.
- Relative humidity or dew point.
- Altitude.
- Date/time.
- Optional location.
- Source per field.
- Density altitude calculated by the app.
- Notes.

## 9.6 Saved range

Required fields:

- Range name.
- Optional coordinates.
- Optional surveyed altitude.
- Common distances.
- Static target profiles.
- Magnetic declination snapshot.
- Notes.
- Location-storage preference.

## 9.7 Static target profile

Allowed target classes:

- Rectangular paper.
- Circular paper.
- Grid paper.
- Printed silhouette-shaped range target that is explicitly a non-living paper target.
- Painted steel.
- Electronic range target.
- Custom static range target.

Required fields:

- Name.
- Physical width.
- Physical height.
- Centre coordinates.
- Scoring zones.
- Optional calibration marker positions.
- Optional reference image.
- Expected impact diameter range.
- Notes.

Do not include people, animals or vehicles as target classes.

---

# 10. Scope data model and verification

Scope data must support angular reticles, BDC reticles and mixed systems.

Create equivalent enums:

```kotlin
enum class AngularUnit { MIL, MOA }

enum class ReticleMeasurementSystem {
    MIL,
    MOA,
    BDC,
    HYBRID,
    UNKNOWN
}

enum class FocalPlane {
    FIRST,
    SECOND,
    UNKNOWN
}

enum class DialDirection {
    CLOCKWISE_UP,
    COUNTERCLOCKWISE_UP,
    UNKNOWN
}

enum class VerificationStatus {
    FACTORY_TEMPLATE,
    REQUIRES_USER_VERIFICATION,
    USER_VERIFIED,
    MODIFIED_AFTER_VERIFICATION
}
```

Scope profile fields:

- Scope family ID.
- Selected variant ID.
- Profile name.
- Manufacturer.
- Model.
- Minimum and maximum magnification.
- Objective diameter.
- Tube diameter.
- Focal plane.
- Turret unit.
- Elevation click value.
- Windage click value.
- Reticle system.
- Reticle name.
- Reticle subtension metadata.
- BDC metadata where applicable.
- Elevation travel.
- Windage travel.
- Zero-stop availability.
- Parallax adjustment.
- Illumination.
- Turret cap type.
- Elevation dial direction.
- Windage dial direction.
- Sight height.
- Zero distance.
- Zero offsets.
- Manufacturer specification source note.
- User verification status.
- Verification date.
- Notes.

Changing any of these fields must reset verification:

- Turret unit.
- Click value.
- Reticle type.
- Focal plane.
- Sight height.
- Zero distance.
- Dial direction.
- Selected physical variant.

An unverified template may be browsed and edited, but final click output must carry a prominent warning or be blocked according to the selected strictness setting.

Default strictness:

```text
Block confident click output until the physical scope variant and click value are confirmed.
```

## 10.1 Built-in scope family 1 — DNT Optics TheOne

Preload one top-level editable scope family:

```text
DNT Optics TheOne 7–35×56 FFP
```

Shared known template fields:

- Manufacturer: DNT Optics.
- Model: TheOne 7–35×56.
- Magnification: 7–35×.
- Objective: 56 mm.
- Tube: 34 mm.
- Focal plane: First.
- Illuminated: Yes.
- Zero stop: Yes.
- Parallax: adjustable.
- Verification: required.

Create selectable unverified variants:

### Variant A

```text
DNT TheOne — MIL / TOR-MIL
Turret unit: MIL
Nominal click: 0.1 MIL
Reticle system: MIL
Reticle name: TOR-MIL
Fine reticle reference: 0.2 MIL
```

### Variant B

```text
DNT TheOne — MOA / TOR-MOA
Turret unit: MOA
Nominal click: 0.25 MOA
Reticle system: MOA
Reticle name: TOR-MOA
```

Do not hard-code elevation or windage travel as authoritative because published regional/product revisions may differ. Store the template value as optional manufacturer metadata, require physical/manual confirmation, and let the user edit it.

First-use verification checklist:

```text
[ ] Physical model is TheOne 7–35×56
[ ] Turret unit confirmed from physical markings
[ ] Click value confirmed from physical markings/manual
[ ] Reticle variant confirmed
[ ] FFP confirmed
[ ] Elevation dial direction confirmed
[ ] Windage dial direction confirmed
[ ] Zero stop confirmed/configured
[ ] Sight height measured
[ ] Zero distance confirmed
```

## 10.2 Built-in scope family 2 — Arken EP-8

Preload one top-level editable scope family:

```text
Arken Optics EP-8 1–8×28 FFP
```

Shared known template fields:

- Manufacturer: Arken Optics.
- Model: EP-8 1–8×28.
- Magnification: 1–8×.
- Objective: 28 mm.
- Tube: 34 mm.
- Focal plane: First.
- Illuminated: Yes.
- Capped turrets.
- Verification: required.

Create selectable unverified variants:

### Variant A

```text
Arken EP-8 — MIL / KLGRID
Turret unit: MIL
Nominal click: 0.1 MIL
Reticle system: MIL
Reticle name: KLGRID
Nominal adjustment range metadata: 30 MRAD
```

### Variant B

```text
Arken EP-8 — MOA / KLBOX
Turret unit: MOA
Nominal click: 0.25 MOA
Reticle system: BDC
Reticle name: KLBOX
Nominal adjustment range metadata: 110 MOA
```

Important KLBOX rule:

The KLBOX is a BDC-style reticle. Do not represent it as a generic MIL reticle. Do not convert a trajectory result into a KLBOX mark unless the user has explicitly created and verified a BDC calibration for the selected cartridge, muzzle velocity, zero and environmental reference.

For the MOA/KLBOX variant:

- Display dial results in MOA.
- Display click count in 0.25 MOA increments after user verification.
- Display the reticle as `BDC` unless a verified reticle calibration exists.
- Do not invent a generic MIL hold.

First-use verification checklist:

```text
[ ] Physical model is EP-8 1–8×28
[ ] Variant is MIL/KLGRID or MOA/KLBOX
[ ] Turret unit confirmed
[ ] Click value confirmed
[ ] Reticle confirmed
[ ] FFP confirmed
[ ] Dial direction confirmed
[ ] Sight height measured
[ ] Zero distance confirmed
```

Built-in templates must be immutable originals. When a user customises one, duplicate it into a user-owned scope profile.

---

# 11. Turret tracking test

Add **Turret Test**.

Inputs:

- Scope.
- Test distance.
- Commanded clicks.
- Nominal click value.
- Measured linear movement.
- Measurement unit.
- Test orientation.
- Notes.
- Date.

Outputs:

- Expected movement.
- Actual angular movement.
- Effective click value.
- Tracking error percentage.
- Confidence.
- Warning if the measured movement is implausible.

Do not silently replace the manufacturer click value.

Let the user choose:

- Manufacturer nominal value.
- Measured effective value.
- Manufacturer value with tracking warning.

Require explicit confirmation before a measured value becomes active.

Preserve every test and revision.

---

# 12. Environmental collection

Create an `EnvironmentalCollectionCoordinator` that gathers compatible readings and reports capability, source, age, accuracy and stability.

Attempt to collect:

- Latitude.
- Longitude.
- Horizontal accuracy.
- GPS altitude.
- Vertical accuracy when available.
- Barometric pressure.
- Ambient temperature only from a genuine ambient sensor.
- Relative humidity only from a genuine humidity sensor.
- Device azimuth.
- Pitch.
- Roll.
- Sensor accuracy.
- Timestamp.

Sensor types:

```kotlin
Sensor.TYPE_PRESSURE
Sensor.TYPE_AMBIENT_TEMPERATURE
Sensor.TYPE_RELATIVE_HUMIDITY
Sensor.TYPE_ROTATION_VECTOR
Sensor.TYPE_ACCELEROMETER
Sensor.TYPE_MAGNETIC_FIELD
Sensor.TYPE_GYROSCOPE
```

Never use:

- Battery temperature.
- CPU temperature.
- GPU temperature.
- Device thermal state.

as ambient air temperature.

## 12.1 Capability detection

At runtime:

- Check each sensor through `SensorManager`.
- Check PackageManager features where useful.
- Show sensor name, vendor, version, resolution, range and reporting mode in Diagnostics.
- Degrade gracefully when absent.
- Do not make an optional sensor a manifest-required feature that excludes compatible phones from installation.

## 12.2 Pressure sampling

For a manual collection:

- Sample for a configurable 5–10 seconds.
- Discard initial settling samples.
- Reject physically implausible values.
- Calculate mean, median, trimmed mean, minimum, maximum and standard deviation.
- Prefer median or trimmed mean.
- Mark unstable readings.
- Show the sample interval.
- Allow the user to accept, retry or override.
- Store raw sample summary, not an unbounded continuous sensor log.

Pressure units:

- Android pressure sensor input is hPa.
- Convert internally to Pa.
- Display hPa by default.

## 12.3 Location

Use a foreground, one-time location request.

Do not request background location.

Show:

- Horizontal accuracy.
- Vertical accuracy.
- Timestamp.
- Approximate-versus-precise status.

Do not replace a saved surveyed range altitude with a poor GPS altitude.

Altitude priority:

1. Saved surveyed range altitude.
2. User-confirmed manual altitude.
3. Good-quality GPS altitude.
4. Optional terrain provider.
5. Weather-provider elevation.

The user must be able to use location temporarily for weather lookup without saving coordinates in the session.

## 12.4 Heading and magnetic declination

Prefer rotation vector.

Fallback to accelerometer and magnetometer.

Store:

- Raw magnetic heading.
- Magnetic accuracy.
- Declination.
- True heading.
- Heading uncertainty.
- Timestamp.

Use the platform geomagnetic model where available and location/date are known. Show whether the result is magnetic or true.

Provide a clear compass-calibration prompt when accuracy is low.

## 12.5 Orientation capture

Provide:

- `Capture firing direction`.
- `Capture inclination`.
- `Capture roll reference`.

Average readings over a short stable interval. Reject capture while motion exceeds a threshold.

Show a stability indicator.

---

# 13. Internet weather and elevation adapters

Create provider-neutral interfaces:

```kotlin
interface WeatherProvider
interface TerrainElevationProvider
```

Implement:

- A fake provider for tests and previews.
- A manual-only provider.
- One production weather adapter behind configuration and a feature flag.
- An optional terrain elevation adapter behind configuration.

A suitable first adapter may use Open-Meteo or another current provider, but:

- Keep provider terms and attribution isolated.
- Do not assume commercial use is permitted without review.
- Do not hard-code a secret.
- Record provider name, model/station metadata, timestamp and spatial resolution where available.
- Make replacement possible without changing domain code.

Weather request may return:

- Temperature at 2 m.
- Relative humidity.
- Dew point.
- Surface pressure.
- Mean sea-level pressure.
- Wind speed.
- Wind direction.
- Gust.
- Provider timestamp.
- Provider elevation.
- Provider/model location metadata.

Pressure terminology must be explicit:

- `stationPressure` or `surfacePressure`.
- `meanSeaLevelPressure`.
- `altimeterSetting`.

Never put mean sea-level pressure into a station-pressure field.

If surface/station pressure is not supplied and the app derives it:

- Retain the original pressure.
- Record the conversion formula.
- Mark source as `CALCULATED`.
- Include uncertainty.
- Show the user that it was derived.

Do not silently override a local barometer.

Default source priorities:

Pressure:

1. Calibrated external weather meter.
2. Device barometer.
3. Manual station pressure.
4. Nearby/current provider surface pressure.
5. Derived pressure.

Temperature:

1. External weather meter.
2. Genuine ambient phone sensor.
3. Manual entry.
4. Weather provider.

Humidity:

1. External weather meter.
2. Genuine humidity sensor.
3. Manual entry.
4. Weather provider.

Wind:

1. Manual local observation.
2. External anemometer.
3. Remote weather estimate.

Remote wind must always be labelled as an estimate and must never silently replace manual local wind.

Cache provider responses with timestamp and coordinates. Never present stale cached data as current without a warning.

---

# 14. Atmospheric calculations

Implement a pure Kotlin environmental-math package with unit tests.

## 14.1 Moist air density

Use station pressure, temperature and relative humidity or dew point.

Use a documented saturation vapour pressure formula such as the Buck equation.

For temperature `tC` in Celsius:

```text
es_hPa =
6.1121 × exp((18.678 − tC / 234.5) × (tC / (257.14 + tC)))
```

For relative humidity `RH` in percent:

```text
e_hPa = RH / 100 × es_hPa
pd_Pa = stationPressure_Pa − e_hPa × 100
pv_Pa = e_hPa × 100
T_K = tC + 273.15
rho = pd_Pa / (Rd × T_K) + pv_Pa / (Rv × T_K)
Rd = 287.05 J/(kg·K)
Rv = 461.495 J/(kg·K)
```

Validate input ranges.

## 14.2 Dew point

Implement a documented Magnus/Buck-compatible dew-point calculation. Preserve which method/version was used.

## 14.3 Pressure altitude

Implement ISA pressure altitude from station pressure.

## 14.4 Density altitude

Calculate density altitude by numerically solving for the ISA altitude at which standard density equals the measured moist-air density.

Do not rely only on a rough aviation shortcut.

## 14.5 Other outputs

Calculate and display:

- Moist air density.
- Density ratio to reference.
- Pressure altitude.
- Density altitude.
- Dew point.
- Vapour pressure.
- Approximate speed of sound.
- Reference-versus-current differences.

Show useful precision without false precision.

---

# 15. Manual wind system

Wind is manually entered by default.

Create the approved rotating wind-direction wheel.

Inputs:

- Wind-from true degrees.
- Wind-from magnetic degrees where applicable.
- Clock-face direction.
- Minimum speed.
- Average speed.
- Maximum speed.
- Gust.
- Selected calculation value.
- Direction of fire.
- Source.
- Timestamp.
- Notes.

Wheel features:

- Drag rotation.
- Tap position.
- Numeric degree entry.
- Clock entry.
- Fine-adjustment buttons.
- Reset.
- Lock.
- Switch true/magnetic reference.
- Visual arrow for direction of fire.
- Visual arrow showing where wind comes from.
- Clear left/right effect label.
- Cardinal and intercardinal labels.
- 0–359° marks.

Convention:

- Bearings are clockwise from north.
- Wind direction means the direction the wind comes from.
- Direction of fire is a bearing.
- Relative angle must be normalised to a signed range.
- Positive and negative crosswind signs must be documented and tested.
- UI wording must use “from” consistently.

Calculate:

- Relative wind-from angle.
- Crosswind component.
- Headwind/tailwind component.
- Minimum/expected/maximum wind bracket.

Do not hide the sign convention.

---

# 16. Ballistics engine

Create `:ballistics` as a pure Kotlin/JVM module.

No Android imports.

Define stable interfaces:

```kotlin
interface BallisticsEngine {
    fun solve(input: TrajectoryInput): TrajectoryResult
    fun rangeCard(input: RangeCardInput): RangeCardResult
}
```

Core requirements:

- G1 drag model.
- G7 drag model.
- Validated reference drag tables.
- Interpolation between table points.
- Gravity.
- Air-relative velocity.
- Horizontal wind vector.
- Headwind/tailwind.
- Sight height.
- Line-of-sight geometry.
- Zero solution.
- Uphill/downhill inclination.
- Time of flight.
- Remaining velocity.
- Remaining energy.
- Mach.
- Supersonic/transonic/subsonic state.
- Numerical convergence checks.
- Maximum calculation distance.
- Traceable engine version.
- Deterministic output.

Use a documented numerical integrator such as adaptive Runge–Kutta or a carefully tested fixed-step RK4 implementation with convergence tests.

Solve bore angle for the selected zero by root finding. Do not approximate the zero with a hard-coded shortcut.

The drag implementation must use an authoritative G1/G7 reference function and the user’s selected BC. Do not invent drag coefficients.

Optional advanced corrections:

- Spin drift.
- Coriolis.
- Aerodynamic jump.

These must be:

- Off by default.
- Independently enabled.
- Labelled advanced/experimental until validated.
- Included separately in the result breakdown.
- Covered by tests before release.

Do not block the core release on advanced corrections.

## 16.1 Reference/current comparison

For the same rifle, ammunition, scope, zero, distance and orientation, solve:

1. Reference atmosphere.
2. Current atmosphere.

Calculate:

```text
environmentalDeviationRadians =
currentElevationRadians − referenceElevationRadians
```

Display:

- Reference elevation.
- Current elevation.
- Environmental deviation.
- Inclination contribution.
- Wind result.
- Optional advanced contributions.
- Final raw angle.
- Final rounded turret setting.
- Click count.
- Residual.

Do not implement a fixed rule such as “add 0.1 MIL per 10°C”.

## 16.2 Uncertainty

Implement an initial sensitivity-based uncertainty estimate and allow later seeded Monte Carlo refinement.

Inputs may include uncertainty for:

- Distance.
- Muzzle velocity.
- BC.
- Pressure.
- Temperature.
- Humidity.
- Altitude.
- Inclination.
- Wind.
- Camera measurement.

Show:

- Raw result.
- Rounded usable result.
- Estimated uncertainty band.
- Dominant uncertainty contributors.

Do not display more decimal places than the data quality supports.

---

# 17. Scope output and clicks

From the raw angular solution:

```text
rawMIL = radians × 1000
rawMOA = radians × 180 / π × 60
```

For a verified MIL turret:

```text
clicks = round(rawMIL / clickValueMIL)
roundedMIL = clicks × clickValueMIL
```

For a verified MOA turret:

```text
clicks = round(rawMOA / clickValueMOA)
roundedMOA = clicks × clickValueMOA
```

Retain:

- Signed direction.
- Raw value.
- Rounded value.
- Click count.
- Rounding residual.
- Turret travel check.
- Revolutions where configured.
- Zero-stop reference.

For a BDC reticle:

- Do not pretend the reticle is a universal angular scale.
- Only show a BDC mark when a user-verified BDC calibration exists.
- Otherwise show dial output and `BDC hold not calibrated`.

For a second-focal-plane reticle:

- Require reticle calibration magnification.
- Show a warning when current magnification is unknown or differs.

Result screen sections:

1. Primary elevation dial.
2. Elevation clicks.
3. Wind result.
4. Reticle hold where valid.
5. Environmental deviation.
6. Reference/current comparison.
7. Confidence.
8. Warnings.
9. Expandable raw calculation trace.

---

# 18. Range cards

Generate a range card for a selected profile combination.

User controls:

- Start distance.
- End distance.
- Increment.
- Metric/imperial.
- Reference/current environment.
- Wind value or bracket.
- Inclination.
- MIL/MOA display.
- Turret and reticle display.
- Columns.

Columns:

- Distance.
- Elevation raw.
- Dial value.
- Clicks.
- Reticle hold where valid.
- Environmental deviation.
- Wind.
- Wind bracket.
- Time of flight.
- Remaining velocity.
- Remaining energy.
- Mach.
- Flight state.
- Uncertainty.
- Warning state.

Export:

- PDF.
- CSV.
- PNG image.
- Android share sheet.

Create outdoor, high-contrast and red-light layouts.

---

# 19. Camera-assisted known-target ranging

Use only known static paper or steel range targets.

Do not recognise or range people, animals or vehicles.

Workflow:

1. Select target profile.
2. Select lens.
3. Capture a still image.
4. Display the image with movable measurement anchors.
5. Mark known width or height.
6. Calculate angular size.
7. Calculate estimated distance.
8. Show uncertainty and calibration validity.
9. Require user confirmation before using the value.

Use CameraX.

Use Camera2 metadata/interoperability where available for:

- Camera ID.
- Physical focal length.
- Sensor physical size.
- Active array.
- Crop region.
- Zoom ratio.
- Resolution.
- Lens distortion.
- Optical stabilisation.
- Principal point if exposed.

Do not use marketing zoom as camera calibration.

Prefer a physical optical lens and a locked zoom ratio.

Formula for angular width using calibrated focal length in pixels:

```text
angularWidth =
2 × atan(pixelWidth / (2 × focalLengthPixels))

distance =
realTargetWidth /
(2 × tan(angularWidth / 2))
```

Also support classic angular measurement:

```text
distanceMetres =
targetSizeMetres × 1000 / measuredMIL
```

Warnings:

- Missing calibration.
- Wrong lens.
- Wrong resolution.
- Changed aspect ratio.
- Digital zoom.
- Target too small in frame.
- Blurred image.
- Unclear edges.
- Target outside calibrated range.
- Excessive perspective distortion.
- High range uncertainty.

---

# 20. Camera calibration

Create a guided calibration wizard.

The user must:

1. Select physical lens.
2. Lock resolution.
3. Lock or record zoom.
4. Place a known target at a measured distance.
5. Capture.
6. Mark edges.
7. Repeat at multiple distances.
8. Review fit and error.
9. Save calibration.

Store:

- Device manufacturer/model.
- Camera ID.
- Lens.
- Resolution.
- Aspect ratio.
- Zoom.
- Effective focal length.
- Distortion terms if used.
- Sample distances.
- Mean absolute error.
- Median percentage error.
- 95th percentile error.
- Valid distance range.
- Date.
- Sample count.
- App version.

Invalidate or warn when configuration changes.

---

# 21. AR orientation and training-video mode

Use ARCore only when runtime-supported.

AR orientation may show:

- Horizon.
- Heading.
- True/magnetic north.
- Inclination.
- Roll.
- Device stability.
- Tracking quality.
- Sensor confidence.
- Static target measurement anchors.
- Environmental summary.

It must not show:

- Corrected point of aim.
- Ballistic impact marker.
- Target tracking box.
- Lead indicator.
- Live corrected firing reticle.

ARCore Depth:

- Optional only.
- Capability checked at runtime.
- Not required for installation.
- Never represented as a dependable long-range rangefinder.
- Show confidence and usable range.
- Tested on a physical supported device.
- Fall back to calibrated known-size target measurement.

Training video:

- Camera image.
- Timestamp.
- Heading.
- Inclination.
- Roll.
- Stability.
- Environmental snapshot.
- Session name.
- Optional audio.

Overlay may show:

- Horizon.
- Angle.
- Heading.
- Sensor confidence.
- Recording state.

No live ballistic aim overlay.

Allow playback with synchronised sensor data and export:

- Original video.
- Video with informational overlay.
- Sensor CSV.

---

# 22. Sessions and verified data

A saved session must contain a complete immutable calculation snapshot.

Store:

- Date/time.
- Optional range/location.
- Profile revisions.
- Reference/current environment.
- Data source per field.
- Distance.
- Distance source and uncertainty.
- Direction.
- Inclination.
- Wind.
- Calculation result.
- Engine version.
- Scope rounding.
- Warnings.
- Notes.
- Photos.
- Training videos.
- Range Analyst strings.

Allow location storage to be disabled.

## 22.1 Verified range record

Allow the user to record after a controlled range exercise:

- Calculated setting.
- Actual dialled setting.
- Observed linear group centre.
- Group size.
- Number of shots.
- Conditions.
- Notes.
- Confidence.

Keep theoretical and verified data separate.

Statuses:

```text
CALCULATED
VERIFIED
BLENDED
DO_NOT_USE
```

Do not silently modify BC, muzzle velocity, zero or click value.

If a guided profile-truing feature is later added:

- Show proposed parameter changes.
- Change one justified parameter at a time where possible.
- Preserve originals.
- Show before/after.
- Require explicit confirmation.
- Keep revision history.

---

# 23. DOPE Range Analyst

Create a separate post-string subsystem named:

**DOPE Range Analyst**

Subtitle:

**AI-assisted target and completed-string review**

It is a review, scoring and training-data system, not a live fire-control system.

## 23.1 State machine

```kotlin
enum class RangeStringState {
    SETUP,
    BEFORE_REFERENCE_REQUIRED,
    READY,
    STRING_ACTIVE,
    REVIEW_PENDING,
    AFTER_IMAGE_REQUIRED,
    ANALYSING,
    USER_CONFIRMATION,
    REVIEW_COMPLETE,
    FAILED
}
```

Hard rules:

- No impact analysis in `STRING_ACTIVE`.
- No target-result speech in `STRING_ACTIVE`.
- No impact-derived angular correction in `STRING_ACTIVE`.
- No correction recommendation at any state.
- Analysis starts only after explicit `End String`.
- Low-confidence results require confirmation.
- User confirmation is authoritative.

## 23.2 Range Analyst workflow

1. Select session and target.
2. Confirm target physical dimensions.
3. Enter expected shot count.
4. Capture clean before-string reference.
5. Capture environment and manual wind.
6. Press `Start String`.
7. Record only allowed session data while active.
8. Press `End String`.
9. Capture/import after-string image.
10. Register and rectify images.
11. Detect impact candidates.
12. Show candidates for user correction.
13. Calculate confirmed group statistics.
14. Speak approved summary over Bluetooth.
15. Save corrected annotations for future training.

Image sources:

- Galaxy S25 camera.
- Imported photograph.
- Electronic-target image.
- Approved target camera still.
- Spotting-camera still.

Do not process a live target feed while the string is active.

## 23.3 Vision pipeline

Implement a deterministic conventional-computer-vision baseline first.

Suggested pipeline:

1. Detect printed calibration markers or user-selected corners.
2. Estimate homography.
3. Perspective-rectify before and after images.
4. Register images.
5. Normalise exposure and colour carefully.
6. Compute structural/pixel difference.
7. Generate candidate regions.
8. Filter by expected impact size and morphology.
9. Classify deterministic false-positive patterns.
10. Optionally pass candidate crops/masks to LiteRT.
11. Merge duplicates.
12. Assign confidence.
13. Require user confirmation.

Candidate labels:

```text
NEW_IMPACT
PRE_EXISTING_HOLE
TARGET_TEAR
STAPLE
TAPE_OR_PATCH
PAINT_SPLATTER
SHADOW
REFLECTION
INSECT_OR_DEBRIS
UNKNOWN
```

Use OpenCV or an equivalent permissively licensed computer-vision library if justified. Record dependency licence and version.

Do not fabricate an AI model. The app must work with the baseline pipeline and manual annotation before a trained model exists.

## 23.4 Confirmation screen

Allow:

- Add impact.
- Delete candidate.
- Move impact.
- Mark pre-existing hole.
- Mark tear.
- Mark staple.
- Mark tape.
- Mark splatter.
- Mark unknown.
- Confirm all.
- Reject analysis.
- Re-run with adjusted thresholds.

Show:

- Numbered markers.
- Confidence.
- Zoom and pan.
- Before/after toggle.
- Difference overlay.
- Target centre.
- Scoring rings.

## 23.5 Group measurements

After confirmation, calculate:

- Confirmed impact count.
- Expected-versus-confirmed count.
- Missing count.
- Extreme spread.
- Mean radius.
- Group centroid.
- Horizontal spread.
- Vertical spread.
- Linear offset from target centre.
- Scoring-zone total.
- Outlier candidates.
- Confidence.
- Comparison with historical strings.

Report offsets in millimetres or inches.

Do not automatically turn a linear offset into a spoken click or hold instruction.

## 23.6 Approved voice events

Use a strict allowlist:

```kotlin
enum class AllowedVoiceEvent {
    BLUETOOTH_CONNECTED,
    BLUETOOTH_DISCONNECTED,
    ENVIRONMENT_RECORDED,
    WIND_RECORDED,
    BEFORE_REFERENCE_SAVED,
    STRING_STARTED,
    STRING_ENDED,
    AFTER_IMAGE_SAVED,
    ANALYSIS_STARTED,
    ANALYSIS_COMPLETE,
    IMPACT_COUNT,
    MISSING_IMPACT_COUNT,
    GROUP_SIZE,
    GROUP_CENTRE_LINEAR_OFFSET,
    HORIZONTAL_SPREAD,
    VERTICAL_SPREAD,
    OUTLIER_NOTICE,
    SCORE_SUMMARY,
    IMAGE_QUALITY_WARNING,
    CONFIRMATION_REQUIRED,
    HISTORICAL_COMPARISON,
    SESSION_SAVED,
    ERROR
}
```

Blocked voice concepts:

- Scope clicks.
- MIL/MOA correction derived from impacts.
- Wind hold.
- Elevation hold.
- Point of aim.
- Lead.
- Target selection.
- Fire commands.

Build automated tests proving blocked concepts cannot reach TTS.

Approved examples:

```text
“String complete.”
“Five impact candidates detected. Please review.”
“Five impacts confirmed.”
“Group size forty-two millimetres.”
“Mean group centre eighteen millimetres low and seven millimetres right.”
“Vertical spread thirty-one millimetres.”
“Wind was recorded at twelve kilometres per hour from two o’clock.”
“This group is eleven millimetres smaller than your previous group at the same distance.”
“Image quality is low. Please confirm all markers.”
“Session saved.”
```

---

# 24. Bluetooth and voice

Use Android TextToSpeech for deterministic messages generated from structured data.

Do not use an unconstrained generative model to invent range instructions.

Voice settings:

- Installed voice selection.
- English South Africa preference where installed.
- UK English fallback.
- Speech rate.
- Volume.
- Concise mode.
- Detailed mode.
- Visual-only mode.
- Repeat last message.
- Test voice.
- Test Bluetooth output.
- Disable speaker fallback.
- Queue controls.

Use normal media audio attributes with speech content type.

Do not force legacy Bluetooth SCO for ordinary media speech unless a documented device-specific need is proven.

Detect active audio route. If Bluetooth disconnects and speaker fallback is disabled:

- Pause queued speech.
- Show a warning.
- Do not unexpectedly play through the phone speaker.

Request and abandon audio focus correctly.

Optional press-to-talk voice commands may include only:

```text
Capture environment
Record wind
Start string
End string
Repeat
Save session
Cancel
```

Do not implement continuous listening by default.

---

# 25. Teaching the Range Analyst

The app must support user correction and data export so the model can improve.

## 25.1 Training record

Store:

- Session ID.
- Before image.
- After image.
- Rectified images.
- Target profile.
- Expected shot count.
- Confirmed impact coordinates/masks.
- Rejected candidates and labels.
- Image quality.
- Camera metadata.
- Calibration profile.
- Baseline pipeline version.
- Model version.
- User corrections.
- Timestamp.

Strip precise location and sensitive metadata from training exports by default.

## 25.2 Teaching queue

Create a queue with:

- Unreviewed records.
- Reviewed records.
- Excluded records.
- Exported records.
- Model-test records.

The user can inspect and correct each annotation.

## 25.3 Desktop ML project

Add a separate `ml/` Python project to the repository.

Required files:

```text
ml/README.md
ml/pyproject.toml
ml/src/prepare_dataset.py
ml/src/validate_dataset.py
ml/src/train.py
ml/src/evaluate.py
ml/src/export_litert.py
ml/src/create_model_card.py
ml/src/sign_model.py
ml/tests/
```

The pipeline must:

- Import app training exports.
- Validate images and annotations.
- Split by session/target, not random individual image, to reduce data leakage.
- Support segmentation or candidate classification.
- Apply documented augmentations.
- Train.
- Evaluate on a held-out set.
- Produce metrics.
- Export a LiteRT-compatible model.
- Produce labels and metadata.
- Produce a model card.
- Calculate checksum.
- Sign the package.
- Preserve reproducibility seeds and dependency lock.

Do not claim a useful model exists until a real corrected dataset is supplied and validation thresholds are met.

The first app release may ship with:

- Conventional CV baseline.
- Manual annotation.
- Model loader.
- Test/demo model that is clearly non-production and disabled for real analysis.

Do not ship a fabricated “accurate” model.

## 25.4 Model package

Package must contain:

- Model.
- Labels.
- Input dimensions.
- Preprocessing.
- Postprocessing.
- Version.
- Training date.
- Dataset summary.
- Validation metrics.
- Compatible app version.
- Checksum.
- Digital signature.
- Model card.

Support:

- Factory baseline.
- Current model.
- Previous model.
- User-trained model.
- Rollback.

Reject unsigned or corrupted packages.

## 25.5 Model metrics

Track:

- Impact-count accuracy.
- False positives per target.
- False negatives per expected impact.
- Median localisation error in mm.
- 95th percentile localisation error.
- Registration failure rate.
- Confidence calibration.
- Performance by target type.
- Performance by lighting.
- Performance by image source.
- Inference latency.
- Memory usage.

On-device incremental training is not part of v1. It may be researched later behind a feature flag after a reliable validation and rollback process exists.

---

# 26. Complete screen inventory

Build all screens in the locked design system.

Core:

1. Splash.
2. First-run privacy and capability introduction.
3. Home dashboard.
4. Rifle list.
5. Add/edit rifle.
6. Ammunition list.
7. Add/edit ammunition.
8. Chronograph string.
9. Scope list.
10. Scope family/variant selector.
11. Scope detail.
12. Scope verification.
13. Turret test.
14. Zero profiles.
15. Reference atmospheres.
16. Saved atmospheric snapshots.
17. Environmental collection.
18. Manual environment edit.
19. Saved ranges.
20. Target library.
21. Orientation/inclinometer.
22. Wind direction wheel.
23. Distance input.
24. Camera measurement.
25. Camera calibration.
26. Calculation results.
27. Calculation trace.
28. Range card.
29. Session list.
30. Session detail.
31. Verified range entry.
32. Comparison/what-if.
33. Diagnostics.
34. Settings.
35. Privacy.
36. Import/export/backup.
37. About and version information.

Range Analyst:

38. Analyst home.
39. Analyst setup.
40. Select target.
41. Before-string capture.
42. Environment/wind snapshot.
43. Ready screen.
44. String active.
45. End-string confirmation.
46. After-string capture/import.
47. Analysis progress.
48. Confirm impacts.
49. Group review.
50. Voice and Bluetooth settings.
51. Teaching queue.
52. Annotation editor.
53. Model performance.
54. Training export.
55. Model management.

For every screen create:

- Loading state.
- Empty state.
- Error state.
- Offline state where relevant.
- Permission-denied state.
- Low-confidence state where relevant.
- Preview/sample data that is explicitly fictional.

---

# 27. Offline-first behaviour

Offline features:

- Profiles.
- Sensors.
- Manual environment.
- Environmental math.
- Ballistic engine.
- Scope conversion.
- Wind wheel.
- Range cards.
- Saved ranges.
- Camera measurement.
- Camera calibration.
- AR orientation when ARCore services are available.
- Sessions.
- Range Analyst baseline.
- TTS using installed voices.
- Export.

Online enhancements:

- Weather.
- Terrain elevation.
- Optional provider attribution.
- Optional model download/update.
- Optional cloud backup in a future release.

No core calculation may depend on an online AI service.

Show data age and source at all times.

---

# 28. Permissions and privacy

Request permissions only at the point of use.

Potential permissions:

- Camera.
- Coarse location.
- Fine location.
- Internet.
- Bluetooth connect/scan only when external devices are enabled.
- Microphone only for video audio or press-to-talk.
- Notifications only when the user enables a feature requiring them.

Do not request:

- Background location.
- Contacts.
- SMS.
- Call logs.
- Broad storage permission.
- Unrelated device data.

Use MediaStore and the Storage Access Framework.

Privacy defaults:

- No account.
- No analytics.
- No advertising SDK.
- No precise location storage unless enabled.
- Remove location EXIF from exported training images by default.
- Export serial references only by explicit selection.
- Store all sessions locally.
- Allow complete deletion.
- Allow encrypted backup.
- Explain what is sent to weather providers.

---

# 29. Security and data integrity

- Never commit API keys.
- Use `local.properties`, environment variables or a secure build secret.
- Do not embed private keys in the APK.
- Validate all imports.
- Version JSON schemas.
- Reject malformed data safely.
- Prevent path traversal.
- Do not execute imported content.
- Use checksums for model packages and backups.
- Use Android Keystore for locally protected encryption keys.
- Prefer field-level protection or an appropriately licensed database-encryption solution for sensitive optional fields.
- Preserve database migrations.
- Test restore before declaring backup complete.
- Log calculation errors without exposing sensitive values.
- No hidden network calls.

---

# 30. Import, export and backup

Support:

- Rifle JSON.
- Ammunition JSON.
- Scope JSON.
- Zero/reference profiles.
- Saved ranges and targets.
- Range cards in CSV/PDF/PNG.
- Session JSON.
- Range Analyst reviewed dataset export.
- Full encrypted backup.
- Plain backup with a clear warning.

When importing duplicates:

- Duplicate.
- Merge where safe.
- Replace with confirmation.
- Cancel.

Never silently overwrite.

Every export must state:

- App version.
- Schema version.
- Engine version.
- Creation date.
- Units.
- Whether precise location was included.

---

# 31. Diagnostics

Create a diagnostics screen showing actual device capabilities:

- Manufacturer/model.
- Android version.
- App version.
- Camera IDs.
- Physical lenses.
- Resolutions.
- CameraX capabilities.
- Camera2 hardware level.
- ARCore support.
- Depth support.
- Pressure sensor.
- Ambient-temperature sensor.
- Humidity sensor.
- Rotation vector.
- Accelerometer.
- Gyroscope.
- Magnetometer.
- Sensor accuracy.
- Location providers.
- Bluetooth support.
- Active audio route.
- Installed TTS engines/voices.
- Internet state.
- Weather-provider state.
- Current database version.
- Ballistics engine version.
- Vision pipeline version.
- Model version.

Provide a copyable diagnostic report that excludes sensitive profile and location data.

---

# 32. Performance and reliability

Targets on the Samsung Galaxy S25:

- Normal screen interaction at smooth frame rate.
- Environmental calculation under 100 ms where practical.
- Single-distance trajectory result under 500 ms after warm-up.
- Range card generation without blocking the main thread.
- Camera analysis off the main thread.
- Vision inference with bounded memory.
- No sensor listener left active after its screen/session stops.
- No location updates left running.
- No TTS queue leak.
- No unbounded raw image duplication.

Use lifecycle-aware collection.

Use WorkManager only for work that must survive process death and is explicitly user-enabled.

---

# 33. Testing

## 33.1 Unit tests

Required:

- SI conversions.
- MIL/MOA/radian conversions.
- Click rounding.
- Rounding residual.
- Scope travel limit.
- BDC no-generic-hold rule.
- Mixed profile handling.
- Chronograph statistics.
- Wind bearing normalisation.
- Wind-vector decomposition.
- Moist air density.
- Dew point.
- Pressure altitude.
- Density altitude solver.
- Reference/current environmental deviation.
- Zero root solver.
- Drag-table interpolation.
- Numerical convergence.
- Camera angular geometry.
- MIL target ranging.
- Calibration fit.
- Uncertainty.
- Source priority.
- Staleness.
- Profile revision invalidation.
- Import/export.
- Model signature validation.
- Group statistics.
- Impact annotation transformations.
- Voice allowlist.
- Range Analyst state machine.

## 33.2 Safety-boundary tests

Prove:

- No analysis in `STRING_ACTIVE`.
- No impact-derived voice correction.
- No click message can be produced by the analyst.
- No MIL/MOA correction message can be produced by the analyst.
- No wind-hold message can be produced by the analyst.
- No corrected aim point exists in AR or video overlay models.
- Only allowlisted voice events reach TTS.
- Bluetooth disconnect respects speaker fallback.
- People/animal/vehicle target types cannot be created through normal UI or imports.
- Invalid calculations do not display a confident result.

## 33.3 Instrumentation tests

- Permission flows.
- Denied permissions.
- Missing sensors.
- Approximate location.
- Offline mode.
- Camera capture.
- Lens changes.
- Configuration changes.
- AR unsupported.
- TTS unavailable.
- Bluetooth disconnect.
- Room migration.
- Process recreation.
- Import/export through SAF.
- Portrait/landscape.
- Font scaling.
- High contrast.
- Red-light mode.
- Navigation.

## 33.4 Fakes

Provide fakes for:

- Sensors.
- Location.
- Weather.
- Terrain.
- Camera metadata.
- Camera capture.
- AR capability.
- Bluetooth audio route.
- TTS.
- Ballistics engine.
- Vision pipeline.
- Model manager.
- File export.

Automated tests must not require a live API key.

## 33.5 Golden tests

Golden-test the approved key screens against the locked design reference. Treat intentional visual changes as explicit owner-review items.

---

# 34. Logging and auditability

Create a local calculation trace containing:

- Profile IDs and revisions.
- Raw inputs.
- Sources.
- Timestamps.
- Units.
- Environmental method version.
- Ballistics engine version.
- Result before rounding.
- Result after rounding.
- Warnings.
- Scope verification status.

Do not log precise coordinates or serial references to general logs.

Provide `Copy calculation details` and `Export calculation trace`.

---

# 35. Accessibility and field usability

- 48 dp minimum touch target.
- Screen-reader labels.
- Logical focus order.
- Font scaling.
- Do not rely only on colour.
- Haptic confirmation.
- Optional audible confirmation.
- Glove-friendly controls.
- Lock critical controls during active capture.
- Prevent screen sleep during explicit active measurement/session only.
- Restore normal screen behaviour afterward.
- One-handed access to primary controls.
- Landscape layouts for range-card and analyst review.
- Large numeric keypad.
- Undo for destructive edits.

---

# 36. Documentation deliverables

Create and maintain:

```text
README.md
ARCHITECTURE.md
IMPLEMENTATION_PLAN.md
DECISIONS.md
DECISIONS_REQUIRED.md
ASSUMPTIONS.md
DEPENDENCY_VERSIONS.md
DESIGN_SYSTEM.md
CALCULATION_METHODS.md
BALLISTICS_ENGINE.md
ENVIRONMENTAL_CALCULATIONS.md
SCOPE_PROFILES.md
CAMERA_CALIBRATION.md
AR_LIMITATIONS.md
RANGE_ANALYST.md
ML_TRAINING.md
VOICE_POLICY.md
PRIVACY.md
PERMISSIONS.md
SECURITY.md
TESTING.md
KNOWN_LIMITATIONS.md
RELEASE_BUILD.md
CHANGELOG.md
```

Document:

- Every formula and constant.
- Drag-table source and licence.
- Pressure terminology.
- Sensor limitations.
- Camera limitations.
- AR depth limitations.
- Scope verification.
- BDC limitations.
- Uncertainty.
- Voice restrictions.
- Model validation.
- Data provenance.
- Offline behaviour.

---

# 37. Continuous integration and quality

Create CI that runs:

```text
./gradlew lint
./gradlew detekt
./gradlew test
./gradlew assembleDebug
```

Add instrumentation tests to a separate job where an emulator is available.

Use dependency locking/version catalogues.

Generate test reports as artifacts.

Do not modify external repository protection settings. Once the required checks are deterministic and proven, report which CI checks should be made required.

---

# 38. Milestone plan

Codex must complete one milestone at a time and keep `IMPLEMENTATION_PLAN.md` current.

## Milestone 0 — Repository and decisions

- Inspect repository.
- Preserve useful existing work.
- Add this prompt and design assets under `docs/`.
- Create documentation skeleton.
- Configure Gradle.
- Select stable dependency versions.
- Record package-ID release gate.
- Create CI baseline.
- Build empty app.

Gate:

- Project compiles.
- Unit-test task runs.
- No secrets committed.

## Milestone 1 — Locked design system and navigation

- Implement exact tokens.
- Implement wordmark asset.
- Topographic background.
- Reusable cards, chips, buttons, fields and result panels.
- Bottom navigation.
- Splash and core screen shells.
- Golden tests.

Gate:

- Key shells match approved board.
- Dynamic colour disabled.
- Accessibility basics pass.

## Milestone 2 — Profiles and database

- Room schema.
- Rifle.
- Ammunition.
- Chronograph.
- Scope families and variants.
- Built-in DNT and Arken templates.
- Scope verification.
- Zero profiles.
- Reference atmosphere.
- Saved ranges and target profiles.
- Import/export schema v1.

Gate:

- CRUD works.
- Migrations tested.
- Scope variants remain unverified until confirmed.
- BDC rule tested.

## Milestone 3 — Sensors, environment and internet adapter

- Capability diagnostics.
- Pressure collection.
- Location.
- Heading/orientation.
- Manual overrides.
- Environmental math.
- Weather-provider seam.
- One configured production adapter.
- Caching and staleness.
- Atmospheric snapshots.

Gate:

- Works without optional sensors.
- No battery temperature misuse.
- Data provenance visible.
- Offline manual environment works.

## Milestone 4 — Ballistics engine

- Pure Kotlin solver.
- G1/G7 drag.
- Zero solver.
- Reference/current comparison.
- Environmental deviation.
- MIL/MOA.
- Scope clicks.
- Uncertainty v1.
- Calculation trace.

Gate:

- Deterministic benchmark fixtures pass.
- Convergence tests pass.
- No hard-coded demo result in production path.
- Invalid inputs block confident output.

## Milestone 5 — Wind, range cards and sessions

- Wind wheel.
- True/magnetic handling.
- Bracket.
- Range cards.
- PDF/CSV/PNG.
- Session logging.
- Verified range entry.
- Comparison/what-if.

Gate:

- Wind conventions tested.
- Export works offline.
- Session snapshot is immutable and traceable.

## Milestone 6 — Camera ranging and calibration

- CameraX.
- Lens capability screen.
- Still capture.
- Manual anchors.
- Camera metadata.
- Calibration wizard.
- Known-target ranging.
- Uncertainty.
- Physical Galaxy S25 test checklist.

Gate:

- No digital-zoom assumption.
- Wrong calibration warns.
- Static targets only.
- Camera result requires confirmation.

## Milestone 7 — AR orientation and training video

- AR capability check.
- Horizon, angle, heading, roll.
- Sensor confidence.
- Optional Depth support.
- Training video and playback.
- Sensor CSV.

Gate:

- No live ballistic aim point.
- AR unsupported fallback.
- Depth limitations visible.
- Physical-device testing documented.

## Milestone 8 — Range Analyst conventional baseline

- State machine.
- Before/after workflow.
- Target registration.
- Perspective correction.
- Image difference.
- Candidate detection.
- Manual confirmation.
- Group statistics.
- Teaching queue.

Gate:

- No analysis while active.
- User annotations authoritative.
- Registration failure blocks statistics.
- No spoken corrections.

## Milestone 9 — Bluetooth voice

- TTS.
- Media audio routing.
- Bluetooth route status.
- Allowlisted templates.
- Voice settings.
- Optional press-to-talk commands.

Gate:

- Safety voice tests pass.
- Speaker fallback respected.
- No unconstrained generated instructions.

## Milestone 10 — LiteRT and desktop training pipeline

- Model loader.
- Signature/checksum.
- Model rollback.
- ML Python project.
- Dataset validator.
- Training/evaluation/export scripts.
- Model card.
- Performance screen.

Gate:

- No claim of production accuracy without dataset metrics.
- App still works without AI model.
- Corrupt/unsigned model rejected.

## Milestone 11 — Hardening and release candidate

- Security review.
- Privacy.
- Backup/restore test.
- Accessibility.
- Performance.
- Database migration test.
- End-to-end physical-device test.
- Known limitations.
- Signed build instructions.
- AAB/APK build.

Gate:

- All required tests pass.
- No critical warning suppressed.
- Release checklist complete.
- Owner confirms package ID and final branding assets.

---

# 39. Codex working protocol

At the start of each milestone:

1. Read this master prompt.
2. Read `DECISIONS.md`, `DECISIONS_REQUIRED.md`, `ASSUMPTIONS.md` and `IMPLEMENTATION_PLAN.md`.
3. Inspect current repository state.
4. State the milestone scope.
5. Identify blockers.
6. Implement only that milestone plus required fixes.
7. Run tests and build.
8. Update documentation.
9. Commit if repository access permits.
10. Stop and report.

Required report format:

```text
Milestone
Status
Completed
Files created
Files modified
Database changes
Tests added
Commands run
Test results
Build result
Warnings
Assumptions
Decisions required
Known limitations
Recommended next milestone
```

Never report a test as passed unless it was run.

Never claim a physical-device feature is verified if it was only tested in an emulator.

Do not delete existing files without explaining why.

Do not rewrite Git history.

Do not weaken tests to make a build pass.

Do not hide compiler warnings by broad suppression.

---

# 40. Definition of done

The project is complete only when:

- The approved DOPE visual system is faithfully implemented.
- Core calculations work offline.
- Data provenance is visible.
- DNT and Arken scope families are preloaded and require physical verification.
- MIL and MOA outputs are correct and tested.
- BDC reticles are not misrepresented as universal angular reticles.
- Environmental deviation is calculated from complete reference/current trajectories.
- Range cards export.
- Camera measurement is calibrated and confidence-labelled.
- AR shows orientation only, not a live ballistic aim point.
- Range Analyst runs only after a string ends.
- Range Analyst voice uses the allowlist.
- User corrections can be exported for model teaching.
- No AI accuracy is fabricated.
- Backup and restore are tested.
- Privacy defaults are conservative.
- All required unit, instrumentation, safety and visual tests pass.
- The app builds as debug APK and release AAB.
- Documentation is complete.
- Physical Samsung Galaxy S25 testing is documented.
- Package ID and release signing decisions are confirmed by the owner.

---

# 41. First Codex action

Do not start with the ballistics solver.

Start with **Milestone 0 only**.

Perform:

1. Inspect the repository.
2. Copy this specification into `docs/DOPE_CODEX_MASTER_BUILD_PROMPT.md`.
3. Place approved assets under `docs/design/`.
4. Create the documentation files.
5. Configure the Android project and version catalogue.
6. Create a minimal app shell.
7. Configure lint, formatting, static analysis and unit testing.
8. Add CI.
9. Build the debug app.
10. Run tests.
11. Report using the required milestone format.
12. Stop for review.
