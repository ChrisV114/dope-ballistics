# Match Plans and Multi-Rifle Stages

## Match structure

A match contains ordered stages. A stage contains ordered engagement steps, and every engagement references its own immutable rifle, ammunition/load, scope and zero-profile revisions. A stage may use one rifle or switch equipment between targets; no stage-level rifle assumption may overwrite the engagement-level selection.

Each planned engagement retains:

- Sequence number and target name.
- Confirmed distance, source, uncertainty and target profile.
- Rifle, ammunition/load, scope and zero dependency fingerprint.
- Predicted raw and rounded elevation/windage setting with calculation trace.
- Planned shot count and progression mode.
- Actual setting used and resulting DOPE-log observation after confirmation.
- Optional notes and user-authored transition instructions.

An equipment change is an explicit ordered transition step, for example from the Smith & Wesson M&P15 Sport III .223 to the Howa 6.5 Creedmoor within one stage. The transition shows the next rifle/load/scope/zero combination and requires manual confirmation. Shot counting, elapsed time or camera sequencing must never infer that the physical change is complete.

## Phone and watch behaviour

The phone is authoritative for creating, editing and syncing a plan. During a multi-gun match it is carried on the user, not placed on the landscape camera stand, and may remain locked with its screen off. Future shot timing/counting must therefore use an explicit foreground match session with visible status, user-controlled start/stop and battery-aware recovery. Camera input is neither assumed nor required in this mode.

The future Galaxy Watch7 companion receives an offline snapshot and shows:

- Match/stage progress.
- Current rifle/load identity in a visually dominant, colour-independent label.
- Current target sequence, confirmed distance, setting and planned shot count.
- Shot-count confidence and manual correction.
- The next engagement or explicit equipment-change step.
- A manual `Ready` confirmation before a different rifle's setting becomes active.

After a confirmed shot group, the watch may wake and present the next planned step. Low-confidence acoustic counts must request confirmation instead of advancing. The watch may display stored/calculated DOPE but must not generate live aim points, impact-derived corrections or unconstrained spoken instructions.

The round 44 mm Galaxy Watch7 is the primary layout baseline, with a 40 mm fallback. Essential setting and confirmation controls must remain clear of the circular edge and system gesture regions.

Fixed-camera sequencing is a separate operating mode for the phone mounted on the rigid landscape stand. One phone cannot simultaneously be treated as body-carried match audio/timing hardware and a fixed target camera. Combining those modes would require a separately paired camera device and is outside the current plan.

## Logging

Completion creates one DOPE-log observation per engagement rather than one aggregate stage record. This preserves which rifle and actual setting applied to each target. A later truing process may use only trusted observations with an exact matching equipment dependency fingerprint, as governed by `DOPE_LOG.md`.
