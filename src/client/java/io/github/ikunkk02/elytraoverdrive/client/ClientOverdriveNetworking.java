package io.github.ikunkk02.elytraoverdrive.client;

import io.github.ikunkk02.elytraoverdrive.ElytraOverdrive;
import io.github.ikunkk02.elytraoverdrive.client.mixin.ClientCommonPacketListenerImplAccessor;
import io.github.ikunkk02.elytraoverdrive.network.OverdriveStateS2CPayload;
import io.github.ikunkk02.elytraoverdrive.network.HeldFireworkPreferenceC2SPayload;
import io.github.ikunkk02.elytraoverdrive.network.RequiredClientPayload;
import io.github.ikunkk02.elytraoverdrive.network.SelectedMultiplierC2SPayload;
import io.github.ikunkk02.elytraoverdrive.flight.FlightSpeedController;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.chat.Component;

public final class ClientOverdriveNetworking {
	private ClientOverdriveNetworking() {
	}

	public static void initialize() {
		ClientConfigurationNetworking.registerGlobalReceiver(RequiredClientPayload.TYPE, (payload, context) -> {
			if (payload.protocolVersion() != RequiredClientPayload.CURRENT_PROTOCOL) {
				((ClientCommonPacketListenerImplAccessor) context.networkHandler())
						.elytraOverdrive$getConnection()
						.disconnect(Component.translatable("disconnect.elytra_overdrive.protocol_mismatch"));
			}
		});

		ClientPlayNetworking.registerGlobalReceiver(OverdriveStateS2CPayload.TYPE, (payload, context) ->
				ClientOverdriveState.update(
						payload.effectiveMultiplier(),
						payload.active(),
						payload.activationSourceOrdinal(),
						payload.allowHeldFireworkOverdrive(),
						payload.localOwnerOverride(),
						payload.acceptedHeldFireworkPreference()
				)
		);

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> sendPreferences());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientOverdriveState.reset());
	}

	public static void sendPreferences() {
		try {
			var validatedMultiplier = FlightSpeedController.validateClientMultiplier(
					ElytraOverdrive.CONFIG.playerSelectedMultiplier()
			);
			if (validatedMultiplier.isPresent()
					&& ClientPlayNetworking.canSend(SelectedMultiplierC2SPayload.TYPE)) {
				ClientPlayNetworking.send(
						new SelectedMultiplierC2SPayload(validatedMultiplier.getAsDouble())
				);
			}
			if (ClientPlayNetworking.canSend(HeldFireworkPreferenceC2SPayload.TYPE)) {
				ClientPlayNetworking.send(new HeldFireworkPreferenceC2SPayload(
						ElytraOverdrive.CONFIG.enableHeldFireworkOverdrive()
				));
			}
		} catch (IllegalStateException ignored) {
			// Config hooks may fire while no play connection exists; JOIN performs the initial sync.
		}
	}
}
