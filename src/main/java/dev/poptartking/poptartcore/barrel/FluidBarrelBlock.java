package dev.poptartking.poptartcore.barrel;

import com.mojang.serialization.MapCodec;
import dev.poptartking.poptartcore.registry.PoptartCoreBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidUtil;

public class FluidBarrelBlock extends BaseEntityBlock {
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(1, 0, 1, 15, 16, 15), Block.box(0, 2, 0, 16, 14, 16));
    private static final VoxelShape[] FORMED_SHAPES = {
        Shapes.empty(),
        SHAPE,
        Shapes.or(Block.box(2, 0, 2, 30, 32, 30), Block.box(0, 2, 0, 32, 30, 32)),
        Shapes.or(Block.box(3, 0, 3, 45, 48, 45), Block.box(0, 3, 0, 48, 45, 48))
    };

    public FluidBarrelBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(PART, Part.SINGLE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(FluidBarrelBlock::new);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(PART) == Part.SINGLE) return SHAPE;
        if (level.getBlockEntity(pos) instanceof FluidBarrelBlockEntity barrel && barrel.formationSize() > 1) {
            BlockPos origin = barrel.formationOrigin();
            return FORMED_SHAPES[barrel.formationSize()]
                    .move(origin.getX() - pos.getX(), origin.getY() - pos.getY(), origin.getZ() - pos.getZ());
        }
        return SHAPE;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && !state.is(oldState.getBlock())) level.scheduleTick(pos, this, 1);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock())) FluidBarrelFormation.onRemoved(level, pos);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        FluidBarrelFormation.onTick(level, pos);
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        return !stack.isEmpty() && FluidUtil.interactWithFluidHandler(player, hand, level, pos, hit.getDirection())
                ? ItemInteractionResult.sidedSuccess(level.isClientSide)
                : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FluidBarrelBlockEntity(pos, state);
    }

    public enum Part implements StringRepresentable {
        SINGLE("single"), BIG("big"), GIANT("giant"), HIDDEN("hidden");

        private final String name;

        Part(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
