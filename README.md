# Server Login Profiles

Server Login Profiles is a standalone, client-side Minecraft mod for assigning a different identity to every saved
multiplayer server. It has no dependency on Auth Me, ViaFabricPlus, Fabric API, or another authentication mod.

## Features

- Official Microsoft, offline, and custom Yggdrasil identities per saved server.
- Third-party skins, capes, and elytra textures through the selected Yggdrasil session service.
- Login settings integrated into Minecraft's add/edit server screen.
- Credentials stored with that vanilla server entry in `servers.dat`, so launcher personal-data export behavior is
  predictable.
- Optional clear-text password storage guarded by a vanilla-style full-screen warning. Passwords are omitted by
  default; access tokens are stored to support automatic reconnects.
- English and Simplified Chinese language files, with no fixed user-interface prose embedded in Java code.
- No authentication, disk access, or version detection on the repeated-connect hot path.

## Supported versions

Every official Java Edition release from 1.21 through 26.2 is built separately for Fabric and NeoForge. See
[the version matrix](docs/VERSION_MATRIX.md) for exact loader versions and beta-channel notes.

PCL CE users can follow the [Chinese manual regression checklist](docs/TESTING.zh-CN.md).

Install the jar matching both the exact Minecraft version and mod loader. No additional mod dependency is required.

## Security

The saved server entry contains an access token. If clear-text password storage is enabled, it also contains the
password. Launchers may include `servers.dat` when exporting saved servers or personal game data. Inspect exports
before sharing them and never publish unredacted `servers.dat`, logs, or crash archives.

Authentication requests do not follow redirects. Fixed error messages are local language keys; arbitrary HTTP
response bodies are not copied into the UI or logs.

## Building

Build the default 26.2 target:

```powershell
.\gradlew.bat clean build
```

Build one configured target:

```powershell
.\gradlew.bat clean build -Ptarget_version=1.21.11
```

Build and collect the complete matrix under `dist/<minecraft-version>`:

```powershell
.\scripts\build-matrix.ps1
```

The project is licensed under MIT. Contributions must be clean-room work compatible with that license.
