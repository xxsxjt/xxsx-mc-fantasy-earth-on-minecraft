package com.xxsx.earthonlinemagic.client.model;

import com.xxsx.earthonlinemagic.EarthOnlineMagic;
import com.xxsx.earthonlinemagic.client.renderer.CrystalSpiderRenderState;
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

public final class CrystalSpiderModel extends EntityModel<CrystalSpiderRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            EarthOnlineMagic.id("crystal_armored_spider"), "main");
    private final ModelPart head;
    private final ModelPart abdomen;
    private final ModelPart crystals;
    private final ModelPart[] legs = new ModelPart[8];
    private final ModelPart[] lowerLegs = new ModelPart[8];

    public CrystalSpiderModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
        head = root.getChild("head");
        abdomen = root.getChild("abdomen");
        crystals = abdomen.getChild("crystals");
        for (int i = 0; i < legs.length; i++) {
            legs[i] = root.getChild("leg_" + i);
            lowerLegs[i] = legs[i].getChild("lower");
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -3.0F, -5.0F, 8.0F, 6.0F, 6.0F)
                        .texOffs(28, 0).addBox(-3.0F, -1.0F, -7.0F, 6.0F, 3.0F, 3.0F),
                PartPose.offset(0.0F, 16.0F, -4.0F));
        PartDefinition abdomen = root.addOrReplaceChild("abdomen", CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-6.0F, -4.0F, -4.0F, 12.0F, 8.0F, 11.0F)
                        .texOffs(48, 16).addBox(-6.5F, -4.5F, -3.5F, 13.0F, 5.0F, 10.0F, new CubeDeformation(0.22F)),
                PartPose.offset(0.0F, 15.0F, 2.0F));
        PartDefinition crystals = abdomen.addOrReplaceChild("crystals", CubeListBuilder.create(),
                PartPose.offset(0.0F, -3.0F, 1.0F));
        crystals.addOrReplaceChild("main", CubeListBuilder.create()
                        .texOffs(0, 40).addBox(-1.5F, -7.0F, -1.5F, 3.0F, 7.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.10F, 0.0F, 0.06F));
        crystals.addOrReplaceChild("left", CubeListBuilder.create()
                        .texOffs(14, 40).addBox(-1.0F, -5.0F, -1.0F, 2.0F, 5.0F, 2.0F),
                PartPose.offsetAndRotation(-3.0F, 0.4F, 1.2F, -0.15F, 0.0F, -0.42F));
        crystals.addOrReplaceChild("rear", CubeListBuilder.create()
                        .texOffs(26, 40).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(3.1F, 0.7F, 2.6F, 0.20F, 0.0F, 0.38F));
        for (int i = 0; i < 8; i++) {
            boolean left = i >= 4;
            int row = i % 4;
            float z = -3.0F + row * 2.2F;
            float yaw = (0.85F - row * 0.18F) * (left ? -1.0F : 1.0F);
            float roll = left ? 0.42F : -0.42F;
            PartDefinition leg = root.addOrReplaceChild("leg_" + i,
                    CubeListBuilder.create().texOffs(row * 20, 52)
                            .addBox(left ? 0.0F : -6.0F, -1.0F, -1.0F, 6.0F, 2.0F, 2.0F),
                    PartPose.offsetAndRotation(left ? 4.0F : -4.0F, 17.0F, z, 0.0F, yaw, roll));
            leg.addOrReplaceChild("lower", CubeListBuilder.create().texOffs(row * 20, 60)
                            .addBox(left ? 0.0F : -7.0F, -1.0F, -1.0F, 7.0F, 2.0F, 2.0F),
                    PartPose.offsetAndRotation(left ? 5.7F : -5.7F, 0.0F, 0.0F,
                            0.0F, 0.0F, left ? 0.52F : -0.52F));
        }
        return LayerDefinition.create(mesh, 96, 80);
    }

    @Override
    public void setupAnim(CrystalSpiderRenderState state) {
        super.setupAnim(state);
        head.yRot = state.yRot * Mth.DEG_TO_RAD * 0.45F;
        head.xRot = state.xRot * Mth.DEG_TO_RAD * 0.35F;
        float walk = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0F);
        for (int i = 0; i < legs.length; i++) {
            boolean left = i >= 4;
            int row = i % 4;
            float phase = walk * 0.75F + row * 0.7F + (left ? Mth.PI : 0.0F);
            float baseYaw = (0.85F - row * 0.18F) * (left ? -1.0F : 1.0F);
            legs[i].zRot = (left ? 0.42F : -0.42F) + Mth.cos(phase) * 0.24F * speed;
            legs[i].yRot = baseYaw + Mth.sin(phase) * 0.16F * speed;
            lowerLegs[i].zRot = (left ? 0.52F : -0.52F) + Mth.sin(phase + 0.45F) * 0.11F * speed;
        }
        float pulse = 1.0F + Mth.sin(state.ageInTicks * 0.18F) * 0.05F;
        crystals.xScale = pulse;
        crystals.yScale = pulse;
        crystals.zScale = pulse;
        abdomen.y = 15.0F + Mth.sin(state.ageInTicks * 0.07F) * 0.04F;
        float attack = Mth.sin(state.attackProgress * Mth.PI);
        head.z -= attack * 1.6F;
        head.xRot -= attack * 0.30F;
        abdomen.z += attack * 0.8F;
        for (int i = 0; i < legs.length; i++) {
            legs[i].zRot += (i >= 4 ? 1.0F : -1.0F) * attack * 0.16F;
        }
    }
}
