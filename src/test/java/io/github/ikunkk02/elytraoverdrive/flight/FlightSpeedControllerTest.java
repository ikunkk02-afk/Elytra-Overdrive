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
		assertTrue(FlightSpeedController.validateClientMultiplier(Double.NEGATIVE_INFINITY).isEmpty());
	}

	@Test
	void expandedClientRangeAcceptsStandardAndExperimentalMaximums() {
		assertEquals(100.0, FlightSpeedController.validateClientMultiplier(100.0).orElseThrow());
		assertEquals(200.0, FlightSpeedController.validateClientMultiplier(200.0).orElseThrow());
	}

	@Test
	void expandedClientRangeRejectsNegativeAndAboveHardMaximum() {
		assertTrue(FlightSpeedController.validateClientMultiplier(-1.0).isEmpty());
		assertTrue(FlightSpeedController.validateClientMultiplier(200.01).isEmpty());
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
		assertFalse(FlightSpeedController.validateClientMultiplier(200.01).isPresent());
	}

	@Test
	void serverMaximumRemainsAuthoritativeAcrossExpandedRange() {
		assertEquals(100.0, FlightSpeedController.clampMultiplier(200.0, 100.0));
		assertEquals(200.0, FlightSpeedController.clampMultiplier(200.0, 200.0));
		assertEquals(150.0, FlightSpeedController.clampMultiplier(150.0, 200.0));
		assertEquals(50.0, FlightSpeedController.clampMultiplier(200.0, 50.0));
		assertEquals(1.0, FlightSpeedController.clampMultiplier(200.0, Double.NaN));
	}

	@Test
	void extremeTargetSpeedAndVelocityRemainFinite() {
		assertEquals(80.0, FlightSpeedController.calculateTargetSpeed(100.0));
		assertEquals(160.0, FlightSpeedController.calculateTargetSpeed(200.0));
		assertTrue(Double.isFinite(FlightSpeedController.calculateTargetSpeed(200.0)));

		FlightVelocity next = FlightSpeedController.calculateNextVelocity(
				new FlightVelocity(159.5, 0.0, 0.0),
				new FlightVelocity(1.0, 0.0, 0.0),
				200.0
		);
		assertTrue(next.isFinite());
		assertTrue(FlightSpeedController.applySpeedLimit(
				new FlightVelocity(Double.MAX_VALUE, 0.0, 0.0), 160.0
		).isFinite());
	}
}
