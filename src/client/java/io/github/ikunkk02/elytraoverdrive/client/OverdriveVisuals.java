package io.github.ikunkk02.elytraoverdrive.client;

import io.github.ikunkk02.elytraoverdrive.ElytraOverdrive;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;

public final class OverdriveVisuals {
	private static final double PARTICLE_SPEED_THRESHOLD = 0.9;
	private static int particleTick;

	private OverdriveVisuals() {
	}

	public static void initialize() {
		ClientTickEvents.END_CLIENT_TICK.register(OverdriveVisuals::tick);
	}

	private static void tick(Minecraft client) {
		if (client.player == null || client.level == null) {
			ClientOverdriveState.tickFov(0.0, false);
			particleTick = 0;
			return;
		}

		double speed = client.player.getDeltaMovement().length();
		ClientOverdriveState.tickFov(speed, ElytraOverdrive.CONFIG.enableHighSpeedFov());
		if (!ElytraOverdrive.CONFIG.showHighSpeedParticles()
				|| !ClientOverdriveState.active()
				|| ClientOverdriveState.effectiveMultiplier() <= 1.0
				|| !Double.isFinite(speed)
				|| speed <= PARTICLE_SPEED_THRESHOLD
				|| ++particleTick % 4 != 0) {
			return;
		}

		Vec3 behind = client.player.position().subtract(client.player.getLookAngle().scale(0.9));
		for (int i = 0; i < 2; i++) {
			double spreadX = (client.player.getRandom().nextDouble() - 0.5) * 0.35;
			double spreadY = (client.player.getRandom().nextDouble() - 0.5) * 0.25;
			double spreadZ = (client.player.getRandom().nextDouble() - 0.5) * 0.35;
			client.level.addParticle(
					ParticleTypes.CLOUD,
					behind.x + spreadX,
					behind.y + client.player.getBbHeight() * 0.45 + spreadY,
					behind.z + spreadZ,
					0.0,
					0.0,
					0.0
			);
		}
	}
}
