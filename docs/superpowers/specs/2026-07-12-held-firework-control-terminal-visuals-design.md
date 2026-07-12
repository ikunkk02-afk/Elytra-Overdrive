# Held Firework Overdrive, Control Terminal, and Flight Visuals V2 Design

## Scope

This design extends the existing Fabric 1.21.1 Elytra Overdrive implementation without replacing its flight-speed algorithm, aerial bombing logic, Breach collision prediction, enchanting-table support, or server-authoritative safety boundaries.

The work has three independently verifiable production phases:

1. Server-gated held-firework activation.
2. A responsive aerospace control terminal configuration screen.
3. Client-only flight visuals V2 and configurable FOV intensity.

Each production phase ends with `gradlew.bat clean test`, `gradlew.bat build`, and its requested commit. Documentation and final verification remain a fourth commit. Client runtime and dedicated-server smoke tests are added at the final verification gate because the UI and visual phases cannot be fully validated by JUnit alone.

## Existing System Boundaries

- `OverdriveFlightHandler` remains the server tick owner for movement, durability, deactivation speed limiting, and state synchronization.
- `FlightSpeedController` remains the only target-speed, acceleration, validation, and maximum-speed implementation.
- `FlightSessionState` remains the per-player server runtime owner and gains only preference and synchronization fields needed by the new activation path.
- Aerial bombing and Breach retain their current handlers, rules, items, packets, and collision behavior.
- All screens, rendering, local particles, and Minecraft client references stay under `src/client`.
- owo-lib remains required. Mod Menu remains optional and is not referenced from common sources.

## Held-Firework Activation Architecture

### Pure activation model

Add `FlightActivationSource` with four values:

- `NONE`
- `ENCHANTMENT`
- `HELD_FIREWORK`
- `BOTH`

Add `FlightActivationResolver`, a pure rules class that accepts already-observed facts rather than Minecraft objects. Its input records whether the Elytra has Overdrive, the player preference is enabled, either hand holds a firework rocket, and server policy allows this specific player. It returns the activation source.

The resolver intentionally does not evaluate the global `enableHighSpeedFlight` switch. This preserves the distinction between source eligibility and final activation: a local owner can be eligible for held-firework mode while the global high-speed system is disabled, but no source can produce actual high-speed movement until the outer flight gate passes.

### Server policy

Add the server-synchronized config option:

```text
allowHeldFireworkOverdrive=false
```

For each player, the authoritative policy is:

```text
allowHeldFireworkOverdrive
OR
server.isSingleplayerOwner(player.getGameProfile())
```

The verified Minecraft 1.21.1 Mojang-mapped API is `MinecraftServer.isSingleplayerOwner(GameProfile)`. The implementation must not use `!server.isDedicatedServer()`, because that would incorrectly authorize LAN guests.

### Final flight gate

`OverdriveFlightHandler` observes all gameplay facts on the server each tick. Actual activation requires:

- the player is alive;
- the player is not a spectator;
- the player is fall-flying;
- the chest slot contains a usable vanilla Elytra;
- `enableHighSpeedFlight` is true;
- the server-clamped effective multiplier is greater than `1.0`;
- the activation source is not `NONE`.

The enchantment fact continues to use the current enchantment lookup. The firework fact checks both hands for `Items.FIREWORK_ROCKET` and never mutates either `ItemStack`. Changing held items is observed on the next server tick. If the firework source disappears while the enchantment source remains, the source changes to `ENCHANTMENT` without deactivating movement.

### Player preference lifecycle

Add the local configuration option:

```text
enableHeldFireworkOverdrive=false
```

The local value may remain saved as `true` between servers. It is only a requested preference, never authorization. Each new server-side `FlightSessionState` starts with its preference set to `false`; the client sends its saved preference once the play connection is ready and when the user explicitly saves a changed draft.

On disconnect, client policy and confirmed flight state reset immediately. Joining a server that denies the mode therefore displays a locked toggle and produces no effect even when the saved local preference is `true`.

### Network protocol

Increase `RequiredClientPayload.CURRENT_PROTOCOL` from 2 to 3. Existing protocol mismatch handling remains mandatory and prevents new and old packet structures from being silently misread.

Add `HeldFireworkPreferenceC2SPayload(boolean enabled)`. It communicates only the player's requested preference. The receiver stores the requested value, but actual activation always re-evaluates server policy, real hands, Elytra usability, fall-flying state, global enablement, and multiplier validity.

Replace the current state payload contract with a versioned state containing:

- effective multiplier;
- active state;
- `FlightActivationSource`;
- server `allowHeldFireworkOverdrive` value;
- local-owner override state;
- accepted player preference.

The payload type identifier also advances from `state_v1` to `state_v2`, and every codec field and send/receive location advances together. The source is encoded as a bounded enum ordinal; invalid ordinals reset the client state rather than being trusted.

The effective multiplier and active source are synchronized when they change. Policy changes force synchronization even if flight is inactive, allowing an open UI to lock or unlock immediately.

### Administrator command

Register:

```text
/elytraoverdrive firework_mode <true|false>
```

