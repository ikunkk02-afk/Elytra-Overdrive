package io.github.ikunkk02.elytraoverdrive.client.mixin;

import io.github.ikunkk02.elytraoverdrive.ElytraOverdrive;
import io.github.ikunkk02.elytraoverdrive.client.ClientOverdriveState;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
	/** Adds a transient, smoothed visual offset without changing the saved FOV option. */
	@Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
	private void elytraOverdrive$applyFovBoost(
			Camera camera,
			float partialTick,
			boolean useFovSetting,
			CallbackInfoReturnable<Double> cir
	) {
		if (!useFovSetting || !ElytraOverdrive.CONFIG.enableHighSpeedFov()) {
			return;
		}

		cir.setReturnValue(cir.getReturnValue() + ClientOverdriveState.interpolatedFovBoost(partialTick));
	}
}
