package dev.poptartking.poptartcore.timekeeping.client;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.backpack.BackpackAttachments;
import dev.poptartking.poptartcore.equipment.AccessoryInventory;
import dev.poptartking.poptartcore.timekeeping.Stopwatch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID, value = Dist.CLIENT)
public final class TimekeepingClient {
    private TimekeepingClient() {}

    @SubscribeEvent
    public static void render(RenderGuiEvent.Post event) {
        ClockHud.render(event.getGuiGraphics());
    }

    @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST)
    public static void tooltip(ItemTooltipEvent event) {
        if (event.getItemStack().is(net.minecraft.world.item.Items.CLOCK))
            event.getToolTip()
                    .removeIf(line ->
                            line.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents text
                                    && text.getKey().equals("tooltip.caverns_and_chasms.time"));
        ClockHud.appendTooltip(event.getItemStack(), event.getToolTip()::add);
    }

    public static ItemStack runningClock(Player player) {
        if (Stopwatch.isRunning(player.getMainHandItem())) return player.getMainHandItem();
        if (Stopwatch.isRunning(player.getOffhandItem())) return player.getOffhandItem();
        var slots = AccessoryInventory.get(player);
        for (int i = 0; i < slots.getSlots(); i++)
            if (Stopwatch.isRunning(slots.getStackInSlot(i))) return slots.getStackInSlot(i);
        var bag = BackpackAttachments.worn(player);
        for (int i = 1; i < BackpackAttachments.slots(BackpackAttachments.rucksack(bag)); i++) {
            var clock = BackpackAttachments.get(bag, i);
            if (Stopwatch.isRunning(clock)) return clock;
        }
        return ItemStack.EMPTY;
    }
}
