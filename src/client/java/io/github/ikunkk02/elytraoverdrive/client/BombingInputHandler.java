package io.github.ikunkk02.elytraoverdrive.client;

import io.github.ikunkk02.elytraoverdrive.ElytraOverdrive;
import io.github.ikunkk02.elytraoverdrive.network.StartBombingC2SPayload;
import io.github.ikunkk02.elytraoverdrive.network.StopBombingC2SPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class BombingInputHandler {
	private static boolean bombingInputActive;

	private BombingInputHandler() {
	}

	public static void initialize() {
		UseItemCallback.EVENT.register((player, world, hand) -> {
			ItemStack stack = player.getItemInHand(hand);
			if (world.isClientSide && hand == InteractionHand.MAIN_HAND && canStartLocally(player)) {
				if (!bombingInputActive && ClientPlayNetworking.canSend(StartBombingC2SPayload.TYPE)) {
					ClientPlayNetworking.send(new StartBombingC2SPayload());
					bombingInputActive = true;
				}
				return InteractionResultHolder.fail(stack);
			}
			return InteractionResultHolder.pass(stack);
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (!bombingInputActive) {
				return;
			}
			if (client.player == null || !client.options.keyUse.isDown() || !canStartLocally(client.player)) {
				stop();
			}
		});
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> bombingInputActive = false);
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> bombingInputActive = false);
	}

	private static boolean canStartLocally(net.minecraft.world.entity.player.Player player) {
		return ElytraOverdrive.CONFIG.enableBombing()
				&& player.isAlive()
				&& !player.isSpectator()
				&& player.isFallFlying()
				&& player.getMainHandItem().is(Items.FLINT_AND_STEEL)
				&& player.getOffhandItem().is(Items.TNT);
	}

	private static void stop() {
		if (ClientPlayNetworking.canSend(StopBombingC2SPayload.TYPE)) {
			ClientPlayNetworking.send(new StopBombingC2SPayload());
		}
		bombingInputActive = false;
	}
}
