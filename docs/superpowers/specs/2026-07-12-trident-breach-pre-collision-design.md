# Trident Breach Pre-Collision Design

## Goal

Make Trident Breach clear a bounded, legal tunnel ahead of a flying player before vanilla movement collision occurs, including at Overdrive speeds.

## Design

Register Breach processing on Fabric's `START_WORLD_TICK`, which runs at the beginning of `ServerLevel.tick()` before entity ticks and collision resolution. Use the player's actual finite `deltaMovement` as the flight direction and speed. Sample from the previous valid body position (or the current body position on first activation) through the current position and onward by `clamp(1.5 + speed * 1.5, 2.0, 12.0)` blocks.

The path sampler owns a private safety ceiling of 1024 unique candidate positions. It deduplicates candidates and emits each near depth layer's complete Breach cross-section before advancing deeper. `maximumBreachBlocksPerTick` is enforced only in `BreachHandler`: air, protected blocks, protected block entities, unloaded or invalid positions, excessive hardness, duplicate positions, and failed destruction do not consume it; only a successful `destroyBlock` increments the break count.

The existing server-authoritative destruction, protection, hardness, drop, and durability rules remain unchanged. No movement cancellation, teleportation, collision-box modification, noclip, or forced chunk loading is introduced. A collision grace period is omitted because the pre-collision event removes the timing root cause; session state retains only the previous valid position.

## Verification

Pure logic tests cover break-budget filtering, predictive extension, speed-dependent look-ahead, near-to-far thick-wall coverage, uniqueness, vertical flight, and bounded extreme inputs. Repository verification runs `gradlew.bat clean test` followed by `gradlew.bat build`.
