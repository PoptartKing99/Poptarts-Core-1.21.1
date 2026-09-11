package dev.poptartking.poptartcore.millstone;

import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;
import dev.poptartking.poptartcore.registry.PoptartCoreBlockEntities;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MillstoneRotorBlock extends KineticBlock implements IBE<MillstoneRotorBlockEntity> {
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0),
            new VoxelShape[] {Block.box(3.0, 8.0, 3.0, 13.0, 10.0, 13.0), Block.box(4.0, 10.0, 4.0, 12.0, 16.0, 12.0)});

    public MillstoneRotorBlock(Properties properties) {
        super(properties);
    }

    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
        return face == Direction.UP;
    }

    public Axis getRotationAxis(BlockState state) {
        return Axis.Y;
    }

    public Class<MillstoneRotorBlockEntity> getBlockEntityClass() {
        return MillstoneRotorBlockEntity.class;
    }

    public BlockEntityType<? extends MillstoneRotorBlockEntity> getBlockEntityType() {
        return PoptartCoreBlockEntities.MILLSTONE_ROTOR.get();
    }

    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockPos master = pos.below();
        if (!level.isClientSide && level.getBlockState(master).getBlock() instanceof MillstoneBlock) {
            level.destroyBlock(master, !player.isCreative());
        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock()) && !movedByPiston) {
            BlockPos master = pos.below();
            if (level.getBlockState(master).getBlock() instanceof MillstoneBlock) {
                level.destroyBlock(master, true);
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos) {
        if (direction == Direction.DOWN
                && !(neighborState.getBlock() instanceof MillstoneBlock)
                && !level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, 1);
        }

        return state;
    }

    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockState(pos.below()).getBlock() instanceof MillstoneBlock)) {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
    }

    public ItemStack getCloneItemStack(
            BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        return new ItemStack(PoptartCoreBlocks.MILLSTONE.get());
    }

    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        turnEntity(level, pos, pos, entity);
    }

    public static void turnEntity(Level level, BlockPos rotorPos, BlockPos standingPos, Entity entity) {
        if (entity.onGround() && !(entity.getDeltaMovement().y > 0.0)) {
            if (!(entity.getY() < standingPos.getY() + 0.45)) {
                if (level.getBlockEntity(rotorPos) instanceof MillstoneRotorBlockEntity rotor) {
                    if (!rotor.isOverspeed()) {
                        float speed = rotor.getSpeed() * 3.0F / 10.0F;
                        if (speed != 0.0F) {
                            if (!level.isClientSide || !(entity instanceof Player)) {
                                if (entity instanceof LivingEntity living) {
                                    float diff = entity.getYHeadRot() - speed;
                                    living.setNoActionTime(20);
                                    living.setYBodyRot(diff);
                                    living.setYHeadRot(diff);
                                    entity.setOnGround(false);
                                    entity.hurtMarked = true;
                                }

                                entity.setYRot(entity.getYRot() - speed);
                                Vec3 origin = new Vec3(rotorPos.getX() + 0.5, entity.getY(), rotorPos.getZ() + 0.5);
                                Vec3 offset = entity.position().subtract(origin);
                                offset = VecHelper.rotate(offset, Mth.clamp(speed, -16.0F, 16.0F), Axis.Y);
                                Vec3 movement = origin.add(offset).subtract(entity.position());
                                entity.setDeltaMovement(
                                        entity.getDeltaMovement().add(movement));
                                entity.hurtMarked = true;
                            }
                        }
                    }
                }
            }
        }
    }
}
