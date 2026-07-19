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
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
        lines.accept(Component.translatable("tooltip.earth_online_magic.notebook.use").withStyle(ChatFormatting.GRAY));
        lines.accept(Component.translatable("tooltip.earth_online_magic.notebook.scope").withStyle(ChatFormatting.DARK_PURPLE));
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
