package io.github.ikunkk02.elytraoverdrive.flight;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FlightSessionStateTest {
	@Test
	void startsWithSafeDefaultsUntilClientSyncs() {
		FlightSessionState state = new FlightSessionState();

		assertEquals(1.0, state.selectedMultiplier());
		assertEquals(1.0, state.effectiveMultiplier(10.0));
		assertFalse(state.active());
	}

	@Test
	void invalidClientValueKeepsLastSafeSelection() {
		FlightSessionState state = new FlightSessionState();
		assertTrue(state.updateSelectedMultiplier(4.0));

		assertFalse(state.updateSelectedMultiplier(Double.NaN));
		assertFalse(state.updateSelectedMultiplier(Double.POSITIVE_INFINITY));
		assertFalse(state.updateSelectedMultiplier(21.0));
		assertEquals(4.0, state.selectedMultiplier());
	}

	@Test
	void serverMaximumCapsEffectiveMultiplier() {
		FlightSessionState state = new FlightSessionState();
		assertTrue(state.updateSelectedMultiplier(20.0));

		assertEquals(5.0, state.effectiveMultiplier(5.0));
	}

	@Test
	void deactivationResetsDurabilityCounter() {
		FlightSessionState state = new FlightSessionState();
		state.setActive(true);
		state.incrementDurabilityTicks();
		state.incrementDurabilityTicks();

		state.setActive(false);

		assertEquals(0, state.durabilityTicks());
	}
}
