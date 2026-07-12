# Held Firework, Control Terminal, and Flight Visuals Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add server-gated held-firework high-speed flight, a responsive aerospace control terminal, and bounded high-speed flight visuals without regressing Overdrive, bombing, Breach, or dedicated-server safety.

**Architecture:** Keep movement, caps, durability, and authorization in the existing server tick path. Add pure activation and visual rules around that path, synchronize only preference intent and confirmed server state, and build a client-only Java-composed owo screen backed by a validated local draft.

**Tech Stack:** Minecraft 1.21.1, Fabric Loader 0.19.3, Fabric API 0.116.13+1.21.1, Java 21, Mojang official mappings, owo-lib 0.12.15.4+1.21, optional Mod Menu 11.0.4, JUnit 5.11.4.

## Global Constraints

- Work directly on `main` in `D:\elytra-overdrive-template-1.21.1`; preserve the existing remote history and MIT license.
- Do not replace `FlightSpeedController`, bombing behavior, Breach collision prediction, enchanting-table support, or existing resource IDs.
- Gameplay authorization, final multiplier, movement, speed caps, and durability remain server-authoritative.
- Client packets carry only local preference intent; clients never assert held items, authorization, activation source, or final multiplier.
- All screen, renderer, particle, and `net.minecraft.client` references remain under `src/client`.
- Mod Menu remains optional; owo-lib remains required.
- Use `MinecraftServer.isSingleplayerOwner(GameProfile)` for local-owner exemption; never authorize all integrated-server players.
- Preserve `showHighSpeedParticles` and all existing config keys; new config keys use safe defaults.
- Normal particle hard caps are Performance 8, Balanced 20, and Cinematic 32 per client tick; a sonic ring uses 24 particles and never exceeds 28.
- Follow TDD: add each behavior test, observe the expected failure, then add the minimum production implementation.
- Do not commit until the full phase-specific `clean test` and `build` commands succeed.

---

## File Map

### Common/server additions

- `flight/FlightActivationSource.java`: authoritative source enum and safe network ordinal decoding.
- `flight/FlightActivationResolver.java`: pure source resolution and final activation gate.
- `flight/HeldFireworkRules.java`: non-mutating main/off-hand rocket detection.
- `flight/FlightSessionState.java`: requested firework preference plus source/policy synchronization cache.
- `flight/OverdriveFlightHandler.java`: existing tick integration, policy evaluation, live synchronization, and policy refresh.
- `network/HeldFireworkPreferenceC2SPayload.java`: boolean client preference intent.
- `network/OverdriveStateS2CPayload.java`: protocol-3 confirmed state, source, and policy.
- `network/OverdriveNetworking.java`: payload registration and authoritative receivers.
- `command/OverdriveCommands.java`: permission-4 firework policy command and immediate online refresh.
- `config/OverdriveConfigModel.java`: server policy and local player/visual fields.

### Pure common additions

- `config/VisualPreset.java`: shared config-safe preset enum with no client imports.
- `config/control/ControlScreenState.java`: navigation, breakpoint, dirty-confirmation, and notification state.
- `config/control/ConfigDraft.java`: player-editable snapshot, validation, defaults, apply, and dirty comparison.
- `config/control/NavigationSection.java`: five sections and translations.
- `config/control/ConfigPermissionState.java`: server policy and local-owner editability mapping.
- `config/control/ControlValueFormatter.java`: locale-stable terminal values.
- `visual/VisualIntensity.java`: pure bounded visual intensity.
- `visual/SonicBoomState.java`: cooldown and hysteresis state machine.
- `visual/FlightBasis.java`: stable forward/right/up basis and ring-plane vectors.

These classes contain no screen, renderer, or `net.minecraft.client` references, so the ordinary JUnit source set can test them while dedicated servers remain safe.

### Client additions

- `client/config/OverdriveControlScreen.java`: responsive owo screen shell and lifecycle.
- `client/config/ControlTerminalTheme.java`: colors, sizes, breakpoint, and surfaces.
- `client/config/ControlPanelComponents.java`: reusable terminal-styled rows and cards.
- `client/config/FlightSettingsPanel.java`: multiplier, FOV, firework preference, and source cards.
- `client/config/BombingSettingsPanel.java`: read-only bombing settings.
- `client/config/BreachSettingsPanel.java`: read-only Breach settings and safety emphasis.
- `client/config/VisualSettingsPanel.java`: preset and visual controls.
- `client/config/ServerPolicyPanel.java`: read-only connection role and synchronized policy.
- `client/config/FlightStatusPanel.java`: confirmed flight state plus local speed/durability observation.
- `client/OverdriveVisuals.java`: client emission and FOV tick integration.

### Tests and resources

- `flight/FlightActivationResolverTest.java`: all activation-source and final-gate cases.
- `flight/HeldFireworkRulesTest.java`: hand detection and stack-count preservation.
- `flight/FlightSessionStateTest.java`: safe preference default and expanded sync cache.
- `config/control/ConfigDraftTest.java`: local draft behavior without rendering.
- `config/control/ControlScreenStateTest.java`: navigation and responsive rules.
- `config/control/ConfigPermissionStateTest.java`: firework toggle locking and role labels.
- `visual/VisualIntensityTest.java`: finite guards, reduce-motion rules, and caps.
- `visual/SonicBoomStateTest.java`: crossing, cooldown, reset, and hysteresis.
- `ResourceContractTest.java`: protocol, config, translations, optional Mod Menu, and source-set contracts.

---

### Task 1: Pure held-firework activation rules

**Files:**
- Create: `src/main/java/io/github/ikunkk02/elytraoverdrive/flight/FlightActivationSource.java`
- Create: `src/main/java/io/github/ikunkk02/elytraoverdrive/flight/FlightActivationResolver.java`
- Create: `src/main/java/io/github/ikunkk02/elytraoverdrive/flight/HeldFireworkRules.java`
- Create: `src/test/java/io/github/ikunkk02/elytraoverdrive/flight/FlightActivationResolverTest.java`
- Create: `src/test/java/io/github/ikunkk02/elytraoverdrive/flight/HeldFireworkRulesTest.java`

