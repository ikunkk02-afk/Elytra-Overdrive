package io.github.ikunkk02.elytraoverdrive.config.control;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ControlScreenStateTest {
	@Test
	void navigationContainsAllFiveTerminalSections() {
		assertEquals(5, NavigationSection.values().length);
		assertEquals(NavigationSection.FLIGHT, NavigationSection.values()[0]);
		assertEquals(NavigationSection.SERVER, NavigationSection.values()[4]);
	}

	@Test
	void scaledWidthsSelectResponsiveLayout() {
		assertEquals(ControlScreenState.LayoutMode.COMPACT, ControlScreenState.layoutForWidth(854));
		assertEquals(ControlScreenState.LayoutMode.THREE_COLUMN, ControlScreenState.layoutForWidth(900));
		assertEquals(ControlScreenState.LayoutMode.THREE_COLUMN, ControlScreenState.layoutForWidth(1920));
	}

	@Test
	void sectionChangeUsesFourTickTransitionAndSaveNoticeExpires() {
		ControlScreenState state = new ControlScreenState();
		state.select(NavigationSection.VISUAL);
		assertEquals(0.0, state.transitionProgress());
		for (int i = 0; i < 4; i++) state.tick();
		assertEquals(1.0, state.transitionProgress());

		state.showSavedNotice();
		assertTrue(state.savedNoticeVisible());
		for (int i = 0; i < 40; i++) state.tick();
		assertFalse(state.savedNoticeVisible());
	}
}
