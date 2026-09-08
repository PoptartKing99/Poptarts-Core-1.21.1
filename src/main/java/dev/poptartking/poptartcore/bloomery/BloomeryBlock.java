package dev.poptartking.poptartcore.bloomery;

import com.mojang.serialization.MapCodec;
import dev.poptartking.poptartcore.registry.PoptartCoreBlockEntities;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.ItemAbilities;
import org.jetbrains.annotations.Nullable;

public class BloomeryBlock extends BaseEntityBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE = Shapes.or(Block.box(0, 0, 0, 16, 12, 16), Block.box(2, 12, 2, 14, 16, 14));

    public BloomeryBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LIT, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(BloomeryBlock::new);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, FACING);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide
                ? null
                : createTickerHelper(
                        blockEntityType, PoptartCoreBlockEntities.BLOOMERY.get(), BloomeryBlockEntity::serverTick);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BloomeryBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof BloomeryBlockEntity bloomery) || bloomery.hasStarted()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (stack.canPerformAction(ItemAbilities.FIRESTARTER_LIGHT)) {
            if (!bloomery.canLight()) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            if (!level.isClientSide) {
                bloomery.light(level.getRandom());
                level.setBlockAndUpdate(pos, state.setValue(LIT, true));
                level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1, 1);
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        if (level.isClientSide) {
            return bloomery.placeableAmount(stack) > 0
                    ? ItemInteractionResult.sidedSuccess(true)
                    : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        return bloomery.place(stack)
                ? ItemInteractionResult.sidedSuccess(false)
                : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) {
            return;
        }

        double centerX = pos.getX() + 0.5;
        double bottomY = pos.getY();
        double centerZ = pos.getZ() + 0.5;
        if (random.nextFloat() < 0.1F) {
            level.playLocalSound(
                    centerX, bottomY, centerZ, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1, 1, false);
        }

        Direction facing = state.getValue(FACING);
        Axis axis = facing.getAxis();
        double sidewaysOffset = random.nextFloat() * 0.6 - 0.3;
        double xOffset = axis == Axis.X ? facing.getStepX() * 0.52 : sidewaysOffset;
        double yOffset = random.nextFloat() * 6.0F / 16.0F;
        double zOffset = axis == Axis.Z ? facing.getStepZ() * 0.52 : sidewaysOffset;
        level.addParticle(ParticleTypes.SMOKE, centerX + xOffset, bottomY + yOffset, centerZ + zOffset, 0, 0, 0);
        level.addParticle(ParticleTypes.FLAME, centerX + xOffset, bottomY + yOffset, centerZ + zOffset, 0, 0, 0);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof BloomeryBlockEntity bloomery
                        && bloomery.hasStarted()
                ? List.of()
                : super.getDrops(state, params);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof BloomeryBlockEntity bloomery) {
            if (bloomery.isDone() && bloomery.bloomCount() > 0) {
                int blooms = Mth.clamp(bloomery.bloomCount(), 1, IronBloomBlock.MAX_BLOOMS);
                level.setBlockAndUpdate(
                        pos,
                        PoptartCoreBlocks.IRON_BLOOM.get().defaultBlockState().setValue(IronBloomBlock.BLOOMS, blooms));
            } else if (bloomery.hasStarted()) {
                ItemStack iron = bloomery.getItem(BloomeryBlockEntity.IRON_SLOT);
                if (!iron.isEmpty()) {
                    Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, iron.copy());
                }
            } else if (!bloomery.isEmpty()) {
                Containers.dropContents(level, pos, bloomery);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
