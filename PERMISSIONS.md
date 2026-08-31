# Permissions

Milestone 3 declares coarse location, fine location and internet access.

- Location is requested only when **Capture location once** is pressed. Denial leaves manual altitude and offline atmospheric calculation available. No background location permission or continuous tracking exists.
- Approximate location is accepted and labelled with horizontal accuracy. GPS altitude and vertical accuracy are optional and retain their provenance.
- Internet is used only when **Fetch current weather** is pressed. There are no startup, background or hidden weather requests.
- SensorManager readings require no runtime permission and every optional sensor is checked at runtime.

Camera, Bluetooth, microphone and notifications remain deferred to their own milestones. Contacts, SMS, call logs, background location and broad storage access are prohibited.
