# AR Orientation and Training Video

## Product boundary

This mode is informational. It records the camera stream and synchronized device orientation for later training review. It does not display or export a corrected point of aim, ballistic impact marker, tracked target, lead indicator or corrected reticle.

ARCore is optional. Runtime support is checked before it is used, installation/update is user-triggered, and unsupported devices retain CameraX video plus sensor orientation. ARCore Depth is reported only as an optional near-field capability. Capability is not measurement confidence; Google's documented best accuracy is roughly 0.5–5 m and depends on scene detail and device movement. DOPE never uses it for long-range target distance.

## Stored and exported data

- Original local MP4 video.
- Session name and recording start/end timestamps.
- Saved environmental summary without coordinates.
- Synchronized elapsed time, magnetic heading, pitch, roll, accuracy and stability samples.
- Optional microphone audio only after explicit enablement and permission.
- Sensor CSV with no coordinates, target identity, ballistic result or aim information.

The current local history is bounded to the newest 100 metadata/sample records. Media is saved through MediaStore on Android 10+ and app-private storage on Android 9. Sharing uses Android's chooser.

## Samsung Galaxy S25 Ultra physical acceptance

Run these checks on the installed version-code-15 review APK and record the results before closing Milestone 7:

1. Open **More > Orientation & training video** and confirm the Close button and recording controls remain clear of the camera cutout and Android gesture/navigation region.
2. Deny camera permission; confirm the fallback explanation remains visible and the rest of DOPE remains usable. Re-open and allow camera.
3. Confirm the ARCore state resolves. If install/update is offered, press it intentionally, return to DOPE and confirm the state refreshes.
4. Confirm Depth is shown as Supported, Unavailable or Not checked and that the long-range-rangefinder limitation is visible.
5. Move near metal, rotate the phone through north and calibrate the compass. Confirm heading, confidence, stability, pitch and roll update without large wrap jumps.
6. Hold the phone still in portrait and landscape. Confirm the horizon direction and inclination/roll signs are understandable and screen/system controls do not overlap.
7. Record at least 30 seconds with audio off. Stop, play back, seek, share the original video and export/open the Sensor CSV.
8. Enable audio and record again. Confirm microphone permission is requested at that moment only and playback contains audio. Deny once and confirm silent recording remains available after switching audio off.
9. Compare several playback timestamps with the synchronized heading/pitch/roll line and confirm it follows the recorded movement.
10. Record for at least 10 minutes with the screen on. Confirm no crash, camera loss, unacceptable temperature warning or corrupt output.

## Pending acceptance item

Burned-in informational-overlay MP4 export remains unimplemented. It must preserve the same safe overlay boundary and pass codec, duration, rotation, audio, storage and thermal tests before Milestone 7 can be marked complete.
