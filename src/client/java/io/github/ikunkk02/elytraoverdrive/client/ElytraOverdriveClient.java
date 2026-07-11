package io.github.ikunkk02.elytraoverdrive.client;

import net.fabricmc.api.ClientModInitializer;

public class ElytraOverdriveClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientOverdriveNetworking.initialize();
		OverdriveVisuals.initialize();
	}
}
