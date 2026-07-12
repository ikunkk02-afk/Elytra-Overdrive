package io.github.ikunkk02.elytraoverdrive.flight;

public enum FlightActivationSource {
	NONE,
	ENCHANTMENT,
	HELD_FIREWORK,
	BOTH;

	public static FlightActivationSource fromNetworkOrdinal(int ordinal) {
		return ordinal >= 0 && ordinal < values().length ? values()[ordinal] : NONE;
	}
}
