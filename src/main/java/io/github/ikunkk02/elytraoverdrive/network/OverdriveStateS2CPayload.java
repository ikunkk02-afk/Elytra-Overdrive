package io.github.ikunkk02.elytraoverdrive.network;

import io.github.ikunkk02.elytraoverdrive.ElytraOverdrive;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record OverdriveStateS2CPayload(
		double effectiveMultiplier,
		boolean active,
		int activationSourceOrdinal,
		boolean allowHeldFireworkOverdrive,
		boolean localOwnerOverride,
		boolean acceptedHeldFireworkPreference
) implements CustomPacketPayload {
	public static final Type<OverdriveStateS2CPayload> TYPE = new Type<>(ElytraOverdrive.id("state_v2"));
	public static final StreamCodec<RegistryFriendlyByteBuf, OverdriveStateS2CPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.DOUBLE,
			OverdriveStateS2CPayload::effectiveMultiplier,
			ByteBufCodecs.BOOL,
			OverdriveStateS2CPayload::active,
			ByteBufCodecs.VAR_INT,
			OverdriveStateS2CPayload::activationSourceOrdinal,
			ByteBufCodecs.BOOL,
			OverdriveStateS2CPayload::allowHeldFireworkOverdrive,
			ByteBufCodecs.BOOL,
			OverdriveStateS2CPayload::localOwnerOverride,
			ByteBufCodecs.BOOL,
			OverdriveStateS2CPayload::acceptedHeldFireworkPreference,
			OverdriveStateS2CPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
