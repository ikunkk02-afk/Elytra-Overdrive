package io.github.ikunkk02.elytraoverdrive.visual;

import io.github.ikunkk02.elytraoverdrive.flight.FlightVelocity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlightBasisTest {
	@Test
	void horizontalAndVerticalDirectionsProduceFiniteOrthonormalBasis() {
		assertOrthonormal(FlightBasis.fromForward(new FlightVelocity(0, 0, 1)));
		assertOrthonormal(FlightBasis.fromForward(new FlightVelocity(0, 1, 0)));
	}

	@Test
	void zeroAndNonFiniteDirectionsUseStableFallback() {
		assertOrthonormal(FlightBasis.fromForward(FlightVelocity.ZERO));
		assertOrthonormal(FlightBasis.fromForward(new FlightVelocity(Double.NaN, 0, 0)));
	}

	private static void assertOrthonormal(FlightBasis basis) {
		assertTrue(basis.forward().isFinite());
		assertTrue(basis.right().isFinite());
		assertTrue(basis.up().isFinite());
		assertEquals(1.0, basis.forward().length(), 1.0E-9);
		assertEquals(1.0, basis.right().length(), 1.0E-9);
		assertEquals(1.0, basis.up().length(), 1.0E-9);
		assertEquals(0.0, FlightBasis.dot(basis.forward(), basis.right()), 1.0E-9);
		assertEquals(0.0, FlightBasis.dot(basis.forward(), basis.up()), 1.0E-9);
		assertEquals(0.0, FlightBasis.dot(basis.right(), basis.up()), 1.0E-9);
	}
}
