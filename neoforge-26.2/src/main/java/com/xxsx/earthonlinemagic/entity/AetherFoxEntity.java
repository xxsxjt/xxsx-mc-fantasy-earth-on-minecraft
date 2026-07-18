package com.xxsx.earthonlinemagic.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public final class AetherFoxEntity extends ContractableFamiliarEntity {
    public AetherFoxEntity(EntityType<? extends AetherFoxEntity> type, Level level) {
        super(type, level, Kind.AETHER_FOX);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return ContractableFamiliarEntity.createAttributes(16.0D, 0.35D, 3.0D, 1.0D, 0.05D);
    }
}
