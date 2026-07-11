package io.github.ikunkk02.elytraoverdrive.network;

import io.github.ikunkk02.elytraoverdrive.ElytraOverdrive;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record StopBombingC2SPayload() implements CustomPacketPayload {
	public static final Type<StopBombingC2SPayload> TYPE = new Type<>(ElytraOverdrive.id("stop_bombing_v1"));
	public static final StreamCodec<RegistryFriendlyByteBuf, StopBombingC2SPayload> CODEC = StreamCodec.unit(
			new StopBombingC2SPayload()
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
