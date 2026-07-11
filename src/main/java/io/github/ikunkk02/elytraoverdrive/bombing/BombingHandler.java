package io.github.ikunkk02.elytraoverdrive.bombing;

import io.github.ikunkk02.elytraoverdrive.ElytraOverdrive;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public final class BombingHandler {
	private static final Map<UUID, BombingSessionState> SESSIONS = new HashMap<>();
	private static final double BACK_OFFSET = 0.35;
	private static final double DOWN_OFFSET = 0.9;

	private BombingHandler() {
	}

	public static void initialize() {
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> resetRuntimeState(handler.player));
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> resetRuntimeState(newPlayer));
		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> resetRuntimeState(player));
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> SESSIONS.clear());
		ServerTickEvents.END_WORLD_TICK.register(BombingHandler::tickWorld);
	}

	public static void start(ServerPlayer player) {
		BombingSessionState state = SESSIONS.computeIfAbsent(player.getUUID(), ignored -> new BombingSessionState());
		if (state.active() || !canBomb(player)) {
			return;
		}

		if (dropBomb(player)) {
			if (canBomb(player)) {
				state.start(ElytraOverdrive.CONFIG.bombingIntervalTicks());
			} else {
				state.stop();
			}
		}
	}

	public static void stop(ServerPlayer player) {
		BombingSessionState state = SESSIONS.get(player.getUUID());
		if (state != null) {
			state.stop();
		}
	}

	public static void resetRuntimeState(ServerPlayer player) {
		SESSIONS.remove(player.getUUID());
	}

	private static void tickWorld(ServerLevel world) {
		for (ServerPlayer player : world.players()) {
			BombingSessionState state = SESSIONS.get(player.getUUID());
			if (state == null || !state.active()) {
				continue;
			}
			if (!canBomb(player)) {
				state.stop();
				continue;
			}
			if (state.tick()) {
				if (!dropBomb(player)) {
					state.stop();
				} else if (canBomb(player)) {
					state.resetInterval(ElytraOverdrive.CONFIG.bombingIntervalTicks());
				} else {
					state.stop();
				}
			}
		}
	}

	private static boolean canBomb(ServerPlayer player) {
		return ElytraOverdrive.CONFIG.enableBombing()
				&& player.isAlive()
				&& !player.isSpectator()
				&& player.isFallFlying()
				&& player.getMainHandItem().is(Items.FLINT_AND_STEEL)
				&& player.getOffhandItem().is(Items.TNT)
				&& !player.getOffhandItem().isEmpty();
	}

	private static boolean dropBomb(ServerPlayer player) {
		if (!canBomb(player)) {
			return false;
		}

		Vec3 playerVelocity = player.getDeltaMovement();
		Vec3 horizontalDirection = new Vec3(playerVelocity.x, 0.0, playerVelocity.z).normalize();
		if (!isFinite(horizontalDirection) || horizontalDirection.lengthSqr() < 1.0E-8) {
			Vec3 look = player.getLookAngle();
			horizontalDirection = new Vec3(look.x, 0.0, look.z).normalize();
		}
		if (!isFinite(horizontalDirection)) {
			horizontalDirection = Vec3.ZERO;
		}

		double x = player.getX() - horizontalDirection.x * BACK_OFFSET;
		double y = player.getBoundingBox().minY - DOWN_OFFSET;
		double z = player.getZ() - horizontalDirection.z * BACK_OFFSET;
		PrimedTnt tnt = new PrimedTnt(player.serverLevel(), x, y, z, player);
		BombVelocity velocity = BombingRules.calculateVelocity(
				new BombVelocity(playerVelocity.x, playerVelocity.y, playerVelocity.z),
				ElytraOverdrive.CONFIG.bombHorizontalInertia()
		);
		tnt.setDeltaMovement(velocity.x(), velocity.y(), velocity.z());
		tnt.setFuse(Math.max(20, Math.min(200, ElytraOverdrive.CONFIG.bombFuseTicks())));
		if (!player.serverLevel().addFreshEntity(tnt)) {
			return false;
		}

		player.serverLevel().playSound(null, tnt, SoundEvents.TNT_PRIMED, SoundSource.PLAYERS, 0.8F, 1.0F);
		player.serverLevel().sendParticles(ParticleTypes.SMOKE, x, y, z, 2, 0.08, 0.08, 0.08, 0.0);
		if (!player.getAbilities().instabuild) {
			player.getOffhandItem().shrink(1);
			ItemStack flintAndSteel = player.getMainHandItem();
			flintAndSteel.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
		}
		return true;
	}

	private static boolean isFinite(Vec3 vector) {
		return Double.isFinite(vector.x) && Double.isFinite(vector.y) && Double.isFinite(vector.z);
	}
}
