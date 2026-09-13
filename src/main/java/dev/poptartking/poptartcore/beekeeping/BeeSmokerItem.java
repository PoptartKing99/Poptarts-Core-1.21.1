package dev.poptartking.poptartcore.beekeeping;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BeeSmokerItem extends Item {
    private static final double REACH = 5.0D;
    private static final double SPRAY_RADIUS = 1.25D;
    private static final int BEE_SMOKE_TICKS = 30;
    private static final int HIVE_SMOKE_TICKS = 100;
    private static final Map<Bee, Integer> BEE_PROGRESS = new WeakHashMap<>();
    private static final Map<Level, Map<Long, Integer>> HIVE_PROGRESS = new WeakHashMap<>();

    public BeeSmokerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        int elapsed = getUseDuration(stack, entity) - remainingUseDuration;
        if (elapsed > 0 && elapsed % 20 == 0) {
            EquipmentSlot slot = entity.getUsedItemHand() == InteractionHand.MAIN_HAND
                    ? EquipmentSlot.MAINHAND
                    : EquipmentSlot.OFFHAND;
            stack.hurtAndBreak(1, entity, slot);
        }

        Vec3 eye = entity.getEyePosition();
        Vec3 view = entity.getViewVector(1.0F);
        Vec3 end = eye.add(view.scale(REACH));
        spawnSmoke(serverLevel, eye, view, entity);

        BlockHitResult blockHit =
                level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity));
        double rayLength = blockHit.getType() == HitResult.Type.MISS
                ? REACH
                : blockHit.getLocation().distanceTo(eye);

        for (Bee bee : level.getEntitiesOfClass(
                Bee.class,
                entity.getBoundingBox().expandTowards(view.scale(REACH)).inflate(SPRAY_RADIUS))) {
            if (!bee.isAlive() || SmokedBees.isSmoked(bee)) {
                continue;
            }

            Vec3 center = bee.position().add(0.0D, bee.getBbHeight() * 0.5D, 0.0D);
            double along = Mth.clamp(center.subtract(eye).dot(view), 0.0D, rayLength);
            if (eye.add(view.scale(along)).distanceTo(center) <= SPRAY_RADIUS) {
                int progress = BEE_PROGRESS.merge(bee, 1, Integer::sum);
                if (progress >= BEE_SMOKE_TICKS) {
                    BEE_PROGRESS.remove(bee);
                    SmokedBees.mark(bee);
                    level.playSound(
                            null,
                            bee.getX(),
                            bee.getY(),
                            bee.getZ(),
                            SoundEvents.EXPERIENCE_ORB_PICKUP,
                            SoundSource.NEUTRAL,
                            0.3F,
                            1.0F);
                    serverLevel.sendParticles(
                            ParticleTypes.HEART,
                            bee.getX(),
                            bee.getY() + bee.getBbHeight() * 0.5D,
                            bee.getZ(),
                            7,
                            bee.getBbWidth() * 0.5D,
                            bee.getBbHeight() * 0.5D,
                            bee.getBbWidth() * 0.5D,
                            0.02D);
                }
            }
        }

        if (blockHit.getType() == HitResult.Type.BLOCK
                && level.getBlockState(blockHit.getBlockPos()).is(BlockTags.BEEHIVES)) {
            BlockPos pos = blockHit.getBlockPos();
            Map<Long, Integer> progress = HIVE_PROGRESS.computeIfAbsent(level, key -> new HashMap<>());
            int ticks = progress.merge(pos.asLong(), 1, Integer::sum);
            if (ticks >= HIVE_SMOKE_TICKS) {
                SmokedHives.mark(level, pos);
                if (ticks == HIVE_SMOKE_TICKS) {
                    level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.3F, 1.0F);
                    serverLevel.sendParticles(
                            ParticleTypes.HEART,
                            pos.getX() + 0.5D,
                            pos.getY() + 0.65D,
                            pos.getZ() + 0.5D,
                            7,
                            0.5D,
                            0.5D,
                            0.5D,
                            0.02D);
                }
            }
        }
    }

    private static void spawnSmoke(ServerLevel level, Vec3 eye, Vec3 view, LivingEntity entity) {
        if (entity.tickCount % 2 != 0) {
            return;
        }

        RandomSource random = level.random;
        double distance = 0.9D + random.nextDouble() * 0.6D;
        Vec3 origin = eye.add(view.scale(distance)).add(0.0D, -0.2D, 0.0D);
        level.sendParticles(
                ParticleTypes.CAMPFIRE_COSY_SMOKE,
                origin.x,
                origin.y,
                origin.z,
                0,
                view.x + (random.nextDouble() - 0.5D) * 0.15D,
                view.y + 0.15D,
                view.z + (random.nextDouble() - 0.5D) * 0.15D,
                0.22D);
    }
}
