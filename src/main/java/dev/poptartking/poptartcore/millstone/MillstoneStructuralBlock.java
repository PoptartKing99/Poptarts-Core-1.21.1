package dev.poptartking.poptartcore.millstone;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.api.equipment.goggles.IProxyHoveringInformation;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class MillstoneStructuralBlock extends DirectionalBlock implements IProxyHoveringInformation {
    public static final BooleanProperty TOP = BooleanProperty.create("top");
    public static final BooleanProperty CORNER = BooleanProperty.create("corner");
    private static final VoxelShape SLAB = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
    private static final VoxelShape TOP_NORTH_EDGE = Block.box(0.0, 0.0, 2.0, 16.0, 8.0, 16.0);
    private static final VoxelShape TOP_SOUTH_EDGE = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 14.0);
    private static final VoxelShape TOP_EAST_EDGE = Block.box(0.0, 0.0, 0.0, 14.0, 8.0, 16.0);
    private static final VoxelShape TOP_WEST_EDGE = Block.box(2.0, 0.0, 0.0, 16.0, 8.0, 16.0);
    private static final VoxelShape TOP_NE_CORNER =
            Shapes.or(Block.box(0.0, 0.0, 8.0, 14.0, 8.0, 16.0), Block.box(0.0, 0.0, 2.0, 8.0, 8.0, 8.0));
    private static final VoxelShape TOP_NW_CORNER =
            Shapes.or(Block.box(2.0, 0.0, 8.0, 16.0, 8.0, 16.0), Block.box(8.0, 0.0, 2.0, 16.0, 8.0, 8.0));
    private static final VoxelShape TOP_SE_CORNER =
            Shapes.or(Block.box(0.0, 0.0, 0.0, 14.0, 8.0, 8.0), Block.box(0.0, 0.0, 8.0, 8.0, 8.0, 14.0));
    private static final VoxelShape TOP_SW_CORNER =
            Shapes.or(Block.box(2.0, 0.0, 0.0, 16.0, 8.0, 8.0), Block.box(8.0, 0.0, 8.0, 16.0, 8.0, 14.0));

    public MillstoneStructuralBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                (((this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(TOP, false))
                        .setValue(CORNER, false));
    }

    protected MapCodec<? extends DirectionalBlock> codec() {
        return simpleCodec(MillstoneStructuralBlock::new);
    }

    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(new Property[] {FACING, TOP, CORNER});
    }

    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!state.getValue(TOP)) {
            return Shapes.block();
        }

        Direction facing = state.getValue(FACING);
        if (state.getValue(CORNER)) {
            return switch (facing) {
                case SOUTH -> TOP_NE_CORNER;
                case EAST -> TOP_NW_CORNER;
                case WEST -> TOP_SE_CORNER;
                case NORTH -> TOP_SW_CORNER;
                default -> SLAB;
            };
        } else {
            return switch (facing) {
                case SOUTH -> TOP_NORTH_EDGE;
                case EAST -> TOP_WEST_EDGE;
                case WEST -> TOP_EAST_EDGE;
                case NORTH -> TOP_SOUTH_EDGE;
                default -> SLAB;
            };
        }
    }

    @Nullable
    public static BlockPos getMaster(BlockGetter level, BlockPos pos, BlockState state) {
        if (level instanceof Level world && !world.hasChunkAt(pos)) {
            return null;
        }
        BlockPos cursor = pos;
        BlockState cursorState = state;
        if (cursorState.getBlock() instanceof MillstoneStructuralBlock && cursorState.getValue(TOP)) {
            cursor = cursor.below();
            if (level instanceof Level world && !world.hasChunkAt(cursor)) {
                return null;
            }
            cursorState = level.getBlockState(cursor);
        }

        for (int i = 0; i < 4; i++) {
            if (cursorState.getBlock() instanceof MillstoneBlock) {
                return cursor;
            }

            if (!(cursorState.getBlock() instanceof MillstoneStructuralBlock)) {
                return null;
            }

            cursor = cursor.relative(cursorState.getValue(FACING));
            if (level instanceof Level world && !world.hasChunkAt(cursor)) {
                return null;
            }
            cursorState = level.getBlockState(cursor);
        }

        return cursorState.getBlock() instanceof MillstoneBlock ? cursor : null;
    }

    public boolean stillValid(BlockGetter level, BlockPos pos, BlockState state) {
        return state.is(this) && getMaster(level, pos, state) != null;
    }

    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockPos master = getMaster(level, pos, state);
        if (master != null && !level.isClientSide) {
            level.destroyBlock(master, !player.isCreative());
        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock()) && !movedByPiston) {
            level.invalidateCapabilities(pos);
            BlockPos master = getMaster(level, pos, state);
            if (master != null) {
                level.destroyBlock(master, true);
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos) {
        if (!this.stillValid(level, pos, state) && !level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, 1);
        }

        return state;
    }

    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.hasChunksAt(pos.offset(-3, -1, -3), pos.offset(3, 1, 3))) {
            level.scheduleTick(pos, this, 20);
            return;
        }
        if (!this.stillValid(level, pos, state)) {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
    }

    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult) {
        if (state.getValue(TOP)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        BlockPos master = getMaster(level, pos, state);
        return master != null && level.getBlockEntity(master) instanceof MillstoneBlockEntity millstone
                ? millstone.insertByHand(player, hand, stack)
                : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (state.getValue(TOP)) {
            return InteractionResult.PASS;
        }

        BlockPos master = getMaster(level, pos, state);
        return master != null && level.getBlockEntity(master) instanceof MillstoneBlockEntity millstone
                ? millstone.extractByHand(player)
                : InteractionResult.PASS;
    }

    public ItemStack getCloneItemStack(
            BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        return new ItemStack(PoptartCoreBlocks.MILLSTONE.get());
    }

    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (state.getValue(TOP)) {
            BlockPos master = getMaster(level, pos, state);
            if (master != null) {
                MillstoneRotorBlock.turnEntity(level, master.offset(MillstoneStructure.ROTOR_OFFSET), pos, entity);
            }
        }
    }

    public BlockPos getInformationSource(Level level, BlockPos pos, BlockState state) {
        BlockPos master = getMaster(level, pos, state);
        return master == null ? pos : master.offset(MillstoneStructure.ROTOR_OFFSET);
    }

    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }
}