**Interfaces:**
- Produces: `FlightActivationSource { NONE, ENCHANTMENT, HELD_FIREWORK, BOTH }`.
- Produces: `FlightActivationResolver.resolve(boolean, boolean, boolean, boolean)`.
- Produces: `FlightActivationResolver.canActivate(FlightActivationSource, boolean, boolean, boolean, boolean, boolean, double)`.
- Produces: `HeldFireworkRules.isHoldingRocket(ItemStack, ItemStack)`.

- [ ] **Step 1: Add failing source-resolution tests**

```java
@Test void deniedServerPolicyRejectsFireworkOnly() {
    assertEquals(NONE, FlightActivationResolver.resolve(false, true, true, false));
}

@Test void allowedPolicyAndPreferenceResolveFirework() {
    assertEquals(HELD_FIREWORK, FlightActivationResolver.resolve(false, true, true, true));
}

@Test void localOwnerOverrideAndLanGuestPolicyStayDistinct() {
    boolean serverConfig = false;
    assertEquals(HELD_FIREWORK,
            FlightActivationResolver.resolve(false, true, true, serverConfig || true));
    assertEquals(NONE,
            FlightActivationResolver.resolve(false, true, true, serverConfig || false));
}

@Test void disabledPreferenceOrMissingRocketRejectsFirework() {
    assertEquals(NONE, FlightActivationResolver.resolve(false, false, true, true));
    assertEquals(NONE, FlightActivationResolver.resolve(false, true, false, true));
}

@Test void enchantmentRemainsIndependentAndCombinesWithFirework() {
    assertEquals(ENCHANTMENT, FlightActivationResolver.resolve(true, false, false, false));
    assertEquals(BOTH, FlightActivationResolver.resolve(true, true, true, true));
    assertEquals(ENCHANTMENT, FlightActivationResolver.resolve(true, true, false, true));
}

@Test void globalDisableBlocksActualActivationForEverySource() {
    assertFalse(FlightActivationResolver.canActivate(HELD_FIREWORK, false, true, false, true, true, 5.0));
    assertFalse(FlightActivationResolver.canActivate(ENCHANTMENT, false, true, false, true, true, 5.0));
}
```

- [ ] **Step 2: Run the resolver test and verify RED**

Run: `gradlew.bat test --tests "*.FlightActivationResolverTest"`

Expected: compilation fails because `FlightActivationResolver` and `FlightActivationSource` do not exist.

- [ ] **Step 3: Implement the pure enum and resolver**

```java
public enum FlightActivationSource {
    NONE, ENCHANTMENT, HELD_FIREWORK, BOTH;

    public static FlightActivationSource fromNetworkOrdinal(int ordinal) {
        return ordinal >= 0 && ordinal < values().length ? values()[ordinal] : NONE;
    }
}
```

```java
public static FlightActivationSource resolve(
        boolean hasEnchantment,
        boolean preferenceEnabled,
        boolean holdingRocket,
        boolean policyAllowsPlayer
) {
    boolean firework = preferenceEnabled && holdingRocket && policyAllowsPlayer;
    if (hasEnchantment && firework) return FlightActivationSource.BOTH;
    if (hasEnchantment) return FlightActivationSource.ENCHANTMENT;
    if (firework) return FlightActivationSource.HELD_FIREWORK;
    return FlightActivationSource.NONE;
}

public static boolean canActivate(
        FlightActivationSource source,
        boolean highSpeedEnabled,
        boolean alive,
        boolean spectator,
        boolean fallFlying,
        boolean usableElytra,
        double effectiveMultiplier
) {
    return source != FlightActivationSource.NONE
            && highSpeedEnabled
            && alive
            && !spectator
            && fallFlying
            && usableElytra
            && Double.isFinite(effectiveMultiplier)
            && effectiveMultiplier > FlightSpeedController.MIN_MULTIPLIER;
}
```

- [ ] **Step 4: Add failing non-mutating hand tests**

```java
@Test void eitherHandCanHoldExactlyOneRocketWithoutConsumption() {
    ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET, 1);
    ItemStack empty = ItemStack.EMPTY;
    assertTrue(HeldFireworkRules.isHoldingRocket(rocket, empty));
    assertTrue(HeldFireworkRules.isHoldingRocket(empty, rocket));
    assertEquals(1, rocket.getCount());
}

@Test void unrelatedItemsDoNotQualify() {
    assertFalse(HeldFireworkRules.isHoldingRocket(new ItemStack(Items.STICK), ItemStack.EMPTY));
}
```

- [ ] **Step 5: Run the hand test and verify RED**

Run: `gradlew.bat test --tests "*.HeldFireworkRulesTest"`

Expected: compilation fails because `HeldFireworkRules` does not exist.

- [ ] **Step 6: Implement read-only hand detection**

```java
public static boolean isHoldingRocket(ItemStack mainHand, ItemStack offHand) {
    return mainHand.is(Items.FIREWORK_ROCKET) || offHand.is(Items.FIREWORK_ROCKET);
}
```

- [ ] **Step 7: Verify the pure rule tests GREEN**

Run: `gradlew.bat test --tests "*.FlightActivationResolverTest" --tests "*.HeldFireworkRulesTest"`

Expected: all activation and hand tests pass without changing either stack count.

---

### Task 2: Session preference, protocol 3, and authoritative server integration

**Files:**
- Modify: `src/main/java/io/github/ikunkk02/elytraoverdrive/config/OverdriveConfigModel.java`
- Modify: `src/main/java/io/github/ikunkk02/elytraoverdrive/flight/FlightSessionState.java`
- Modify: `src/main/java/io/github/ikunkk02/elytraoverdrive/flight/OverdriveFlightHandler.java`
- Create: `src/main/java/io/github/ikunkk02/elytraoverdrive/network/HeldFireworkPreferenceC2SPayload.java`
- Modify: `src/main/java/io/github/ikunkk02/elytraoverdrive/network/OverdriveStateS2CPayload.java`
- Modify: `src/main/java/io/github/ikunkk02/elytraoverdrive/network/RequiredClientPayload.java`
- Modify: `src/main/java/io/github/ikunkk02/elytraoverdrive/network/OverdriveNetworking.java`
- Modify: `src/client/java/io/github/ikunkk02/elytraoverdrive/client/ClientOverdriveState.java`
- Modify: `src/client/java/io/github/ikunkk02/elytraoverdrive/client/ClientOverdriveNetworking.java`
- Modify: `src/test/java/io/github/ikunkk02/elytraoverdrive/flight/FlightSessionStateTest.java`
- Modify: `src/test/java/io/github/ikunkk02/elytraoverdrive/ResourceContractTest.java`

