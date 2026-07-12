package io.github.ikunkk02.elytraoverdrive.breach;

public final class BreachBreakBudget {
	private final int maximumSuccessfulBreaks;
	private int successfulBreaks;

	public BreachBreakBudget(int maximumSuccessfulBreaks) {
		this.maximumSuccessfulBreaks = Math.max(1, maximumSuccessfulBreaks);
	}

	public boolean canAttemptBreak() {
		return this.successfulBreaks < this.maximumSuccessfulBreaks;
	}

	public void recordSuccessfulBreak() {
		if (this.canAttemptBreak()) {
			this.successfulBreaks++;
		}
	}

	public int successfulBreaks() {
		return this.successfulBreaks;
	}
}
