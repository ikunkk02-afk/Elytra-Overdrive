package io.github.ikunkk02.elytraoverdrive.visual;

import io.github.ikunkk02.elytraoverdrive.flight.FlightVelocity;

public record FlightBasis(FlightVelocity forward, FlightVelocity right, FlightVelocity up) {
	private static final FlightVelocity DEFAULT_FORWARD = new FlightVelocity(0.0, 0.0, 1.0);
	private static final FlightVelocity WORLD_UP = new FlightVelocity(0.0, 1.0, 0.0);
	private static final FlightVelocity FALLBACK_UP = new FlightVelocity(1.0, 0.0, 0.0);

	public static FlightBasis fromForward(FlightVelocity direction) {
		FlightVelocity forward = direction == null ? FlightVelocity.ZERO : direction.normalizedOrZero();
		if (forward.length() <= 1.0E-9) forward = DEFAULT_FORWARD;
		FlightVelocity referenceUp = Math.abs(dot(forward, WORLD_UP)) > 0.98 ? FALLBACK_UP : WORLD_UP;
		FlightVelocity right = cross(referenceUp, forward).normalizedOrZero();
		FlightVelocity up = cross(forward, right).normalizedOrZero();
		return new FlightBasis(forward, right, up);
	}

	public static double dot(FlightVelocity left, FlightVelocity right) {
		return left.x() * right.x() + left.y() * right.y() + left.z() * right.z();
	}

	public static FlightVelocity cross(FlightVelocity left, FlightVelocity right) {
		return new FlightVelocity(
				left.y() * right.z() - left.z() * right.y(),
				left.z() * right.x() - left.x() * right.z(),
				left.x() * right.y() - left.y() * right.x()
		);
	}
}
