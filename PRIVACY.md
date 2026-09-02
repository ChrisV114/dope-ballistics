# Privacy

Defaults: no account, analytics, advertising, hidden network calls, background location, or precise location storage. Data remains local unless the user explicitly invokes a documented online provider or export. Training exports strip precise location and sensitive metadata by default.

Profile exports exclude rifle serial numbers. Reference-atmosphere and saved-range coordinates are omitted unless the user explicitly includes precise location; each export records whether location was included.

Milestone 3 requests a one-time foreground location only after the user presses the capture control. Precise snapshot coordinates default to excluded. Pressing the weather control sends latitude and longitude to Open-Meteo; the UI shows attribution and cached age. No weather request occurs automatically or in the background.

Milestone 7 training recordings are local and user-started. Audio is off by default and requires a separate microphone permission. The synchronized Sensor CSV contains elapsed time, timestamp, magnetic heading, pitch, roll, accuracy and stability only; it contains no latitude, longitude, ballistic correction, target identity or aim point. Sharing always uses the Android chooser.
