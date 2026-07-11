package io.github.ikunkk02.elytraoverdrive.config;

import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Hook;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.RangeConstraint;
import io.wispforest.owo.config.annotation.SectionHeader;
import io.wispforest.owo.config.annotation.Sync;

@Config(name = "elytra-overdrive", wrapperName = "OverdriveConfig")
@Modmenu(modId = "elytra-overdrive")
public class OverdriveConfigModel {
	@SectionHeader("player")
	@RangeConstraint(min = 1.0, max = 20.0, decimalPlaces = 1)
	@Hook
	public double playerSelectedMultiplier = 2.0;

	public boolean showHighSpeedParticles = true;

	public boolean enableHighSpeedFov = true;

	@SectionHeader("server")
	@Sync(Option.SyncMode.OVERRIDE_CLIENT)
	public boolean enableHighSpeedFlight = true;

	@Sync(Option.SyncMode.OVERRIDE_CLIENT)
	@RangeConstraint(min = 1.0, max = 20.0, decimalPlaces = 1)
	public double serverMaximumMultiplier = 10.0;

	@Sync(Option.SyncMode.OVERRIDE_CLIENT)
	public boolean extraDurabilityDamage = true;

	@Sync(Option.SyncMode.OVERRIDE_CLIENT)
	@RangeConstraint(min = 10.0, max = 200.0, decimalPlaces = 0)
	public int extraDurabilityIntervalTicks = 40;
}
