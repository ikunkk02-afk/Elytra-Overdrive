package io.github.ikunkk02.elytraoverdrive.flight;

import io.github.ikunkk02.elytraoverdrive.ElytraOverdrive;
import io.github.ikunkk02.elytraoverdrive.enchantment.OverdriveEnchantments;
import io.github.ikunkk02.elytraoverdrive.network.OverdriveStateS2CPayload;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public final class OverdriveFlightHandler {
	private static final Map<UUID, FlightSessionState> SESSIONS = new HashMap<>();

	private OverdriveFlightHandler() {
	}

	public static void initialize() {
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			FlightSessionState state = new FlightSessionState();
			SESSIONS.put(handler.player.getUUID(), state);
			synchronize(handler.player, state, FlightSpeedController.MIN_MULTIPLIER, false);
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> SESSIONS.remove(handler.player.getUUID()));
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> resetRuntimeState(newPlayer));
		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> resetRuntimeState(player));
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> SESSIONS.clear());
		ServerTickEvents.END_WORLD_TICK.register(OverdriveFlightHandler::tickWorld);
	}

	public static boolean updateSelectedMultiplier(ServerPlayer player, double multiplier) {
		FlightSessionState state = SESSIONS.computeIfAbsent(player.getUUID(), ignored -> new FlightSessionState());
		return state.updateSelectedMultiplier(multiplier);
	}

	public static void resetRuntimeState(ServerPlayer player) {
		FlightSessionState state = SESSIONS.computeIfAbsent(player.getUUID(), ignored -> new FlightSessionState());
		state.setActive(false);
		player.setDeltaMovement(Vec3.ZERO);
		synchronize(player, state, state.effectiveMultiplier(ElytraOverdrive.CONFIG.serverMaximumMultiplier()), false);
	}

	private static void tickWorld(ServerLevel world) {
		for (ServerPlayer player : world.players()) {
			tickPlayer(player);
		}
	}

	private static void tickPlayer(ServerPlayer player) {
		FlightSessionState state = SESSIONS.computeIfAbsent(player.getUUID(), ignored -> new FlightSessionState());
		double effectiveMultiplier = state.effectiveMultiplier(ElytraOverdrive.CONFIG.serverMaximumMultiplier());
		boolean canActivate = canActivate(player, effectiveMultiplier);

		if (!canActivate) {
			deactivate(player, state, effectiveMultiplier);
			return;
		}

		FlightVelocity current = fromVec3(player.getDeltaMovement());
		FlightVelocity look = fromVec3(player.getLookAngle());
		FlightVelocity next = FlightSpeedController.calculateNextVelocity(current, look, effectiveMultiplier);
		if (!next.equals(current)) {
			player.setDeltaMovement(toVec3(next));
			player.hurtMarked = true;
		}

		state.setActive(true);
		tickDurability(player, state, effectiveMultiplier);
		synchronize(player, state, effectiveMultiplier, true);
	}

	private static boolean canActivate(ServerPlayer player, double effectiveMultiplier) {
		if (!ElytraOverdrive.CONFIG.enableHighSpeedFlight()
				|| effectiveMultiplier <= FlightSpeedController.MIN_MULTIPLIER
				|| player.isSpectator()
				|| !player.isFallFlying()) {
			return false;
		}

		ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
		return chest.is(Items.ELYTRA)
				&& ElytraItem.isFlyEnabled(chest)
				&& OverdriveEnchantments.hasOverdrive(player, chest);
	}

	private static void tickDurability(ServerPlayer player, FlightSessionState state, double effectiveMultiplier) {
		if (!ElytraOverdrive.CONFIG.extraDurabilityDamage()
				|| effectiveMultiplier <= FlightSpeedController.MIN_MULTIPLIER
				|| player.getAbilities().instabuild) {
			state.resetDurabilityTicks();
			return;
		}

		int interval = Math.max(10, Math.min(200, ElytraOverdrive.CONFIG.extraDurabilityIntervalTicks()));
		if (state.incrementDurabilityTicks() < interval) {
			return;
		}

		state.resetDurabilityTicks();
		ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
		chest.hurtAndBreak(1, player, EquipmentSlot.CHEST);
		if (!ElytraItem.isFlyEnabled(chest)) {
			deactivate(player, state, effectiveMultiplier);
		}
	}

	private static void deactivate(ServerPlayer player, FlightSessionState state, double effectiveMultiplier) {
		if (state.active()) {
			FlightVelocity limited = FlightSpeedController.applySpeedLimit(
					fromVec3(player.getDeltaMovement()),
					FlightSpeedController.VANILLA_DEACTIVATION_SPEED_LIMIT
			);
			player.setDeltaMovement(toVec3(limited));
			player.hurtMarked = true;
		}

		state.setActive(false);
		synchronize(player, state, effectiveMultiplier, false);
	}

	private static void synchronize(
			ServerPlayer player,
			FlightSessionState state,
			double effectiveMultiplier,
			boolean active
	) {
		if (!state.needsSynchronization(effectiveMultiplier, active)
				|| !ServerPlayNetworking.canSend(player, OverdriveStateS2CPayload.TYPE)) {
			return;
		}

		ServerPlayNetworking.send(player, new OverdriveStateS2CPayload(effectiveMultiplier, active));
		state.markSynchronized(effectiveMultiplier, active);
	}

	private static FlightVelocity fromVec3(Vec3 vector) {
		return new FlightVelocity(vector.x, vector.y, vector.z);
	}

	private static Vec3 toVec3(FlightVelocity velocity) {
		return new Vec3(velocity.x(), velocity.y(), velocity.z());
	}
}
