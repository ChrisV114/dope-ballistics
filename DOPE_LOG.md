# DOPE Log and Truing

## Milestone 5 implemented baseline

The DOPE log is an append-only history of what was calculated, what was actually used and what the shooter confirmed at a static range target. Historical entries remain immutable; corrections create a superseding record so the original evidence is recoverable.

Each controlled-range observation retains:

- Rifle, ammunition/load, bullet, scope and zero-profile snapshot IDs and revisions.
- Ammunition lot and chronograph string when known.
- Verified target distance, source, uncertainty and confirmation state.
- Session identifier and optional saved-range identity.
- Reference and current atmospheric snapshot, wind and inclination.
- Engine version, calculation trace and predicted raw/rounded setting.
- Actual elevation dial setting used, including unit and optional clicks.
- Optional result classification and user notes.
- Timestamp, confidence and an explicit verified-data status.

Milestone 5 does not silently use these observations to modify a profile. Ordered target names, actual elevation/windage settings per match engagement and reused-setting provenance remain requirements for the later match-plan implementation.

## Accuracy improvement rules

If a guided truing workflow is later added, it may improve equipment-specific predictions only through an explicit, reviewable derived true profile:

1. Filter observations to the exact rifle, ammunition/load, scope and zero dependency fingerprint.
2. Exclude unconfirmed range, missing critical conditions, unit ambiguity, scope changes and observations not approved for truing.
3. Show observed minus predicted residual at every distance.
4. Propose bounded changes to one parameter family at a time. Muzzle-velocity and BC truing must remain separate and preserve the manufacturer value.
5. Show before/after residuals, observation count, distance coverage, dominant uncertainties and any held-out validation observations.
6. Require explicit acceptance to create a new versioned derived profile. Never overwrite the source profile or historical log.
7. Allow rollback and comparison between original, previous true and proposed true profiles.

One observation must never silently train the profile. Low-confidence or contradictory data remains useful in the log but inactive for truing. The app must not claim increasing accuracy merely from usage count; improvement requires sufficient, consistent, confirmed data and must be demonstrated by lower held-out residuals.

All future learning and log analysis remains local-first. The implemented session JSON carries schema/app/engine/unit/privacy metadata; range cards carry the same core provenance in CSV/PDF/PNG form. Precise location is excluded unless both the environment snapshot and the session save explicitly allow it.
