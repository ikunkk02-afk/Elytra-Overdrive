package io.github.ikunkk02.elytraoverdrive.flight;

public record FlightVelocity(double x, double y, double z) {
	public static final FlightVelocity ZERO = new FlightVelocity(0.0, 0.0, 0.0);

	public boolean isFinite() {
		return Double.isFinite(this.x) && Double.isFinite(this.y) && Double.isFinite(this.z);
	}

	public double length() {
		return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
	}

	public FlightVelocity add(FlightVelocity other) {
		return new FlightVelocity(this.x + other.x, this.y + other.y, this.z + other.z);
	}

	public FlightVelocity scale(double scale) {
		return new FlightVelocity(this.x * scale, this.y * scale, this.z * scale);
	}

	public FlightVelocity normalizedOrZero() {
		if (!this.isFinite()) {
			return ZERO;
		}

		double length = this.length();
		if (!Double.isFinite(length) || length <= FlightSpeedController.VELOCITY_EPSILON) {
			return ZERO;
		}

		return this.scale(1.0 / length);
	}
}
