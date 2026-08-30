# Release Build

Milestone 0 verifies debug assembly only. A signed APK/AAB is out of scope.

Before release: confirm package ID and branding, define keystore custody, run all quality gates, verify migrations/backup/restore, complete physical-device acceptance, document known limitations, and keep secrets outside the repository.


Milestone 0 debug artifact:

- Path: `app/build/outputs/apk/debug/app-debug.apk`
- SHA-256: `66B7B07E2155FF3079175AA8491CD0A4FCE74FD5F3BE4857649571A1440233DF`
- Signing: automatically generated debug signing only; not a release artifact.
