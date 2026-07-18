package com.xxsx.earthonlinemagic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AetherChunkField extends SavedData {
    private static final long REFRESH_INTERVAL_TICKS = 600L;
    private static final double MAX_DISTURBANCE = 45.0D;
    private static final double DISTURBANCE_RECOVERY_PER_TICK = 0.003D;
    private static final int SCAN_RADIUS_XZ = 8;
    private static final int SCAN_RADIUS_Y = 5;
    private static final int MAX_SOURCE_CACHE_ENTRIES = 256;

    private static final Codec<AetherChunkField> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, DisturbanceState.CODEC)
                    .optionalFieldOf("chunks", Map.<String, DisturbanceState>of())
                    .forGetter(data -> data.disturbedChunks)
    ).apply(instance, AetherChunkField::new));
    private static final SavedDataType<AetherChunkField> TYPE = new SavedDataType<>(
            EarthOnlineMagic.id("aether_chunk_field"), AetherChunkField::new, CODEC);

    private final Map<String, DisturbanceState> disturbedChunks = new HashMap<>();
    private final Map<String, CachedSources> sourceCache = new LinkedHashMap<>(64, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, CachedSources> eldest) {
            return size() > MAX_SOURCE_CACHE_ENTRIES;
        }
    };

    private AetherChunkField() {
    }

    private AetherChunkField(Map<String, DisturbanceState> states) {
        states.forEach((key, state) -> {
            double disturbance = clamp(state.disturbance(), 0.0D, MAX_DISTURBANCE);
            if (disturbance > 0.0D) {
                disturbedChunks.put(key, new DisturbanceState(disturbance, state.lastUpdate()));
            }
        });
    }

    public static Reading read(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            return data(serverLevel).readAt(serverLevel, pos);
        }
        return buildReading(level, pos, scanSources(level, pos), 0.0D);
    }

    public static void disturb(Level level, BlockPos pos, double restoredMana) {
        if (restoredMana <= 0.0D || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        data(serverLevel).disturbAt(serverLevel, pos, restoredMana);
    }

    public static Component gradeName(int value) {
        if (value >= 75) {
            return Component.translatable("aether.earth_online_magic.grade.dense");
        }
        if (value >= 50) {
            return Component.translatable("aether.earth_online_magic.grade.stable");
        }
        if (value >= 25) {
            return Component.translatable("aether.earth_online_magic.grade.faint");
        }
        return Component.translatable("aether.earth_online_magic.grade.quiet");
    }

    private static AetherChunkField data(ServerLevel level) {
        return level.getServer().getDataStorage().computeIfAbsent(TYPE);
    }

    private Reading readAt(ServerLevel level, BlockPos pos) {
        String key = chunkKey(level, pos);
        double disturbance = currentDisturbance(key, level.getGameTime());
        SourceScan sources = cachedSources(level, pos, key);
        return buildReading(level, pos, sources, disturbance);
    }

    private void disturbAt(ServerLevel level, BlockPos pos, double restoredMana) {
        String key = chunkKey(level, pos);
        long now = level.getGameTime();
        double current = currentDisturbance(key, now);
        double added = Math.max(0.5D, restoredMana * 0.35D);
        double updated = clamp(current + added, 0.0D, MAX_DISTURBANCE);
        disturbedChunks.put(key, new DisturbanceState(updated, now));
        setDirty();
    }

    private double currentDisturbance(String key, long now) {
        DisturbanceState state = disturbedChunks.get(key);
        if (state == null) {
            return 0.0D;
        }
        if (now < state.lastUpdate()) {
            disturbedChunks.put(key, new DisturbanceState(state.disturbance(), now));
            setDirty();
            return state.disturbance();
        }
        double recovered = clamp(
                state.disturbance() - (now - state.lastUpdate()) * DISTURBANCE_RECOVERY_PER_TICK,
                0.0D,
                MAX_DISTURBANCE);
        if (recovered <= 0.0D) {
            disturbedChunks.remove(key);
            setDirty();
        }
        return recovered;
    }

    private SourceScan cachedSources(ServerLevel level, BlockPos pos, String key) {
        long now = level.getGameTime();
        CachedSources cached = sourceCache.get(key);
        if (cached == null
                || now < cached.lastRefresh()
                || now - cached.lastRefresh() >= REFRESH_INTERVAL_TICKS
                || Math.abs(pos.getY() - cached.anchorY()) > SCAN_RADIUS_Y) {
            SourceScan scan = scanSources(level, pos);
            sourceCache.put(key, new CachedSources(scan, now, pos.getY()));
            return scan;
        }
        return cached.sources();
    }

    private static Reading buildReading(Level level, BlockPos pos, SourceScan scan, double disturbance) {
        int base = naturalBase(level, pos, ChunkPos.containing(pos));
        int value = clamp((int) Math.round(
                base + scan.crystal() + scan.ritual() + scan.rune() + scan.workbench() - disturbance), 0, 100);
        String sourceKey = disturbance >= 28.0D && value < 35
                ? "aether.earth_online_magic.source.disturbed"
                : scan.mainSourceKey();
        return new Reading(value, disturbance, sourceKey);
    }

    private static String chunkKey(Level level, BlockPos pos) {
        return level.dimension().identifier() + "|" + ChunkPos.containing(pos).pack();
    }

    private static int naturalBase(Level level, BlockPos pos, ChunkPos chunk) {
        String dimension = level.dimension().identifier().toString();
        int value;
        if ("minecraft:overworld".equals(dimension)) {
            value = 30;
        } else if ("minecraft:the_nether".equals(dimension)) {
            value = 22;
        } else if ("minecraft:the_end".equals(dimension)) {
            value = 34;
        } else {
            value = 24;
        }

        int y = pos.getY();
        if (y > 112) {
            value += 9;
        } else if (y > 72) {
            value += 5;
        } else if (y < -16) {
            value += 5;
        }

        long seed = level instanceof ServerLevel serverLevel ? serverLevel.getSeed() : 0L;
        long mixed = mix(seed
                ^ ((long) chunk.x() * 0xD6E8FEB86659FD93L)
                ^ ((long) chunk.z() * 0xA5A3564E27F886ABL)
                ^ dimension.hashCode());
        value += Math.floorMod(mixed, 31) - 10;
        return clamp(value, 0, 100);
    }

    private static SourceScan scanSources(Level level, BlockPos pos) {
        int crystal = 0;
        int ritual = 0;
        int rune = 0;
        int workbench = 0;

        int minX = pos.getX() - SCAN_RADIUS_XZ;
        int maxX = pos.getX() + SCAN_RADIUS_XZ;
        int minY = pos.getY() - SCAN_RADIUS_Y;
        int maxY = pos.getY() + SCAN_RADIUS_Y;
        int minZ = pos.getZ() - SCAN_RADIUS_XZ;
        int maxZ = pos.getZ() + SCAN_RADIUS_XZ;

        for (BlockPos sample : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
            Block block = level.getBlockState(sample).getBlock();
            if (block == EarthOnlineMagic.AETHER_CRYSTAL_CLUSTER.get() || block == Blocks.AMETHYST_CLUSTER) {
                crystal = Math.min(36, crystal + 18);
            } else if (block == EarthOnlineMagic.RITUAL_PEDESTAL.get()) {
                ritual = Math.min(36, ritual + (MagicStructures.isFormalRitualCircle(level, sample) ? 30 : 12));
            } else if (block == Blocks.ENCHANTING_TABLE) {
                ritual = Math.min(30, ritual + 15);
            } else if (block == EarthOnlineMagic.ARCANE_FOCUS_MAT.get()) {
                rune = Math.min(28, rune + 8);
            } else if (block == EarthOnlineMagic.RUNE_CARVING_TABLE.get() || block == Blocks.BOOKSHELF) {
                rune = Math.min(22, rune + 5);
            } else if (block == EarthOnlineMagic.ALCHEMY_TABLE.get()
                    || block == Blocks.GLOWSTONE
                    || block == Blocks.REDSTONE_BLOCK) {
                workbench = Math.min(18, workbench + 4);
            }
        }

        String sourceKey = "aether.earth_online_magic.source.natural";
        int strongest = 0;
        if (crystal > strongest) {
            strongest = crystal;
            sourceKey = "aether.earth_online_magic.source.crystal";
        }
        if (ritual > strongest) {
            strongest = ritual;
            sourceKey = "aether.earth_online_magic.source.ritual";
        }
        if (rune > strongest) {
            strongest = rune;
            sourceKey = "aether.earth_online_magic.source.rune";
        }
        if (workbench > strongest) {
            sourceKey = "aether.earth_online_magic.source.workbench";
        }
        return new SourceScan(crystal, ritual, rune, workbench, sourceKey);
    }

    private static long mix(long value) {
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdL;
        value ^= value >>> 33;
        value *= 0xc4ceb9fe1a85ec53L;
        value ^= value >>> 33;
        return value;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public record Reading(int value, double disturbance, String mainSourceKey) {
        public Component mainSource() {
            return Component.translatable(mainSourceKey);
        }
    }

    private record SourceScan(int crystal, int ritual, int rune, int workbench, String mainSourceKey) {
    }

    private record CachedSources(SourceScan sources, long lastRefresh, int anchorY) {
    }

    private record DisturbanceState(double disturbance, long lastUpdate) {
        private static final Codec<DisturbanceState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("disturbance").forGetter(DisturbanceState::disturbance),
                Codec.LONG.fieldOf("last_update").forGetter(DisturbanceState::lastUpdate)
        ).apply(instance, DisturbanceState::new));
    }
}
