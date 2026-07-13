package io.github.ikunkk02.elytraoverdrive.visual;

import io.github.ikunkk02.elytraoverdrive.config.VisualPreset;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisualIntensityTest {
	@Test
	void invalidInactiveAndVanillaMultiplierInputsReturnZero() {
		assertEquals(VisualIntensity.ZERO, VisualIntensity.fromSpeed(
				Double.NaN, 5.0, true, VisualPreset.BALANCED, false
		));
		assertEquals(VisualIntensity.ZERO, VisualIntensity.fromSpeed(
				4.0, 5.0, false, VisualPreset.BALANCED, false
		));
		assertEquals(VisualIntensity.ZERO, VisualIntensity.fromSpeed(
				4.0, 1.0, true, VisualPreset.BALANCED, false
		));
	}

	@Test
	void presetsNeverExceedTheirHardParticleCaps() {
		assertTrue(VisualIntensity.fromSpeed(100, 20, true, VisualPreset.PERFORMANCE, false)
				.particleBudget() <= 8);
		assertTrue(VisualIntensity.fromSpeed(100, 20, true, VisualPreset.BALANCED, false)
				.particleBudget() <= 20);
		assertTrue(VisualIntensity.fromSpeed(100, 20, true, VisualPreset.CINEMATIC, false)
				.particleBudget() <= 32);
	}

	@Test
	void experimentalMaximumStillUsesExistingParticleHardCaps() {
		for (VisualPreset preset : VisualPreset.values()) {
			VisualIntensity intensity = VisualIntensity.fromSpeed(160.0, 200.0, true, preset, false);
			assertTrue(intensity.particleBudget() <= preset.particleLimit());
			assertTrue(intensity.normalized() <= 1.0);
		}
	}

	@Test
	void reduceMotionSuppressesDynamicParticleEffects() {
		VisualIntensity normal = VisualIntensity.fromSpeed(
				5.0, 5.0, true, VisualPreset.CINEMATIC, false
		);
		VisualIntensity reduced = VisualIntensity.fromSpeed(
				5.0, 5.0, true, VisualPreset.CINEMATIC, true
		);

		assertEquals(0, reduced.speedLineCount());
		assertFalse(reduced.sonicRingAllowed());
		assertTrue(reduced.particleBudget() < normal.particleBudget());
	}

	@Test
	void speedRaisesIntensitySmoothlyWithoutUnboundedCounts() {
		VisualIntensity low = VisualIntensity.fromSpeed(1.0, 5.0, true, VisualPreset.BALANCED, false);
		VisualIntensity high = VisualIntensity.fromSpeed(5.0, 5.0, true, VisualPreset.BALANCED, false);

		assertTrue(high.normalized() > low.normalized());
		assertTrue(high.wingtipLength() >= low.wingtipLength());
		assertTrue(high.particleBudget() <= VisualPreset.BALANCED.particleLimit());
	}
}
