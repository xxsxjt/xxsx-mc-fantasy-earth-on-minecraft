package com.xxsx.earthonlinemagic.client;

import com.xxsx.earthonlinemagic.ArcaneSeatEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class InvisibleSeatRenderer extends EntityRenderer<ArcaneSeatEntity, EntityRenderState> {
    public InvisibleSeatRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
        this.shadowStrength = 0.0F;
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}
