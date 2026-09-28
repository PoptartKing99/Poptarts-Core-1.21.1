package dev.poptartking.poptartcore.lostheart;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public final class HeartCrystalItem extends Item {
    public HeartCrystalItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (Hearts.get(player) >= Hearts.MAX) {
            return InteractionResultHolder.pass(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player && level instanceof ServerLevel server && Hearts.get(player) < Hearts.MAX) {
            Hearts.set(player, Hearts.get(player) + 1);
            player.heal(2.0F);
            if (Hearts.get(player) == Hearts.MAX) {
                LostHeartTokens.of(server.getServer()).bump(player.getUUID());
            }
            EquipmentSlot slot = player.getUsedItemHand() == InteractionHand.OFF_HAND
                    ? EquipmentSlot.OFFHAND
                    : EquipmentSlot.MAINHAND;
            stack.hurtAndBreak(1, player, slot);
            level.playLocalSound(player, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.7F, 1.2F);
            server.sendParticles(
                    ParticleTypes.HEART, player.getX(), player.getY() + 1.2, player.getZ(), 3, 0.35, 0.35, 0.35, 0.02);
        }
        return stack;
    }
}
