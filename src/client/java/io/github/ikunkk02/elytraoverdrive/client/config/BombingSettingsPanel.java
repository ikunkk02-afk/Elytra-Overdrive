package io.github.ikunkk02.elytraoverdrive.client.config;

import io.github.ikunkk02.elytraoverdrive.ElytraOverdrive;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public final class BombingSettingsPanel {
	private BombingSettingsPanel() {
	}

	public static FlowLayout build() {
		FlowLayout panel = ControlPanelComponents.panel(Sizing.fill(100));
		panel.child(ControlPanelComponents.title("screen.elytra_overdrive.bombing.title"));
		boolean armed = isArmedLocally();
		panel.child(ControlPanelComponents.label(
				Component.translatable(armed
						? "screen.elytra_overdrive.bombing.armed"
						: "screen.elytra_overdrive.bombing.safe"),
				armed ? ControlTerminalTheme.WARNING : ControlTerminalTheme.ACCENT
		));
		panel.child(serverRow("screen.elytra_overdrive.server.bombing", ElytraOverdrive.CONFIG.enableBombing()));
		panel.child(serverRow("screen.elytra_overdrive.bombing.interval", ElytraOverdrive.CONFIG.bombingIntervalTicks()));
		panel.child(serverRow("screen.elytra_overdrive.bombing.fuse", ElytraOverdrive.CONFIG.bombFuseTicks()));
		panel.child(serverRow("screen.elytra_overdrive.bombing.inertia", ElytraOverdrive.CONFIG.bombHorizontalInertia()));
		panel.child(ControlPanelComponents.label(
				Component.translatable("screen.elytra_overdrive.server_controlled"),
				ControlTerminalTheme.SECONDARY
		));
		return panel;
	}

	private static FlowLayout serverRow(String key, Object value) {
		return ControlPanelComponents.row(Component.translatable(key), Component.literal(String.valueOf(value)));
	}

	private static boolean isArmedLocally() {
		Minecraft client = Minecraft.getInstance();
		return ElytraOverdrive.CONFIG.enableBombing()
				&& client.player != null
				&& client.player.isFallFlying()
				&& client.player.getMainHandItem().is(Items.FLINT_AND_STEEL)
				&& client.player.getOffhandItem().is(Items.TNT);
	}
}
