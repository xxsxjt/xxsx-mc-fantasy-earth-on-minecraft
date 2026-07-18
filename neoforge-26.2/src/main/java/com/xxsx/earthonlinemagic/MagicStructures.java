package com.xxsx.earthonlinemagic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public final class MagicStructures {
    private MagicStructures() {
    }

    public static int tierFor(MagicMachineBlock.Kind kind, Level level, BlockPos pos) {
        if (kind == MagicMachineBlock.Kind.RITUAL_PEDESTAL && isFormalRitualCircle(level, pos)) {
            return 1;
        }
        return 0;
    }

    public static boolean isFormalRitualCircle(Level level, BlockPos center) {
        if (!is(level, center, EarthOnlineMagic.RITUAL_PEDESTAL.get())) {
            return false;
        }
        return isAetherFocus(level, center.north().east())
                && isAetherFocus(level, center.north().west())
                && isAetherFocus(level, center.south().east())
                && isAetherFocus(level, center.south().west())
                && isRitualMark(level, center.north())
                && isRitualMark(level, center.south())
                && isRitualMark(level, center.east())
                && isRitualMark(level, center.west());
    }

    public static Optional<BlockPos> findFormalRitualCenter(Level level, BlockPos clicked) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos center = clicked.offset(dx, 0, dz);
                if (isFormalRitualCircle(level, center)) {
                    return Optional.of(center);
                }
            }
        }
        return Optional.empty();
    }

    private static boolean isAetherFocus(Level level, BlockPos pos) {
        Block block = level.getBlockState(pos).getBlock();
        return block == EarthOnlineMagic.AETHER_CRYSTAL_CLUSTER.get()
                || block == Blocks.AMETHYST_CLUSTER;
    }

    private static boolean isRitualMark(Level level, BlockPos pos) {
        Block block = level.getBlockState(pos).getBlock();
        return block == EarthOnlineMagic.RUNE_CARVING_TABLE.get()
                || block == EarthOnlineMagic.ALCHEMY_TABLE.get()
                || block == Blocks.BOOKSHELF
                || block == Blocks.GLOWSTONE
                || block == Blocks.REDSTONE_BLOCK;
    }

    private static boolean is(Level level, BlockPos pos, Block block) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() == block;
    }
}
