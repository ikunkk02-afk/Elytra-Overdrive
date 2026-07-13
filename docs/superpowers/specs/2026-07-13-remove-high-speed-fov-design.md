# Remove High-Speed FOV Design

## Goal

Remove every Elytra Overdrive runtime path that reads, calculates, injects, or applies a high-speed FOV adjustment while preserving all flight, particle, control-terminal, VisualPreset, and Reduce Motion behavior.

## Runtime design

Delete the client-only `GameRendererMixin` and remove its client Mixin registration so Elytra Overdrive no longer targets `GameRenderer#getFov`. Delete `FovBoostCalculator`, the smoothed FOV fields and methods in `ClientOverdriveState`, and the FOV tick calls in `OverdriveVisuals`.

`VisualPreset` remains the particle-budget preset. `VisualIntensity` remains the bounded particle intensity model for wingtip trails, speed lines, and sonic boom rings. Their FOV-only fields and calculations are removed. `reduceMotion` remains and continues to reduce particle budgets, limit wingtip emission, and disable speed lines and sonic boom rings.

## Configuration and UI

Remove `enableHighSpeedFov` and `fovIntensity` from `OverdriveConfigModel`, the generated wrapper API, `ConfigDraft.PlayerSettings`, draft accessors, persistence, defaults, and validation. Remove the FOV toggle, intensity slider, and translations from the aerospace control terminal.

owo-lib `0.12.15.4+1.21` loads a config by iterating only the current model's options and looking up each option in the parsed JSON object. Extra keys are not traversed or rejected. Existing config files containing the two removed keys therefore load safely; the keys are ignored and disappear after a later save. No legacy fields or custom migration layer are needed.

## Documentation

Update `README.md`, `README_zh_CN.md`, and current files under `docs/implementation` so they describe particle presets and Reduce Motion without FOV. Historical Superpowers specs and plans remain records of the earlier implementation; this design supersedes their FOV sections.

## Testing

Delete the calculator-only unit test. Update the resource contract first so it fails while any production FOV hook, state, config field, UI key, or Mixin registration remains. Update particle and config-draft tests to remove FOV assertions while retaining coverage for particle caps, 200x behavior, VisualPreset, and Reduce Motion.

Final verification is `git diff --check`, a full source search, `gradlew.bat clean test`, and `gradlew.bat build`. Zoomify interaction, vanilla spyglass behavior, and visual flight behavior remain manual game checks.
