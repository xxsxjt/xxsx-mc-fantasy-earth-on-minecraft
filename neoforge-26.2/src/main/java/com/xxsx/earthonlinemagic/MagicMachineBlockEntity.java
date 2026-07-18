package com.xxsx.earthonlinemagic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;
import java.util.Optional;

public class MagicMachineBlockEntity extends BaseContainerBlockEntity {
    public static final int SLOT_PRIMARY = 0;
    public static final int SLOT_REAGENT = 1;
    public static final int INPUT_SLOT_COUNT = 2;
    public static final int SLOT_OUTPUT_START = 2;
    public static final int OUTPUT_SLOT_COUNT = 3;
    public static final int SLOT_COUNT = SLOT_OUTPUT_START + OUTPUT_SLOT_COUNT;
    public static final int DATA_COUNT = 8;
    private static final int INVENTORY_VERSION = 2;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> machineProcessTime();
                case 2 -> redstoneMode.id;
                case 3 -> active ? 1 : 0;
                case 4 -> fieldValue;
                case 5 -> (int) Math.round(disturbance);
                case 6 -> structureTier;
                case 7 -> processState.id;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = Math.max(0, value);
                case 2 -> redstoneMode = RedstoneMode.byId(value);
                case 3 -> active = value != 0;
                case 4 -> fieldValue = Math.max(0, value);
                case 5 -> disturbance = Math.max(0, value);
                case 6 -> structureTier = Math.max(0, value);
                case 7 -> processState = ProcessState.byId(value);
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int progress;
    private RedstoneMode redstoneMode = RedstoneMode.ALWAYS;
    private boolean active;
    private int fieldValue;
    private double disturbance;
    private int structureTier;
    private ProcessState processState = ProcessState.MISSING_INPUT;
    private String currentRecipeId = "";

