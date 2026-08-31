# Wind, Range Cards and Sessions

## Wind convention

- Bearings are degrees clockwise from north and wind direction means where the wind comes from.
- Direction of fire uses the same true or magnetic reference as the wind observation.
- Relative direction is normalized to `[-180, 180)`.
- Positive headwind travels from the target toward the shooter.
- Positive crosswind moves the projectile right; negative moves it left.
- Magnetic bearings convert to true with east-positive declination. Without declination, relative components remain valid while true bearings remain unavailable.

The screen accepts tap/drag wheel input, numeric bearing, clock direction, direction of fire, minimum/average/maximum/gust speed, selected speed, source and notes. Lock freezes the reviewed observation for downstream calculation until the user unlocks it.

The calculation screen reuses that same state and provides quick average-speed and wind-from adjustments. Every adjustment preserves an ordered minimum/average/maximum bracket and recalculates immediately; the full wheel remains available for deliberate entry.

## Range cards

Range cards use the active verified rifle/ammunition/scope/zero chain and current environment. The regular start/end/increment series is merged with saved target distances only when their distance is confirmed and their DOPE inclusion flag is enabled. Unconfirmed measurements are rejected.

The active chain is selected explicitly from saved zero setups. Creating one links one rifle, ammunition belonging to that rifle, one physically verified scope, the confirmed zero geometry and a manual zero-reference atmosphere. Room version 5 stores only the selected zero ID; migration does not rewrite existing profile or session records.

Each row includes raw and dialled elevation, clicks, environmental deviation, selected/minimum/maximum windage, time of flight, remaining velocity/energy, Mach/flight state, uncertainty and warning state. CSV, PDF and PNG are generated locally without network access and shared through Android's chooser.

## Immutable sessions

A completed session freezes the full calculation evidence and receives a SHA-256 content hash. A verified range record separately stores the theoretical result and actual dialled setting with group observations, shot count, conditions, confidence and status. The DAO exposes no update or delete operation for either table. Corrections must append a new record and may link it through `supersedesRecordId` when the correction UI is implemented.

Precise location is stored only when the source environment snapshot permits export and the user opts in again for the session. Saving a session never changes BC, muzzle velocity, zero, click value or any profile revision.
