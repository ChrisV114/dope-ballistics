# Known Limitations

- Milestone 0 contains only a minimal Compose shell; no product workflow exists.
- The locked visual design is not implemented until Milestone 1.
- Profiles, storage, sensors, location, networking, calculations, exports, camera, AR, Range Analyst, voice and ML are not implemented.
- No emulator or physical Samsung Galaxy S25 verification is claimed.
- The working package ID is not release-approved.

- Milestone 1 screens are design/navigation shells; profile persistence, calculations, ranging and exports remain deferred.
- Target-size choices do not perform camera ranging. IDPA/A-series values and nominal gong sizes require confirmation against the physical target.
- Match plans, Wear OS display/shot counting, fixed-camera target sequencing and pistol drill cues are documented future requirements only.
- Automatic acoustic shot counting is not implemented and will require confidence, manual correction and physical range testing.
- Host-side golden tests passed, but local emulator instrumentation and physical Samsung Galaxy S25 checks were not run.
- Android Compose Screenshot Testing is an isolated alpha16 build-time dependency because no stable AGP 9-compatible Android tool currently satisfies the required golden-test gate.


- Gradle reports `ReportingExtension.file(String)` as deprecated through a current third-party quality plugin; the build passes on Gradle 9.7.1, but that upstream plugin must be upgraded before Gradle 10.
- Debug packaging reports that `libandroidx.graphics.path.so` cannot be stripped and packages it unchanged; this is a debug dependency warning, not a failed build.
