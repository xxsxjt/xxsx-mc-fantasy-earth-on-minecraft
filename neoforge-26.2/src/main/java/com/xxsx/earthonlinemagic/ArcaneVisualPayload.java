package com.xxsx.earthonlinemagic;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ArcaneVisualPayload(int entityId, int actionId) implements CustomPacketPayload {
    public static final Type<ArcaneVisualPayload> TYPE =
            new Type<>(EarthOnlineMagic.id("arcane_visual"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ArcaneVisualPayload> CODEC =
            new StreamCodec<>() {
                @Override
                public ArcaneVisualPayload decode(RegistryFriendlyByteBuf buf) {
                    return new ArcaneVisualPayload(buf.readVarInt(), buf.readVarInt());
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, ArcaneVisualPayload payload) {
                    buf.writeVarInt(payload.entityId());
                    buf.writeVarInt(payload.actionId());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
