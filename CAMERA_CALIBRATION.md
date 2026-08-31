# Camera Calibration

Camera implementation remains deferred to Milestone 6. Known static paper/cardboard or steel targets only. Calibration must bind device, physical lens, resolution, aspect ratio, zoom and error metrics. Marketing zoom is not calibration; changed configurations warn or invalidate the result.

Milestone 1 adds a design-only target-range shell with:

- Manual measured width and height.
- Official IDPA cardboard target overall dimensions, 460.4 × 781.1 mm (18 1/8 × 30 3/4 in), sourced from the 2026 IDPA Rulebook.
- ISO A4 and A3 paper sizes.
- Common 150, 200 and 300 mm circular gong presets, explicitly labelled nominal rather than governing-standard dimensions.
- Custom gong width/height or diameter.

Every preset must expose its dimensions/source and require confirmation. The user should measure the physical target when practical. No distance result may be produced until Milestone 6 implements calibrated angular geometry and uncertainty.

## Requested fixed-camera target sequence

A future session may let the user pre-mark and order stationary targets before shooting. The approved physical baseline is the Samsung S25 on the owner's rigid landscape stand with every target retained in one fixed wide frame. A configured and confirmed shot count moves a border and sequence-number highlight to the next saved target region; digital crop/zoom is optional and not required. The highlight is not a crosshair or aim point. The app cannot pan, select an unmarked target, identify an impact, infer a hit, calculate a correction or run Range Analyst while the string is active. Low-confidence counts pause for manual confirmation; impact review stays post-string.
