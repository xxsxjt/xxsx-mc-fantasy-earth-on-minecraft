package com.xxsx.earthonlinemagic.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public final class ManaWispEntity extends ContractableFamiliarEntity {
    public ManaWispEntity(EntityType<? extends ManaWispEntity> type, Level level) {
        super(type, level, Kind.MANA_WISP);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return ContractableFamiliarEntity.createAttributes(12.0D, 0.31D, 2.0D, 0.0D, 0.0D);
    }
}
