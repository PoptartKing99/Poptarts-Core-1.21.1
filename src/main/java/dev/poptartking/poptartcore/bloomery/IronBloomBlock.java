package dev.poptartking.poptartcore.bloomery;

import dev.poptartking.poptartcore.registry.PoptartCoreTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class IronBloomBlock extends Block {
    public static final IntegerProperty BLOOMS = IntegerProperty.create("blooms", 1, 9);
    public static final int MAX_BLOOMS = 9;
    private static final VoxelShape[] SHAPES = makeShapes();

    public IronBloomBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(BLOOMS, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BLOOMS);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(BLOOMS)];
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return (!context.isSecondaryUseActive()
                        && context.getItemInHand().is(asItem())
                        && state.getValue(BLOOMS) < MAX_BLOOMS)
                || super.canBeReplaced(state, context);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState existing = context.getLevel().getBlockState(context.getClickedPos());
        return existing.is(this)
                ? existing.setValue(BLOOMS, Math.min(MAX_BLOOMS, existing.getValue(BLOOMS) + 1))
                : super.getStateForPlacement(context);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockState result = super.playerWillDestroy(level, pos, state, player);
        // This callback runs before mining spends the hammer's final durability point.
        if (!player.getAbilities().instabuild && player.getMainHandItem().is(PoptartCoreTags.HAMMERS)) {
            int blooms = state.getValue(BLOOMS);
            if (!level.isClientSide) {
                dropNuggets(level, pos);
            }
            level.setBlock(pos, blooms > 1 ? state.setValue(BLOOMS, blooms - 1) : Blocks.AIR.defaultBlockState(), 3);
        }
        return result;
    }

    @Override
    public boolean onDestroyedByPlayer(
            BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        // Hammer processing already changed the pile. Do not remove the remaining blooms.
        if (level.getBlockState(pos) != state) {
            return false;
        }
        if (!level.isClientSide && !player.getAbilities().instabuild) {
            popResource(level, pos, new ItemStack(this, state.getValue(BLOOMS)));
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    private static void dropNuggets(Level level, BlockPos pos) {
        popResource(level, pos, new ItemStack(Items.IRON_NUGGET, 6 + level.random.nextInt(4)));
    }

    private static VoxelShape[] makeShapes() {
        VoxelShape base = Block.box(0, 0, 0, 16, 1, 16);
        VoxelShape[] rocks = {
            Block.box(6, 0, 5, 11, 4, 10),
            Block.box(4, 0, 7, 8, 3, 11),
            Block.box(4, 0, 3, 8, 2, 7),
            Block.box(10, 0, 7, 14, 2, 11),
            Block.box(9, 0, 2, 13, 3, 6),
            Block.box(8, 0, 10, 12, 3, 13),
            Block.box(2, 0, 4, 6, 4, 8),
            Block.box(3, 0, 10, 7, 2, 14)
        };
        VoxelShape[] shapes = new VoxelShape[MAX_BLOOMS + 1];
        VoxelShape accumulated = base;
        shapes[1] = accumulated;
        for (int index = 0; index < rocks.length; index++) {
            accumulated = Shapes.or(accumulated, rocks[index]);
            shapes[index + 2] = accumulated;
        }
        return shapes;
    }
}
