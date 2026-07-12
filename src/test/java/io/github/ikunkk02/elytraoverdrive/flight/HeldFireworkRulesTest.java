package io.github.ikunkk02.elytraoverdrive.flight;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeldFireworkRulesTest {
	@BeforeAll
	static void bootstrapMinecraftRegistries() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	void eitherHandCanHoldExactlyOneRocketWithoutConsumption() {
		ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET, 1);

		assertTrue(HeldFireworkRules.isHoldingRocket(rocket, ItemStack.EMPTY));
		assertTrue(HeldFireworkRules.isHoldingRocket(ItemStack.EMPTY, rocket));
		assertEquals(1, rocket.getCount());
	}

	@Test
	void unrelatedItemsDoNotQualify() {
		assertFalse(HeldFireworkRules.isHoldingRocket(new ItemStack(Items.STICK), ItemStack.EMPTY));
	}
}
