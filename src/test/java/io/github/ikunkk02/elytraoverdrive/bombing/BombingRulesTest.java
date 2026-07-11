package io.github.ikunkk02.elytraoverdrive.bombing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BombingRulesTest {
	@Test
	void invalidIntervalsAreClamped() {
		assertEquals(4, BombingRules.clampInterval(-100));
		assertEquals(100, BombingRules.clampInterval(500));
	}

	@Test
	void trajectoryInheritsConfiguredHorizontalVelocity() {
		BombVelocity result = BombingRules.calculateVelocity(new BombVelocity(2.0, 0.4, -1.0), 0.7);

		assertEquals(1.4, result.x(), 1.0E-9);
		assertEquals(-0.7, result.z(), 1.0E-9);
		assertTrue(result.y() <= 0.0);
	}

	@Test
	void nonFiniteVelocityFallsBackSafely() {
		BombVelocity result = BombingRules.calculateVelocity(
				new BombVelocity(Double.NaN, Double.POSITIVE_INFINITY, 1.0),
				0.7
		);

		assertTrue(result.isFinite());
		assertEquals(BombingRules.SAFE_FALLBACK_VELOCITY, result);
	}
}
