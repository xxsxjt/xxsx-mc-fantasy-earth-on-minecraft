package com.xxsx.earthonlinemagic.client.model;

import com.xxsx.earthonlinemagic.EarthOnlineMagic;
import com.xxsx.earthonlinemagic.client.renderer.FamiliarRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;

public final class ManaWispModel extends EntityModel<FamiliarRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(EarthOnlineMagic.id("mana_wisp"), "main");
    private final ModelPart core;
    private final ModelPart ring;
    private final ModelPart leftFin;
    private final ModelPart rightFin;
    private final ModelPart flame;

    public ManaWispModel(ModelPart root) {
        super(root, RenderTypes::entityTranslucent);
        core = root.getChild("core");
        ring = root.getChild("ring");
        leftFin = root.getChild("left_fin");
        rightFin = root.getChild("right_fin");
        flame = root.getChild("flame");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("core", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F)
                        .texOffs(24, 0).addBox(-3.5F, -3.5F, -3.5F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.12F)),
                PartPose.offset(0.0F, 13.0F, 0.0F));
        root.addOrReplaceChild("ring", CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-5.0F, -1.0F, -5.0F, 10.0F, 2.0F, 10.0F, new CubeDeformation(-0.15F)),
                PartPose.offset(0.0F, 16.0F, 0.0F));
        root.addOrReplaceChild("left_fin", CubeListBuilder.create().texOffs(0, 30)
                        .addBox(0.0F, -4.0F, -1.0F, 7.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(2.5F, 13.0F, 0.0F, 0.0F, 0.22F, -0.16F));
        root.addOrReplaceChild("right_fin", CubeListBuilder.create().texOffs(16, 30)
                        .addBox(-7.0F, -4.0F, -1.0F, 7.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(-2.5F, 13.0F, 0.0F, 0.0F, -0.22F, 0.16F));
        root.addOrReplaceChild("flame", CubeListBuilder.create().texOffs(34, 30)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 8.0F, 4.0F),
                PartPose.offset(0.0F, 16.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(FamiliarRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        core.y = 13.0F + Mth.sin(age * 0.12F) * 0.8F;
        ring.y = 16.0F + Mth.sin(age * 0.12F) * 0.5F;
        ring.yRot = age * 0.045F;
        leftFin.zRot = -0.16F - Mth.sin(age * 0.18F) * 0.22F;
        rightFin.zRot = -leftFin.zRot;
        flame.yScale = 1.0F + Mth.sin(age * 0.20F) * (0.08F + state.stability * 0.06F);
        float pulse = 1.0F + Mth.sin(age * 0.24F) * 0.06F;
        core.xScale = pulse;
        core.zScale = pulse;
    }
}