**Interfaces:**
- Consumes: activation interfaces from Task 1.
- Produces: `OverdriveFlightHandler.updateHeldFireworkPreference(ServerPlayer, boolean)`.
- Produces: `OverdriveFlightHandler.refreshPolicyForAll(MinecraftServer)`.
- Produces: client getters for `activationSource`, `serverAllowsHeldFirework`, `localOwnerOverride`, and `serverAcceptedFireworkPreference`.

- [ ] **Step 1: Extend session tests before session production code**

```java
@Test void heldFireworkPreferenceStartsFalseAndCanTrackClientIntent() {
    FlightSessionState state = new FlightSessionState();
    assertFalse(state.heldFireworkPreference());
    state.setHeldFireworkPreference(true);
    assertTrue(state.heldFireworkPreference());
}

@Test void sourceAndPolicyChangesRequireSynchronization() {
    FlightSessionState state = new FlightSessionState();
    assertTrue(state.needsSynchronization(5.0, false, NONE, false, false, false));
    state.markSynchronized(5.0, false, NONE, false, false, false);
    assertFalse(state.needsSynchronization(5.0, false, NONE, false, false, false));
    assertTrue(state.needsSynchronization(5.0, false, NONE, true, false, false));
    assertTrue(state.needsSynchronization(5.0, true, HELD_FIREWORK, true, false, true));
}
```

- [ ] **Step 2: Verify session tests RED**

Run: `gradlew.bat test --tests "*.FlightSessionStateTest"`

Expected: compilation fails on the new preference and expanded synchronization methods.

- [ ] **Step 3: Add session fields and exact synchronization signature**

```java
private boolean heldFireworkPreference;
private FlightActivationSource activationSource = FlightActivationSource.NONE;
private FlightActivationSource lastSyncedSource = FlightActivationSource.NONE;
private boolean lastSyncedServerPolicy;
private boolean lastSyncedOwnerOverride;
private boolean lastSyncedAcceptedPreference;

public boolean heldFireworkPreference() { return heldFireworkPreference; }
public void setHeldFireworkPreference(boolean enabled) { heldFireworkPreference = enabled; }
public FlightActivationSource activationSource() { return activationSource; }
public void setActivationSource(FlightActivationSource source) {
    activationSource = source == null ? FlightActivationSource.NONE : source;
}
```

Replace the old two-field sync comparison with:

```java
public boolean needsSynchronization(double multiplier, boolean active, FlightActivationSource source,
        boolean serverPolicy, boolean ownerOverride, boolean acceptedPreference)
```

and an identical-parameter `markSynchronized(...)` that stores every value.

- [ ] **Step 4: Add resource-contract RED assertions for config and protocol**

```java
assertTrue(contains(configModel, "allowHeldFireworkOverdrive = false"));
assertTrue(contains(configModel, "enableHeldFireworkOverdrive = false"));
assertTrue(contains(requiredPayload, "CURRENT_PROTOCOL = 3"));
assertTrue(contains(statePayload, "state_v2"));
assertTrue(contains(handler, "isSingleplayerOwner"));
```

- [ ] **Step 5: Verify resource contract RED**

Run: `gradlew.bat test --tests "*.ResourceContractTest"`

Expected: assertions fail because the fields, protocol 3, state-v2 payload, and owner check are absent.

- [ ] **Step 6: Add config fields with correct ownership**

Add local player preference without `@Sync`:

```java
public boolean enableHeldFireworkOverdrive = false;
```

Add server policy with override sync:

```java
@Sync(Option.SyncMode.OVERRIDE_CLIENT)
public boolean allowHeldFireworkOverdrive = false;
```

- [ ] **Step 7: Implement versioned payloads and codec fields**

Create the C2S record with `ByteBufCodecs.BOOL`. Change the S2C record to:

```java
public record OverdriveStateS2CPayload(
        double effectiveMultiplier,
        boolean active,
        int activationSourceOrdinal,
        boolean allowHeldFireworkOverdrive,
        boolean localOwnerOverride,
        boolean acceptedHeldFireworkPreference
) implements CustomPacketPayload
```

Use the type ID `state_v2`, and compose the codec in exactly that field order. Set `CURRENT_PROTOCOL = 3`.

- [ ] **Step 8: Integrate resolver into the existing tick without copying movement**

In `tickPlayer`, compute:

```java
ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
boolean usableElytra = chest.is(Items.ELYTRA) && ElytraItem.isFlyEnabled(chest);
boolean ownerOverride = player.server.isSingleplayerOwner(player.getGameProfile());
boolean policyAllows = ElytraOverdrive.CONFIG.allowHeldFireworkOverdrive() || ownerOverride;
FlightActivationSource source = FlightActivationResolver.resolve(
        usableElytra && OverdriveEnchantments.hasOverdrive(player, chest),
        state.heldFireworkPreference(),
        HeldFireworkRules.isHoldingRocket(player.getMainHandItem(), player.getOffhandItem()),
        policyAllows
);
boolean canActivate = FlightActivationResolver.canActivate(
        source,
        ElytraOverdrive.CONFIG.enableHighSpeedFlight(),
        player.isAlive(),
        player.isSpectator(),
        player.isFallFlying(),
        usableElytra,
        effectiveMultiplier
);
```

Store the resolved source only when `canActivate` is true; deactivation stores `NONE`. Leave the calls to `FlightSpeedController.calculateNextVelocity`, `tickDurability`, and deactivation speed limiting in their existing path.

- [ ] **Step 9: Register preference receiver and client lifecycle**

Register `HeldFireworkPreferenceC2SPayload` in `playC2S`. Its receiver calls only:

