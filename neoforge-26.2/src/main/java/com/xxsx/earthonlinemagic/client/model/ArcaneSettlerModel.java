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
    private final ModelPart witchHat;
    private final ModelPart goblinEars;
    private final ModelPart lens;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart witchPack;
    private final ModelPart goblinPack;
    private final ModelPart researcherPack;

    public ArcaneSettlerModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
        this.head = root.getChild("head");
        this.witchHat = this.head.getChild("witch_hat");
        this.goblinEars = this.head.getChild("goblin_ears");
        this.lens = this.head.getChild("lens");
        this.body = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.witchPack = root.getChild("witch_pack");
        this.goblinPack = root.getChild("goblin_pack");
        this.researcherPack = root.getChild("researcher_pack");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.ZERO);
        head.addOrReplaceChild("hair_cap", CubeListBuilder.create()
                        .texOffs(32, 0).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 3.0F, 9.0F,
                                new CubeDeformation(0.06F)), PartPose.ZERO);
        PartDefinition hat = head.addOrReplaceChild("witch_hat", CubeListBuilder.create()
                        .texOffs(70, 0).addBox(-5.0F, -1.0F, -5.0F, 10.0F, 2.0F, 10.0F),
                PartPose.offset(0.0F, -8.5F, 0.0F));
        hat.addOrReplaceChild("crown", CubeListBuilder.create()
                        .texOffs(70, 14).addBox(-3.0F, -7.0F, -3.0F, 6.0F, 7.0F, 6.0F,
                                new CubeDeformation(0.08F)), PartPose.ZERO);
        head.addOrReplaceChild("goblin_ears", CubeListBuilder.create()
                        .texOffs(104, 0).addBox(-7.0F, -5.0F, -0.5F, 3.0F, 3.0F, 1.0F)
                        .texOffs(112, 0).addBox(4.0F, -5.0F, -0.5F, 3.0F, 3.0F, 1.0F),
                PartPose.ZERO);
        head.addOrReplaceChild("lens", CubeListBuilder.create().texOffs(104, 8)
                        .addBox(-1.5F, -1.5F, -0.6F, 3.0F, 3.0F, 1.0F),
                PartPose.offset(2.0F, -4.0F, -4.0F));

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 20).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F),
                PartPose.ZERO);
        body.addOrReplaceChild("coat", CubeListBuilder.create()
                        .texOffs(26, 20).addBox(-4.5F, -0.5F, -2.5F, 9.0F, 13.0F, 5.0F,
                                new CubeDeformation(0.04F)), PartPose.ZERO);
        body.addOrReplaceChild("coat_skirt", CubeListBuilder.create()
                        .texOffs(56, 20).addBox(-5.0F, 0.0F, -3.0F, 10.0F, 8.0F, 6.0F,
                                new CubeDeformation(0.03F)), PartPose.offset(0.0F, 10.0F, 0.0F));

        PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                        .texOffs(0, 42).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-5.0F, 2.0F, 0.0F));
        rightArm.addOrReplaceChild("right_sleeve", CubeListBuilder.create()
                        .texOffs(32, 42).addBox(-3.5F, -2.5F, -2.5F, 5.0F, 9.0F, 5.0F,
                                new CubeDeformation(0.03F)), PartPose.ZERO);
        PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                        .texOffs(16, 42).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(5.0F, 2.0F, 0.0F));
        leftArm.addOrReplaceChild("left_sleeve", CubeListBuilder.create()
                        .texOffs(52, 42).addBox(-1.5F, -2.5F, -2.5F, 5.0F, 9.0F, 5.0F,
                                new CubeDeformation(0.03F)), PartPose.ZERO);

        PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                        .texOffs(0, 60).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-1.9F, 12.0F, 0.0F));
        rightLeg.addOrReplaceChild("right_boot", CubeListBuilder.create()
                        .texOffs(32, 60).addBox(-2.5F, -1.0F, -2.8F, 5.0F, 5.0F, 5.0F),
                PartPose.offset(0.0F, 8.0F, 0.0F));
        PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                        .texOffs(16, 60).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(1.9F, 12.0F, 0.0F));
        leftLeg.addOrReplaceChild("left_boot", CubeListBuilder.create()
                        .texOffs(52, 60).addBox(-2.5F, -1.0F, -2.8F, 5.0F, 5.0F, 5.0F),
                PartPose.offset(0.0F, 8.0F, 0.0F));

        PartDefinition witchPack = root.addOrReplaceChild("witch_pack", CubeListBuilder.create()
                        .texOffs(0, 80).addBox(-4.5F, 0.0F, 0.0F, 9.0F, 12.0F, 4.0F),
                PartPose.offset(0.0F, 2.0F, 2.0F));
        witchPack.addOrReplaceChild("herb_bottle", CubeListBuilder.create()
                        .texOffs(28, 80).addBox(-1.5F, -2.0F, -1.5F, 3.0F, 5.0F, 3.0F),
                PartPose.offset(5.0F, 7.0F, 2.5F));

        PartDefinition goblinPack = root.addOrReplaceChild("goblin_pack", CubeListBuilder.create()
                        .texOffs(42, 80).addBox(-5.0F, 0.0F, 0.0F, 10.0F, 11.0F, 5.0F),
                PartPose.offset(0.0F, 3.0F, 2.0F));
        goblinPack.addOrReplaceChild("tool_roll", CubeListBuilder.create()
                        .texOffs(74, 80).addBox(-4.0F, -2.0F, -2.0F, 8.0F, 4.0F, 4.0F),
                PartPose.offset(0.0F, 1.0F, 2.5F));

        PartDefinition researcherPack = root.addOrReplaceChild("researcher_pack", CubeListBuilder.create()
                        .texOffs(0, 102).addBox(-4.5F, 0.0F, 0.0F, 9.0F, 13.0F, 4.0F),
                PartPose.offset(0.0F, 2.0F, 2.0F));
        researcherPack.addOrReplaceChild("scroll_tube", CubeListBuilder.create()
                        .texOffs(28, 102).addBox(-1.5F, -7.0F, -1.5F, 3.0F, 14.0F, 3.0F),
                PartPose.offset(4.5F, 6.0F, 2.5F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(ArcaneSettlerRenderState state) {
        super.setupAnim(state);
        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
        float walk = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0F);
        this.rightLeg.xRot = Mth.cos(walk * 0.6662F) * 1.0F * speed;
        this.leftLeg.xRot = Mth.cos(walk * 0.6662F + Mth.PI) * 1.0F * speed;
        if (state.trading) {
            this.rightArm.xRot = -0.72F;
            this.leftArm.xRot = -0.72F;
            this.rightArm.yRot = -0.28F;
            this.leftArm.yRot = 0.28F;
        } else {
            this.rightArm.xRot = this.leftLeg.xRot * 0.65F;
            this.leftArm.xRot = this.rightLeg.xRot * 0.65F;
            this.rightArm.yRot = 0.0F;
            this.leftArm.yRot = 0.0F;
        }
        this.witchHat.visible = state.role == 0;
        this.witchPack.visible = state.role == 0;
        this.goblinEars.visible = state.role == 1;
        this.goblinPack.visible = state.role == 1;
        this.lens.visible = state.role == 2;
        this.researcherPack.visible = state.role == 2;
        this.lens.xScale = 1.0F + Mth.sin(state.ageInTicks * 0.18F) * 0.08F;
        this.body.yRot = Mth.sin(state.ageInTicks * 0.03F) * 0.015F;
    }
}