The command node is visible so a non-administrator receives an explicit translated permission failure. The execution handler checks the verified `CommandSourceStack.hasPermission(4)` API before changing state. On success it calls the generated owo config setter, calls the public `OverdriveConfig.save()` method, then recalculates and synchronizes every online player immediately.

Success and permission messages use translation keys so English and Simplified Chinese clients receive clear feedback. No new permission library is introduced.

## Aerospace Control Terminal Architecture

### Integration strategy

Remove `@Modmenu` from the config model so it does not register the standard generated configuration screen. During client initialization, register the custom screen with the verified owo API:

```text
ConfigScreenProviders.register("elytra-overdrive", parent -> new OverdriveControlScreen(parent))
```

owo-lib already supplies the optional Mod Menu plugin entrypoint that consumes these providers. Elytra Overdrive therefore does not need a compile-time or runtime dependency on the Mod Menu API, and dedicated servers never load the screen classes.

Use a Java component-based `BaseOwoScreen<FlowLayout>`. owo-lib 0.12.15.4 provides the required flow, grid, stack, scroll, label, button, slider, animation, sizing, positioning, and surface APIs. Java composition is preferred over XML for dynamic server locking, live status replacement, and width-dependent layout changes.

No blur surface, custom shader, custom font, reflection, or rendering Mixin is added.

### File responsibilities

Client configuration code is split under `client/config`:

- `OverdriveControlScreen`: screen lifecycle, responsive shell, footer actions, confirmation overlays, and save notification.
- `ControlScreenState`: selected navigation section, responsive mode, transition timing, dirty state, and transient notification timing.
- `ConfigDraft`: snapshot, validation, default restoration, dirty comparison, and application of player-editable values.
- `NavigationSection`: the five navigation values and translation keys.
- `FlightSettingsPanel`: multiplier, FOV, held-firework preference, and activation cards.
- `BombingSettingsPanel`: read-only bombing status and server values.
- `BreachSettingsPanel`: read-only Breach values and range explanation.
- `VisualSettingsPanel`: visual preset and local visual toggles.
- `ServerPolicyPanel`: read-only server policy and connection role.
- `FlightStatusPanel`: live server-confirmed flight state plus reliable local speed and Elytra durability reads.
- `ControlPanelComponents`: shared terminal cards, labels, separators, toggles, numeric rows, and disabled-state styling.
- `ControlTerminalTheme`: colors, spacing, breakpoint, and shared surfaces.

No single screen file owns the content of every page.

### Draft and save semantics

Opening the screen creates a `ConfigDraft` from the current generated config wrapper. Editing UI controls mutates only the draft. The draft validates all numeric ranges before application.

Save applies only player-owned fields through generated setters, calls `ElytraOverdrive.CONFIG.save()`, and sends multiplier and held-firework preference packets once if those values changed. It does not send packets while a slider moves pixel by pixel.

Restore Defaults uses a two-step confirmation and resets only player-owned fields. It never changes synchronized server fields. Back closes immediately when clean and uses a confirmation overlay when dirty.

### Layout and responsiveness

The normal layout has a fixed-width navigation column, a flexible scrollable content column, and a status column. The footer remains outside the scrolling content.

At a scaled screen width below the theme breakpoint, the status column is removed and replaced by a compact horizontal status card above the central page. Navigation widths and padding shrink, the content remains vertically scrollable, and footer buttons divide available width. This mode targets 854×480 and high GUI scales without overlapping buttons or hiding the return action.

The screen rebuilds its component tree on resize using the same draft and navigation state, so unsaved changes survive resolution and GUI-scale changes.

### Server-controlled options

Bombing, Breach, and server policy pages render server-synchronized fields as text cards rather than interactive controls. The held-firework personal toggle is interactive only when the confirmed server policy or local-owner override allows it. A locally saved `true` value on a denied server remains represented in the draft but is rendered disabled with a `LOCKED` label and explanatory text; saving cannot make the server accept it.

The screen never sends a packet capable of writing server configuration.

### Live status data

The status panel reads activation, source, effective multiplier, policy, owner override, and accepted preference from `ClientOverdriveState`. Current velocity and Elytra durability are local read-only observations and are never labeled as authorization.

If no player or level exists, the panel displays `NO FLIGHT DATA` and never dereferences player state. Flight labels distinguish high-speed flight, vanilla gliding, and not flying.

## Flight Visuals V2

### Configuration

Preserve `showHighSpeedParticles` and `enableHighSpeedFov`. Add local fields:

- `visualPreset=BALANCED`
- `enableWingtipTrails=true`
- `enableSpeedLines=true`
- `enableSonicBoomRing=true`
- `reduceMotion=false`
- `fovIntensity=1.0`, constrained to `0.0` through `1.5`

The enum order is `PERFORMANCE`, `BALANCED`, `CINEMATIC`. Missing fields in an older JSON5 file use owo's model defaults, so existing configuration files remain compatible.

### Pure visual logic

