package com.xxsx.earthonlinemagic;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ArcaneActionPayload(int focusId) implements CustomPacketPayload {
    public static final int STOP_RIDING = -1;
    public static final int OPEN_PANEL = -2;
    public static final int PRACTICE = -3;
    public static final int REFRESH_STATUS = -4;
    public static final Type<ArcaneActionPayload> TYPE =
            new Type<>(EarthOnlineMagic.id("arcane_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ArcaneActionPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT.cast(), ArcaneActionPayload::focusId,
            ArcaneActionPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
