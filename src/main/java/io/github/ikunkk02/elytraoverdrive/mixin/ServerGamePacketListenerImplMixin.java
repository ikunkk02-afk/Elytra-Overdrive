package io.github.ikunkk02.elytraoverdrive.mixin;

import io.github.ikunkk02.elytraoverdrive.flight.OverdriveFlightHandler;
import java.util.Set;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.RelativeMovement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
	@Shadow
	public ServerPlayer player;

	/**
	 * Teleport is the narrow boundary where old flight velocity must never survive.
	 * Normal movement remains untouched and all ongoing acceleration stays in the server tick handler.
	 */
	@Inject(
			method = "teleport(DDDFFLjava/util/Set;)V",
			at = @At("HEAD")
	)
	private void elytraOverdrive$clearFlightState(
			double x,
			double y,
			double z,
			float yaw,
			float pitch,
			Set<RelativeMovement> relativeMovements,
			CallbackInfo ci
	) {
		OverdriveFlightHandler.resetRuntimeState(this.player);
	}
}
