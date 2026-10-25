package dev.poptartking.poptartcore.backpack;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.equipment.UtilityWield;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public final class BackpackAttachmentEffects {
    private static final Map<Player, QuiverLoan> QUIVERS = new IdentityHashMap<>();

    public static synchronized void clear(Player player) {
        QUIVERS.remove(player);
    }

    @SubscribeEvent
    public static void logout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        clear(event.getEntity());
    }

    @SubscribeEvent
    public static void death(net.neoforged.neoforge.event.entity.living.LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) clear(player);
    }

    private static final class QuiverLoan {
        final ItemStack bag;
        final int slot;
        final ItemStack working;
        ItemStack base;

        QuiverLoan(ItemStack bag, int slot, ItemStack stack) {
            this.bag = bag;
            this.slot = slot;
            working = stack.copy();
            base = stack.copy();
        }
    }

    private BackpackAttachmentEffects() {}

    public static synchronized ItemStack quiver(Player player) {
        if (UtilityWield.isAttachmentWielding(player)
                && "supplementaries:quiver".equals(BackpackAttachments.id(UtilityWield.hand(player))))
            return UtilityWield.hand(player);
        var bag = BackpackAttachments.worn(player);
        int slot = BackpackAttachments.find(bag, "supplementaries:quiver");
        if (slot < 0) {
            QUIVERS.remove(player);
            return ItemStack.EMPTY;
        }
        var current = BackpackAttachments.get(bag, slot);
        var loan = QUIVERS.get(player);
        if (loan == null || loan.bag != bag || loan.slot != slot || !ItemStack.matches(current, loan.base)) {
            loan = new QuiverLoan(bag, slot, current);
            QUIVERS.put(player, loan);
        }
        return loan.working;
    }

    /** Quiver code edits a borrowed stack; persist that edit immediately, before a bag can be moved. */
    public static synchronized void itemChanged(ItemStack changed) {
        for (var loan : QUIVERS.values()) {
            if (loan.working != changed || loan.bag.isEmpty()) continue;
            if (ItemStack.matches(BackpackAttachments.get(loan.bag, loan.slot), loan.base)) {
                BackpackAttachments.set(loan.bag, loan.slot, changed);
                loan.base = changed.copy();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static synchronized void tick(PlayerTickEvent.Post event) {
        var player = event.getEntity();
        var loan = QUIVERS.get(player);
        if (loan != null && loan.bag != BackpackAttachments.worn(player)) QUIVERS.remove(player);
        if (player.level().isClientSide || !player.isAlive()) return;
        if (player.tickCount % 5 == 0 && BackpackAttachments.has(player, "minecraft:heavy_core"))
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 3, true, false, false));
    }

    @SubscribeEvent
    public static void knockback(LivingKnockBackEvent event) {
        if (event.getEntity() instanceof Player player && BackpackAttachments.has(player, "minecraft:heavy_core"))
            event.setCanceled(true);
    }
}
