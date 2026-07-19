package com.xxsx.earthonlinemagic;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        boolean learned = ArcanaPower.learnArcaneInitiation(player);
        player.sendSystemMessage(Component.translatable(learned
                        ? "message.earth_online_magic.arcane_notes.learned"
                        : "message.earth_online_magic.arcane_notes.already_learned")
                .withStyle(learned ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.YELLOW));
        if (learned && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            MagicJourney.complete(serverPlayer, MagicJourney.Milestone.INITIATION);
        }
        ArcaneAdaptationNotesItem.sendAdaptationStatus(player);
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            ArcaneNetwork.sync(serverPlayer, player.blockPosition(), true);
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
        lines.accept(Component.translatable("tooltip.earth_online_magic.arcane_initiation_notes").withStyle(ChatFormatting.LIGHT_PURPLE));
        lines.accept(Component.translatable("tooltip.earth_online_magic.arcane_initiation_notes.use").withStyle(ChatFormatting.GRAY));
    }
}
