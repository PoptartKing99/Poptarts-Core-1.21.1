package dev.poptartking.poptartcore.anvil;

import com.mojang.serialization.MapCodec;
import dev.poptartking.poptartcore.registry.PoptartWorkstationBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PrimitiveAnvilBlock extends AnvilBlock {
    private static final VoxelShape BASE = Block.box(2.0, 0.0, 2.0, 14.0, 2.0, 14.0);
    private static final VoxelShape X_TOP = Block.box(0.0, 2.0, 2.0, 16.0, 14.0, 14.0);
    private static final VoxelShape Z_TOP = Block.box(2.0, 2.0, 0.0, 14.0, 14.0, 16.0);
    private static final VoxelShape X_AXIS_AABB = Shapes.or(BASE, X_TOP);
    private static final VoxelShape Z_AXIS_AABB = Shapes.or(BASE, Z_TOP);
    private static final Component CONTAINER_TITLE = Component.translatable("menu.title.poptartcore.primitive_anvil");

    public PrimitiveAnvilBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends FallingBlock> codec() {
        return simpleCodec(PrimitiveAnvilBlock::new);
    }

    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider(
                (containerId, playerInventory, player) ->
                        new AnvilMenu(containerId, playerInventory, ContainerLevelAccess.create(level, pos)),
                CONTAINER_TITLE);
    }

    @Override
    public void maybeDamage(Level level, BlockPos pos, BlockState state, RandomSource random) {
        if (random.nextFloat() >= 0.25F) return;
        Block newBlock = state.is((Block) PoptartWorkstationBlocks.PRIMITIVE_ANVIL.get())
                ? (Block) PoptartWorkstationBlocks.CHIPPED_PRIMITIVE_ANVIL.get()
                : (state.is((Block) PoptartWorkstationBlocks.CHIPPED_PRIMITIVE_ANVIL.get())
                        ? (Block) PoptartWorkstationBlocks.DAMAGED_PRIMITIVE_ANVIL.get()
                        : null);
        if (newBlock == null) {
            level.setBlockAndUpdate(pos, (state.getValue(WATERLOGGED) ? Blocks.WATER : Blocks.AIR).defaultBlockState());
            level.levelEvent(1029, pos, 0);
        } else {
            replace(level, pos, newBlock.withPropertiesOf(state));
            level.levelEvent(1030, pos, 0);
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction direction = (Direction) state.getValue(FACING);
        return direction.getAxis() == Axis.X ? X_AXIS_AABB : Z_AXIS_AABB;
    }
}
