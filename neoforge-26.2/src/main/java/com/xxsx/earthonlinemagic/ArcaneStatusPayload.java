package com.xxsx.earthonlinemagic;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ArcaneStatusPayload(
        double currentMana,
        double maxMana,
        int fieldValue,
        double disturbance,
        int remainingTicks,
        int focusId,
        int unlockedMask,
        int journeyMask,
        int focusLevel,
        int focusXp,
        int focusXpNeeded,
        int skillRemainingTicks,
        boolean earthHumanLinked,
        double fatigue,
        double bodyIntegrity,
        String sourceKey,
        boolean seated,
        boolean openScreen) implements CustomPacketPayload {
    public static final Type<ArcaneStatusPayload> TYPE =
            new Type<>(EarthOnlineMagic.id("arcane_status"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ArcaneStatusPayload> CODEC = new StreamCodec<>() {
        @Override
        public ArcaneStatusPayload decode(RegistryFriendlyByteBuf buf) {
            return new ArcaneStatusPayload(
                    buf.readDouble(), buf.readDouble(), buf.readVarInt(), buf.readDouble(),
                    buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                    buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                    buf.readBoolean(),
                    buf.readDouble(), buf.readDouble(), buf.readUtf(), buf.readBoolean(), buf.readBoolean());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ArcaneStatusPayload payload) {
            buf.writeDouble(payload.currentMana());
            buf.writeDouble(payload.maxMana());
            buf.writeVarInt(payload.fieldValue());
            buf.writeDouble(payload.disturbance());
            buf.writeVarInt(payload.remainingTicks());
            buf.writeVarInt(payload.focusId());
            buf.writeVarInt(payload.unlockedMask());
            buf.writeVarInt(payload.journeyMask());
            buf.writeVarInt(payload.focusLevel());
            buf.writeVarInt(payload.focusXp());
            buf.writeVarInt(payload.focusXpNeeded());
            buf.writeVarInt(payload.skillRemainingTicks());
            buf.writeBoolean(payload.earthHumanLinked());
            buf.writeDouble(payload.fatigue());
            buf.writeDouble(payload.bodyIntegrity());
            buf.writeUtf(payload.sourceKey());
            buf.writeBoolean(payload.seated());
            buf.writeBoolean(payload.openScreen());
        }
    };

    public static ArcaneStatusPayload empty() {
        return new ArcaneStatusPayload(0.0D, 20.0D, 0, 0.0D, 0,
                ArcaneFocus.ATTUNEMENT.id(), 0, 0, 0, 0, 0, 0, false, 0.0D, 1.0D,
                "aether.earth_online_magic.source.natural", false, false);
    }

    public boolean isUnlocked(ArcaneFocus focus) {
        return (unlockedMask & (1 << focus.id())) != 0;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
