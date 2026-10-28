package dev.poptartking.poptartcore.backpack;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.equipment.AccessoryInventory;
import dev.poptartking.poptartcore.registry.PoptartCoreAttachments;
import dev.poptartking.poptartcore.registry.PoptartEquipmentBlocks;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BackpackVisualGameTests {
    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void appearanceChecksMatchRenderedSnapshotsAcrossComponentEdits(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        for (boolean rucksack : new boolean[] {false, true}) {
            var bag =
                    new ItemStack((rucksack ? PoptartEquipmentBlocks.RUCKSACK : PoptartEquipmentBlocks.BACKPACK).get());
            AccessoryInventory.get(player).setStackInSlot(AccessoryInventory.BACK_SLOT, bag);
            sync(helper, player, bag);
            BackpackColors.set(bag, BackpackColors.Part.BODY, 0x123456);
            sync(helper, player, bag);
            BackpackColors.set(bag, BackpackColors.Part.FLAPS, 0x654321);
            sync(helper, player, bag);
            BackpackAttachments.set(bag, 1, new ItemStack(Items.COMPASS));
            sync(helper, player, bag);
            bag.set(DataComponents.CUSTOM_NAME, Component.literal("Named bag"));
            sync(helper, player, bag);
            bag.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
            sync(helper, player, bag);
            bag.remove(DataComponents.CUSTOM_NAME);
            bag.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
            bag.remove(BackpackColors.BODY.get());
            bag.remove(DataComponents.DYED_COLOR);
            BackpackAttachments.set(bag, 1, ItemStack.EMPTY);
            sync(helper, player, bag);
            var visual = player.getData(PoptartCoreAttachments.BASIC_BACKPACK_VISUAL);
            bag.set(
                    DataComponents.CONTAINER,
                    ItemContainerContents.fromItems(List.of(new ItemStack(Items.DIAMOND, 7))));
            sync(helper, player, bag);
            helper.assertTrue(
                    player.getData(PoptartCoreAttachments.BASIC_BACKPACK_VISUAL) == visual,
                    "Storage-only edits leave the visible snapshot unchanged");
            helper.assertTrue(
                    !visual.has(DataComponents.CONTAINER) && !visual.has(DataComponents.BLOCK_ENTITY_DATA),
                    "Storage never appears in the synchronized visual");
            AccessoryInventory.get(player).setStackInSlot(AccessoryInventory.BACK_SLOT, ItemStack.EMPTY);
            sync(helper, player, ItemStack.EMPTY);
        }
        helper.succeed();
    }

    private static void sync(GameTestHelper helper, Player player, ItemStack bag) {
        BackpackVisual.sync(new PlayerTickEvent.Post(player));
        helper.assertTrue(
                ItemStack.matches(
                        BackpackVisual.renderStack(bag), player.getData(PoptartCoreAttachments.BASIC_BACKPACK_VISUAL)),
                "The optimized comparison must produce the same visual as the original stripped snapshot");
    }
}
