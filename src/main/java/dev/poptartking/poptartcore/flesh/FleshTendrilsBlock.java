package dev.poptartking.poptartcore.flesh;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.MultifaceSpreader;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class FleshTendrilsBlock extends MultifaceBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<FleshTendrilsBlock> CODEC = simpleCodec(FleshTendrilsBlock::new);
    private final MultifaceSpreader spreader = new MultifaceSpreader(this);

    public FleshTendrilsBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                (BlockState) this.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, false));
    }

    protected MapCodec<FleshTendrilsBlock> codec() {
        return CODEC;
    }

    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[] {BlockStateProperties.WATERLOGGED});
    }

    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos) {
        boolean waterlogged = (Boolean) state.getValue(BlockStateProperties.WATERLOGGED);
        if (waterlogged) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        BlockState updated = super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        return waterlogged && updated.isAir() ? Blocks.WATER.defaultBlockState() : updated;
    }

    protected FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED)
                ? Fluids.WATER.getSource(false)
                : super.getFluidState(state);
    }

    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getFluidState().isEmpty();
    }

    public MultifaceSpreader getSpreader() {
        return this.spreader;
    }
}
