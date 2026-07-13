package io.github.ikunkk02.elytraoverdrive.client.config;

import io.github.ikunkk02.elytraoverdrive.config.VisualPreset;
import io.github.ikunkk02.elytraoverdrive.config.control.ConfigDraft;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.network.chat.Component;

public final class VisualSettingsPanel {
	private VisualSettingsPanel() {
	}

	public static FlowLayout build(ConfigDraft draft, Runnable rebuild) {
		FlowLayout panel = ControlPanelComponents.panel(Sizing.fill(100));
		panel.child(ControlPanelComponents.title("screen.elytra_overdrive.visual.title"));
		panel.child(ControlPanelComponents.button(
				Component.translatable(
						"screen.elytra_overdrive.visual.preset",
						Component.translatable(presetKey(draft.visualPreset()))
				),
				button -> {
					draft.visualPreset(next(draft.visualPreset()));
					rebuild.run();
				}
		));
		panel.child(ControlPanelComponents.toggle(
				"screen.elytra_overdrive.visual.master",
				draft.showHighSpeedParticles(), true, draft::showHighSpeedParticles
		));
		panel.child(ControlPanelComponents.toggle(
				"screen.elytra_overdrive.visual.wingtips",
				draft.enableWingtipTrails(), true, draft::enableWingtipTrails
		));
		panel.child(ControlPanelComponents.toggle(
				"screen.elytra_overdrive.visual.speed_lines",
				draft.enableSpeedLines(), true, draft::enableSpeedLines
		));
		panel.child(ControlPanelComponents.toggle(
				"screen.elytra_overdrive.visual.sonic_ring",
				draft.enableSonicBoomRing(), true, draft::enableSonicBoomRing
		));
		panel.child(ControlPanelComponents.toggle(
				"screen.elytra_overdrive.visual.reduce_motion",
				draft.reduceMotion(), true, draft::reduceMotion
		));
		return panel;
	}

	private static VisualPreset next(VisualPreset preset) {
		VisualPreset[] values = VisualPreset.values();
		return values[(preset.ordinal() + 1) % values.length];
	}

	private static String presetKey(VisualPreset preset) {
		return "screen.elytra_overdrive.visual.preset." + preset.name().toLowerCase(java.util.Locale.ROOT);
	}
}
