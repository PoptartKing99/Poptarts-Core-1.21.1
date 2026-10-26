package dev.poptartking.poptartcore.decoration;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** Two-block decorative tapper, supported by a log column. */
public final class WoodenBucketBlock extends Block implements net.minecraft.world.level.block.EntityBlock {
    public static final MapCodec<WoodenBucketBlock> CODEC = simpleCodec(WoodenBucketBlock::new);
    public static final net.minecraft.world.level.block.state.properties.BooleanProperty OPEN =
            net.minecraft.world.level.block.state.properties.BooleanProperty.create("open");
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    private static final VoxelShape[] BUCKET = {
        Block.box(3, 0, 0, 13, 14, 10), Block.box(6, 0, 3, 16, 14, 13),
        Block.box(3, 0, 6, 13, 14, 16), Block.box(0, 0, 3, 10, 14, 13)
    };
    private static final VoxelShape[] TAP = {
        Block.box(6, 0, 0, 10, 6, 6), Block.box(10, 0, 6, 16, 6, 10),
        Block.box(6, 0, 10, 10, 6, 16), Block.box(0, 0, 6, 6, 6, 10)
    };

    public WoodenBucketBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition
                .any()
                .setValue(OPEN, false)
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HALF, FACING, OPEN);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        if (!face.getAxis().isHorizontal()) return null;
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() >= level.getMaxBuildHeight() - 1
                || !level.getBlockState(pos.above()).canBeReplaced(context)) return null;
        BlockState state = defaultBlockState().setValue(FACING, face.getOpposite());
        return state.canSurvive(level, pos) ? state : null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos lower = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
        BlockPos log = lower.relative(state.getValue(FACING));
        return level.getBlockState(log).is(BlockTags.LOGS)
                && level.getBlockState(log.above()).is(BlockTags.LOGS);
    }

    @Override
    public void setPlacedBy(
            Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighbor,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos) {
        Direction partner = state.getValue(HALF) == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN;
        if (direction == partner
                && (!neighbor.is(this)
                        || neighbor.getValue(HALF) == state.getValue(HALF)
                        || neighbor.getValue(FACING) != state.getValue(FACING))) return Blocks.AIR.defaultBlockState();
        return state.canSurvive(level, pos) ? state : Blocks.AIR.defaultBlockState();
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player.isCreative() && state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockPos lower = pos.below();
            if (level.getBlockState(lower).is(this)) level.destroyBlock(lower, false);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    @Nullable
    public net.minecraft.world.level.block.entity.BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new TreeTapperBlockEntity(pos, state) : null;
    }

    @Override
    @Nullable
    public <T extends net.minecraft.world.level.block.entity.BlockEntity>
            net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(
                    Level level, BlockState state, net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
        if (level.isClientSide
                || state.getValue(HALF) != DoubleBlockHalf.LOWER
                || type != dev.poptartking.poptartcore.registry.PoptartCoreBlockEntities.TREE_TAPPER.get()) return null;
        return (world, pos, current, entity) ->
                TreeTapperBlockEntity.tick(world, pos, current, (TreeTapperBlockEntity) entity);
    }

    @Override
    protected net.minecraft.world.InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.phys.BlockHitResult hit) {
        if (state.getValue(HALF) != DoubleBlockHalf.UPPER) return net.minecraft.world.InteractionResult.PASS;
        if (!level.isClientSide) {
            boolean open = !state.getValue(OPEN);
            level.setBlock(pos, state.setValue(OPEN, open), Block.UPDATE_ALL);
            BlockPos lower = pos.below();
            BlockState bucket = level.getBlockState(lower);
            if (bucket.is(this)) level.setBlock(lower, bucket.setValue(OPEN, open), Block.UPDATE_ALL);
            level.playSound(
                    null,
                    pos,
                    net.minecraft.sounds.SoundEvents.LEVER_CLICK,
                    net.minecraft.sounds.SoundSource.BLOCKS,
                    .3F,
                    open ? .6F : .5F);
        }
        return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int index =
                switch (state.getValue(FACING)) {
                    case NORTH -> 0;
                    case EAST -> 1;
                    case SOUTH -> 2;
                    case WEST -> 3;
                    default -> throw new IllegalStateException("Tapper facing must be horizontal");
                };
        return (state.getValue(HALF) == DoubleBlockHalf.LOWER ? BUCKET : TAP)[index];
    }
}
