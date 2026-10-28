package dev.poptartking.poptartcore.hammer;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.bloomery.IronBloomBlock;
import dev.poptartking.poptartcore.registry.PoptartCoreTags;
import dev.poptartking.poptartcore.registry.PoptartToolItems;
import dev.poptartking.poptartcore.registry.PoptartWorkstationBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HammerItemGameTests {
    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void hammerRetainsDurabilityRepairAndSharedTag(GameTestHelper helper) {
        var hammer = new ItemStack(PoptartToolItems.HAMMER.get());
        var repair = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("supplementaries:slidy_block")));
        helper.assertTrue(
                hammer.getMaxDamage() == 720 && hammer.getMaxStackSize() == 1 && hammer.is(PoptartCoreTags.HAMMERS),
                "Hammer retains durability, stacking and compatibility");
        helper.assertTrue(hammer.getItem().isValidRepairItem(hammer, repair), "Slidy block repairs the hammer");
        helper.assertTrue(
                !hammer.getItem().isValidRepairItem(hammer, new ItemStack(Items.DIRT)),
                "Unrelated items cannot repair the hammer");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void hammerProcessesExactlyOneBloomIntoNineNuggets(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(1, 1, 1));
        var state =
                PoptartWorkstationBlocks.IRON_BLOOM.get().defaultBlockState().setValue(IronBloomBlock.BLOOMS, 2);
        level.setBlockAndUpdate(pos, state);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(PoptartToolItems.HAMMER.get()));
        state.getBlock().playerWillDestroy(level, pos, state, player);
        helper.assertTrue(level.getBlockState(pos).getValue(IronBloomBlock.BLOOMS) == 1, "Only one bloom is processed");
        int nuggets = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(1)).stream()
                .filter(e -> e.getItem().is(Items.IRON_NUGGET))
                .mapToInt(e -> e.getItem().getCount())
                .sum();
        helper.assertTrue(nuggets == 9, "One processed bloom drops nine iron nuggets");
        helper.succeed();
    }
}
