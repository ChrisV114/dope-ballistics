# Security

- No API keys, signing material, private keys or local SDK paths may be committed.
- Imports must be schema-versioned, validated, non-executable and protected against path traversal.
- Sensitive local keys use Android Keystore.
- Backups and model packages require integrity checks.
- General logs exclude precise coordinates and serial references.
- Release signing is blocked pending owner decisions.

