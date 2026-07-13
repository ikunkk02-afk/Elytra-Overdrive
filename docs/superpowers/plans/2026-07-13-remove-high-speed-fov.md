# Remove High-Speed FOV Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove all Elytra Overdrive high-speed FOV behavior without changing flight, particles, VisualPreset, Reduce Motion, combat, or server speed limits.

**Architecture:** Remove the complete client FOV injection chain and its configuration/UI surface. Keep the existing particle model and presets, shrinking only their FOV-only data. Rely on owo-lib's current-option iteration to ignore obsolete keys in old JSON5 configs.

**Tech Stack:** Java 21, Fabric 1.21.1, Fabric Loom, Mixin, owo-lib config, JUnit 5, Markdown/JSON resources.

## Global Constraints

- Do not retain or replace any `GameRenderer#getFov` injection.
- Preserve high-speed flight, 100x, experimental 200x, held-firework flight, TNT bombing, Breach, particle effects, VisualPreset, Reduce Motion, control terminal, and server limits.
- Remove `enableHighSpeedFov` and `fovIntensity` completely because owo-lib safely ignores obsolete JSON5 keys.
- Use one final commit named `fix: remove high-speed FOV modification` and push `origin/main`.

---

### Task 1: Establish the no-FOV regression contract

**Files:**
- Modify: `src/test/java/io/github/ikunkk02/elytraoverdrive/ResourceContractTest.java`
- Modify: `src/test/java/io/github/ikunkk02/elytraoverdrive/visual/VisualIntensityTest.java`
- Modify: `src/test/java/io/github/ikunkk02/elytraoverdrive/config/control/ConfigDraftTest.java`
- Delete: `src/test/java/io/github/ikunkk02/elytraoverdrive/visual/FovBoostCalculatorTest.java`

**Interfaces:**
- Consumes: source files and JSON resources from the repository.
- Produces: a negative contract that rejects FOV runtime/config/UI hooks while retaining particle and Reduce Motion assertions.

- [ ] **Step 1: Replace the configurable-FOV resource contract**

Add assertions that the client Mixin JSON does not contain `GameRendererMixin`, the deleted class paths do not exist, and production Java sources contain none of `getFov`, `tickFov`, `interpolatedFovBoost`, `enableHighSpeedFov`, or `fovIntensity`. Keep positive assertions for Dust particles, sonic boom state, particle budgets, VisualPreset, and Reduce Motion.

- [ ] **Step 2: Run the focused contract and verify RED**

Run: `.\gradlew.bat test --tests io.github.ikunkk02.elytraoverdrive.ResourceContractTest`

Expected: FAIL because the current Mixin/config/state still contains FOV behavior.

- [ ] **Step 3: Remove obsolete FOV-only test expectations**

Delete `FovBoostCalculatorTest`. In `VisualIntensityTest`, rename the Reduce Motion test and assert only speed-line suppression, sonic-ring suppression, and reduced particle budget. In `ConfigDraftTest`, remove FOV clamping and constructor arguments while preserving 200x multiplier and defaults coverage.

### Task 2: Remove the production, configuration, and UI FOV chain

**Files:**
- Delete: `src/client/java/io/github/ikunkk02/elytraoverdrive/client/mixin/GameRendererMixin.java`
- Delete: `src/main/java/io/github/ikunkk02/elytraoverdrive/visual/FovBoostCalculator.java`
- Modify: `src/client/resources/elytra-overdrive.client.mixins.json`
- Modify: `src/client/java/io/github/ikunkk02/elytraoverdrive/client/ClientOverdriveState.java`
- Modify: `src/client/java/io/github/ikunkk02/elytraoverdrive/client/OverdriveVisuals.java`
- Modify: `src/main/java/io/github/ikunkk02/elytraoverdrive/config/OverdriveConfigModel.java`
- Modify: `src/main/java/io/github/ikunkk02/elytraoverdrive/config/control/ConfigDraft.java`
- Modify: `src/main/java/io/github/ikunkk02/elytraoverdrive/config/VisualPreset.java`
- Modify: `src/main/java/io/github/ikunkk02/elytraoverdrive/visual/VisualIntensity.java`
- Modify: `src/client/java/io/github/ikunkk02/elytraoverdrive/client/config/FlightSettingsPanel.java`
- Modify: `src/client/java/io/github/ikunkk02/elytraoverdrive/client/config/VisualSettingsPanel.java`
- Modify: `src/main/resources/assets/elytra-overdrive/lang/en_us.json`
- Modify: `src/main/resources/assets/elytra-overdrive/lang/zh_cn.json`

