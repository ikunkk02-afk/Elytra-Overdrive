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

	@Sync(Option.SyncMode.OVERRIDE_CLIENT)
	@RangeConstraint(min = 1.0, max = 30.0, decimalPlaces = 0)
	public int elytraEnchantability = 10;

	@SectionHeader("bombing")
	@Sync(Option.SyncMode.OVERRIDE_CLIENT)
	public boolean enableBombing = true;

	@Sync(Option.SyncMode.OVERRIDE_CLIENT)
	@RangeConstraint(min = 4.0, max = 100.0, decimalPlaces = 0)
	public int bombingIntervalTicks = 12;

	@Sync(Option.SyncMode.OVERRIDE_CLIENT)
	@RangeConstraint(min = 20.0, max = 200.0, decimalPlaces = 0)
	public int bombFuseTicks = 80;

	@Sync(Option.SyncMode.OVERRIDE_CLIENT)
	@RangeConstraint(min = 0.0, max = 1.5, decimalPlaces = 2)
	public double bombHorizontalInertia = 0.70;

	@SectionHeader("tridentBreach")
	@Sync(Option.SyncMode.OVERRIDE_CLIENT)
	public boolean enableTridentBreach = true;

	@Sync(Option.SyncMode.OVERRIDE_CLIENT)
	@RangeConstraint(min = 0.3, max = 10.0, decimalPlaces = 1)
	public double minimumBreachSpeed = 1.2;

	@Sync(Option.SyncMode.OVERRIDE_CLIENT)
	@RangeConstraint(min = 1.0, max = 128.0, decimalPlaces = 0)
	public int maximumBreachBlocksPerTick = 32;

	@Sync(Option.SyncMode.OVERRIDE_CLIENT)
	@RangeConstraint(min = 0.1, max = 10.0, decimalPlaces = 1)
	public double breachDurabilityMultiplier = 1.0;

	@Sync(Option.SyncMode.OVERRIDE_CLIENT)
	public boolean breachDropsBlocks = true;

	@Sync(Option.SyncMode.OVERRIDE_CLIENT)
	public boolean protectBlockEntitiesFromBreach = true;
}
