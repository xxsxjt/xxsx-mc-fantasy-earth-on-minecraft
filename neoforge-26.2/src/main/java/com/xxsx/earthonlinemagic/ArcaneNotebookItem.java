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

public class ArcaneNotebookItem extends Item {
    public ArcaneNotebookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            openClientHandbook();
            return InteractionResult.SUCCESS;
        }
        if (!level.isClientSide()) {
            sendGuide(player);
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
        lines.accept(Component.translatable("tooltip.earth_online_magic.notebook.use").withStyle(ChatFormatting.GRAY));
        lines.accept(Component.translatable("tooltip.earth_online_magic.notebook.scope").withStyle(ChatFormatting.DARK_PURPLE));
    }

    public static void sendGuide(Player player) {
        AetherChunkField.Reading reading = AetherChunkField.read(player.level(), player.blockPosition());
        player.sendSystemMessage(Component.translatable("guide.earth_online_magic.line0").withStyle(ChatFormatting.LIGHT_PURPLE));
        player.sendSystemMessage(Component.translatable("guide.earth_online_magic.line1").withStyle(ChatFormatting.WHITE));
        player.sendSystemMessage(Component.translatable("guide.earth_online_magic.line2").withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.translatable("guide.earth_online_magic.line3").withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.translatable("guide.earth_online_magic.line4").withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(Component.translatable("guide.earth_online_magic.line5").withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.translatable("guide.earth_online_magic.line6").withStyle(ChatFormatting.GREEN));
        player.sendSystemMessage(Component.translatable("guide.earth_online_magic.line7").withStyle(ChatFormatting.DARK_GRAY));
        player.sendSystemMessage(Component.translatable("message.earth_online_magic.arcana.status",
                ArcanaPower.format(ArcanaPower.getCurrentMana(player)),
                ArcanaPower.format(ArcanaPower.getMaxMana(player)),
                ArcanaPower.format(ArcanaPower.getXuanhuanBonus(player)),
                ArcanaPower.format(ArcanaPower.getMagicBonus(player))).withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.translatable("message.earth_online_magic.notebook.field",
                AetherChunkField.gradeName(reading.value()),
                reading.value(),
                reading.mainSource(),
                ArcanaPower.format(reading.disturbance())).withStyle(ChatFormatting.DARK_PURPLE));
    }

    private static void openClientHandbook() {
        try {
            Class.forName("com.xxsx.earthonlinemagic.client.EarthOnlineMagicClient")
                    .getMethod("openHandbook")
                    .invoke(null);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
