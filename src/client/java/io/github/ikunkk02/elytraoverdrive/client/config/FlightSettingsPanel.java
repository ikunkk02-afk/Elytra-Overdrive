package io.github.ikunkk02.elytraoverdrive.client.config;

import io.github.ikunkk02.elytraoverdrive.ElytraOverdrive;
import io.github.ikunkk02.elytraoverdrive.client.ClientOverdriveState;
import io.github.ikunkk02.elytraoverdrive.config.control.ConfigDraft;
import io.github.ikunkk02.elytraoverdrive.config.control.ConfigPermissionState;
import io.github.ikunkk02.elytraoverdrive.config.control.ControlValueFormatter;
import io.github.ikunkk02.elytraoverdrive.enchantment.OverdriveEnchantments;
import io.github.ikunkk02.elytraoverdrive.flight.HeldFireworkRules;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.DiscreteSliderComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public final class FlightSettingsPanel {
	private FlightSettingsPanel() {
	}

	public static FlowLayout build(ConfigDraft draft) {
		FlowLayout panel = ControlPanelComponents.panel(Sizing.fill(100));
		panel.child(ControlPanelComponents.title("screen.elytra_overdrive.flight.title"));

		LabelComponent multiplier = ControlPanelComponents.label(
				Component.literal(ControlValueFormatter.multiplier(draft.playerSelectedMultiplier())),
				ControlTerminalTheme.TEXT
		).shadow(true);
		panel.child(multiplier);

		DiscreteSliderComponent slider = Components.discreteSlider(Sizing.fill(100), 1.0, 20.0)
				.decimalPlaces(1)
				.snap(true)
				.setFromDiscreteValue(draft.playerSelectedMultiplier());
		slider.onChanged().subscribe(value -> {
			draft.playerSelectedMultiplier(value);
			multiplier.text(Component.literal(ControlValueFormatter.multiplier(value)));
		});
		panel.child(slider);
		panel.child(ControlPanelComponents.row(
				Component.translatable("screen.elytra_overdrive.flight.selected"),
				Component.literal(ControlValueFormatter.multiplier(draft.playerSelectedMultiplier()))
		));
		panel.child(ControlPanelComponents.row(
				Component.translatable("screen.elytra_overdrive.flight.server_max"),
				Component.literal(ControlValueFormatter.multiplier(ElytraOverdrive.CONFIG.serverMaximumMultiplier()))
		));
		panel.child(ControlPanelComponents.row(
				Component.translatable("screen.elytra_overdrive.flight.effective"),
				Component.literal(ControlValueFormatter.multiplier(ClientOverdriveState.effectiveMultiplier()))
		));

		panel.child(ControlPanelComponents.toggle(
				"screen.elytra_overdrive.flight.fov",
				draft.enableHighSpeedFov(),
				true,
				draft::enableHighSpeedFov
		));

		ConfigPermissionState permission = ConfigPermissionState.from(
				ClientOverdriveState.serverAllowsHeldFirework(),
				ClientOverdriveState.localOwnerOverride()
		);
		panel.child(ControlPanelComponents.toggle(
				"screen.elytra_overdrive.flight.firework_preference",
				draft.enableHeldFireworkOverdrive(),
				permission.canEditHeldFireworkPreference(),
				draft::enableHeldFireworkOverdrive
		));
		if (!permission.canEditHeldFireworkPreference()) {
			panel.child(ControlPanelComponents.label(
					Component.translatable("screen.elytra_overdrive.firework.locked"),
					ControlTerminalTheme.WARNING
			));
			panel.child(ControlPanelComponents.label(
					Component.translatable("screen.elytra_overdrive.firework.locked_help"),
					ControlTerminalTheme.SECONDARY
			).maxWidth(360));
		}

		Minecraft client = Minecraft.getInstance();
		boolean enchantmentAvailable = false;
		boolean holdingRocket = false;
		if (client.player != null) {
			ItemStack chest = client.player.getItemBySlot(EquipmentSlot.CHEST);
			enchantmentAvailable = OverdriveEnchantments.hasOverdrive(client.player, chest);
			holdingRocket = HeldFireworkRules.isHoldingRocket(
					client.player.getMainHandItem(), client.player.getOffhandItem()
			);
		}
		panel.child(ControlPanelComponents.row(
				Component.translatable("screen.elytra_overdrive.source.enchantment"),
				Component.translatable(enchantmentAvailable
						? "screen.elytra_overdrive.state.available"
						: "screen.elytra_overdrive.state.not_equipped")
		));
		panel.child(ControlPanelComponents.row(
				Component.translatable("screen.elytra_overdrive.source.held_firework"),
				Component.translatable(fireworkStatusKey(permission, draft, holdingRocket))
		));
		return panel;
	}

	private static String fireworkStatusKey(
			ConfigPermissionState permission,
			ConfigDraft draft,
			boolean holdingRocket
	) {
		if (!permission.canEditHeldFireworkPreference()) return "screen.elytra_overdrive.state.server_denied";
		if (!draft.enableHeldFireworkOverdrive()) return "screen.elytra_overdrive.state.player_disabled";
		if (permission.authorization() == ConfigPermissionState.Authorization.LOCAL_OWNER_OVERRIDE) {
			return "screen.elytra_overdrive.state.owner_authorized";
		}
		return holdingRocket
				? "screen.elytra_overdrive.state.enabled"
				: "screen.elytra_overdrive.state.no_firework";
	}
}
