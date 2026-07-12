package io.github.ikunkk02.elytraoverdrive.flight;

public final class FlightActivationResolver {
	private FlightActivationResolver() {
	}

	public static FlightActivationSource resolve(
			boolean hasEnchantment,
			boolean preferenceEnabled,
			boolean holdingRocket,
			boolean policyAllowsPlayer
	) {
		boolean firework = preferenceEnabled && holdingRocket && policyAllowsPlayer;
		if (hasEnchantment && firework) {
			return FlightActivationSource.BOTH;
		}
		if (hasEnchantment) {
			return FlightActivationSource.ENCHANTMENT;
		}
		if (firework) {
			return FlightActivationSource.HELD_FIREWORK;
		}
		return FlightActivationSource.NONE;
	}

	public static boolean canActivate(
			FlightActivationSource source,
			boolean highSpeedEnabled,
			boolean alive,
			boolean spectator,
			boolean fallFlying,
			boolean usableElytra,
			double effectiveMultiplier
	) {
		return source != null
				&& source != FlightActivationSource.NONE
				&& highSpeedEnabled
				&& alive
				&& !spectator
				&& fallFlying
				&& usableElytra
				&& Double.isFinite(effectiveMultiplier)
				&& effectiveMultiplier > FlightSpeedController.MIN_MULTIPLIER;
	}
}
