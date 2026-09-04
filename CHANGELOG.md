# Changelog

## 0.1.1

- Renamed the project to **AuthWeave — Per-Server Login Profiles**.
- Kept the `serverloginprofiles` mod ID and existing configuration keys for backward compatibility.

## 0.1.0

- Added independent official, offline, and custom Yggdrasil identities for each saved multiplayer server.
- Added third-party skin, cape, and elytra texture support.
- Added optional clear-text password storage with a vanilla-style export warning.
- Added English and Simplified Chinese translations.
- Restored the launcher identity when leaving multiplayer or opening an integrated world.
- Added standalone Fabric and NeoForge builds for every official Minecraft release from 1.21 through 26.2.

Security note: saved access tokens and optionally saved passwords are stored inside the corresponding `servers.dat`
entry. Review launcher exports before sharing them.
