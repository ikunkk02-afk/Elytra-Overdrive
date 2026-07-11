package io.github.ikunkk02.elytraoverdrive.breach;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public final class BreachPathSampler {
	public static final double SAMPLE_STEP = 0.35;
	public static final double FORWARD_BUFFER = 0.75;

	private BreachPathSampler() {
	}

	public static List<BreachBlockPosition> sample(
			BreachVector previous,
			BreachVector current,
			BreachVector direction,
			int level,
			int maximumPositions
	) {
		if (!previous.isFinite() || !current.isFinite() || maximumPositions <= 0) {
			return List.of();
		}

		BreachVector forward = direction.normalizedOrZero();
		if (forward.equals(BreachVector.ZERO)) {
			return List.of();
		}
		BreachVector reference = Math.abs(forward.y()) < 0.9
				? new BreachVector(0.0, 1.0, 0.0)
				: new BreachVector(0.0, 0.0, 1.0);
		BreachVector right = forward.cross(reference).normalizedOrZero();
		BreachVector up = right.cross(forward).normalizedOrZero();
		if (right.equals(BreachVector.ZERO) || up.equals(BreachVector.ZERO)) {
			return List.of();
		}

		BreachVector end = current.add(forward.scale(FORWARD_BUFFER));
		BreachVector segment = end.subtract(previous);
		double distance = segment.length();
		if (!Double.isFinite(distance)) {
			return List.of();
		}
		BreachVector segmentDirection = segment.normalizedOrZero();
		if (segmentDirection.equals(BreachVector.ZERO)) {
			segmentDirection = forward;
		}
		int iterationLimit = Math.min(1024, Math.max(8, maximumPositions * 8));
		int samples = Math.max(1, (int)Math.min(Math.ceil(distance / SAMPLE_STEP), iterationLimit));
		LinkedHashSet<BreachBlockPosition> positions = new LinkedHashSet<>(Math.min(maximumPositions, 128));
		List<BreachOffset> offsets = BreachRules.crossSectionOffsets(level);
		for (int sample = 0; sample <= samples && positions.size() < maximumPositions; sample++) {
			double traveled = Math.min(distance, sample * SAMPLE_STEP);
			BreachVector center = previous.add(segmentDirection.scale(traveled));
			for (BreachOffset offset : offsets) {
				BreachVector candidate = center
						.add(right.scale(offset.right()))
						.add(up.scale(offset.up()));
				positions.add(new BreachBlockPosition(
						floorToInt(candidate.x()),
						floorToInt(candidate.y()),
						floorToInt(candidate.z())
				));
				if (positions.size() >= maximumPositions) {
					break;
				}
			}
		}
		return new ArrayList<>(positions);
	}

	private static int floorToInt(double value) {
		return (int)Math.floor(value);
	}
}
