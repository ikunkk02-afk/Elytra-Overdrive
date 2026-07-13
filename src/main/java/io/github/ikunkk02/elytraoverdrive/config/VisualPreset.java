package io.github.ikunkk02.elytraoverdrive.config;

public enum VisualPreset {
	PERFORMANCE(8),
	BALANCED(20),
	CINEMATIC(32);

	private final int particleLimit;

	VisualPreset(int particleLimit) {
		this.particleLimit = particleLimit;
	}

	public int particleLimit() {
		return this.particleLimit;
	}
}
