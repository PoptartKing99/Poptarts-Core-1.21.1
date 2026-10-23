package dev.poptartking.poptartcore.anvil;

import com.mojang.serialization.MapCodec;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import dev.poptartking.poptartcore.registry.PoptartCoreTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AnvilBlock extends FallingBlock implements EntityBlock {
    private static final VoxelShape BASE = Block.box(2.0, 0.0, 2.0, 14.0, 4.0, 14.0);
    private static final VoxelShape X_LEG1 = Block.box(3.0, 4.0, 4.0, 13.0, 5.0, 12.0);
    private static final VoxelShape X_LEG2 = Block.box(4.0, 5.0, 6.0, 12.0, 10.0, 10.0);
    private static final VoxelShape X_TOP = Block.box(0.0, 10.0, 3.0, 16.0, 16.0, 13.0);
    private static final VoxelShape Z_LEG1 = Block.box(4.0, 4.0, 3.0, 12.0, 5.0, 13.0);
    private static final VoxelShape Z_LEG2 = Block.box(6.0, 5.0, 4.0, 10.0, 10.0, 12.0);
    private static final VoxelShape Z_TOP = Block.box(3.0, 10.0, 0.0, 13.0, 16.0, 16.0);
    private static final VoxelShape X_AXIS_AABB = Shapes.or(BASE, new VoxelShape[] {X_LEG1, X_LEG2, X_TOP});
    private static final VoxelShape Z_AXIS_AABB = Shapes.or(BASE, new VoxelShape[] {Z_LEG1, Z_LEG2, Z_TOP});
    private static final Component CONTAINER_TITLE = Component.translatable("menu.title.poptartcore.anvil");
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public AnvilBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                (BlockState) ((BlockState) ((BlockState) this.stateDefinition.any()).setValue(FACING, Direction.NORTH))
                        .setValue(WATERLOGGED, false));
    }

    protected MapCodec<? extends FallingBlock> codec() {
        return simpleCodec(AnvilBlock::new);
    }

    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult) {
        if (stack.is(PoptartCoreTags.HAMMERS)
                && level.getBlockEntity(pos) instanceof AnvilBlockEntity anvil
                && anvil.hammer().isEmpty()) {
            if (!level.isClientSide) {
                anvil.setHammer(stack.split(1));
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            }

            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        } else if ((state.is((Block) PoptartCoreBlocks.CHIPPED_ANVIL.get())
                        || state.is((Block) PoptartCoreBlocks.DAMAGED_ANVIL.get()))
                && stack.is(Items.IRON_INGOT)) {
            Block newBlock = state.is((Block) PoptartCoreBlocks.DAMAGED_ANVIL.get())
                    ? (Block) PoptartCoreBlocks.CHIPPED_ANVIL.get()
                    : (Block) PoptartCoreBlocks.ANVIL.get();
            replace(level, pos, newBlock.withPropertiesOf(state));
            level.levelEvent(1030, pos, 0);
            level.levelEvent(2011, pos, 5);
            stack.consume(1, player);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        } else {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        }
    }

    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider(
                (containerId, playerInventory, player) ->
                        new AnvilMenu(containerId, playerInventory, ContainerLevelAccess.create(level, pos)),
                CONTAINER_TITLE);
    }

    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player.isSecondaryUseActive()
                && level.getBlockEntity(pos) instanceof AnvilBlockEntity anvil
                && !anvil.hammer().isEmpty()) {
            if (!level.isClientSide) {
                takeHammer(level, pos, player, anvil);
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        } else if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            player.openMenu(state.getMenuProvider(level, pos));
            player.awardStat(Stats.INTERACT_WITH_ANVIL);
            return InteractionResult.CONSUME;
        }
    }

    private static void takeHammer(Level level, BlockPos pos, Player player, AnvilBlockEntity anvil) {
        ItemStack hammer = anvil.hammer().copy();
        anvil.setHammer(ItemStack.EMPTY);
        if (!player.getInventory().add(hammer)) {
            player.drop(hammer, false);
        }

        level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AnvilBlockEntity(pos, state);
    }

    protected static void replace(Level level, BlockPos pos, BlockState newState) {
        ItemStack hammer = ItemStack.EMPTY;
        if (level.getBlockEntity(pos) instanceof AnvilBlockEntity anvil) {
            hammer = anvil.hammer().copy();
            anvil.setHammer(ItemStack.EMPTY);
        }

        level.setBlockAndUpdate(pos, newState);
        if (!hammer.isEmpty() && level.getBlockEntity(pos) instanceof AnvilBlockEntity moved) {
            moved.setHammer(hammer);
        }
    }

    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof AnvilBlockEntity anvil) {
            Containers.dropItemStack(
                    level, (double) pos.getX(), (double) pos.getY(), (double) pos.getZ(), anvil.hammer());
        }

        super.onRemove(state, level, pos, newState, moved);
    }

    public void maybeDamage(Level level, BlockPos pos, BlockState state, RandomSource random) {
        if (random.nextFloat() < 0.05F) {
            Block newBlock = state.is((Block) PoptartCoreBlocks.ANVIL.get())
                    ? (Block) PoptartCoreBlocks.CHIPPED_ANVIL.get()
                    : (state.is((Block) PoptartCoreBlocks.CHIPPED_ANVIL.get())
                            ? (Block) PoptartCoreBlocks.DAMAGED_ANVIL.get()
                            : null);
            if (newBlock == null) {
                level.setBlockAndUpdate(
                        pos, (state.getValue(WATERLOGGED) ? Blocks.WATER : Blocks.AIR).defaultBlockState());
                level.levelEvent(1029, pos, 0);
            } else {
                replace(level, pos, newBlock.withPropertiesOf(state));
                level.levelEvent(1030, pos, 0);
            }
        }
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return (BlockState) this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getClockWise());
    }

    protected BlockState rotate(BlockState state, Rotation rot) {
        return (BlockState) state.setValue(FACING, rot.rotate((Direction) state.getValue(FACING)));
    }

    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction direction = (Direction) state.getValue(FACING);
        return direction.getAxis() == Axis.X ? X_AXIS_AABB : Z_AXIS_AABB;
    }

    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(new Property[] {FACING}).add(new Property[] {WATERLOGGED});
    }

    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    public int getDustColor(BlockState state, BlockGetter reader, BlockPos pos) {
        return state.getMapColor(reader, pos).col;
    }

    public DamageSource getFallDamageSource(Entity entity) {
        return entity.damageSources().anvil(entity);
    }

    protected void falling(FallingBlockEntity entity) {
        entity.setHurtsEntities(2.0F, 40);
    }

    public void onLand(
            Level level, BlockPos pos, BlockState state, BlockState replaceableState, FallingBlockEntity fallingBlock) {
        if (!fallingBlock.isSilent()) {
            level.levelEvent(1031, pos, 0);
        }
    }

    public void onBrokenAfterFall(Level level, BlockPos pos, FallingBlockEntity fallingBlock) {
        if (!fallingBlock.isSilent()) {
            level.levelEvent(1029, pos, 0);
        }
    }
}
