package com.xxsx.earthonlinemagic.client;

import com.xxsx.earthonlinemagic.MagicMachineBlock;
import com.xxsx.earthonlinemagic.MagicMachineBlockEntity;
import com.xxsx.earthonlinemagic.MagicMachineMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Optional;

public class MagicMachineScreen extends AbstractContainerScreen<MagicMachineMenu> {
    private static final int INVENTORY_TOP = 84;
    private static final int WARNING = 0xFFE07A55;
    private static final int OK = 0xFF65B7A7;
    private static final int MUTED = 0xFF8D9194;

    public MagicMachineScreen(MagicMachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 74;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 8;
        addRenderableWidget(Button.builder(Component.empty(), b -> cycleRedstoneMode())
                .bounds(this.leftPos + 7, this.topPos + 59, 18, 18)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("screen.earth_online_magic.button.handbook"), button -> {
                    this.onClose();
                    EarthOnlineMagicClient.openHandbook();
                })
                .bounds(this.leftPos + this.imageWidth - 42, this.topPos + 4, 34, 14)
                .build());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractBackground(g, mouseX, mouseY, delta);
        Palette palette = palette();
        int x = this.leftPos;
        int y = this.topPos;
        g.fill(x, y, x + this.imageWidth, y + this.imageHeight, palette.frame);
        g.fill(x + 2, y + 2, x + this.imageWidth - 2, y + this.imageHeight - 2, palette.base);
        g.fill(x + 4, y + 19, x + this.imageWidth - 4, y + 72, palette.workArea);
        g.outline(x, y, this.imageWidth, this.imageHeight, palette.outline);
        g.fill(x + 4, y + 80, x + this.imageWidth - 4, y + 81, palette.divider);
        drawMachineInstrument(g, palette);
        drawMachineSlots(g, palette);
        drawInventorySlots(g, palette);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        drawRedstoneIcon(g);
        drawStatus(g);
        drawTooltips(g, mouseX, mouseY);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        Palette palette = palette();
        g.text(this.font, trimmedTitle(), this.titleLabelX, this.titleLabelY, palette.title, false);
        g.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, palette.inventoryText, false);
    }

    private void drawMachineInstrument(GuiGraphicsExtractor g, Palette palette) {
        int progress = Math.min(100, 100 * this.menu.progress() / this.menu.maxProgress());
        switch (this.menu.kind()) {
            case ALCHEMY_TABLE -> drawAlchemyApparatus(g, palette, progress);
            case RUNE_CARVING_TABLE -> drawRunePlate(g, palette, progress);
            case RITUAL_PEDESTAL -> drawRitualRing(g, palette, progress);
        }
    }

    private void drawAlchemyApparatus(GuiGraphicsExtractor g, Palette palette, int progress) {
        int x = this.leftPos + 79;
        int y = this.topPos + 25;
        g.fill(x + 2, y + 4, x + 9, y + 29, palette.glass);
        g.fill(x + 16, y + 4, x + 23, y + 29, palette.glass);
        g.outline(x + 2, y + 4, 7, 25, palette.motifEdge);
        g.outline(x + 16, y + 4, 7, 25, palette.motifEdge);
        int liquid = Math.max(1, 19 * progress / 100);
        g.fill(x + 4, y + 27 - liquid, x + 8, y + 27, 0xFF3FB6B1);
        g.fill(x + 18, y + 27 - liquid, x + 22, y + 27, 0xFFD2A548);
        g.fill(x + 7, y + 29, x + 19, y + 33, palette.motifEdge);
        g.fill(x + 11, y + 31, x + 15, y + 38, palette.motifDark);
        if (progress > 0) {
            g.fill(x + 11, y + 7 + (progress / 20) * 4, x + 14, y + 10 + (progress / 20) * 4, 0xFFDAF7F0);
        }
    }

    private void drawRunePlate(GuiGraphicsExtractor g, Palette palette, int progress) {
        int x = this.leftPos + 76;
        int y = this.topPos + 24;
        g.fill(x, y, x + 32, y + 39, palette.motifDark);
        g.outline(x, y, 32, 39, palette.motifEdge);
        g.fill(x + 5, y + 5, x + 27, y + 34, palette.plate);
        g.fill(x + 10, y + 10, x + 22, y + 11, palette.rune);
        g.fill(x + 15, y + 9, x + 16, y + 29, palette.rune);
        g.fill(x + 9, y + 27, x + 23, y + 28, palette.rune);
        int scanY = y + 6 + 27 * progress / 100;
        g.fill(x + 4, scanY, x + 28, scanY + 2, 0xFF78D6E4);
    }

    private void drawRitualRing(GuiGraphicsExtractor g, Palette palette, int progress) {
        int cx = this.leftPos + 90;
        int cy = this.topPos + 43;
        g.fill(cx - 20, cy - 20, cx + 21, cy + 21, palette.motifDark);
        g.outline(cx - 20, cy - 20, 41, 41, palette.motifEdge);
        g.outline(cx - 14, cy - 14, 29, 29, palette.rune);
        int[][] nodes = {{0, -15}, {15, 0}, {0, 15}, {-15, 0}, {0, 0}};
        int lit = Math.max(0, Math.min(5, (progress + 19) / 20));
        for (int i = 0; i < nodes.length; i++) {
            int color = i < lit ? 0xFF9DDDE3 : 0xFF59656B;
            int size = i == 4 ? 4 : 2;
            g.fill(cx + nodes[i][0] - size, cy + nodes[i][1] - size,
                    cx + nodes[i][0] + size + 1, cy + nodes[i][1] + size + 1, color);
        }
        if (progress >= 60) {
            g.fill(cx - 1, cy - 11, cx + 2, cy + 12, 0xFFB7E9EA);
            g.fill(cx - 11, cy - 1, cx + 12, cy + 2, 0xFFB7E9EA);
        }
    }

    private void drawMachineSlots(GuiGraphicsExtractor g, Palette palette) {
        MagicMachineMenu.MachineLayout layout = MagicMachineMenu.layoutFor(this.menu.kind());
        drawSlotFrame(g, layout.primaryX(), layout.primaryY(), palette.primarySlot, palette.slotEdge);
        drawSlotFrame(g, layout.reagentX(), layout.reagentY(), palette.reagentSlot, palette.slotEdge);
        drawSlotFrame(g, layout.output0X(), layout.output0Y(), palette.outputSlot, palette.slotEdge);
        drawSlotFrame(g, layout.output1X(), layout.output1Y(), palette.outputSlot, palette.slotEdge);
        drawSlotFrame(g, layout.output2X(), layout.output2Y(), palette.outputSlot, palette.slotEdge);
    }

    private void drawInventorySlots(GuiGraphicsExtractor g, Palette palette) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlotFrame(g, 8 + col * 18, INVENTORY_TOP + row * 18, palette.base, palette.slotEdge);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawSlotFrame(g, 8 + col * 18, INVENTORY_TOP + 58, palette.base, palette.slotEdge);
        }
    }

    private void drawSlotFrame(GuiGraphicsExtractor g, int slotX, int slotY, int fill, int edge) {
        int x = this.leftPos + slotX - 1;
        int y = this.topPos + slotY - 1;
        g.fill(x, y, x + 18, y + 18, edge);
        g.fill(x + 1, y + 1, x + 17, y + 17, fill);
        g.outline(x, y, 18, 18, 0xFF232628);
    }

    private void drawStatus(GuiGraphicsExtractor g) {
        Palette palette = palette();
        int color = statusColor();
        Component status = fitted(statusComponent(), 132);
        int x = this.leftPos + 30;
        int y = this.topPos + 63;
        g.fill(x, y + 2, x + 5, y + 7, color);
        g.text(this.font, status, x + 8, y, color, false);
        g.text(this.font, Component.translatable("screen.earth_online_magic.machine.field",
                this.menu.fieldValue(), this.menu.disturbance()), this.leftPos + 7, this.topPos + 18, palette.secondaryText, false);
        drawStructureIndicator(g);
    }

    private Component statusComponent() {
        Optional<MagicMachineBlock.Recipe> recipe = currentRecipe();
        return switch (this.menu.processState()) {
            case MISSING_INPUT -> Component.translatable("screen.earth_online_magic.machine.missing_inputs");
            case INVALID_COMBINATION -> Component.translatable("screen.earth_online_magic.machine.invalid_combination");
            case FIELD_LOW -> Component.translatable("screen.earth_online_magic.machine.field_low",
                    recipe.map(this::effectiveMinField).orElse(0));
            case OUTPUT_FULL -> Component.translatable("screen.earth_online_magic.machine.output_full");
            case REDSTONE_PAUSED -> Component.translatable("screen.earth_online_magic.machine.redstone_paused");
            case RUNNING -> recipe.map(value -> Component.translatable(value.noteKey()))
                    .orElseGet(() -> Component.translatable("screen.earth_online_magic.machine.running"));
        };
    }

    private int statusColor() {
        return switch (this.menu.processState()) {
            case INVALID_COMBINATION, FIELD_LOW, OUTPUT_FULL -> WARNING;
            case RUNNING -> OK;
            case MISSING_INPUT, REDSTONE_PAUSED -> MUTED;
        };
    }

    private Optional<MagicMachineBlock.Recipe> currentRecipe() {
        return MagicMachineBlock.findRecipe(this.menu.kind(),
                this.menu.getSlot(MagicMachineBlockEntity.SLOT_PRIMARY).getItem(),
                this.menu.getSlot(MagicMachineBlockEntity.SLOT_REAGENT).getItem());
    }

    private void drawRedstoneIcon(GuiGraphicsExtractor g) {
        int x = this.leftPos + 8;
        int y = this.topPos + 60;
        MagicMachineBlockEntity.RedstoneMode redstone = this.menu.redstoneMode();
        g.item(new ItemStack(redstoneIcon(redstone)), x, y);
        if (redstone == MagicMachineBlockEntity.RedstoneMode.REQUIRE_NO_SIGNAL) {
            g.fill(x, y, x + 16, y + 16, 0x99000000);
        }
    }

    private Item redstoneIcon(MagicMachineBlockEntity.RedstoneMode redstone) {
        return switch (redstone) {
            case ALWAYS -> Items.BARRIER;
            case REQUIRE_SIGNAL, REQUIRE_NO_SIGNAL -> Items.REDSTONE_TORCH;
        };
    }

    private void drawTooltips(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        if (isHovering(7, 4, 120, 14, mouseX, mouseY)) {
            g.setComponentTooltipForNextFrame(this.font, List.of(
                    Component.translatable(this.menu.kind().displayNameKey()),
                    Component.translatable("screen.earth_online_magic.machine.role." + this.menu.kind().blockId())
            ), mouseX, mouseY);
        }
        if (isHovering(7, 59, 18, 18, mouseX, mouseY)) {
            MagicMachineBlockEntity.RedstoneMode redstone = this.menu.redstoneMode();
            g.setComponentTooltipForNextFrame(this.font, List.of(
                    Component.translatable("screen.earth_online_magic.redstone.current", Component.translatable(redstone.labelKey())),
                    Component.translatable(redstone.descriptionKey()),
                    Component.translatable("screen.earth_online_magic.redstone.tooltip")
            ), mouseX, mouseY);
        }
        if (this.menu.kind() == MagicMachineBlock.Kind.RITUAL_PEDESTAL && isHovering(157, 18, 11, 10, mouseX, mouseY)) {
            g.setComponentTooltipForNextFrame(this.font, List.of(
                    Component.translatable(structureStatusKey()),
                    Component.translatable("screen.earth_online_magic.structure.ritual.hint")
            ), mouseX, mouseY);
        }
    }

    private void cycleRedstoneMode() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode == null) {
            return;
        }
        int id = switch (this.menu.redstoneMode()) {
            case ALWAYS -> MagicMachineMenu.BUTTON_REDSTONE_REQUIRE_SIGNAL;
            case REQUIRE_SIGNAL -> MagicMachineMenu.BUTTON_REDSTONE_REQUIRE_NO_SIGNAL;
            case REQUIRE_NO_SIGNAL -> MagicMachineMenu.BUTTON_REDSTONE_ALWAYS;
        };
        mc.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
    }

    private Component trimmedTitle() {
        String text = this.title.getString();
        if (this.font.width(text) > 116) {
            return Component.literal(this.font.plainSubstrByWidth(text, 113) + "...");
        }
        return this.title;
    }

    private void drawStructureIndicator(GuiGraphicsExtractor g) {
        if (this.menu.kind() != MagicMachineBlock.Kind.RITUAL_PEDESTAL) {
            return;
        }
        int x = this.leftPos + 159;
        int y = this.topPos + 20;
        int color = this.menu.structureTier() > 0 ? OK : 0xFF68737A;
        g.fill(x - 3, y - 3, x + 8, y + 8, 0xFF14191D);
        g.outline(x - 1, y - 1, 7, 7, color);
        g.fill(x + 2, y + 2, x + 5, y + 5, color);
    }

    private Component fitted(Component component, int width) {
        String text = component.getString();
        if (this.font.width(text) <= width) {
            return component;
        }
        return Component.literal(this.font.plainSubstrByWidth(text, Math.max(0, width - this.font.width("..."))) + "...");
    }

    private int effectiveMinField(MagicMachineBlock.Recipe recipe) {
        return MagicMachineBlockEntity.effectiveMinField(this.menu.kind(), recipe.minField(), this.menu.structureTier());
    }

    private String structureStatusKey() {
        return this.menu.structureTier() > 0
                ? "screen.earth_online_magic.structure.ritual.formed"
                : "screen.earth_online_magic.structure.ritual.portable";
    }

    private Palette palette() {
        return switch (this.menu.kind()) {
            case ALCHEMY_TABLE -> new Palette(0xFF78623B, 0xFFDDD0AC, 0xFFC8BD9E, 0xFF362E22,
                    0xFF9C8659, 0xFF887047, 0xFF5A4A30, 0xFFB8CFCC, 0xFFE4D6AD, 0xFFD9C784,
                    0xFF4A4030, 0xFFF7EFD9, 0xFF6B665A, 0xFF584F3F, 0x99DDF6F2, 0xFF393127, 0xFF2C827D);
            case RUNE_CARVING_TABLE -> new Palette(0xFF4F5D63, 0xFFBFC6C5, 0xFFAEB6B5, 0xFF242A2D,
                    0xFF718086, 0xFF65747B, 0xFF394349, 0xFFB4C3C4, 0xFFD6C28D, 0xFFC7D4D5,
                    0xFF465156, 0xFFF1F5F3, 0xFF59666A, 0xFF4A565A, 0x99D3ECEC, 0xFF394247, 0xFF4C91A0);
            case RITUAL_PEDESTAL -> new Palette(0xFF43525B, 0xFF242B31, 0xFF303940, 0xFF11161A,
                    0xFF5D6D76, 0xFF536671, 0xFF1B2328, 0xFF3E4C54, 0xFF53666E, 0xFF4A5A62,
                    0xFF687982, 0xFFE2EDF0, 0xFF98A7AC, 0xFFBAC5C8, 0x995B7079, 0xFF182126, 0xFF78C5CF);
        };
    }

    private record Palette(int frame, int base, int workArea, int outline, int divider, int motifEdge,
                           int motifDark, int primarySlot, int reagentSlot, int outputSlot, int slotEdge,
                           int title, int secondaryText, int inventoryText, int glass, int plate, int rune) {
    }
}
