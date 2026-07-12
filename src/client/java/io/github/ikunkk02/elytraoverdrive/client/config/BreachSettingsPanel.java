package io.github.ikunkk02.elytraoverdrive.client.config;

import io.github.ikunkk02.elytraoverdrive.ElytraOverdrive;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.network.chat.Component;

public final class BreachSettingsPanel {
	private BreachSettingsPanel() {
	}

	public static FlowLayout build() {
		FlowLayout panel = ControlPanelComponents.panel(Sizing.fill(100));
		panel.child(ControlPanelComponents.title("screen.elytra_overdrive.breach.title"));
		panel.child(ControlPanelComponents.label(
				Component.translatable("screen.elytra_overdrive.breach.patterns"),
				ControlTerminalTheme.SECONDARY
		).maxWidth(420));
		panel.child(row("screen.elytra_overdrive.server.breach", ElytraOverdrive.CONFIG.enableTridentBreach()));
		panel.child(row("screen.elytra_overdrive.breach.minimum_speed", ElytraOverdrive.CONFIG.minimumBreachSpeed()));
		panel.child(row("screen.elytra_overdrive.breach.maximum_blocks", ElytraOverdrive.CONFIG.maximumBreachBlocksPerTick()));
		panel.child(row("screen.elytra_overdrive.breach.durability", ElytraOverdrive.CONFIG.breachDurabilityMultiplier()));
		panel.child(row("screen.elytra_overdrive.breach.drops", ElytraOverdrive.CONFIG.breachDropsBlocks()));
		panel.child(ControlPanelComponents.label(
				Component.translatable("screen.elytra_overdrive.breach.block_entity_protection"),
				ElytraOverdrive.CONFIG.protectBlockEntitiesFromBreach()
						? ControlTerminalTheme.ACCENT
						: ControlTerminalTheme.WARNING
		));
		panel.child(row(
				"screen.elytra_overdrive.breach.block_entity_protection",
				ElytraOverdrive.CONFIG.protectBlockEntitiesFromBreach()
		));
		panel.child(ControlPanelComponents.label(
				Component.translatable("screen.elytra_overdrive.server_controlled"),
				ControlTerminalTheme.SECONDARY
		));
		return panel;
	}

	private static FlowLayout row(String key, Object value) {
		return ControlPanelComponents.row(Component.translatable(key), Component.literal(String.valueOf(value)));
	}
}
