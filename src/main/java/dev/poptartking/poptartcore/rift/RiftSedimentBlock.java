package dev.poptartking.poptartcore.rift;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.MultifaceSpreader;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;

public class RiftSedimentBlock extends MultifaceBlock {
    public static final MapCodec<RiftSedimentBlock> CODEC = simpleCodec(RiftSedimentBlock::new);
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private static final Map<Direction, BooleanProperty> FACES = PipeBlock.PROPERTY_BY_DIRECTION;
    private static final int BURN_TICKS = 14;

    private final MultifaceSpreader spreader = new MultifaceSpreader(this);

    public RiftSedimentBlock(Properties properties) {
        super(properties);
        BlockState state = stateDefinition.any().setValue(LIT, false);
        for (BooleanProperty face : FACES.values()) {
            state = state.setValue(face, false);
        }
        registerDefaultState(state);
    }

    @Override
    protected MapCodec<? extends MultifaceBlock> codec() {
        return CODEC;
    }

    @Override
    public MultifaceSpreader getSpreader() {
        return spreader;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT);
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
        if (state.getValue(LIT) || !stack.canPerformAction(ItemAbilities.FIRESTARTER_LIGHT)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        level.playSound(
                player,
                pos,
                SoundEvents.FLINTANDSTEEL_USE,
                SoundSource.BLOCKS,
                1.0F,
                level.getRandom().nextFloat() * 0.4F + 0.8F);
        if (level instanceof ServerLevel serverLevel) {
            RiftPortalIgnition.arm(serverLevel, pos);
            serverLevel.setBlock(pos, state.setValue(LIT, true), Block.UPDATE_ALL);
            serverLevel.scheduleTick(pos, this, burnTicks(state, serverLevel.random));
            serverLevel.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            if (player != null) {
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT)) {
            spread(level, pos);
            consume(level, pos, random);
        }
    }

    static void spread(ServerLevel level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = pos.relative(direction);
            ignite(level, neighbor);
            if (direction.getAxis() != Axis.Y) {
                ignite(level, neighbor.above());
                ignite(level, neighbor.below());
            }
        }
    }

    private static void ignite(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.hasProperty(LIT) && !state.getValue(LIT) && state.getBlock() instanceof RiftSedimentBlock) {
            level.setBlock(pos, state.setValue(LIT, true), Block.UPDATE_ALL);
            level.scheduleTick(pos, state.getBlock(), burnTicks(state, level.random));
        }
    }

    static int burnTicks(BlockState state, RandomSource random) {
        return BURN_TICKS + random.nextInt(5);
    }

    static void consume(ServerLevel level, BlockPos pos, RandomSource random) {
        level.removeBlock(pos, false);
        if (RiftPortalIgnition.startArmed(level, pos)) {
            level.sendParticles(
                    ParticleTypes.REVERSE_PORTAL,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    10,
                    0.3,
                    0.3,
                    0.3,
                    0.25);
            level.playSound(
                    null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.4F, 0.7F + random.nextFloat() * 0.2F);
        } else {
            level.sendParticles(
                    ParticleTypes.PORTAL,
                    pos.getX() + 0.5,
                    pos.getY() + 0.2,
                    pos.getZ() + 0.5,
                    6,
                    0.2,
                    0.15,
                    0.2,
                    0.15);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        for (Direction face : DIRECTIONS) {
            if (!hasFace(state, face)) {
                continue;
            }

            Vec3 surface = new Vec3(
                    pos.getX() + 0.5 + face.getStepX() * 0.44,
                    pos.getY() + 0.5 + face.getStepY() * 0.44,
                    pos.getZ() + 0.5 + face.getStepZ() * 0.44);
            Vec3 outward = Vec3.atLowerCornerOf(face.getOpposite().getNormal());
            if (state.getValue(LIT)) {
                Vec3 point = scatter(surface, face, random, 0.35);
                level.addParticle(
                        dev.poptartking.poptartcore.registry.PoptartCoreParticles.RIFT_FIRE_FLAME.get(),
                        point.x,
                        point.y,
                        point.z,
                        outward.x * 0.02,
                        outward.y * 0.02 + 0.012,
                        outward.z * 0.02);
                if (random.nextInt(3) == 0) {
                    Vec3 spark = scatter(surface, face, random, 0.25);
                    level.addParticle(
                            ParticleTypes.ELECTRIC_SPARK,
                            spark.x,
                            spark.y,
                            spark.z,
                            outward.x * 0.1,
                            outward.y * 0.1 + 0.05,
                            outward.z * 0.1);
                }
            } else if (random.nextInt(24) == 0) {
                Vec3 point = scatter(surface, face, random, 0.4);
                level.addParticle(
                        ParticleTypes.PORTAL,
                        point.x,
                        point.y,
                        point.z,
                        outward.x * 0.12,
                        outward.y * 0.12,
                        outward.z * 0.12);
            }
        }

        if (state.getValue(LIT) && random.nextInt(6) == 0) {
            level.playLocalSound(
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    SoundEvents.FIRE_AMBIENT,
                    SoundSource.BLOCKS,
                    0.35F,
                    1.4F + random.nextFloat() * 0.4F,
                    false);
        }
    }

    private static Vec3 scatter(Vec3 surface, Direction face, RandomSource random, double spread) {
        double first = (random.nextDouble() - 0.5) * 2.0 * spread;
        double second = (random.nextDouble() - 0.5) * 2.0 * spread;
        return switch (face.getAxis()) {
            case X -> surface.add(0.0, first, second);
            case Y -> surface.add(first, 0.0, second);
            case Z -> surface.add(first, second, 0.0);
        };
    }
}
