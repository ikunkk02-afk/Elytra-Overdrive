package io.github.ikunkk02.elytraoverdrive.visual;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FovBoostCalculatorTest {
	@Test
	void extremeSpeedBoostRemainsBounded() {
		double boost = FovBoostCalculator.targetBoost(160.0, 1.0);
		assertTrue(Double.isFinite(boost));
		assertEquals(15.0, boost);
	}

	@Test
	void invalidInputsProduceNoBoost() {
		assertEquals(0.0, FovBoostCalculator.targetBoost(Double.NaN, 1.0));
		assertEquals(0.0, FovBoostCalculator.targetBoost(160.0, Double.POSITIVE_INFINITY));
	}
}
