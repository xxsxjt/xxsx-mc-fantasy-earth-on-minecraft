package com.xxsx.earthonlinemagic.client.model;

import com.xxsx.earthonlinemagic.EarthOnlineMagic;
import com.xxsx.earthonlinemagic.client.renderer.ArcaneSettlerRenderState;
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

public final class ArcaneSettlerModel extends EntityModel<ArcaneSettlerRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            EarthOnlineMagic.id("arcane_settler"), "main");
    private final ModelPart head;
    private final ModelPart hat;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart satchel;
    private final ModelPart lens;

    public ArcaneSettlerModel(ModelPart root) {
        super(root, RenderTypes::entityTranslucent);
        head = root.getChild("head");
        hat = head.getChild("hat");
        body = root.getChild("body");
        rightArm = root.getChild("right_arm");
        leftArm = root.getChild("left_arm");
        rightLeg = root.getChild("right_leg");
        leftLeg = root.getChild("left_leg");
        satchel = root.getChild("satchel");
        lens = head.getChild("lens");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offset(0.0F, 6.0F, 0.0F));
        head.addOrReplaceChild("hat", CubeListBuilder.create()
                        .texOffs(32, 0).addBox(-5.0F, -1.0F, -5.0F, 10.0F, 2.0F, 10.0F)
                        .texOffs(32, 12).addBox(-3.0F, -6.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.12F)),
                PartPose.offsetAndRotation(0.0F, -8.0F, 0.0F, -0.08F, 0.0F, 0.0F));
        head.addOrReplaceChild("lens", CubeListBuilder.create().texOffs(0, 50)
                        .addBox(-1.5F, -1.5F, -0.8F, 3.0F, 3.0F, 1.0F),
                PartPose.offset(2.0F, -4.0F, -4.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(16, 18).addBox(-4.0F, 0.0F, -2.5F, 8.0F, 12.0F, 5.0F)
                        .texOffs(16, 36).addBox(-5.0F, 7.0F, -3.0F, 10.0F, 7.0F, 6.0F, new CubeDeformation(0.08F)),
                PartPose.offset(0.0F, 6.0F, 0.0F));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 18)
                        .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 13.0F, 4.0F),
                PartPose.offset(-5.0F, 8.0F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(44, 18)
                        .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 13.0F, 4.0F),
                PartPose.offset(5.0F, 8.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 36)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F),
                PartPose.offset(-2.0F, 18.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(48, 36)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F),
                PartPose.offset(2.0F, 18.0F, 0.0F));
        root.addOrReplaceChild("satchel", CubeListBuilder.create().texOffs(10, 50)
                        .addBox(-3.5F, -3.0F, 0.0F, 7.0F, 7.0F, 3.0F),
                PartPose.offset(4.0F, 14.0F, 2.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(ArcaneSettlerRenderState state) {
        super.setupAnim(state);
        head.yRot = state.yRot * Mth.DEG_TO_RAD;
        head.xRot = state.xRot * Mth.DEG_TO_RAD;
        float walk = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0F);
        rightLeg.xRot = Mth.cos(walk * 0.6662F) * 1.0F * speed;
        leftLeg.xRot = Mth.cos(walk * 0.6662F + Mth.PI) * 1.0F * speed;
        if (state.trading) {
            rightArm.xRot = -0.72F;
            leftArm.xRot = -0.72F;
            rightArm.yRot = -0.28F;
            leftArm.yRot = 0.28F;
        } else {
            rightArm.xRot = leftLeg.xRot * 0.65F;
            leftArm.xRot = rightLeg.xRot * 0.65F;
            rightArm.yRot = 0.0F;
            leftArm.yRot = 0.0F;
        }
        hat.visible = state.role == 0;
        satchel.visible = state.role == 1;
        lens.visible = state.role == 2;
        lens.xScale = 1.0F + Mth.sin(state.ageInTicks * 0.18F) * 0.08F;
        body.yRot = Mth.sin(state.ageInTicks * 0.03F) * 0.015F;
    }
}
