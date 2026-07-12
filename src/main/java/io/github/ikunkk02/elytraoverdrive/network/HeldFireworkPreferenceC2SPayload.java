package io.github.ikunkk02.elytraoverdrive.network;

import io.github.ikunkk02.elytraoverdrive.ElytraOverdrive;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record HeldFireworkPreferenceC2SPayload(boolean enabled) implements CustomPacketPayload {
	public static final Type<HeldFireworkPreferenceC2SPayload> TYPE =
			new Type<>(ElytraOverdrive.id("held_firework_preference_v1"));
	public static final StreamCodec<RegistryFriendlyByteBuf, HeldFireworkPreferenceC2SPayload> CODEC =
			StreamCodec.composite(
					ByteBufCodecs.BOOL,
					HeldFireworkPreferenceC2SPayload::enabled,
					HeldFireworkPreferenceC2SPayload::new
			);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
