package dev.poptartking.poptartcore.milk;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreAttachments;
import dev.poptartking.poptartcore.registry.PoptartCoreDataMaps;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract;

@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public final class Milking {
    private static final float EPSILON = 1.0E-4F;

    private Milking() {}

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onMilk(EntityInteract event) {
        if (!(event.getTarget() instanceof LivingEntity animal) || animal.isBaby()) {
            return;
        }

        MilkRecharge recharge = PoptartCoreDataMaps.milkRecharge(animal.getType());
        if (recharge == null) {
            return;
        }

        ItemStack held = event.getItemStack();
        MilkRecharge.MilkProduct product = recharge.productFor(held);
        if (product == null) {
            return;
        }

        event.setCanceled(true);
        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        if (animal.level().isClientSide) {
            event.setCancellationResult(InteractionResult.SUCCESS);
        } else if (available(animal, recharge) + EPSILON < product.value()) {
            player.playSound(SoundEvents.BUCKET_EMPTY, 0.4F, 1.4F);
            event.setCancellationResult(InteractionResult.CONSUME);
        } else {
            take(animal, recharge, product.value());
            player.playSound(SoundEvents.COW_MILK, 1.0F, 1.0F);
            player.setItemInHand(hand, ItemUtils.createFilledResult(held, player, new ItemStack(product.result())));
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    public static float available(LivingEntity animal, MilkRecharge recharge) {
        long fullAt = animal.getData(PoptartCoreAttachments.MILK_FULL_AT.get());
        long now = animal.level().getGameTime();
        if (fullAt <= now) {
            return 1.0F;
        }

        float missing = Math.min(1.0F, (float) (fullAt - now) / recharge.rechargeTicks());
        float stored = 1.0F - missing;
        float step = recharge.step();
        return Math.max(0.0F, (float) Math.floor((stored + EPSILON) / step) * step);
    }

    private static void take(LivingEntity animal, MilkRecharge recharge, float value) {
        long now = animal.level().getGameTime();
        long fullAt = animal.getData(PoptartCoreAttachments.MILK_FULL_AT.get());
        long missingTicks = Math.max(0L, fullAt - now);
        long addedTicks = (long) Math.ceil(value * recharge.rechargeTicks());
        animal.setData(PoptartCoreAttachments.MILK_FULL_AT.get(), now + missingTicks + addedTicks);
    }
}
