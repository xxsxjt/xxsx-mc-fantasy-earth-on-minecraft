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

public final class RuneWolfModel extends EntityModel<FamiliarRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(EarthOnlineMagic.id("rune_wolf"), "main");
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart armor;
    private final ModelPart lens;
    private final ModelPart[] legs;
    private final ModelPart tail;

    public RuneWolfModel(ModelPart root) {
        super(root, RenderTypes::entityTranslucent);
        head = root.getChild("head");
        body = root.getChild("body");
        armor = root.getChild("armor");
        lens = root.getChild("lens");
        legs = new ModelPart[]{root.getChild("front_right_leg"), root.getChild("front_left_leg"),
                root.getChild("back_right_leg"), root.getChild("back_left_leg")};
        tail = root.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 26)
                        .addBox(-4.5F, -5.0F, -7.0F, 9.0F, 9.0F, 14.0F),
                PartPose.offset(0.0F, 15.0F, 0.0F));
        root.addOrReplaceChild("armor", CubeListBuilder.create()
                        .texOffs(50, 24).addBox(-5.0F, -5.8F, -6.0F, 10.0F, 4.0F, 12.0F, new CubeDeformation(0.18F))
                        .texOffs(54, 42).addBox(-4.8F, -2.0F, -7.0F, 9.6F, 4.0F, 4.0F, new CubeDeformation(0.15F)),
                PartPose.offset(0.0F, 15.0F, 0.0F));
        root.addOrReplaceChild("lens", CubeListBuilder.create().texOffs(82, 0)
                        .addBox(-2.0F, -2.0F, -1.0F, 4.0F, 4.0F, 1.0F),
                PartPose.offset(0.0F, 15.0F, -7.6F));
        root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -4.0F, -5.0F, 8.0F, 7.0F, 7.0F)
                        .texOffs(30, 0).addBox(-2.0F, -0.5F, -8.0F, 4.0F, 3.0F, 4.0F)
                        .texOffs(48, 0).addBox(-3.7F, -7.5F, -2.0F, 3.0F, 5.0F, 2.0F)
                        .texOffs(60, 0).addBox(0.7F, -7.5F, -2.0F, 3.0F, 5.0F, 2.0F),
                PartPose.offset(0.0F, 11.5F, -7.0F));
        leg(root, "front_right_leg", 0, 52, -2.8F, -4.5F);
        leg(root, "front_left_leg", 14, 52, 2.8F, -4.5F);
        leg(root, "back_right_leg", 28, 52, -2.8F, 4.5F);
        leg(root, "back_left_leg", 42, 52, 2.8F, 4.5F);
        root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 68)
                        .addBox(-2.5F, -2.5F, 0.0F, 5.0F, 5.0F, 14.0F),
                PartPose.offsetAndRotation(0.0F, 14.0F, 6.0F, 0.36F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 104, 96);
    }

    private static void leg(PartDefinition root, String name, int u, int v, float x, float z) {
        root.addOrReplaceChild(name, CubeListBuilder.create().texOffs(u, v)
                        .addBox(-1.7F, 0.0F, -1.7F, 3.4F, 7.0F, 3.4F),
                PartPose.offset(x, 17.0F, z));
    }

    @Override
    public void setupAnim(FamiliarRenderState state) {
        super.setupAnim(state);
        float walk = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0F);
        head.yRot = state.yRot * Mth.DEG_TO_RAD;
        head.xRot = state.xRot * Mth.DEG_TO_RAD + (state.aggressive ? -0.12F : 0.0F);
        legs[0].xRot = Mth.cos(walk * 0.66F) * 1.05F * speed;
        legs[1].xRot = Mth.cos(walk * 0.66F + Mth.PI) * 1.05F * speed;
        legs[2].xRot = legs[1].xRot;
        legs[3].xRot = legs[0].xRot;
        armor.yScale = 1.0F + Mth.sin(state.ageInTicks * 0.12F) * 0.018F;
        float pulse = 1.0F + Mth.sin(state.ageInTicks * 0.22F) * (0.05F + state.stability * 0.05F);
        lens.xScale = pulse;
        lens.yScale = pulse;
        lens.zScale = pulse;
        tail.yRot = Mth.sin(state.ageInTicks * 0.10F) * 0.24F;
        body.xRot = state.aggressive ? -0.06F : 0.0F;
    }
}
