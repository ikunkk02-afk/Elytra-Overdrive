package io.github.ikunkk02.elytraoverdrive.config.control;

public final class ControlScreenState {
	public static final int COMPACT_BREAKPOINT = 900;
	private static final int TRANSITION_TICKS = 4;
	private static final int SAVED_NOTICE_TICKS = 40;

	private NavigationSection selected = NavigationSection.FLIGHT;
	private int transitionTick = TRANSITION_TICKS;
	private int savedNoticeTicks;

	public static LayoutMode layoutForWidth(int width) {
		return width < COMPACT_BREAKPOINT ? LayoutMode.COMPACT : LayoutMode.THREE_COLUMN;
	}

	public void select(NavigationSection section) {
		if (section == null || section == this.selected) return;
		this.selected = section;
		this.transitionTick = 0;
	}

	public void tick() {
		if (this.transitionTick < TRANSITION_TICKS) this.transitionTick++;
		if (this.savedNoticeTicks > 0) this.savedNoticeTicks--;
	}

	public void showSavedNotice() {
		this.savedNoticeTicks = SAVED_NOTICE_TICKS;
	}

	public NavigationSection selected() { return this.selected; }
	public double transitionProgress() { return this.transitionTick / (double) TRANSITION_TICKS; }
	public boolean savedNoticeVisible() { return this.savedNoticeTicks > 0; }

	public enum LayoutMode {
		THREE_COLUMN,
		COMPACT
	}
}
