package dev.poptartking.poptartcore.beekeeping;

import dev.poptartking.poptartcore.PoptartCore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public class BeehiveSupportBlock extends Block {
    public static final BooleanProperty ANGLED = BooleanProperty.create("angled");
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape TOP = Block.box(0, 13, 0, 16, 16, 16);
    private static final VoxelShape STRAIGHT_SHAPE = Shapes.or(
            TOP,
            Block.box(0, 0, 0, 3, 13, 3),
            Block.box(13, 0, 0, 16, 13, 3),
            Block.box(0, 0, 13, 3, 13, 16),
            Block.box(13, 0, 13, 16, 13, 16));
    private static final VoxelShape ANGLED_SHAPE_X = Shapes.or(
            TOP,
            angledLegs(-3, 1.5, 0, 4, 1, 4),
            angledLegs(-1.5, 3, 4, 8, 1, 4),
            angledLegs(0, 5, 8, 13, 1, 4));
    private static final VoxelShape ANGLED_SHAPE_Z = Shapes.or(
            TOP,
            angledLegsZ(-3, 1.5, 0, 4, 1, 4),
            angledLegsZ(-1.5, 3, 4, 8, 1, 4),
            angledLegsZ(0, 5, 8, 13, 1, 4));

    public BeehiveSupportBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(ANGLED, false).setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ANGLED, FACING);
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        BlockPos pos = event.getPos();
        BlockState state = event.getLevel().getBlockState(pos);
        if (!player.isSecondaryUseActive() || !(state.getBlock() instanceof BeehiveSupportBlock)) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
        if (!event.getLevel().isClientSide) {
            event.getLevel().setBlock(pos, state.cycle(ANGLED), Block.UPDATE_ALL);
        }
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!state.getValue(ANGLED)) {
            return STRAIGHT_SHAPE;
        }
        return state.getValue(FACING).getAxis() == Direction.Axis.Z ? ANGLED_SHAPE_X : ANGLED_SHAPE_Z;
    }

    private static VoxelShape angledLegs(
            double minX, double maxX, double minY, double maxY, double minZ, double maxZ) {
        return Shapes.or(
                Block.box(minX, minY, minZ, maxX, maxY, maxZ),
                Block.box(16 - maxX, minY, minZ, 16 - minX, maxY, maxZ),
                Block.box(minX, minY, 16 - maxZ, maxX, maxY, 16 - minZ),
                Block.box(16 - maxX, minY, 16 - maxZ, 16 - minX, maxY, 16 - minZ));
    }

    private static VoxelShape angledLegsZ(
            double minZ, double maxZ, double minY, double maxY, double minX, double maxX) {
        return Shapes.or(
                Block.box(minX, minY, minZ, maxX, maxY, maxZ),
                Block.box(minX, minY, 16 - maxZ, maxX, maxY, 16 - minZ),
                Block.box(16 - maxX, minY, minZ, 16 - minX, maxY, maxZ),
                Block.box(16 - maxX, minY, 16 - maxZ, 16 - minX, maxY, 16 - minZ));
    }
}
