package com.xxsx.earthonlinemagic.client;

import com.xxsx.earthonlinemagic.ArcaneVisualAction;
import com.xxsx.earthonlinemagic.ArcaneVisualPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

import java.util.HashMap;
import java.util.Map;

public final class ArcanePlayerAnimations {
    private static final Map<Integer, ActiveAction> ACTIVE = new HashMap<>();
    private static ClientLevel activeLevel;

    private ArcanePlayerAnimations() {
    }

    public static void start(ArcaneVisualPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        ensureLevel(minecraft.level);
        ArcaneVisualAction action = ArcaneVisualAction.byId(payload.actionId());
        ACTIVE.put(payload.entityId(), new ActiveAction(action, minecraft.level.getGameTime()));
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            ACTIVE.clear();
            activeLevel = null;
            return;
        }
        ensureLevel(minecraft.level);
        long now = minecraft.level.getGameTime();
        ACTIVE.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }

    public static void renderPlayer(RenderPlayerEvent.Pre<?> event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        ActiveAction active = ACTIVE.get(event.getRenderState().id);
        if (active == null) {
            return;
        }
        float progress = active.progress(minecraft.level.getGameTime(), event.getPartialTick());
        if (progress < 0.0F || progress >= 1.0F) {
            return;
        }
        apply(event.getRenderer().getModel(), active.action(), progress);
    }

    private static void apply(PlayerModel model, ArcaneVisualAction action, float progress) {
        float pulse = Mth.sin(progress * Mth.PI);
        switch (action) {
            case ATTUNEMENT -> {
                model.rightArm.xRot = -0.82F - pulse * 0.22F;
                model.leftArm.xRot = -0.82F - pulse * 0.22F;
                model.rightArm.yRot = -0.68F + progress * 0.20F;
                model.leftArm.yRot = 0.68F - progress * 0.20F;
                model.rightArm.zRot = 0.16F;
                model.leftArm.zRot = -0.16F;
            }
            case MANA_RECOVERY -> {
                model.rightArm.xRot = -1.30F + pulse * 0.12F;
                model.leftArm.xRot = -1.30F + pulse * 0.12F;
                model.rightArm.yRot = -0.38F;
                model.leftArm.yRot = 0.38F;
                model.head.xRot += 0.08F * pulse;
            }
            case AETHER_PULSE -> {
                model.rightArm.xRot = -1.72F + progress * 0.38F;
                model.rightArm.yRot = -0.24F + pulse * 0.18F;
                model.leftArm.xRot = -0.65F;
                model.leftArm.yRot = 0.54F;
                model.body.yRot += (progress - 0.5F) * 0.22F;
            }
            case BODY_WARD -> {
                model.rightArm.xRot = -1.08F;
                model.leftArm.xRot = -1.08F;
                model.rightArm.zRot = 0.82F - pulse * 0.16F;
                model.leftArm.zRot = -0.82F + pulse * 0.16F;
                model.body.xRot -= 0.06F * pulse;
            }
            case BREATH_WARD -> {
                model.rightArm.xRot = -0.92F - pulse * 0.18F;
                model.leftArm.xRot = -0.92F - pulse * 0.18F;
                model.rightArm.yRot = -0.46F;
                model.leftArm.yRot = 0.46F;
                model.rightArm.zRot = 0.20F + pulse * 0.12F;
                model.leftArm.zRot = -0.20F - pulse * 0.12F;
                model.head.xRot += 0.14F * pulse;
            }
        }
        copyPose(model.rightArm, model.rightSleeve);
        copyPose(model.leftArm, model.leftSleeve);
        copyPose(model.body, model.jacket);
    }

    private static void copyPose(ModelPart source, ModelPart target) {
        target.x = source.x;
        target.y = source.y;
        target.z = source.z;
        target.xRot = source.xRot;
        target.yRot = source.yRot;
        target.zRot = source.zRot;
        target.xScale = source.xScale;
        target.yScale = source.yScale;
        target.zScale = source.zScale;
    }

    private static void ensureLevel(ClientLevel level) {
        if (activeLevel != level) {
            ACTIVE.clear();
            activeLevel = level;
        }
    }

    private record ActiveAction(ArcaneVisualAction action, long startTick) {
        boolean isExpired(long now) {
            return now - startTick >= action.durationTicks();
        }

        float progress(long now, float partialTick) {
            return (now + partialTick - startTick) / action.durationTicks();
        }
    }
}
