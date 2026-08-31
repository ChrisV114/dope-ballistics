# Known Limitations

- Milestone 4 provides a deterministic point-mass G1/G7 engine. Wind UI, range-card presentation/export, camera, AR, Range Analyst, voice and ML remain deferred.
- G1/G7 accuracy is limited by the supplied manufacturer BC and its validity across velocity bands; the engine does not invent or convert coefficients.
- The current solver excludes spin drift, Coriolis, aerodynamic jump, custom Doppler drag curves and verified BDC holds.
- Uncertainty v1 is one-sided local sensitivity with root-sum-square combination; it does not yet model input correlation or Monte Carlo distributions.
- The ballistic benchmark fixtures protect deterministic behaviour and convergence but have not yet been independently validated by live-fire drop data or a certified reference solver across the full operating envelope.
- Three owner-observed 6.5 Creedmoor DOPE points agree closely with the standard-atmosphere fixture, but their original environment/session metadata is unknown and no truing workflow is implemented until Milestone 5.
- Actual per-target match settings, the immutable DOPE log and learning/true-profile workflow are specified in `DOPE_LOG.md` but deliberately not implemented inside the Milestone 4 engine.
- No physical Galaxy S25 sensor validation has yet confirmed barometer stability, compass calibration behaviour, GPS vertical accuracy or landscape field ergonomics.
- Open-Meteo values are model estimates, not on-site measurements. Weather wind never overwrites the manual wind screen.
- Orientation currently requires a rotation-vector sensor; the accelerometer-plus-magnetometer fallback remains to be added before physical acceptance if the target device lacks rotation vector support.
- True heading remains unavailable until a reviewed magnetic-declination source is connected; captured headings are labelled magnetic.
- Cached weather is an offline fallback and displays age/staleness; it must not be mistaken for a current observation.
- The speed-of-sound method currently applies temperature only; humidity correction remains deferred pending method validation and is shared by environment and trajectory calculations.
- Import/export logic is implemented, but Android Storage Access Framework file selection remains deferred to the milestone that adds the full backup/export UI.
- Zero, atmosphere and saved-range records have repository CRUD but do not yet have complete dedicated editing screens.
- Target-size choices do not perform camera ranging. IDPA/A-series values and nominal gong sizes require confirmation against the physical target.
- Confirmed manual target distances can populate the target's DOPE distance flag; camera/range-finder execution remains Milestone 6.
- Match plans, Wear OS display/shot counting, fixed-camera target sequencing and pistol drill cues are documented future requirements only.
- Automatic acoustic shot counting is not implemented and will require confidence, manual correction and physical range testing.
- GitHub API 35 emulator instrumentation passed for Milestones 1, 2 and 3. No physical Samsung Galaxy S25 acceptance is claimed.
- The application ID is owner-approved as `za.co.bdstudio.dope`; release signing, keystore custody and public distribution remain undecided.
- Android Compose Screenshot Testing is an isolated alpha16 build-time dependency because no stable AGP 9-compatible Android tool currently satisfies the required golden-test gate.


- Gradle reports `ReportingExtension.file(String)` as deprecated through a current third-party quality plugin; the build passes on Gradle 9.7.1, but that upstream plugin must be upgraded before Gradle 10.
- Debug packaging reports that `libandroidx.graphics.path.so` cannot be stripped and packages it unchanged; this is a debug dependency warning, not a failed build.
