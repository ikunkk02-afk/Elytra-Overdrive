package io.github.ikunkk02.elytraoverdrive.flight;

public final class FlightSessionState {
	private double selectedMultiplier = FlightSpeedController.MIN_MULTIPLIER;
	private boolean active;
	private int durabilityTicks;
	private double lastSyncedMultiplier = Double.NaN;
	private boolean lastSyncedActive;

	public double selectedMultiplier() {
		return this.selectedMultiplier;
	}

	public boolean updateSelectedMultiplier(double multiplier) {
		var validated = FlightSpeedController.validateClientMultiplier(multiplier);
		if (validated.isEmpty()) {
			return false;
		}

		this.selectedMultiplier = validated.getAsDouble();
		return true;
	}

	public double effectiveMultiplier(double serverMaximumMultiplier) {
		return FlightSpeedController.clampMultiplier(this.selectedMultiplier, serverMaximumMultiplier);
	}

	public boolean active() {
		return this.active;
	}

	public void setActive(boolean active) {
		this.active = active;
		if (!active) {
			this.durabilityTicks = 0;
		}
	}

	public int durabilityTicks() {
		return this.durabilityTicks;
	}

	public int incrementDurabilityTicks() {
		return ++this.durabilityTicks;
	}

	public void resetDurabilityTicks() {
		this.durabilityTicks = 0;
	}

	public boolean needsSynchronization(double effectiveMultiplier, boolean active) {
		return Double.compare(this.lastSyncedMultiplier, effectiveMultiplier) != 0
				|| this.lastSyncedActive != active;
	}

	public void markSynchronized(double effectiveMultiplier, boolean active) {
		this.lastSyncedMultiplier = effectiveMultiplier;
		this.lastSyncedActive = active;
	}
}
