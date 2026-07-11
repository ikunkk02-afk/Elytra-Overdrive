package io.github.ikunkk02.elytraoverdrive.client;

import io.github.ikunkk02.elytraoverdrive.ElytraOverdrive;
import io.github.ikunkk02.elytraoverdrive.client.mixin.ClientCommonPacketListenerImplAccessor;
import io.github.ikunkk02.elytraoverdrive.network.OverdriveStateS2CPayload;
import io.github.ikunkk02.elytraoverdrive.network.RequiredClientPayload;
import io.github.ikunkk02.elytraoverdrive.network.SelectedMultiplierC2SPayload;
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
				ClientOverdriveState.update(payload.effectiveMultiplier(), payload.active())
		);

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> sendSelectedMultiplier());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientOverdriveState.reset());
		ElytraOverdrive.CONFIG.subscribeToPlayerSelectedMultiplier(value -> sendSelectedMultiplier());
	}

	private static void sendSelectedMultiplier() {
		try {
			if (ClientPlayNetworking.canSend(SelectedMultiplierC2SPayload.TYPE)) {
				ClientPlayNetworking.send(
						new SelectedMultiplierC2SPayload(ElytraOverdrive.CONFIG.playerSelectedMultiplier())
				);
			}
		} catch (IllegalStateException ignored) {
			// Config hooks may fire while no play connection exists; JOIN performs the initial sync.
		}
	}
}
