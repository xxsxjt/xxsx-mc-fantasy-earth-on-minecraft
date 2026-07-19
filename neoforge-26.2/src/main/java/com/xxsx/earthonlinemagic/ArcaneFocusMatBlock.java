package com.xxsx.earthonlinemagic;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ArcaneFocusMatBlock extends Block {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 3.0D, 15.0D);

    public ArcaneFocusMatBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return sit(state, level, pos, player);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hitResult) {
        return sit(state, level, pos, player);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    private InteractionResult sit(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS_SERVER;
        }
        if (ArcanaPower.getMagicResearchLevel(serverPlayer) <= 0) {
            serverPlayer.sendSystemMessage(Component.translatable(
                    "message.earth_online_magic.practice.requires_initiation")
                    .withStyle(ChatFormatting.RED));
            return InteractionResult.SUCCESS_SERVER;
        }
        ArcaneSeatEntity seat = findSeat(serverLevel, pos);
        if (seat != null && seat.isVehicle()) {
            if (seat.getFirstPassenger() == serverPlayer) {
                ArcaneNetwork.sync(serverPlayer, pos, true);
                return InteractionResult.SUCCESS_SERVER;
            }
            serverPlayer.sendSystemMessage(Component.translatable("message.earth_online_magic.arcane_focus_mat.occupied")
                    .withStyle(ChatFormatting.YELLOW));
            return InteractionResult.SUCCESS_SERVER;
        }
        if (serverPlayer.isPassenger()) {
            serverPlayer.stopRiding();
        }

        boolean created = false;
        if (seat == null) {
            seat = new ArcaneSeatEntity(EarthOnlineMagic.ARCANE_SEAT.get(), serverLevel);
            seat.bindTo(pos);
            if (!serverLevel.addFreshEntity(seat)) {
                return InteractionResult.SUCCESS_SERVER;
            }
            created = true;
        }
        if (!serverPlayer.startRiding(seat, true, true)) {
            if (created) {
                seat.discard();
            }
            return InteractionResult.SUCCESS_SERVER;
        }

        Direction facing = state.getValue(FACING);
        serverPlayer.setYRot(facing.toYRot());
        serverPlayer.setYHeadRot(facing.toYRot());
        serverPlayer.setYBodyRot(facing.toYRot());
        serverLevel.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.PLAYERS, 0.5F, 0.9F);

        serverPlayer.sendSystemMessage(Component.translatable("message.earth_online_magic.arcane_focus_mat.sit")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        performSeatedFocus(serverLevel, pos, serverPlayer, false);
        ArcaneNetwork.sync(serverPlayer, pos, true);
        return InteractionResult.SUCCESS_SERVER;
    }

    private static ArcaneSeatEntity findSeat(ServerLevel level, BlockPos pos) {
        AABB box = new AABB(pos).inflate(0.25D);
        for (ArcaneSeatEntity seat : level.getEntities(EntityTypeTest.forClass(ArcaneSeatEntity.class), box, seat -> seat.isBoundTo(pos))) {
            return seat;
        }
        return null;
    }

    public static boolean performSeatedFocus(ServerLevel level, BlockPos pos, ServerPlayer serverPlayer, boolean quietCooldown) {
        return ArcanePractice.perform(level, pos, serverPlayer,
                ArcanePractice.Support.FOCUS_MAT, quietCooldown);
    }
}
