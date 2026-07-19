package com.xxsx.earthonlinemagic.client;

import com.xxsx.earthonlinemagic.ArcanaPower;
import com.xxsx.earthonlinemagic.ArcaneStatusPayload;
import com.xxsx.earthonlinemagic.ArcaneSeatEntity;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class ArcaneFocusHud {
    private static final String[] STAGES = {
            "hud.earth_online_magic.focus.stage.stabilize",
            "hud.earth_online_magic.focus.stage.sense",
            "hud.earth_online_magic.focus.stage.attune",
            "hud.earth_online_magic.focus.stage.anchor"
    };

    private ArcaneFocusHud() {
    }

    public static String stageKey(int stage) {
        return STAGES[Math.max(0, Math.min(STAGES.length - 1, stage))];
    }

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.gui.screen() != null
                || !(minecraft.player.getVehicle() instanceof ArcaneSeatEntity seat)) {
            return;
        }

        Font font = minecraft.font;
        int panelWidth = Math.min(188, graphics.guiWidth() - 16);
        int panelHeight = 68;
        int x = (graphics.guiWidth() - panelWidth) / 2;
        int y = Math.max(8, graphics.guiHeight() - 94);

        ArcaneStatusPayload status = EarthOnlineMagicClient.arcaneStatus();
        float cycleTick = Math.max(0.0F,
                ArcanaPower.MAGIC_FOCUS_COOLDOWN_TICKS - status.remainingTicks());
        float progress = Math.min(1.0F, cycleTick / ArcanaPower.MAGIC_FOCUS_COOLDOWN_TICKS);
        int stage = Math.min(STAGES.length - 1, (int) (progress * STAGES.length));
        int remainingSeconds = Math.max(1, (status.remainingTicks() + 19) / 20);

        int border = 0xD88E7CFF;
        int panel = 0xD0141320;
        int accent = 0xFFAA9CFF;
        int muted = 0xFFDDD8F2;

        graphics.fill(x, y, x + panelWidth, y + panelHeight, border);
        graphics.fill(x + 1, y + 1, x + panelWidth - 1, y + panelHeight - 1, panel);
        int glyphX = x + 8;
        int glyphY = y + 8;
        int glyphSize = 38;
        graphics.outline(glyphX, glyphY, glyphSize, glyphSize, 0xFF62597B);
        graphics.outline(glyphX + 6, glyphY + 6, glyphSize - 12, glyphSize - 12, 0xFF4D456D);
        int gcx = glyphX + glyphSize / 2;
        int gcy = glyphY + glyphSize / 2;
        graphics.fill(gcx - 1, glyphY + 3, gcx + 1, glyphY + glyphSize - 3, 0xFF494260);
        graphics.fill(glyphX + 3, gcy - 1, glyphX + glyphSize - 3, gcy + 1, 0xFF494260);
        int lit = Math.min(4, Math.max(0, stage + 1));
        int[][] nodes = {{0, -15}, {15, 0}, {0, 15}, {-15, 0}};
        for (int i = 0; i < nodes.length; i++) {
            int color = i < lit ? accent : 0xFF62597B;
            graphics.fill(gcx + nodes[i][0] - 2, gcy + nodes[i][1] - 2,
                    gcx + nodes[i][0] + 3, gcy + nodes[i][1] + 3, color);
        }
        int pulse = 2 + Math.round(progress * 2.0F);
        graphics.fill(gcx - pulse, gcy - pulse, gcx + pulse + 1, gcy + pulse + 1, 0xFFE4DEFF);

        int textX = x + 54;
        int textWidth = panelWidth - 62;
        Component focus = Component.translatable(
                com.xxsx.earthonlinemagic.ArcaneFocus.byId(status.focusId()).titleKey());
        Component title = fit(font, Component.translatable(
                "hud.earth_online_magic.focus.title", focus, status.focusLevel()), textWidth);
        graphics.text(font, title, textX, y + 7, 0xFFF8F4FF, false);

        Component stageText = Component.translatable(stageKey(stage));
        Component remainingText = Component.translatable("hud.earth_online_magic.focus.remaining", remainingSeconds);
        graphics.text(font, fit(font, stageText, textWidth), textX, y + 20, accent, false);
        graphics.text(font, remainingText, textX, y + 33, muted, false);

        int segmentY = y + 47;
        int gap = 2;
        int segmentWidth = Math.max(8, (textWidth - gap * 3) / 4);
        for (int i = 0; i < 4; i++) {
            int sx = textX + i * (segmentWidth + gap);
            int color = i <= stage ? accent : 0xFF62597B;
            graphics.fill(sx, segmentY, Math.min(x + panelWidth - 6, sx + segmentWidth), segmentY + 4, 0xFF312B43);
            graphics.fill(sx + 1, segmentY + 1, Math.min(x + panelWidth - 7, sx + segmentWidth - 1), segmentY + 3, color);
        }
        graphics.centeredText(font, fit(font, Component.translatable("hud.earth_online_magic.focus.hint"), panelWidth - 18),
                x + panelWidth / 2, y + 57, muted);
    }

    private static Component fit(Font font, Component value, int width) {
        String text = value.getString();
        if (font.width(text) <= width) {
            return value;
        }
        return Component.literal(font.plainSubstrByWidth(text, Math.max(0, width - font.width("..."))) + "...");
    }
}
