package io.github.ikunkk02.elytraoverdrive.network;

import io.github.ikunkk02.elytraoverdrive.ElytraOverdrive;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RequiredClientPayload(int protocolVersion) implements CustomPacketPayload {
	public static final int CURRENT_PROTOCOL = 2;
	public static final Type<RequiredClientPayload> TYPE = new Type<>(ElytraOverdrive.id("required_client_v1"));
	public static final StreamCodec<FriendlyByteBuf, RequiredClientPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT,
			RequiredClientPayload::protocolVersion,
			RequiredClientPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
