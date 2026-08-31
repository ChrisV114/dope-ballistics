# Security

- No API keys, signing material, private keys or local SDK paths may be committed.
- Imports must be schema-versioned, validated, non-executable and protected against path traversal.
- Profile schema v1 rejects unknown fields, unsupported units/schema versions, invalid target classes, unsafe target wording and unconfirmed DOPE distances. Payloads are limited to 10 MB.
- Duplicate imports require one explicit policy: duplicate with remapped UUIDs, merge non-conflicting rows, confirmed replace, or cancel. No silent overwrite is permitted.
- Sensitive local keys use Android Keystore.
- Backups and model packages require integrity checks.
- General logs exclude precise coordinates and serial references.
- The Open-Meteo adapter is HTTPS-only, has 10-second connect/read timeouts, uses no embedded secret, and is invoked only by an explicit user action.
- Weather pressure fields are parsed separately: `surface_pressure` is the station-pressure estimate and `pressure_msl` is metadata only.
- Environmental inputs are range-validated before persistence and calculation; missing optional sensors do not weaken manual input validation.
- Release signing is blocked pending owner decisions.
