package dev.poptartking.poptartcore.flesh;

import dev.poptartking.poptartcore.registry.PoptartCoreSounds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LifebudBlock extends Block implements SimpleWaterloggedBlock {
    public static final BooleanProperty ROOTED = BooleanProperty.create("rooted");
    private static final VoxelShape SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 6.0, 11.0);
    private static final Direction[] DIRECTIONS = Direction.values();

    public LifebudBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                (BlockState) ((BlockState) ((BlockState) this.stateDefinition.any()).setValue(ROOTED, false))
                        .setValue(BlockStateProperties.WATERLOGGED, false));
    }

    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(new Property[] {ROOTED, BlockStateProperties.WATERLOGGED});
    }

    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) == 0) {
            FleshOrganisms.Organism organism = FleshOrganisms.of(level, pos);
            if (organism != null && FleshGrowth.hasRoomForTendrils(level, organism, 0.25)) {
                List<BlockPos> options = new ArrayList<>();

                for (int sample = 0; sample < 6; sample++) {
                    BlockPos member = organism.randomMember(random);
                    if (member == null) {
                        break;
                    }

                    BlockPos target = member.relative(DIRECTIONS[random.nextInt(DIRECTIONS.length)]);
                    if (level.isLoaded(target) && FleshGrowth.canSprout(level, target, organism::contains)) {
                        options.add(target);
                    }
                }

                BlockPos best = FleshGrowth.choose(level, options, organism, null, true);
                if (best != null) {
                    FleshGrowth.sprout(level, best, random, 3, organism::contains);
                }
            }
        }
    }

    public static boolean rootsInto(BlockState state) {
        return state.getBlock() instanceof DecayingFleshBlock;
    }

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

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(
                        ROOTED,
                        rootsInto(context.getLevel()
                                .getBlockState(context.getClickedPos().below())))
                .setValue(
                        BlockStateProperties.WATERLOGGED,
                        context.getLevel()
                                .getFluidState(context.getClickedPos())
                                .isSourceOfType(Fluids.WATER));
    }

    protected FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED)
                ? Fluids.WATER.getSource(false)
                : super.getFluidState(state);
    }

    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos) {
        if ((Boolean) state.getValue(BlockStateProperties.WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return direction == Direction.DOWN ? (BlockState) state.setValue(ROOTED, rootsInto(neighborState)) : state;
    }
}
