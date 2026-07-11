package io.github.ikunkk02.elytraoverdrive.breach;

public record BreachVector(double x, double y, double z) {
	public static final BreachVector ZERO = new BreachVector(0.0, 0.0, 0.0);
	private static final double EPSILON = 1.0E-8;

	public boolean isFinite() {
		return Double.isFinite(this.x) && Double.isFinite(this.y) && Double.isFinite(this.z);
	}

	public double length() {
		return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
	}

	public BreachVector add(BreachVector other) {
		return new BreachVector(this.x + other.x, this.y + other.y, this.z + other.z);
	}

	public BreachVector subtract(BreachVector other) {
		return new BreachVector(this.x - other.x, this.y - other.y, this.z - other.z);
	}

	public BreachVector scale(double factor) {
		return new BreachVector(this.x * factor, this.y * factor, this.z * factor);
	}

	public double dot(BreachVector other) {
		return this.x * other.x + this.y * other.y + this.z * other.z;
	}

	public BreachVector cross(BreachVector other) {
		return new BreachVector(
				this.y * other.z - this.z * other.y,
				this.z * other.x - this.x * other.z,
				this.x * other.y - this.y * other.x
		);
	}

	public BreachVector normalizedOrZero() {
		if (!this.isFinite()) {
			return ZERO;
		}
		double length = this.length();
		if (!Double.isFinite(length) || length <= EPSILON) {
			return ZERO;
		}
		return this.scale(1.0 / length);
	}
}
