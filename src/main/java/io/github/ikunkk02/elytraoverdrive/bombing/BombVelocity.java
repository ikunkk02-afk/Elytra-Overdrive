package io.github.ikunkk02.elytraoverdrive.bombing;

public record BombVelocity(double x, double y, double z) {
	public boolean isFinite() {
		return Double.isFinite(this.x) && Double.isFinite(this.y) && Double.isFinite(this.z);
	}
}
