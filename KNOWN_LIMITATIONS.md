# Known Limitations

- Milestone 2 provides profile persistence and profile JSON import/export logic; sensors, networking, ballistics calculations, range-card export, camera, AR, Range Analyst, voice and ML remain deferred.
- Import/export logic is implemented, but Android Storage Access Framework file selection remains deferred to the milestone that adds the full backup/export UI.
- Zero, atmosphere and saved-range records have repository CRUD but do not yet have complete dedicated editing screens.
- Target-size choices do not perform camera ranging. IDPA/A-series values and nominal gong sizes require confirmation against the physical target.
- Confirmed manual target distances can populate the target's DOPE distance flag; camera/range-finder execution remains Milestone 6.
- Match plans, Wear OS display/shot counting, fixed-camera target sequencing and pistol drill cues are documented future requirements only.
- Automatic acoustic shot counting is not implemented and will require confidence, manual correction and physical range testing.
- GitHub API 35 emulator instrumentation passed for Milestones 1 and 2, including Milestone 2 migration/CRUD acceptance. No physical Samsung Galaxy S25 acceptance is claimed.
- The working package ID is not release-approved.
- Android Compose Screenshot Testing is an isolated alpha16 build-time dependency because no stable AGP 9-compatible Android tool currently satisfies the required golden-test gate.


- Gradle reports `ReportingExtension.file(String)` as deprecated through a current third-party quality plugin; the build passes on Gradle 9.7.1, but that upstream plugin must be upgraded before Gradle 10.
- Debug packaging reports that `libandroidx.graphics.path.so` cannot be stripped and packages it unchanged; this is a debug dependency warning, not a failed build.
