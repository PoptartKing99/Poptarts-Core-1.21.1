package dev.poptartking.poptartcore.decoration;

import com.farcr.nomansland.common.registry.items.NMLItems;
import com.mojang.serialization.MapCodec;
import dev.poptartking.poptartcore.registry.PoptartCoreFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

/** Two-block log-mounted tapper that collects resin or maple syrup. */
public final class TreeTapperBlock extends Block implements net.minecraft.world.level.block.EntityBlock {
    private static final int RESIN_PER_ITEM = 100;
    private static final int RESIN_PER_HARVEST = 200;
    private static final int SYRUP_PER_BOTTLE = 200;
    public static final MapCodec<TreeTapperBlock> CODEC = simpleCodec(TreeTapperBlock::new);
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

    public TreeTapperBlock(Properties properties) {
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
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        if (state.getValue(HALF) != DoubleBlockHalf.LOWER
                || !stack.is(Items.GLASS_BOTTLE)
                || !(level.getBlockEntity(pos) instanceof TreeTapperBlockEntity tapper)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        var fluid = tapper.storedFluid();
        if (!fluid.is(PoptartCoreFluids.MAPLE_SYRUP.source().get()) || fluid.getAmount() < SYRUP_PER_BOTTLE) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide) {
            tapper.drainHandler().drain(SYRUP_PER_BOTTLE, IFluidHandler.FluidAction.EXECUTE);
            player.setItemInHand(
                    hand,
                    ItemUtils.createFilledResult(stack, player, new ItemStack(NMLItems.MAPLE_SYRUP_BOTTLE.get())));
            level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected net.minecraft.world.InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.phys.BlockHitResult hit) {
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            var held = player.getMainHandItem();
            if ((!held.isEmpty() && !held.is(NMLItems.RESIN.get()))
                    || !(level.getBlockEntity(pos) instanceof TreeTapperBlockEntity tapper)) {
                return net.minecraft.world.InteractionResult.PASS;
            }
            var fluid = tapper.storedFluid();
            if (!fluid.is(PoptartCoreFluids.RESIN.source().get()) || fluid.getAmount() < RESIN_PER_HARVEST) {
                return net.minecraft.world.InteractionResult.PASS;
            }
            if (!level.isClientSide) {
                tapper.drainHandler().drain(RESIN_PER_HARVEST, IFluidHandler.FluidAction.EXECUTE);
                var resin = new ItemStack(NMLItems.RESIN.get(), RESIN_PER_HARVEST / RESIN_PER_ITEM);
                if (!player.addItem(resin)) player.drop(resin, false);
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
        }
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
