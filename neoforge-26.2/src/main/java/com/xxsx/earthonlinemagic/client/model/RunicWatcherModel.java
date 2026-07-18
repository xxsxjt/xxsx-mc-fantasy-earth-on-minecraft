package com.xxsx.earthonlinemagic.client.model;

import com.xxsx.earthonlinemagic.EarthOnlineMagic;
import com.xxsx.earthonlinemagic.client.renderer.RunicWatcherRenderState;
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

public final class RunicWatcherModel extends EntityModel<RunicWatcherRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            EarthOnlineMagic.id("runic_watcher"), "main");

    private final ModelPart head;
    private final ModelPart lens;
    private final ModelPart torso;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart rightPylon;
    private final ModelPart leftPylon;

    public RunicWatcherModel(ModelPart root) {
        super(root, RenderTypes::entityTranslucent);
        this.head = root.getChild("head");
        this.lens = this.head.getChild("lens");
        this.torso = root.getChild("torso");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.rightPylon = root.getChild("right_pylon");
        this.leftPylon = root.getChild("left_pylon");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        CubeDeformation rim = new CubeDeformation(0.18F);

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -7.0F, -4.0F, 8.0F, 7.0F, 8.0F)
                        .texOffs(32, 0).addBox(-4.0F, -7.0F, -4.0F, 8.0F, 7.0F, 8.0F, rim),
                PartPose.offset(0.0F, 5.0F, 0.0F));
        head.addOrReplaceChild("lens",
                CubeListBuilder.create().texOffs(0, 52).addBox(-2.0F, -2.0F, -0.8F, 4.0F, 4.0F, 1.0F),
                PartPose.offset(0.0F, -3.8F, -4.0F));

        root.addOrReplaceChild("torso",
                CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-5.0F, 0.0F, -3.0F, 10.0F, 11.0F, 6.0F)
                        .texOffs(32, 16).addBox(-5.0F, 1.0F, -3.5F, 10.0F, 4.0F, 1.0F, rim),
                PartPose.offset(0.0F, 5.0F, 0.0F));

        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(0, 34).addBox(-4.0F, -2.0F, -2.5F, 5.0F, 13.0F, 5.0F),
                PartPose.offset(-6.0F, 7.0F, 0.0F));
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(20, 34).addBox(-1.0F, -2.0F, -2.5F, 5.0F, 13.0F, 5.0F),
                PartPose.offset(6.0F, 7.0F, 0.0F));
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(40, 34).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 9.0F, 5.0F),
                PartPose.offset(-2.7F, 15.0F, 0.0F));
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(40, 48).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 9.0F, 5.0F),
                PartPose.offset(2.7F, 15.0F, 0.0F));

        root.addOrReplaceChild("right_pylon",
                CubeListBuilder.create().texOffs(10, 54).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 8.0F, 2.0F),
                PartPose.offsetAndRotation(-4.2F, 6.5F, 3.4F, -0.18F, 0.0F, -0.28F));
        root.addOrReplaceChild("left_pylon",
                CubeListBuilder.create().texOffs(18, 54).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 8.0F, 2.0F),
                PartPose.offsetAndRotation(4.2F, 6.5F, 3.4F, -0.18F, 0.0F, 0.28F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(RunicWatcherRenderState state) {
        super.setupAnim(state);
        float walk = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0F);
        float age = state.ageInTicks;

        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
        this.torso.yRot = Mth.sin(age * 0.035F) * 0.035F;
        this.rightLeg.xRot = Mth.cos(walk * 0.6662F) * 1.05F * speed;
        this.leftLeg.xRot = Mth.cos(walk * 0.6662F + Mth.PI) * 1.05F * speed;

        float combatLift = state.aggressive ? -0.65F : 0.0F;
        this.rightArm.xRot = Mth.cos(walk * 0.6662F + Mth.PI) * 0.7F * speed + combatLift;
        this.leftArm.xRot = Mth.cos(walk * 0.6662F) * 0.7F * speed + combatLift;
        this.rightArm.zRot = -0.08F - (state.aggressive ? 0.22F : 0.0F);
        this.leftArm.zRot = 0.08F + (state.aggressive ? 0.22F : 0.0F);

        float pylonSwing = Mth.sin(age * 0.095F) * 0.12F;
        this.rightPylon.zRot = -0.28F - pylonSwing;
        this.leftPylon.zRot = 0.28F + pylonSwing;
        float pulse = 1.0F + Mth.sin(age * 0.22F) * (0.06F + (1.0F - state.integrity) * 0.05F);
        this.lens.xScale = pulse;
        this.lens.yScale = pulse;
        this.lens.zScale = pulse;
    }
}
