# Assumptions

- The repository root is `D:\1Dope`; the nested build pack is source material, not the final repository root.
- The approved design system is implemented in Milestone 1; profile, calculator and measurement values shown there are explicitly labelled shell/preview data.
- JDK and Android SDK components may be installed into an ignored local `.tools` directory for reproducible verification on this machine.
- English (South Africa) and metric defaults remain requirements.
- Common gong presets are nominal conveniences, not claims of a universal standard, and always require confirmation against the physical target.
- Wear OS, automatic shot counting, fixed-camera sequencing and pistol drill playback are future requirements and do not expand the completed Milestone 2 implementation scope.
- The future Wear OS baseline is the owner's Samsung Galaxy Watch7 44 mm paired with the Samsung Galaxy S25; layouts must still tolerate the smaller 40 mm display.
- Live-fire drill cues will use EARMOR Bluetooth hearing protection; dry-fire cues may use the phone speaker. Watch display/vibration are secondary outputs.
- The future fixed-camera baseline is the owner's rigid landscape stand with every user-marked stationary target retained in one wide frame.
- Milestone 2 stores target-distance provenance and the confirmed DOPE-inclusion flag, but camera measurement and automatic handoff execution remain Milestone 6.
- Room 2.8.4 is retained as the mature Android-only database line; Room 3 is not required for the current non-KMP module.
