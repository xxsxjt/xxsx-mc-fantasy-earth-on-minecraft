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
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            EarthOnlineMagic.id("rune_wolf"), "main");

    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart armor;
    private final ModelPart lens;
    private final ModelPart[] legs;
    private final ModelPart tail;
    private final ModelPart tailTip;

    public RuneWolfModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.armor = root.getChild("armor");
        this.lens = this.head.getChild("lens");
        this.legs = new ModelPart[]{root.getChild("front_right_leg"), root.getChild("front_left_leg"),
                root.getChild("back_right_leg"), root.getChild("back_left_leg")};
        this.tail = root.getChild("tail");
        this.tailTip = this.tail.getChild("tail_tip");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 24)
                        .addBox(-4.0F, -4.0F, -7.0F, 8.0F, 8.0F, 14.0F),
                PartPose.offset(0.0F, 15.0F, 0.0F));

        PartDefinition armor = root.addOrReplaceChild("armor", CubeListBuilder.create()
                        .texOffs(48, 20).addBox(-5.0F, -5.0F, -6.0F, 10.0F, 5.0F, 12.0F,
                                new CubeDeformation(0.16F))
                        .texOffs(48, 40).addBox(-4.5F, -3.0F, -7.2F, 9.0F, 5.0F, 5.0F,
                                new CubeDeformation(0.12F)),
                PartPose.offset(0.0F, 15.0F, 0.0F));
        armor.addOrReplaceChild("back_rune", CubeListBuilder.create()
                        .texOffs(92, 20).addBox(-2.0F, -0.5F, -2.0F, 4.0F, 1.0F, 4.0F),
                PartPose.offset(0.0F, -5.7F, 1.0F));

        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.5F, -3.5F, -4.5F, 7.0F, 7.0F, 7.0F)
                        .texOffs(30, 0).addBox(-2.0F, -0.4F, -8.5F, 4.0F, 3.0F, 5.0F)
                        .texOffs(48, 0).addBox(-3.1F, -7.8F, -1.8F, 2.0F, 5.0F, 2.0F)
                        .texOffs(58, 0).addBox(1.1F, -7.8F, -1.8F, 2.0F, 5.0F, 2.0F),
                PartPose.offset(0.0F, 11.5F, -7.0F));
        head.addOrReplaceChild("lens", CubeListBuilder.create().texOffs(70, 0)
                        .addBox(-1.5F, -1.5F, -0.6F, 3.0F, 3.0F, 1.0F),
                PartPose.offset(0.0F, -2.0F, -4.5F));

        leg(root, "front_right_leg", 0, -2.7F, -4.7F);
        leg(root, "front_left_leg", 1, 2.7F, -4.7F);
        leg(root, "back_right_leg", 2, -2.7F, 4.7F);
        leg(root, "back_left_leg", 3, 2.7F, 4.7F);

        PartDefinition tail = root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 84)
                        .addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 7.0F),
                PartPose.offsetAndRotation(0.0F, 14.0F, 6.0F, 0.34F, 0.0F, 0.0F));
        tail.addOrReplaceChild("tail_tip", CubeListBuilder.create().texOffs(28, 84)
                        .addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 7.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 6.4F, 0.06F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    private static void leg(PartDefinition root, String name, int index, float x, float z) {
        int legU = index * 12;
        int pawU = index * 16;
        PartDefinition leg = root.addOrReplaceChild(name, CubeListBuilder.create().texOffs(legU, 52)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F),
                PartPose.offset(x, 16.5F, z));
        leg.addOrReplaceChild("paw", CubeListBuilder.create().texOffs(pawU, 68)
                        .addBox(-1.5F, -0.5F, -2.5F, 3.0F, 2.0F, 4.0F),
                PartPose.offset(0.0F, 5.5F, -0.2F));
    }

    @Override
    public void setupAnim(FamiliarRenderState state) {
        super.setupAnim(state);
        float walk = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0F);
        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD + (state.aggressive ? -0.12F : 0.0F);
        this.legs[0].xRot = Mth.cos(walk * 0.66F) * 1.05F * speed;
        this.legs[1].xRot = Mth.cos(walk * 0.66F + Mth.PI) * 1.05F * speed;
        this.legs[2].xRot = this.legs[1].xRot;
        this.legs[3].xRot = this.legs[0].xRot;
        this.armor.yScale = 1.0F + Mth.sin(state.ageInTicks * 0.12F) * 0.018F;
        float pulse = 1.0F + Mth.sin(state.ageInTicks * 0.22F) * (0.05F + state.stability * 0.05F);
        this.lens.xScale = pulse;
        this.lens.yScale = pulse;
        this.lens.zScale = pulse;
        this.tail.yRot = Mth.sin(state.ageInTicks * 0.1F) * 0.2F;
        this.tailTip.yRot = Mth.sin(state.ageInTicks * 0.13F + 0.7F) * 0.25F;
        this.body.xRot = state.aggressive ? -0.06F : 0.0F;
        float attack = Mth.sin(state.attackProgress * Mth.PI);
        this.head.xRot -= attack * 0.64F;
        this.legs[0].xRot -= attack * 0.40F;
        this.legs[1].xRot -= attack * 0.40F;
        if (state.sitting) {
            this.body.xRot = 0.16F;
            this.legs[0].xRot = -0.12F;
            this.legs[1].xRot = -0.12F;
            this.legs[2].xRot = -1.05F;
            this.legs[3].xRot = -1.05F;
            this.tail.xRot = 0.96F;
        }
    }
}