```java
OverdriveFlightHandler.updateHeldFireworkPreference(context.player(), payload.enabled());
```

On join, the server state starts false. Client join sends multiplier and saved preference once. Disconnect resets confirmed policy/source state. Config UI save uses a public `sendPreferences()` method; remove the existing multiplier config hook so draft slider changes do not send per pixel.

Implement `refreshPolicyForAll` without applying an extra acceleration step: resolve each online player's current source from real server state, deactivate immediately when the new source cannot pass the final gate, otherwise update `activationSource`, and force the expanded S2C comparison. A `BOTH` source becomes `ENCHANTMENT` when policy is revoked; a firework-only source deactivates and receives the existing deactivation speed limit.

- [ ] **Step 10: Validate and apply S2C state on the client**

`ClientOverdriveState.update(...)` rejects non-finite/out-of-range multipliers and invalid source ordinals. `active` becomes true only when multiplier is greater than 1, the source is non-`NONE`, and the server payload says active.

- [ ] **Step 11: Verify focused networking/session tests GREEN**

Run: `gradlew.bat test --tests "*.FlightActivationResolverTest" --tests "*.HeldFireworkRulesTest" --tests "*.FlightSessionStateTest" --tests "*.ResourceContractTest"`

Expected: all focused tests pass and common sources have no client imports.

---

### Task 3: Administrator command and first production commit

**Files:**
- Create: `src/main/java/io/github/ikunkk02/elytraoverdrive/command/OverdriveCommands.java`
- Modify: `src/main/java/io/github/ikunkk02/elytraoverdrive/ElytraOverdrive.java`
- Modify: `src/main/resources/assets/elytra-overdrive/lang/en_us.json`
- Modify: `src/main/resources/assets/elytra-overdrive/lang/zh_cn.json`
- Modify: `src/test/java/io/github/ikunkk02/elytraoverdrive/ResourceContractTest.java`

**Interfaces:**
- Consumes: `OverdriveFlightHandler.refreshPolicyForAll(MinecraftServer)` from Task 2.
- Produces: `/elytraoverdrive firework_mode <true|false>`.

- [ ] **Step 1: Add command and translation contract tests**

Assert the command source contains `hasPermission(4)`, generated config setter use, `CONFIG.save()`, and `refreshPolicyForAll`. Assert both language files contain success, denied, enabled, and disabled translation keys.

- [ ] **Step 2: Verify command contract RED**

Run: `gradlew.bat test --tests "*.ResourceContractTest"`

Expected: assertions fail because the command and translations do not exist.

- [ ] **Step 3: Register the Fabric command**

Use `CommandRegistrationCallback.EVENT.register` and build:

```java
Commands.literal("elytraoverdrive")
    .then(Commands.literal("firework_mode")
        .then(Commands.argument("enabled", BoolArgumentType.bool())
            .executes(context -> setFireworkMode(
                context.getSource(),
                BoolArgumentType.getBool(context, "enabled")
            ))))
```

The handler first checks `source.hasPermission(4)`. Failure calls `sendFailure(Component.translatable("command.elytra_overdrive.no_permission"))` and returns `0`. Success sets the config, saves, refreshes all players, broadcasts a translated success through `sendSuccess`, and returns `Command.SINGLE_SUCCESS`.

- [ ] **Step 4: Verify command contract GREEN**

Run: `gradlew.bat test --tests "*.ResourceContractTest"`

Expected: command and bilingual resource assertions pass.

- [ ] **Step 5: Run the phase-1 verification gate**

Run: `gradlew.bat clean test`

Expected: every test passes.

Run: `gradlew.bat build`

Expected: `BUILD SUCCESSFUL` and remapped JARs are produced.

- [ ] **Step 6: Commit held-firework mode**

```powershell
git add .
git diff --cached --check
git commit -m "feat: add server-gated held firework overdrive"
```

Expected: one feature commit containing resolver, protocol, command, config, translations, and tests.

---

### Task 4: Testable configuration draft, permissions, navigation, and formatting

**Files:**
- Create: `src/main/java/io/github/ikunkk02/elytraoverdrive/config/control/ConfigDraft.java`
- Create: `src/main/java/io/github/ikunkk02/elytraoverdrive/config/control/ControlScreenState.java`
- Create: `src/main/java/io/github/ikunkk02/elytraoverdrive/config/control/NavigationSection.java`
- Create: `src/main/java/io/github/ikunkk02/elytraoverdrive/config/control/ConfigPermissionState.java`
- Create: `src/main/java/io/github/ikunkk02/elytraoverdrive/config/control/ControlValueFormatter.java`
- Create: `src/test/java/io/github/ikunkk02/elytraoverdrive/config/control/ConfigDraftTest.java`
- Create: `src/test/java/io/github/ikunkk02/elytraoverdrive/config/control/ControlScreenStateTest.java`
- Create: `src/test/java/io/github/ikunkk02/elytraoverdrive/config/control/ConfigPermissionStateTest.java`

**Interfaces:**
- Produces: immutable `ConfigDraft.Snapshot` and mutable draft accessors.
- Produces: `ConfigDraft.from(OverdriveConfig)`, `validated()`, `restorePlayerDefaults()`, `applyTo(OverdriveConfig)`, and `isDirty()`.
- Produces: `ControlScreenState.LayoutMode { THREE_COLUMN, COMPACT }` with breakpoint selection.
- Produces: permission labels and `canEditHeldFireworkPreference`.

- [ ] **Step 1: Add draft RED tests**

Tests create a pure `ConfigDraft.Snapshot` with multiplier, local preference, particle/FOV toggles, preset, effect toggles, reduce motion, and FOV intensity. Verify unchanged drafts are clean, an edit becomes dirty, validation clamps multiplier to 1–20 and FOV intensity to 0–1.5, and restore defaults changes only player-owned fields.

- [ ] **Step 2: Verify draft tests RED**

Run: `gradlew.bat test --tests "*.ConfigDraftTest"`

Expected: compilation fails because the draft types do not exist.

- [ ] **Step 3: Implement the pure snapshot and draft**

Use explicit player defaults:

```java
2.0, false, true, true, BALANCED, true, true, true, false, 1.0
```

