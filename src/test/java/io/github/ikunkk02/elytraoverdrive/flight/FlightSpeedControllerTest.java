package io.github.ikunkk02.elytraoverdrive.flight;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlightSpeedControllerTest {

	@Test
	void multiplierBelowOneClampsToOne() {
		assertEquals(1.0, FlightSpeedController.clampMultiplier(0.25, 10.0));
	}

	@Test
	void multiplierAboveServerMaximumClampsToServerMaximum() {
		assertEquals(5.0, FlightSpeedController.clampMultiplier(12.0, 5.0));
	}

	@Test
	void nanClientMultiplierIsRejected() {
		assertTrue(FlightSpeedController.validateClientMultiplier(Double.NaN).isEmpty());
	}

	@Test
	void infiniteClientMultiplierIsRejected() {
		assertTrue(FlightSpeedController.validateClientMultiplier(Double.POSITIVE_INFINITY).isEmpty());
	}

	@Test
	void multiplierOneLeavesFiniteVelocityUnchanged() {
		var velocity = new FlightVelocity(0.4, -0.1, 0.8);

		assertEquals(
				velocity,
				FlightSpeedController.calculateNextVelocity(velocity, new FlightVelocity(0.0, 0.0, 1.0), 1.0)
		);
	}

	@Test
	void velocityAboveTargetIsLimitedInsteadOfAccelerated() {
		var result = FlightSpeedController.calculateNextVelocity(
				new FlightVelocity(4.0, 0.0, 0.0),
				new FlightVelocity(1.0, 0.0, 0.0),
				2.0
		);

		assertEquals(1.6, result.length(), 1.0E-9);
	}

	@Test
	void zeroVelocityDoesNotProduceNan() {
		var result = FlightSpeedController.calculateNextVelocity(
				FlightVelocity.ZERO,
				new FlightVelocity(0.0, 0.0, 1.0),
				2.0
		);

		assertTrue(result.isFinite());
		assertTrue(result.length() > 0.0);
	}

	@Test
	void illegalVelocityRecoversToZero() {
		var result = FlightSpeedController.calculateNextVelocity(
				new FlightVelocity(Double.NaN, 1.0, 0.0),
				new FlightVelocity(0.0, 0.0, 1.0),
				10.0
		);

		assertEquals(FlightVelocity.ZERO, result);
	}

	@Test
	void playerSelectionCannotExceedServerLimit() {
		var validated = FlightSpeedController.validateClientMultiplier(20.0);

		assertTrue(validated.isPresent());
		double effective = FlightSpeedController.clampMultiplier(validated.getAsDouble(), 5.0);
		assertEquals(5.0, effective);
		assertEquals(4.0, FlightSpeedController.calculateTargetSpeed(effective));
	}

	@Test
	void outOfGlobalRangeClientMultiplierIsRejected() {
		assertFalse(FlightSpeedController.validateClientMultiplier(20.01).isPresent());
	}
}
