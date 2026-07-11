package io.github.ikunkk02.elytraoverdrive.breach;

import java.util.UUID;
import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

public final class BreachBreakContext {
	private static final ThreadLocal<ActiveBreak> ACTIVE_BREAK = new ThreadLocal<>();

	private BreachBreakContext() {
	}

	public static boolean run(Player player, BlockPos pos, BooleanSupplier operation) {
		if (ACTIVE_BREAK.get() != null) {
			return false;
		}
		ACTIVE_BREAK.set(new ActiveBreak(player.getUUID(), pos.immutable()));
		try {
			return operation.getAsBoolean();
		} finally {
			ACTIVE_BREAK.remove();
		}
	}

	public static boolean matches(Player player, BlockPos pos) {
		ActiveBreak active = ACTIVE_BREAK.get();
		return active != null && active.playerId().equals(player.getUUID()) && active.pos().equals(pos);
	}

	private record ActiveBreak(UUID playerId, BlockPos pos) {
	}
}
