package dev.poptartking.poptartcore.flesh;

import com.mojang.serialization.MapCodec;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class UlcerBlock extends DirectionalBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<UlcerBlock> CODEC = simpleCodec(UlcerBlock::new);
    private static final Map<Direction, VoxelShape> SHAPES = buildShapes();

    public UlcerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                (BlockState) ((BlockState) ((BlockState) this.stateDefinition.any()).setValue(FACING, Direction.UP))
                        .setValue(BlockStateProperties.WATERLOGGED, false));
    }

    protected MapCodec<UlcerBlock> codec() {
        return CODEC;
    }

    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(new Property[] {FACING, BlockStateProperties.WATERLOGGED});
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean waterlogged =
                context.getLevel().getFluidState(context.getClickedPos()).isSourceOfType(Fluids.WATER);
        BlockState state =
                (BlockState) ((BlockState) this.defaultBlockState().setValue(FACING, context.getClickedFace()))
                        .setValue(BlockStateProperties.WATERLOGGED, waterlogged);
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = (Direction) state.getValue(FACING);
        BlockPos support = pos.relative(facing.getOpposite());
        return level.getBlockState(support).isFaceSturdy(level, support, facing);
    }

    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighbor,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos) {
        boolean waterlogged = (Boolean) state.getValue(BlockStateProperties.WATERLOGGED);
        if (waterlogged) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        if (direction == ((Direction) state.getValue(FACING)).getOpposite() && !state.canSurvive(level, pos)) {
            return waterlogged ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
        } else {
            return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
        }
    }

    protected FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED)
                ? Fluids.WATER.getSource(false)
                : super.getFluidState(state);
    }

    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    protected BlockState rotate(BlockState state, Rotation rotation) {
        return (BlockState) state.setValue(FACING, rotation.rotate((Direction) state.getValue(FACING)));
    }

    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation((Direction) state.getValue(FACING)));
    }

    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        if (!FleshGrowth.isUlcerFood(stack)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        } else {
            if (level instanceof ServerLevel server) {
                ItemStack food = stack.copyWithCount(1);
                stack.consume(1, player);
                FleshGrowth.feed(server, pos, food);
            }

            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
    }

    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level instanceof ServerLevel server
                && entity instanceof ItemEntity item
                && !item.isRemoved()
                && item.getAge() % 10 == 0) {
            ItemStack stack = item.getItem();
            if (!FleshGrowth.isUlcerFood(stack)) {
                return;
            }

            ItemStack food = stack.split(1);
            if (stack.isEmpty()) {
                item.discard();
            } else {
                item.setItem(stack);
            }

            FleshGrowth.feed(server, pos, food);
            return;
        }
    }

    private static Map<Direction, VoxelShape> buildShapes() {
        Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);

        for (Direction facing : Direction.values()) {
            shapes.put(
                    facing,
                    Shapes.join(
                            box(facing, 3.0, 0.0, 3.0, 13.0, 10.0, 13.0),
                            box(facing, 4.0, 1.0, 4.0, 12.0, 10.0, 12.0),
                            BooleanOp.ONLY_FIRST));
        }

        return shapes;
    }

    private static VoxelShape box(
            Direction facing, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return switch (facing) {
            case UP -> Block.box(minX, minY, minZ, maxX, maxY, maxZ);
            case DOWN -> Block.box(minX, 16.0 - maxY, minZ, maxX, 16.0 - minY, maxZ);
            case NORTH -> Block.box(minX, minZ, 16.0 - maxY, maxX, maxZ, 16.0 - minY);
            case SOUTH -> Block.box(minX, minZ, minY, maxX, maxZ, maxY);
            case WEST -> Block.box(16.0 - maxY, minX, minZ, 16.0 - minY, maxX, maxZ);
            case EAST -> Block.box(minY, minX, minZ, maxY, maxX, maxZ);
            default -> throw new MatchException(null, null);
        };
    }
}
