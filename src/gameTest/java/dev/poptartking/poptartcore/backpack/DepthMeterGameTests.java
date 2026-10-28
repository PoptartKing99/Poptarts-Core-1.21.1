package dev.poptartking.poptartcore.backpack;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.depthmeter.DepthMeter;
import dev.poptartking.poptartcore.equipment.AccessoryInventory;
import dev.poptartking.poptartcore.registry.PoptartEquipmentBlocks;
import dev.poptartking.poptartcore.registry.PoptartToolItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DepthMeterGameTests {
    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void seaLevelBandsAndNetherErrorMatchWayward(GameTestHelper h) {
        var level = h.getLevel();
        int sea = level.getSeaLevel();
        h.assertTrue(DepthMeter.compute(level, sea) == 0F, "Neutral at sea level");
        h.assertTrue(DepthMeter.compute(level, sea - 1) == .04F, "First low frame below sea level");
        h.assertTrue(Math.abs(DepthMeter.compute(level, sea + 1) - .44F) < .00001F, "First high frame above sea level");
        h.assertTrue(
                Math.abs(DepthMeter.compute(level, level.getMinBuildHeight()) - .4F) < .00001F, "Deepest low frame");
        h.assertTrue(
                Math.abs(DepthMeter.compute(level, level.getMaxBuildHeight()) - .8F) < .00001F, "Highest frame clamps");
        h.assertTrue(DepthMeter.compute(level, level.getMinBuildHeight() - 1) == 0F, "Void is neutral");
        h.assertTrue(DepthMeter.attachmentState(level, sea - 1).equals("depth_meter_low"), "Low attachment state");
        h.assertTrue(DepthMeter.attachmentState(level, sea + 1).equals("depth_meter_high"), "High attachment state");
        var nether = level.getServer().getLevel(Level.NETHER);
        float expected = nether.getGameTime() / 10L % 2L == 0L ? .84F : 0F;
        h.assertTrue(Math.abs(DepthMeter.compute(nether, 64) - expected) < .00001F, "Nether flash follows world time");
        h.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void heldUtilityAndAllBagMountsProvideReadout(GameTestHelper h) {
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        var meter = PoptartToolItems.DEPTH_METER.get();
        h.assertTrue(!DepthMeter.carries(player, meter), "No readout without meter");
        player.getInventory().setItem(9, new ItemStack(meter));
        h.assertTrue(!DepthMeter.carries(player, meter), "Storage alone does not enable readout");
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(meter));
        h.assertTrue(DepthMeter.carries(player, meter), "Offhand enables readout");
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        var accessories = AccessoryInventory.get(player);
        accessories.setStackInSlot(AccessoryInventory.UTILITY_SLOT, new ItemStack(meter));
        h.assertTrue(DepthMeter.carries(player, meter), "Utility slot enables readout");
        accessories.setStackInSlot(AccessoryInventory.UTILITY_SLOT, ItemStack.EMPTY);
        for (boolean ruck : new boolean[] {false, true}) {
            var bag = new ItemStack((ruck ? PoptartEquipmentBlocks.RUCKSACK : PoptartEquipmentBlocks.BACKPACK)
                    .get()
                    .asItem());
            for (int slot = 1; slot < BackpackAttachments.slots(ruck); slot++) {
                h.assertTrue(BackpackAttachments.fits(ruck, slot, new ItemStack(meter)), "Meter fits every side mount");
                BackpackAttachments.set(bag, slot, new ItemStack(meter));
                accessories.setStackInSlot(AccessoryInventory.BACK_SLOT, bag);
                h.assertTrue(DepthMeter.carries(player, meter), "Mounted meter enables readout");
                BackpackAttachments.set(bag, slot, ItemStack.EMPTY);
            }
        }
        h.succeed();
    }
}
