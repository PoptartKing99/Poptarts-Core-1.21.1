package dev.poptartking.poptartcore.treetap;

import com.farcr.nomansland.common.registry.items.NMLItems;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import dev.poptartking.poptartcore.registry.PoptartCoreFluids;
import dev.poptartking.poptartcore.registry.PoptartMaterialItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TreeTapGameTests {
    private TreeTapGameTests() {}

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void losingSupportImmediatelyDropsTapAndAttachedBucket(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(3, 2, 3));
        var tap = PoptartCoreBlocks.TREE_TAP.get();
        var bounds = new net.minecraft.world.phys.AABB(pos).inflate(2);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            for (int fill : new int[] {0, 3, 5, -1}) {
                level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, bounds)
                        .forEach(net.minecraft.world.entity.Entity::discard);
                var support = pos.relative(facing.getOpposite());
                level.setBlockAndUpdate(support, Blocks.SPRUCE_LOG.defaultBlockState());
                level.setBlockAndUpdate(
                        pos,
                        tap.defaultBlockState()
                                .setValue(TreeTapBlock.FACING, facing)
                                .setValue(TreeTapBlock.WOOD, TreeTapBlock.Wood.SPRUCE)
                                .setValue(TreeTapBlock.HAS_BUCKET, fill >= 0)
                                .setValue(TreeTapBlock.FILL_LEVEL, Math.max(0, fill))
                                .setValue(TreeTapBlock.FULL, fill == 5));
                if (fill >= 0 && fill < 5) TreeTapBlock.scheduleNextFill(level, pos);
                level.setBlockAndUpdate(pos.above(), Blocks.STONE.defaultBlockState());
                helper.assertTrue(level.getBlockState(pos).is(tap), "Unrelated neighbor changes keep tap attached");
                level.setBlockAndUpdate(support, Blocks.AIR.defaultBlockState());
                helper.assertTrue(
                        level.getBlockState(pos).isAir(),
                        "Tap breaks in the same support update, facing=" + facing + ", fill=" + fill);
                var drops = level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, bounds);
                int taps = drops.stream()
                        .filter(entity -> entity.getItem().is(tap.asItem()))
                        .mapToInt(entity -> entity.getItem().getCount())
                        .sum();
                int buckets = drops.stream()
                        .filter(entity -> entity.getItem().is(Items.BUCKET))
                        .mapToInt(entity -> entity.getItem().getCount())
                        .sum();
                helper.assertTrue(taps == 1 && buckets == (fill >= 0 ? 1 : 0), "Tap and attached bucket drop once");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 45)
    public static void eachTenUnitFillPlaysOneIndependentAnimation(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(2, 2, 3));
        var otherPos = pos.east(2);
        for (var target : new BlockPos[] {pos, otherPos}) {
            level.setBlockAndUpdate(target.south(), Blocks.JUNGLE_LOG.defaultBlockState());
            level.setBlockAndUpdate(
                    target,
                    PoptartCoreBlocks.TREE_TAP
                            .get()
                            .defaultBlockState()
                            .setValue(TreeTapBlock.WOOD, TreeTapBlock.Wood.JUNGLE)
                            .setValue(TreeTapBlock.HAS_BUCKET, true));
        }
        var first = (TreeTapBlockEntity) level.getBlockEntity(pos);
        var second = (TreeTapBlockEntity) level.getBlockEntity(otherPos);
        helper.runAtTickTime(1, () -> {
            helper.assertTrue(first.dripFrame(0) == -1 && second.dripFrame(0) == -1, "Idle taps do not loop");
            first.addDrip();
            helper.assertTrue(
                    first.collectedAmount() == 10 && first.dripFrame(0) == 0, "Ten units starts one animation");
            helper.assertTrue(second.collectedAmount() == 0 && second.dripFrame(0) == -1, "Neighbor stays idle");
        });
        helper.runAtTickTime(9, () -> {
            second.addDrip();
            helper.assertTrue(
                    first.dripFrame(0) == 4 && second.dripFrame(0) == 0, "Each tap has its own animation phase");
        });
        helper.runAtTickTime(21, () -> {
            helper.assertTrue(first.dripFrame(0) == -1 && second.dripFrame(0) == 6, "Animation stops after one pass");
            helper.assertTrue(
                    first.collectedAmount() == 10 && second.collectedAmount() == 10,
                    "Animation itself adds no extra contents");
        });
        helper.runAtTickTime(30, () -> {
            helper.assertTrue(second.dripFrame(0) == -1, "Second animation also finishes");
            var saved = first.saveWithFullMetadata(level.registryAccess());
            var restored = new TreeTapBlockEntity(pos, first.getBlockState());
            restored.loadWithComponents(saved, level.registryAccess());
            restored.setLevel(level);
            helper.assertTrue(
                    restored.collectedAmount() == 10 && restored.dripFrame(0) == -1,
                    "Reload keeps contents without replaying old drips");
            helper.succeed();
        });
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void oneHundredDripsFillBucketAndFluidTransferDoesNotAnimate(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(3, 2, 3));
        level.setBlockAndUpdate(pos.south(), Blocks.JUNGLE_LOG.defaultBlockState());
        level.setBlockAndUpdate(
                pos,
                PoptartCoreBlocks.TREE_TAP
                        .get()
                        .defaultBlockState()
                        .setValue(TreeTapBlock.WOOD, TreeTapBlock.Wood.JUNGLE)
                        .setValue(TreeTapBlock.HAS_BUCKET, true));
        var storage = (TreeTapBlockEntity) level.getBlockEntity(pos);
        var fluid = storage.fluidHandler();
        fluid.fill(new FluidStack(PoptartCoreFluids.LATEX.source().get(), 10), IFluidHandler.FluidAction.EXECUTE);
        helper.assertTrue(storage.dripFrame(0) == -1, "Pipe insertion is not a natural drip");
        fluid.drain(10, IFluidHandler.FluidAction.EXECUTE);
        for (int i = 1; i <= 100; i++) {
            storage.addDrip();
            helper.assertTrue(storage.collectedAmount() == i * 10, "Each drip adds exactly ten units");
        }
        storage.addDrip();
        helper.assertTrue(storage.isFull() && storage.collectedAmount() == 1000, "Full bucket stops filling");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void fillDelaysVaryWithinFifteenToTwentyOneSeconds(GameTestHelper helper) {
        var random = net.minecraft.util.RandomSource.create(19345);
        long total = 0;
        var delays = new java.util.HashSet<Integer>();
        for (int i = 0; i < 10000; i++) {
            int delay = TreeTapBlock.nextFillDelay(random);
            helper.assertTrue(delay >= 300 && delay <= 420, "Every drip stays within 15-21 seconds");
            total += delay;
            delays.add(delay);
        }
        helper.assertTrue(delays.size() == 121, "All tick delays in the range are represented");
        helper.assertTrue(Math.abs(total / 10000.0 - 360) < 3, "Average stays close to 18 seconds per drip");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void neighboringBucketsReceiveIndependentFillDeadlines(GameTestHelper helper) {
        var level = helper.getLevel();
        var tap = PoptartCoreBlocks.TREE_TAP.get();
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var deadlines = new java.util.HashSet<Long>();
        for (int x = 1; x <= 5; x++) {
            var pos = helper.absolutePos(new BlockPos(x, 2, 3));
            level.setBlockAndUpdate(pos.south(), Blocks.SPRUCE_LOG.defaultBlockState());
            level.setBlockAndUpdate(pos, tap.defaultBlockState().setValue(TreeTapBlock.WOOD, TreeTapBlock.Wood.SPRUCE));
            tap.useItemOn(
                    new ItemStack(Items.BUCKET),
                    level.getBlockState(pos),
                    level,
                    pos,
                    player,
                    net.minecraft.world.InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(pos), Direction.NORTH, pos, false));
            var ticks = (net.minecraft.world.ticks.LevelChunkTicks<net.minecraft.world.level.block.Block>)
                    level.getChunkAt(pos).getBlockTicks();
            var tick = ticks.getAll()
                    .filter(scheduled -> scheduled.pos().equals(pos) && scheduled.type() == tap)
                    .findFirst()
                    .orElseThrow();
            long delay = tick.triggerTick() - level.getGameTime();
            helper.assertTrue(delay >= 300 && delay <= 420, "Bucket attachment schedules a randomized drip");
            deadlines.add(tick.triggerTick());
            TreeTapBlock.scheduleNextFill(level, pos);
            helper.assertTrue(
                    ticks.getAll()
                                    .filter(scheduled -> scheduled.pos().equals(pos))
                                    .count()
                            == 1,
                    "Existing timer is not duplicated");
        }
        helper.assertTrue(deadlines.size() > 1, "Simultaneously attached buckets have different deadlines");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void jungleBucketGivesOneLatexBottlePerFillStage(GameTestHelper helper) {
        BlockPos tapPos = helper.absolutePos(new BlockPos(3, 2, 3));
        var level = helper.getLevel();
        level.setBlockAndUpdate(tapPos.south(), Blocks.JUNGLE_LOG.defaultBlockState());
        TreeTapBlock tap = PoptartCoreBlocks.TREE_TAP.get();
        BlockState bucket = tap.defaultBlockState()
                .setValue(TreeTapBlock.FACING, Direction.NORTH)
                .setValue(TreeTapBlock.WOOD, TreeTapBlock.Wood.JUNGLE)
                .setValue(TreeTapBlock.HAS_BUCKET, true);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var hit = new BlockHitResult(Vec3.atCenterOf(tapPos), Direction.NORTH, tapPos, false);
        ItemStack bottles = new ItemStack(Items.GLASS_BOTTLE, 5);
        level.setBlockAndUpdate(tapPos, bucket);
        var tapTank = level.getCapability(Capabilities.FluidHandler.BLOCK, tapPos, Direction.NORTH);
        helper.assertTrue(tapTank != null, "Jungle tap has no fluid tank");

        helper.assertTrue(
                TreeTapBlock.Wood.fromLog(Blocks.JUNGLE_LOG.defaultBlockState()) == TreeTapBlock.Wood.JUNGLE,
                "Jungle log is not a tap source");
        for (int stage = 1; stage <= 5; stage++) {
            int toAdd = stage * LatexBottleFluidHandler.AMOUNT
                    - tapTank.getFluidInTank(0).getAmount();
            int added = tapTank.fill(
                    new FluidStack(PoptartCoreFluids.LATEX.source().get(), toAdd), IFluidHandler.FluidAction.EXECUTE);
            helper.assertTrue(added == toAdd, "Tap did not accept the expected latex amount");
            BlockState filled = level.getBlockState(tapPos);
            helper.assertTrue(filled.getValue(TreeTapBlock.FILL_LEVEL) == stage, "Tank did not update the model");
            tap.useItemOn(bottles, filled, level, tapPos, player, net.minecraft.world.InteractionHand.MAIN_HAND, hit);

            helper.assertTrue(bottles.getCount() == 5 - stage, "Wrong number of glass bottles consumed");
            int latex = player.getInventory().items.stream()
                    .filter(stack -> stack.is(PoptartMaterialItems.LATEX_BOTTLE.get()))
                    .mapToInt(ItemStack::getCount)
                    .sum();
            helper.assertTrue(latex == stage, "Wrong number of latex bottles at stage " + stage);
            BlockState remaining = level.getBlockState(tapPos);
            helper.assertTrue(remaining.getValue(TreeTapBlock.HAS_BUCKET), "Collecting latex removed the bucket");
            helper.assertTrue(!remaining.getValue(TreeTapBlock.FULL), "Collecting latex left the bucket full");
            helper.assertTrue(
                    remaining.getValue(TreeTapBlock.FILL_LEVEL) == stage - 1,
                    "Collecting latex removed the wrong amount at stage " + stage);
            helper.assertTrue(
                    tapTank.getFluidInTank(0).getAmount() == (stage - 1) * LatexBottleFluidHandler.AMOUNT,
                    "Collecting latex removed the wrong fluid amount at stage " + stage);
        }
        ItemStack latexBottle = new ItemStack(PoptartMaterialItems.LATEX_BOTTLE.get());
        var bottleTank = latexBottle.getCapability(Capabilities.FluidHandler.ITEM);
        helper.assertTrue(bottleTank != null, "Latex bottle has no fluid capability");
        helper.assertTrue(
                bottleTank.getFluidInTank(0).getAmount() == LatexBottleFluidHandler.AMOUNT,
                "Latex bottle does not contain 200 mB");
        var barrelTank = new net.neoforged.neoforge.fluids.capability.templates.FluidTank(1000);
        var transfer =
                net.neoforged.neoforge.fluids.FluidUtil.tryEmptyContainer(latexBottle, barrelTank, 1000, player, true);
        helper.assertTrue(transfer.isSuccess(), "Latex bottle could not empty into a fluid tank");
        helper.assertTrue(
                barrelTank.getFluidAmount() == LatexBottleFluidHandler.AMOUNT,
                "Fluid tank received the wrong latex amount");
        helper.assertTrue(transfer.getResult().is(Items.GLASS_BOTTLE), "Emptying latex did not return a glass bottle");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void spruceBucketGivesTwoResinPerFillStage(GameTestHelper helper) {
        BlockPos tapPos = helper.absolutePos(new BlockPos(3, 2, 3));
        var level = helper.getLevel();
        level.setBlockAndUpdate(tapPos.south(), Blocks.SPRUCE_LOG.defaultBlockState());
        TreeTapBlock tap = PoptartCoreBlocks.TREE_TAP.get();
        BlockState bucket = tap.defaultBlockState()
                .setValue(TreeTapBlock.FACING, Direction.NORTH)
                .setValue(TreeTapBlock.WOOD, TreeTapBlock.Wood.SPRUCE)
                .setValue(TreeTapBlock.HAS_BUCKET, true);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var hit = new BlockHitResult(Vec3.atCenterOf(tapPos), Direction.NORTH, tapPos, false);

        int expectedResin = 0;
        for (int stage = 1; stage <= 5; stage++) {
            if (stage == 1) level.setBlockAndUpdate(tapPos, bucket);
            var storage = (TreeTapBlockEntity) level.getBlockEntity(tapPos);
            for (int fill = 0; fill < stage * 20; fill++) storage.addDrip();
            BlockState filled = level.getBlockState(tapPos);
            player.setShiftKeyDown(true);
            tap.useWithoutItem(filled, level, tapPos, player, hit);
            helper.assertTrue(level.getBlockState(tapPos).equals(filled), "Sneaking removed a filled bucket");
            player.setShiftKeyDown(false);
            tap.useWithoutItem(filled, level, tapPos, player, hit);

            expectedResin += stage * 2;
            int resin = player.getInventory().items.stream()
                    .filter(stack -> stack.is(NMLItems.RESIN.get()))
                    .mapToInt(stack -> stack.getCount())
                    .sum();
            helper.assertTrue(resin == expectedResin, "Spruce bucket gave the wrong resin amount at stage " + stage);
            BlockState empty = level.getBlockState(tapPos);
            helper.assertTrue(empty.getValue(TreeTapBlock.HAS_BUCKET), "Resin collection removed the bucket");
            helper.assertTrue(!empty.getValue(TreeTapBlock.FULL), "Resin collection left the bucket full");
            helper.assertTrue(empty.getValue(TreeTapBlock.FILL_LEVEL) == 0, "Resin collection did not reset filling");
        }
        BlockState empty = level.getBlockState(tapPos);
        tap.useWithoutItem(empty, level, tapPos, player, hit);
        helper.assertTrue(
                level.getBlockState(tapPos).getValue(TreeTapBlock.HAS_BUCKET),
                "Normal right-click removed an empty bucket");
        player.setShiftKeyDown(true);
        tap.useWithoutItem(empty, level, tapPos, player, hit);
        helper.assertTrue(
                !level.getBlockState(tapPos).getValue(TreeTapBlock.HAS_BUCKET),
                "Sneaking did not remove an empty bucket");
        helper.assertTrue(
                player.getInventory().items.stream().anyMatch(stack -> stack.is(Items.BUCKET)),
                "Removing the bucket did not return it to the player");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void everyWoodSharesStorageAndPreservesSavedAmounts(GameTestHelper helper) {
        var pos = helper.absolutePos(new BlockPos(3, 2, 3));
        var level = helper.getLevel();
        var tap = PoptartCoreBlocks.TREE_TAP.get();
        for (TreeTapBlock.Wood wood : TreeTapBlock.Wood.values()) {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            var log =
                    switch (wood) {
                        case MAPLE ->
                            net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(
                                    net.minecraft.resources.ResourceLocation.parse("nomansland:maple_log"));
                        case PINE ->
                            net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(
                                    net.minecraft.resources.ResourceLocation.parse("nomansland:pine_log"));
                        case SPRUCE -> Blocks.SPRUCE_LOG;
                        case BIRCH -> Blocks.BIRCH_LOG;
                        case ACACIA -> Blocks.ACACIA_LOG;
                        case JUNGLE -> Blocks.JUNGLE_LOG;
                    };
            level.setBlockAndUpdate(pos.south(), log.defaultBlockState());
            level.setBlockAndUpdate(
                    pos,
                    tap.defaultBlockState().setValue(TreeTapBlock.WOOD, wood).setValue(TreeTapBlock.HAS_BUCKET, true));
            var storage = (TreeTapBlockEntity) level.getBlockEntity(pos);
            for (int stage = 1; stage <= 5; stage++) {
                for (int drip = 0; drip < 20; drip++) tap.tick(level.getBlockState(pos), level, pos, level.random);
                helper.assertTrue(
                        storage.collectedAmount() == stage * TreeTapBlockEntity.AMOUNT_PER_STAGE,
                        wood + " uses common amount storage");
                helper.assertTrue(
                        level.getBlockState(pos).getValue(TreeTapBlock.FILL_LEVEL) == stage,
                        wood + " visual level follows amount");
            }
            storage.addDrip();
            helper.assertTrue(
                    storage.collectedAmount() == TreeTapBlockEntity.CAPACITY && storage.isFull(), "Capacity is capped");
            var restored = new TreeTapBlockEntity(pos, level.getBlockState(pos));
            restored.loadWithComponents(storage.saveWithFullMetadata(level.registryAccess()), level.registryAccess());
            helper.assertTrue(restored.collectedAmount() == TreeTapBlockEntity.CAPACITY, wood + " saves its amount");
            var capability = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, Direction.NORTH);
            helper.assertTrue(
                    (capability != null) == (wood == TreeTapBlock.Wood.JUNGLE),
                    "Only latex exposes a fluid capability");
            if (wood == TreeTapBlock.Wood.MAPLE || wood == TreeTapBlock.Wood.JUNGLE) {
                var player = helper.makeMockPlayer(GameType.SURVIVAL);
                var bottle = new ItemStack(Items.GLASS_BOTTLE);
                tap.useItemOn(
                        bottle,
                        level.getBlockState(pos),
                        level,
                        pos,
                        player,
                        net.minecraft.world.InteractionHand.MAIN_HAND,
                        new BlockHitResult(Vec3.atCenterOf(pos), Direction.NORTH, pos, false));
                helper.assertTrue(storage.collectedAmount() == 800 && bottle.isEmpty(), "Bottle removes one serving");
                helper.assertTrue(level.getBlockTicks().hasScheduledTick(pos, tap), "Collection resumes filling");
                helper.assertTrue(
                        player.getInventory().items.stream()
                                .anyMatch(stack -> stack.is(
                                        wood == TreeTapBlock.Wood.MAPLE
                                                ? NMLItems.MAPLE_SYRUP_BOTTLE.get()
                                                : PoptartMaterialItems.LATEX_BOTTLE.get())),
                        "Bottle output unchanged");
            }
            if (wood == TreeTapBlock.Wood.PINE)
                helper.assertTrue(
                        storage.collectResin() == 10 && storage.collectedAmount() == 0,
                        "Pine still produces two resin per stage");
        }
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void oldSavesAndPartialLatexKeepTheirExactAmounts(GameTestHelper helper) {
        var pos = helper.absolutePos(new BlockPos(3, 2, 3));
        var level = helper.getLevel();
        var tap = PoptartCoreBlocks.TREE_TAP.get();
        for (TreeTapBlock.Wood wood : TreeTapBlock.Wood.values()) {
            var state = tap.defaultBlockState()
                    .setValue(TreeTapBlock.WOOD, wood)
                    .setValue(TreeTapBlock.HAS_BUCKET, true)
                    .setValue(TreeTapBlock.FILL_LEVEL, 3);
            var loaded = new TreeTapBlockEntity(pos, state);
            var old = new net.minecraft.nbt.CompoundTag();
            if (wood == TreeTapBlock.Wood.JUNGLE)
                old.put(
                        "Latex",
                        new FluidStack(PoptartCoreFluids.LATEX.source().get(), 425).save(level.registryAccess()));
            loaded.loadWithComponents(old, level.registryAccess());
            helper.assertTrue(
                    loaded.collectedAmount() == (wood == TreeTapBlock.Wood.JUNGLE ? 425 : 600),
                    wood + " migrates old saved contents");
            var restored = new TreeTapBlockEntity(pos, state);
            restored.loadWithComponents(loaded.saveWithFullMetadata(level.registryAccess()), level.registryAccess());
            helper.assertTrue(
                    restored.collectedAmount() == loaded.collectedAmount(), "Migration survives another save");
        }
        level.setBlockAndUpdate(pos.south(), Blocks.JUNGLE_LOG.defaultBlockState());
        level.setBlockAndUpdate(
                pos,
                tap.defaultBlockState()
                        .setValue(TreeTapBlock.WOOD, TreeTapBlock.Wood.JUNGLE)
                        .setValue(TreeTapBlock.HAS_BUCKET, true));
        var storage = (TreeTapBlockEntity) level.getBlockEntity(pos);
        var fluid = storage.fluidHandler();
        helper.assertTrue(
                fluid.fill(
                                        new FluidStack(
                                                PoptartCoreFluids.LATEX.source().get(), 425),
                                        IFluidHandler.FluidAction.SIMULATE)
                                == 425
                        && storage.collectedAmount() == 0,
                "Simulated fill changes nothing");
        fluid.fill(new FluidStack(PoptartCoreFluids.LATEX.source().get(), 425), IFluidHandler.FluidAction.EXECUTE);
        fluid.drain(225, IFluidHandler.FluidAction.SIMULATE);
        helper.assertTrue(storage.collectedAmount() == 425, "Simulated drain changes nothing");
        helper.assertTrue(
                storage.collectServing()
                        && storage.collectServing()
                        && !storage.collectServing()
                        && storage.collectedAmount() == 25,
                "Collection never discards a partial serving");
        helper.assertTrue(
                fluid.fill(
                                new FluidStack(net.minecraft.world.level.material.Fluids.WATER, 100),
                                IFluidHandler.FluidAction.EXECUTE)
                        == 0,
                "Wrong fluid rejected");
        helper.succeed();
    }
}
