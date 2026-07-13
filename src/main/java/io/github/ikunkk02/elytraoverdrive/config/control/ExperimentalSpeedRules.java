package io.github.ikunkk02.elytraoverdrive.config.control;

import io.github.ikunkk02.elytraoverdrive.flight.FlightSpeedController;

public final class ExperimentalSpeedRules {
	private ExperimentalSpeedRules() {
	}

	public static double uiMaximum(boolean enabled) {
		return enabled
				? FlightSpeedController.MAX_MULTIPLIER
				: FlightSpeedController.STANDARD_MAX_MULTIPLIER;
	}

	public static boolean requestEnable(boolean currentValue) {
		return currentValue;
	}

	public static boolean cancelEnable(boolean currentValue) {
		return currentValue;
	}

	public static boolean confirmEnable(boolean currentValue) {
		return true;
	}

	public static double selectionAfterDisable(double selectedMultiplier) {
		if (!Double.isFinite(selectedMultiplier)) return FlightSpeedController.MIN_MULTIPLIER;
		return Math.max(
				FlightSpeedController.MIN_MULTIPLIER,
				Math.min(FlightSpeedController.STANDARD_MAX_MULTIPLIER, selectedMultiplier)
		);
	}
}
