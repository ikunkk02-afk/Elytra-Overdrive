package io.github.ikunkk02.elytraoverdrive;

import io.github.ikunkk02.elytraoverdrive.config.OverdriveConfig;
import io.github.ikunkk02.elytraoverdrive.bombing.BombingHandler;
import io.github.ikunkk02.elytraoverdrive.breach.BreachHandler;
import io.github.ikunkk02.elytraoverdrive.flight.OverdriveFlightHandler;
import io.github.ikunkk02.elytraoverdrive.network.OverdriveNetworking;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElytraOverdrive implements ModInitializer {
	public static final String MOD_ID = "elytra-overdrive";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final OverdriveConfig CONFIG = OverdriveConfig.createAndLoad();

	@Override
	public void onInitialize() {
		OverdriveNetworking.initialize();
		OverdriveFlightHandler.initialize();
		BombingHandler.initialize();
		BreachHandler.initialize();
		LOGGER.info("Initializing Elytra Overdrive");
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
