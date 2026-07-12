package io.github.ikunkk02.elytraoverdrive.client;

import io.github.ikunkk02.elytraoverdrive.ElytraOverdrive;
import io.github.ikunkk02.elytraoverdrive.client.config.OverdriveControlScreen;
import io.wispforest.owo.config.ui.ConfigScreenProviders;
import net.fabricmc.api.ClientModInitializer;

public class ElytraOverdriveClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ConfigScreenProviders.register(ElytraOverdrive.MOD_ID, OverdriveControlScreen::new);
		ClientOverdriveNetworking.initialize();
		OverdriveVisuals.initialize();
		BombingInputHandler.initialize();
	}
}
