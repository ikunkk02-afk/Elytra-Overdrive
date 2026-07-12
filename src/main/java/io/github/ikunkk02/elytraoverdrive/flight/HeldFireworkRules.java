package io.github.ikunkk02.elytraoverdrive.flight;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class HeldFireworkRules {
	private HeldFireworkRules() {
	}

	public static boolean isHoldingRocket(ItemStack mainHand, ItemStack offHand) {
		return mainHand.is(Items.FIREWORK_ROCKET) || offHand.is(Items.FIREWORK_ROCKET);
	}
}
