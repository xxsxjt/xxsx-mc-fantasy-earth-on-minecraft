package com.xxsx.earthonlinemagic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class MagicMachineBlock extends Block implements EntityBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    private static final List<Recipe> RECIPES = createRecipes();

    private final Kind kind;

    public MagicMachineBlock(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
        registerDefaultState(stateDefinition.any().setValue(ACTIVE, false).setValue(FORMED, false).setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return openMachineAt(level, pos, player);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hitResult) {
        return openMachineAt(level, pos, player);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MagicMachineBlockEntity(pos, state);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (kind != Kind.RITUAL_PEDESTAL || !state.getValue(FORMED)) {
            return;
        }
        int[][] nodes = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}, {1, -1}, {1, 1}, {-1, 1}, {-1, -1}};
        int[] node = nodes[random.nextInt(nodes.length)];
        double x = pos.getX() + 0.5D + node[0] * 0.82D;
        double z = pos.getZ() + 0.5D + node[1] * 0.82D;
        level.addParticle(random.nextBoolean() ? ParticleTypes.ENCHANT : ParticleTypes.REVERSE_PORTAL,
                x, pos.getY() + 1.05D, z, -node[0] * 0.01D, 0.006D, -node[1] * 0.01D);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return type == EarthOnlineMagic.MAGIC_MACHINE_BLOCK_ENTITY.get()
                ? (tickerLevel, pos, tickerState, blockEntity) -> MagicMachineBlockEntity.serverTick(tickerLevel, pos, tickerState, (MagicMachineBlockEntity) blockEntity)
                : null;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof MagicMachineBlockEntity machine) {
            Containers.dropContents(level, pos, (Container) machine);
            level.updateNeighbourForOutputSignal(pos, this);
        }
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE, FORMED, FACING);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    public Kind kind() {
        return kind;
    }

    public static InteractionResult openMachineAt(Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof MagicMachineBlockEntity machine) {
            serverPlayer.openMenu(machine, buf -> buf.writeBlockPos(pos));
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    public static List<Recipe> recipesFor(Kind kind) {
        return RECIPES.stream().filter(recipe -> recipe.kind == kind && recipe.isAvailable()).toList();
    }

    public static Optional<Recipe> findRecipe(Kind kind, ItemStack primary, ItemStack reagent) {
        if (primary.isEmpty() || reagent.isEmpty()) {
            return Optional.empty();
        }
        return RECIPES.stream()
                .filter(recipe -> recipe.kind == kind && recipe.matches(primary, reagent))
                .findFirst();
    }

    public static boolean acceptsInput(Kind kind, int inputIndex, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return RECIPES.stream()
                .filter(recipe -> recipe.kind == kind)
                .anyMatch(recipe -> recipe.input(inputIndex).matches(stack));
    }

    private static List<Recipe> createRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        recipes.add(r("mana_salt_crystallization", Kind.ALCHEMY_TABLE,
                item(EarthOnlineMagic.ARCANE_DUST::get, 2), item(() -> Items.GLOWSTONE_DUST, 1),
                25, 90, out(EarthOnlineMagic.CRYSTALLIZED_MANA_SALT::get, 2)));
        recipes.add(r("aether_glass_fusion", Kind.ALCHEMY_TABLE,
                item(EarthOnlineMagic.AETHER_CRYSTAL::get, 1), item(() -> Items.GLASS, 1),
                42, 130, out(EarthOnlineMagic.AETHER_GLASS::get, 2)));
        recipes.add(r("starter_arcane_dust", Kind.ALCHEMY_TABLE,
                item(() -> Items.GLOWSTONE_DUST, 1), item(() -> Items.AMETHYST_SHARD, 1),
                20, 80, out(EarthOnlineMagic.ARCANE_DUST::get, 2)));
        recipes.add(r("geological_arcane_dust", Kind.ALCHEMY_TABLE,
                tagged(EarthMaterialTags.ARCANA_GEOLOGY_CATALYSTS, () -> Items.AMETHYST_SHARD, 1),
                item(() -> Items.GLOWSTONE_DUST, 1), 30, 105,
                out(EarthOnlineMagic.ARCANE_DUST::get, 3)));

        recipes.add(r("rune_copper_plate", Kind.RUNE_CARVING_TABLE,
                tagged(EarthMaterialTags.MANA_CONDUCTORS, () -> Items.COPPER_INGOT, 1),
                item(EarthOnlineMagic.RUNE_INK::get, 1), 36, 120,
                out(EarthOnlineMagic.RUNE_COPPER_PLATE::get, 2)));
        recipes.add(r("body_ward_notes", Kind.RUNE_CARVING_TABLE,
                item(EarthOnlineMagic.RUNE_COPPER_PLATE::get, 1), item(EarthOnlineMagic.RUNE_INK::get, 1),
                38, 160, out(EarthOnlineMagic.ARCANE_BODY_WARD_NOTES::get, 1)));
        recipes.add(r("breath_ward_notes", Kind.RUNE_CARVING_TABLE,
                item(EarthOnlineMagic.RUNE_COPPER_PLATE::get, 1), item(EarthOnlineMagic.AETHER_GLASS::get, 1),
                45, 180, out(EarthOnlineMagic.ARCANE_BREATH_WARD_NOTES::get, 1)));
        recipes.add(r("inscribed_ritual_core", Kind.RUNE_CARVING_TABLE,
                item(EarthOnlineMagic.RITUAL_CHALK::get, 1), item(EarthOnlineMagic.RUNE_INK::get, 1),
                40, 150, out(EarthOnlineMagic.DORMANT_RITUAL_CORE::get, 1)));

        recipes.add(r("core_activation", Kind.RITUAL_PEDESTAL,
                item(EarthOnlineMagic.DORMANT_RITUAL_CORE::get, 1), item(EarthOnlineMagic.CRYSTALLIZED_MANA_SALT::get, 1),
                45, 200, out(EarthOnlineMagic.AETHER_CRYSTAL::get, 2)));
        recipes.add(r("core_sealing", Kind.RITUAL_PEDESTAL,
                item(EarthOnlineMagic.AETHER_GLASS::get, 1), item(EarthOnlineMagic.CRYSTALLIZED_MANA_SALT::get, 2),
                50, 180, out(EarthOnlineMagic.DORMANT_RITUAL_CORE::get, 1)));
        recipes.add(r("substrate_condensation", Kind.RITUAL_PEDESTAL,
                tagged(EarthMaterialTags.AETHER_CRYSTAL_SUBSTRATES, () -> Items.AMETHYST_SHARD, 1),
                item(EarthOnlineMagic.CRYSTALLIZED_MANA_SALT::get, 1), 48, 190,
                out(EarthOnlineMagic.AETHER_CRYSTAL::get, 2)));
        return List.copyOf(recipes);
    }

    private static Recipe r(String id, Kind kind, Input primary, Input reagent, int minField,
                            int processTicks, Output... outputs) {
        return new Recipe(id, kind, primary, reagent,
                "recipe.earth_online_magic.machine." + id, minField, processTicks, List.of(outputs));
    }

    private static Input item(Supplier<? extends ItemLike> item, int count) {
        return new Input(null, item, count);
    }

    private static Input tagged(TagKey<Item> tag, Supplier<? extends ItemLike> fallback, int count) {
        return new Input(tag, fallback, count);
    }

    private static Output out(Supplier<? extends ItemLike> item, int count) {
        return new Output(item, count);
    }

    public record Recipe(String id, Kind kind, Input primary, Input reagent, String noteKey,
                         int minField, int processTicks, List<Output> outputs) {
        public boolean matches(ItemStack primaryStack, ItemStack reagentStack) {
            return primary.matchesWithCount(primaryStack) && reagent.matchesWithCount(reagentStack);
        }

        public boolean isAvailable() {
            return primary.isAvailable() && reagent.isAvailable();
        }

        public Input input(int index) {
            return index == 0 ? primary : reagent;
        }

        public List<ItemStack> primaryStacks() {
            return primary.displayStacks();
        }

        public List<ItemStack> reagentStacks() {
            return reagent.displayStacks();
        }

        public List<ItemStack> outputStacks() {
            return outputs.stream().map(Output::stack).toList();
        }
    }

    public record Input(@Nullable TagKey<Item> tag, Supplier<? extends ItemLike> fallback, int count) {
        public boolean matches(ItemStack stack) {
            Item fallbackItem = fallback.get().asItem();
            return stack.getItem() == fallbackItem || (tag != null && stack.is(tag));
        }

        public boolean matchesWithCount(ItemStack stack) {
            return matches(stack) && stack.getCount() >= count;
        }

        public boolean isAvailable() {
            return true;
        }

        public List<ItemStack> displayStacks() {
            Item fallbackItem = fallback.get().asItem();
            List<ItemStack> stacks = new ArrayList<>();
            if (tag != null) {
                for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
                    stacks.add(new ItemStack(holder.value(), count));
                }
            }
            if (stacks.stream().noneMatch(stack -> stack.getItem() == fallbackItem)) {
                stacks.add(new ItemStack(fallbackItem, count));
            }
            return List.copyOf(stacks);
        }
    }

    public record Output(Supplier<? extends ItemLike> item, int count) {
        public ItemStack stack() {
            return new ItemStack(item.get().asItem(), count);
        }
    }

    public enum Kind {
        ALCHEMY_TABLE("alchemy_table", "Alchemy Table", "调和魔尘、盐晶和以太玻璃。"),
        RUNE_CARVING_TABLE("rune_carving_table", "Rune Carving Table", "把符文墨水刻入铜片、石材和仪式部件。"),
        RITUAL_PEDESTAL("ritual_pedestal", "Ritual Pedestal", "承载小型仪式，把材料转化为稳定魔法核心。");

        private final String blockId;
        private final String fallbackName;
        private final String fallbackDescription;

        Kind(String blockId, String fallbackName, String fallbackDescription) {
            this.blockId = blockId;
            this.fallbackName = fallbackName;
            this.fallbackDescription = fallbackDescription;
        }

        public String blockId() {
            return blockId;
        }

        public String displayNameKey() {
            return "block.earth_online_magic." + blockId;
        }

        public String descriptionKey() {
            return "tooltip.earth_online_magic.machine." + blockId + ".description";
        }

        public String localizedDisplayName() {
            return Language.getInstance().getOrDefault(displayNameKey(), fallbackName);
        }

        public String fallbackDescription() {
            return fallbackDescription;
        }
    }
}
