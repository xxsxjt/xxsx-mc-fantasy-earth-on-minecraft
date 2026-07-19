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

public final class AetherFoxModel extends EntityModel<FamiliarRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(EarthOnlineMagic.id("aether_fox"), "main");
    private final ModelPart head;
    private final ModelPart leftEarFin;
    private final ModelPart rightEarFin;
    private final ModelPart body;
    private final ModelPart[] legs;
    private final ModelPart leftTail;
    private final ModelPart rightTail;
    private final ModelPart leftTailMid;
    private final ModelPart rightTailMid;

    public AetherFoxModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
        head = root.getChild("head");
        leftEarFin = head.getChild("left_ear_fin");
        rightEarFin = head.getChild("right_ear_fin");
        body = root.getChild("body");
        legs = new ModelPart[]{root.getChild("front_right_leg"), root.getChild("front_left_leg"),
                root.getChild("back_right_leg"), root.getChild("back_left_leg")};
        leftTail = root.getChild("left_tail");
        rightTail = root.getChild("right_tail");
        leftTailMid = leftTail.getChild("mid");
        rightTailMid = rightTail.getChild("mid");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 24).addBox(-4.0F, -4.0F, -6.0F, 8.0F, 8.0F, 12.0F)
                        .texOffs(42, 25).addBox(-3.6F, -4.7F, -5.0F, 7.2F, 1.2F, 10.0F, new CubeDeformation(0.08F)),
                PartPose.offset(0.0F, 15.0F, 0.0F));
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -4.0F, -5.0F, 8.0F, 7.0F, 7.0F)
                        .texOffs(30, 0).addBox(-2.0F, -0.5F, -8.0F, 4.0F, 3.0F, 4.0F),
                PartPose.offset(0.0F, 12.0F, -6.0F));
        head.addOrReplaceChild("left_ear_fin", CubeListBuilder.create().texOffs(48, 0)
                        .addBox(0.0F, -6.0F, -1.0F, 5.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(1.0F, -3.0F, -1.0F, 0.0F, -0.18F, -0.15F));
        head.addOrReplaceChild("right_ear_fin", CubeListBuilder.create().texOffs(60, 0)
                        .addBox(-5.0F, -6.0F, -1.0F, 5.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(-1.0F, -3.0F, -1.0F, 0.0F, 0.18F, 0.15F));
        leg(root, "front_right_leg", 0, 48, -2.5F, -4.0F);
        leg(root, "front_left_leg", 12, 48, 2.5F, -4.0F);
        leg(root, "back_right_leg", 24, 48, -2.5F, 4.0F);
        leg(root, "back_left_leg", 36, 48, 2.5F, 4.0F);
        PartDefinition leftTail = root.addOrReplaceChild("left_tail", CubeListBuilder.create().texOffs(0, 64)
                        .addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 7.0F),
                PartPose.offsetAndRotation(2.0F, 14.0F, 5.0F, 0.35F, 0.22F, 0.0F));
        PartDefinition leftTailMid = leftTail.addOrReplaceChild("mid", CubeListBuilder.create().texOffs(48, 64)
                        .addBox(-2.5F, -2.5F, 0.0F, 5.0F, 5.0F, 7.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 6.3F, 0.12F, 0.12F, 0.0F));
        leftTailMid.addOrReplaceChild("tip", CubeListBuilder.create().texOffs(0, 84)
                        .addBox(-3.0F, -3.0F, 0.0F, 6.0F, 6.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 6.3F, 0.10F, 0.10F, 0.0F));

        PartDefinition rightTail = root.addOrReplaceChild("right_tail", CubeListBuilder.create().texOffs(24, 64)
                        .addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 7.0F),
                PartPose.offsetAndRotation(-2.0F, 14.0F, 5.0F, 0.35F, -0.22F, 0.0F));
        PartDefinition rightTailMid = rightTail.addOrReplaceChild("mid", CubeListBuilder.create().texOffs(74, 64)
                        .addBox(-2.5F, -2.5F, 0.0F, 5.0F, 5.0F, 7.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 6.3F, 0.12F, -0.12F, 0.0F));
        rightTailMid.addOrReplaceChild("tip", CubeListBuilder.create().texOffs(26, 84)
                        .addBox(-3.0F, -3.0F, 0.0F, 6.0F, 6.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 6.3F, 0.10F, -0.10F, 0.0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    private static void leg(PartDefinition root, String name, int u, int v, float x, float z) {
        root.addOrReplaceChild(name, CubeListBuilder.create().texOffs(u, v)
                        .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F),
                PartPose.offset(x, 18.0F, z));
    }

    @Override
    public void setupAnim(FamiliarRenderState state) {
        super.setupAnim(state);
        float walk = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0F);
        head.yRot = state.yRot * Mth.DEG_TO_RAD;
        head.xRot = state.xRot * Mth.DEG_TO_RAD;
        legs[0].xRot = Mth.cos(walk * 0.68F) * 1.1F * speed;
        legs[1].xRot = Mth.cos(walk * 0.68F + Mth.PI) * 1.1F * speed;
        legs[2].xRot = legs[1].xRot;
        legs[3].xRot = legs[0].xRot;
        float tail = Mth.sin(state.ageInTicks * 0.10F) * (0.25F + state.stability * 0.16F);
        leftTail.yRot = 0.22F + tail;
        rightTail.yRot = -0.22F - tail;
        leftTailMid.yRot = 0.12F + tail * 0.45F;
        rightTailMid.yRot = -0.12F - tail * 0.45F;
        leftEarFin.zScale = 1.0F + Mth.sin(state.ageInTicks * 0.18F) * 0.06F;
        rightEarFin.zScale = leftEarFin.zScale;
        body.y = 15.0F + Mth.sin(state.ageInTicks * 0.08F) * 0.05F;
        float attack = Mth.sin(state.attackProgress * Mth.PI);
        head.xRot -= attack * 0.52F;
        body.xRot = -attack * 0.10F;
        legs[0].xRot -= attack * 0.38F;
        legs[1].xRot -= attack * 0.38F;
        if (state.sitting) {
            body.xRot = 0.18F;
            legs[0].xRot = -0.10F;
            legs[1].xRot = -0.10F;
            legs[2].xRot = -1.08F;
            legs[3].xRot = -1.08F;
            leftTail.xRot = 0.92F;
            rightTail.xRot = 0.92F;
        }
    }
}
