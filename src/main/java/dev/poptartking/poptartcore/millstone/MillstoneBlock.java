package dev.poptartking.poptartcore.millstone;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.api.equipment.goggles.IProxyHoveringInformation;
import dev.poptartking.poptartcore.registry.PoptartCoreBlockEntities;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class MillstoneBlock extends BaseEntityBlock implements IProxyHoveringInformation {
    public MillstoneBlock(Properties properties) {
        super(properties);
    }

    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(MillstoneBlock::new);
    }

    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MillstoneBlockEntity(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(
                blockEntityType,
                PoptartCoreBlockEntities.MILLSTONE.get(),
                level.isClientSide ? MillstoneBlockEntity::clientTick : MillstoneBlockEntity::serverTick);
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        for (BlockPos offset : MillstoneStructure.ALL_OFFSETS) {
            BlockPos target = pos.offset(offset);
            if (level.isOutsideBuildHeight(target)
                    || !level.getWorldBorder().isWithinBounds(target)
                    || !level.hasChunkAt(target)
                    || !level.getBlockState(target).canBeReplaced()) {
                return null;
            }
        }

        return this.defaultBlockState();
    }

    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, 1);
        }
    }

    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos) {
        if (level instanceof Level realLevel
                && !realLevel.isClientSide
                && !realLevel.getBlockTicks().hasScheduledTick(pos, this)) {
            realLevel.scheduleTick(pos, this, 1);
        }

        return state;
    }

    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!MillstoneStructure.isLoaded(level, pos)) {
            level.scheduleTick(pos, this, 20);
            return;
        }
        for (BlockPos offset : MillstoneStructure.BASE_OFFSETS) {
            BlockPos target = pos.offset(offset);
            BlockState expected = (((PoptartCoreBlocks.MILLSTONE_STRUCTURAL.get())
                                    .defaultBlockState()
                                    .setValue(MillstoneStructuralBlock.FACING, MillstoneStructure.baseFacing(offset)))
                            .setValue(MillstoneStructuralBlock.TOP, false))
                    .setValue(MillstoneStructuralBlock.CORNER, MillstoneStructure.isCorner(offset));
            if (!this.placePiece(level, pos, target, expected)) {
                return;
            }
        }

        BlockPos rotorPos = pos.offset(MillstoneStructure.ROTOR_OFFSET);
        if (this.placePiece(level, pos, rotorPos, (PoptartCoreBlocks.MILLSTONE_ROTOR.get()).defaultBlockState())) {
            for (BlockPos offset : MillstoneStructure.TOP_OFFSETS) {
                BlockPos target = pos.offset(offset);
                BlockPos baseOffset = offset.below();
                BlockState expected = (((PoptartCoreBlocks.MILLSTONE_STRUCTURAL.get())
                                        .defaultBlockState()
                                        .setValue(
                                                MillstoneStructuralBlock.FACING,
                                                MillstoneStructure.baseFacing(baseOffset)))
                                .setValue(MillstoneStructuralBlock.TOP, true))
                        .setValue(MillstoneStructuralBlock.CORNER, MillstoneStructure.isCorner(baseOffset));
                if (!this.placePiece(level, pos, target, expected)) {
                    return;
                }
            }
        }
    }

    private boolean placePiece(ServerLevel level, BlockPos controllerPos, BlockPos target, BlockState expected) {
        BlockState current = level.getBlockState(target);
        if (current == expected) {
            return true;
        } else if (!current.is(expected.getBlock()) && !current.canBeReplaced()) {
            level.destroyBlock(controllerPos, true);
            return false;
        } else {
            level.setBlockAndUpdate(target, expected);
            return true;
        }
    }

    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult) {
        return level.getBlockEntity(pos) instanceof MillstoneBlockEntity millstone
                ? millstone.insertByHand(player, hand, stack)
                : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return level.getBlockEntity(pos) instanceof MillstoneBlockEntity millstone
                ? millstone.extractByHand(player)
                : InteractionResult.PASS;
    }

    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock()) && !movedByPiston) {
            level.invalidateCapabilities(pos);
            if (level.getBlockEntity(pos) instanceof MillstoneBlockEntity millstone) {
                millstone.dropBuffers();
            }

            for (BlockPos offset : MillstoneStructure.ALL_OFFSETS) {
                BlockPos target = pos.offset(offset);
                level.invalidateCapabilities(target);
                if (!level.hasChunkAt(target)) {
                    continue;
                }
                BlockState piece = level.getBlockState(target);
                if (piece.is(PoptartCoreBlocks.MILLSTONE_STRUCTURAL.get())
                        || piece.is(PoptartCoreBlocks.MILLSTONE_ROTOR.get())) {
                    level.setBlockAndUpdate(target, Blocks.AIR.defaultBlockState());
                }
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    public BlockPos getInformationSource(Level level, BlockPos pos, BlockState state) {
        return pos.offset(MillstoneStructure.ROTOR_OFFSET);
    }

    public static void destroyController(Level level, BlockPos controllerPos, Player player) {
        if (!level.isClientSide) {
            Block block = level.getBlockState(controllerPos).getBlock();
            if (block instanceof MillstoneBlock) {
                level.destroyBlock(controllerPos, player == null || !player.isCreative());
            }
        }
    }
}
