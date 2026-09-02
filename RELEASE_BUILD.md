# Release Build

Milestones through 6 verify private signed review APK assembly only. A public release APK/AAB is out of scope.

Before public release: reconfirm branding, define keystore custody and distribution, run all quality gates, verify migrations/backup/restore, complete physical-device acceptance, document known limitations, and keep secrets outside the repository. The owner-approved application ID is `za.co.bdstudio.dope`.


Post-Milestone-2 owner-review debug artifact:

- Path: `app/build/outputs/apk/debug/app-debug.apk`
- Application ID: `za.co.bdstudio.dope`
- SHA-256: `3BE1F942774758A0C5031BDBFA3F21F887E2043370869B49A0B4FDA48E15D22F`
- Signing: automatically generated debug signing only; not a release artifact.

Milestone-3 local debug artifact:

- Path: `app/build/outputs/apk/debug/app-debug.apk`
- Application ID: `za.co.bdstudio.dope`
- Version name: `0.3.0-m3`
- SHA-256: `7B65617B47CA2AC607D3036DB11ED8FBFBA422876626E8E867910EBAAD2358F8`
- Signing: automatically generated debug signing only; not a release artifact.

Milestone-4 local debug artifact:

- Path: `app/build/outputs/apk/debug/app-debug.apk`
- Application ID: `za.co.bdstudio.dope`
- Version name: `0.4.0-m4`
- SHA-256: `F7EBB634E05D5B507EC33B23030478F546414216F27137ECCD2BA249CAEC9EF7`
- Signing: automatically generated debug signing only; not a release artifact.

Milestone-5 local debug artifact:

- Path: `app/build/outputs/apk/debug/app-debug.apk`
- Application ID: `za.co.bdstudio.dope`
- Version name: `0.5.0-m5`
- SHA-256: `256CAD384B7F56C86BDA9A97B5BA997B04D63C1905C20AE2ABD9409B146BC2EC`
- Signing: automatically generated debug signing only; not a release artifact.

Milestone-5 review-fix local debug artifact:

- Path: `app/build/outputs/apk/debug/app-debug.apk`
- Application ID: `za.co.bdstudio.dope`
- Version name: `0.5.1-m5-review`
- SHA-256: `002F6CA1141ED7A5179CB92665174D07452812FFAF80CE4339C121B83074AFE3`
- Signing: automatically generated debug signing only; not a release artifact.

Milestone-5 test-profile review artifact:

- Path: `app/build/outputs/apk/debug/app-debug.apk`
- Application ID: `za.co.bdstudio.dope`
- Version name: `0.5.2-m5-review`
- SHA-256: `9D0C1C994A17DF44A2278217CB7146376FC707040A1F3C878E7F936A0D0D74CB`
- Signing: automatically generated debug signing only; not a release artifact.

Milestone-5 starter-profile repair artifact:

- Path: `app/build/outputs/apk/debug/app-debug.apk`
- Application ID: `za.co.bdstudio.dope`
- Version name: `0.5.3-m5-review`
- SHA-256: `578CC6FDA66C7AB0FE4B717083FD33EC2A1D68FB6E018CC71B47240BCEC3069C`
- Signing: automatically generated debug signing only; not a release artifact.

Milestone-5 calculator-usability review artifact:

- Path: `app/build/outputs/apk/debug/app-debug.apk`
- Application ID: `za.co.bdstudio.dope`
- Version name: `0.5.4-m5-review`
- SHA-256: `2ABCEF9D23097B6AB90AB1662C117E60ECD5871E55C1B3C12353FA4422912F84`
- Signing: automatically generated debug signing only; not a release artifact.

Milestone-5 previous-DOPE review artifact:

- Path: `app/build/outputs/apk/debug/app-debug.apk`
- Application ID: `za.co.bdstudio.dope`
- Version code: `11`
- Version name: `0.5.6-m5-review`
- SHA-256: `31EDB58266BD720EB784EC35F4BA894869652165140CAEF49B3FF397188F6501`
- Signing certificate SHA-256: `5AD535D1D96C0A79A6D77BE223BCB6D659CCE53180C3910C48DD456D664232A3`
- Signing: approved private review key; intended to update an installed review build signed by the same certificate.

Milestone-6 camera-calibration review artifact:

- Path: `app/build/outputs/apk/debug/app-debug.apk`
- Application ID: `za.co.bdstudio.dope`
- Version code: `12`
- Version name: `0.6.0-m6-review`
- SHA-256: `76CB7E02C5406FC1B1D52AB407A8650DB824FBDD9204F372005CA9B7F21FA9E7`
- Signing certificate SHA-256: `5AD535D1D96C0A79A6D77BE223BCB6D659CCE53180C3910C48DD456D664232A3`
- Signing: approved private review key; intended to update an installed review build signed by the same certificate.

Milestone-6 S25 Ultra zoom review artifact:

- Path: `app/build/outputs/apk/debug/app-debug.apk`
- Application ID: `za.co.bdstudio.dope`
- Version code: `13`
- Version name: `0.6.1-m6-review`
- SHA-256: `E6F8EB09E1B715AF1015046183C5483ADBF7EC9E670067755219483DB92A36E6`
- Signing certificate SHA-256: `5AD535D1D96C0A79A6D77BE223BCB6D659CCE53180C3910C48DD456D664232A3`
- Signing: approved private review key; intended to update the installed version-code-12 review build without a clean install.
