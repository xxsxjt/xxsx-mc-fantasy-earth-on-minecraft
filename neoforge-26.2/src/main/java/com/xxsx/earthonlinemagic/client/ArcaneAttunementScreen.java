package com.xxsx.earthonlinemagic.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.xxsx.earthonlinemagic.ArcanaPower;
import com.xxsx.earthonlinemagic.ArcaneFocus;
import com.xxsx.earthonlinemagic.ArcaneStatusPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ArcaneAttunementScreen extends Screen {
    private static final int BACKDROP = 0xAE090812;
    private static final int PANEL = 0xF0131120;
    private static final int PANEL_ALT = 0xFF211E35;
    private static final int EDGE = 0xFF8E7CFF;
    private static final int ACCENT = 0xFFAA9CFF;
    private static final int INK = 0xFFF8F4FF;
    private static final int MUTED = 0xFFC8C0E2;
    private static final int LOCKED = 0xFF927B8D;

    private final Map<ArcaneFocus, Button> focusButtons = new EnumMap<>(ArcaneFocus.class);
    private Button primaryActionButton;
    private Button spellButton;
    private int refreshTicks;

    public ArcaneAttunementScreen() {
        super(Component.translatable("screen.earth_online_magic.attunement.title"));
    }

    @Override
    protected void init() {
        focusButtons.clear();
        int left = panelLeft();
        int top = panelTop();
        int buttonWidth = navigationWidth() - 20;
        int y = top + 37;
        for (ArcaneFocus focus : ArcaneFocus.values()) {
            Button button = addRenderableWidget(Button.builder(Component.translatable(focus.titleKey()),
                            ignored -> EarthOnlineMagicClient.requestArcaneFocus(focus))
                    .bounds(left + 10, y, buttonWidth, 18)
                    .build());
            button.setTooltip(Tooltip.create(Component.translatable(focus.descriptionKey())));
            focusButtons.put(focus, button);
            y += 20;
        }
        spellButton = addRenderableWidget(Button.builder(
                        Component.translatable("screen.earth_online_magic.attunement.skill"),
                        ignored -> EarthOnlineMagicClient.requestActivateArcaneSkill())
                .bounds(left + 10, top + panelHeight() - 50, buttonWidth, 18)
                .build());
        primaryActionButton = addRenderableWidget(Button.builder(
                        Component.translatable("screen.earth_online_magic.attunement.practice"),
                        ignored -> performPrimaryAction())
                .bounds(left + 10, top + panelHeight() - 28, buttonWidth, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal("X"), ignored -> onClose())
                .bounds(left + panelWidth() - 28, top + 8, 18, 18)
                .build());
        updateButtons();
    }

    @Override
    public void tick() {
        updateButtons();
        if (minecraft == null || minecraft.player == null || minecraft.getConnection() == null) {
            onClose();
            return;
        }
        if (++refreshTicks >= 20) {
            refreshTicks = 0;
            EarthOnlineMagicClient.requestAttunementRefresh();
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        ArcaneStatusPayload status = EarthOnlineMagicClient.arcaneStatus();
        ArcaneFocus focus = ArcaneFocus.byId(status.focusId());
        int left = panelLeft();
        int top = panelTop();
        int w = panelWidth();
        int h = panelHeight();
        int navW = navigationWidth();
        int contentX = left + navW + 8;
        int contentW = left + w - 10 - contentX;

        g.fill(0, 0, width, height, BACKDROP);
        g.fill(left, top, left + w, top + h, PANEL);
        g.outline(left, top, w, h, EDGE);
        g.fill(left + 1, top + 1, left + w - 1, top + 34, PANEL_ALT);
        g.fill(left + navW, top + 35, left + navW + 1, top + h - 8, 0xFF4D456D);
        drawAmbientMotif(g, left, top, w);

        g.text(font, title, left + 12, top + 10, INK, false);
        Component support = Component.translatable(status.seated()
                ? "screen.earth_online_magic.attunement.support.focus_mat"
                : "screen.earth_online_magic.attunement.support.free");
        g.text(font, fitted(support, Math.max(60, w - navW - 46)), left + navW + 8, top + 11,
                status.seated() ? ACCENT : MUTED, false);

        List<FormattedCharSequence> description = font.split(Component.translatable(focus.descriptionKey()),
                Math.max(80, contentW));
        if (!description.isEmpty()) {
            g.text(font, description.getFirst(), contentX, top + 39, MUTED, false);
        }

        int circuitWidth = Math.min(68, Math.max(54, contentW / 3));
        int meterX = contentX + circuitWidth + 6;
        int meterWidth = Math.max(78, contentW - circuitWidth - 6);
        int y = top + 52;
        drawCompactBar(g, meterX, y, meterWidth,
                Component.translatable("screen.earth_online_magic.attunement.mana"),
                status.maxMana() <= 0.0D ? 0.0D : status.currentMana() / status.maxMana(),
                Math.round(status.currentMana()) + " / " + Math.round(status.maxMana()), ACCENT);
        y += 17;
        drawCompactBar(g, meterX, y, meterWidth,
                Component.translatable("screen.earth_online_magic.attunement.field"),
                status.fieldValue() / 100.0D, status.fieldValue() + " / 100", fieldColor(status.fieldValue()));
        y += 17;
        drawCompactBar(g, meterX, y, meterWidth,
                Component.translatable("screen.earth_online_magic.attunement.disturbance"),
                status.disturbance() / 45.0D, Math.round(status.disturbance()) + " / 45", 0xFFD575B6);
        y += 17;
        boolean focusUnlocked = status.isUnlocked(focus);
        drawCompactBar(g, meterX, y, meterWidth,
                Component.translatable("screen.earth_online_magic.attunement.growth"),
                !focusUnlocked ? 0.0D : status.focusXpNeeded() <= 0
                        ? 1.0D : status.focusXp() / (double) status.focusXpNeeded(),
                !focusUnlocked
                        ? Component.translatable("screen.earth_online_magic.attunement.growth.locked").getString()
                        : status.focusXpNeeded() <= 0
                        ? "Lv." + status.focusLevel() + " · MAX"
                        : "Lv." + status.focusLevel() + " · " + status.focusXp() + "/" + status.focusXpNeeded(),
                0xFF75C7E8);
        drawCircuit(g, contentX, top + 52, circuitWidth, 73, status);

        int stageY = top + h - 39;
        drawStageTrack(g, contentX, stageY, contentW, status);

        if (h >= 194 && contentW >= 188) {
            Component source = Component.translatable("screen.earth_online_magic.attunement.source",
                    Component.translatable(status.sourceKey()));
            g.text(font, fitted(source, contentW), contentX, stageY - 12, MUTED, false);
        }
        super.extractRenderState(g, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if ((event.key() == InputConstants.KEY_LSHIFT || event.key() == InputConstants.KEY_RSHIFT)
                && EarthOnlineMagicClient.arcaneStatus().seated()) {
            leaveFocusMat();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().gui.setScreen(null);
    }

    private void performPrimaryAction() {
        if (EarthOnlineMagicClient.arcaneStatus().seated()) {
            leaveFocusMat();
        } else {
            EarthOnlineMagicClient.requestPractice();
        }
    }

    private void leaveFocusMat() {
        EarthOnlineMagicClient.requestStopAttuning();
        onClose();
    }

    private void updateButtons() {
        ArcaneStatusPayload status = EarthOnlineMagicClient.arcaneStatus();
        ArcaneFocus selected = ArcaneFocus.byId(status.focusId());
        for (Map.Entry<ArcaneFocus, Button> entry : focusButtons.entrySet()) {
            boolean unlocked = status.isUnlocked(entry.getKey());
            entry.getValue().active = unlocked && entry.getKey() != selected;
            entry.getValue().setTooltip(Tooltip.create(Component.translatable(unlocked
                    ? entry.getKey().descriptionKey()
                    : "screen.earth_online_magic.attunement.focus.locked")));
        }
        if (primaryActionButton != null) {
            boolean seated = status.seated();
            primaryActionButton.setMessage(Component.translatable(seated
                    ? "screen.earth_online_magic.attunement.leave_focus_mat"
                    : "screen.earth_online_magic.attunement.practice"));
            primaryActionButton.active = seated
                    || status.isUnlocked(ArcaneFocus.ATTUNEMENT) && status.remainingTicks() <= 0;
            primaryActionButton.setTooltip(Tooltip.create(Component.translatable(seated
                    ? "screen.earth_online_magic.attunement.leave_focus_mat.tooltip"
                    : "screen.earth_online_magic.attunement.practice.tooltip")));
        }
        if (spellButton != null) {
            int seconds = Math.max(0, (status.skillRemainingTicks() + 19) / 20);
            spellButton.setMessage(seconds > 0
                    ? Component.translatable("screen.earth_online_magic.attunement.skill.cooldown", seconds)
                    : Component.translatable("screen.earth_online_magic.attunement.skill"));
            spellButton.active = status.isUnlocked(selected) && status.skillRemainingTicks() <= 0;
            spellButton.setTooltip(Tooltip.create(Component.translatable(
                    "screen.earth_online_magic.attunement.skill." + selected.path() + ".tooltip")));
        }
    }

    private void drawCompactBar(GuiGraphicsExtractor g, int x, int y, int width, Component label,
                                double ratio, String value, int color) {
        double clamped = Math.max(0.0D, Math.min(1.0D, ratio));
        g.text(font, label, x, y, MUTED, false);
        g.text(font, value, x + width - font.width(value), y, INK, false);
        g.fill(x, y + 11, x + width, y + 16, 0xFF373249);
        g.fill(x + 1, y + 12, x + 1 + (int) Math.round((width - 2) * clamped), y + 15, color);
    }

    private void drawCircuit(GuiGraphicsExtractor g, int x, int y, int width, int height,
                             ArcaneStatusPayload status) {
        int cx = x + width / 2;
        int cy = y + height / 2;
        int radius = Math.min(width, height) / 2 - 4;
        float progress = 1.0F - Math.min(1.0F,
                status.remainingTicks() / (float) ArcanaPower.MAGIC_FOCUS_COOLDOWN_TICKS);
        g.outline(cx - radius, cy - radius, radius * 2, radius * 2, 0xFF4D456D);
        g.outline(cx - radius + 6, cy - radius + 6, radius * 2 - 12, radius * 2 - 12, 0xFF62597B);
        g.fill(cx - radius, cy - 1, cx + radius, cy + 1, 0xFF39334E);
        g.fill(cx - 1, cy - radius, cx + 1, cy + radius, 0xFF39334E);
        int[][] nodes = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
        int activeNodes = Math.min(4, Math.max(0, (int) Math.ceil(progress * 4.0F)));
        for (int i = 0; i < nodes.length; i++) {
            int nx = cx + nodes[i][0] * radius;
            int ny = cy + nodes[i][1] * radius;
            int color = i < activeNodes ? ACCENT : 0xFF62597B;
            g.fill(nx - 3, ny - 3, nx + 4, ny + 4, 0xFF171421);
            g.fill(nx - 1, ny - 1, nx + 2, ny + 2, color);
        }
        int pulse = 2 + Math.round(2.0F * progress);
        g.fill(cx - pulse, cy - pulse, cx + pulse + 1, cy + pulse + 1, 0xFFE4DEFF);
    }

    private void drawStageTrack(GuiGraphicsExtractor g, int x, int y, int width, ArcaneStatusPayload status) {
        float progress = 1.0F - Math.min(1.0F, status.remainingTicks() / (float) ArcanaPower.MAGIC_FOCUS_COOLDOWN_TICKS);
        int current = Math.min(3, Math.max(0, (int) (progress * 4.0F)));
        int lineY = y + 2;
        int gap = 3;
        int segmentWidth = Math.max(8, (width - gap * 3) / 4);
        for (int i = 0; i < 4; i++) {
            int sx = x + i * (segmentWidth + gap);
            int color = i <= current ? ACCENT : 0xFF62597B;
            g.fill(sx, lineY, Math.min(x + width, sx + segmentWidth), lineY + 5, 0xFF312B43);
            g.fill(sx + 1, lineY + 1, Math.min(x + width, sx + segmentWidth - 1), lineY + 4, color);
        }
        Component stage = Component.translatable(ArcaneFocusHud.stageKey(current));
        Component remaining = Component.translatable("screen.earth_online_magic.attunement.remaining",
                Math.max(0, (status.remainingTicks() + 19) / 20));
        g.text(font, stage, x, y + 10, ACCENT, false);
        g.text(font, remaining, x + width - font.width(remaining), y + 10, MUTED, false);
    }

    private void drawAmbientMotif(GuiGraphicsExtractor g, int left, int top, int width) {
        long time = Minecraft.getInstance().level == null ? 0L : Minecraft.getInstance().level.getGameTime();
        for (int i = 0; i < 8; i++) {
            int px = left + width - 98 + i * 10;
            int py = top + 8 + (int) Math.round(Math.cos((time + i * 9) * 0.11D) * 3.0D);
            g.fill(px, py, px + 2, py + 2, i % 2 == 0 ? ACCENT : 0xFFE4DEFF);
        }
    }

    private static int fieldColor(int value) {
        if (value < 25) return LOCKED;
        if (value < 50) return 0xFFD6A55B;
        return ACCENT;
    }

    private Component fitted(Component component, int width) {
        String text = component.getString();
        if (font.width(text) <= width) {
            return component;
        }
        return Component.literal(font.plainSubstrByWidth(text, Math.max(0, width - font.width("..."))) + "...");
    }

    private int panelWidth() {
        return Math.min(430, Math.max(304, width - 16));
    }

    private int panelHeight() {
        return Math.min(238, Math.max(166, height - 16));
    }

    private int navigationWidth() {
        return Math.min(118, Math.max(104, panelWidth() / 4 + 12));
    }

    private int panelLeft() {
        return (width - panelWidth()) / 2;
    }

    private int panelTop() {
        return (height - panelHeight()) / 2;
    }
}
