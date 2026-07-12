package io.github.ikunkk02.elytraoverdrive.flight;

import org.junit.jupiter.api.Test;

import static io.github.ikunkk02.elytraoverdrive.flight.FlightActivationSource.BOTH;
import static io.github.ikunkk02.elytraoverdrive.flight.FlightActivationSource.ENCHANTMENT;
import static io.github.ikunkk02.elytraoverdrive.flight.FlightActivationSource.HELD_FIREWORK;
import static io.github.ikunkk02.elytraoverdrive.flight.FlightActivationSource.NONE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlightActivationResolverTest {
	@Test
	void deniedServerPolicyRejectsFireworkOnly() {
		assertEquals(NONE, FlightActivationResolver.resolve(false, true, true, false));
	}

	@Test
	void dedicatedServerPolicyAllowsFireworkOnly() {
		assertEquals(HELD_FIREWORK, FlightActivationResolver.resolve(false, true, true, true));
	}

	@Test
	void disabledPreferenceRejectsFireworkOnly() {
		assertEquals(NONE, FlightActivationResolver.resolve(false, false, true, true));
	}

	@Test
	void missingHeldRocketRejectsFireworkOnly() {
		assertEquals(NONE, FlightActivationResolver.resolve(false, true, false, true));
	}

	@Test
	void localOwnerOverrideAndLanGuestRemainDistinct() {
		boolean configuredPolicy = false;
		assertEquals(HELD_FIREWORK, FlightActivationResolver.resolve(
				false, true, true, configuredPolicy || true
		));
		assertEquals(NONE, FlightActivationResolver.resolve(
				false, true, true, configuredPolicy || false
		));
	}

	@Test
	void enchantmentRemainsIndependent() {
		assertEquals(ENCHANTMENT, FlightActivationResolver.resolve(true, false, false, false));
	}

	@Test
	void bothSourcesAreReportedTogether() {
		assertEquals(BOTH, FlightActivationResolver.resolve(true, true, true, true));
	}

	@Test
	void removingRocketFallsBackToEnchantment() {
		assertEquals(ENCHANTMENT, FlightActivationResolver.resolve(true, true, false, true));
	}

	@Test
	void globalDisableBlocksEveryResolvedSource() {
		assertFalse(FlightActivationResolver.canActivate(
				HELD_FIREWORK, false, true, false, true, true, 5.0
		));
		assertFalse(FlightActivationResolver.canActivate(
				ENCHANTMENT, false, true, false, true, true, 5.0
		));
	}

	@Test
	void finalGateRequiresEveryFlightSafetyCondition() {
		assertTrue(FlightActivationResolver.canActivate(
				HELD_FIREWORK, true, true, false, true, true, 5.0
		));
		assertFalse(FlightActivationResolver.canActivate(
				NONE, true, true, false, true, true, 5.0
		));
		assertFalse(FlightActivationResolver.canActivate(
				HELD_FIREWORK, true, false, false, true, true, 5.0
		));
		assertFalse(FlightActivationResolver.canActivate(
				HELD_FIREWORK, true, true, true, true, true, 5.0
		));
		assertFalse(FlightActivationResolver.canActivate(
				HELD_FIREWORK, true, true, false, false, true, 5.0
		));
		assertFalse(FlightActivationResolver.canActivate(
				HELD_FIREWORK, true, true, false, true, false, 5.0
		));
		assertFalse(FlightActivationResolver.canActivate(
				HELD_FIREWORK, true, true, false, true, true, 1.0
		));
		assertFalse(FlightActivationResolver.canActivate(
				HELD_FIREWORK, true, true, false, true, true, Double.NaN
		));
	}
}
