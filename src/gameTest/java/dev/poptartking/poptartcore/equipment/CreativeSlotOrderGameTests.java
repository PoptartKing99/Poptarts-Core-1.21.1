package dev.poptartking.poptartcore.equipment;

import dev.poptartking.poptartcore.PoptartCore;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CreativeSlotOrderGameTests {
    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void hiddenSlotsKeepTrashUpdatesAligned(GameTestHelper helper) {
        var storage = new SimpleContainer(55);
        List<Slot> inventory = new ArrayList<>();
        List<Slot> creative = new ArrayList<>();
        var targets = new IdentityHashMap<Slot, Slot>();
        for (int index = 0; index < 55; index++) {
            Slot original = new Slot(storage, index, 0, 0);
            original.index = index;
            original.set(new ItemStack(Items.STONE));
            inventory.add(original);
            if (index == 46 || index == 47) continue;
            Slot wrapper = new Slot(storage, index, 0, 0);
            targets.put(wrapper, original);
            creative.add(wrapper);
        }
        Slot trash = new Slot(new SimpleContainer(1), 0, 0, 0);
        creative.add(trash);
        helper.assertTrue(creative.size() == 54, "Reproduce reported 54-entry creative menu before repair");
        CreativeSlotOrder.restoreMissing(creative, inventory, targets::get, slot -> {
            Slot wrapper = new CreativeSlotOrder.HiddenSlot(slot);
            targets.put(wrapper, slot);
            return wrapper;
        });
        helper.assertTrue(
                creative.size() == 56 && creative.get(55) == trash, "Missing entries must be restored before trash");
        helper.assertTrue(
                !creative.get(46).isActive() && !creative.get(47).isActive(), "Restored entries must stay hidden");
        for (int index = 0; index < inventory.size(); index++) {
            helper.assertTrue(
                    targets.get(creative.get(index)) == inventory.get(index),
                    "Every packet index must resolve to its original slot");
            creative.get(index).set(ItemStack.EMPTY);
        }
        helper.assertTrue(
                storage.isEmpty(), "Shift-trash updates must clear all 55 inventory slots without shifted writes");
        CreativeSlotOrder.restoreMissing(creative, inventory, targets::get, slot -> {
            throw new AssertionError("Repeated repair must not add slots");
        });
        helper.succeed();
    }
}
