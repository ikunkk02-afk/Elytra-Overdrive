package io.github.ikunkk02.elytraoverdrive.config.control;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExperimentalSpeedRulesTest {
	@Test
	void uiMaximumDependsOnlyOnConfirmedExperimentalState() {
		assertEquals(100.0, ExperimentalSpeedRules.uiMaximum(false));
		assertEquals(200.0, ExperimentalSpeedRules.uiMaximum(true));
	}

	@Test
	void enableRequestDoesNotApplyUntilConfirmed() {
		assertFalse(ExperimentalSpeedRules.requestEnable(false));
		assertFalse(ExperimentalSpeedRules.cancelEnable(false));
		assertTrue(ExperimentalSpeedRules.confirmEnable(false));
	}

	@Test
	void disablingClampsOnlySelectionsAboveStandardRange() {
		assertEquals(100.0, ExperimentalSpeedRules.selectionAfterDisable(150.0));
		assertEquals(80.0, ExperimentalSpeedRules.selectionAfterDisable(80.0));
	}
}
