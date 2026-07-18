package com.xxsx.earthonlinemagic.settlement;

import com.xxsx.earthonlinemagic.EarthOnlineMagic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public final class MagicSettlementAnchorBlock extends Block implements EntityBlock {
    public MagicSettlementAnchorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MagicSettlementAnchorBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                            BlockEntityType<T> type) {
        if (level.isClientSide() || type != EarthOnlineMagic.SETTLEMENT_ANCHOR_BLOCK_ENTITY.get()) {
            return null;
        }
        return (tickerLevel, pos, tickerState, blockEntity) -> MagicSettlementAnchorBlockEntity.serverTick(
                tickerLevel, pos, tickerState, (MagicSettlementAnchorBlockEntity) blockEntity);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof MagicSettlementAnchorBlockEntity anchor) {
            player.sendSystemMessage(Component.translatable("message.earth_online_magic.settlement.anchor",
                    Component.translatable("settlement.earth_online_magic." + anchor.settlementType())));
        }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.WITCH, pos.getX() + 0.5D, pos.getY() + 1.08D,
                    pos.getZ() + 0.5D, 0.0D, 0.025D, 0.0D);
        }
    }
}
