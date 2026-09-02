# Permissions

Milestones 3, 6 and 7 declare coarse location, fine location, internet, camera and microphone access.

- Location is requested only when **Capture location once** is pressed. Denial leaves manual altitude and offline atmospheric calculation available. No background location permission or continuous tracking exists.
- Approximate location is accepted and labelled with horizontal accuracy. GPS altitude and vertical accuracy are optional and retain their provenance.
- Internet is used only when **Fetch current weather** is pressed. There are no startup, background or hidden weather requests.
- SensorManager readings require no runtime permission and every optional sensor is checked at runtime.
- Camera is requested only from Camera diagnostics or Orientation & training video. Denial leaves all non-camera app features available.
- Microphone is requested only when the user enables optional audio and starts a training recording. Silent video remains available after denial. No background listening or automatic recording exists.
- ARCore is optional. Its install/update prompt appears only after the user presses the visible control on a supported device.
- Video and Sensor CSV use MediaStore on Android 10+ and app-private/FileProvider storage on Android 9. No broad storage permission is requested.

Bluetooth and notifications remain deferred to their own milestones. Contacts, SMS, call logs, background location and broad storage access are prohibited.
