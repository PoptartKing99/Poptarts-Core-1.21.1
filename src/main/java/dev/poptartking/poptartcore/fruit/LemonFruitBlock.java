package dev.poptartking.poptartcore.fruit;

import com.mojang.serialization.MapCodec;
import dev.poptartking.poptartcore.registry.PoptartFruitBlocks;
import dev.poptartking.poptartcore.registry.PoptartMaterialItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class LemonFruitBlock extends Block implements BonemealableBlock {
    public static final MapCodec<LemonFruitBlock> CODEC = simpleCodec(LemonFruitBlock::new);
    public static final IntegerProperty AGE = BlockStateProperties.AGE_4;
    public static final int RIPE_AGE = 4;

    private static final VoxelShape SMALL = shape(3);
    private static final VoxelShape MEDIUM = shape(4);
    private static final VoxelShape LARGE = shape(5);
    private static final VoxelShape[] SHAPES = {SMALL, MEDIUM, MEDIUM, LARGE, LARGE};

    public LemonFruitBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    public MapCodec<LemonFruitBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        var offset = state.getOffset(level, pos);
        return SHAPES[state.getValue(AGE)].move(offset.x, offset.y, offset.z);
    }

    @Override
    protected float getMaxVerticalOffset() {
        // The stem extends three model pixels into the supporting leaf.
        return 3.0F / 16.0F;
    }

    private static VoxelShape shape(int bodyHeight) {
        // The stem extends into the leaf above; only the fruit itself is selectable.
        int bottom = 15 - bodyHeight;
        return Shapes.or(
                box(5.5, bottom, 7.5, 9.5, 15, 11.5),
                box(6.5, 15, 8.5, 8.5, 16, 10.5),
                box(6.5, bottom - 1, 8.5, 8.5, bottom, 10.5));
    }

    public static boolean isRipe(BlockState state) {
        return state.getValue(AGE) == RIPE_AGE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.above()).is(PoptartFruitBlocks.FLOWERING_LEMON_LEAVES.get());
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighbor,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos) {
        if (direction == Direction.UP && !neighbor.is(PoptartFruitBlocks.FLOWERING_LEMON_LEAVES.get())) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!isRipe(state)) return InteractionResult.PASS;
        if (!level.isClientSide) {
            BlockState current = level.getBlockState(pos);
            if (!current.is(this) || !isRipe(current) || !level.removeBlock(pos, false)) {
                return InteractionResult.PASS;
            }

            ItemStack lemon = new ItemStack(PoptartMaterialItems.LEMON.get());
            if (!player.addItem(lemon)) popResource(level, pos, lemon);
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.3F, 1.1F);
            level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(PoptartMaterialItems.LEMON.get());
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return !isRipe(state) && state.canSurvive(level, pos);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        BlockState current = level.getBlockState(pos);
        if (current.is(this) && isValidBonemealTarget(level, pos, current)) {
            level.setBlock(pos, current.cycle(AGE), Block.UPDATE_ALL);
        }
    }
}
