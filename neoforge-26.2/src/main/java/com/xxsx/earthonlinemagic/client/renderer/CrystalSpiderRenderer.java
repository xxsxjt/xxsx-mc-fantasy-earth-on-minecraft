package com.xxsx.earthonlinemagic.client.renderer;

import com.xxsx.earthonlinemagic.EarthOnlineMagic;
import com.xxsx.earthonlinemagic.client.model.CrystalSpiderModel;
import com.xxsx.earthonlinemagic.entity.CrystalArmoredSpiderEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public final class CrystalSpiderRenderer extends MobRenderer<CrystalArmoredSpiderEntity,
        CrystalSpiderRenderState, CrystalSpiderModel> {
    private static final Identifier TEXTURE = EarthOnlineMagic.id("textures/entity/crystal_armored_spider.png");

    public CrystalSpiderRenderer(EntityRendererProvider.Context context) {
        super(context, new CrystalSpiderModel(context.bakeLayer(CrystalSpiderModel.LAYER_LOCATION)), 0.75F);
    }

    @Override
    public CrystalSpiderRenderState createRenderState() {
        return new CrystalSpiderRenderState();
    }

    @Override
    public void extractRenderState(CrystalArmoredSpiderEntity entity, CrystalSpiderRenderState state,
                                   float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.attackProgress = entity.getAttackAnim(partialTick);
    }

    @Override
    public Identifier getTextureLocation(CrystalSpiderRenderState state) {
        return TEXTURE;
    }
}
