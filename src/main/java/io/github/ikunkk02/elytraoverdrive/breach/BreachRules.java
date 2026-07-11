package io.github.ikunkk02.elytraoverdrive.breach;

import java.util.List;

public final class BreachRules {
	public static final double MIN_DIRECTION_DOT = 0.65;
	private static final List<BreachOffset> LEVEL_ONE = List.of(new BreachOffset(0, 0));
	private static final List<BreachOffset> LEVEL_TWO = List.of(
			new BreachOffset(0, 0),
			new BreachOffset(0, 1),
			new BreachOffset(0, -1),
			new BreachOffset(1, 0),
			new BreachOffset(-1, 0)
	);
	private static final List<BreachOffset> LEVEL_THREE = List.of(
			new BreachOffset(0, 0),
			new BreachOffset(-1, 1), new BreachOffset(0, 1), new BreachOffset(1, 1),
			new BreachOffset(-1, 0), new BreachOffset(1, 0),
			new BreachOffset(-1, -1), new BreachOffset(0, -1), new BreachOffset(1, -1)
	);

	private BreachRules() {
	}

	public static List<BreachOffset> crossSectionOffsets(int level) {
		return switch (Math.max(1, Math.min(3, level))) {
			case 1 -> LEVEL_ONE;
			case 2 -> LEVEL_TWO;
			default -> LEVEL_THREE;
		};
	}

	public static boolean canActivate(BreachVector velocity, BreachVector look, double minimumSpeed) {
		if (!velocity.isFinite() || !look.isFinite() || !Double.isFinite(minimumSpeed)) {
			return false;
		}
		double safeMinimum = Math.max(0.3, Math.min(10.0, minimumSpeed));
		if (velocity.length() < safeMinimum) {
			return false;
		}
		BreachVector direction = velocity.normalizedOrZero();
		BreachVector lookDirection = look.normalizedOrZero();
		return !direction.equals(BreachVector.ZERO)
				&& !lookDirection.equals(BreachVector.ZERO)
				&& direction.dot(lookDirection) >= MIN_DIRECTION_DOT;
	}

	public static boolean canBreakHardness(int level, double hardness) {
		if (!Double.isFinite(hardness) || hardness < 0.0) {
			return false;
		}
		double maximum = switch (Math.max(1, Math.min(3, level))) {
			case 1 -> 3.0;
			case 2 -> 10.0;
			default -> 50.0;
		};
		return hardness <= maximum;
	}

	public static int durabilityCost(double hardness, double multiplier) {
		if (!Double.isFinite(hardness) || hardness < 0.0 || !Double.isFinite(multiplier)) {
			return 1;
		}
		double safeMultiplier = Math.max(0.1, Math.min(10.0, multiplier));
		double cost = Math.ceil(hardness * safeMultiplier);
		return (int)Math.max(1.0, Math.min(Integer.MAX_VALUE, cost));
	}
}
