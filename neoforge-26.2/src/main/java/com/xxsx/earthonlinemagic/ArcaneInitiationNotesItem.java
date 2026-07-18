package com.xxsx.earthonlinemagic;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class ArcaneInitiationNotesItem extends Item {
    public ArcaneInitiationNotesItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            AetherChunkField.Reading reading = AetherChunkField.read(level, player.blockPosition());
            boolean learned = ArcanaPower.learnArcaneInitiation(player);
            long cooldown = ArcanaPower.getMagicFocusCooldownTicks(player, level);
            if (!learned && cooldown > 0L) {
                long seconds = (cooldown + 19L) / 20L;
                player.sendSystemMessage(Component.translatable("message.earth_online_magic.arcane_notes.cooldown",
                        seconds).withStyle(ChatFormatting.YELLOW));
                player.sendSystemMessage(Component.translatable("message.earth_online_magic.arcana.status",
                        ArcanaPower.format(ArcanaPower.getCurrentMana(player)),
                        ArcanaPower.format(ArcanaPower.getMaxMana(player)),
                        ArcanaPower.format(ArcanaPower.getXuanhuanBonus(player)),
                        ArcanaPower.format(ArcanaPower.getMagicBonus(player))).withStyle(ChatFormatting.GOLD));
                return InteractionResult.SUCCESS_SERVER;
            }

            double restored = ArcanaPower.focusAmbientMagic(player, reading.value());
            AetherChunkField.disturb(level, player.blockPosition(), Math.max(1.0D, restored));
            ArcanaPower.startMagicFocusCooldown(player, level);
            ArcanaPower.recordAction(player, level, "magic_training_initiation");
            EarthHumanCompat.RecoveryReport report = player instanceof ServerPlayer serverPlayer
                    ? EarthHumanCompat.recoverWard(serverPlayer, 1.0D + restored * 0.025D, 0.05D + restored * 0.003D)
                    : new EarthHumanCompat.RecoveryReport(0.0D, 0.0D);
            AetherChunkField.Reading after = AetherChunkField.read(level, player.blockPosition());
            player.sendSystemMessage(Component.translatable(learned
                    ? "message.earth_online_magic.arcane_notes.learned"
                    : "message.earth_online_magic.arcane_notes.practiced").withStyle(ChatFormatting.LIGHT_PURPLE));
            player.sendSystemMessage(Component.translatable("message.earth_online_magic.arcane_notes.focused",
                    AetherChunkField.gradeName(reading.value()),
                    reading.value(),
                    ArcanaPower.format(restored)).withStyle(ChatFormatting.AQUA));
            player.sendSystemMessage(Component.translatable("message.earth_online_magic.arcane_notes.field",
                    after.mainSource(),
                    ArcanaPower.format(after.disturbance())).withStyle(ChatFormatting.DARK_PURPLE));
            if (report.changed()) {
                player.sendSystemMessage(Component.translatable("message.earth_online_magic.human_recovery",
                        ArcanaPower.format(report.fatigueReduced()),
                        ArcanaPower.format(report.bodyHealed())).withStyle(ChatFormatting.GREEN));
            }
            player.sendSystemMessage(Component.translatable("message.earth_online_magic.arcana.status",
                    ArcanaPower.format(ArcanaPower.getCurrentMana(player)),
                    ArcanaPower.format(ArcanaPower.getMaxMana(player)),
                    ArcanaPower.format(ArcanaPower.getXuanhuanBonus(player)),
                    ArcanaPower.format(ArcanaPower.getMagicBonus(player))).withStyle(ChatFormatting.GOLD));
        }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
        lines.accept(Component.translatable("tooltip.earth_online_magic.arcane_initiation_notes").withStyle(ChatFormatting.LIGHT_PURPLE));
        lines.accept(Component.translatable("tooltip.earth_online_magic.arcane_initiation_notes.use").withStyle(ChatFormatting.GRAY));
    }
}
