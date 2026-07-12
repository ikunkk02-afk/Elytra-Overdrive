package io.github.ikunkk02.elytraoverdrive.breach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.HashSet;
import org.junit.jupiter.api.Test;

class BreachPathSamplerTest {
	@Test
	void sampledPathContainsNoDuplicateBlockPositions() {
		var positions = BreachPathSampler.sample(
				new BreachVector(0.5, 64.5, 0.5),
				new BreachVector(0.5, 64.5, 5.5),
				new BreachVector(0.0, 0.0, 1.0),
				3
		);

		assertFalse(positions.isEmpty());
		assertEquals(positions.size(), new HashSet<>(positions).size());
	}

	@Test
	void sampledPathNeverExceedsInternalSafetyLimit() {
		var positions = BreachPathSampler.sample(
				new BreachVector(0.5, 64.5, 0.5),
				new BreachVector(0.5, 64.5, 10_000.5),
				new BreachVector(0.0, 0.0, 1.0),
				3
		);

		assertEquals(BreachPathSampler.MAX_SCAN_POSITIONS_PER_TICK, positions.size());
	}

	@Test
	void verticalFlightBuildsFiniteStableCrossSection() {
		var positions = BreachPathSampler.sample(
				new BreachVector(0.5, 64.5, 0.5),
				new BreachVector(0.5, 67.5, 0.5),
				new BreachVector(0.0, 1.0, 0.0),
				3
		);

		assertFalse(positions.isEmpty());
		assertTrue(positions.stream().allMatch(BreachBlockPosition::isValidIntegerPosition));
	}

	@Test
	void extremeFiniteDistanceStillHasBoundedWork() {
		assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
			var positions = BreachPathSampler.sample(
					new BreachVector(0.5, 64.5, 0.5),
					new BreachVector(0.5, 64.5, 1.0E150),
					new BreachVector(0.0, 0.0, 1.0),
					1
			);
			assertTrue(positions.size() <= BreachPathSampler.MAX_SCAN_POSITIONS_PER_TICK);
		});
	}

	@Test
	void predictivePathExtendsAheadOfCurrentPosition() {
		var positions = BreachPathSampler.sample(
				new BreachVector(0.5, 64.5, 0.5),
				new BreachVector(0.5, 64.5, 1.5),
				new BreachVector(0.0, 0.0, 2.0),
				1
		);

		assertTrue(positions.stream().anyMatch(position -> position.z() >= 5));
	}

	@Test
	void lookAheadDistanceIncreasesWithSpeed() {
		double lowSpeed = BreachPathSampler.calculateLookAheadDistance(0.5);
		double highSpeed = BreachPathSampler.calculateLookAheadDistance(5.0);

		assertTrue(lowSpeed >= 2.0);
		assertTrue(highSpeed > lowSpeed);
		assertTrue(highSpeed <= 12.0);
	}

	@Test
	void thickWallCanExposeMultipleDepthLayersInOneTick() {
		var positions = BreachPathSampler.sample(
				new BreachVector(0.5, 64.5, 0.5),
				new BreachVector(0.5, 64.5, 0.5),
				new BreachVector(0.0, 0.0, 3.0),
				1
		);

		for (int depth = 1; depth <= 5; depth++) {
			int expectedDepth = depth;
			assertTrue(positions.stream().anyMatch(position -> position.z() == expectedDepth));
		}
	}
}
