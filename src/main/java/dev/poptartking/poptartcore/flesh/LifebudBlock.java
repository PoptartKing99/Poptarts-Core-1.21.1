package dev.poptartking.poptartcore.flesh;

import dev.poptartking.poptartcore.registry.PoptartCoreSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class LifebudBlock extends Block {
    public static final BooleanProperty ROOTED = BooleanProperty.create("rooted");
    private static final VoxelShape SHAPE = Block.box(5, 0, 5, 11, 6, 11);

    public LifebudBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(ROOTED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ROOTED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(
                        ROOTED,
                        rootsInto(context.getLevel()
                                .getBlockState(context.getClickedPos().below())));
    }

    public static boolean rootsInto(BlockState state) {
        return state.getBlock() instanceof DecayingFleshBlock;
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos) {
        return direction == Direction.DOWN ? state.setValue(ROOTED, rootsInto(neighborState)) : state;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            level.playSound(
                    null,
                    pos,
                    PoptartCoreSounds.FLESH_GEODE_BREATHING.get(),
                    SoundSource.BLOCKS,
                    1.0F,
                    0.7F + level.getRandom().nextFloat() * 0.2F);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
