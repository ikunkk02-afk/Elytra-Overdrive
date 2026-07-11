package io.github.ikunkk02.elytraoverdrive.client;

import io.github.ikunkk02.elytraoverdrive.flight.FlightSpeedController;

public final class ClientOverdriveState {
	private static double effectiveMultiplier = FlightSpeedController.MIN_MULTIPLIER;
	private static boolean active;
	private static double previousFovBoost;
	private static double fovBoost;

	private ClientOverdriveState() {
	}

	public static void update(double multiplier, boolean isActive) {
		if (!Double.isFinite(multiplier)
				|| multiplier < FlightSpeedController.MIN_MULTIPLIER
				|| multiplier > FlightSpeedController.MAX_MULTIPLIER) {
			reset();
			return;
		}

		effectiveMultiplier = multiplier;
		active = isActive && multiplier > FlightSpeedController.MIN_MULTIPLIER;
	}

	public static void tickFov(double speed, boolean enabled) {
		previousFovBoost = fovBoost;
		double target = 0.0;
		if (enabled && active && Double.isFinite(speed) && speed > 0.9) {
			target = Math.min(15.0, Math.max(0.0, (speed - 0.9) * 6.0));
		}
		fovBoost += (target - fovBoost) * 0.15;
		if (Math.abs(fovBoost) < 1.0E-4 && target == 0.0) {
			fovBoost = 0.0;
		}
	}

	public static double interpolatedFovBoost(float partialTick) {
		double partial = Math.max(0.0, Math.min(1.0, partialTick));
		return previousFovBoost + (fovBoost - previousFovBoost) * partial;
	}

	public static double effectiveMultiplier() {
		return effectiveMultiplier;
	}

	public static boolean active() {
		return active;
	}

	public static void reset() {
		effectiveMultiplier = FlightSpeedController.MIN_MULTIPLIER;
		active = false;
		previousFovBoost = 0.0;
		fovBoost = 0.0;
	}
}
