package io.github.ikunkk02.elytraoverdrive.config.control;

public enum NavigationSection {
	FLIGHT("flight"),
	BOMBING("bombing"),
	BREACH("breach"),
	VISUAL("visual"),
	SERVER("server");

	private final String key;

	NavigationSection(String key) {
		this.key = key;
	}

	public String translationKey() {
		return "screen.elytra_overdrive.nav." + this.key;
	}
}
