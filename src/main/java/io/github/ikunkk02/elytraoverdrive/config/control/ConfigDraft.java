package io.github.ikunkk02.elytraoverdrive.config.control;

import io.github.ikunkk02.elytraoverdrive.config.OverdriveConfig;
import io.github.ikunkk02.elytraoverdrive.config.VisualPreset;
import io.github.ikunkk02.elytraoverdrive.flight.FlightSpeedController;

public final class ConfigDraft {
	private PlayerSettings original;
	private double playerSelectedMultiplier;
	private boolean enableHeldFireworkOverdrive;
	private boolean enableExperimentalExtremeSpeed;
	private boolean showHighSpeedParticles;
	private VisualPreset visualPreset;
	private boolean enableWingtipTrails;
	private boolean enableSpeedLines;
	private boolean enableSonicBoomRing;
	private boolean reduceMotion;

	public ConfigDraft(PlayerSettings settings) {
		this.original = settings;
		load(settings);
	}

	public static ConfigDraft from(OverdriveConfig config) {
		return new ConfigDraft(new PlayerSettings(
				config.playerSelectedMultiplier(),
				config.enableHeldFireworkOverdrive(),
				config.enableExperimentalExtremeSpeed(),
				config.showHighSpeedParticles(),
				config.visualPreset(),
				config.enableWingtipTrails(),
				config.enableSpeedLines(),
				config.enableSonicBoomRing(),
				config.reduceMotion()
		));
	}

	public PlayerSettings current() {
		return new PlayerSettings(
				this.playerSelectedMultiplier,
				this.enableHeldFireworkOverdrive,
				this.enableExperimentalExtremeSpeed,
				this.showHighSpeedParticles,
				this.visualPreset,
				this.enableWingtipTrails,
				this.enableSpeedLines,
				this.enableSonicBoomRing,
				this.reduceMotion
		);
	}

	public boolean isDirty() {
		return !this.original.equals(current());
	}

	public void restorePlayerDefaults() {
		load(PlayerSettings.defaults());
	}

	public void applyTo(OverdriveConfig config) {
		PlayerSettings settings = current();
		config.playerSelectedMultiplier(settings.playerSelectedMultiplier());
		config.enableHeldFireworkOverdrive(settings.enableHeldFireworkOverdrive());
		config.enableExperimentalExtremeSpeed(settings.enableExperimentalExtremeSpeed());
		config.showHighSpeedParticles(settings.showHighSpeedParticles());
		config.visualPreset(settings.visualPreset());
		config.enableWingtipTrails(settings.enableWingtipTrails());
		config.enableSpeedLines(settings.enableSpeedLines());
		config.enableSonicBoomRing(settings.enableSonicBoomRing());
		config.reduceMotion(settings.reduceMotion());
		config.save();
		this.original = settings;
	}

	private void load(PlayerSettings settings) {
		this.playerSelectedMultiplier = clamp(
				settings.playerSelectedMultiplier(),
				FlightSpeedController.MIN_MULTIPLIER,
				FlightSpeedController.MAX_MULTIPLIER
		);
		this.enableHeldFireworkOverdrive = settings.enableHeldFireworkOverdrive();
		this.enableExperimentalExtremeSpeed = settings.enableExperimentalExtremeSpeed();
		if (!this.enableExperimentalExtremeSpeed) {
			this.playerSelectedMultiplier = ExperimentalSpeedRules.selectionAfterDisable(this.playerSelectedMultiplier);
		}
		this.showHighSpeedParticles = settings.showHighSpeedParticles();
		this.visualPreset = settings.visualPreset() == null ? VisualPreset.BALANCED : settings.visualPreset();
		this.enableWingtipTrails = settings.enableWingtipTrails();
		this.enableSpeedLines = settings.enableSpeedLines();
		this.enableSonicBoomRing = settings.enableSonicBoomRing();
		this.reduceMotion = settings.reduceMotion();
	}

	private static double clamp(double value, double minimum, double maximum) {
		return Double.isFinite(value) ? Math.max(minimum, Math.min(maximum, value)) : minimum;
	}

	public double playerSelectedMultiplier() { return this.playerSelectedMultiplier; }
	public void playerSelectedMultiplier(double value) {
		this.playerSelectedMultiplier = clamp(
				value, FlightSpeedController.MIN_MULTIPLIER, FlightSpeedController.MAX_MULTIPLIER
		);
	}
	public boolean enableHeldFireworkOverdrive() { return this.enableHeldFireworkOverdrive; }
	public void enableHeldFireworkOverdrive(boolean value) { this.enableHeldFireworkOverdrive = value; }
	public boolean enableExperimentalExtremeSpeed() { return this.enableExperimentalExtremeSpeed; }
	public void enableExperimentalExtremeSpeed(boolean value) {
		this.enableExperimentalExtremeSpeed = value;
		if (!value) {
			this.playerSelectedMultiplier = ExperimentalSpeedRules.selectionAfterDisable(this.playerSelectedMultiplier);
		}
	}
	public boolean showHighSpeedParticles() { return this.showHighSpeedParticles; }
	public void showHighSpeedParticles(boolean value) { this.showHighSpeedParticles = value; }
	public VisualPreset visualPreset() { return this.visualPreset; }
	public void visualPreset(VisualPreset value) { this.visualPreset = value == null ? VisualPreset.BALANCED : value; }
	public boolean enableWingtipTrails() { return this.enableWingtipTrails; }
	public void enableWingtipTrails(boolean value) { this.enableWingtipTrails = value; }
	public boolean enableSpeedLines() { return this.enableSpeedLines; }
	public void enableSpeedLines(boolean value) { this.enableSpeedLines = value; }
	public boolean enableSonicBoomRing() { return this.enableSonicBoomRing; }
	public void enableSonicBoomRing(boolean value) { this.enableSonicBoomRing = value; }
	public boolean reduceMotion() { return this.reduceMotion; }
	public void reduceMotion(boolean value) { this.reduceMotion = value; }

	public record PlayerSettings(
			double playerSelectedMultiplier,
			boolean enableHeldFireworkOverdrive,
			boolean enableExperimentalExtremeSpeed,
			boolean showHighSpeedParticles,
			VisualPreset visualPreset,
			boolean enableWingtipTrails,
			boolean enableSpeedLines,
			boolean enableSonicBoomRing,
			boolean reduceMotion
	) {
		public static PlayerSettings defaults() {
			return new PlayerSettings(
					2.0, false, false, true, VisualPreset.BALANCED,
					true, true, true, false
			);
		}
	}
}
