package io.github.ikunkk02.elytraoverdrive.breach;

import io.github.ikunkk02.elytraoverdrive.ElytraOverdrive;
import io.github.ikunkk02.elytraoverdrive.enchantment.OverdriveEnchantments;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class BreachHandler {
	private static final Map<UUID, BreachSessionState> SESSIONS = new HashMap<>();

	private BreachHandler() {
	}

	public static void initialize() {
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> resetRuntimeState(handler.player));
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> resetRuntimeState(newPlayer));
		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> resetRuntimeState(player));
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> SESSIONS.clear());
		ServerTickEvents.END_WORLD_TICK.register(BreachHandler::tickWorld);
	}

	public static void resetRuntimeState(ServerPlayer player) {
		SESSIONS.remove(player.getUUID());
	}

	private static void tickWorld(ServerLevel world) {
		for (ServerPlayer player : world.players()) {
			tickPlayer(player);
		}
	}

	private static void tickPlayer(ServerPlayer player) {
		BreachSessionState session = SESSIONS.computeIfAbsent(player.getUUID(), ignored -> new BreachSessionState());
		ItemStack trident = player.getMainHandItem();
		int level = OverdriveEnchantments.getBreachLevel(player, trident);
		Vec3 velocity = player.getDeltaMovement();
		Vec3 look = player.getLookAngle();
		if (!canActivate(player, trident, level, velocity, look)) {
			session.reset();
			return;
		}

		BreachVector current = bodyCenter(player);
		if (!session.initialized()) {
			session.updatePosition(current);
			return;
		}
		BreachVector previous = session.previousPosition();
		session.updatePosition(current);
		int maximum = Math.max(1, Math.min(128, ElytraOverdrive.CONFIG.maximumBreachBlocksPerTick()));
		for (BreachBlockPosition candidate : BreachPathSampler.sample(
				previous,
				current,
				fromVec3(velocity),
				level,
				maximum
		)) {
			if (!isUsableTrident(player.getMainHandItem())) {
				session.reset();
				break;
			}
			tryBreak(player, new BlockPos(candidate.x(), candidate.y(), candidate.z()), level);
		}
	}

	private static boolean canActivate(
			ServerPlayer player,
			ItemStack trident,
			int level,
			Vec3 velocity,
			Vec3 look
	) {
		return ElytraOverdrive.CONFIG.enableTridentBreach()
				&& player.isAlive()
				&& !player.isSpectator()
				&& player.isFallFlying()
				&& level > 0
				&& isUsableTrident(trident)
				&& BreachRules.canActivate(
						fromVec3(velocity),
						fromVec3(look),
						ElytraOverdrive.CONFIG.minimumBreachSpeed()
				);
	}

	private static boolean tryBreak(ServerPlayer player, BlockPos pos, int level) {
		ServerLevel world = player.serverLevel();
		if (!world.isInWorldBounds(pos)
				|| !world.isLoaded(pos)
				|| player.server.isUnderSpawnProtection(world, pos, player)) {
			return false;
		}
		BlockState state = world.getBlockState(pos);
		if (state.isAir() || state.is(BreachTags.PROTECTED)) {
			return false;
		}
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (ElytraOverdrive.CONFIG.protectBlockEntitiesFromBreach() && blockEntity != null) {
			return false;
		}
		float hardness = state.getDestroySpeed(world, pos);
		if (!BreachRules.canBreakHardness(level, hardness)) {
			return false;
		}

		boolean destroyed = BreachBreakContext.run(player, pos, () -> player.gameMode.destroyBlock(pos));
		if (!destroyed) {
			return false;
		}
		if (ElytraOverdrive.CONFIG.breachDropsBlocks()) {
			Block.dropResources(state, world, pos, blockEntity, player, new ItemStack(Items.NETHERITE_PICKAXE));
		}
		if (!player.getAbilities().instabuild) {
			int cost = BreachRules.durabilityCost(hardness, ElytraOverdrive.CONFIG.breachDurabilityMultiplier());
			player.getMainHandItem().hurtAndBreak(cost, player, EquipmentSlot.MAINHAND);
		}
		return true;
	}

	private static boolean isUsableTrident(ItemStack stack) {
		return stack.is(Items.TRIDENT)
				&& !stack.isEmpty()
				&& (!stack.isDamageableItem() || stack.getDamageValue() < stack.getMaxDamage() - 1);
	}

	private static BreachVector bodyCenter(ServerPlayer player) {
		return new BreachVector(player.getX(), player.getY() + player.getBbHeight() * 0.5, player.getZ());
	}

	private static BreachVector fromVec3(Vec3 vector) {
		return new BreachVector(vector.x, vector.y, vector.z);
	}
}
