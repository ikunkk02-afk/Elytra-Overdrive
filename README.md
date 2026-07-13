# Elytra Overdrive

High-speed Elytra flight, aerial bombing, trident breaching, and advanced flight controls for Fabric.

[简体中文](README_zh_CN.md)

- Minecraft 1.21.1
- Fabric
- Java 21
- MIT License

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [How to Use](#how-to-use)
- [Configuration](#configuration)
- [Server Administration](#server-administration)
- [Building from Source](#building-from-source)
- [Development Setup](#development-setup)
- [Useful Gradle Commands](#useful-gradle-commands)
- [Project Structure](#project-structure)
- [Compatibility](#compatibility)
- [Troubleshooting](#troubleshooting)
- [License](#license)

## Overview

Elytra Overdrive is a Fabric mod for Minecraft 1.21.1 that expands vanilla Elytra gameplay. It adds the Overdrive high-speed flight enchantment, enchantment-table support for Elytra, an optional held-firework flight mode, aerial TNT bombing, the Breach trident enchantment, an advanced control terminal, and configurable flight visuals.

Gameplay decisions are server-authoritative. The server validates flight activation, effective speed, bombing, block breaking, durability use, permissions, and safety limits; multiplayer clients cannot bypass server settings.

## Features

### Overdrive Enchantment

- `Overdrive` (`超载`) is an Elytra enchantment available through the enchanting table, enchanted books, and anvils.
- While gliding, it accelerates the player according to their selected multiplier.
- The final multiplier never exceeds the server maximum.
- The standard player range reaches `100×`; an explicit performance warning unlocks the experimental `100×–200×` range.
- An Elytra without Overdrive keeps vanilla flight behavior unless the optional held-firework mode is authorized and enabled.

### Enchantable Elytra

The mod allows a vanilla Elytra to participate in the normal enchanting-table flow:

1. Put the Elytra in an enchanting table.
2. Add lapis lazuli.
3. Select one of the normal enchantment offers.

This does not force every enchantment to support Elytra. Treasure enchantments still follow vanilla rules, and Overdrive is not guaranteed on every roll.

### Held Firework Overdrive

Held Firework Overdrive is an optional second source of high-speed flight. It activates only when all applicable conditions are met:

- The player is gliding with a usable Elytra.
- A Firework Rocket is held in either hand.
- The player's personal option is enabled.
- The server allows the feature, or the player is the owner of the integrated singleplayer server.

Merely holding a rocket does not consume it. Actively using the rocket still follows vanilla consumption and boosting rules. Both the player option and dedicated-server permission default to `false`, and the server multiplier cap always applies.

### Aerial TNT Bombing

While gliding, hold Flint and Steel in the main hand and TNT in the off hand:

- Right-click once to drop one primed TNT.
- Hold right-click to continue bombing at the server-configured interval.
- Release right-click to stop.

Survival players consume one TNT and one point of Flint and Steel durability for each successfully spawned bomb. Creative players do not consume either item. TNT spawning, fuse time, velocity, inventory use, and frequency limits are controlled by the server.

### Trident Breach

`Breach` (`破阵`) is a three-level enchantment for the vanilla Trident:

| Level | Cross-section |
|---|---|
| Breach I | Center block (`1×1`) |
| Breach II | Five-block cross |
| Breach III | Full `3×3` |

To activate Breach, the player must be Elytra-gliding, hold a Breach-enchanted Trident in the main hand, and reach the configured minimum speed. The server performs predictive path sampling and pre-collision clearing with a per-tick budget, without force-loading chunks.

Unbreakable and administrator blocks are protected, and Block Entities are protected by default. Trident durability cost scales with the hardness of each successfully broken block.

### Flight Visuals

Client-side flight feedback includes:

- Wingtip Trails
- Speed Lines
- Sonic Boom Ring
- Dynamic FOV

The `Performance`, `Balanced`, and `Cinematic` presets apply hard particle limits and different FOV factors. Individual effects can be disabled, and Reduce Motion lowers particle and FOV intensity. Visual settings never change the server's speed calculation.

### Aerospace Control Terminal

The custom control terminal contains five sections:

- Flight
- Bombing
- Breach
- Visual
- Server

It displays confirmed flight state, activation source, selected multiplier, server cap, current speed, and Elytra durability. Player and visual settings can be edited locally; bombing, Breach, and server policy values are read-only for ordinary players.

Mod Menu provides a convenient way to open the terminal, but it is not a required dependency.

### Experimental Extreme Speed

- Standard player range: up to `100×`
- Experimental range: up to `200×`
- Server default maximum: `100×`
- Server configurable maximum: `200×`
- Experimental Extreme Speed is disabled by default

Speeds above `100×` can significantly increase chunk loading, world generation, memory usage, network traffic, client frame time, and server tick load. Actual results depend on hardware, whether the world is pregenerated, server performance, chunk-loading speed, and network conditions. `200×` is experimental and is not guaranteed to be stable on every server.

## Requirements

### Required

| Component | Version |
|---|---|
| Minecraft | `1.21.1` |
| Fabric Loader | `0.19.3` or newer compatible version |
| Fabric API | `0.116.13+1.21.1` or newer compatible version |
| owo-lib | `0.12.15.4+1.21` or newer compatible version |
| Java | `21` or newer compatible runtime |

### Optional

| Component | Version | Purpose |
|---|---|---|
| Mod Menu | `11.0.4` or newer compatible version | Convenient access to the configuration screen |

For multiplayer, Elytra Overdrive must be installed on both the client and the server. Mod Menu is not required on dedicated servers, and the mod remains loadable without Mod Menu.

## Installation

### Client

1. Install Minecraft 1.21.1.
2. Install Fabric Loader for Minecraft 1.21.1.
3. Download Fabric API and owo-lib for Minecraft 1.21.1.
4. Optionally install Mod Menu.
5. Put the Elytra Overdrive release JAR and required dependency JARs in the Minecraft `mods` directory:
   - Windows: `%APPDATA%\.minecraft\mods`
   - Linux: `~/.minecraft/mods`
   - macOS: `~/Library/Application Support/minecraft/mods`
6. Start the Fabric profile.

Use the built release JAR, not the repository source ZIP, sources JAR, or development JAR.

### Dedicated Server

1. Install a Fabric Server for Minecraft 1.21.1.
2. Put Fabric API, owo-lib, and the Elytra Overdrive release JAR in the server's `mods/` directory.
3. Start the server once to generate configuration files.
4. Stop the server and edit `config/elytra-overdrive.json5` if server policy changes are required.
5. Restart the server.

Mod Menu is not required on dedicated servers. Server settings always take precedence over player preferences.

## How to Use

### High-Speed Flight with Overdrive

1. Obtain an Elytra.
2. Enchant it with Overdrive using an enchanting table, enchanted book, or anvil.
3. Equip the Elytra and start gliding.
4. Choose a multiplier greater than `1.0×` in the control terminal.

### High-Speed Flight with a Held Firework

1. A server administrator enables held-firework mode, or the player uses their own singleplayer world.
2. Enable the personal Held Firework Overdrive option.
3. Hold a Firework Rocket in either hand.
4. Equip a usable Elytra and start gliding.

### Bombing

1. Equip an Elytra and start gliding.
2. Hold Flint and Steel in the main hand.
3. Hold TNT in the off hand.
4. Right-click once or hold right-click to bomb continuously.

### Breach

1. Obtain a Trident and enchant it with Breach.
2. Hold it in the main hand while Elytra-gliding.
3. Reach the configured minimum speed.
4. Aim toward breakable blocks.

## Configuration

The configuration file is `config/elytra-overdrive.json5`. Player settings are editable in the control terminal. Server-authoritative values are synchronized from the server and shown as read-only to ordinary players.

### Flight and Visual Settings

| Option | Default | Description |
|---|---:|---|
| `playerSelectedMultiplier` | `2.0` | Player-requested flight multiplier; persistent range `1.0–200.0`, then capped by the server. |
| `enableHeldFireworkOverdrive` | `false` | Personal request to use held-firework high-speed flight. |
| `enableExperimentalExtremeSpeed` | `false` | Unlocks the warned `100×–200×` local UI range; this never grants server authority. |
| `showHighSpeedParticles` | `true` | Master switch for local high-speed particles. |
| `enableHighSpeedFov` | `true` | Enables the local dynamic FOV effect. |
| `visualPreset` | `BALANCED` | Particle budget and FOV preset: `PERFORMANCE`, `BALANCED`, or `CINEMATIC`. |
| `enableWingtipTrails` | `true` | Shows wingtip trails while confirmed high-speed flight is active. |
| `enableSpeedLines` | `true` | Shows peripheral speed lines when permitted by the selected preset. |
| `enableSonicBoomRing` | `true` | Enables the threshold-based sonic boom ring when permitted by the preset. |
| `reduceMotion` | `false` | Reduces particle motion and FOV intensity and disables speed lines and the sonic ring. |
| `fovIntensity` | `1.0` | Additional FOV intensity; allowed range `0.0–1.5`. |

### Server Flight Settings

| Option | Default | Description |
|---|---:|---|
| `enableHighSpeedFlight` | `true` | Master server switch for all high-speed flight. |
| `allowHeldFireworkOverdrive` | `false` | Allows players on the server to request held-firework mode. |
| `serverMaximumMultiplier` | `100.0` | Maximum effective multiplier for new configurations; allowed range `1.0–200.0`. Existing configured values are preserved. |
| `extraDurabilityDamage` | `true` | Enables additional Elytra durability use during high-speed flight. |
| `extraDurabilityIntervalTicks` | `40` | Ticks between additional durability attempts; allowed range `10–200`. |
| `elytraEnchantability` | `10` | Enchantment quality used for Elytra in the enchanting table; allowed range `1–30`. |

### Bombing Settings

| Option | Default | Description |
|---|---:|---|
| `enableBombing` | `true` | Enables aerial TNT bombing. |
| `bombingIntervalTicks` | `12` | Server ticks between bombs while the use button is held; allowed range `4–100`. |
| `bombFuseTicks` | `80` | Fuse duration of dropped TNT; allowed range `20–200`. |
| `bombHorizontalInertia` | `0.70` | Fraction of player horizontal velocity inherited by TNT; allowed range `0.0–1.5`. |

### Breach Settings

| Option | Default | Description |
|---|---:|---|
| `enableTridentBreach` | `true` | Enables the Breach system. |
| `minimumBreachSpeed` | `1.2` | Minimum real movement speed in blocks per tick; allowed range `0.3–10.0`. |
| `maximumBreachBlocksPerTick` | `32` | Maximum unique positions inspected per player per server tick; allowed range `1–128`. |
| `breachDurabilityMultiplier` | `1.0` | Scales durability cost by block hardness; allowed range `0.1–10.0`. |
| `breachDropsBlocks` | `true` | Generates loot-table drops after permitted breaks. |
| `protectBlockEntitiesFromBreach` | `true` | Protects containers and other Block Entities. |

## Server Administration

Dedicated servers keep Held Firework Overdrive disabled by default. Permission level 4 is required for the implemented administrator command:

```mcfunction
/elytraoverdrive firework_mode true
/elytraoverdrive firework_mode false
```

- `true` permits players to enable their personal held-firework option.
- `false` revokes that permission.
- A successful change is saved to the owo configuration and synchronized immediately to online players.
- In an integrated singleplayer server, the local world owner is authorized even when the server option is `false`. Other players joining through LAN do not receive that owner override.

Other server rules are managed through `config/elytra-overdrive.json5` and take effect according to owo-lib's configuration behavior.

## Building from Source

The project uses the included Gradle Wrapper; installing Gradle separately is unnecessary. Install Git and a JDK 21 distribution such as Eclipse Temurin, OpenJDK, or Microsoft Build of OpenJDK.

### 1. Verify Java

```bash
java -version
```

The output should report Java 21.

### 2. Clone

```bash
git clone https://github.com/ikunkk02-afk/Elytra-Overdrive.git
cd Elytra-Overdrive
```

### 3. Build on Windows

```powershell
.\gradlew.bat clean build
```

### 4. Build on Linux or macOS

```bash
chmod +x gradlew
./gradlew clean build
```

### 5. Output

The remapped release JAR is written to:

```text
build/libs/elytra-overdrive-<version>.jar
```

For version 1.0.0, the release file is `build/libs/elytra-overdrive-1.0.0.jar`. Do not distribute `*-sources.jar` or any development/test JAR.

### 6. Run Tests

Windows:

```powershell
.\gradlew.bat clean test
```

Linux/macOS:

```bash
./gradlew clean test
```

### 7. Full Verification

Windows:

```powershell
.\gradlew.bat clean test
.\gradlew.bat build
```

Linux/macOS:

```bash
./gradlew clean test
./gradlew build
```

## Development Setup

IntelliJ IDEA and VS Code are both suitable; another IDE with Gradle and Java support can also be used.

1. Clone the repository.
2. Open or import it as a Gradle project.
3. Select JDK 21 for the project and Gradle toolchain.
4. Allow Gradle to download dependencies.
5. Wait for Fabric Loom to finish preparing the development environment.

Launch the development client:

```powershell
.\gradlew.bat runClient
```

Launch the development server:

```powershell
.\gradlew.bat runServer
```

On Linux/macOS, replace `gradlew.bat` with `./gradlew`. A development server may require accepting Minecraft's EULA on its first run.

## Useful Gradle Commands

| Windows command | Purpose |
|---|---|
| `.\gradlew.bat build` | Build and test the mod |
| `.\gradlew.bat test` | Run unit and resource-contract tests |
| `.\gradlew.bat clean` | Remove build outputs |
| `.\gradlew.bat runClient` | Launch the development client |
| `.\gradlew.bat runServer` | Launch the development server |
| `.\gradlew.bat tasks` | List available Gradle tasks |

On Linux/macOS, replace `.\gradlew.bat` with `./gradlew`.

## Project Structure

| Path | Purpose |
|---|---|
| `src/main/java` | Common and server-authoritative gameplay, networking, configuration, commands, and mixins |
| `src/client/java` | Client UI, input, particles, visual state, and FOV logic |
| `src/main/resources` | Fabric metadata, data-driven enchantments, tags, mixin configs, icon, and translations |
| `src/client/resources` | Client-only mixin configuration |
| `src/test/java` | Pure logic and resource-contract unit tests |
| `docs/implementation` | Detailed implementation notes and manual test checklists |

## Compatibility

- Supports Minecraft 1.21.1 with Fabric and Java 21.
- Does not support Forge or NeoForge and does not claim compatibility with other Minecraft versions.
- Multiplayer requires the mod on both client and server because the server performs a required protocol check.
- Server authority controls speed, permissions, bombing, block destruction, durability, and safety caps.
- Client screens and visuals are isolated in the Fabric client source set, so dedicated servers do not load them.
- The custom configuration screen avoids shader requirements and is designed to remain usable with common Fabric rendering optimization mods. Compatibility with every mod or modpack is not guaranteed.
- Mods that replace the final server flight-physics stage or intercept block breaking may require compatibility testing.

## Troubleshooting

### Game crashes on startup

- Confirm Minecraft is exactly 1.21.1.
- Confirm Java 21 is being used.
- Confirm Fabric API and owo-lib are installed and match Minecraft 1.21.1.
- Confirm the installed Fabric Loader meets the minimum version.
- Remove duplicate or wrong-version JARs from the `mods` folder.

### Mod does not appear in Mod Menu

Mod Menu is optional. Its absence does not mean Elytra Overdrive failed to load. Confirm the mod JAR is in the correct `mods` folder and inspect the game log. Installing a compatible Mod Menu version provides convenient access to the control terminal.

### Held Firework Overdrive does not work

- Confirm the server permits held-firework mode or you are the integrated-server owner.
- Enable the personal option in the control terminal.
- Start Elytra gliding with a usable Elytra.
- Hold a Firework Rocket in either hand.
- Select a multiplier greater than `1.0×`.

### High-speed flight does not activate

- Confirm the Elytra has Overdrive, or all held-firework conditions are satisfied.
- Confirm the selected multiplier is greater than `1.0×`.
- Confirm the server has `enableHighSpeedFlight=true`.
- Confirm the Elytra is not too damaged to fly.

### Breach does not break blocks

- Confirm the main-hand Trident has Breach.
- Confirm the player is gliding above the configured minimum speed and aiming along the movement direction.
- Check whether the block is protected, contains a Block Entity, exceeds the level's hardness limit, or is inside spawn protection.
- Confirm `enableTridentBreach=true`.

### Build fails

First confirm Java 21:

```bash
java -version
```

Then rerun with a stack trace.

Windows:

```powershell
.\gradlew.bat clean build --stacktrace
```

Linux/macOS:

```bash
./gradlew clean build --stacktrace
```

## License

This project is licensed under the [MIT License](LICENSE). Copyright (c) 2026 寿云.
