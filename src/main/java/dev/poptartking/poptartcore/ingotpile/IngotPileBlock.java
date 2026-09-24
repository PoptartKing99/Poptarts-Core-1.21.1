package dev.poptartking.poptartcore.ingotpile;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class IngotPileBlock extends BaseEntityBlock {
    public static final MapCodec<IngotPileBlock> CODEC = simpleCodec(IngotPileBlock::new);
    public static final IntegerProperty COUNT = IntegerProperty.create("count", 1, 64);

    public IngotPileBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(COUNT, 1));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(COUNT); }

    @Override
    protected RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Block.box(.25, 0, .25, 15.75, ((state.getValue(COUNT) + 7) / 8) * 2, 15.75);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.is(this) || below.isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
            net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN && !canSurvive(state, level, pos))
            level.scheduleTick(pos, this, 1);
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canSurvive(state, level, pos)) level.destroyBlock(pos, false);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        if (player.isSecondaryUseActive()) return InteractionResult.PASS;
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof IngotPileBlockEntity pile) {
            ItemStack removed = pile.removeLast();
            if (!removed.isEmpty()) {
                if (!player.addItem(removed)) player.drop(removed, false);
                if (pile.count() == 0) level.removeBlock(pos, false);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moved) {
        if (!state.is(next.getBlock()) && !level.isClientSide
                && level.getBlockEntity(pos) instanceof IngotPileBlockEntity pile) {
            for (ItemStack stack : pile.takeAll()) Block.popResource(level, pos, stack);
        }
        super.onRemove(state, level, pos, next, moved);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player,
            boolean willHarvest, FluidState fluid) {
        if (player.getAbilities().instabuild && level.getBlockEntity(pos) instanceof IngotPileBlockEntity pile)
            pile.takeAll();
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level,
            BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof IngotPileBlockEntity pile && !pile.ingots().isEmpty())
            return pile.ingots().getLast().copy();
        return ItemStack.EMPTY;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new IngotPileBlockEntity(pos, state); }
}
