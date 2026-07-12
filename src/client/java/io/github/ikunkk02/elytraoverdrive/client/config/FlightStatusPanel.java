package io.github.ikunkk02.elytraoverdrive.client.config;

import io.github.ikunkk02.elytraoverdrive.ElytraOverdrive;
import io.github.ikunkk02.elytraoverdrive.client.ClientOverdriveState;
import io.github.ikunkk02.elytraoverdrive.config.control.ControlValueFormatter;
import io.github.ikunkk02.elytraoverdrive.flight.FlightActivationSource;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class FlightStatusPanel {
	private final FlowLayout component;
	private final LabelComponent data;

	public FlightStatusPanel(Sizing horizontalSizing) {
		this.component = ControlPanelComponents.panel(horizontalSizing);
		this.component.child(ControlPanelComponents.title("screen.elytra_overdrive.status.title"));
		this.data = ControlPanelComponents.label(Component.empty(), ControlTerminalTheme.TEXT).maxWidth(240);
		this.component.child(this.data);
		refresh();
	}

	public FlowLayout component() {
		return this.component;
	}

	public void refresh() {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.level == null) {
			this.data.text(Component.translatable("screen.elytra_overdrive.status.no_data"));
			return;
		}

		String flightKey = ClientOverdriveState.active()
				? "screen.elytra_overdrive.status.high_speed"
				: client.player.isFallFlying()
						? "screen.elytra_overdrive.status.vanilla_gliding"
						: "screen.elytra_overdrive.status.not_flying";
		ItemStack chest = client.player.getItemBySlot(EquipmentSlot.CHEST);
		String durability = "--";
		if (chest.is(Items.ELYTRA) && chest.isDamageableItem()) {
			durability = ControlValueFormatter.durability(
					chest.getMaxDamage() - chest.getDamageValue(), chest.getMaxDamage()
			);
		}
		Component status = Component.translatable("screen.elytra_overdrive.status.lines",
				Component.translatable(flightKey),
				Component.translatable(sourceKey(ClientOverdriveState.activationSource())),
				ControlValueFormatter.multiplier(ElytraOverdrive.CONFIG.playerSelectedMultiplier()),
				ControlValueFormatter.multiplier(ElytraOverdrive.CONFIG.serverMaximumMultiplier()),
				ControlValueFormatter.multiplier(ClientOverdriveState.effectiveMultiplier()),
				ControlValueFormatter.speed(client.player.getDeltaMovement().length()),
				durability,
				ClientOverdriveState.mayRequestHeldFirework()
		);
		this.data.text(status);
	}

	private static String sourceKey(FlightActivationSource source) {
		return "screen.elytra_overdrive.source." + source.name().toLowerCase(java.util.Locale.ROOT);
	}
}
