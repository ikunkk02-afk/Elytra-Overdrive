package io.github.ikunkk02.elytraoverdrive.client;

import io.github.ikunkk02.elytraoverdrive.flight.FlightSpeedController;
import io.github.ikunkk02.elytraoverdrive.flight.FlightActivationSource;

public final class ClientOverdriveState {
	private static double effectiveMultiplier = FlightSpeedController.MIN_MULTIPLIER;
	private static boolean active;
	private static FlightActivationSource activationSource = FlightActivationSource.NONE;
	private static boolean serverAllowsHeldFirework;
	private static boolean localOwnerOverride;
	private static boolean serverAcceptedFireworkPreference;
	private static double previousFovBoost;
	private static double fovBoost;

	private ClientOverdriveState() {
	}

	public static void update(
			double multiplier,
			boolean isActive,
			int activationSourceOrdinal,
			boolean serverPolicy,
			boolean ownerOverride,
			boolean acceptedPreference
	) {
		if (!Double.isFinite(multiplier)
				|| multiplier < FlightSpeedController.MIN_MULTIPLIER
				|| multiplier > FlightSpeedController.MAX_MULTIPLIER
				|| activationSourceOrdinal < 0
				|| activationSourceOrdinal >= FlightActivationSource.values().length) {
			reset();
			return;
		}

		activationSource = FlightActivationSource.fromNetworkOrdinal(activationSourceOrdinal);
		effectiveMultiplier = multiplier;
		active = isActive
				&& multiplier > FlightSpeedController.MIN_MULTIPLIER
				&& activationSource != FlightActivationSource.NONE;
		serverAllowsHeldFirework = serverPolicy;
		localOwnerOverride = ownerOverride;
		serverAcceptedFireworkPreference = acceptedPreference && (serverPolicy || ownerOverride);
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

	public static FlightActivationSource activationSource() {
		return activationSource;
	}

	public static boolean serverAllowsHeldFirework() {
		return serverAllowsHeldFirework;
	}

	public static boolean localOwnerOverride() {
		return localOwnerOverride;
	}

	public static boolean serverAcceptedFireworkPreference() {
		return serverAcceptedFireworkPreference;
	}

	public static boolean mayRequestHeldFirework() {
		return serverAllowsHeldFirework || localOwnerOverride;
	}

	public static void reset() {
		effectiveMultiplier = FlightSpeedController.MIN_MULTIPLIER;
		active = false;
		activationSource = FlightActivationSource.NONE;
		serverAllowsHeldFirework = false;
		localOwnerOverride = false;
		serverAcceptedFireworkPreference = false;
		previousFovBoost = 0.0;
		fovBoost = 0.0;
	}
}