The draft retains an `original` snapshot and a mutable `current` snapshot. `applyTo` calls generated setters only for local player/visual fields and then `config.save()`.

- [ ] **Step 4: Add navigation, responsive, permission, and formatting RED tests**

Verify all five enum sections exist, widths below 900 scaled pixels select `COMPACT`, widths at or above 900 select `THREE_COLUMN`, page transition progress reaches 1.0 after four client ticks (approximately 200 ms), policy denial disables the firework toggle, owner override enables it, and numeric formatting returns `5.0×`, `4.25 blocks/tick`, and `80 / 432` without locale-dependent corruption.

- [ ] **Step 5: Verify state tests RED**

Run: `gradlew.bat test --tests "*.ControlScreenStateTest" --tests "*.ConfigPermissionStateTest"`

Expected: compilation fails because the state and permission types do not exist.

- [ ] **Step 6: Implement pure state helpers**

`NavigationSection` stores translation keys. `ControlScreenState` stores selected section, layout mode, dirty-confirmation state, a four-tick clamped transition progress, and notification expiry. `ConfigPermissionState.from(serverAllowed, ownerOverride)` produces editable/locked state and role labels without reading `Minecraft`.

- [ ] **Step 7: Verify all configuration logic GREEN**

Run: `gradlew.bat test --tests "*.ConfigDraftTest" --tests "*.ControlScreenStateTest" --tests "*.ConfigPermissionStateTest"`

Expected: all pure UI-logic tests pass.

---

### Task 5: Build the responsive owo control terminal

**Files:**
- Remove annotation from: `src/main/java/io/github/ikunkk02/elytraoverdrive/config/OverdriveConfigModel.java`
- Create: `src/client/java/io/github/ikunkk02/elytraoverdrive/client/config/ControlTerminalTheme.java`
- Create: `src/client/java/io/github/ikunkk02/elytraoverdrive/client/config/ControlPanelComponents.java`
- Create: `src/client/java/io/github/ikunkk02/elytraoverdrive/client/config/OverdriveControlScreen.java`
- Create: `src/client/java/io/github/ikunkk02/elytraoverdrive/client/config/FlightSettingsPanel.java`
- Create: `src/client/java/io/github/ikunkk02/elytraoverdrive/client/config/BombingSettingsPanel.java`
- Create: `src/client/java/io/github/ikunkk02/elytraoverdrive/client/config/BreachSettingsPanel.java`
- Create: `src/client/java/io/github/ikunkk02/elytraoverdrive/client/config/VisualSettingsPanel.java`
- Create: `src/client/java/io/github/ikunkk02/elytraoverdrive/client/config/ServerPolicyPanel.java`
- Create: `src/client/java/io/github/ikunkk02/elytraoverdrive/client/config/FlightStatusPanel.java`
- Modify: `src/client/java/io/github/ikunkk02/elytraoverdrive/client/ElytraOverdriveClient.java`
- Modify: `src/main/resources/assets/elytra-overdrive/lang/en_us.json`
- Modify: `src/main/resources/assets/elytra-overdrive/lang/zh_cn.json`
- Modify: `src/test/java/io/github/ikunkk02/elytraoverdrive/ResourceContractTest.java`

**Interfaces:**
- Consumes: Task 4 draft/state helpers and Task 2 confirmed client state.
- Produces: custom provider registered through `ConfigScreenProviders.register`.

- [ ] **Step 1: Add source-set and provider contract tests**

Assert `@Modmenu` is absent from the config model, `ConfigScreenProviders.register` appears only in client sources, the screen extends `BaseOwoScreen`, and no main source imports `net.minecraft.client` or `com.terraformersmc.modmenu`.

- [ ] **Step 2: Verify UI contract RED**

Run: `gradlew.bat test --tests "*.ResourceContractTest"`

Expected: the custom provider and screen assertions fail.

- [ ] **Step 3: Implement theme and shared components using verified owo APIs**

Use flat and outline surfaces only:

```java
static final int BACKGROUND = 0xFF0A0F18;
static final int PANEL = 0xE6111A26;
static final int BORDER = 0xFF26384A;
static final int ACCENT = 0xFF4EBAD1;
static final int TEXT = 0xFFE5EDF2;
static final int SECONDARY = 0xFF8297A8;
static final int WARNING = 0xFFE39A45;
static final int COMPACT_BREAKPOINT = 900;
```

Build cards from `Containers.verticalFlow`, `Surface.flat`, `Surface.outline`, labels, buttons, checkboxes, and discrete sliders. Do not call `Surface.blur`.

- [ ] **Step 4: Implement the screen shell and responsive rebuild**

`OverdriveControlScreen` stores `parent`, `ConfigDraft`, and `ControlScreenState`. `build(rootComponent)` constructs header, normal or compact body, and footer. `resize` preserves the draft and section while rebuilding. Footer actions call save, two-step defaults, and dirty-exit confirmation. Page content uses the four-tick transition progress for a small opacity/position interpolation; hover surfaces brighten only the border color, toggle interpolation is bounded, and the saved notification expires after 40 ticks. If an owo animation method does not compile against 0.12.15.4, keep the tested tick interpolation and omit that individual flourish.

- [ ] **Step 5: Implement the five focused page panels**

- Flight panel owns only player multiplier, server maximum, confirmed final multiplier, FOV, firework preference, and activation cards. It uses plain `LOCKED` text and no unsupported Unicode lock glyph.
- Bombing panel renders `enableBombing`, interval, fuse, and inertia as `SERVER CONTROLLED` rows. Its `ARMED` presentation requires the synchronized server enable flag plus reliable local observations of fall-flying, main-hand flint and steel, and offhand TNT; otherwise it displays `SAFE` and never claims that the server has spawned a bomb.
- Breach panel renders all six existing server values and highlights enabled block-entity protection.
- Visual panel edits only local visual fields.
- Server panel renders the high-speed master switch, server maximum multiplier, confirmed held-firework policy, extra durability state and interval, bombing state, and Breach state. It displays `REMOTE SERVER / PLAYER ACCESS` or `INTEGRATED SERVER / LOCAL OWNER`; local owners additionally receive `LOCAL OWNER OVERRIDE`. Remote administrators still see read-only rows.

