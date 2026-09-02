# Camera Calibration

Milestone 6 provides CameraX capability inspection, app-private still capture, manual anchors and locally stored multi-distance calibration evidence for known static paper/cardboard or steel references. Camera-derived distance calculation remains unavailable; users must enter and confirm a separately measured distance.

The owner confirmed a Samsung Galaxy S25 Ultra. The preview requests only zoom values within the runtime range CameraX reports for the selected camera and provides 1×, 3×, 5×, 10× and maximum shortcuts where supported. Samsung Camera's advertised 100× digital mode is not assumed to be available to third-party CameraX capture. The UI displays both the active zoom and runtime range so physical acceptance can record the actual limit.

Calibration binds device, camera ID, focal length, resolution, aspect ratio, zoom and error metrics. Changing camera or zoom clears the current transient still and unsaved samples. It does not alter profiles, DOPE records or saved sessions. Marketing zoom is not calibration; changed configurations warn or invalidate saved evidence.

Milestone 1 adds a design-only target-range shell with:

- Manual measured width and height.
- Official IDPA cardboard target overall dimensions, 460.4 × 781.1 mm (18 1/8 × 30 3/4 in), sourced from the 2026 IDPA Rulebook.
- ISO A4 and A3 paper sizes.
- Common 150, 200 and 300 mm circular gong presets, explicitly labelled nominal rather than governing-standard dimensions.
- Custom gong width/height or diameter.

Every preset must expose its dimensions/source and require confirmation. The user should measure the physical target when practical. No distance result is produced by the current camera workflow.

## Requested fixed-camera target sequence

A future session may let the user pre-mark and order stationary targets before shooting. The approved physical baseline is the Samsung Galaxy S25 Ultra on the owner's rigid landscape stand with every target retained in one fixed wide frame. A configured and confirmed shot count moves a border and sequence-number highlight to the next saved target region; digital crop/zoom is optional and not required. The highlight is not a crosshair or aim point. The app cannot pan, select an unmarked target, identify an impact, infer a hit, calculate a correction or run Range Analyst while the string is active. Low-confidence counts pause for manual confirmation; impact review stays post-string.
