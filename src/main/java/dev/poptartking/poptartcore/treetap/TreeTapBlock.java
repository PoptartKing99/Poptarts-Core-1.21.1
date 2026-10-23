package dev.poptartking.poptartcore.treetap;

import com.farcr.nomansland.common.registry.items.NMLItems;
import com.mojang.serialization.MapCodec;
import dev.poptartking.poptartcore.registry.PoptartMaterialItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TreeTapBlock extends BaseEntityBlock {
    public enum Wood implements StringRepresentable {
        MAPLE,
        SPRUCE,
        PINE,
        BIRCH,
        ACACIA,
        JUNGLE;

        private static final ResourceLocation MAPLE_LOG =
                ResourceLocation.fromNamespaceAndPath("nomansland", "maple_log");
        private static final ResourceLocation PINE_LOG =
                ResourceLocation.fromNamespaceAndPath("nomansland", "pine_log");

        @Override
        public String getSerializedName() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }

        static Wood fromLog(BlockState state) {
            if (MAPLE_LOG.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()))) return MAPLE;
            if (state.is(Blocks.SPRUCE_LOG)) return SPRUCE;
            if (PINE_LOG.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()))) return PINE;
            if (state.is(Blocks.BIRCH_LOG)) return BIRCH;
            if (state.is(Blocks.ACACIA_LOG)) return ACACIA;
            if (state.is(Blocks.JUNGLE_LOG)) return JUNGLE;
            return null;
        }
    }

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<Wood> WOOD = EnumProperty.create("wood", Wood.class);
    public static final BooleanProperty HAS_BUCKET = BooleanProperty.create("has_bucket");
    public static final BooleanProperty FULL = BooleanProperty.create("full");
    public static final IntegerProperty FILL_LEVEL = IntegerProperty.create("fill_level", 0, 5);
    // Retained for taps saved by the intermittent-drip version.
    public static final BooleanProperty DRIPPING = BooleanProperty.create("dripping");
    static final int MIN_FILL_INTERVAL = 15 * 20;
    static final int MAX_FILL_INTERVAL = 21 * 20;

    static int nextFillDelay(RandomSource random) {
        return MIN_FILL_INTERVAL + random.nextInt(MAX_FILL_INTERVAL - MIN_FILL_INTERVAL + 1);
    }

    static void scheduleNextFill(Level level, BlockPos pos) {
        Block block = level.getBlockState(pos).getBlock();
        if (block instanceof TreeTapBlock && !level.getBlockTicks().hasScheduledTick(pos, block)) {
            level.scheduleTick(pos, block, nextFillDelay(level.random));
        }
    }

    private static final VoxelShape NORTH_SHAPE = Block.box(3, 0, 4, 13, 13, 16);
    private static final VoxelShape SOUTH_SHAPE = Block.box(3, 0, 0, 13, 13, 12);
    private static final VoxelShape EAST_SHAPE = Block.box(0, 0, 3, 12, 13, 13);
    private static final VoxelShape WEST_SHAPE = Block.box(4, 0, 3, 16, 13, 13);

    public TreeTapBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition
                .any()
                .setValue(FACING, Direction.NORTH)
                .setValue(WOOD, Wood.MAPLE)
                .setValue(HAS_BUCKET, false)
                .setValue(FULL, false)
                .setValue(FILL_LEVEL, 0)
                .setValue(DRIPPING, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(TreeTapBlock::new);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TreeTapBlockEntity(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        if (!face.getAxis().isHorizontal()) return null;
        Wood wood = Wood.fromLog(
                context.getLevel().getBlockState(context.getClickedPos().relative(face.getOpposite())));
        return wood == null ? null : defaultBlockState().setValue(FACING, face).setValue(WOOD, wood);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WOOD, HAS_BUCKET, FULL, FILL_LEVEL, DRIPPING);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return Wood.fromLog(
                        level.getBlockState(pos.relative(state.getValue(FACING).getOpposite())))
                == state.getValue(WOOD);
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos) {
        if (direction == state.getValue(FACING).getOpposite() && !canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
            return;
        }
        if (!state.getValue(HAS_BUCKET) || state.getValue(FULL)) return;
        if (level.getBlockEntity(pos) instanceof TreeTapBlockEntity tap) {
            tap.addDrip();
            if (!tap.isFull()) scheduleNextFill(level, pos);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult) {
        if (stack.is(Items.GLASS_BOTTLE)
                && (state.getValue(WOOD) == Wood.MAPLE || state.getValue(WOOD) == Wood.JUNGLE)
                && state.getValue(HAS_BUCKET)
                && state.getValue(FILL_LEVEL) > 0) {
            if (!level.isClientSide) {
                if (!(level.getBlockEntity(pos) instanceof TreeTapBlockEntity tap) || !tap.collectServing()) {
                    return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                }
                if (!player.getAbilities().instabuild) stack.shrink(1);
                ItemStack syrup = new ItemStack(
                        state.getValue(WOOD) == Wood.JUNGLE
                                ? PoptartMaterialItems.LATEX_BOTTLE.get()
                                : NMLItems.MAPLE_SYRUP_BOTTLE.get());
                if (!player.addItem(syrup)) player.drop(syrup, false);
                level.playSound((Player) null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!stack.is(Items.BUCKET) || state.getValue(HAS_BUCKET)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide) {
            level.setBlock(
                    pos,
                    state.setValue(HAS_BUCKET, true)
                            .setValue(FULL, false)
                            .setValue(FILL_LEVEL, 0)
                            .setValue(DRIPPING, false),
                    Block.UPDATE_ALL);
            scheduleNextFill(level, pos);
            if (!player.getAbilities().instabuild) stack.shrink(1);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!state.getValue(HAS_BUCKET)) return InteractionResult.PASS;
        if (!level.isClientSide) {
            if (!(level.getBlockEntity(pos) instanceof TreeTapBlockEntity tap)) return InteractionResult.PASS;
            if (player.isShiftKeyDown()) {
                if (tap.collectedAmount() == 0) {
                    level.setBlock(
                            pos,
                            state.setValue(HAS_BUCKET, false)
                                    .setValue(FULL, false)
                                    .setValue(DRIPPING, false),
                            Block.UPDATE_ALL);
                    ItemStack bucket = new ItemStack(Items.BUCKET);
                    if (!player.addItem(bucket)) player.drop(bucket, false);
                }
                return InteractionResult.SUCCESS;
            }
            int resinCount = tap.collectResin();
            if (resinCount > 0) {
                ItemStack resin = new ItemStack(NMLItems.RESIN.get(), resinCount);
                if (!player.addItem(resin)) player.drop(resin, false);
                level.playSound((Player) null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS);
                scheduleNextFill(level, pos);
                return InteractionResult.SUCCESS;
            }
            if (state.getValue(FULL)) {
                player.displayClientMessage(
                        Component.translatable(
                                state.getValue(WOOD) == Wood.JUNGLE
                                        ? "message.poptartcore.tree_tap.latex_bottle"
                                        : "message.poptartcore.tree_tap.full"),
                        true);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }
}
