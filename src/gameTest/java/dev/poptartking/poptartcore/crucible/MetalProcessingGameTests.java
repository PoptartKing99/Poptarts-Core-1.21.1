package dev.poptartking.poptartcore.crucible;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.blastfurnace.BlastFurnaceBlock;
import dev.poptartking.poptartcore.registry.PoptartCoreFluids;
import dev.poptartking.poptartcore.registry.PoptartMaterialItems;
import dev.poptartking.poptartcore.registry.PoptartProcessingItems;
import dev.poptartking.poptartcore.registry.PoptartWorkstationBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MetalProcessingGameTests {
    @GameTest(template = "millstone_test")
    public static void crucibleMeltsAndCastsLead(GameTestHelper helper) {
        BlockPos relativePos = new BlockPos(2, 2, 2);
        helper.setBlock(relativePos, PoptartWorkstationBlocks.CRUCIBLE.get());
        var level = helper.getLevel();
        var pos = helper.absolutePos(relativePos);
        var crucible = (CrucibleBlockEntity) level.getBlockEntity(pos);
        Item leadIngot = PoptartMaterialItems.LEAD_INGOT.get();
        Fluid moltenLead = PoptartCoreFluids.MOLTEN_LEAD.source().get();
        assertMeltsAndCasts(helper, crucible, PoptartMaterialItems.RAW_LEAD.get(), moltenLead, leadIngot, 500);
        assertMeltsAndCasts(helper, crucible, leadIngot, moltenLead, leadIngot, 500);
        helper.succeed();
    }

    @GameTest(template = "millstone_test")
    public static void copperAndSteelRemeltAndCastAtTheirWorkstations(GameTestHelper helper) {
        BlockPos relativePos = new BlockPos(2, 2, 2);
        helper.setBlock(relativePos, PoptartWorkstationBlocks.CRUCIBLE.get());
        var level = helper.getLevel();
        var crucible = (CrucibleBlockEntity) level.getBlockEntity(helper.absolutePos(relativePos));
        var steelIngot = PoptartMaterialItems.STEEL_INGOT.get();
        helper.assertTrue(
                !crucible.canPlaceItem(CrucibleBlockEntity.INPUT_SLOT_1, new ItemStack(steelIngot)),
                "Steel remelting should require the blast furnace");
        assertMeltsAndCasts(
                helper,
                crucible,
                Items.COPPER_INGOT,
                PoptartCoreFluids.MOLTEN_COPPER.source().get(),
                Items.COPPER_INGOT,
                500);

        BlockPos blastPos = helper.absolutePos(new BlockPos(5, 2, 2));
        var blastState = PoptartWorkstationBlocks.BLAST_FURNACE.get().defaultBlockState();
        level.setBlock(blastPos, blastState, 2);
        level.setBlock(blastPos.above(), blastState.setValue(BlastFurnaceBlock.HALF, DoubleBlockHalf.UPPER), 2);
        var blast = (CrucibleBlockEntity) level.getBlockEntity(blastPos);
        assertMeltsAndCasts(
                helper,
                blast,
                steelIngot,
                PoptartCoreFluids.MOLTEN_STEEL.source().get(),
                steelIngot,
                250);
        helper.succeed();
    }

    private static void assertMeltsAndCasts(
            GameTestHelper helper,
            CrucibleBlockEntity furnace,
            Item input,
            Fluid expectedFluid,
            Item output,
            int meltingTicks) {
        ItemStack stack = new ItemStack(input);
        helper.assertTrue(furnace.canPlaceItem(CrucibleBlockEntity.INPUT_SLOT_1, stack), "Expected to accept " + input);
        furnace.setItem(furnace.fuelSlot, new ItemStack(Items.COAL));
        furnace.setItem(CrucibleBlockEntity.INPUT_SLOT_1, stack);
        tick(furnace, meltingTicks);
        var fluid = furnace.tank.getFluid();
        helper.assertTrue(
                fluid.getFluid() == expectedFluid && fluid.getAmount() == 250,
                "Expected 250 mB of molten metal from " + input);
        helper.assertTrue(furnace.getItem(CrucibleBlockEntity.INPUT_SLOT_1).isEmpty(), "Expected the input to melt");

        furnace.setItem(furnace.containerSlot, new ItemStack(PoptartProcessingItems.INGOT_MOULD.get()));
        tick(furnace, CrucibleBlockEntity.CASTING_TIME);
        var result = furnace.removeItem(furnace.resultSlot, 1);
        helper.assertTrue(
                result.is(output) && furnace.getItem(furnace.resultSlot).isEmpty(), "Expected one cast ingot");
        helper.assertTrue(furnace.tank.isEmpty(), "Casting should consume the molten metal");
        furnace.removeItem(furnace.containerSlot, 1);
    }

    private static void tick(CrucibleBlockEntity furnace, int ticks) {
        for (int tick = 0; tick < ticks; tick++) {
            CrucibleBlockEntity.serverTick(furnace.getLevel(), furnace.getBlockPos(), furnace.getBlockState(), furnace);
        }
    }
}
