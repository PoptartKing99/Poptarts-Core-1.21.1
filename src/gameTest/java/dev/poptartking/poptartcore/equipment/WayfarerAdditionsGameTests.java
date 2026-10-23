package dev.poptartking.poptartcore.equipment;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import dev.poptartking.poptartcore.registry.PoptartMaterialItems;
import dev.poptartking.poptartcore.registry.PoptartRingItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WayfarerAdditionsGameTests {
    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void magnetPullsOnlyReadyItemsInRangeAndStopsWhenUnequipped(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var pos = Vec3.atCenterOf(helper.absolutePos(new BlockPos(4, 2, 4)));
        player.setPos(pos);
        var ring = new ItemStack(PoptartRingItems.MAGNET_RING.get());
        var item = (EquipmentRing) ring.getItem();
        helper.assertTrue(
                ring.getMaxDamage() == 547
                        && item.isValidRepairItem(ring, new ItemStack(PoptartMaterialItems.MAGNETITE.get())),
                "Wayfarer durability and magnetite repair");
        new PlayerAccessoryHandler(player).setStackInSlot(0, ring);
        helper.assertTrue(!EquipmentRing.canEquip(player, 1, ring.copy()), "Duplicate magnet ring rejected");
        var near = drop(helper, pos.add(2, .5, 0), false);
        var delayed = drop(helper, pos.add(-2, .5, 0), true);
        var distant = drop(helper, pos.add(5, .5, 0), false);
        RingEffects.tick(player);
        helper.assertTrue(
                near.getDeltaMovement().x < 0
                        && Math.abs(near.getDeltaMovement().length() - .02) < .000001,
                "Four-block magnet adds .02 acceleration toward player");
        helper.assertTrue(
                delayed.getDeltaMovement().equals(Vec3.ZERO)
                        && distant.getDeltaMovement().equals(Vec3.ZERO),
                "Pickup delay and distance respected");
        new PlayerAccessoryHandler(player).setStackInSlot(0, ItemStack.EMPTY);
        near.setDeltaMovement(Vec3.ZERO);
        RingEffects.tick(player);
        helper.assertTrue(near.getDeltaMovement().equals(Vec3.ZERO), "Unequipping stops pulling");
        near.discard();
        delayed.discard();
        distant.discard();
        helper.succeed();
    }

    private static ItemEntity drop(GameTestHelper helper, Vec3 position, boolean delayed) {
        var item = new ItemEntity(helper.getLevel(), position.x, position.y, position.z, new ItemStack(Items.DIRT));
        item.setDeltaMovement(Vec3.ZERO);
        if (delayed) item.setPickUpDelay(40);
        else item.setNoPickUpDelay();
        helper.getLevel().addFreshEntity(item);
        return item;
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void crystalIsPlaceableWaterloggableAndRubberMatchesWool(GameTestHelper helper) {
        var crystal = PoptartCoreBlocks.QUARTZ_CRYSTAL.get();
        helper.assertTrue(
                new ItemStack(crystal).getItem() instanceof net.minecraft.world.item.BlockItem,
                "Existing quartz crystal ID is now a block item");
        var state = crystal.defaultBlockState();
        helper.assertTrue(
                state.getLightEmission() == 0 && state.hasProperty(AmethystClusterBlock.WATERLOGGED),
                "Crystal supports waterlogging without light");
        for (var direction : Direction.values()) {
            var pos = helper.absolutePos(new BlockPos(4, 3, 4));
            helper.getLevel()
                    .setBlockAndUpdate(pos.relative(direction.getOpposite()), Blocks.STONE.defaultBlockState());
            helper.assertTrue(
                    state.setValue(AmethystClusterBlock.FACING, direction).canSurvive(helper.getLevel(), pos),
                    "Crystal supports all six attachment directions");
        }
        helper.assertTrue(
                PoptartCoreBlocks.RUBBER_BLOCK.get().getFriction() == Blocks.BLACK_WOOL.getFriction(),
                "Rubber uses Wayfarer wool-like properties");
        helper.succeed();
    }
}
