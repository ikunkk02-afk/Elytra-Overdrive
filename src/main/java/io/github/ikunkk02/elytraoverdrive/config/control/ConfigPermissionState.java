package io.github.ikunkk02.elytraoverdrive.config.control;

public record ConfigPermissionState(boolean canEditHeldFireworkPreference, Authorization authorization) {
	public static ConfigPermissionState from(boolean serverAllowed, boolean ownerOverride) {
		if (ownerOverride) {
			return new ConfigPermissionState(true, Authorization.LOCAL_OWNER_OVERRIDE);
		}
		if (serverAllowed) {
			return new ConfigPermissionState(true, Authorization.SERVER_ALLOWED);
		}
		return new ConfigPermissionState(false, Authorization.LOCKED);
	}

	public enum Authorization {
		LOCKED,
		SERVER_ALLOWED,
		LOCAL_OWNER_OVERRIDE
	}
}
