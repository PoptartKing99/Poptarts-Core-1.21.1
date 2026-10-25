package dev.poptartking.poptartcore.backpack;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class RucksackBlock extends BackpackBlock {
    private static final VoxelShape MODEL_SHAPE = net.minecraft.world.phys.shapes.Shapes.or(
            Block.box(2, 0, 5, 14, 21, 13), Block.box(3, 0, 13, 13, 11, 15),
            Block.box(14, 2, 6, 17, 10, 12), Block.box(-1, 2, 6, 2, 10, 12),
            Block.box(14, 15, 7, 16, 20, 12), Block.box(0, 15, 7, 2, 20, 12));
    private static final VoxelShape[] LOWER_SHAPES = rotations(MODEL_SHAPE);
    private static final VoxelShape[] UPPER_SHAPES = rotations(MODEL_SHAPE.move(0, -1, 0));
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    private static final MapCodec<RucksackBlock> CODEC = simpleCodec(RucksackBlock::new);

    public RucksackBlock(Properties properties) {
        super(properties, 6);
        registerDefaultState(defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HALF);
    }

    @Override
    public BlockPos origin(BlockState state, BlockPos pos) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var pos = context.getClickedPos();
        if (pos.getY() + 1 >= context.getLevel().getMaxBuildHeight()
                || !context.getLevel().getBlockState(pos.above()).canBeReplaced(context)
                || !context.getLevel()
                        .isUnobstructed(
                                defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER),
                                pos.above(),
                                CollisionContext.of(context.getPlayer()))) return null;
        return super.getStateForPlacement(context).setValue(HALF, DoubleBlockHalf.LOWER);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? super.newBlockEntity(pos, state) : null;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return (state.getValue(HALF) == DoubleBlockHalf.UPPER ? UPPER_SHAPES : LOWER_SHAPES)
                [state.getValue(FACING).get2DDataValue()];
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? List.of() : super.getDrops(state, builder);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!replacement.is(this)) {
            var partner = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos.above();
            if (level.getBlockState(partner).is(this)) {
                if (state.getValue(HALF) == DoubleBlockHalf.UPPER) level.destroyBlock(partner, true);
                else level.removeBlock(partner, false);
            }
        }
        super.onRemove(state, level, pos, replacement, moving);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER && player.getAbilities().instabuild) {
            var lower = pos.below();
            if (level.getBlockState(lower).is(this)) {
                super.playerWillDestroy(level, lower, level.getBlockState(lower), player);
                level.removeBlock(lower, false);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
