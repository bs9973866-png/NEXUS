# NEXUS

A client-side social GUI for Minecraft Java **1.21.11**, Fabric, Java 21.

NEXUS has no separate NEXUS account or registration flow. It uses your existing Minecraft nickname and stores the contact list/settings locally.

## Included in this build

- Right Shift opens the NEXUS overlay.
- NEXUS button is injected into the main menu and pause menu.
- Custom dark, red-accented UI with a compact uniform font face instead of the standard default UI font.
- Russian and English interface toggle, saved locally.
- Local nickname contact list (up to 100 entries, no registration).
- Friend-request message and direct messages sent using the current server's `/msg` command.
- Local outgoing-message history per contact. Replies are handled by the server and appear in normal Minecraft chat.
- Skin vault stores up to 10 validated 64x64 or legacy 64x32 PNG files.
- Selected skin face preview inside profile and skin library.
- GitHub Actions builds the mod JAR and sources JAR.

## Important limits

This is a **client-only** mod. The friend list is local, not a global account/presence service. Friend requests and messages can only reach users on the current server when that server supports `/msg`; cross-server messaging and online presence are not configured. Incoming replies are shown by Minecraft's normal chat and are not mirrored into the NEXUS thread history.

Imported skins are previewed and stored locally. Selecting one does not change the official Minecraft session skin or force other players to see it. That requires a skin service/server integration. Public world hosting/tunnelling, voice transport, and synchronized emotes/cosmetics are not included in this client-only build and are presented as future service integrations rather than working buttons.

## Build

Use JDK 21 and Gradle 9.6.1, or open the repository's **Actions** tab and download the artifact from the latest successful **Build NEXUS** run.

```bash
gradle --no-daemon clean build
```

The runtime JAR is in `build/libs/nexus-0.1.0.jar`. The `nexus-0.1.0-sources.jar` is source code for developers and is not the file to install.

## Install

Copy only the runtime `nexus-0.1.0.jar` to the Minecraft instance's `mods` folder alongside Fabric Loader and Fabric API for Minecraft 1.21.11.

## Local files

- Skin PNGs: `.minecraft/nexus/skins`
- Contacts, chat history, and language: `.minecraft/config/nexus/`
