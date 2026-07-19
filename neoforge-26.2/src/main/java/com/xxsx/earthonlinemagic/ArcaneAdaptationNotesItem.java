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
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (ArcanaPower.getMagicResearchLevel(player) <= 0) {
            player.sendSystemMessage(Component.translatable(
                    "message.earth_online_magic.arcane_adaptation.requires_initiation")
                    .withStyle(ChatFormatting.RED));
            return InteractionResult.SUCCESS_SERVER;
        }
        boolean learned = switch (type) {
            case BODY_WARD -> ArcanaPower.learnArcaneBodyWard(player);
            case BREATH_WARD -> ArcanaPower.learnArcaneBreathWard(player);
        };
        ArcaneFocus focus = switch (type) {
            case BODY_WARD -> ArcaneFocus.BODY_WARD;
            case BREATH_WARD -> ArcaneFocus.BREATH_WARD;
        };
        ArcanaPower.setArcaneFocus(player, focus);
        player.sendSystemMessage(Component.translatable("message.earth_online_magic." + type.id
                        + (learned ? ".learned" : ".already_learned"))
                .withStyle(learned ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.YELLOW));
        sendAdaptationStatus(player);
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            ArcaneNetwork.sync(serverPlayer, player.blockPosition(), true);
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
        lines.accept(Component.translatable("tooltip.earth_online_magic." + type.id).withStyle(ChatFormatting.LIGHT_PURPLE));
        lines.accept(Component.translatable("tooltip.earth_online_magic." + type.id + ".use").withStyle(ChatFormatting.GRAY));
    }

    static void sendAdaptationStatus(Player player) {
        player.sendSystemMessage(Component.translatable(
                "message.earth_online_magic.arcane_adaptation.body_status",
                Math.round(ArcanaPower.getExhaustionReduction(player) * 100.0D),
                Math.round(ArcanaPower.getCombatDamageReduction(player) * 100.0D),
                Math.round((ArcanaPower.getBreathMultiplier(player) - 1.0D) * 100.0D),
                ArcanaPower.format(ArcanaPower.getMaxMana(player)))
                .withStyle(ChatFormatting.DARK_PURPLE));
    }
}
