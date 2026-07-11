package io.github.ikunkk02.elytraoverdrive.enchantment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ElytraEnchantingRulesTest {
	@Test
	void enchantabilityIsClampedToSafeRange() {
		assertEquals(1, ElytraEnchantingRules.clampEnchantability(-10));
		assertEquals(10, ElytraEnchantingRules.clampEnchantability(10));
		assertEquals(30, ElytraEnchantingRules.clampEnchantability(100));
	}
}
