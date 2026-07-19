package com.xxsx.earthonlinemagic;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class MagicMachineMenu extends AbstractContainerMenu {
    public static final int BUTTON_REDSTONE_ALWAYS = 0;
    public static final int BUTTON_REDSTONE_REQUIRE_SIGNAL = 1;
    public static final int BUTTON_REDSTONE_REQUIRE_NO_SIGNAL = 2;
    private static final int PLAYER_INV_START = MagicMachineBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_END = PLAYER_INV_END + 9;

    private final Container container;
    private final ContainerData data;
    private final MagicMachineBlock.Kind kind;

    public MagicMachineMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(containerId, inventory, buf.readBlockPos());
    }

    private MagicMachineMenu(int containerId, Inventory inventory, BlockPos pos) {
        this(containerId, inventory, new SimpleContainer(MagicMachineBlockEntity.SLOT_COUNT),
                new SimpleContainerData(MagicMachineBlockEntity.DATA_COUNT), kindFromClientLevel(inventory, pos));
    }

    public MagicMachineMenu(int containerId, Inventory inventory, MagicMachineBlockEntity machine, ContainerData data) {
        this(containerId, inventory, machine, data, machine.kind());
    }

    private MagicMachineMenu(int containerId, Inventory inventory, Container container, ContainerData data, MagicMachineBlock.Kind kind) {
        super(EarthOnlineMagic.MAGIC_MACHINE_MENU.get(), containerId);
        checkContainerSize(container, MagicMachineBlockEntity.SLOT_COUNT);
        checkContainerDataCount(data, MagicMachineBlockEntity.DATA_COUNT);
        this.container = container;
        this.data = data;
        this.kind = kind;
        this.container.startOpen(inventory.player);
        MachineLayout layout = layoutFor(kind);
        addSlot(new Slot(container, MagicMachineBlockEntity.SLOT_PRIMARY, layout.primaryX, layout.primaryY) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return MagicMachineBlock.acceptsInput(MagicMachineMenu.this.kind, 0, stack);
            }
        });
        addSlot(new Slot(container, MagicMachineBlockEntity.SLOT_REAGENT, layout.reagentX, layout.reagentY) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return MagicMachineBlock.acceptsInput(MagicMachineMenu.this.kind, 1, stack);
            }
        });
        addSlot(new OutputSlot(container, MagicMachineBlockEntity.SLOT_OUTPUT_START, layout.output0X, layout.output0Y));
        addSlot(new OutputSlot(container, MagicMachineBlockEntity.SLOT_OUTPUT_START + 1, layout.output1X, layout.output1Y));
        addSlot(new OutputSlot(container, MagicMachineBlockEntity.SLOT_OUTPUT_START + 2, layout.output2X, layout.output2Y));
        addStandardInventorySlots(inventory, 8, 84);
        addDataSlots(data);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (container instanceof MagicMachineBlockEntity machine && id >= BUTTON_REDSTONE_ALWAYS && id <= BUTTON_REDSTONE_REQUIRE_NO_SIGNAL) {
            machine.setRedstoneMode(MagicMachineBlockEntity.RedstoneMode.byId(id));
            return true;
        }
        return super.clickMenuButton(player, id);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack moved = stack.copy();
        boolean outputSlot = index >= MagicMachineBlockEntity.SLOT_OUTPUT_START
                && index < MagicMachineBlockEntity.SLOT_COUNT;
        if (index < MagicMachineBlockEntity.SLOT_COUNT) {
            if (!moveItemStackTo(stack, PLAYER_INV_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (MagicMachineBlock.acceptsInput(this.kind, 0, stack)) {
            boolean movedInput = moveItemStackTo(stack, MagicMachineBlockEntity.SLOT_PRIMARY,
                    MagicMachineBlockEntity.SLOT_PRIMARY + 1, false);
            if (!movedInput && MagicMachineBlock.acceptsInput(this.kind, 1, stack)) {
                movedInput = moveItemStackTo(stack, MagicMachineBlockEntity.SLOT_REAGENT,
                        MagicMachineBlockEntity.SLOT_REAGENT + 1, false);
            }
            if (!movedInput) {
                return ItemStack.EMPTY;
            }
        } else if (MagicMachineBlock.acceptsInput(this.kind, 1, stack)) {
            if (!moveItemStackTo(stack, MagicMachineBlockEntity.SLOT_REAGENT,
                    MagicMachineBlockEntity.SLOT_REAGENT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < PLAYER_INV_END) {
            if (!moveItemStackTo(stack, PLAYER_INV_END, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, PLAYER_INV_START, PLAYER_INV_END, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (outputSlot && player instanceof ServerPlayer serverPlayer) {
            MagicJourney.complete(serverPlayer, MagicJourney.Milestone.FACILITY_OUTPUT);
        }
        return moved;
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    public MagicMachineBlock.Kind kind() {
        return kind;
    }

    public int progress() {
        return data.get(0);
    }

    public int maxProgress() {
        return Math.max(1, data.get(1));
    }

    public MagicMachineBlockEntity.RedstoneMode redstoneMode() {
        return MagicMachineBlockEntity.RedstoneMode.byId(data.get(2));
    }

    public boolean active() {
        return data.get(3) != 0;
    }

    public int fieldValue() {
        return data.get(4);
    }

    public int disturbance() {
        return data.get(5);
    }

    public int structureTier() {
        return data.get(6);
    }

    public MagicMachineBlockEntity.ProcessState processState() {
        return MagicMachineBlockEntity.ProcessState.byId(data.get(7));
    }

    public static MachineLayout layoutFor(MagicMachineBlock.Kind kind) {
        return switch (kind) {
            case ALCHEMY_TABLE -> new MachineLayout(34, 36, 58, 36, 118, 25, 140, 25, 129, 47);
            case RUNE_CARVING_TABLE -> new MachineLayout(38, 25, 38, 49, 122, 36, 144, 36, 133, 57);
            case RITUAL_PEDESTAL -> new MachineLayout(30, 36, 56, 36, 116, 25, 140, 25, 128, 49);
        };
    }

    private static MagicMachineBlock.Kind kindFromClientLevel(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockState(pos).getBlock() instanceof MagicMachineBlock block) {
            return block.kind();
        }
        return MagicMachineBlock.Kind.ALCHEMY_TABLE;
    }

    private static class OutputSlot extends Slot {
        OutputSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public void onTake(Player player, ItemStack carried) {
            super.onTake(player, carried);
            if (!carried.isEmpty() && player instanceof ServerPlayer serverPlayer) {
                MagicJourney.complete(serverPlayer, MagicJourney.Milestone.FACILITY_OUTPUT);
            }
        }
    }

    public record MachineLayout(int primaryX, int primaryY, int reagentX, int reagentY,
                                int output0X, int output0Y, int output1X, int output1Y,
                                int output2X, int output2Y) {
    }
}
