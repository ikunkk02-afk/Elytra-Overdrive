package io.github.ikunkk02.elytraoverdrive.visual;

public final class SonicBoomState {
	private final double triggerSpeed;
	private final double resetSpeed;
	private final int cooldownDuration;
	private boolean armed = true;
	private int cooldownTicks;

	public SonicBoomState(double triggerSpeed, double resetSpeed, int cooldownDuration) {
		if (!Double.isFinite(triggerSpeed)
				|| !Double.isFinite(resetSpeed)
				|| triggerSpeed <= resetSpeed
				|| resetSpeed < 0.0
				|| cooldownDuration < 0) {
			throw new IllegalArgumentException("Invalid sonic boom thresholds");
		}
		this.triggerSpeed = triggerSpeed;
		this.resetSpeed = resetSpeed;
		this.cooldownDuration = cooldownDuration;
	}

	public boolean tick(double speed, boolean active, boolean enabled) {
		if (!active || !enabled || !Double.isFinite(speed) || speed < 0.0) {
			reset();
			return false;
		}

		if (this.cooldownTicks > 0) this.cooldownTicks--;
		if (!this.armed && speed <= this.resetSpeed && this.cooldownTicks == 0) {
			this.armed = true;
			return false;
		}
		if (this.armed && this.cooldownTicks == 0 && speed >= this.triggerSpeed) {
			this.armed = false;
			this.cooldownTicks = this.cooldownDuration;
			return true;
		}
		return false;
	}

	public void reset() {
		this.armed = true;
		this.cooldownTicks = 0;
	}
}
