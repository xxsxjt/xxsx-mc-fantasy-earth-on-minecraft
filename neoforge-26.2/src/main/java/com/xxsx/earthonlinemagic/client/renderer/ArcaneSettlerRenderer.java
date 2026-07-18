package com.xxsx.earthonlinemagic.client.renderer;

import com.xxsx.earthonlinemagic.EarthOnlineMagic;
import com.xxsx.earthonlinemagic.client.model.ArcaneSettlerModel;
import com.xxsx.earthonlinemagic.entity.ArcaneSettlerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public final class ArcaneSettlerRenderer extends MobRenderer<ArcaneSettlerEntity,
        ArcaneSettlerRenderState, ArcaneSettlerModel> {
    private static final Identifier[] TEXTURES = {
            EarthOnlineMagic.id("textures/entity/arcane_settler_witch.png"),
            EarthOnlineMagic.id("textures/entity/arcane_settler_goblin.png"),
            EarthOnlineMagic.id("textures/entity/arcane_settler_researcher.png")
    };

    public ArcaneSettlerRenderer(EntityRendererProvider.Context context) {
        super(context, new ArcaneSettlerModel(context.bakeLayer(ArcaneSettlerModel.LAYER_LOCATION)), 0.48F);
    }

    @Override
    public ArcaneSettlerRenderState createRenderState() {
        return new ArcaneSettlerRenderState();
    }

    @Override
    public void extractRenderState(ArcaneSettlerEntity entity, ArcaneSettlerRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.role = entity.getRole().id();
        state.trading = entity.isTrading();
    }

    @Override
    public Identifier getTextureLocation(ArcaneSettlerRenderState state) {
        return TEXTURES[Math.max(0, Math.min(TEXTURES.length - 1, state.role))];
    }
}
