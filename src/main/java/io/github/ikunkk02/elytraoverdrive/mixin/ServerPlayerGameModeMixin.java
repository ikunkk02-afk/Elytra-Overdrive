package io.github.ikunkk02.elytraoverdrive.mixin;

import io.github.ikunkk02.elytraoverdrive.breach.BreachBreakContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {
	@Shadow
	protected ServerPlayer player;

	@Redirect(
			method = "destroyBlock",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/Item;canAttackBlock(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;)Z"
			)
	)
	private boolean elytraOverdrive$allowBreachInCreative(
			Item item,
			BlockState state,
			Level level,
			BlockPos pos,
			Player player
	) {
		return BreachBreakContext.matches(player, pos) || item.canAttackBlock(state, level, pos, player);
	}

	@Redirect(
			method = "destroyBlock",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/level/ServerPlayer;hasCorrectToolForDrops(Lnet/minecraft/world/level/block/state/BlockState;)Z"
			)
	)
	private boolean elytraOverdrive$suppressNativeBreachDrops(ServerPlayer player, BlockState state, BlockPos pos) {
		return !BreachBreakContext.matches(player, pos) && player.hasCorrectToolForDrops(state);
	}

	@Redirect(
			method = "destroyBlock",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/ItemStack;mineBlock(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;)V"
			)
	)
	private void elytraOverdrive$suppressNativeBreachDurability(
			ItemStack stack,
			Level level,
			BlockState state,
			BlockPos pos,
			Player player
	) {
		if (!BreachBreakContext.matches(player, pos)) {
			stack.mineBlock(level, state, pos, player);
		}
	}
}
