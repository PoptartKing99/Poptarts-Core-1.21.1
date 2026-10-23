package dev.poptartking.poptartcore.equipment;

import dev.poptartking.poptartcore.PoptartCore;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingSwapItemsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Wayfarer-style working hand. Real hand inventory slots are never moved or overwritten. */
@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public final class UtilityWield {
    private static final Map<Player, Session> SESSIONS = new WeakHashMap<>();

    private static final class Session {
        ItemStack working;
        ItemStack base;
        int lease = 30;

        Session(ItemStack stack) {
            working = stack.copy();
            base = stack.copy();
        }
    }

    private UtilityWield() {}

    public static boolean isWielding(Player player) {
        return SESSIONS.containsKey(player);
    }

    /** Null means the real hand. Empty means an exhausted working stack, not the real hand. */
    public static ItemStack hand(Player player) {
        var session = SESSIONS.get(player);
        return session == null ? null : session.working;
    }

    public static boolean store(Player player, ItemStack stack) {
        var session = SESSIONS.get(player);
        if (session == null) return false;
        session.working = stack;
        return true;
    }

    public static boolean start(Player player) {
        if (isWielding(player)) return true;
        if (!UtilityActions.canWield(player) || player.isUsingItem()) return false;
        SESSIONS.put(player, new Session(UtilityActions.stack(player)));
        return true;
    }

    /** Vanilla use-item packets perform use; this packet only establishes the virtual hand. */
    public static void input(Player player, boolean held) {
        if (player.level().isClientSide) return;
        if (!held) {
            stop(player);
            return;
        }
        if (start(player)) SESSIONS.get(player).lease = 30;
    }

    private static boolean reconcile(Player player) {
        var session = SESSIONS.get(player);
        if (session == null) return false;
        // An external inventory edit wins. Never write a stale working copy over a replacement.
        if (!ItemStack.matches(UtilityActions.stack(player), session.base)) return false;
        if (!player.level().isClientSide && !ItemStack.matches(session.working, session.base)) {
            UtilityActions.setStack(player, session.working.copy());
            session.base = session.working.copy();
        }
        if (session.working.isEmpty()) return false;
        if (!AccessoryType.UTILITY.accepts(session.working)) {
            if (!player.level().isClientSide) {
                UtilityActions.setStack(player, ItemStack.EMPTY);
                player.getInventory().placeItemBackInInventory(session.working.copy());
            }
            return false;
        }
        return true;
    }

    public static void stop(Player player) {
        if (!isWielding(player)) return;
        player.stopUsingItem();
        reconcile(player);
        SESSIONS.remove(player);
    }

    public static void tick(Player player) {
        var session = SESSIONS.get(player);
        if (session == null) return;
        if (!player.isAlive()
                || player.isSpectator()
                || !reconcile(player)
                || !player.level().isClientSide && --session.lease <= 0) stop(player);
    }

    @SubscribeEvent
    public static void playerTick(PlayerTickEvent.Post event) {
        tick(event.getEntity());
    }

    @SubscribeEvent
    public static void swapHands(LivingSwapItemsEvent.Hands event) {
        if (event.getEntity() instanceof Player player && isWielding(player)) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void death(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) stop(player);
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        stop(event.getEntity());
    }
}
