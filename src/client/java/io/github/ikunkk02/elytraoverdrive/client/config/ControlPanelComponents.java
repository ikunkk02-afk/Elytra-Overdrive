package io.github.ikunkk02.elytraoverdrive.client.config;

import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.CheckboxComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public final class ControlPanelComponents {
	private ControlPanelComponents() {
	}

	public static FlowLayout panel(Sizing horizontalSizing) {
		return panel(horizontalSizing, ControlTerminalTheme.BORDER);
	}

	public static FlowLayout panel(Sizing horizontalSizing, int borderColor) {
		FlowLayout panel = Containers.verticalFlow(horizontalSizing, Sizing.content());
		panel.surface(ControlTerminalTheme.panel(borderColor));
		panel.padding(Insets.of(8));
		panel.gap(5);
		return panel;
	}

	public static LabelComponent label(Component text, int color) {
		return Components.label(text).color(Color.ofArgb(color));
	}

	public static LabelComponent title(String translationKey) {
		return label(Component.translatable(translationKey), ControlTerminalTheme.ACCENT).shadow(true);
	}

	public static FlowLayout row(Component name, Component value) {
		FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
		row.verticalAlignment(io.wispforest.owo.ui.core.VerticalAlignment.CENTER);
		row.child(label(name, ControlTerminalTheme.SECONDARY).horizontalSizing(Sizing.fill(55)));
		LabelComponent valueLabel = label(value, ControlTerminalTheme.TEXT);
		valueLabel.horizontalTextAlignment(HorizontalAlignment.RIGHT);
		valueLabel.horizontalSizing(Sizing.fill(45));
		row.child(valueLabel);
		return row;
	}

	public static CheckboxComponent toggle(
			String translationKey,
			boolean checked,
			boolean active,
			Consumer<Boolean> onChanged
	) {
		CheckboxComponent checkbox = Components.checkbox(Component.translatable(translationKey));
		checkbox.checked(checked).onChanged(onChanged);
		checkbox.active = active;
		return checkbox;
	}

	public static ButtonComponent button(Component text, Consumer<ButtonComponent> action) {
		ButtonComponent button = Components.button(text, action);
		button.horizontalSizing(Sizing.fill(100));
		return button;
	}
}
