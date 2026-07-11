package io.github.ikunkk02.elytraoverdrive.breach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BreachRulesTest {
	@Test
	void enchantmentLevelsHaveExpectedCrossSections() {
		assertEquals(1, BreachRules.crossSectionOffsets(1).size());
		assertEquals(5, BreachRules.crossSectionOffsets(2).size());
		assertEquals(9, BreachRules.crossSectionOffsets(3).size());
	}

	@Test
	void zeroAndNonFiniteVelocityCannotActivate() {
		assertFalse(BreachRules.canActivate(BreachVector.ZERO, new BreachVector(0.0, 0.0, 1.0), 1.2));
		assertFalse(BreachRules.canActivate(
				new BreachVector(Double.NaN, 0.0, 1.0),
				new BreachVector(0.0, 0.0, 1.0),
				1.2
		));
	}

	@Test
	void speedBelowMinimumCannotActivate() {
		assertFalse(BreachRules.canActivate(
				new BreachVector(0.0, 0.0, 1.19),
				new BreachVector(0.0, 0.0, 1.0),
				1.2
		));
	}

	@Test
	void lowDirectionDotProductCannotActivate() {
		assertFalse(BreachRules.canActivate(
				new BreachVector(1.2, 0.0, 0.0),
				new BreachVector(0.0, 0.0, 1.0),
				1.2
		));
		assertTrue(BreachRules.canActivate(
				new BreachVector(0.0, 0.0, 1.2),
				new BreachVector(0.0, 0.0, 1.0),
				1.2
		));
	}

	@Test
	void negativeAndNonFiniteHardnessAreNeverBreakable() {
		assertFalse(BreachRules.canBreakHardness(3, -1.0));
		assertFalse(BreachRules.canBreakHardness(3, Double.NaN));
		assertFalse(BreachRules.canBreakHardness(3, Double.POSITIVE_INFINITY));
	}

	@Test
	void levelsEnforceHardnessLimits() {
		assertTrue(BreachRules.canBreakHardness(1, 3.0));
		assertFalse(BreachRules.canBreakHardness(1, 3.01));
		assertTrue(BreachRules.canBreakHardness(2, 10.0));
		assertFalse(BreachRules.canBreakHardness(2, 10.01));
		assertTrue(BreachRules.canBreakHardness(3, 50.0));
		assertFalse(BreachRules.canBreakHardness(3, 50.01));
	}

	@Test
	void durabilityCostIsAtLeastOneAndRejectsIllegalNumbers() {
		assertEquals(1, BreachRules.durabilityCost(0.0, 1.0));
		assertEquals(2, BreachRules.durabilityCost(1.5, 1.0));
		assertEquals(3, BreachRules.durabilityCost(3.0, 1.0));
		assertEquals(1, BreachRules.durabilityCost(Double.NaN, 1.0));
		assertEquals(1, BreachRules.durabilityCost(3.0, Double.POSITIVE_INFINITY));
	}
}
