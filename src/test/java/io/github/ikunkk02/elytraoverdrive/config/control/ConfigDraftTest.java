package io.github.ikunkk02.elytraoverdrive.config.control;

import io.github.ikunkk02.elytraoverdrive.config.VisualPreset;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigDraftTest {
	@Test
	void unchangedDraftIsCleanAndEditsBecomeDirty() {
		ConfigDraft draft = new ConfigDraft(ConfigDraft.PlayerSettings.defaults());

		assertFalse(draft.isDirty());
		draft.playerSelectedMultiplier(5.0);
		assertTrue(draft.isDirty());
	}

	@Test
	void numericValuesClampToSupportedRanges() {
		ConfigDraft draft = new ConfigDraft(ConfigDraft.PlayerSettings.defaults());

		draft.playerSelectedMultiplier(200.0);
		assertEquals(200.0, draft.playerSelectedMultiplier());
	}

	@Test
	void restoreDefaultsTouchesOnlyPlayerOwnedDraftValues() {
		ConfigDraft.PlayerSettings original = new ConfigDraft.PlayerSettings(
				7.0, true, true, false, VisualPreset.CINEMATIC,
				false, false, false, true
		);
		ConfigDraft draft = new ConfigDraft(original);

		draft.restorePlayerDefaults();

		assertEquals(ConfigDraft.PlayerSettings.defaults(), draft.current());
		assertTrue(draft.isDirty());
	}

	@Test
	void disablingExperimentalModeImmediatelyClampsSelection() {
		ConfigDraft draft = new ConfigDraft(ConfigDraft.PlayerSettings.defaults());
		draft.enableExperimentalExtremeSpeed(true);
		draft.playerSelectedMultiplier(150.0);

		draft.enableExperimentalExtremeSpeed(false);

		assertFalse(draft.enableExperimentalExtremeSpeed());
		assertEquals(100.0, draft.playerSelectedMultiplier());
	}
}