Each panel returns a bounded `FlowLayout` placed inside a vertical `ScrollContainer`.

- [ ] **Step 6: Implement status panel null safety and confirmed-state labels**

When `Minecraft.getInstance().player` or `level` is null, return `NO FLIGHT DATA`. Otherwise source, multiplier, policy, and active state come from `ClientOverdriveState`; velocity and chest-stack durability are local observations.

- [ ] **Step 7: Register the provider without a Mod Menu API dependency**

In client initialization:

```java
ConfigScreenProviders.register(ElytraOverdrive.MOD_ID, OverdriveControlScreen::new);
```

Remove `@Modmenu` and its import from the common config model. Do not add a `modmenu` entrypoint or compile dependency.

- [ ] **Step 8: Add complete English and Simplified Chinese control-terminal translations**

Add keys for title/subtitle, system status, protocol, five navigation sections, footer buttons, confirmation text, save notification, role labels, locked explanation, every field label, source names, flight states, and no-data text. Read both files explicitly as UTF-8 during verification.

- [ ] **Step 9: Compile and verify UI contracts GREEN**

Run: `gradlew.bat test --tests "*.ConfigDraftTest" --tests "*.ControlScreenStateTest" --tests "*.ConfigPermissionStateTest" --tests "*.ResourceContractTest"`

Expected: UI logic and source-boundary contracts pass.

Run: `gradlew.bat compileClientJava`

Expected: the screen compiles against owo-lib 0.12.15.4 APIs without reflection or invented methods.

---

### Task 6: Verify and commit the aerospace control terminal

**Files:**
- All Task 4 and Task 5 files.

**Interfaces:**
- Produces: complete custom configuration screen for later visual controls.

- [ ] **Step 1: Run the phase-2 clean test gate**

Run: `gradlew.bat clean test`

Expected: all server, UI logic, and resource tests pass.

- [ ] **Step 2: Run the phase-2 build gate**

Run: `gradlew.bat build`

Expected: `BUILD SUCCESSFUL`; common and client sources compile and remap.

- [ ] **Step 3: Commit the control terminal**

```powershell
git add .
git diff --cached --check
git commit -m "feat: add aerospace control terminal"
```

Expected: a focused UI commit with no gameplay changes to bombing or Breach.

---

### Task 7: Pure visual intensity, flight basis, and sonic hysteresis

**Files:**
- Create: `src/main/java/io/github/ikunkk02/elytraoverdrive/config/VisualPreset.java`
- Create: `src/main/java/io/github/ikunkk02/elytraoverdrive/visual/VisualIntensity.java`
- Create: `src/main/java/io/github/ikunkk02/elytraoverdrive/visual/SonicBoomState.java`
- Create: `src/main/java/io/github/ikunkk02/elytraoverdrive/visual/FlightBasis.java`
- Create: `src/test/java/io/github/ikunkk02/elytraoverdrive/visual/VisualIntensityTest.java`
- Create: `src/test/java/io/github/ikunkk02/elytraoverdrive/visual/SonicBoomStateTest.java`
- Create: `src/test/java/io/github/ikunkk02/elytraoverdrive/visual/FlightBasisTest.java`

**Interfaces:**
- Produces: `VisualPreset.particleLimit()` and FOV factor.
- Produces: `VisualIntensity.fromSpeed(double, double, boolean, VisualPreset, boolean)` and `scaledFovFactor(double)`.
- Produces: `SonicBoomState.tick(double speed, boolean active, boolean enabled)`.
- Produces: `FlightBasis.fromForward(FlightVelocity)` with `FlightVelocity forward()`, `right()`, and `up()`.

- [ ] **Step 1: Add visual intensity RED tests**

```java
@Test void invalidOrInactiveInputsReturnZero() {
    assertEquals(VisualIntensity.ZERO, VisualIntensity.fromSpeed(Double.NaN, 5, true, BALANCED, false));
    assertEquals(VisualIntensity.ZERO, VisualIntensity.fromSpeed(4, 5, false, BALANCED, false));
    assertEquals(VisualIntensity.ZERO, VisualIntensity.fromSpeed(4, 1, true, BALANCED, false));
}

@Test void presetCountsNeverExceedHardCaps() {
    assertTrue(VisualIntensity.fromSpeed(100, 20, true, PERFORMANCE, false).particleBudget() <= 8);
    assertTrue(VisualIntensity.fromSpeed(100, 20, true, BALANCED, false).particleBudget() <= 20);
    assertTrue(VisualIntensity.fromSpeed(100, 20, true, CINEMATIC, false).particleBudget() <= 32);
}

@Test void reduceMotionDisablesLinesAndRingAndReducesFov() {
    VisualIntensity normal = VisualIntensity.fromSpeed(5, 5, true, CINEMATIC, false);
    VisualIntensity reduced = VisualIntensity.fromSpeed(5, 5, true, CINEMATIC, true);
    assertEquals(0, reduced.speedLineCount());
    assertFalse(reduced.sonicRingAllowed());
    assertTrue(reduced.scaledFovFactor(1.0) < normal.scaledFovFactor(1.0));
    assertEquals(0.0, normal.scaledFovFactor(0.0));
}
```

- [ ] **Step 2: Verify visual intensity RED**

Run: `gradlew.bat test --tests "*.VisualIntensityTest"`

Expected: compilation fails because visual logic does not exist.

- [ ] **Step 3: Implement bounded smooth intensity**

Use a clamped normalized speed ramp and exact preset limits. `particleBudget` never exceeds the preset limit. Performance returns zero speed lines and disables sonic rings. Reduce Motion returns no lines/ring and halves the wingtip budget and FOV factor. `scaledFovFactor` clamps intensity to 0.0–1.5, so zero intensity returns exactly zero.

- [ ] **Step 4: Add sonic state RED tests**

