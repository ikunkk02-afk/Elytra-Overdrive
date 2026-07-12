package io.github.ikunkk02.elytraoverdrive.config.control;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigPermissionStateTest {
	@Test
	void deniedServerLocksHeldFireworkPreference() {
		ConfigPermissionState state = ConfigPermissionState.from(false, false);
		assertFalse(state.canEditHeldFireworkPreference());
		assertEquals(ConfigPermissionState.Authorization.LOCKED, state.authorization());
	}

	@Test
	void serverPolicyOrOwnerOverrideUnlocksPreference() {
		assertTrue(ConfigPermissionState.from(true, false).canEditHeldFireworkPreference());
		assertEquals(
				ConfigPermissionState.Authorization.SERVER_ALLOWED,
				ConfigPermissionState.from(true, false).authorization()
		);
		assertTrue(ConfigPermissionState.from(false, true).canEditHeldFireworkPreference());
		assertEquals(
				ConfigPermissionState.Authorization.LOCAL_OWNER_OVERRIDE,
				ConfigPermissionState.from(false, true).authorization()
		);
	}

	@Test
	void terminalFormattingIsStable() {
		assertEquals("5.0×", ControlValueFormatter.multiplier(5.0));
		assertEquals("4.25 blocks/tick", ControlValueFormatter.speed(4.25));
		assertEquals("80 / 432", ControlValueFormatter.durability(80, 432));
	}
}
