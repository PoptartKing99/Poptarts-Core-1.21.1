package dev.poptartking.poptartcore.equipment;

import dev.poptartking.poptartcore.PoptartCore;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingSwapItemsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Working hand shared by utility items and primary backpack attachments. Real hand slots are never overwritten. */
@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public final class UtilityWield {
    // Client and integrated-server players compare equal by entity ID, but own different bags and use sessions.
    private static final Map<Player, Session> SESSIONS = Collections.synchronizedMap(new IdentityHashMap<>());

    private static final class Session {
        ItemStack working;
        ItemStack base;
        int lease = 30;
        final boolean attachment;
        ItemStack bag;

        Session(ItemStack stack, boolean attachment, ItemStack bag) {
            working = stack.copy();
            base = stack.copy();
            this.attachment = attachment;
            this.bag = bag;
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
        return start(player, false);
    }

    public static boolean isAttachmentWielding(Player player) {
        var session = SESSIONS.get(player);
        return session != null && session.attachment;
    }

    public static boolean startAttachment(Player player) {
        return start(player, true);
    }

    private static boolean start(Player player, boolean attachment) {
        if (isWielding(player)) return SESSIONS.get(player).attachment == attachment;
        if (player.isUsingItem()
                || !(attachment
                        ? dev.poptartking.poptartcore.backpack.BackpackAttachments.canUse(player)
                        : UtilityActions.canWield(player))) return false;
        var bag = attachment ? dev.poptartking.poptartcore.backpack.BackpackAttachments.worn(player) : ItemStack.EMPTY;
        var stack = attachment
                ? dev.poptartking.poptartcore.backpack.BackpackAttachments.get(bag, 1)
                : UtilityActions.stack(player);
        SESSIONS.put(player, new Session(stack, attachment, bag));
        return true;
    }

    private static ItemStack source(Player player, Session session) {
        if (!session.attachment) return UtilityActions.stack(player);
        var current = dev.poptartking.poptartcore.backpack.BackpackAttachments.worn(player);
        if (!sameAttachmentSource(session.bag, current, player.level().isClientSide)) return ItemStack.EMPTY;
        session.bag = current;
        return dev.poptartking.poptartcore.backpack.BackpackAttachments.get(current, 1);
    }

    public static boolean sameAttachmentSource(ItemStack bound, ItemStack current, boolean clientSide) {
        // Inventory synchronization recreates client stacks. The server still requires the original bag object.
        return bound == current
                || clientSide
                        && !current.isEmpty()
                        && (ItemStack.matches(bound, current) || sameBagExceptPrimaryPassiveData(bound, current));
    }

    private static boolean sameExceptPassiveData(ItemStack first, ItemStack second) {
        net.minecraft.core.component.DataComponentType<?> component;
        if (first.is(net.minecraft.world.item.Items.COMPASS) && second.is(net.minecraft.world.item.Items.COMPASS))
            component = dev.poptartking.poptartcore.navigation.NavigationComponents.WAYPOINTS.get();
        else if (first.is(net.minecraft.world.item.Items.CLOCK) && second.is(net.minecraft.world.item.Items.CLOCK))
            component = dev.poptartking.poptartcore.timekeeping.TimekeepingComponents.STOPWATCH.get();
        else return false;
        var a = first.copy();
        var b = second.copy();
        a.remove(component);
        b.remove(component);
        return ItemStack.matches(a, b);
    }

    private static boolean sameBagExceptPrimaryPassiveData(ItemStack first, ItemStack second) {
        var a = dev.poptartking.poptartcore.backpack.BackpackAttachments.get(first, 1);
        var b = dev.poptartking.poptartcore.backpack.BackpackAttachments.get(second, 1);
        if (!sameExceptPassiveData(a, b)) return false;
        var copy = first.copy();
        dev.poptartking.poptartcore.backpack.BackpackAttachments.set(copy, 1, b);
        return ItemStack.matches(copy, second);
    }

    private static void setSource(Player player, Session session, ItemStack stack) {
        if (session.attachment) dev.poptartking.poptartcore.backpack.BackpackAttachments.set(session.bag, 1, stack);
        else UtilityActions.setStack(player, stack);
    }

    /** Vanilla use-item packets perform use; this packet only establishes the virtual hand. */
    public static void input(Player player, boolean held) {
        input(player, held, false);
    }

    public static void input(Player player, boolean held, boolean attachment) {
        if (player.level().isClientSide) return;
        if (!held) {
            if (isWielding(player) && SESSIONS.get(player).attachment == attachment) stop(player);
            return;
        }
        if (start(player, attachment)) SESSIONS.get(player).lease = 30;
    }

    private static boolean reconcile(Player player) {
        var session = SESSIONS.get(player);
        if (session == null) return false;
        // An external inventory edit wins. Never write a stale working copy over a replacement.
        var source = source(player, session);
        if (player.level().isClientSide
                && sameExceptPassiveData(source, session.base)
                && !ItemStack.matches(source, session.base)) {
            if (source.is(net.minecraft.world.item.Items.COMPASS))
                session.working.set(
                        dev.poptartking.poptartcore.navigation.NavigationComponents.WAYPOINTS.get(),
                        source.get(dev.poptartking.poptartcore.navigation.NavigationComponents.WAYPOINTS.get()));
            else
                session.working.set(
                        dev.poptartking.poptartcore.timekeeping.TimekeepingComponents.STOPWATCH.get(),
                        source.get(dev.poptartking.poptartcore.timekeeping.TimekeepingComponents.STOPWATCH.get()));
            session.base = source.copy();
        }
        if (!ItemStack.matches(source, session.base)) {
            if (session.attachment)
                com.mojang.logging.LogUtils.getLogger()
                        .debug(
                                "Backpack attachment use stopped: source changed on {}",
                                player.level().isClientSide ? "client" : "server");
            return false;
        }
        if (!player.level().isClientSide && !ItemStack.matches(session.working, session.base)) {
            setSource(player, session, session.working.copy());
            session.base = session.working.copy();
        }
        if (session.working.isEmpty()) return false;
        if (!(session.attachment
                ? dev.poptartking.poptartcore.backpack.BackpackAttachments.fits(
                        dev.poptartking.poptartcore.backpack.BackpackAttachments.rucksack(session.bag),
                        1,
                        session.working)
                : AccessoryType.UTILITY.accepts(session.working))) {
            if (!player.level().isClientSide) {
                setSource(player, session, ItemStack.EMPTY);
                player.getInventory().placeItemBackInInventory(session.working.copy());
            }
            return false;
        }
        return true;
    }

    /** Commit a server-side edit of the working item without ending its use session. */
    public static void commit(Player player) {
        if (!player.level().isClientSide && !reconcile(player)) stop(player);
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
