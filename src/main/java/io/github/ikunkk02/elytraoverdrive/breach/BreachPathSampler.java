package io.github.ikunkk02.elytraoverdrive.breach;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public final class BreachPathSampler {
	public static final double SAMPLE_STEP = 0.35;
	public static final int MAX_SCAN_POSITIONS_PER_TICK = 1024;
	private static final double MIN_LOOK_AHEAD_DISTANCE = 2.0;
	private static final double MAX_LOOK_AHEAD_DISTANCE = 12.0;

	private BreachPathSampler() {
	}

	public static List<BreachBlockPosition> sample(
			BreachVector previous,
			BreachVector current,
			BreachVector velocity,
			int level
	) {
		if (!previous.isFinite() || !current.isFinite() || !velocity.isFinite()) {
			return List.of();
		}

		BreachVector forward = velocity.normalizedOrZero();
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

		BreachVector end = current.add(forward.scale(calculateLookAheadDistance(velocity.length())));
		BreachVector segment = end.subtract(previous);
		double distance = segment.length();
		if (!Double.isFinite(distance)) {
			return List.of();
		}
		BreachVector segmentDirection = segment.normalizedOrZero();
		if (segmentDirection.equals(BreachVector.ZERO)) {
			segmentDirection = forward;
		}
		int samples = Math.max(1, (int)Math.min(Math.ceil(distance / SAMPLE_STEP), MAX_SCAN_POSITIONS_PER_TICK));
		LinkedHashSet<BreachBlockPosition> positions = new LinkedHashSet<>(128);
		List<BreachOffset> offsets = BreachRules.crossSectionOffsets(level);
		for (int sample = 0; sample <= samples && positions.size() < MAX_SCAN_POSITIONS_PER_TICK; sample++) {
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
				if (positions.size() >= MAX_SCAN_POSITIONS_PER_TICK) {
					break;
				}
			}
		}
		return new ArrayList<>(positions);
	}

	public static double calculateLookAheadDistance(double speed) {
		if (!Double.isFinite(speed)) {
			return MIN_LOOK_AHEAD_DISTANCE;
		}
		return Math.clamp(1.5 + Math.max(0.0, speed) * 1.5, MIN_LOOK_AHEAD_DISTANCE, MAX_LOOK_AHEAD_DISTANCE);
	}

	private static int floorToInt(double value) {
		return (int)Math.floor(value);
	}
}
