package com.xxsx.earthonlinemagic.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xxsx.earthonlinemagic.EarthOnlineMagic;
import com.xxsx.earthonlinemagic.client.model.RunicWatcherModel;
import com.xxsx.earthonlinemagic.entity.RunicWatcherEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public final class RunicWatcherRenderer extends MobRenderer<RunicWatcherEntity, RunicWatcherRenderState, RunicWatcherModel> {
    private static final Identifier TEXTURE = EarthOnlineMagic.id("textures/entity/runic_watcher.png");

    public RunicWatcherRenderer(EntityRendererProvider.Context context) {
        super(context, new RunicWatcherModel(context.bakeLayer(RunicWatcherModel.LAYER_LOCATION)), 0.52F);
    }

    @Override
    public RunicWatcherRenderState createRenderState() {
        return new RunicWatcherRenderState();
    }

    @Override
    public void extractRenderState(RunicWatcherEntity entity, RunicWatcherRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.aggressive = entity.getTarget() != null;
        state.integrity = entity.getHealth() / entity.getMaxHealth();
        state.attackProgress = entity.getAttackAnim(partialTick);
    }

    @Override
    public Identifier getTextureLocation(RunicWatcherRenderState state) {
        return TEXTURE;
    }

    @Override
    protected void scale(RunicWatcherRenderState state, PoseStack poseStack) {
        poseStack.scale(1.04F, 1.04F, 1.04F);
    }
}
