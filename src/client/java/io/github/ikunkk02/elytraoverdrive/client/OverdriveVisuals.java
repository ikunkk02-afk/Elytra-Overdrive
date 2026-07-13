package io.github.ikunkk02.elytraoverdrive.client;

import io.github.ikunkk02.elytraoverdrive.ElytraOverdrive;
import io.github.ikunkk02.elytraoverdrive.config.VisualPreset;
import io.github.ikunkk02.elytraoverdrive.flight.FlightActivationSource;
import io.github.ikunkk02.elytraoverdrive.flight.FlightVelocity;
import io.github.ikunkk02.elytraoverdrive.visual.FlightBasis;
import io.github.ikunkk02.elytraoverdrive.visual.SonicBoomState;
import io.github.ikunkk02.elytraoverdrive.visual.VisualIntensity;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class OverdriveVisuals {
	private static final DustColorTransitionOptions CYAN_TO_WHITE = new DustColorTransitionOptions(
			new Vector3f(0.18F, 0.72F, 0.92F),
			new Vector3f(0.95F, 0.98F, 1.0F),
			0.65F
	);
	private static final SonicBoomState SONIC_BOOM = new SonicBoomState(4.0, 3.2, 60);
	private static final int SONIC_RING_PARTICLES = 24;

	private OverdriveVisuals() {
	}

	public static void initialize() {
		ClientTickEvents.END_CLIENT_TICK.register(OverdriveVisuals::tick);
	}

	private static void tick(Minecraft client) {
		if (client.player == null || client.level == null) {
			SONIC_BOOM.reset();
			return;
		}

		double speed = client.player.getDeltaMovement().length();
		VisualPreset preset = ElytraOverdrive.CONFIG.visualPreset();
		boolean reduceMotion = ElytraOverdrive.CONFIG.reduceMotion();

		boolean confirmedActive = ClientOverdriveState.active()
				&& ClientOverdriveState.activationSource() != FlightActivationSource.NONE;
		VisualIntensity intensity = VisualIntensity.fromSpeed(
				speed,
				ClientOverdriveState.effectiveMultiplier(),
				confirmedActive,
				preset,
				reduceMotion
		);
		boolean particlesEnabled = ElytraOverdrive.CONFIG.showHighSpeedParticles()
				&& confirmedActive
				&& intensity.particleBudget() > 0;
		boolean emitRing = SONIC_BOOM.tick(
				speed,
				confirmedActive,
				particlesEnabled
						&& ElytraOverdrive.CONFIG.enableSonicBoomRing()
						&& intensity.sonicRingAllowed()
		);
		if (!particlesEnabled) return;

		FlightBasis basis = FlightBasis.fromForward(fromVec3(client.player.getLookAngle()));
		Vec3 forward = toVec3(basis.forward());
		Vec3 right = toVec3(basis.right());
		Vec3 up = toVec3(basis.up());
		Vec3 center = client.player.position()
				.add(0.0, client.player.getBbHeight() * 0.52, 0.0)
				.subtract(forward.scale(0.35));

		int remaining = intensity.particleBudget();
		if (ElytraOverdrive.CONFIG.enableWingtipTrails()) {
			remaining = emitWingtipTrails(client, center, forward, right, up, intensity, remaining);
		}
		if (remaining > 0 && ElytraOverdrive.CONFIG.enableSpeedLines()) {
			emitSpeedLines(client, center, forward, right, up, intensity, remaining);
		}
		if (emitRing) {
			emitSonicRing(client, center, right, up);
		}
	}

	private static int emitWingtipTrails(
			Minecraft client,
			Vec3 center,
			Vec3 forward,
			Vec3 right,
			Vec3 up,
			VisualIntensity intensity,
			int budget
	) {
		Vec3 leftTip = center.subtract(right.scale(0.95)).add(up.scale(0.12));
		Vec3 rightTip = center.add(right.scale(0.95)).add(up.scale(0.12));
		for (int point = 0; point < intensity.wingtipPointsPerWing() && budget >= 2; point++) {
			double distance = intensity.wingtipLength()
					* (point + 1.0) / Math.max(1, intensity.wingtipPointsPerWing());
			Vec3 offset = forward.scale(-distance);
			addDust(client, leftTip.add(offset));
			addDust(client, rightTip.add(offset));
			budget -= 2;
		}
		return budget;
	}

	private static void emitSpeedLines(
			Minecraft client,
			Vec3 center,
			Vec3 forward,
			Vec3 right,
			Vec3 up,
			VisualIntensity intensity,
			int budget
	) {
		for (int line = 0; line < intensity.speedLineCount() && budget > 0; line++) {
			double angle = client.player.getRandom().nextDouble() * Math.PI * 2.0;
			double radius = 0.85 + client.player.getRandom().nextDouble() * 0.75;
			Vec3 origin = center
					.add(right.scale(Math.cos(angle) * radius))
					.add(up.scale(Math.sin(angle) * radius))
					.add(forward.scale(0.6));
			for (int point = 0; point < intensity.speedLinePoints() && budget > 0; point++) {
				addDust(client, origin.add(forward.scale(-0.32 * point)));
				budget--;
			}
		}
	}

	private static void emitSonicRing(Minecraft client, Vec3 center, Vec3 right, Vec3 up) {
		int count = Math.min(28, SONIC_RING_PARTICLES);
		for (int point = 0; point < count; point++) {
			double angle = Math.PI * 2.0 * point / count;
			Vec3 position = center
					.add(right.scale(Math.cos(angle) * 1.65))
					.add(up.scale(Math.sin(angle) * 1.65));
			addDust(client, position);
		}
	}

	private static void addDust(Minecraft client, Vec3 position) {
		client.level.addParticle(CYAN_TO_WHITE, position.x, position.y, position.z, 0.0, 0.0, 0.0);
	}

	private static FlightVelocity fromVec3(Vec3 vector) {
		return new FlightVelocity(vector.x, vector.y, vector.z);
	}

	private static Vec3 toVec3(FlightVelocity vector) {
		return new Vec3(vector.x(), vector.y(), vector.z());
	}
}
