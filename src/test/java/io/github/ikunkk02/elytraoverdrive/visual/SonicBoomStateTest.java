package io.github.ikunkk02.elytraoverdrive.visual;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SonicBoomStateTest {
	@Test
	void firstThresholdCrossingTriggersOnlyOnce() {
		SonicBoomState state = new SonicBoomState(4.0, 3.2, 60);

		assertFalse(state.tick(3.9, true, true));
		assertTrue(state.tick(4.0, true, true));
		assertFalse(state.tick(4.2, true, true));
	}

	@Test
	void cooldownAndHysteresisBothBlockRetriggering() {
		SonicBoomState state = new SonicBoomState(4.0, 3.2, 60);
		assertTrue(state.tick(4.1, true, true));

		for (int i = 0; i < 60; i++) {
			assertFalse(state.tick(3.8, true, true));
		}
		assertFalse(state.tick(4.2, true, true));
		assertFalse(state.tick(3.2, true, true));
		assertTrue(state.tick(4.1, true, true));
	}

	@Test
	void inactiveOrReducedMotionStateResetsTheTrigger() {
		SonicBoomState state = new SonicBoomState(4.0, 3.2, 60);
		assertTrue(state.tick(4.1, true, true));
		assertFalse(state.tick(4.1, false, true));
		assertTrue(state.tick(4.1, true, true));
		assertFalse(state.tick(4.1, true, false));
	}
}
