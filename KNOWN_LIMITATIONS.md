# Known Limitations

- Milestone 3 provides environment collection and atmospheric math; ballistic trajectory calculations, range-card export, camera, AR, Range Analyst, voice and ML remain deferred.
- No physical Galaxy S25 sensor validation has yet confirmed barometer stability, compass calibration behaviour, GPS vertical accuracy or landscape field ergonomics.
- Open-Meteo values are model estimates, not on-site measurements. Weather wind never overwrites the manual wind screen.
- Orientation currently requires a rotation-vector sensor; the accelerometer-plus-magnetometer fallback remains to be added before physical acceptance if the target device lacks rotation vector support.
- True heading remains unavailable until a reviewed magnetic-declination source is connected; captured headings are labelled magnetic.
- Cached weather is an offline fallback and displays age/staleness; it must not be mistaken for a current observation.
- The speed-of-sound method currently applies temperature only; humidity correction remains deferred pending method validation.
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
