package dev.poptartking.poptartcore.timekeeping;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.backpack.BackpackAttachments;
import dev.poptartking.poptartcore.equipment.AccessoryInventory;
import dev.poptartking.poptartcore.equipment.UtilityWield;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public final class TimekeepingEvents {
    private TimekeepingEvents() {}

    public static void tick(ServerPlayer player) {
        var working = UtilityWield.hand(player);
        if (working != null && Stopwatch.isRunning(working)) {
            Stopwatch.tickHeld(working, player);
            UtilityWield.commit(player);
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++)
            Stopwatch.tickHeld(player.getInventory().getItem(i), player);
        var inventory = AccessoryInventory.get(player);
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (i == AccessoryInventory.UTILITY_SLOT
                    && UtilityWield.isWielding(player)
                    && !UtilityWield.isAttachmentWielding(player)) continue;
            Stopwatch.tickHeld(inventory.getStackInSlot(i), player);
        }
        var bag = BackpackAttachments.worn(player);
        for (int i = 1; i < BackpackAttachments.slots(BackpackAttachments.rucksack(bag)); i++) {
            if (i == 1 && UtilityWield.isAttachmentWielding(player)) continue;
            var stack = BackpackAttachments.get(bag, i);
            if (!Stopwatch.isRunning(stack)) continue;
            var copy = stack.copy();
            Stopwatch.tickHeld(copy, player);
            if (!net.minecraft.world.item.ItemStack.matches(copy, stack)) BackpackAttachments.set(bag, i, copy);
        }
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) tick(player);
    }
}
