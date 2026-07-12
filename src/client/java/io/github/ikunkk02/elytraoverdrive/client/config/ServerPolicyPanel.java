package io.github.ikunkk02.elytraoverdrive.client.config;

import io.github.ikunkk02.elytraoverdrive.ElytraOverdrive;
import io.github.ikunkk02.elytraoverdrive.client.ClientOverdriveState;
import io.github.ikunkk02.elytraoverdrive.config.control.ControlValueFormatter;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.network.chat.Component;

public final class ServerPolicyPanel {
	private ServerPolicyPanel() {
	}

	public static FlowLayout build() {
		FlowLayout panel = ControlPanelComponents.panel(Sizing.fill(100));
		panel.child(ControlPanelComponents.title("screen.elytra_overdrive.server.title"));
		panel.child(ControlPanelComponents.label(
				Component.translatable(ClientOverdriveState.localOwnerOverride()
						? "screen.elytra_overdrive.server.local_owner"
						: "screen.elytra_overdrive.server.remote_player"),
				ControlTerminalTheme.ACCENT
		));
		if (ClientOverdriveState.localOwnerOverride()) {
			panel.child(ControlPanelComponents.label(
					Component.translatable("screen.elytra_overdrive.server.local_override"),
					ControlTerminalTheme.WARNING
			));
		}
		panel.child(row("screen.elytra_overdrive.server.high_speed", ElytraOverdrive.CONFIG.enableHighSpeedFlight()));
		panel.child(row(
				"screen.elytra_overdrive.flight.server_max",
				ControlValueFormatter.multiplier(ElytraOverdrive.CONFIG.serverMaximumMultiplier())
		));
		panel.child(row("screen.elytra_overdrive.server.firework", ClientOverdriveState.serverAllowsHeldFirework()));
		panel.child(row("screen.elytra_overdrive.server.durability", ElytraOverdrive.CONFIG.extraDurabilityDamage()));
		panel.child(row("screen.elytra_overdrive.server.durability_interval", ElytraOverdrive.CONFIG.extraDurabilityIntervalTicks()));
		panel.child(row("screen.elytra_overdrive.server.bombing", ElytraOverdrive.CONFIG.enableBombing()));
		panel.child(row("screen.elytra_overdrive.server.breach", ElytraOverdrive.CONFIG.enableTridentBreach()));
		panel.child(ControlPanelComponents.label(
				Component.translatable("screen.elytra_overdrive.server.read_only"),
				ControlTerminalTheme.SECONDARY
		));
		return panel;
	}

	private static FlowLayout row(String key, Object value) {
		return ControlPanelComponents.row(Component.translatable(key), Component.literal(String.valueOf(value)));
	}
}
