package io.github.ikunkk02.elytraoverdrive.mixin;

import io.github.ikunkk02.elytraoverdrive.ElytraOverdrive;
import io.github.ikunkk02.elytraoverdrive.enchantment.ElytraEnchantingRules;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemMixin {
	@Inject(method = "getEnchantmentValue", at = @At("RETURN"), cancellable = true)
	private void elytraOverdrive$setElytraEnchantability(CallbackInfoReturnable<Integer> cir) {
		if ((Object)this == Items.ELYTRA) {
			cir.setReturnValue(ElytraEnchantingRules.clampEnchantability(
					ElytraOverdrive.CONFIG.elytraEnchantability()
			));
		}
	}
}
