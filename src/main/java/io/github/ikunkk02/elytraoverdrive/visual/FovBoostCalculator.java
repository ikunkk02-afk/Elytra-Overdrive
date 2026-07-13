package io.github.ikunkk02.elytraoverdrive.visual;

public final class FovBoostCalculator {
	public static final double MAXIMUM_BASE_BOOST = 15.0;

	private FovBoostCalculator() {
	}

	public static double targetBoost(double speed, double scaledFovFactor) {
		if (!Double.isFinite(speed) || !Double.isFinite(scaledFovFactor) || speed <= 0.9 || scaledFovFactor <= 0.0) {
			return 0.0;
		}
		return Math.min(MAXIMUM_BASE_BOOST, Math.max(0.0, (speed - 0.9) * 6.0)) * scaledFovFactor;
	}
}
