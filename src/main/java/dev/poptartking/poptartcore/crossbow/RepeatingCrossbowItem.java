package dev.poptartking.poptartcore.crossbow;

import dev.poptartking.poptartcore.registry.PoptartCoreItems;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class RepeatingCrossbowItem extends CrossbowItem {
    private boolean startSoundPlayed;
    private boolean midLoadSoundPlayed;

    public RepeatingCrossbowItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return getChargeDuration() + 3;
    }

    public static int getChargeDuration() {
        return 15;
    }

    @Override
    public void performShooting(
            Level level,
            LivingEntity shooter,
            InteractionHand hand,
            ItemStack weapon,
            float velocity,
            float inaccuracy,
            @Nullable LivingEntity target) {
        super.performShooting(level, shooter, hand, weapon, velocity, inaccuracy * 2.0F, target);
    }

    @Override
    protected void shootProjectile(
            LivingEntity shooter,
            Projectile projectile,
            int index,
            float velocity,
            float inaccuracy,
            float angle,
            @Nullable LivingEntity target) {
        super.shootProjectile(shooter, projectile, index, velocity, inaccuracy * 2.0F, angle, target);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int count) {
        if (!level.isClientSide) {
            ChargingSounds chargingSounds = getChargingSounds();
            float progress = (float) (stack.getUseDuration(livingEntity) - count) / getChargeDuration();
            if (progress < 0.2F) {
                startSoundPlayed = false;
                midLoadSoundPlayed = false;
            }
            if (progress >= 0.2F && !startSoundPlayed) {
                startSoundPlayed = true;
                chargingSounds.start().ifPresent(sound -> playLoadingSound(level, livingEntity, sound.value()));
            }
            if (progress >= 0.5F && !midLoadSoundPlayed) {
                midLoadSoundPlayed = true;
                chargingSounds.mid().ifPresent(sound -> playLoadingSound(level, livingEntity, sound.value()));
            }
            if (progress == 1.0F && startSoundPlayed && midLoadSoundPlayed) {
                livingEntity.stopUsingItem();
            }
        }
    }

    private static void playLoadingSound(Level level, LivingEntity entity, SoundEvent sound) {
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound, SoundSource.PLAYERS, 0.5F, 1.0F);
    }

    @Override
    public void onStopUsing(ItemStack stack, LivingEntity entity, int count) {
        if (count == 3) {
            releaseUsing(stack, entity.level(), entity, 0);
        }
        super.onStopUsing(stack, entity, count);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        int usedTicks = getUseDuration(stack, entity) - timeLeft;
        if (getPowerForTime(usedTicks) == 1.0F && !isCharged(stack) && tryLoadProjectiles(entity, stack)) {
            getChargingSounds()
                    .end()
                    .ifPresent(sound -> level.playSound(
                            null,
                            entity.getX(),
                            entity.getY(),
                            entity.getZ(),
                            sound.value(),
                            entity.getSoundSource(),
                            1.0F,
                            1.0F / (level.getRandom().nextFloat() * 0.5F + 1.0F) + 0.2F));
        }
    }

    private static float getPowerForTime(int usedTicks) {
        return Math.min((float) usedTicks / getChargeDuration(), 1.0F);
    }

    private static boolean tryLoadProjectiles(LivingEntity shooter, ItemStack crossbowStack) {
        List<ItemStack> projectiles = draw(crossbowStack, shooter.getProjectile(crossbowStack), shooter);
        if (projectiles.isEmpty()) {
            return false;
        }
        crossbowStack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(projectiles));
        return true;
    }

    private static ChargingSounds getChargingSounds() {
        return new ChargingSounds(
                Optional.of(SoundEvents.CROSSBOW_QUICK_CHARGE_2),
                Optional.empty(),
                Optional.of(SoundEvents.CROSSBOW_LOADING_END));
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.is(PoptartCoreItems.REDSTONE_CIRCUIT.get());
    }
}