Add `VisualIntensity`, a pure value computed from actual speed, effective multiplier, active state, preset, and reduce-motion state. It returns zero for non-finite speed, inactive flight, or multipliers at or below `1.0`. Otherwise it uses a clamped smooth ramp and exposes bounded trail length, speed-line count, and spawn cadence values.

Normal per-tick particle hard limits are:

- `PERFORMANCE`: 8
- `BALANCED`: 20
- `CINEMATIC`: 32

`reduceMotion` disables speed lines and sonic rings, reduces wingtip emission and FOV intensity, and never increases a preset's limits.

### Wingtip trails

`OverdriveVisuals` derives a stable orthonormal basis from player look direction and a world-up fallback. Approximate wingtip positions are computed from player position plus forward, right, and up offsets. This makes both trails rotate with the player rather than remain aligned to world axes.

Short Dust or DustColorTransition point sequences extend behind the wingtip positions. Length and cadence use `VisualIntensity`; a per-tick budget stops emission at the preset limit. First-person offsets remain behind and outside the center line.

### Speed lines

Speed-line origins are sampled in an annulus around the flight axis so they remain away from the crosshair. Each line is a short bounded series of cyan-white Dust points extending opposite the flight direction. Frequency and length rise smoothly but remain inside the shared per-tick budget. Performance mode disables them by default; reduce-motion always disables them.

### Sonic-boom ring

Add a pure `SonicBoomState` state machine with:

- trigger speed: 4.0 blocks/tick;
- reset speed: 3.2 blocks/tick;
- cooldown: 60 client ticks;
- ring particle count: 24, capped at 28.

Crossing the trigger while armed emits one ring and disarms the state. Cooldown alone does not rearm it; speed must first fall to or below the reset threshold. The ring plane uses two perpendicular vectors derived from the flight direction and contains no damage, knockback, block changes, or networking.

The state resets on disconnect, missing world/player data, and loss of confirmed high-speed activation.

### FOV

Keep the current smoothed client FOV path. Its target becomes:

```text
existing speed-based boost × fovIntensity × preset factor × reduce-motion factor
```

Preset factors are conservative for Performance, unchanged for Balanced, and moderately stronger for Cinematic. `fovIntensity=0` produces no extra FOV. Reduce Motion substantially lowers the target. The interpolation continues to return smoothly to zero after flight stops and never writes the vanilla FOV option.

Both particles and FOV require the latest server-confirmed active state, effective multiplier, and non-`NONE` activation source. Merely holding a client-visible firework never starts visual effects.

## Testing Strategy

### Held-firework rules

Pure resolver tests cover denied policy, accepted dedicated-server policy, disabled preference, absent firework, local-owner override, LAN guest denial, independent enchantment activation, `BOTH`, source change after removing the firework, and the distinction between source eligibility and global final activation.

Session and policy tests cover safe preference defaults, malicious `true` preference remaining ineffective under denied policy, synchronization dirtiness after policy/source changes, and hand checks that do not mutate stack counts. Minecraft integration remains covered by source-contract tests for the real server owner and item APIs.

### UI logic

JUnit tests cover draft snapshot/defaults/validation/dirty state, navigation state, permission-to-edit mapping, value formatting, responsive breakpoint selection, and locked local preference behavior. Full rendering is verified manually at 854×480, 1280×720, 1920×1080, and multiple GUI scales.

### Visual rules

Pure tests cover non-finite inputs, inactive state, multiplier `1.0`, all three hard caps, reduce-motion suppression, first threshold crossing, cooldown rejection, hysteresis rejection, rearming below reset, and zero FOV intensity.

### Regression and runtime verification

Each production phase runs clean tests and build before commit. Final verification includes:

- `git diff --check`
- `gradlew.bat clean test`
- `gradlew.bat build`
- `gradlew.bat runServer` dedicated-server startup smoke test
- `gradlew.bat runClient` client startup and configuration-screen smoke test
- packaged JAR audit for accidental client classes in common entrypoints
- final status, local SHA, remote SHA, and push confirmation

Gameplay behavior, responsive visual quality, active firework use, LAN ownership, Sodium, and Iris remain explicit manual test items because they require interactive environments or multiple clients.

## Failure Handling and Compatibility

- Invalid client multipliers retain the last safe selection; invalid preference packets cannot grant policy.
- Invalid S2C enum values reset client state instead of displaying an invented source.
- A missing play connection prevents preference sends without crashing; join performs the initial sync.
- Policy changes recalculate every player's state immediately and cannot leave an old held-firework authorization active.
- Older local config files gain safe defaults for new fields.
- Protocol 2 clients receive the existing explicit incompatibility disconnect instead of parsing protocol 3 payloads.
- owo's blur surface and advanced shader paths are intentionally unused for Sodium/Iris compatibility.
- Approximate wingtip coordinates do not track renderer animation bones; this is an accepted compatibility limitation that avoids player-renderer replacement.

## Commit Sequence

After this design commit, production work uses the requested commits:

1. `feat: add server-gated held firework overdrive`
2. `feat: add aerospace control terminal`
3. `feat: improve overdrive flight visuals`
4. `docs: document firework flight and visual redesign`

The final push targets `origin/main` only after all verification commands succeed.
