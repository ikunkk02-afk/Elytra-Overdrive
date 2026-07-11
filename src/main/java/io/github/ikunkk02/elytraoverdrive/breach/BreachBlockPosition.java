package io.github.ikunkk02.elytraoverdrive.breach;

public record BreachBlockPosition(int x, int y, int z) {
	public boolean isValidIntegerPosition() {
		return true;
	}
}
