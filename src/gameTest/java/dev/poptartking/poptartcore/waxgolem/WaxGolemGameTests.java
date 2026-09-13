package dev.poptartking.poptartcore.waxgolem;

import com.farcr.nomansland.common.block.cauldrons.FourLayeredCauldronBlock;
import com.farcr.nomansland.common.block.tap.TapBlock;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import dev.poptartking.poptartcore.registry.PoptartCoreEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WaxGolemGameTests {
    private WaxGolemGameTests() {}

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void savesLifecycleAndHiveMemory(GameTestHelper helper) {
        WaxGolem original = createGolem(helper, new BlockPos(3, 1, 3));
        original.setHome(new BlockPos(2, 1, 2));
        original.setTool(new ItemStack(Items.SHEARS));
        original.feedWax(1);
        original.hive(new BlockPos(5, 2, 5)).seen(4, helper.getLevel().getGameTime());

        CompoundTag saved = new CompoundTag();
        original.addAdditionalSaveData(saved);
        WaxGolem restored = PoptartCoreEntities.WAX_GOLEM.get().create(helper.getLevel());
        helper.assertTrue(restored != null, "Wax Golem entity type failed to create");
        restored.readAdditionalSaveData(saved);

        helper.assertTrue(restored.lit(), "Lit state did not survive saving");
        helper.assertTrue(restored.home().equals(new BlockPos(2, 1, 2)), "Home position did not survive saving");
        helper.assertTrue(restored.hives().size() == 1, "Hive memory did not survive saving");
        helper.assertTrue(restored.hives().getFirst().honeyLevel() == 4, "Saved hive honey level changed");
        helper.assertTrue(restored.tool().is(Items.SHEARS), "Held tool did not survive saving");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void constructsFromWaxBlocksAndPumpkin(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos relativeLower = new BlockPos(3, 1, 3);
        BlockPos lower = helper.absolutePos(relativeLower);
        BlockPos upper = lower.above();
        BlockPos head = upper.above();
        level.setBlockAndUpdate(lower, PoptartCoreBlocks.WAX_BLOCK.get().defaultBlockState());
        level.setBlockAndUpdate(upper, PoptartCoreBlocks.WAX_BLOCK.get().defaultBlockState());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockSnapshot snapshot = BlockSnapshot.create(level.dimension(), level, head);
        level.setBlockAndUpdate(head, Blocks.CARVED_PUMPKIN.defaultBlockState());

        NeoForge.EVENT_BUS.post(new BlockEvent.EntityPlaceEvent(snapshot, level.getBlockState(upper), player));

        helper.assertEntityPresent(PoptartCoreEntities.WAX_GOLEM.get(), relativeLower);
        helper.assertTrue(level.getBlockState(lower).isAir(), "Lower wax block was not consumed");
        helper.assertTrue(level.getBlockState(upper).isAir(), "Upper wax block was not consumed");
        helper.assertTrue(level.getBlockState(head).isAir(), "Carved pumpkin was not consumed");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void extinguishingClearsSittingPose(GameTestHelper helper) {
        WaxGolem golem = createGolem(helper, new BlockPos(3, 1, 3));
        golem.setSitting(true);

        golem.extinguish();

        helper.assertTrue(!golem.lit(), "Wax Golem remained lit after being extinguished");
        helper.assertTrue(!golem.sitting(), "Extinguished Wax Golem retained its sunken sitting pose");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void consumableIgnitersRelightAndAreConsumed(GameTestHelper helper) {
        WaxGolem golem = createGolem(helper, new BlockPos(3, 1, 3));
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        List<Item> igniters = List.of(Items.FIRE_CHARGE, Items.TORCH, Items.CANDLE);
        for (Item igniter : igniters) {
            golem.extinguish();
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(igniter, 2));

            player.interactOn(golem, InteractionHand.MAIN_HAND);

            helper.assertTrue(golem.lit(), igniter + " did not relight the Wax Golem");
            helper.assertTrue(
                    player.getMainHandItem().getCount() == 1,
                    "Relighting the Wax Golem did not consume exactly one " + igniter);
        }
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void spentGolemDoesNotWasteIgniter(GameTestHelper helper) {
        WaxGolem golem = createGolem(helper, new BlockPos(3, 1, 3));
        golem.spendLife(WaxGolem.LIFETIME_TICKS);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FIRE_CHARGE, 2));

        player.interactOn(golem, InteractionHand.MAIN_HAND);

        helper.assertTrue(!golem.lit(), "Completely spent Wax Golem relit without being rewaxed");
        helper.assertTrue(player.getMainHandItem().getCount() == 2, "Spent Wax Golem consumed an unusable igniter");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void harvestsSmokedFullHive(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos golemPos = helper.absolutePos(new BlockPos(3, 1, 3));
        BlockPos hivePos = helper.absolutePos(new BlockPos(3, 2, 4));
        BlockPos campfirePos = hivePos.below(2);
        level.setBlockAndUpdate(golemPos.below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(campfirePos, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
        level.setBlockAndUpdate(
                hivePos,
                Blocks.BEEHIVE
                        .defaultBlockState()
                        .setValue(BeehiveBlock.FACING, Direction.NORTH)
                        .setValue(BeehiveBlock.HONEY_LEVEL, 5));

        WaxGolem golem = createGolem(helper, new BlockPos(3, 1, 3));
        golem.setTool(new ItemStack(Items.SHEARS));
        golem.setState(WaxGolemState.ACTIVE);
        golem.observeHive(hivePos);
        HarvestHiveGoal goal = new HarvestHiveGoal(golem);
        helper.assertTrue(goal.canUse(), "Wax Golem did not select a full smoked hive");
        goal.start();
        goal.tick();

        helper.assertTrue(
                level.getBlockState(hivePos).getValue(BeehiveBlock.HONEY_LEVEL) == 0,
                "Wax Golem did not empty the hive");
        int honeycomb = level
                .getEntitiesOfClass(ItemEntity.class, golem.getBoundingBox().inflate(4))
                .stream()
                .filter(item -> item.getItem().is(Items.HONEYCOMB))
                .mapToInt(item -> item.getItem().getCount())
                .sum();
        helper.assertTrue(honeycomb == 2, "Wax Golem did not produce two honeycombs");
        helper.assertTrue(golem.tool().getDamageValue() == 1, "Wax Golem did not damage its shears");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void depositsItemsAndUsesHoneyCauldrons(GameTestHelper helper) {
        var level = helper.getLevel();
        WaxGolem golem = createGolem(helper, new BlockPos(3, 1, 3));
        BlockPos chestPos = helper.absolutePos(new BlockPos(5, 1, 3));
        BlockPos cauldronPos = helper.absolutePos(new BlockPos(3, 1, 5));
        level.setBlockAndUpdate(chestPos, Blocks.CHEST.defaultBlockState());
        level.setBlockAndUpdate(cauldronPos, Blocks.CAULDRON.defaultBlockState());

        ItemStack honeycomb = new ItemStack(Items.HONEYCOMB, 2);
        helper.assertTrue(DepositSites.insert(golem, chestPos, honeycomb), "Wax Golem could not insert into a chest");
        helper.assertTrue(honeycomb.isEmpty(), "Chest insertion left carried honeycomb behind");
        helper.assertTrue(
                ContainerAccess.container(level, chestPos).getItem(0).getCount() == 2,
                "Chest did not receive both honeycombs");

        helper.assertTrue(HoneyCauldrons.pourBottle(level, cauldronPos), "No Man's Land honey cauldron rejected honey");
        helper.assertTrue(
                HoneyCauldrons.level(level.getBlockState(cauldronPos)) == 1, "Honey cauldron level was wrong");
        helper.assertTrue(
                HoneyCauldrons.drawBottle(level, cauldronPos).is(Items.HONEY_BOTTLE),
                "Wax Golem could not draw honey from the cauldron");
        helper.assertTrue(
                level.getBlockState(cauldronPos).is(Blocks.CAULDRON), "Drained cauldron did not become empty");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void usesFullDoubleChestInventory(GameTestHelper helper) {
        var level = helper.getLevel();
        WaxGolem golem = createGolem(helper, new BlockPos(3, 1, 3));
        BlockPos leftPos = helper.absolutePos(new BlockPos(5, 1, 3));
        BlockPos rightPos = leftPos.east();
        level.setBlockAndUpdate(
                leftPos,
                Blocks.CHEST
                        .defaultBlockState()
                        .setValue(ChestBlock.FACING, Direction.NORTH)
                        .setValue(ChestBlock.TYPE, ChestType.LEFT));
        level.setBlockAndUpdate(
                rightPos,
                Blocks.CHEST
                        .defaultBlockState()
                        .setValue(ChestBlock.FACING, Direction.NORTH)
                        .setValue(ChestBlock.TYPE, ChestType.RIGHT));
        Container doubleChest = ContainerAccess.container(level, leftPos);

        helper.assertTrue(doubleChest != null, "Wax Golem could not access the double chest");
        helper.assertTrue(doubleChest.getContainerSize() == 54, "Wax Golem only saw one half of the double chest");
        for (int slot = 0; slot < 27; slot++) {
            doubleChest.setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
        }
        ItemStack honeycomb = new ItemStack(Items.HONEYCOMB, 2);
        helper.assertTrue(DepositSites.insert(golem, leftPos, honeycomb), "Wax Golem ignored the empty chest half");
        helper.assertTrue(honeycomb.isEmpty(), "Double chest insertion left carried honeycomb behind");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 600)
    public static void automaticallyHarvestsCollectsAndDeposits(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos hivePos = helper.absolutePos(new BlockPos(3, 2, 5));
        BlockPos chestPos = helper.absolutePos(new BlockPos(6, 1, 3));
        level.setBlockAndUpdate(hivePos.below(2), Blocks.CAMPFIRE.defaultBlockState());
        level.setBlockAndUpdate(
                hivePos,
                Blocks.BEEHIVE
                        .defaultBlockState()
                        .setValue(BeehiveBlock.FACING, Direction.NORTH)
                        .setValue(BeehiveBlock.HONEY_LEVEL, 5));
        level.setBlockAndUpdate(chestPos, Blocks.CHEST.defaultBlockState());
        WaxGolem golem = createGolem(helper, new BlockPos(3, 1, 3));
        golem.setTool(new ItemStack(Items.SHEARS));
        golem.setState(WaxGolemState.ACTIVE);

        helper.succeedWhen(() -> {
            Container chest = ContainerAccess.container(level, chestPos);
            int deposited = 0;
            for (int slot = 0; slot < chest.getContainerSize(); slot++) {
                ItemStack stack = chest.getItem(slot);
                if (stack.is(Items.HONEYCOMB)) {
                    deposited += stack.getCount();
                }
            }
            helper.assertTrue(
                    level.getBlockState(hivePos).getValue(BeehiveBlock.HONEY_LEVEL) == 0,
                    "Scheduled AI did not harvest the hive");
            helper.assertTrue(deposited == 2, "Scheduled AI did not collect and deposit two honeycombs");
        });
    }

    @GameTest(template = "millstone_test", timeoutTicks = 600)
    public static void automaticallyFerriesTappedHoney(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos source = helper.absolutePos(new BlockPos(3, 1, 5));
        BlockPos tap = source.above();
        BlockPos hive = tap.south();
        BlockPos destination = helper.absolutePos(new BlockPos(6, 1, 5));
        level.setBlockAndUpdate(
                source, NMLBlocks.HONEY_CAULDRON.get().defaultBlockState().setValue(FourLayeredCauldronBlock.LEVEL, 2));
        level.setBlockAndUpdate(
                tap, NMLBlocks.TAP.get().defaultBlockState().setValue(TapBlock.FACING, Direction.NORTH));
        level.setBlockAndUpdate(hive, Blocks.BEEHIVE.defaultBlockState());
        level.setBlockAndUpdate(destination, Blocks.CAULDRON.defaultBlockState());
        WaxGolem golem = createGolem(helper, new BlockPos(3, 1, 3));
        golem.setTool(new ItemStack(Items.GLASS_BOTTLE, 2));
        golem.setState(WaxGolemState.ACTIVE);

        helper.succeedWhen(() -> {
            helper.assertTrue(
                    HoneyCauldrons.level(level.getBlockState(source)) == 1,
                    "Scheduled AI did not draw honey from the tap-fed cauldron");
            helper.assertTrue(
                    HoneyCauldrons.level(level.getBlockState(destination)) == 1,
                    "Scheduled AI did not pour honey into the destination cauldron");
        });
    }

    private static WaxGolem createGolem(GameTestHelper helper, BlockPos relativePos) {
        WaxGolem golem = PoptartCoreEntities.WAX_GOLEM.get().create(helper.getLevel());
        helper.assertTrue(golem != null, "Wax Golem entity type failed to create");
        BlockPos position = helper.absolutePos(relativePos);
        golem.moveTo(position.getX() + 0.5D, position.getY(), position.getZ() + 0.5D, 0.0F, 0.0F);
        golem.setHome(position);
        helper.getLevel().addFreshEntity(golem);
        return golem;
    }
}
