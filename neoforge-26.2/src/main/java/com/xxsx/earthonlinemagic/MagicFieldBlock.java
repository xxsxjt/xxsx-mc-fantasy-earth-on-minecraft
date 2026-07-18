package com.xxsx.earthonlinemagic;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class MagicFieldBlock extends Block {
    public MagicFieldBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return describe(level, pos, player);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return describe(level, pos, player);
    }

    private InteractionResult describe(Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        var ritualCenter = MagicStructures.findFormalRitualCenter(level, pos);
        if (ritualCenter.isPresent()) {
            return MagicMachineBlock.openMachineAt(level, ritualCenter.get(), player);
        }
        AetherChunkField.Reading reading = AetherChunkField.read(level, pos);
        player.sendSystemMessage(Component.translatable("message.earth_online_magic.block.field",
                AetherChunkField.gradeName(reading.value()),
                reading.value(),
                reading.mainSource(),
                ArcanaPower.format(reading.disturbance())).withStyle(ChatFormatting.LIGHT_PURPLE));
        return InteractionResult.SUCCESS_SERVER;
    }
}
