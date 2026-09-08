package dev.poptartking.poptartcore.clinker;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class ClinkerPillarBlock extends Block {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<PillarSegment> SEGMENT = EnumProperty.create("segment", PillarSegment.class);
    public static final MapCodec<ClinkerPillarBlock> CODEC = simpleCodec(ClinkerPillarBlock::new);

    public ClinkerPillarBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(
                stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(SEGMENT, PillarSegment.SHORT));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, SEGMENT);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        return defaultBlockState()
                .setValue(
                        FACING,
                        connectedFacing(
                                context.getLevel(),
                                pos,
                                context.getHorizontalDirection().getOpposite()))
                .setValue(SEGMENT, segment(context.getLevel(), pos));
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighbor,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos) {
        return direction.getAxis() == Direction.Axis.Y ? state.setValue(SEGMENT, segment(level, pos)) : state;
    }

    private Direction connectedFacing(BlockGetter level, BlockPos pos, Direction placedFacing) {
        BlockState below = level.getBlockState(pos.below());
        if (below.is(this)) {
            return below.getValue(FACING);
        }

        BlockState above = level.getBlockState(pos.above());
        return above.is(this) ? above.getValue(FACING) : placedFacing;
    }

    private PillarSegment segment(BlockGetter level, BlockPos pos) {
        boolean below = level.getBlockState(pos.below()).is(this);
        boolean above = level.getBlockState(pos.above()).is(this);

        if (below && above) {
            return PillarSegment.SHAFT;
        }
        if (below) {
            return PillarSegment.CAPITAL;
        }
        return above ? PillarSegment.BASE : PillarSegment.SHORT;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
