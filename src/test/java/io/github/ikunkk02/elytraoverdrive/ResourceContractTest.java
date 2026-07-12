package io.github.ikunkk02.elytraoverdrive;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceContractTest {
	private static final Path RESOURCES = Path.of("src", "main", "resources");

	@Test
	void overdriveEnchantmentUsesTheDedicatedElytraTag() throws IOException {
		JsonObject enchantment = readJson(RESOURCES.resolve("data/elytra-overdrive/enchantment/overdrive.json"));

		assertEquals("#elytra-overdrive:enchantable/overdrive", enchantment.get("supported_items").getAsString());
		assertEquals("#elytra-overdrive:enchantable/overdrive", enchantment.get("primary_items").getAsString());
		assertEquals(1, enchantment.get("max_level").getAsInt());
		assertEquals(2, enchantment.get("weight").getAsInt());
		assertEquals("chest", enchantment.getAsJsonArray("slots").get(0).getAsString());
	}

	@Test
	void dedicatedItemTagContainsOnlyVanillaElytra() throws IOException {
		JsonObject tag = readJson(RESOURCES.resolve("data/elytra-overdrive/tags/item/enchantable/overdrive.json"));
		JsonArray values = tag.getAsJsonArray("values");

		assertEquals(1, values.size());
		assertEquals("minecraft:elytra", values.get(0).getAsString());
	}

	@Test
	void breachEnchantmentUsesTheDedicatedTridentTag() throws IOException {
		JsonObject enchantment = readJson(RESOURCES.resolve("data/elytra-overdrive/enchantment/breach.json"));

		assertEquals("#elytra-overdrive:enchantable/breach", enchantment.get("supported_items").getAsString());
		assertEquals(3, enchantment.get("max_level").getAsInt());
		assertEquals("mainhand", enchantment.getAsJsonArray("slots").get(0).getAsString());
	}

	@Test
	void breachItemTagContainsOnlyVanillaTrident() throws IOException {
		JsonObject tag = readJson(RESOURCES.resolve("data/elytra-overdrive/tags/item/enchantable/breach.json"));
		JsonArray values = tag.getAsJsonArray("values");

		assertEquals(1, values.size());
		assertEquals("minecraft:trident", values.get(0).getAsString());
	}

	@Test
	void overdriveIsAvailableThroughTheNonTreasurePool() throws IOException {
		JsonObject tag = readJson(RESOURCES.resolve("data/minecraft/tags/enchantment/non_treasure.json"));
		List<String> values = tag.getAsJsonArray("values").asList().stream().map(element -> element.getAsString()).toList();

		assertTrue(values.contains("elytra-overdrive:overdrive"));
		assertTrue(values.contains("elytra-overdrive:breach"));
	}

	@Test
	void metadataKeepsOwoRequiredAndModMenuOptional() throws IOException {
		JsonObject metadata = readJson(RESOURCES.resolve("fabric.mod.json"));

		assertEquals("elytra-overdrive", metadata.get("id").getAsString());
		assertTrue(metadata.getAsJsonObject("depends").has("owo"));
		assertFalse(metadata.getAsJsonObject("depends").has("modmenu"));
		assertTrue(metadata.getAsJsonObject("suggests").has("modmenu"));
	}

	@Test
	void bothLanguagesContainEnchantmentAndConfigText() throws IOException {
		for (String language : List.of("en_us", "zh_cn")) {
			JsonObject translations = readJson(RESOURCES.resolve("assets/elytra-overdrive/lang/" + language + ".json"));

			assertTrue(translations.has("enchantment.elytra_overdrive.overdrive"));
			assertTrue(translations.has("enchantment.elytra_overdrive.overdrive.description"));
			assertTrue(translations.has("enchantment.elytra_overdrive.breach"));
			assertTrue(translations.has("enchantment.elytra_overdrive.breach.description"));
			assertTrue(translations.has("text.config.elytra-overdrive.title"));
			assertTrue(translations.has("text.config.elytra-overdrive.option.playerSelectedMultiplier"));
			assertTrue(translations.has("text.config.elytra-overdrive.option.serverMaximumMultiplier"));
			assertTrue(translations.has("text.config.elytra-overdrive.option.enableTridentBreach"));
		}
	}

	@Test
	void mainSourcesDoNotImportMinecraftClientClasses() throws IOException {
		Path mainJava = Path.of("src", "main", "java");

		try (var files = Files.walk(mainJava)) {
			List<Path> offenders = files
					.filter(path -> path.toString().endsWith(".java"))
					.filter(path -> contains(path, "import net.minecraft.client"))
					.toList();
			assertTrue(offenders.isEmpty(), () -> "Client imports found in main sources: " + offenders);
		}
	}

	@Test
	void breachKeepsSpawnProtectionAndOwnsDurabilityDamage() throws IOException {
		Path handler = Path.of("src", "main", "java", "io", "github", "ikunkk02", "elytraoverdrive", "breach", "BreachHandler.java");
		Path mixin = Path.of("src", "main", "java", "io", "github", "ikunkk02", "elytraoverdrive", "mixin", "ServerPlayerGameModeMixin.java");

		assertTrue(contains(handler, "isUnderSpawnProtection"));
		assertTrue(contains(mixin, "suppressNativeBreachDurability"));
	}

	@Test
	void bombingInputResetsAcrossClientConnections() throws IOException {
		Path input = Path.of("src", "client", "java", "io", "github", "ikunkk02", "elytraoverdrive", "client", "BombingInputHandler.java");

		assertTrue(contains(input, "ClientPlayConnectionEvents.DISCONNECT"));
	}

	@Test
	void elytraEnchantingMixinOnlyChangesVanillaElytraEnchantability() throws IOException {
		Path mixin = Path.of("src", "main", "java", "io", "github", "ikunkk02", "elytraoverdrive", "mixin", "ItemMixin.java");
		String source = Files.readString(mixin);

		assertTrue(source.contains("@Mixin(Item.class)"));
		assertTrue(source.contains("method = \"getEnchantmentValue\""));
		assertTrue(source.contains("@At(\"RETURN\")"));
		assertTrue(source.contains("(Object)this == Items.ELYTRA"));
		assertFalse(source.contains("EnchantmentMenu"));
	}

	@Test
	void heldFireworkProtocolAndAuthorityRemainServerOwned() throws IOException {
		Path configModel = Path.of("src", "main", "java", "io", "github", "ikunkk02", "elytraoverdrive", "config", "OverdriveConfigModel.java");
		Path requiredPayload = Path.of("src", "main", "java", "io", "github", "ikunkk02", "elytraoverdrive", "network", "RequiredClientPayload.java");
		Path statePayload = Path.of("src", "main", "java", "io", "github", "ikunkk02", "elytraoverdrive", "network", "OverdriveStateS2CPayload.java");
		Path preferencePayload = Path.of("src", "main", "java", "io", "github", "ikunkk02", "elytraoverdrive", "network", "HeldFireworkPreferenceC2SPayload.java");
		Path handler = Path.of("src", "main", "java", "io", "github", "ikunkk02", "elytraoverdrive", "flight", "OverdriveFlightHandler.java");

		assertTrue(contains(configModel, "enableHeldFireworkOverdrive = false"));
		assertTrue(contains(configModel, "allowHeldFireworkOverdrive = false"));
		assertTrue(contains(requiredPayload, "CURRENT_PROTOCOL = 3"));
		assertTrue(contains(statePayload, "state_v2"));
		assertTrue(contains(statePayload, "activationSourceOrdinal"));
		assertTrue(Files.exists(preferencePayload));
		assertTrue(contains(handler, "isSingleplayerOwner"));
		assertTrue(contains(handler, "HeldFireworkRules.isHoldingRocket"));
		assertFalse(contains(handler, "shrink("));
	}

	@Test
	void fireworkModeCommandUsesPermissionFourAndPersistsPolicy() throws IOException {
		Path command = Path.of("src", "main", "java", "io", "github", "ikunkk02", "elytraoverdrive", "command", "OverdriveCommands.java");
		assertTrue(Files.exists(command));
		assertTrue(contains(command, "hasPermission(4)"));
		assertTrue(contains(command, "allowHeldFireworkOverdrive(enabled)"));
		assertTrue(contains(command, "CONFIG.save()"));
		assertTrue(contains(command, "refreshPolicyForAll"));

		for (String language : List.of("en_us", "zh_cn")) {
			JsonObject translations = readJson(RESOURCES.resolve("assets/elytra-overdrive/lang/" + language + ".json"));
			assertTrue(translations.has("command.elytra_overdrive.firework_mode.enabled"));
			assertTrue(translations.has("command.elytra_overdrive.firework_mode.disabled"));
			assertTrue(translations.has("command.elytra_overdrive.no_permission"));
		}
	}

	private static JsonObject readJson(Path path) throws IOException {
		return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
	}

	private static boolean contains(Path path, String needle) {
		try {
			return Files.readString(path).contains(needle);
		} catch (IOException exception) {
			throw new IllegalStateException("Could not inspect " + path, exception);
		}
	}
}
