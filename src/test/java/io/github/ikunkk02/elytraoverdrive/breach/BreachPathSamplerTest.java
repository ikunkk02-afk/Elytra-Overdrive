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
				3,
				128
		);

		assertFalse(positions.isEmpty());
		assertEquals(positions.size(), new HashSet<>(positions).size());
	}

	@Test
	void sampledPathNeverExceedsConfiguredLimit() {
		var positions = BreachPathSampler.sample(
				new BreachVector(0.5, 64.5, 0.5),
				new BreachVector(0.5, 64.5, 10_000.5),
				new BreachVector(0.0, 0.0, 1.0),
				3,
				32
		);

		assertEquals(32, positions.size());
	}

	@Test
	void verticalFlightBuildsFiniteStableCrossSection() {
		var positions = BreachPathSampler.sample(
				new BreachVector(0.5, 64.5, 0.5),
				new BreachVector(0.5, 67.5, 0.5),
				new BreachVector(0.0, 1.0, 0.0),
				3,
				128
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
					1,
					32
			);
			assertTrue(positions.size() <= 32);
		});
	}
}
