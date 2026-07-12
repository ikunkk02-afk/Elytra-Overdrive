package io.github.ikunkk02.elytraoverdrive.client.config;

import io.github.ikunkk02.elytraoverdrive.ElytraOverdrive;
import io.github.ikunkk02.elytraoverdrive.client.ClientOverdriveNetworking;
import io.github.ikunkk02.elytraoverdrive.client.ClientOverdriveState;
import io.github.ikunkk02.elytraoverdrive.config.control.ConfigDraft;
import io.github.ikunkk02.elytraoverdrive.config.control.ControlScreenState;
import io.github.ikunkk02.elytraoverdrive.config.control.NavigationSection;
import io.github.ikunkk02.elytraoverdrive.network.RequiredClientPayload;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public final class OverdriveControlScreen extends BaseOwoScreen<FlowLayout> {
	private final Screen parent;
	private final ConfigDraft draft;
	private final ControlScreenState state = new ControlScreenState();
	private FlightStatusPanel statusPanel;
	private LabelComponent savedNotice;
	private boolean confirmDefaults;
	private boolean confirmExit;
	private boolean lastMayRequestHeldFirework;
	private ControlScreenState.LayoutMode layoutMode;

	public OverdriveControlScreen(Screen parent) {
		super(Component.translatable("screen.elytra_overdrive.title"));
		this.parent = parent;
		this.draft = ConfigDraft.from(ElytraOverdrive.CONFIG);
		this.lastMayRequestHeldFirework = ClientOverdriveState.mayRequestHeldFirework();
	}

	@Override
	protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
		return OwoUIAdapter.create(this, Containers::verticalFlow);
	}

	@Override
	protected void build(FlowLayout root) {
		this.layoutMode = ControlScreenState.layoutForWidth(this.width);
		root.surface(ControlTerminalTheme.background());
		root.padding(Insets.of(this.layoutMode == ControlScreenState.LayoutMode.COMPACT ? 5 : 10));
		root.gap(6);
		root.child(buildHeader());
		root.child(buildBody());
		root.child(buildFooter());
	}

	private FlowLayout buildHeader() {
		FlowLayout header = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
		header.surface(ControlTerminalTheme.panel());
		header.padding(Insets.of(7));
		header.verticalAlignment(VerticalAlignment.CENTER);

		FlowLayout identity = Containers.verticalFlow(Sizing.fill(70), Sizing.content());
		identity.child(ControlPanelComponents.label(
				Component.literal("ELYTRA OVERDRIVE"), ControlTerminalTheme.TEXT
		).shadow(true));
		identity.child(ControlPanelComponents.label(
				Component.translatable("screen.elytra_overdrive.subtitle"), ControlTerminalTheme.SECONDARY
		));
		this.savedNotice = ControlPanelComponents.label(
				this.state.savedNoticeVisible()
						? Component.translatable("screen.elytra_overdrive.saved")
						: Component.empty(),
				ControlTerminalTheme.ACCENT
		);
		identity.child(this.savedNotice);
		header.child(identity);

		FlowLayout system = Containers.verticalFlow(Sizing.fill(30), Sizing.content());
		system.horizontalAlignment(HorizontalAlignment.RIGHT);
		system.child(ControlPanelComponents.label(
				Component.translatable("screen.elytra_overdrive.system_online"), ControlTerminalTheme.ACCENT
		));
		system.child(ControlPanelComponents.label(
				Component.translatable("screen.elytra_overdrive.protocol", RequiredClientPayload.CURRENT_PROTOCOL),
				ControlTerminalTheme.SECONDARY
		));
		header.child(system);

		return header;
	}

	private FlowLayout buildBody() {
		FlowLayout body = Containers.horizontalFlow(Sizing.fill(100), Sizing.expand());
		body.gap(6);
		body.child(buildNavigation());

		FlowLayout content = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
		content.gap(6);
		if (this.layoutMode == ControlScreenState.LayoutMode.COMPACT) {
			this.statusPanel = new FlightStatusPanel(Sizing.fill(100));
			content.child(this.statusPanel.component());
		}
		content.child(buildSelectedPanel());
		body.child(Containers.verticalScroll(Sizing.expand(), Sizing.expand(), content).scrollbarThiccness(4));

		if (this.layoutMode == ControlScreenState.LayoutMode.THREE_COLUMN) {
			this.statusPanel = new FlightStatusPanel(Sizing.fixed(220));
			body.child(this.statusPanel.component());
		}
		return body;
	}

	private FlowLayout buildNavigation() {
		int width = this.layoutMode == ControlScreenState.LayoutMode.COMPACT ? 105 : 145;
		FlowLayout navigation = ControlPanelComponents.panel(Sizing.fixed(width));
		for (NavigationSection section : NavigationSection.values()) {
			ButtonComponent button = ControlPanelComponents.button(
					Component.translatable(section.translationKey()),
					pressed -> {
						this.state.select(section);
						this.confirmDefaults = false;
						this.confirmExit = false;
						rebuild();
					}
			);
			button.active(section != this.state.selected());
			navigation.child(button);
		}
		return navigation;
	}

	private FlowLayout buildSelectedPanel() {
		return switch (this.state.selected()) {
			case FLIGHT -> FlightSettingsPanel.build(this.draft);
			case BOMBING -> BombingSettingsPanel.build();
			case BREACH -> BreachSettingsPanel.build();
			case VISUAL -> VisualSettingsPanel.build(this.draft, this::rebuild);
			case SERVER -> ServerPolicyPanel.build();
		};
	}

	private FlowLayout buildFooter() {
		FlowLayout footer = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
		footer.surface(ControlTerminalTheme.panel());
		footer.padding(Insets.of(6));
		footer.gap(6);
		ButtonComponent save = ControlPanelComponents.button(
				Component.translatable("screen.elytra_overdrive.save"), pressed -> save()
		);
		save.horizontalSizing(Sizing.fill(33));
		footer.child(save);
		ButtonComponent defaults = ControlPanelComponents.button(
				Component.translatable(this.confirmDefaults
						? "screen.elytra_overdrive.confirm_defaults"
						: "screen.elytra_overdrive.defaults"),
				pressed -> restoreDefaults()
		);
		defaults.horizontalSizing(Sizing.fill(33));
		footer.child(defaults);
		ButtonComponent back = ControlPanelComponents.button(
				Component.translatable(this.confirmExit
						? "screen.elytra_overdrive.confirm_exit"
						: "screen.elytra_overdrive.back"),
				pressed -> onClose()
		);
		back.horizontalSizing(Sizing.fill(34));
		footer.child(back);
		return footer;
	}

	private void save() {
		this.draft.applyTo(ElytraOverdrive.CONFIG);
		ClientOverdriveNetworking.sendPreferences();
		this.state.showSavedNotice();
		this.confirmDefaults = false;
		this.confirmExit = false;
		if (this.savedNotice != null) {
			this.savedNotice.text(Component.translatable("screen.elytra_overdrive.saved"));
		}
	}

	private void restoreDefaults() {
		if (!this.confirmDefaults) {
			this.confirmDefaults = true;
			rebuild();
			return;
		}
		this.draft.restorePlayerDefaults();
		this.confirmDefaults = false;
		rebuild();
	}

	@Override
	public void onClose() {
		if (this.draft.isDirty() && !this.confirmExit) {
			this.confirmExit = true;
			rebuild();
			return;
		}
		Minecraft.getInstance().setScreen(this.parent);
	}

	@Override
	public void tick() {
		super.tick();
		this.state.tick();
		if (this.statusPanel != null) this.statusPanel.refresh();
		if (this.savedNotice != null && !this.state.savedNoticeVisible()) {
			this.savedNotice.text(Component.empty());
		}
		boolean mayRequest = ClientOverdriveState.mayRequestHeldFirework();
		if (mayRequest != this.lastMayRequestHeldFirework) {
			this.lastMayRequestHeldFirework = mayRequest;
			rebuild();
		}
	}

	@Override
	public void resize(Minecraft client, int width, int height) {
		ControlScreenState.LayoutMode previous = this.layoutMode;
		super.resize(client, width, height);
		if (this.uiAdapter != null && previous != ControlScreenState.layoutForWidth(width)) {
			rebuild();
		}
	}

	private void rebuild() {
		if (this.uiAdapter == null) return;
		FlowLayout root = this.uiAdapter.rootComponent;
		root.clearChildren();
		build(root);
	}
}
