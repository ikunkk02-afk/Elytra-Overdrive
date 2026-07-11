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
	void overdriveIsAvailableThroughTheNonTreasurePool() throws IOException {
		JsonObject tag = readJson(RESOURCES.resolve("data/minecraft/tags/enchantment/non_treasure.json"));
		List<String> values = tag.getAsJsonArray("values").asList().stream().map(element -> element.getAsString()).toList();

		assertTrue(values.contains("elytra-overdrive:overdrive"));
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
			assertTrue(translations.has("text.config.elytra-overdrive.title"));
			assertTrue(translations.has("text.config.elytra-overdrive.option.playerSelectedMultiplier"));
			assertTrue(translations.has("text.config.elytra-overdrive.option.serverMaximumMultiplier"));
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
