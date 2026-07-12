# Trident Breach Pre-Collision Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make Trident Breach clear legal blocks along a speed-scaled predicted flight path before player collision.

**Architecture:** Fabric's start-world event invokes the server-owned Breach handler before entity movement. Pure logic calculates bounded look-ahead and ordered candidate positions, while the handler independently counts only successful block destruction against the configured break budget.

**Tech Stack:** Java 21, Minecraft 1.21.1, Fabric API 0.116.13, JUnit 5, Gradle.

## Global Constraints

- Preserve all existing activation, protection, hardness, durability, drop, and server-authority rules.
- Do not modify TNT Bombing, Overdrive flight, enchanting-table behavior, player collision, or chunk loading.
- Use actual `player.getDeltaMovement().length()` and cap look-ahead at 12 blocks.
- Enforce a 1024-position internal scan ceiling and the configured maximum only for successful breaks.

---

### Task 1: Red tests for predictive sampling and break budgeting

**Files:**
- Modify: `src/test/java/io/github/ikunkk02/elytraoverdrive/breach/BreachPathSamplerTest.java`
- Create: `src/test/java/io/github/ikunkk02/elytraoverdrive/breach/BreachBreakBudgetTest.java`

**Interfaces:**
- Consumes: current `BreachPathSampler.sample(...)` behavior.
- Produces: executable specifications for look-ahead, depth ordering, and `BreachBreakBudget` success accounting.

- [ ] Add tests named `airPositionsDoNotConsumeBreakBudget`, `predictivePathExtendsAheadOfCurrentPosition`, `lookAheadDistanceIncreasesWithSpeed`, and `thickWallCanExposeMultipleDepthLayersInOneTick`.
- [ ] Run `gradlew.bat test --tests "*BreachPathSamplerTest" --tests "*BreachBreakBudgetTest"` and retain the expected compilation/assertion failures as red evidence.

### Task 2: Minimal pure-logic implementation

**Files:**
- Modify: `src/main/java/io/github/ikunkk02/elytraoverdrive/breach/BreachPathSampler.java`
- Create: `src/main/java/io/github/ikunkk02/elytraoverdrive/breach/BreachBreakBudget.java`

**Interfaces:**
- Produces: `calculateLookAheadDistance(double)`, predictive `sample(...)`, and stateful `BreachBreakBudget` success accounting.

- [ ] Implement look-ahead as `Math.clamp(1.5 + speed * 1.5, 2.0, 12.0)` with non-finite input handled safely.
- [ ] Sample unique candidates under a 1024-position ceiling in stable near-to-far depth-layer order.
- [ ] Count budget only when the supplied break attempt returns true.
- [ ] Re-run the focused tests and confirm green.

### Task 3: Pre-collision server integration

**Files:**
- Modify: `src/main/java/io/github/ikunkk02/elytraoverdrive/breach/BreachHandler.java`

**Interfaces:**
- Consumes: predictive sampler and break-budget processor from Task 2.
- Produces: Breach execution on `ServerTickEvents.START_WORLD_TICK` before entity movement.

- [ ] Replace only Breach's end-world registration with start-world registration.
- [ ] Use current body position as the first activation's start and previous position thereafter.
- [ ] Pass actual velocity to predictive sampling and stop immediately when the trident becomes unusable.
- [ ] Keep `tryBreak` and every existing protection rule intact.

### Task 4: Verification and publication

**Files:**
- Verify all changed production, test, spec, and plan files.

**Interfaces:**
- Produces: tested build, commit, and pushed `origin/main`.

- [ ] Run `gradlew.bat clean test` and confirm all tests pass.
- [ ] Run `gradlew.bat build` and confirm `BUILD SUCCESSFUL`.
- [ ] Review `git diff --check`, `git diff`, and `git status --short` for scoped changes.
- [ ] Commit with `fix: make trident breach clear blocks before collision` and push `origin main`.
