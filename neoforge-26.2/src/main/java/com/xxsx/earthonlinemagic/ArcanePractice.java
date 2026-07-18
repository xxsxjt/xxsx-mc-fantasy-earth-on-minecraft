package com.xxsx.earthonlinemagic;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.Locale;

public final class ArcanePractice {
    public enum Support {
        FREE(0, 0.72D, 0.72D, "free"),
        FOCUS_MAT(16, 1.0D, 1.0D, "focus_mat");

        private final int fieldBonus;
        private final double efficiency;
        private final double recoveryScale;
        private final String translationSuffix;

        Support(int fieldBonus, double efficiency, double recoveryScale, String translationSuffix) {
            this.fieldBonus = fieldBonus;
            this.efficiency = efficiency;
            this.recoveryScale = recoveryScale;
            this.translationSuffix = translationSuffix;
        }

        int fieldBonus() {
            return fieldBonus;
        }

        double efficiency() {
            return efficiency;
        }

        double recoveryScale() {
            return recoveryScale;
        }

        int effectDuration(int baseTicks) {
            return Math.max(20, (int) Math.round(baseTicks * recoveryScale));
        }
    }

    private ArcanePractice() {
    }

    public static boolean perform(ServerLevel level, BlockPos pos, ServerPlayer player,
                                  Support support, boolean quietCooldown) {
        boolean learned = ArcanaPower.learnArcaneInitiation(player);
        long cooldown = ArcanaPower.getMagicFocusCooldownTicks(player, level);
        if (!learned && cooldown > 0L) {
            if (!quietCooldown && cooldown > 20L) {
                player.sendSystemMessage(Component.translatable("message.earth_online_magic.arcane_notes.cooldown",
                        (cooldown + 19L) / 20L).withStyle(ChatFormatting.YELLOW));
            }
            return false;
        }

        ArcaneFocus focus = ArcanaPower.getArcaneFocus(player);
        AetherChunkField.Reading reading = AetherChunkField.read(level, pos);
        int focusedAether = Math.min(100, reading.value() + support.fieldBonus());
        double focusScale = switch (focus) {
            case ATTUNEMENT -> 1.0D;
            case BODY_WARD -> 0.72D;
            case BREATH_WARD -> 0.82D;
        };
        int usableAether = Math.max(1,
                (int) Math.round(focusedAether * focusScale * support.efficiency()));
        double restored = ArcanaPower.focusAmbientMagic(player, usableAether);
        AetherChunkField.disturb(level, pos, Math.max(1.0D, restored));
        ArcanaPower.startMagicFocusCooldown(player, level);
        ArcanaPower.recordAction(player, level,
                "attunement_" + support.translationSuffix + "_" + focus.name().toLowerCase(Locale.ROOT));

        double recoveryScale = support.recoveryScale();
        EarthHumanCompat.RecoveryReport report;
        switch (focus) {
            case BODY_WARD -> {
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE,
                        support.effectDuration(140), 0, true, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,
                        support.effectDuration(160), 0, true, false, true));
                report = EarthHumanCompat.recoverWard(player,
                        (5.2D + restored * 0.075D) * recoveryScale,
                        (0.48D + restored * 0.016D) * recoveryScale);
            }
            case BREATH_WARD -> {
                player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING,
                        support.effectDuration(150), 0, true, false, true));
                int airCap = player.getMaxAirSupply() + (int) Math.round(ArcanaPower.getBreathCapacityBonus(player));
                player.setAirSupply(Math.min(airCap,
                        player.getAirSupply() + (int) Math.round(120.0D * recoveryScale)));
                report = EarthHumanCompat.recoverBreath(player,
                        (3.8D + restored * 0.055D) * recoveryScale,
                        (0.28D + restored * 0.010D) * recoveryScale);
            }
            default -> {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,
                        support.effectDuration(80), 0, true, false, true));
                report = EarthHumanCompat.recoverWard(player,
                        (3.0D + restored * 0.05D) * recoveryScale,
                        (0.22D + restored * 0.010D) * recoveryScale);
            }
        }

        String state = learned ? "learned" : "used";
        player.sendSystemMessage(Component.translatable(
                "message.earth_online_magic.practice." + state + "." + support.translationSuffix)
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        player.sendSystemMessage(Component.translatable("message.earth_online_magic.practice.result",
                AetherChunkField.gradeName(reading.value()),
                reading.value(),
                ArcanaPower.format(restored),
                ArcanaPower.format(report.fatigueReduced()),
                ArcanaPower.format(report.bodyHealed()),
                Math.round(support.efficiency() * 100.0D)).withStyle(ChatFormatting.AQUA));
        emitPractice(level, pos, focus, support);
        return true;
    }

    public static void emitFocusChange(ServerLevel level, BlockPos pos, ArcaneFocus focus, boolean supported) {
        var particle = switch (focus) {
            case ATTUNEMENT -> ParticleTypes.REVERSE_PORTAL;
            case BODY_WARD -> ParticleTypes.ENCHANT;
            case BREATH_WARD -> ParticleTypes.BUBBLE_POP;
        };
        level.sendParticles(particle,
                pos.getX() + 0.5D, pos.getY() + (supported ? 0.55D : 1.05D), pos.getZ() + 0.5D,
                supported ? 16 : 9, 0.44D, supported ? 0.30D : 0.48D, 0.44D, 0.025D);
    }

    private static void emitPractice(ServerLevel level, BlockPos pos, ArcaneFocus focus, Support support) {
        var particle = switch (focus) {
            case ATTUNEMENT -> ParticleTypes.REVERSE_PORTAL;
            case BODY_WARD -> ParticleTypes.WITCH;
            case BREATH_WARD -> ParticleTypes.BUBBLE_POP;
        };
        boolean supported = support == Support.FOCUS_MAT;
        level.sendParticles(particle,
                pos.getX() + 0.5D, pos.getY() + (supported ? 0.55D : 1.0D), pos.getZ() + 0.5D,
                supported ? 14 : 8, 0.42D, supported ? 0.28D : 0.52D, 0.42D, 0.018D);
        level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS,
                supported ? 0.70F : 0.46F, supported ? 1.10F : 0.92F);
    }
}
