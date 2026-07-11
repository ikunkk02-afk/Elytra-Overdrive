package io.github.ikunkk02.elytraoverdrive.enchantment;

public final class ElytraEnchantingRules {
	public static final int MIN_ENCHANTABILITY = 1;
	public static final int MAX_ENCHANTABILITY = 30;

	private ElytraEnchantingRules() {
	}

	public static int clampEnchantability(int value) {
		return Math.max(MIN_ENCHANTABILITY, Math.min(MAX_ENCHANTABILITY, value));
	}
}
