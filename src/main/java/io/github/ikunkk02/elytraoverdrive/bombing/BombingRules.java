package io.github.ikunkk02.elytraoverdrive.bombing;

public final class BombingRules {
	public static final int MIN_INTERVAL_TICKS = 4;
	public static final int MAX_INTERVAL_TICKS = 100;
	public static final BombVelocity SAFE_FALLBACK_VELOCITY = new BombVelocity(0.0, -0.15, 0.0);
	private static final double MAX_HORIZONTAL_SPEED = 10.0;

	private BombingRules() {
	}

	public static int clampInterval(int intervalTicks) {
		return Math.max(MIN_INTERVAL_TICKS, Math.min(MAX_INTERVAL_TICKS, intervalTicks));
	}

	public static BombVelocity calculateVelocity(BombVelocity playerVelocity, double horizontalInertia) {
		if (!playerVelocity.isFinite() || !Double.isFinite(horizontalInertia)) {
			return SAFE_FALLBACK_VELOCITY;
		}

		double inertia = Math.max(0.0, Math.min(1.5, horizontalInertia));
		double x = playerVelocity.x() * inertia;
		double z = playerVelocity.z() * inertia;
		double horizontalSpeed = Math.hypot(x, z);
		if (!Double.isFinite(horizontalSpeed)) {
			return SAFE_FALLBACK_VELOCITY;
		}
		if (horizontalSpeed > MAX_HORIZONTAL_SPEED) {
			double scale = MAX_HORIZONTAL_SPEED / horizontalSpeed;
			x *= scale;
			z *= scale;
		}

		double inheritedVertical = Math.max(-1.0, Math.min(0.1, playerVelocity.y() * 0.25));
		BombVelocity result = new BombVelocity(x, inheritedVertical - 0.15, z);
		return result.isFinite() ? result : SAFE_FALLBACK_VELOCITY;
	}
}
