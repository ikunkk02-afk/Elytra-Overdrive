package io.github.ikunkk02.elytraoverdrive.network;

import io.github.ikunkk02.elytraoverdrive.ElytraOverdrive;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SelectedMultiplierC2SPayload(double multiplier) implements CustomPacketPayload {
	public static final Type<SelectedMultiplierC2SPayload> TYPE = new Type<>(ElytraOverdrive.id("selected_multiplier_v1"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SelectedMultiplierC2SPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.DOUBLE,
			SelectedMultiplierC2SPayload::multiplier,
			SelectedMultiplierC2SPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
