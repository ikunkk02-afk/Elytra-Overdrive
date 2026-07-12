package io.github.ikunkk02.elytraoverdrive.config;

public enum VisualPreset {
	PERFORMANCE(8, 0.7),
	BALANCED(20, 1.0),
	CINEMATIC(32, 1.2);

	private final int particleLimit;
	private final double fovFactor;

	VisualPreset(int particleLimit, double fovFactor) {
		this.particleLimit = particleLimit;
		this.fovFactor = fovFactor;
	}

	public int particleLimit() {
		return this.particleLimit;
	}

	public double fovFactor() {
		return this.fovFactor;
	}
}
