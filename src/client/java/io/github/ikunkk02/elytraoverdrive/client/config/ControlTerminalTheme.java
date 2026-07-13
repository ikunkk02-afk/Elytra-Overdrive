package io.github.ikunkk02.elytraoverdrive.client.config;

import io.wispforest.owo.ui.core.Surface;

public final class ControlTerminalTheme {
	public static final int BACKGROUND = 0xFF0A0F18;
	public static final int PANEL = 0xE6111A26;
	public static final int PANEL_HOVER = 0xEE162334;
	public static final int BORDER = 0xFF26384A;
	public static final int ACCENT = 0xFF4EBAD1;
	public static final int TEXT = 0xFFE5EDF2;
	public static final int SECONDARY = 0xFF8297A8;
	public static final int WARNING = 0xFFE39A45;

	private ControlTerminalTheme() {
	}

	public static Surface background() {
		return Surface.flat(BACKGROUND);
	}

	public static Surface panel() {
		return panel(BORDER);
	}

	public static Surface panel(int borderColor) {
		return Surface.flat(PANEL).and(Surface.outline(borderColor));
	}

	public static Surface selectedPanel() {
		return Surface.flat(PANEL_HOVER).and(Surface.outline(ACCENT));
	}
}
