package com.xxsx.earthonlinemagic.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public final class RuneWolfEntity extends ContractableFamiliarEntity {
    public RuneWolfEntity(EntityType<? extends RuneWolfEntity> type, Level level) {
        super(type, level, Kind.RUNE_WOLF);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return ContractableFamiliarEntity.createAttributes(26.0D, 0.33D, 6.0D, 5.0D, 0.25D);
    }
}
