package io.github.ikunkk02.elytraoverdrive.bombing;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BombingSessionStateTest {
	@Test
	void startAllowsImmediateBomb() {
		BombingSessionState state = new BombingSessionState();

		assertTrue(state.start(12));
		assertTrue(state.active());
	}

	@Test
	void intervalMustElapseBeforeNextBomb() {
		BombingSessionState state = new BombingSessionState();
		state.start(12);

		for (int tick = 1; tick < 12; tick++) {
			assertFalse(state.tick());
		}
		assertTrue(state.tick());
	}

	@Test
	void stopPreventsFutureBombs() {
		BombingSessionState state = new BombingSessionState();
		state.start(4);
		state.stop();

		assertFalse(state.active());
		assertFalse(state.tick());
	}

	@Test
	void repeatedStartDoesNotResetCooldown() {
		BombingSessionState state = new BombingSessionState();
		state.start(4);
		assertFalse(state.tick());
		assertFalse(state.tick());

		assertFalse(state.start(4));
		assertFalse(state.tick());
		assertTrue(state.tick());
	}
}
