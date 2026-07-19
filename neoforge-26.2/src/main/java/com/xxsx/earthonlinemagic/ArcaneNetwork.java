package com.xxsx.earthonlinemagic;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ArcaneNetwork {
    private ArcaneNetwork() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("0.7.0-alpha.5")
                .playToClient(ArcaneStatusPayload.TYPE, ArcaneStatusPayload.CODEC)
                .playToClient(ArcaneVisualPayload.TYPE, ArcaneVisualPayload.CODEC)
                .playToServer(ArcaneActionPayload.TYPE, ArcaneActionPayload.CODEC, ArcaneNetwork::handleAction);
    }

    public static void sync(ServerPlayer player, BlockPos pos, boolean openScreen) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        AetherChunkField.Reading reading = AetherChunkField.read(level, pos);
        EarthHumanCompat.HumanSnapshot human = EarthHumanCompat.snapshot(player);
        ArcaneFocus focus = ArcanaPower.getArcaneFocus(player);
        PacketDistributor.sendToPlayer(player, new ArcaneStatusPayload(
                ArcanaPower.getCurrentMana(player),
                ArcanaPower.getMaxMana(player),
                reading.value(),
                reading.disturbance(),
                (int) Math.min(Integer.MAX_VALUE, ArcanaPower.getMagicFocusCooldownTicks(player, level)),
                focus.id(),
                ArcanaPower.getArcaneFocusMask(player),
                MagicJourney.reconcile(player),
                ArcanaPower.getFocusLevel(player, focus),
                ArcanaPower.getFocusXp(player, focus),
                ArcanaPower.getFocusXpNeeded(player, focus),
                (int) Math.min(Integer.MAX_VALUE, ArcanaPower.getSkillCooldownTicks(player, level)),
                human.linked(),
                human.fatigue(),
                human.bodyIntegrity(),
                reading.mainSourceKey(),
                player.getVehicle() instanceof ArcaneSeatEntity,
                openScreen));
    }

    public static void broadcastVisual(ServerPlayer player, ArcaneVisualAction action) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                new ArcaneVisualPayload(player.getId(), action.id()));
    }

    private static void handleAction(ArcaneActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)
                    || !(player.level() instanceof ServerLevel level)) {
                return;
            }

            ArcaneSeatEntity seat = player.getVehicle() instanceof ArcaneSeatEntity arcaneSeat
                    ? arcaneSeat
                    : null;
            BlockPos pos = seat == null ? player.blockPosition() : seat.sourcePos();
            int action = payload.focusId();

            if (action == ArcaneActionPayload.OPEN_PANEL) {
                sync(player, pos, true);
                return;
            }
            if (action == ArcaneActionPayload.REFRESH_STATUS) {
                sync(player, pos, false);
                return;
            }
            if (action == ArcaneActionPayload.STOP_RIDING) {
                if (seat != null) {
                    player.stopRiding();
                }
                sync(player, player.blockPosition(), false);
                return;
            }
            if (action == ArcaneActionPayload.PRACTICE) {
                ArcanePractice.perform(level, pos, player,
                        seat == null ? ArcanePractice.Support.FREE : ArcanePractice.Support.FOCUS_MAT,
                        false);
                sync(player, pos, false);
                return;
            }
            if (action == ArcaneActionPayload.ACTIVATE_SKILL) {
                ArcaneSkill.activate(level, player);
                sync(player, pos, false);
                return;
            }
            if (action < 0) {
                return;
            }

            ArcaneFocus focus = ArcaneFocus.byId(action);
            if (!focus.isUnlocked(player)) {
                player.sendSystemMessage(Component.translatable(
                        "message.earth_online_magic.attunement.focus.locked",
                        Component.translatable(focus.titleKey())));
                sync(player, pos, false);
                return;
            }
            if (ArcanaPower.setArcaneFocus(player, focus)) {
                level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE,
                        SoundSource.PLAYERS, 0.58F, 0.92F + focus.id() * 0.12F);
                if (seat != null) {
                    seat.emitFocusChange(focus);
                } else {
                    ArcanePractice.emitFocusChange(level, pos, focus, false);
                }
            }
            sync(player, pos, false);
        });
    }
}
