package io.github.ikunkk02.elytraoverdrive.network;

import io.github.ikunkk02.elytraoverdrive.ElytraOverdrive;
import io.github.ikunkk02.elytraoverdrive.flight.OverdriveFlightHandler;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;

public final class OverdriveNetworking {
	private OverdriveNetworking() {
	}

	public static void initialize() {
		PayloadTypeRegistry.configurationS2C().register(RequiredClientPayload.TYPE, RequiredClientPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SelectedMultiplierC2SPayload.TYPE, SelectedMultiplierC2SPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(OverdriveStateS2CPayload.TYPE, OverdriveStateS2CPayload.CODEC);

		ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> {
			if (!ServerConfigurationNetworking.canSend(handler, RequiredClientPayload.TYPE)) {
				handler.disconnect(Component.translatable("disconnect.elytra_overdrive.client_required"));
				return;
			}

			ServerConfigurationNetworking.send(
					handler,
					new RequiredClientPayload(RequiredClientPayload.CURRENT_PROTOCOL)
			);
		});

		ServerPlayNetworking.registerGlobalReceiver(SelectedMultiplierC2SPayload.TYPE, (payload, context) -> {
			if (!OverdriveFlightHandler.updateSelectedMultiplier(context.player(), payload.multiplier())) {
				ElytraOverdrive.LOGGER.debug(
						"Rejected invalid overdrive multiplier from player {}",
						context.player().getGameProfile().getName()
				);
			}
		});
	}
}