```java
@Test void firstHighThresholdCrossingTriggersOnce() {
    SonicBoomState state = new SonicBoomState(4.0, 3.2, 60);
    assertFalse(state.tick(3.9, true, true));
    assertTrue(state.tick(4.0, true, true));
    assertFalse(state.tick(4.2, true, true));
}

@Test void cooldownAndHysteresisBothPreventRetrigger() {
    SonicBoomState state = new SonicBoomState(4.0, 3.2, 60);
    assertTrue(state.tick(4.1, true, true));
    for (int i = 0; i < 60; i++) assertFalse(state.tick(3.8, true, true));
    assertFalse(state.tick(4.2, true, true));
    assertFalse(state.tick(3.2, true, true));
    assertTrue(state.tick(4.1, true, true));
}
```

- [ ] **Step 5: Verify sonic tests RED**

Run: `gradlew.bat test --tests "*.SonicBoomStateTest"`

Expected: compilation fails because the state machine does not exist.

- [ ] **Step 6: Implement exact sonic parameters and reset behavior**

Use trigger 4.0, reset 3.2, cooldown 60. Inactive or disabled input resets the state and returns false. Triggering disarms; speed at or below reset rearms only after cooldown reaches zero.

- [ ] **Step 7: Add and implement finite orthonormal basis tests**

Test horizontal, vertical, and zero `FlightVelocity` inputs. `FlightBasis.fromForward(FlightVelocity)` returns `FlightVelocity forward()`, `right()`, and `up()` values that stay finite; right and up vectors have near-unit length and near-zero dot product with forward. Use world-up `(0,1,0)` unless almost parallel, then fall back to `(1,0,0)`.

- [ ] **Step 8: Verify all pure visual tests GREEN**

Run: `gradlew.bat test --tests "*.VisualIntensityTest" --tests "*.SonicBoomStateTest" --tests "*.FlightBasisTest"`

Expected: all visual rule and geometry tests pass.

---

### Task 8: Emit Dust visuals and apply configurable FOV

**Files:**
- Modify: `src/main/java/io/github/ikunkk02/elytraoverdrive/config/OverdriveConfigModel.java`
- Modify: `src/client/java/io/github/ikunkk02/elytraoverdrive/client/OverdriveVisuals.java`
- Modify: `src/client/java/io/github/ikunkk02/elytraoverdrive/client/ClientOverdriveState.java`
- Modify: `src/client/java/io/github/ikunkk02/elytraoverdrive/client/mixin/GameRendererMixin.java`
- Modify: `src/main/java/io/github/ikunkk02/elytraoverdrive/config/control/ConfigDraft.java`
- Modify: `src/client/java/io/github/ikunkk02/elytraoverdrive/client/config/VisualSettingsPanel.java`
- Modify: `src/main/resources/assets/elytra-overdrive/lang/en_us.json`
- Modify: `src/main/resources/assets/elytra-overdrive/lang/zh_cn.json`
- Modify: `src/test/java/io/github/ikunkk02/elytraoverdrive/ResourceContractTest.java`

**Interfaces:**
- Consumes: pure visual types from Task 7 and server-confirmed `ClientOverdriveState`.
- Produces: three-layer Dust visuals and scaled smoothed FOV.

- [ ] **Step 1: Add config and particle contract RED tests**

Assert the model contains all seven visual fields and range 0.0–1.5 for FOV intensity. Assert `OverdriveVisuals` uses Dust/DustColorTransition types, contains no server networking calls, and no longer uses `ParticleTypes.CLOUD`.

- [ ] **Step 2: Verify visual contract RED**

Run: `gradlew.bat test --tests "*.ResourceContractTest"`

Expected: assertions fail because the new fields and Dust implementation are absent.

- [ ] **Step 3: Add visual config defaults and generated accessors**

Add:

```java
public VisualPreset visualPreset = VisualPreset.BALANCED;
public boolean enableWingtipTrails = true;
public boolean enableSpeedLines = true;
public boolean enableSonicBoomRing = true;
public boolean reduceMotion = false;
@RangeConstraint(min = 0.0, max = 1.5, decimalPlaces = 2)
public double fovIntensity = 1.0;
```

Keep `showHighSpeedParticles` and `enableHighSpeedFov` unchanged.

- [ ] **Step 4: Implement a single per-tick particle budget**

At client tick end, return early unless player, level, confirmed active state, non-`NONE` source, finite speed, multiplier greater than 1, and global particle switch are valid. Create one budget counter from `VisualIntensity`; wingtip and speed-line emitters decrement it and stop at zero.

- [ ] **Step 5: Implement oriented wingtip trails**

Use `FlightBasis` and approximate local offsets. Emit short cyan-to-white Dust transition point sequences behind left/right positions. Keep the positions behind the first-person center line and scale trail length only within intensity bounds.

- [ ] **Step 6: Implement peripheral speed lines**

Sample line origins from an annulus around the flight axis, never the center. Emit bounded point sequences opposite forward direction. Respect the local toggle, preset, reduce-motion state, and remaining budget.

- [ ] **Step 7: Implement the one-shot sonic ring**

When `SonicBoomState.tick` returns true, emit 24 points in the right/up ring plane. Clamp any requested count to 28. This method touches only `client.level.addParticle` and never sends a packet or changes entities/blocks.

- [ ] **Step 8: Scale the existing smoothed FOV**

Pass validated `fovIntensity`, preset factor, and reduce-motion state into `ClientOverdriveState.tickFov`. The target is existing speed boost multiplied by those factors. `GameRendererMixin` still modifies only the returned transient FOV when `useFovSetting` is true; it never writes options.

- [ ] **Step 9: Add explicit zero-intensity FOV test**

The pure intensity test must prove `scaledFovFactor(0)` is zero. Extend `ResourceContractTest` to assert that `ClientOverdriveState.tickFov` multiplies its existing speed-based target by `scaledFovFactor(fovIntensity)` and retains the existing interpolation-to-zero branch.

- [ ] **Step 10: Verify visual tests and client compilation GREEN**

Run: `gradlew.bat test --tests "*.VisualIntensityTest" --tests "*.SonicBoomStateTest" --tests "*.FlightBasisTest" --tests "*.ResourceContractTest"`

Expected: all visual and contract tests pass.

Run: `gradlew.bat compileClientJava`

Expected: Dust particle constructors and all Mojang-mapped 1.21.1 APIs compile.

