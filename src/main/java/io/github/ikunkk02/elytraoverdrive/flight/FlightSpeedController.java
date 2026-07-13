package io.github.ikunkk02.elytraoverdrive.flight;

import java.util.OptionalDouble;

public final class FlightSpeedController {
	public static final double MIN_MULTIPLIER = 1.0;
	public static final double STANDARD_MAX_MULTIPLIER = 100.0;
	public static final double MAX_MULTIPLIER = 200.0;
	public static final double TARGET_SPEED_PER_MULTIPLIER = 0.8;
	public static final double ACCELERATION_PER_MULTIPLIER = 0.04;
	public static final double MAX_ACCELERATION_PER_TICK = 0.8;
	public static final double VANILLA_DEACTIVATION_SPEED_LIMIT = 1.5;
	public static final double VELOCITY_EPSILON = 1.0E-9;

	private FlightSpeedController() {
	}

	public static OptionalDouble validateClientMultiplier(double multiplier) {
		if (!Double.isFinite(multiplier) || multiplier < MIN_MULTIPLIER || multiplier > MAX_MULTIPLIER) {
			return OptionalDouble.empty();
		}

		return OptionalDouble.of(multiplier);
	}

	public static double clampMultiplier(double selectedMultiplier, double serverMaximumMultiplier) {
		double safeSelected = Double.isFinite(selectedMultiplier)
				? clamp(selectedMultiplier, MIN_MULTIPLIER, MAX_MULTIPLIER)
				: MIN_MULTIPLIER;
		double safeServerMaximum = Double.isFinite(serverMaximumMultiplier)
				? clamp(serverMaximumMultiplier, MIN_MULTIPLIER, MAX_MULTIPLIER)
				: MIN_MULTIPLIER;
		return clamp(Math.min(safeSelected, safeServerMaximum), MIN_MULTIPLIER, MAX_MULTIPLIER);
	}

	public static double calculateTargetSpeed(double finalMultiplier) {
		double safeMultiplier = clampMultiplier(finalMultiplier, MAX_MULTIPLIER);
		return TARGET_SPEED_PER_MULTIPLIER * safeMultiplier;
	}

	public static FlightVelocity calculateNextVelocity(
			FlightVelocity currentVelocity,
			FlightVelocity lookDirection,
			double finalMultiplier
	) {
		if (currentVelocity == null || !currentVelocity.isFinite()) {
			return FlightVelocity.ZERO;
		}

		double safeMultiplier = clampMultiplier(finalMultiplier, MAX_MULTIPLIER);
		if (safeMultiplier <= MIN_MULTIPLIER) {
			return currentVelocity;
		}

		double targetSpeed = calculateTargetSpeed(safeMultiplier);
		double currentSpeed = currentVelocity.length();
		if (!Double.isFinite(currentSpeed)) {
			return FlightVelocity.ZERO;
		}
		if (currentSpeed >= targetSpeed) {
			return applySpeedLimit(currentVelocity, targetSpeed);
		}

		FlightVelocity direction = currentVelocity.normalizedOrZero();
		if (direction.length() <= VELOCITY_EPSILON && lookDirection != null) {
			direction = lookDirection.normalizedOrZero();
		}
		if (direction.length() <= VELOCITY_EPSILON) {
			return currentVelocity;
		}

		double acceleration = Math.min(
				ACCELERATION_PER_MULTIPLIER * (safeMultiplier - MIN_MULTIPLIER),
				MAX_ACCELERATION_PER_TICK
		);
		return applySpeedLimit(currentVelocity.add(direction.scale(acceleration)), targetSpeed);
	}

	public static FlightVelocity applySpeedLimit(FlightVelocity velocity, double maximumSpeed) {
		if (velocity == null || !velocity.isFinite() || !Double.isFinite(maximumSpeed) || maximumSpeed < 0.0) {
			return FlightVelocity.ZERO;
		}

		double speed = velocity.length();
		if (!Double.isFinite(speed)) {
			return FlightVelocity.ZERO;
		}
		if (speed <= maximumSpeed) {
			return velocity;
		}
		if (speed <= VELOCITY_EPSILON || maximumSpeed <= VELOCITY_EPSILON) {
			return FlightVelocity.ZERO;
		}

		return velocity.scale(maximumSpeed / speed);
	}

	private static double clamp(double value, double minimum, double maximum) {
		return Math.max(minimum, Math.min(maximum, value));
	}
}
