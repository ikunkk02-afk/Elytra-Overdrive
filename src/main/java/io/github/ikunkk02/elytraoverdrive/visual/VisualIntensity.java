package io.github.ikunkk02.elytraoverdrive.visual;

import io.github.ikunkk02.elytraoverdrive.config.VisualPreset;

public record VisualIntensity(
		double normalized,
		int particleBudget,
		int wingtipPointsPerWing,
		double wingtipLength,
		int speedLineCount,
		int speedLinePoints,
		boolean sonicRingAllowed
) {
	public static final VisualIntensity ZERO = new VisualIntensity(0.0, 0, 0, 0.0, 0, 0, false);

	public static VisualIntensity fromSpeed(
			double actualSpeed,
			double effectiveMultiplier,
			boolean active,
			VisualPreset preset,
			boolean reduceMotion
	) {
		if (!active
				|| !Double.isFinite(actualSpeed)
				|| actualSpeed < 0.0
				|| !Double.isFinite(effectiveMultiplier)
				|| effectiveMultiplier <= 1.0
				|| preset == null) {
			return ZERO;
		}

		double linear = clamp((actualSpeed - 0.7) / 4.3, 0.0, 1.0);
		double intensity = linear * linear * (3.0 - 2.0 * linear);
		int limit = preset.particleLimit();
		int wingtipPoints = switch (preset) {
			case PERFORMANCE -> 1 + (int)Math.round(intensity);
			case BALANCED -> 2 + (int)Math.round(intensity * 2.0);
			case CINEMATIC -> 3 + (int)Math.round(intensity * 3.0);
		};
		int speedLines = switch (preset) {
			case PERFORMANCE -> 0;
			case BALANCED -> intensity > 0.2 ? 1 + (int)Math.floor(intensity * 2.0) : 0;
			case CINEMATIC -> intensity > 0.1 ? 2 + (int)Math.floor(intensity * 3.0) : 0;
		};
		int linePoints = preset == VisualPreset.CINEMATIC ? 4 : 3;
		boolean ring = preset != VisualPreset.PERFORMANCE;

		if (reduceMotion) {
			wingtipPoints = Math.min(1, wingtipPoints);
			speedLines = 0;
			linePoints = 0;
			ring = false;
			limit = Math.min(limit, 6);
		}

		int requested = wingtipPoints * 2 + speedLines * linePoints;
		int budget = Math.min(limit, Math.max(wingtipPoints * 2, requested));
		return new VisualIntensity(
				intensity,
				budget,
				wingtipPoints,
				0.35 + intensity * (preset == VisualPreset.CINEMATIC ? 1.35 : 0.9),
				speedLines,
				linePoints,
				ring
		);
	}

	private static double clamp(double value, double minimum, double maximum) {
		return Double.isFinite(value) ? Math.max(minimum, Math.min(maximum, value)) : minimum;
	}
}
