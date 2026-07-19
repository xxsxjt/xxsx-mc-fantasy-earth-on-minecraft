package com.xxsx.earthonlinemagic;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ManaConsumableItem extends MagicMaterialItem {
    private final double restoreAmount;

    public ManaConsumableItem(Properties properties, String hintKey, double restoreAmount) {
        super(properties, hintKey);
        this.restoreAmount = restoreAmount;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            ItemStack stack = player.getItemInHand(hand);
            Component displayName = stack.getHoverName().copy();
            double before = ArcanaPower.getCurrentMana(player);
            ArcanaPower.setCurrentMana(player, before + restoreAmount);
            double restored = ArcanaPower.getCurrentMana(player) - before;
            if (restored <= 0.0D) {
                player.sendSystemMessage(Component.translatable(
                        "message.earth_online_magic.mana_consumable.full",
                        ArcanaPower.format(ArcanaPower.getCurrentMana(player)),
                        ArcanaPower.format(ArcanaPower.getMaxMana(player))).withStyle(ChatFormatting.YELLOW));
                return InteractionResult.SUCCESS_SERVER;
            }
            if (restored > 0.0D) {
                AetherChunkField.disturb(level, player.blockPosition(), restored * 0.25D);
            }
            if (player instanceof ServerPlayer serverPlayer) {
                ArcaneNetwork.broadcastVisual(serverPlayer, ArcaneVisualAction.MANA_RECOVERY);
            }
            EarthHumanCompat.RecoveryReport report = player instanceof ServerPlayer serverPlayer
                    ? EarthHumanCompat.recoverWard(serverPlayer, Math.min(3.5D, restored * 0.08D), Math.min(0.20D, restored * 0.006D))
                    : new EarthHumanCompat.RecoveryReport(0.0D, 0.0D);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            if (player instanceof ServerPlayer serverPlayer) {
                MagicJourney.complete(serverPlayer, MagicJourney.Milestone.MANA_RECOVERY);
            }
            player.sendSystemMessage(Component.translatable("message.earth_online_magic.mana_consumable.used",
                    displayName,
                    ArcanaPower.format(restored),
                    ArcanaPower.format(ArcanaPower.getCurrentMana(player)),
                    ArcanaPower.format(ArcanaPower.getMaxMana(player))).withStyle(ChatFormatting.LIGHT_PURPLE));
            if (report.changed()) {
                player.sendSystemMessage(Component.translatable("message.earth_online_magic.human_recovery",
                        ArcanaPower.format(report.fatigueReduced()),
                        ArcanaPower.format(report.bodyHealed())).withStyle(ChatFormatting.GREEN));
            }
        }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }
}
