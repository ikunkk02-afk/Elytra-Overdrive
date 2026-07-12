package io.github.ikunkk02.elytraoverdrive.config.control;

import io.github.ikunkk02.elytraoverdrive.config.OverdriveConfig;
import io.github.ikunkk02.elytraoverdrive.config.VisualPreset;

public final class ConfigDraft {
	private PlayerSettings original;
	private double playerSelectedMultiplier;
	private boolean enableHeldFireworkOverdrive;
	private boolean showHighSpeedParticles;
	private boolean enableHighSpeedFov;
	private VisualPreset visualPreset;
	private boolean enableWingtipTrails;
	private boolean enableSpeedLines;
	private boolean enableSonicBoomRing;
	private boolean reduceMotion;
	private double fovIntensity;

	public ConfigDraft(PlayerSettings settings) {
		this.original = settings;
		load(settings);
	}

	public static ConfigDraft from(OverdriveConfig config) {
		return new ConfigDraft(new PlayerSettings(
				config.playerSelectedMultiplier(),
				config.enableHeldFireworkOverdrive(),
				config.showHighSpeedParticles(),
				config.enableHighSpeedFov(),
				config.visualPreset(),
				config.enableWingtipTrails(),
				config.enableSpeedLines(),
				config.enableSonicBoomRing(),
				config.reduceMotion(),
				config.fovIntensity()
		));
	}

	public PlayerSettings current() {
		return new PlayerSettings(
				this.playerSelectedMultiplier,
				this.enableHeldFireworkOverdrive,
				this.showHighSpeedParticles,
				this.enableHighSpeedFov,
				this.visualPreset,
				this.enableWingtipTrails,
				this.enableSpeedLines,
				this.enableSonicBoomRing,
				this.reduceMotion,
				this.fovIntensity
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
		config.showHighSpeedParticles(settings.showHighSpeedParticles());
		config.enableHighSpeedFov(settings.enableHighSpeedFov());
		config.visualPreset(settings.visualPreset());
		config.enableWingtipTrails(settings.enableWingtipTrails());
		config.enableSpeedLines(settings.enableSpeedLines());
		config.enableSonicBoomRing(settings.enableSonicBoomRing());
		config.reduceMotion(settings.reduceMotion());
		config.fovIntensity(settings.fovIntensity());
		config.save();
		this.original = settings;
	}

	private void load(PlayerSettings settings) {
		this.playerSelectedMultiplier = clamp(settings.playerSelectedMultiplier(), 1.0, 20.0);
		this.enableHeldFireworkOverdrive = settings.enableHeldFireworkOverdrive();
		this.showHighSpeedParticles = settings.showHighSpeedParticles();
		this.enableHighSpeedFov = settings.enableHighSpeedFov();
		this.visualPreset = settings.visualPreset() == null ? VisualPreset.BALANCED : settings.visualPreset();
		this.enableWingtipTrails = settings.enableWingtipTrails();
		this.enableSpeedLines = settings.enableSpeedLines();
		this.enableSonicBoomRing = settings.enableSonicBoomRing();
		this.reduceMotion = settings.reduceMotion();
		this.fovIntensity = clamp(settings.fovIntensity(), 0.0, 1.5);
	}

	private static double clamp(double value, double minimum, double maximum) {
		return Double.isFinite(value) ? Math.max(minimum, Math.min(maximum, value)) : minimum;
	}

	public double playerSelectedMultiplier() { return this.playerSelectedMultiplier; }
	public void playerSelectedMultiplier(double value) { this.playerSelectedMultiplier = clamp(value, 1.0, 20.0); }
	public boolean enableHeldFireworkOverdrive() { return this.enableHeldFireworkOverdrive; }
	public void enableHeldFireworkOverdrive(boolean value) { this.enableHeldFireworkOverdrive = value; }
	public boolean showHighSpeedParticles() { return this.showHighSpeedParticles; }
	public void showHighSpeedParticles(boolean value) { this.showHighSpeedParticles = value; }
	public boolean enableHighSpeedFov() { return this.enableHighSpeedFov; }
	public void enableHighSpeedFov(boolean value) { this.enableHighSpeedFov = value; }
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
	public double fovIntensity() { return this.fovIntensity; }
	public void fovIntensity(double value) { this.fovIntensity = clamp(value, 0.0, 1.5); }

	public record PlayerSettings(
			double playerSelectedMultiplier,
			boolean enableHeldFireworkOverdrive,
			boolean showHighSpeedParticles,
			boolean enableHighSpeedFov,
			VisualPreset visualPreset,
			boolean enableWingtipTrails,
			boolean enableSpeedLines,
			boolean enableSonicBoomRing,
			boolean reduceMotion,
			double fovIntensity
	) {
		public static PlayerSettings defaults() {
			return new PlayerSettings(
					2.0, false, true, true, VisualPreset.BALANCED,
					true, true, true, false, 1.0
			);
		}
	}
}
