package io.github.ikunkk02.elytraoverdrive.bombing;

public final class BombingSessionState {
	private boolean active;
	private int ticksUntilNextBomb;

	public boolean active() {
		return this.active;
	}

	public boolean start(int intervalTicks) {
		if (this.active) {
			return false;
		}

		this.active = true;
		this.ticksUntilNextBomb = BombingRules.clampInterval(intervalTicks);
		return true;
	}

	public void stop() {
		this.active = false;
		this.ticksUntilNextBomb = 0;
	}

	public boolean tick() {
		if (!this.active) {
			return false;
		}

		if (--this.ticksUntilNextBomb > 0) {
			return false;
		}

		this.ticksUntilNextBomb = BombingRules.MIN_INTERVAL_TICKS;
		return true;
	}

	public void resetInterval(int intervalTicks) {
		this.ticksUntilNextBomb = BombingRules.clampInterval(intervalTicks);
	}
}
