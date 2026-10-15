# NEXUS

A client-side social mod for Minecraft Java Edition **1.21.11**, Fabric, Java 21.

## Current status

This repository is the initial implementation, not a finished Essential replacement.

Implemented in this starter:
- Right Shift opens the custom NEXUS menu.
- Dark red/pink UI with Overview, Friends, Worlds, Voice, Skins and Emotes pages.
- Local skin library with PNG validation and a hard limit of 10 saved skin files.
- GitHub Actions build workflow that uploads the built JAR as an artifact.

Not implemented yet:
- Public world hosting through an internet tunnel/relay.
- Friend accounts, friend requests and online presence.
- Real voice transport and proximity audio.
- Applying the selected local skin to the player model or syncing skins to other players.
- Network-synchronized emote animations and cosmetic rendering.

These require a trusted relay/service and additional networking/rendering integration. The UI intentionally labels unavailable network features instead of pretending they work.

## Build

Requires JDK 21 and Gradle 9.6.1 (or use the GitHub Actions workflow).

```bash
gradle --no-daemon build
```

The mod JAR will be under `build/libs/`.

## Install

Copy the built `nexus-*.jar` into the Minecraft `mods` folder alongside Fabric Loader and Fabric API for 1.21.11.

Open the NEXUS menu with **Right Shift**.

## Skin storage

Imported PNG files are saved locally to `.minecraft/nexus/skins`. The import dialog accepts 64x64 skins and legacy 64x32 skins, up to ten files.

## Roadmap

1. Verify the CI build and launch on Minecraft 1.21.11.
2. Add skin texture registration and player-skin override.
3. Add friend identity, invites and presence through a small relay backend.
4. Integrate proximity voice transport.
5. Implement secure tunnel hosting and connection lifecycle.
6. Add synced emotes, cosmetic rendering, and privacy controls.