---

### Task 9: Verify and commit flight visuals V2

**Files:**
- All Task 7 and Task 8 files.

**Interfaces:**
- Produces: bounded, server-confirmed visual effects and configurable FOV.

- [ ] **Step 1: Run the phase-3 clean test gate**

Run: `gradlew.bat clean test`

Expected: all tests pass.

- [ ] **Step 2: Run the phase-3 build gate**

Run: `gradlew.bat build`

Expected: `BUILD SUCCESSFUL` with remapped JARs.

- [ ] **Step 3: Commit visuals V2**

```powershell
git add .
git diff --cached --check
git commit -m "feat: improve overdrive flight visuals"
```

Expected: one focused client-visual commit with config and tests.

---

### Task 10: Documentation, runtime smoke tests, final commit, and push

**Files:**
- Modify: `README.md`
- Create: `docs/implementation/held-firework-overdrive.md`
- Create: `docs/implementation/control-terminal-ui.md`
- Create: `docs/implementation/flight-visuals-v2.md`

**Interfaces:**
- Produces: user-facing and implementation documentation matching protocol 3 and measured limits.

- [ ] **Step 1: Update README in UTF-8**

Document default-denied server policy, permission-4 command, local player opt-in, exact singleplayer owner behavior, no passive rocket consumption, unchanged vanilla right-click use, custom terminal, presets, particle layers, and client/server trust boundary.

- [ ] **Step 2: Write the three implementation documents**

The firework document records resolver truth table, `isSingleplayerOwner`, payload fields, protocol 3, source enum, policy refresh, and lifecycle reset. The UI document records file responsibilities, draft semantics, responsive breakpoint, lock rules, and no-Mod-Menu/dedicated-server behavior. The visuals document records Dust layers, budgets 8/20/32, ring 4.0/3.2/60/24, FOV scaling, and approximate-wingtip limitation.

- [ ] **Step 3: Verify language and Markdown encoding**

Run:

```powershell
Get-Content -Encoding utf8 README.md | Select-Object -First 20
Get-Content -Encoding utf8 src/main/resources/assets/elytra-overdrive/lang/zh_cn.json | Select-Object -First 20
```

Expected: Chinese text renders correctly without mojibake.

- [ ] **Step 4: Run final static and automated verification**

Run: `git diff --check`

Expected: no whitespace errors.

Run: `gradlew.bat clean test`

Expected: all tests pass from a clean build.

Run: `gradlew.bat build`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Run dedicated-server smoke verification**

Run: `gradlew.bat runServer`

Expected: Fabric server reaches the normal startup/EULA boundary or a running server prompt without client-class loading errors. Stop it cleanly after startup evidence.

- [ ] **Step 6: Run client smoke verification**

Run: `gradlew.bat runClient`

Expected: client reaches the title screen without Mixin, networking, owo UI, or resource-loading exceptions. Open the configuration screen when interactive control is available; otherwise record the remaining manual UI check.

- [ ] **Step 7: Audit packaged artifacts and source-set boundaries**

Run:

```powershell
jar tf (Get-ChildItem build/libs/*-1.0.0.jar | Where-Object Name -NotMatch 'sources' | Select-Object -First 1).FullName | Select-String 'client/config|client/visual|fabric.mod.json'
rg -n "import net.minecraft.client|import com.terraformersmc.modmenu" src/main/java
```

Expected: client classes are packaged for clients, no common source imports client or Mod Menu classes, and `fabric.mod.json` keeps Mod Menu under `suggests` only.

- [ ] **Step 8: Commit documentation**

```powershell
git add .
git diff --cached --check
git commit -m "docs: document firework flight and visual redesign"
```

Expected: README and three implementation documents are committed.

- [ ] **Step 9: Capture final repository evidence**

Run:

```powershell
git status --short --branch
git log --oneline -8
git rev-parse HEAD
git ls-remote origin refs/heads/main
```

Expected: clean local `main`, local branch ahead only by the new commits, and the remote SHA still identifies the pre-push baseline.

- [ ] **Step 10: Push and verify origin/main**

Run: `git push origin main`

Expected: fast-forward push succeeds.

Run:

```powershell
$local = git rev-parse HEAD
$remote = (git ls-remote origin refs/heads/main).Split()[0]
"LOCAL=$local"
"REMOTE=$remote"
```

Expected: local and remote full SHAs match exactly.

---

## Manual Verification Ledger

Record as completed only when observed in an interactive environment:

- Dedicated server default denies held-firework mode.
- Permission-4 command unlocks online clients immediately; ordinary players receive a permission message.
- Main hand and offhand rockets activate only after opt-in; passive holding does not change stack count.
- Normal right-click rocket use still consumes and boosts through vanilla behavior.
- Removing the rocket changes source on the next tick; enchanted Elytra continues through `ENCHANTMENT`.
- Local owner is exempt; LAN guest is not.
- Server policy shutdown immediately deactivates firework-only flight and locks the open screen.
- Overdrive speed cap and extra durability remain effective for firework activation.
- Mod Menu opens the terminal; title-screen opening is null-safe; no Mod Menu still starts.
- 854×480, 1280×720, 1920×1080, and multiple GUI scales show no overlap or trapped navigation.
- English and Simplified Chinese labels fit or scroll appropriately.
- Wingtip trails rotate with the player; speed lines avoid the crosshair; sonic ring triggers once with hysteresis.
- Performance is visibly sparse, Cinematic stays below its cap, and Reduce Motion suppresses dynamic effects.
- Sodium and Iris environments render without UI or particle corruption.
- Overdrive enchanting, TNT bombing, Breach thick-wall clearing, respawn, teleport, and dimension changes retain existing behavior.

## Known Compatibility Limits to Report

- Wingtip locations are a stable orientation-based approximation and do not read Elytra renderer animation bones.
- Full responsive layout, Sodium/Iris rendering, LAN owner separation, and vanilla rocket consumption require manual runtime testing.
- Mods that replace player flight physics after the server tick may still conflict with the existing Overdrive velocity layer.
- Protocol 2 clients are intentionally rejected with the explicit mismatch message after protocol 3 ships.