    public MagicMachineBlockEntity(BlockPos pos, BlockState state) {
        super(EarthOnlineMagic.MAGIC_MACHINE_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MagicMachineBlockEntity machine) {
        boolean changed = machine.active;
        machine.active = false;
        AetherChunkField.Reading reading = AetherChunkField.read(level, pos);
        machine.fieldValue = reading.value();
        machine.disturbance = reading.disturbance();
        machine.structureTier = MagicStructures.tierFor(machine.kind(), level, pos);

        if (!machine.redstoneMode.allows(level.hasNeighborSignal(pos))) {
            machine.processState = ProcessState.REDSTONE_PAUSED;
            machine.finishTick(level, pos, state, changed);
            return;
        }
        ItemStack primary = machine.items.get(SLOT_PRIMARY);
        ItemStack reagent = machine.items.get(SLOT_REAGENT);
        if (primary.isEmpty() || reagent.isEmpty()) {
            machine.processState = ProcessState.MISSING_INPUT;
        }
        Optional<MagicMachineBlock.Recipe> match = MagicMachineBlock.findRecipe(machine.kind(), primary, reagent);
        if (match.isEmpty()) {
            if (!primary.isEmpty() && !reagent.isEmpty()) {
                machine.processState = ProcessState.INVALID_COMBINATION;
            }
            if (machine.progress != 0) {
                machine.progress = 0;
                changed = true;
            }
            machine.currentRecipeId = "";
            machine.finishTick(level, pos, state, changed);
            return;
        }
        MagicMachineBlock.Recipe recipe = match.get();
        if (!recipe.id().equals(machine.currentRecipeId)) {
            machine.progress = 0;
            machine.currentRecipeId = recipe.id();
            changed = true;
        }
        int requiredField = machine.effectiveMinField(recipe);
        if (reading.value() < requiredField) {
            machine.processState = ProcessState.FIELD_LOW;
            machine.finishTick(level, pos, state, changed);
            return;
        }
        if (!machine.canFitOutputs(recipe.outputStacks())) {
            machine.processState = ProcessState.OUTPUT_FULL;
            machine.finishTick(level, pos, state, changed);
            return;
        }
        machine.active = true;
        machine.processState = ProcessState.RUNNING;
        machine.progress++;
        changed = true;
        machine.emitWorkingEffects(level, pos);
        if (machine.progress >= machine.machineProcessTime()) {
            machine.consumeInput(SLOT_PRIMARY, recipe.primary().count());
            machine.consumeInput(SLOT_REAGENT, recipe.reagent().count());
            machine.insertOutputs(recipe.outputStacks());
            AetherChunkField.disturb(level, pos, Math.max(1.0D, requiredField / (machine.structureTier > 0 ? 10.0D : 8.0D)));
            machine.emitCompletionEffects(level, pos);
            machine.progress = 0;
        }
        machine.finishTick(level, pos, state, changed);
    }

    public MagicMachineBlock.Kind kind() {
        if (getBlockState().getBlock() instanceof MagicMachineBlock block) {
            return block.kind();
        }
        return MagicMachineBlock.Kind.ALCHEMY_TABLE;
    }

    public ContainerData data() {
        return data;
    }

    public void setRedstoneMode(RedstoneMode mode) {
        if (redstoneMode != mode) {
            redstoneMode = mode;
            setChanged();
        }
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot >= SLOT_PRIMARY && slot <= SLOT_REAGENT
                && MagicMachineBlock.acceptsInput(kind(), slot, stack);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable(kind().displayNameKey());
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new MagicMachineMenu(containerId, inventory, this, data);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
        if (input.getIntOr("InventoryVersion", 0) < INVENTORY_VERSION) {
            ItemStack oldOutput0 = this.items.get(1);
            ItemStack oldOutput1 = this.items.get(2);
            ItemStack oldOutput2 = this.items.get(3);
            this.items.set(SLOT_REAGENT, ItemStack.EMPTY);
            this.items.set(SLOT_OUTPUT_START, oldOutput0);
            this.items.set(SLOT_OUTPUT_START + 1, oldOutput1);
            this.items.set(SLOT_OUTPUT_START + 2, oldOutput2);
        }
        this.progress = input.getIntOr("Progress", 0);
        this.redstoneMode = RedstoneMode.byId(input.getIntOr("RedstoneMode", 0));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        output.putInt("Progress", progress);
        output.putInt("RedstoneMode", redstoneMode.id);
        output.putInt("InventoryVersion", INVENTORY_VERSION);
    }

    private boolean canFitOutputs(List<ItemStack> outputs) {
        NonNullList<ItemStack> simulated = NonNullList.withSize(OUTPUT_SLOT_COUNT, ItemStack.EMPTY);
        for (int i = 0; i < OUTPUT_SLOT_COUNT; i++) {
            simulated.set(i, this.items.get(SLOT_OUTPUT_START + i).copy());
        }
        for (ItemStack output : outputs) {
            if (!fitInto(simulated, output.copy(), 0, simulated.size())) {
                return false;
            }
        }
        return true;
    }

    private void insertOutputs(List<ItemStack> outputs) {
        for (ItemStack output : outputs) {
            fitInto(this.items, output.copy(), SLOT_OUTPUT_START, SLOT_COUNT);
        }
    }

    private static boolean fitInto(NonNullList<ItemStack> stacks, ItemStack output, int start, int end) {
        for (int i = start; i < end && !output.isEmpty(); i++) {
            ItemStack existing = stacks.get(i);
            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, output)) {
                int move = Math.min(output.getCount(), existing.getMaxStackSize() - existing.getCount());
                if (move > 0) {
                    existing.grow(move);
                    output.shrink(move);
                }
            }
        }
        for (int i = start; i < end && !output.isEmpty(); i++) {
            if (stacks.get(i).isEmpty()) {
                int move = Math.min(output.getCount(), output.getMaxStackSize());
                stacks.set(i, output.copyWithCount(move));
                output.shrink(move);
            }
        }
        return output.isEmpty();
    }

    private void finishTick(Level level, BlockPos pos, BlockState state, boolean changed) {
        BlockState nextState = state;
        if (state.hasProperty(MagicMachineBlock.ACTIVE) && state.getValue(MagicMachineBlock.ACTIVE) != active) {
            nextState = nextState.setValue(MagicMachineBlock.ACTIVE, active);
        }
        boolean formed = structureTier > 0;
        if (state.hasProperty(MagicMachineBlock.FORMED) && state.getValue(MagicMachineBlock.FORMED) != formed) {
            nextState = nextState.setValue(MagicMachineBlock.FORMED, formed);
        }
        if (nextState != state) {
            level.setBlock(pos, nextState, Block.UPDATE_CLIENTS);
            changed = true;
        }
        if (changed) {
            setChanged();
        }
    }

    private int machineProcessTime() {
        int base = MagicMachineBlock.findRecipe(kind(), items.get(SLOT_PRIMARY), items.get(SLOT_REAGENT))
                .map(MagicMachineBlock.Recipe::processTicks)
                .orElse(100);
        return structureTier > 0 ? Math.max(40, base * 3 / 4) : base;
    }

    private int effectiveMinField(MagicMachineBlock.Recipe recipe) {
        return effectiveMinField(kind(), recipe.minField(), structureTier);
    }

    public static int effectiveMinField(MagicMachineBlock.Kind kind, int base, int structureTier) {
        if (kind == MagicMachineBlock.Kind.RITUAL_PEDESTAL && structureTier > 0) {
            return Math.max(1, base - 12);
        }
        return base;
    }

    public enum RedstoneMode {
        ALWAYS(0, "screen.earth_online_magic.redstone.always", "screen.earth_online_magic.redstone.always.desc"),
        REQUIRE_SIGNAL(1, "screen.earth_online_magic.redstone.require_signal", "screen.earth_online_magic.redstone.require_signal.desc"),
        REQUIRE_NO_SIGNAL(2, "screen.earth_online_magic.redstone.require_no_signal", "screen.earth_online_magic.redstone.require_no_signal.desc");

        final int id;
        private final String labelKey;
        private final String descriptionKey;

        RedstoneMode(int id, String labelKey, String descriptionKey) {
            this.id = id;
            this.labelKey = labelKey;
            this.descriptionKey = descriptionKey;
        }

        public static RedstoneMode byId(int id) {
            for (RedstoneMode mode : values()) {
                if (mode.id == id) {
                    return mode;
                }
            }
            return ALWAYS;
        }

        public boolean allows(boolean powered) {
            return switch (this) {
                case ALWAYS -> true;
                case REQUIRE_SIGNAL -> powered;
                case REQUIRE_NO_SIGNAL -> !powered;
            };
        }

        public String labelKey() {
            return labelKey;
        }

        public String descriptionKey() {
            return descriptionKey;
        }
    }

    public enum ProcessState {
        MISSING_INPUT(0),
        INVALID_COMBINATION(1),
        FIELD_LOW(2),
        OUTPUT_FULL(3),
        REDSTONE_PAUSED(4),
        RUNNING(5);

        private final int id;

        ProcessState(int id) {
            this.id = id;
        }

        public static ProcessState byId(int id) {
            for (ProcessState state : values()) {
                if (state.id == id) {
                    return state;
                }
            }
            return MISSING_INPUT;
        }
    }

    private void consumeInput(int slot, int count) {
        ItemStack stack = items.get(slot);
        stack.shrink(count);
        if (stack.isEmpty()) {
            items.set(slot, ItemStack.EMPTY);
        }
    }

    private void emitWorkingEffects(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel) || level.getGameTime() % 10L != 0L) {
            return;
        }
        var particle = switch (kind()) {
            case ALCHEMY_TABLE -> ParticleTypes.WITCH;
            case RUNE_CARVING_TABLE -> ParticleTypes.ENCHANT;
            case RITUAL_PEDESTAL -> ParticleTypes.REVERSE_PORTAL;
        };
        serverLevel.sendParticles(particle, pos.getX() + 0.5D, pos.getY() + 1.03D, pos.getZ() + 0.5D,
                kind() == MagicMachineBlock.Kind.RITUAL_PEDESTAL ? 3 : 1,
                0.24D, 0.12D, 0.24D, 0.008D);
    }

    private void emitCompletionEffects(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        switch (kind()) {
            case ALCHEMY_TABLE -> {
                serverLevel.sendParticles(ParticleTypes.WITCH, pos.getX() + 0.5D, pos.getY() + 1.08D, pos.getZ() + 0.5D,
                        12, 0.34D, 0.20D, 0.34D, 0.02D);
                serverLevel.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.7F, 1.15F);
            }
            case RUNE_CARVING_TABLE -> {
                serverLevel.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5D, pos.getY() + 1.08D, pos.getZ() + 0.5D,
                        18, 0.38D, 0.20D, 0.38D, 0.025D);
                serverLevel.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 0.75F, 1.35F);
            }
            case RITUAL_PEDESTAL -> {
                serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, pos.getX() + 0.5D, pos.getY() + 1.15D, pos.getZ() + 0.5D,
                        24, 0.55D, 0.35D, 0.55D, 0.025D);
                serverLevel.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 0.9F, 0.8F);
            }
        }
    }
}
