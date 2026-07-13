package io.github.ikunkk02.elytraoverdrive.client.config;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public final class ExperimentalSpeedWarningScreen extends BaseOwoScreen<FlowLayout> {
	private final Screen parent;
	private final Runnable confirmAction;

	public ExperimentalSpeedWarningScreen(Screen parent, Runnable confirmAction) {
		super(Component.translatable("screen.elytra_overdrive.experimental_warning.title"));
		this.parent = parent;
		this.confirmAction = confirmAction;
	}

	@Override
	protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
		return OwoUIAdapter.create(this, Containers::verticalFlow);
	}

	@Override
	protected void build(FlowLayout root) {
		root.surface(ControlTerminalTheme.background());
		root.padding(Insets.of(12));
		root.gap(8);
		root.horizontalAlignment(HorizontalAlignment.CENTER);

		FlowLayout dialog = ControlPanelComponents.panel(Sizing.fill(100), ControlTerminalTheme.WARNING);
		dialog.horizontalSizing(Sizing.fixed(Math.min(720, Math.max(320, this.width - 40))));
		dialog.child(ControlPanelComponents.label(
				Component.translatable("screen.elytra_overdrive.experimental_warning.title"),
				ControlTerminalTheme.WARNING
		).shadow(true));

		FlowLayout message = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
		message.child(ControlPanelComponents.label(
				Component.translatable("screen.elytra_overdrive.experimental_warning.body"),
				ControlTerminalTheme.TEXT
		).maxWidth(Math.min(680, Math.max(280, this.width - 80))));
		dialog.child(Containers.verticalScroll(
				Sizing.fill(100), Sizing.fixed(Math.max(220, this.height - 150)), message
		).scrollbarThiccness(4));

		FlowLayout buttons = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
		buttons.gap(8);
		ButtonComponent cancel = ControlPanelComponents.button(
				Component.translatable("screen.elytra_overdrive.experimental_warning.cancel"),
				button -> cancel()
		);
		cancel.horizontalSizing(Sizing.fill(40));
		buttons.child(cancel);
		ButtonComponent confirm = ControlPanelComponents.button(
				Component.translatable("screen.elytra_overdrive.experimental_warning.confirm"),
				button -> confirm()
		);
		confirm.horizontalSizing(Sizing.fill(60));
		buttons.child(confirm);
		dialog.child(buttons);
		root.child(dialog);
		this.setInitialFocus(cancel);
	}

	private void cancel() {
		Minecraft.getInstance().setScreen(this.parent);
	}

	private void confirm() {
		this.confirmAction.run();
		Minecraft.getInstance().setScreen(this.parent);
	}

	@Override
	public void onClose() {
		cancel();
	}
}
