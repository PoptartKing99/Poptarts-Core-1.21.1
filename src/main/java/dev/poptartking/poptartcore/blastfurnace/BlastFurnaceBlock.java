package dev.poptartking.poptartcore.blastfurnace;

import com.mojang.serialization.MapCodec;
import dev.poptartking.poptartcore.registry.PoptartCoreBlockEntities;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlastFurnaceBlock extends BaseEntityBlock implements WorldlyContainerHolder {
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private static final VoxelShape LOWER_SHAPE = Shapes.block();
    private static final VoxelShape UPPER_SHAPE =
            Shapes.or(Block.box(0, 0, 0, 16, 4, 16), Block.box(1, 4, 1, 15, 14, 15), Block.box(3, 14, 3, 13, 16, 13));
    private static final VoxelShape FULL_SHAPE_LOWER = Shapes.or(LOWER_SHAPE, UPPER_SHAPE.move(0, 1, 0));
    private static final VoxelShape FULL_SHAPE_UPPER = FULL_SHAPE_LOWER.move(0, -1, 0);

    public BlastFurnaceBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition
                .any()
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(BlastFurnaceBlock::new);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HALF, FACING, LIT);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? UPPER_SHAPE : LOWER_SHAPE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? FULL_SHAPE_UPPER : FULL_SHAPE_LOWER;
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? UPPER_SHAPE : LOWER_SHAPE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        return pos.getY() < context.getLevel().getMaxBuildHeight() - 1
                        && context.getLevel().getBlockState(pos.above()).canBeReplaced(context)
                ? defaultBlockState()
                        .setValue(FACING, context.getHorizontalDirection().getOpposite())
                : null;
    }

    @Override
    public void setPlacedBy(
            Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
        level.invalidateCapabilities(pos.above());
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) return true;
        BlockState below = level.getBlockState(pos.below());
        return below.is(this) && below.getValue(HALF) == DoubleBlockHalf.LOWER;
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighbor,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos) {
        boolean towardOtherHalf = direction.getAxis() == Direction.Axis.Y
                && (state.getValue(HALF) == DoubleBlockHalf.LOWER) == (direction == Direction.UP);
        if (!towardOtherHalf) return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
        return neighbor.is(this) && neighbor.getValue(HALF) != state.getValue(HALF)
                ? state
                : Blocks.AIR.defaultBlockState();
    }

    private BlockPos lowerPos(BlockState state, BlockPos pos) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
    }

    @Override
    public boolean onDestroyedByPlayer(
            BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockPos lowerPos = pos.below();
            BlockState lower = level.getBlockState(lowerPos);
            if (lower.is(this) && lower.getValue(HALF) == DoubleBlockHalf.LOWER) {
                // Remove the inventory half without automatic block loot or a recursive upper-half removal.
                level.setBlock(lowerPos, Blocks.AIR.defaultBlockState(), UPDATE_ALL | UPDATE_KNOWN_SHAPE);
            }
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    public void playerDestroy(
            Level level,
            Player player,
            BlockPos pos,
            BlockState state,
            @Nullable BlockEntity blockEntity,
            ItemStack tool) {
        // Minecraft only calls this after a successful harvest with the correct tool, never in Creative.
        super.playerDestroy(
                level, player, lowerPos(state, pos), state.setValue(HALF, DoubleBlockHalf.LOWER), blockEntity, tool);
    }

    @Nullable
    @Override
    public WorldlyContainer getContainer(BlockState state, LevelAccessor level, BlockPos pos) {
        if (level.getBlockEntity(lowerPos(state, pos)) instanceof BlastFurnaceBlockEntity furnace) {
            return state.getValue(HALF) == DoubleBlockHalf.LOWER ? furnace : furnace.topHalf();
        }
        return null;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide
                && level.getBlockEntity(lowerPos(state, pos)) instanceof BlastFurnaceBlockEntity furnace)
            player.openMenu(furnace);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockPos otherHalf = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
            level.invalidateCapabilities(pos);
            level.invalidateCapabilities(otherHalf);

            if (state.getValue(HALF) == DoubleBlockHalf.LOWER
                    && level.getBlockEntity(pos) instanceof BlastFurnaceBlockEntity furnace) {
                Containers.dropContents(level, pos, furnace);
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new BlastFurnaceBlockEntity(pos, state) : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return !level.isClientSide && state.getValue(HALF) == DoubleBlockHalf.LOWER
                ? createTickerHelper(
                        type, PoptartCoreBlockEntities.BLAST_FURNACE.get(), BlastFurnaceBlockEntity::serverTick)
                : null;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT) || state.getValue(HALF) != DoubleBlockHalf.LOWER) return;
        double x = pos.getX() + .5, y = pos.getY() + .6, z = pos.getZ() + .5;
        if (random.nextFloat() < .1F)
            level.playLocalSound(x, y, z, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1, 1, false);
        Direction facing = state.getValue(FACING);
        double offset = random.nextFloat() * .6 - .3;
        double dx = facing.getAxis() == Direction.Axis.X ? facing.getStepX() * .52 : offset;
        double dz = facing.getAxis() == Direction.Axis.Z ? facing.getStepZ() * .52 : offset;
        level.addParticle(ParticleTypes.SMOKE, x + dx, y + random.nextFloat() * .375, z + dz, 0, 0, 0);
        level.addParticle(ParticleTypes.FLAME, x + dx, y + random.nextFloat() * .375, z + dz, 0, 0, 0);
    }
}
