package dev.poptartking.poptartcore.backpack;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BackpackBlock extends BaseEntityBlock {
    private static final VoxelShape[] SHAPES = rotations(net.minecraft.world.phys.shapes.Shapes.or(
            Block.box(3, 0, 6, 13, 15, 12), Block.box(3, 0, 12, 13, 7, 14),
            Block.box(13, 0, 6, 15, 8, 12), Block.box(1, 0, 6, 3, 8, 12)));

    protected static VoxelShape[] rotations(VoxelShape south) {
        var shapes = new VoxelShape[4];
        shapes[0] = south;
        for (int turn = 1; turn < 4; turn++) {
            var rotated = net.minecraft.world.phys.shapes.Shapes.empty();
            for (var box : shapes[turn - 1].toAabbs()) {
                rotated = net.minecraft.world.phys.shapes.Shapes.or(
                        rotated,
                        net.minecraft.world.phys.shapes.Shapes.box(
                                1 - box.maxZ, box.minY, box.minX, 1 - box.minZ, box.maxY, box.maxX));
            }
            shapes[turn] = rotated;
        }
        return shapes;
    }

    private final int rows;
    public static final MapCodec<BackpackBlock> CODEC = simpleCodec(BackpackBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final net.minecraft.world.level.block.state.properties.BooleanProperty DYED =
            net.minecraft.world.level.block.state.properties.BooleanProperty.create("dyed");
    public static final net.minecraft.world.level.block.state.properties.BooleanProperty BODY_DYED =
            net.minecraft.world.level.block.state.properties.BooleanProperty.create("body_dyed");

    public BackpackBlock(Properties properties) {
        this(properties, 3);
    }

    protected BackpackBlock(Properties properties, int rows) {
        super(properties);
        this.rows = rows;
        registerDefaultState(stateDefinition
                .any()
                .setValue(FACING, Direction.NORTH)
                .setValue(DYED, false)
                .setValue(BODY_DYED, false));
    }

    public int rows() {
        return rows;
    }

    public BlockPos origin(BlockState state, BlockPos pos) {
        return pos;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, DYED, BODY_DYED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(DYED, context.getItemInHand().has(net.minecraft.core.component.DataComponents.DYED_COLOR))
                .setValue(BODY_DYED, context.getItemInHand().has(BackpackColors.BODY.get()));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(FACING).get2DDataValue()];
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BackpackBlockEntity(pos, state);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide
                && player.getAbilities().instabuild
                && level.getBlockEntity(pos) instanceof BackpackBlockEntity backpack
                && (!backpack.isEmpty()
                        || !backpack.attachments()
                                .equals(net.minecraft.world.item.component.ItemContainerContents.EMPTY))) {
            var stack = new net.minecraft.world.item.ItemStack(this);
            stack.applyComponents(backpack.collectComponents());
            Block.popResource(level, pos, stack);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.isSpectator()) return InteractionResult.PASS;
        if (player.isShiftKeyDown() && player.getMainHandItem().isEmpty()) return InteractionResult.CONSUME;
        if (!level.isClientSide && level.getBlockEntity(origin(state, pos)) instanceof BackpackBlockEntity backpack)
            player.openMenu(backpack);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
