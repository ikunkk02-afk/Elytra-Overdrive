package io.github.ikunkk02.elytraoverdrive.flight;

public final class FlightSessionState {
	private double selectedMultiplier = FlightSpeedController.MIN_MULTIPLIER;
	private boolean heldFireworkPreference;
	private boolean active;
	private FlightActivationSource activationSource = FlightActivationSource.NONE;
	private int durabilityTicks;
	private double lastSyncedMultiplier = Double.NaN;
	private boolean lastSyncedActive;
	private FlightActivationSource lastSyncedSource = FlightActivationSource.NONE;
	private boolean lastSyncedServerPolicy;
	private boolean lastSyncedOwnerOverride;
	private boolean lastSyncedAcceptedPreference;

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

	public boolean heldFireworkPreference() {
		return this.heldFireworkPreference;
	}

	public void setHeldFireworkPreference(boolean enabled) {
		this.heldFireworkPreference = enabled;
	}

	public boolean active() {
		return this.active;
	}

	public void setActive(boolean active) {
		this.active = active;
		if (!active) {
			this.durabilityTicks = 0;
			this.activationSource = FlightActivationSource.NONE;
		}
	}

	public FlightActivationSource activationSource() {
		return this.activationSource;
	}

	public void setActivationSource(FlightActivationSource source) {
		this.activationSource = source == null ? FlightActivationSource.NONE : source;
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

	public boolean needsSynchronization(
			double effectiveMultiplier,
			boolean active,
			FlightActivationSource source,
			boolean serverPolicy,
			boolean ownerOverride,
			boolean acceptedPreference
	) {
		return Double.compare(this.lastSyncedMultiplier, effectiveMultiplier) != 0
				|| this.lastSyncedActive != active
				|| this.lastSyncedSource != source
				|| this.lastSyncedServerPolicy != serverPolicy
				|| this.lastSyncedOwnerOverride != ownerOverride
				|| this.lastSyncedAcceptedPreference != acceptedPreference;
	}

	public void markSynchronized(
			double effectiveMultiplier,
			boolean active,
			FlightActivationSource source,
			boolean serverPolicy,
			boolean ownerOverride,
			boolean acceptedPreference
	) {
		this.lastSyncedMultiplier = effectiveMultiplier;
		this.lastSyncedActive = active;
		this.lastSyncedSource = source;
		this.lastSyncedServerPolicy = serverPolicy;
		this.lastSyncedOwnerOverride = ownerOverride;
		this.lastSyncedAcceptedPreference = acceptedPreference;
	}
}