**Interfaces:**
- Consumes: existing server-confirmed client state and particle configuration.
- Produces: particle-only VisualPreset/VisualIntensity and a client Mixin list with no renderer FOV target.

- [ ] **Step 1: Delete the renderer hook and calculator**

Delete both FOV-only classes and remove only `GameRendererMixin` from the client Mixin array.

- [ ] **Step 2: Remove client FOV state and ticking**

Remove FOV imports, fields, methods, tick calls, and reset assignments. Keep effective multiplier, activation source, policy state, particle ticking, and sonic-boom reset logic unchanged.

- [ ] **Step 3: Remove config and control-terminal FOV options**

Remove both model fields and all corresponding draft record components, copies, persistence, defaults, accessors, and clamping. Remove the flight toggle and visual intensity slider. Remove only their English and Chinese translation keys.

- [ ] **Step 4: Make visual models particle-only**

Change `VisualPreset` constants to accept only particle limits. Remove `fovFactor` from `VisualIntensity.ZERO`, record components, construction, Reduce Motion calculations, and methods. Preserve particle limits `8/20/32` and Reduce Motion particle cap `6` with speed lines and sonic ring disabled.

- [ ] **Step 5: Run focused tests and verify GREEN**

Run: `.\gradlew.bat test --tests io.github.ikunkk02.elytraoverdrive.ResourceContractTest --tests io.github.ikunkk02.elytraoverdrive.visual.VisualIntensityTest --tests io.github.ikunkk02.elytraoverdrive.config.control.ConfigDraftTest`

Expected: PASS with zero failures.

### Task 3: Update documentation, verify, commit, and push

**Files:**
- Modify: `README.md`
- Modify: `README_zh_CN.md`
- Modify: relevant Markdown files under `docs/implementation/`
- Create: `docs/superpowers/specs/2026-07-13-remove-high-speed-fov-design.md`
- Create: `docs/superpowers/plans/2026-07-13-remove-high-speed-fov.md`

**Interfaces:**
- Consumes: the final production behavior and user-specified manual checklist.
- Produces: current documentation with no advertised FOV feature and a pushed `main` commit.

- [ ] **Step 1: Remove current FOV documentation**

Remove Dynamic FOV, FOV intensity, renderer injection, smoothing, and FOV recovery claims. Rewrite preset and Reduce Motion descriptions around particles only. Keep historical Superpowers documents unchanged except for the new superseding design and plan.

- [ ] **Step 2: Search and inspect the final runtime surface**

Run `rg -n -i "enableHighSpeedFov|fovIntensity|FovBoostCalculator|tickFov|interpolatedFovBoost|previousFovBoost|fovBoost|GameRendererMixin|getFov" src README.md README_zh_CN.md docs/implementation` and confirm no matches.

- [ ] **Step 3: Run final verification**

Run `.\gradlew.bat clean test`, `.\gradlew.bat build`, `git diff --check`, `git status --short --branch`, and `git diff`.

Expected: both Gradle commands exit 0, diff check is empty, and the diff contains only FOV removal plus the approved design/plan.

- [ ] **Step 4: Commit and push**

Run `git add <all related files>`, `git commit -m "fix: remove high-speed FOV modification"`, and `git push origin main`.

Expected: a new commit on `main` and a successful fast-forward push to `origin/main`.
