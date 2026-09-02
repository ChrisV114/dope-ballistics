# AR and Orientation Limitations

Milestone 7 treats AR as an optional informational enhancement. The app checks ARCore at runtime and continues with CameraX plus Android orientation sensors when ARCore is unsupported, missing or still resolving. Google Play Services for AR is requested only after the user presses the install/update control.

The overlay may show a horizon, magnetic heading, inclination, roll, stability, sensor confidence and recording state. It never shows a corrected point of aim, ballistic impact marker, tracked target, lead indicator or live corrected firing reticle.

ARCore Depth is checked only when ARCore is installed and camera permission has been granted. The screen states that the check is capability-only and that Google's documented best accuracy is roughly 0.5–5 m under scene/motion-dependent conditions. Depth is disabled as a product dependency, is never described as a long-range rangefinder, and cannot establish target distance for the ballistic calculator. A visible sensor-only fallback remains available.

Training video is user-started, stored locally and may include microphone audio only after a separate point-of-use permission. Playback synchronizes the saved orientation stream with the original video. Original-video sharing and privacy-bounded Sensor CSV export are implemented. A burned-in informational-overlay MP4 remains pending codec and physical-device validation.

Physical Samsung Galaxy S25 Ultra acceptance is still required for heading accuracy, compass calibration prompts, sensor stability, ARCore/Depth status, video/audio capture, thermal behaviour, playback synchronization and safe-area controls.
