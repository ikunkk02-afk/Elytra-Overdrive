package io.github.ikunkk02.elytraoverdrive.breach;

public final class BreachSessionState {
	private BreachVector previousPosition;

	public BreachVector previousPosition() {
		return this.previousPosition;
	}

	public boolean initialized() {
		return this.previousPosition != null;
	}

	public void updatePosition(BreachVector position) {
		this.previousPosition = position;
	}

	public void reset() {
		this.previousPosition = null;
	}
}
