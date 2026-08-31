# Release Build

Milestone 2 verifies debug assembly only. A signed APK/AAB is out of scope.

Before public release: reconfirm branding, define keystore custody and distribution, run all quality gates, verify migrations/backup/restore, complete physical-device acceptance, document known limitations, and keep secrets outside the repository. The owner-approved application ID is `za.co.bdstudio.dope`.


Post-Milestone-2 owner-review debug artifact:

- Path: `app/build/outputs/apk/debug/app-debug.apk`
- Application ID: `za.co.bdstudio.dope`
- SHA-256: `3BE1F942774758A0C5031BDBFA3F21F887E2043370869B49A0B4FDA48E15D22F`
- Signing: automatically generated debug signing only; not a release artifact.
