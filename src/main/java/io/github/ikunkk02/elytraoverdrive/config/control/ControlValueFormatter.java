package io.github.ikunkk02.elytraoverdrive.config.control;

import java.util.Locale;

public final class ControlValueFormatter {
	private ControlValueFormatter() {
	}

	public static String multiplier(double value) {
		return String.format(Locale.ROOT, "%.1f×", value);
	}

	public static String speed(double value) {
		return String.format(Locale.ROOT, "%.2f blocks/tick", value);
	}

	public static String durability(int remaining, int maximum) {
		return remaining + " / " + maximum;
	}
}
