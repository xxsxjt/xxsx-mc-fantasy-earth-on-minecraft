package com.xxsx.earthonlinemagic.client.renderer;

import com.xxsx.earthonlinemagic.EarthOnlineMagic;
import com.xxsx.earthonlinemagic.client.model.CrystalSpiderModel;
import com.xxsx.earthonlinemagic.entity.CrystalArmoredSpiderEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public final class CrystalSpiderRenderer extends MobRenderer<CrystalArmoredSpiderEntity,
        LivingEntityRenderState, CrystalSpiderModel> {
    private static final Identifier TEXTURE = EarthOnlineMagic.id("textures/entity/crystal_armored_spider.png");

    public CrystalSpiderRenderer(EntityRendererProvider.Context context) {
        super(context, new CrystalSpiderModel(context.bakeLayer(CrystalSpiderModel.LAYER_LOCATION)), 0.75F);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return TEXTURE;
    }
}
