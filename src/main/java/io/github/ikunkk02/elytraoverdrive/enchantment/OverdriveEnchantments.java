package io.github.ikunkk02.elytraoverdrive.enchantment;

import io.github.ikunkk02.elytraoverdrive.ElytraOverdrive;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class OverdriveEnchantments {
	public static final ResourceKey<Enchantment> OVERDRIVE = ResourceKey.create(
			Registries.ENCHANTMENT,
			ElytraOverdrive.id("overdrive")
	);
	public static final ResourceKey<Enchantment> BREACH = ResourceKey.create(
			Registries.ENCHANTMENT,
			ElytraOverdrive.id("breach")
	);

	private OverdriveEnchantments() {
	}

	public static boolean hasOverdrive(LivingEntity entity, ItemStack chestStack) {
		if (!chestStack.is(Items.ELYTRA)) {
			return false;
		}

		var enchantment = entity.registryAccess()
				.registryOrThrow(Registries.ENCHANTMENT)
				.getHolderOrThrow(OVERDRIVE);
		return EnchantmentHelper.getItemEnchantmentLevel(enchantment, chestStack) > 0;
	}

	public static boolean canUseOverdriveElytra(LivingEntity entity) {
		ItemStack chestStack = entity.getItemBySlot(EquipmentSlot.CHEST);
		return chestStack.is(Items.ELYTRA)
				&& ElytraItem.isFlyEnabled(chestStack)
				&& hasOverdrive(entity, chestStack);
	}

	public static int getBreachLevel(LivingEntity entity, ItemStack stack) {
		if (!stack.is(Items.TRIDENT)) {
			return 0;
		}
		var enchantment = entity.registryAccess()
				.registryOrThrow(Registries.ENCHANTMENT)
				.getHolderOrThrow(BREACH);
		return EnchantmentHelper.getItemEnchantmentLevel(enchantment, stack);
	}
}
