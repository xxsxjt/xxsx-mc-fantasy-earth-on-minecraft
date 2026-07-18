package com.xxsx.earthonlinemagic.settlement;

import com.xxsx.earthonlinemagic.EarthOnlineMagic;
import com.xxsx.earthonlinemagic.entity.ArcaneSettlerEntity;
import com.xxsx.earthonlinemagic.entity.ContractableFamiliarEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class MagicSettlementAnchorBlockEntity extends BlockEntity {
    private String settlementType = MagicSettlementFeature.Type.WITCH_HAMLET.id();
    private boolean initialized;
    private int warmupTicks;

    public MagicSettlementAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(EarthOnlineMagic.SETTLEMENT_ANCHOR_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MagicSettlementAnchorBlockEntity anchor) {
        if (anchor.initialized || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (++anchor.warmupTicks < 40) {
            return;
        }
        anchor.spawnSettlementResidents(serverLevel, pos);
        anchor.initialized = true;
        anchor.setChanged();
    }

    private void spawnSettlementResidents(ServerLevel level, BlockPos pos) {
        switch (MagicSettlementFeature.Type.byId(settlementType)) {
            case WITCH_HAMLET -> {
                spawnResident(level, pos.offset(-3, 1, 0), ArcaneSettlerEntity.Role.HEDGE_WITCH);
                spawnResident(level, pos.offset(3, 1, 0), ArcaneSettlerEntity.Role.HEDGE_WITCH);
                spawnResident(level, pos.offset(0, 1, -2), ArcaneSettlerEntity.Role.GOBLIN_APPRAISER);
                spawnFamiliar(level, pos.offset(3, 1, 3), EarthOnlineMagic.MANA_WISP.get());
            }
            case GOBLIN_EXCHANGE -> {
                spawnResident(level, pos.offset(-2, 1, 0), ArcaneSettlerEntity.Role.GOBLIN_APPRAISER);
                spawnResident(level, pos.offset(2, 1, 0), ArcaneSettlerEntity.Role.GOBLIN_APPRAISER);
                spawnResident(level, pos.offset(0, 1, 3), ArcaneSettlerEntity.Role.HEDGE_WITCH);
            }
            case ACADEMY_OUTPOST -> {
                spawnResident(level, pos.offset(-2, 1, 0), ArcaneSettlerEntity.Role.ACADEMY_RESEARCHER);
                spawnResident(level, pos.offset(2, 1, 0), ArcaneSettlerEntity.Role.ACADEMY_RESEARCHER);
                spawnResident(level, pos.offset(0, 1, 3), ArcaneSettlerEntity.Role.GOBLIN_APPRAISER);
                spawnFamiliar(level, pos.offset(-3, 1, 3), EarthOnlineMagic.AETHER_FOX.get());
            }
        }
        EarthOnlineMagic.LOGGER.info("Initialized magic settlement {} at {}", settlementType, pos);
    }

    private static void spawnResident(ServerLevel level, BlockPos pos, ArcaneSettlerEntity.Role role) {
        ArcaneSettlerEntity resident = EarthOnlineMagic.ARCANE_SETTLER.get().create(level, EntitySpawnReason.STRUCTURE);
        if (resident == null) {
            return;
        }
        BlockPos spawnPos = findOpenSpawn(level, pos);
        resident.setRole(role);
        resident.snapTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
                level.getRandom().nextFloat() * 360.0F, 0.0F);
        resident.setPersistenceRequired();
        level.addFreshEntity(resident);
    }

    private static void spawnFamiliar(ServerLevel level, BlockPos pos,
                                      EntityType<? extends ContractableFamiliarEntity> type) {
        ContractableFamiliarEntity familiar = type.create(level, EntitySpawnReason.STRUCTURE);
        if (familiar == null) {
            return;
        }
        BlockPos spawnPos = findOpenSpawn(level, pos);
        familiar.snapTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
                level.getRandom().nextFloat() * 360.0F, 0.0F);
        familiar.setPersistenceRequired();
        level.addFreshEntity(familiar);
    }

    private static BlockPos findOpenSpawn(ServerLevel level, BlockPos preferred) {
        int[] verticalOffsets = {-1, 0, 1, -2};
        for (int verticalOffset : verticalOffsets) {
            for (int radius = 0; radius <= 3; radius++) {
                for (int x = -radius; x <= radius; x++) {
                    for (int z = -radius; z <= radius; z++) {
                        if (Math.max(Math.abs(x), Math.abs(z)) != radius) {
                            continue;
                        }
                        BlockPos candidate = preferred.offset(x, verticalOffset, z);
                        if (!level.isEmptyBlock(candidate.below())
                                && level.isEmptyBlock(candidate)
                                && level.isEmptyBlock(candidate.above())) {
                            return candidate;
                        }
                    }
                }
            }
        }
        return preferred;
    }

    public void configure(String type) {
        settlementType = MagicSettlementFeature.Type.byId(type).id();
        initialized = false;
        warmupTicks = 0;
        setChanged();
    }

    public String settlementType() {
        return settlementType;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("SettlementType", settlementType);
        output.putBoolean("Initialized", initialized);
        output.putInt("WarmupTicks", warmupTicks);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        settlementType = MagicSettlementFeature.Type.byId(
                input.getStringOr("SettlementType", MagicSettlementFeature.Type.WITCH_HAMLET.id())).id();
        initialized = input.getBooleanOr("Initialized", false);
        warmupTicks = input.getIntOr("WarmupTicks", 0);
    }
}
