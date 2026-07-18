package com.xxsx.earthonlinemagic.settlement;

import com.mojang.serialization.Codec;
import com.xxsx.earthonlinemagic.EarthOnlineMagic;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class MagicSettlementFeature extends Feature<NoneFeatureConfiguration> {
    private static final int UPDATE_FLAGS = 2;
    private final Type type;

    public MagicSettlementFeature(Codec<NoneFeatureConfiguration> codec, Type type) {
        super(codec);
        this.type = type;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos center = context.origin();
        while (level.isEmptyBlock(center) && center.getY() > level.getMinY() + 2) {
            center = center.below();
        }
        center = center.above();
        if (level.isEmptyBlock(center.below())) {
            return false;
        }
        int radius = type == Type.ACADEMY_OUTPOST ? 5 : 4;
        prepareSite(level, center, radius);
        switch (type) {
            case WITCH_HAMLET -> buildWitchHamlet(level, center);
            case GOBLIN_EXCHANGE -> buildGoblinExchange(level, center);
            case ACADEMY_OUTPOST -> buildAcademyOutpost(level, center);
        }
        level.setBlock(center, EarthOnlineMagic.SETTLEMENT_ANCHOR.get().defaultBlockState(), UPDATE_FLAGS);
        if (level.getBlockEntity(center) instanceof MagicSettlementAnchorBlockEntity anchor) {
            anchor.configure(type.id());
        }
        return true;
    }

    private static void prepareSite(WorldGenLevel level, BlockPos center, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                level.setBlock(center.offset(x, -1, z), Blocks.STONE_BRICKS.defaultBlockState(), UPDATE_FLAGS);
                for (int y = 0; y <= 6; y++) {
                    level.setBlock(center.offset(x, y, z), Blocks.AIR.defaultBlockState(), UPDATE_FLAGS);
                }
            }
        }
    }

    private static void buildWitchHamlet(WorldGenLevel level, BlockPos center) {
        fillFloor(level, center, 4, Blocks.MOSS_BLOCK.defaultBlockState());
        buildHut(level, center.offset(-3, 0, 2), Blocks.DARK_OAK_PLANKS.defaultBlockState(), Blocks.MANGROVE_SLAB.defaultBlockState());
        buildHut(level, center.offset(3, 0, 2), Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.DARK_OAK_SLAB.defaultBlockState());
        level.setBlock(center.offset(-2, 0, -2), Blocks.CAULDRON.defaultBlockState(), UPDATE_FLAGS);
        level.setBlock(center.offset(2, 0, -2), Blocks.BREWING_STAND.defaultBlockState(), UPDATE_FLAGS);
        for (int x = -3; x <= 3; x += 2) {
            level.setBlock(center.offset(x, 0, -3), Blocks.RED_MUSHROOM.defaultBlockState(), UPDATE_FLAGS);
        }
        cornerLights(level, center, 4, Blocks.SOUL_LANTERN.defaultBlockState());
    }

    private static void buildGoblinExchange(WorldGenLevel level, BlockPos center) {
        fillFloor(level, center, 4, Blocks.MUD_BRICKS.defaultBlockState());
        for (int x = -3; x <= 3; x += 3) {
            level.setBlock(center.offset(x, 0, 2), Blocks.BARREL.defaultBlockState(), UPDATE_FLAGS);
            level.setBlock(center.offset(x, 0, -2), Blocks.COPPER_BLOCK.weathering().unaffected().defaultBlockState(), UPDATE_FLAGS);
        }
        line(level, center.offset(-4, 0, 4), 9, true, Blocks.RAIL.defaultBlockState());
        buildCanopy(level, center.offset(-2, 0, 0), Blocks.WOOL.orange().defaultBlockState());
        buildCanopy(level, center.offset(2, 0, 0), Blocks.WOOL.gray().defaultBlockState());
        cornerLights(level, center, 4, Blocks.LANTERN.defaultBlockState());
    }

    private static void buildAcademyOutpost(WorldGenLevel level, BlockPos center) {
        fillFloor(level, center, 5, Blocks.SMOOTH_STONE.defaultBlockState());
        for (int x : new int[]{-4, 4}) {
            for (int z : new int[]{-4, 4}) {
                column(level, center.offset(x, 0, z), 5, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
                level.setBlock(center.offset(x, 5, z), Blocks.AMETHYST_CLUSTER.defaultBlockState(), UPDATE_FLAGS);
            }
        }
        for (int z = -3; z <= 3; z++) {
            level.setBlock(center.offset(-3, 0, z), Blocks.BOOKSHELF.defaultBlockState(), UPDATE_FLAGS);
        }
        buildCanopy(level, center.offset(3, 0, 1), Blocks.WOOL.blue().defaultBlockState());
        level.setBlock(center.offset(0, 0, -3), Blocks.ENCHANTING_TABLE.defaultBlockState(), UPDATE_FLAGS);
        level.setBlock(center.offset(0, 0, 3), Blocks.LECTERN.defaultBlockState(), UPDATE_FLAGS);
    }

    private static void fillFloor(WorldGenLevel level, BlockPos center, int radius, BlockState state) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                level.setBlock(center.offset(x, -1, z), state, UPDATE_FLAGS);
            }
        }
    }

    private static void buildHut(WorldGenLevel level, BlockPos center, BlockState wall, BlockState roof) {
        for (int x : new int[]{-1, 1}) {
            for (int z : new int[]{-1, 1}) {
                column(level, center.offset(x, 0, z), 3, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            }
        }
        line(level, center.offset(-1, 0, 1), 3, true, wall);
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                level.setBlock(center.offset(x, 3, z), roof, UPDATE_FLAGS);
            }
        }
    }

    private static void buildCanopy(WorldGenLevel level, BlockPos center, BlockState cloth) {
        for (int x : new int[]{-1, 1}) {
            for (int z : new int[]{-1, 1}) {
                column(level, center.offset(x, 0, z), 3, Blocks.COPPER_GRATE.weathering().unaffected().defaultBlockState());
            }
        }
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                level.setBlock(center.offset(x, 3, z), cloth, UPDATE_FLAGS);
            }
        }
    }

    private static void cornerLights(WorldGenLevel level, BlockPos center, int radius, BlockState light) {
        for (int x : new int[]{-radius, radius}) {
            for (int z : new int[]{-radius, radius}) {
                column(level, center.offset(x, 0, z), 2, Blocks.DARK_OAK_FENCE.defaultBlockState());
                level.setBlock(center.offset(x, 2, z), light, UPDATE_FLAGS);
            }
        }
    }

    private static void column(WorldGenLevel level, BlockPos start, int height, BlockState state) {
        for (int y = 0; y < height; y++) {
            level.setBlock(start.above(y), state, UPDATE_FLAGS);
        }
    }

    private static void line(WorldGenLevel level, BlockPos start, int length, boolean alongX, BlockState state) {
        for (int i = 0; i < length; i++) {
            level.setBlock(start.offset(alongX ? i : 0, 0, alongX ? 0 : i), state, UPDATE_FLAGS);
        }
    }

    public enum Type {
        WITCH_HAMLET("witch_hamlet"),
        GOBLIN_EXCHANGE("goblin_exchange"),
        ACADEMY_OUTPOST("academy_outpost");

        private final String id;

        Type(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }

        public static Type byId(String id) {
            for (Type type : values()) {
                if (type.id.equals(id)) {
                    return type;
                }
            }
            return WITCH_HAMLET;
        }
    }
}
