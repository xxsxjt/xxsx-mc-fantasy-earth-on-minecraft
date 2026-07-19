package com.xxsx.earthonlinemagic.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.level.Level;

public final class ManaWispEntity extends ContractableFamiliarEntity {
    public ManaWispEntity(EntityType<? extends ManaWispEntity> type, Level level) {
        super(type, level, Kind.MANA_WISP);
        this.moveControl = new FlyingMoveControl<>(this, 12, true);
        setNoGravity(true);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FlyingPathNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.removeAllGoals(goal -> goal instanceof WaterAvoidingRandomStrollGoal);
        this.goalSelector.addGoal(7, new WaterAvoidingRandomFlyingGoal(this, 0.92D));
    }

    @Override
    public void tick() {
        super.tick();
        setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return ContractableFamiliarEntity.createAttributes(12.0D, 0.31D, 2.0D, 0.0D, 0.0D)
                .add(Attributes.FLYING_SPEED, 0.31D);
    }
}
