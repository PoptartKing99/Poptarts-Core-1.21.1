package dev.poptartking.poptartcore.fruit;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartFruitBlocks;
import dev.poptartking.poptartcore.registry.PoptartMaterialItems;
import java.util.HashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@EventBusSubscriber(modid = PoptartCore.MOD_ID)
@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LemonGrowthGameTests {
    private static final BlockPos FRUIT_POS = new BlockPos(2, 2, 2);

    @SubscribeEvent
    public static void template(ServerAboutToStartEvent event) throws Exception {
        event.getServer()
                .getStructureManager()
                .getOrCreate(PoptartCore.location("lemon_growth_test"))
                .load(
                        event.getServer().registryAccess().lookupOrThrow(Registries.BLOCK),
                        TagParser.parseTag("{size:[5,5,5],palette:[{Name:\"minecraft:air\"}],blocks:[],entities:[]}"));
    }

    @GameTest(template = "lemon_growth_test")
    public static void lemonGrowsThroughAllStagesAndRegrowsAfterPicking(GameTestHelper helper) {
        plantFloweringLeaf(helper);
        helper.startSequence()
                .thenWaitUntil(() -> {
                    helper.assertTrue(
                            helper.getLevel().getRawBrightness(helper.absolutePos(FRUIT_POS), 0) >= 9,
                            "Waiting for light below the leaf");
                })
                .thenExecute(() -> {
                    var lemon = PoptartMaterialItems.LEMON.get();
                    var player = helper.makeMockPlayer(GameType.SURVIVAL);
                    for (int age = 0; age <= LemonFruitBlock.RIPE_AGE; age++) {
                        growthTick(helper);
                        helper.assertBlockPresent(PoptartFruitBlocks.LEMON_FRUIT.get(), FRUIT_POS);
                        helper.assertBlockProperty(FRUIT_POS, LemonFruitBlock.AGE, age);
                        if (age < LemonFruitBlock.RIPE_AGE) {
                            helper.useBlock(FRUIT_POS, player);
                            helper.assertTrue(player.getInventory().countItem(lemon) == 0, "Unripe lemon was picked");
                            helper.assertBlockProperty(FRUIT_POS, LemonFruitBlock.AGE, age);
                        }
                    }
                    growthTick(helper);
                    helper.assertBlockProperty(FRUIT_POS, LemonFruitBlock.AGE, LemonFruitBlock.RIPE_AGE);
                    helper.useBlock(FRUIT_POS, player);
                    helper.useBlock(FRUIT_POS, player);
                    helper.assertBlockPresent(Blocks.AIR, FRUIT_POS);
                    helper.assertBlockPresent(PoptartFruitBlocks.FLOWERING_LEMON_LEAVES.get(), FRUIT_POS.above());
                    helper.assertTrue(player.getInventory().countItem(lemon) == 1, "Expected exactly one lemon");
                    growthTick(helper);
                    helper.assertBlockProperty(FRUIT_POS, LemonFruitBlock.AGE, 0);
                })
                .thenSucceed();
    }

    @GameTest(template = "lemon_growth_test")
    public static void lemonGrowthNeverReplacesAnOccupiedBlock(GameTestHelper helper) {
        plantFloweringLeaf(helper);
        helper.setBlock(FRUIT_POS, Blocks.STONE);
        growthTick(helper);
        helper.assertBlockPresent(Blocks.STONE, FRUIT_POS);
        helper.succeed();
    }

    @GameTest(template = "lemon_growth_test")
    public static void lemonFallsWhenTheFloweringLeafIsRemoved(GameTestHelper helper) {
        plantFloweringLeaf(helper);
        helper.setBlock(FRUIT_POS, PoptartFruitBlocks.LEMON_FRUIT.get());
        helper.assertTrue(
                helper.getBlockState(FRUIT_POS).canSurvive(helper.getLevel(), helper.absolutePos(FRUIT_POS)),
                "Flowering leaf should support a lemon");
        helper.setBlock(FRUIT_POS.above(), Blocks.OAK_LEAVES);
        helper.assertBlockPresent(Blocks.AIR, FRUIT_POS);
        helper.succeed();
    }

    @GameTest(template = "lemon_growth_test")
    public static void boneMealAdvancesOneStageAndOnlyRipeFruitDropsLemons(GameTestHelper helper) {
        plantFloweringLeaf(helper);
        var fruit = PoptartFruitBlocks.LEMON_FRUIT.get();
        var level = helper.getLevel();
        var pos = helper.absolutePos(FRUIT_POS);
        helper.setBlock(FRUIT_POS, fruit);
        level.destroyBlock(pos, true);
        helper.assertTrue(lemonDrops(helper) == 0, "Breaking unripe fruit dropped a lemon");
        helper.setBlock(FRUIT_POS, fruit);
        var boneMeal = new ItemStack(Items.BONE_MEAL, 5);
        for (int age = 1; age <= LemonFruitBlock.RIPE_AGE; age++) {
            helper.assertTrue(BoneMealItem.growCrop(boneMeal, level, pos), "Expected bone meal to work");
            helper.assertBlockProperty(FRUIT_POS, LemonFruitBlock.AGE, age);
        }
        helper.assertTrue(!BoneMealItem.growCrop(boneMeal, level, pos), "Ripe lemons should not consume bone meal");
        helper.assertTrue(boneMeal.getCount() == 1, "Expected one bone meal per stage");
        level.destroyBlock(pos, true);
        helper.assertTrue(lemonDrops(helper) == 1, "Expected one ripe lemon drop");
        helper.succeed();
    }

    private static void plantFloweringLeaf(GameTestHelper helper) {
        helper.setBlock(FRUIT_POS.below(), Blocks.GLOWSTONE);
        helper.setBlock(
                FRUIT_POS.above(),
                PoptartFruitBlocks.FLOWERING_LEMON_LEAVES
                        .get()
                        .defaultBlockState()
                        .setValue(LeavesBlock.PERSISTENT, true));
    }

    @GameTest(template = "lemon_growth_test")
    public static void hangingLemonOffsetsMoveTheSelectionAndCollisionShapes(GameTestHelper helper) {
        var level = helper.getLevel();
        var fruit = PoptartFruitBlocks.LEMON_FRUIT.get().defaultBlockState();
        var xOffsets = new HashSet<Double>();
        var yOffsets = new HashSet<Double>();
        var zOffsets = new HashSet<Double>();
        int[] bottomPixels = {11, 10, 10, 9, 9};
        for (BlockPos relative : BlockPos.betweenClosed(1, 2, 1, 3, 2, 3)) {
            var pos = helper.absolutePos(relative);
            var offset = fruit.getOffset(level, pos);
            xOffsets.add(offset.x);
            yOffsets.add(offset.y);
            zOffsets.add(offset.z);
            helper.assertTrue(offset.y >= -3.0 / 16 && offset.y <= 0, "The stem must stay attached to the leaf");

            for (int age = 0; age <= LemonFruitBlock.RIPE_AGE; age++) {
                var state = fruit.setValue(LemonFruitBlock.AGE, age);
                helper.assertTrue(state.getOffset(level, pos).equals(offset), "Fruit must not jump when it ripens");
                var expected =
                        new AABB(5.5 / 16, bottomPixels[age] / 16.0, 7.5 / 16, 9.5 / 16, 1, 11.5 / 16).move(offset);
                var outline = state.getShape(level, pos);
                var actual = outline.bounds();
                helper.assertTrue(
                        expected.getMinPosition().distanceToSqr(actual.getMinPosition()) < 1.0E-12
                                && expected.getMaxPosition().distanceToSqr(actual.getMaxPosition()) < 1.0E-12,
                        "Selection bounds must follow the rendered fruit at age " + age);
                helper.assertTrue(
                        !Shapes.joinIsNotEmpty(outline, state.getCollisionShape(level, pos), BooleanOp.NOT_SAME),
                        "Collision must match the lemon outline at age " + age);
            }
        }
        helper.assertTrue(
                xOffsets.size() > 1 && yOffsets.size() > 1 && zOffsets.size() > 1,
                "Fruit positions should vary on all three axes");
        helper.succeed();
    }

    private static void growthTick(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(FRUIT_POS.above());
        var state = level.getBlockState(pos);
        helper.assertTrue(state.isRandomlyTicking(), "Flowering leaf must receive random ticks");
        PoptartFruitBlocks.FLOWERING_LEMON_LEAVES
                .get()
                .randomTick(state, level, pos, successfulGrowthRoll());
    }

    private static RandomSource successfulGrowthRoll() {
        for (long seed = 0; seed < 128; seed++) {
            if (RandomSource.create(seed).nextInt(7) == 0) return RandomSource.create(seed);
        }
        throw new AssertionError("Could not choose a deterministic successful growth roll");
    }

    private static int lemonDrops(GameTestHelper helper) {
        return helper.getLevel()
                .getEntitiesOfClass(
                        ItemEntity.class,
                        new AABB(helper.absolutePos(FRUIT_POS)).inflate(1),
                        item -> item.getItem().is(PoptartMaterialItems.LEMON.get()))
                .stream()
                .mapToInt(item -> item.getItem().getCount())
                .sum();
    }
}
