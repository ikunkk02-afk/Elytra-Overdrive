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
		assertFalse(state.heldFireworkPreference());
		assertEquals(FlightActivationSource.NONE, state.activationSource());
	}

	@Test
	void heldFireworkPreferenceTracksIntentWithoutGrantingAuthority() {
		FlightSessionState state = new FlightSessionState();

		state.setHeldFireworkPreference(true);

		assertTrue(state.heldFireworkPreference());
		assertEquals(FlightActivationSource.NONE, state.activationSource());
	}

	@Test
	void expandedConfirmedStateControlsSynchronization() {
		FlightSessionState state = new FlightSessionState();

		assertTrue(state.needsSynchronization(
				5.0, false, FlightActivationSource.NONE, false, false, false
		));
		state.markSynchronized(5.0, false, FlightActivationSource.NONE, false, false, false);
		assertFalse(state.needsSynchronization(
				5.0, false, FlightActivationSource.NONE, false, false, false
		));
		assertTrue(state.needsSynchronization(
				5.0, false, FlightActivationSource.NONE, true, false, false
		));
		assertTrue(state.needsSynchronization(
				5.0, true, FlightActivationSource.HELD_FIREWORK, true, false, true
		));
	}

	@Test
	void invalidClientValueKeepsLastSafeSelection() {
		FlightSessionState state = new FlightSessionState();
		assertTrue(state.updateSelectedMultiplier(4.0));

		assertFalse(state.updateSelectedMultiplier(Double.NaN));
		assertFalse(state.updateSelectedMultiplier(Double.POSITIVE_INFINITY));
		assertFalse(state.updateSelectedMultiplier(201.0));
		assertEquals(4.0, state.selectedMultiplier());
	}

	@Test
	void serverMaximumCapsEffectiveMultiplier() {
		FlightSessionState state = new FlightSessionState();
		assertTrue(state.updateSelectedMultiplier(200.0));

		assertEquals(100.0, state.effectiveMultiplier(100.0));
		assertEquals(200.0, state.effectiveMultiplier(200.0));
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
