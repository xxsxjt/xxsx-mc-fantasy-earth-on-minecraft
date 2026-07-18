package com.xxsx.earthonlinemagic;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class ArcaneAdaptationNotesItem extends Item {
    public enum Type {
        BODY_WARD("arcane_body_ward_notes"),
        BREATH_WARD("arcane_breath_ward_notes");

        private final String id;

        Type(String id) {
            this.id = id;
        }
    }

    private final Type type;

    public ArcaneAdaptationNotesItem(Properties properties, Type type) {
        super(properties);
        this.type = type;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            AetherChunkField.Reading reading = AetherChunkField.read(level, player.blockPosition());
            boolean learned = switch (type) {
                case BODY_WARD -> ArcanaPower.learnArcaneBodyWard(player);
                case BREATH_WARD -> ArcanaPower.learnArcaneBreathWard(player);
            };
            long cooldown = ArcanaPower.getMagicFocusCooldownTicks(player, level);
            if (!learned && cooldown > 0L) {
                long seconds = (cooldown + 19L) / 20L;
                player.sendSystemMessage(Component.translatable("message.earth_online_magic.arcane_notes.cooldown",
                        seconds).withStyle(ChatFormatting.YELLOW));
                return InteractionResult.SUCCESS_SERVER;
            }

            double restored = ArcanaPower.focusAmbientMagic(player, reading.value());
            AetherChunkField.disturb(level, player.blockPosition(), Math.max(1.0D, restored));
            ArcanaPower.startMagicFocusCooldown(player, level);
            ArcanaPower.recordAction(player, level, "combat_" + type.id);
            if (type == Type.BODY_WARD) {
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 220, 0, true, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 220, 0, true, false, true));
            } else {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, true, false, true));
            }
            EarthHumanCompat.RecoveryReport report = player instanceof ServerPlayer serverPlayer
                    ? recoverHuman(serverPlayer, restored)
                    : new EarthHumanCompat.RecoveryReport(0.0D, 0.0D);
            AetherChunkField.Reading after = AetherChunkField.read(level, player.blockPosition());
            player.sendSystemMessage(Component.translatable("message.earth_online_magic." + type.id + (learned ? ".learned" : ".practiced"))
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            player.sendSystemMessage(Component.translatable("message.earth_online_magic.arcane_adaptation.body_status",
                    ArcanaPower.getArcaneBodyWardLevel(player),
                    ArcanaPower.getArcaneBreathWardLevel(player),
                    ArcanaPower.format(ArcanaPower.getBreathCapacityBonus(player)),
                    ArcanaPower.format(ArcanaPower.getEnduranceBonus(player)),
                    ArcanaPower.format(restored)).withStyle(ChatFormatting.AQUA));
            player.sendSystemMessage(Component.translatable("message.earth_online_magic.arcane_notes.field",
                    after.mainSource(),
                    ArcanaPower.format(after.disturbance())).withStyle(ChatFormatting.DARK_PURPLE));
            if (report.changed()) {
                player.sendSystemMessage(Component.translatable("message.earth_online_magic.human_recovery",
                        ArcanaPower.format(report.fatigueReduced()),
                        ArcanaPower.format(report.bodyHealed())).withStyle(ChatFormatting.GREEN));
            }
        }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }

    private EarthHumanCompat.RecoveryReport recoverHuman(ServerPlayer player, double restoredMana) {
        return switch (type) {
            case BODY_WARD -> EarthHumanCompat.recoverWard(player, 2.4D + restoredMana * 0.04D,
                    0.22D + restoredMana * 0.010D);
            case BREATH_WARD -> EarthHumanCompat.recoverBreath(player, 1.8D + restoredMana * 0.03D,
                    0.18D + restoredMana * 0.008D);
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
        lines.accept(Component.translatable("tooltip.earth_online_magic." + type.id).withStyle(ChatFormatting.LIGHT_PURPLE));
        lines.accept(Component.translatable("tooltip.earth_online_magic." + type.id + ".use").withStyle(ChatFormatting.GRAY));
    }
}
