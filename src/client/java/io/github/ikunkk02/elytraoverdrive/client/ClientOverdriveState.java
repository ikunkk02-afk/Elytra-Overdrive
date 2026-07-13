package io.github.ikunkk02.elytraoverdrive.client;

import io.github.ikunkk02.elytraoverdrive.flight.FlightSpeedController;
import io.github.ikunkk02.elytraoverdrive.flight.FlightActivationSource;
import io.github.ikunkk02.elytraoverdrive.config.VisualPreset;
import io.github.ikunkk02.elytraoverdrive.visual.VisualIntensity;
import io.github.ikunkk02.elytraoverdrive.visual.FovBoostCalculator;

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

	public static void tickFov(
			double speed,
			boolean enabled,
			double fovIntensity,
			VisualPreset preset,
			boolean reduceMotion
	) {
		previousFovBoost = fovBoost;
		double target = 0.0;
		if (enabled && active && Double.isFinite(speed) && speed > 0.9) {
			VisualIntensity visualIntensity = VisualIntensity.fromSpeed(
					speed, effectiveMultiplier, true, preset, reduceMotion
			);
			target = FovBoostCalculator.targetBoost(
					speed, visualIntensity.scaledFovFactor(fovIntensity)
			);
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
