# Release Build

Milestone 2 verifies debug assembly only. A signed APK/AAB is out of scope.

Before release: confirm package ID and branding, define keystore custody, run all quality gates, verify migrations/backup/restore, complete physical-device acceptance, document known limitations, and keep secrets outside the repository.


Milestone 2 debug artifact:

- Path: `app/build/outputs/apk/debug/app-debug.apk`
- SHA-256: `DDFB0650A9723F464B119E079215FDB89D65E07010DCDC0ABB8E6A668605EAD9`
- Signing: automatically generated debug signing only; not a release artifact.
