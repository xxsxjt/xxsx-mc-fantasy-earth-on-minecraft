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

    public AetherFoxModel(ModelPart root) {
        super(root, RenderTypes::entityTranslucent);
        head = root.getChild("head");
        leftEarFin = head.getChild("left_ear_fin");
        rightEarFin = head.getChild("right_ear_fin");
        body = root.getChild("body");
        legs = new ModelPart[]{root.getChild("front_right_leg"), root.getChild("front_left_leg"),
                root.getChild("back_right_leg"), root.getChild("back_left_leg")};
        leftTail = root.getChild("left_tail");
        rightTail = root.getChild("right_tail");
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
        root.addOrReplaceChild("left_tail", CubeListBuilder.create().texOffs(0, 62)
                        .addBox(-2.2F, -2.2F, 0.0F, 4.4F, 4.4F, 15.0F),
                PartPose.offsetAndRotation(2.0F, 14.0F, 5.0F, 0.35F, 0.22F, 0.0F));
        root.addOrReplaceChild("right_tail", CubeListBuilder.create().texOffs(40, 62)
                        .addBox(-2.2F, -2.2F, 0.0F, 4.4F, 4.4F, 15.0F),
                PartPose.offsetAndRotation(-2.0F, 14.0F, 5.0F, 0.35F, -0.22F, 0.0F));
        return LayerDefinition.create(mesh, 96, 96);
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
        leftEarFin.zScale = 1.0F + Mth.sin(state.ageInTicks * 0.18F) * 0.06F;
        rightEarFin.zScale = leftEarFin.zScale;
        body.y = 15.0F + Mth.sin(state.ageInTicks * 0.08F) * 0.05F;
    }
}
