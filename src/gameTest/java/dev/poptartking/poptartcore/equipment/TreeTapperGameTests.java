package dev.poptartking.poptartcore.equipment;

import com.farcr.nomansland.common.registry.items.NMLItems;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.decoration.TreeTapperBlock;
import dev.poptartking.poptartcore.decoration.TreeTapperBlockEntity;
import dev.poptartking.poptartcore.registry.PoptartCoreFluids;
import dev.poptartking.poptartcore.registry.PoptartWorkstationBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@net.neoforged.fml.common.EventBusSubscriber(modid = PoptartCore.MOD_ID)
@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TreeTapperGameTests {
    @net.neoforged.bus.api.SubscribeEvent
    public static void template(net.neoforged.neoforge.event.server.ServerAboutToStartEvent event) throws Exception {
        event.getServer()
                .getStructureManager()
                .getOrCreate(PoptartCore.location("tree_tapper_test"))
                .load(
                        event.getServer()
                                .registryAccess()
                                .lookupOrThrow(net.minecraft.core.registries.Registries.BLOCK),
                        net.minecraft.nbt.TagParser.parseTag(
                                "{size:[9,12,9],palette:[{Name:\"minecraft:air\"}],blocks:[],entities:[]}"));
    }

    @GameTest(template = "tree_tapper_test", timeoutTicks = 40)
    public static void placementRequiresLogsAndRoom(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(4, 2, 4));
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 3);
        helper.assertTrue(!place(helper, pos, Direction.UP), "Cannot place on ground");
        level.setBlock(pos.north(), Blocks.STONE.defaultBlockState(), 3);
        level.setBlock(pos.north().above(), Blocks.STONE.defaultBlockState(), 3);
        helper.assertTrue(!place(helper, pos, Direction.SOUTH), "Cannot mount stone");
        level.setBlock(pos.north(), Blocks.OAK_LOG.defaultBlockState(), 3);
        helper.assertTrue(!place(helper, pos, Direction.SOUTH), "Requires upper log too");
        level.setBlock(pos.north().above(), Blocks.OAK_LOG.defaultBlockState(), 3);
        level.setBlock(pos.above(), Blocks.STONE.defaultBlockState(), 3);
        helper.assertTrue(!place(helper, pos, Direction.SOUTH), "Cannot overwrite occupied upper space");
        helper.succeed();
    }

    @GameTest(template = "tree_tapper_test", timeoutTicks = 40)
    public static void mountsEverySideAndDropsExactlyOnce(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(4, 2, 4));
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            for (boolean breakUpper : new boolean[] {false, true}) {
                level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2))
                        .forEach(ItemEntity::discard);
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 3);
                level.setBlock(pos.relative(direction), Blocks.OAK_LOG.defaultBlockState(), 3);
                level.setBlock(pos.relative(direction).above(), Blocks.OAK_LOG.defaultBlockState(), 3);
                helper.assertTrue(place(helper, pos, direction.getOpposite()), "Place against " + direction);
                helper.assertTrue(
                        level.getBlockState(pos).getValue(TreeTapperBlock.FACING) == direction, "Faces supporting log");
                helper.assertTrue(
                        level.getBlockState(pos.above()).getValue(TreeTapperBlock.HALF) == DoubleBlockHalf.UPPER,
                        "Tap occupies upper cell");
                level.destroyBlock(breakUpper ? pos.above() : pos, true);
                helper.assertTrue(
                        level.getBlockState(pos).isAir()
                                && level.getBlockState(pos.above()).isAir(),
                        "Both parts removed");
                int count = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2)).stream()
                        .filter(e -> e.getItem()
                                .is(PoptartWorkstationBlocks.TREE_TAPPER.item().get()))
                        .mapToInt(e -> e.getItem().getCount())
                        .sum();
                helper.assertTrue(count == 1, "Exactly one tapper dropped; got " + count);
                level.setBlock(pos.relative(direction), Blocks.AIR.defaultBlockState(), 3);
                level.setBlock(pos.relative(direction).above(), Blocks.AIR.defaultBlockState(), 3);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "tree_tapper_test", timeoutTicks = 40)
    public static void removingEitherSupportingLogRemovesTapper(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(4, 2, 4));
        for (boolean upper : new boolean[] {false, true}) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 3);
            level.setBlock(pos.north(), Blocks.OAK_LOG.defaultBlockState(), 3);
            level.setBlock(pos.north().above(), Blocks.OAK_LOG.defaultBlockState(), 3);
            helper.assertTrue(place(helper, pos, Direction.SOUTH), "Valid mount");
            level.destroyBlock(upper ? pos.north().above() : pos.north(), false);
            helper.assertTrue(
                    level.getBlockState(pos).isAir()
                            && level.getBlockState(pos.above()).isAir(),
                    "Missing support removes whole tapper");
        }
        helper.succeed();
    }

    @GameTest(template = "tree_tapper_test", timeoutTicks = 80)
    public static void naturalTreesFillInTenMbStepsAndPauseWhenClosed(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(4, 2, 4));
        for (String species : new String[] {"maple", "pine", "spruce"}) {
            var id = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                    species.equals("spruce") ? "minecraft" : "nomansland", species + "_log");
            var log = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(id);
            var leaves = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                            id.getNamespace(), species + "_leaves"));
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 3);
            var root = pos.north().atY(pos.getY() - 2);
            level.setBlock(root, Blocks.DIRT.defaultBlockState(), 3);
            for (int y = 1; y <= 6; y++) level.setBlock(root.above(y), log.defaultBlockState(), 3);
            var canopy = leaves.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.PERSISTENT, false)
                    .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.DISTANCE, 1);
            level.setBlock(root.above(7), canopy, 3);
            helper.assertTrue(place(helper, pos, Direction.SOUTH), "Place " + species);
            var entity = (dev.poptartking.poptartcore.decoration.TreeTapperBlockEntity) level.getBlockEntity(pos);
            var expected = species.equals("maple")
                    ? dev.poptartking.poptartcore.registry.PoptartCoreFluids.MAPLE_SYRUP
                            .source()
                            .get()
                    : dev.poptartking.poptartcore.registry.PoptartCoreFluids.RESIN
                            .source()
                            .get();
            helper.assertTrue(
                    dev.poptartking.poptartcore.decoration.TapperTreeRules.fluidAt(
                                    level, pos.north().above())
                            == expected,
                    "NML natural-tree rule for " + species);
            var player = helper.makeMockPlayer(GameType.SURVIVAL);
            var hit = new BlockHitResult(Vec3.atCenterOf(pos.above()), Direction.SOUTH, pos.above(), false);
            level.getBlockState(pos.above()).useWithoutItem(level, player, hit);
            helper.assertTrue(level.getBlockState(pos).getValue(TreeTapperBlock.OPEN), "Turning tap opens both parts");
            for (int event = 0; event < 100; event++) {
                var tag = entity.getUpdateTag(level.registryAccess());
                tag.putInt("RemainingTicks", 1);
                entity.loadWithComponents(tag, level.registryAccess());
                dev.poptartking.poptartcore.decoration.TreeTapperBlockEntity.tick(
                        level, pos, level.getBlockState(pos), entity);
                helper.assertTrue(entity.storedFluid().getAmount() == (event + 1) * 10, "Exactly 10 mB per event");
            }
            helper.assertTrue(entity.storedFluid().is(expected), "Correct collected fluid");
            dev.poptartking.poptartcore.decoration.TreeTapperBlockEntity.tick(
                    level, pos, level.getBlockState(pos), entity);
            helper.assertTrue(entity.storedFluid().getAmount() == 1000, "Tank never overfills");
            var restored =
                    new dev.poptartking.poptartcore.decoration.TreeTapperBlockEntity(pos, level.getBlockState(pos));
            restored.loadWithComponents(entity.getUpdateTag(level.registryAccess()), level.registryAccess());
            helper.assertTrue(
                    restored.storedFluid().getAmount() == 1000
                            && restored.storedFluid().is(expected),
                    "Fluid survives save/load");
            helper.assertTrue(
                    entity.drainHandler()
                                    .fill(
                                            new net.neoforged.neoforge.fluids.FluidStack(expected, 10),
                                            net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE)
                            == 0,
                    "External filling rejected");
            entity.drainHandler()
                    .drain(1000, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
            level.getBlockState(pos.above()).useWithoutItem(level, player, hit);
            var timer = entity.remainingTicks();
            dev.poptartking.poptartcore.decoration.TreeTapperBlockEntity.tick(
                    level, pos, level.getBlockState(pos), entity);
            helper.assertTrue(
                    entity.storedFluid().isEmpty() && entity.remainingTicks() == timer, "Closed tap pauses collection");
            level.setBlock(root.above(7), Blocks.AIR.defaultBlockState(), 3);
            helper.assertTrue(
                    dev.poptartking.poptartcore.decoration.TapperTreeRules.fluidAt(
                                    level, pos.north().above())
                            == net.minecraft.world.level.material.Fluids.EMPTY,
                    "Bare log column cannot produce fluid");
        }
        var random = net.minecraft.util.RandomSource.create(3);
        int total = 0;
        var intervals = new java.util.HashSet<Integer>();
        for (int i = 0; i < 10000; i++) {
            int n = dev.poptartking.poptartcore.decoration.TreeTapperBlockEntity.nextInterval(random);
            helper.assertTrue(n >= 180 && n <= 300, "Interval bounds");
            total += n;
            intervals.add(n);
        }
        helper.assertTrue(
                intervals.size() > 100 && Math.abs(total / 10000.0 - 240) < 2, "Random timing averages 12 seconds");
        helper.succeed();
    }

    @GameTest(template = "tree_tapper_test", timeoutTicks = 40)
    public static void dripMovesDownTipThenNarrowsAndStretchesWithoutReleasePop(GameTestHelper helper) {
        var start = dev.poptartking.poptartcore.decoration.TapperDripShape.forming(0);
        var gathered = dev.poptartking.poptartcore.decoration.TapperDripShape.forming(
                dev.poptartking.poptartcore.decoration.TapperDripShape.FORM_TICKS * .65F);
        var release = dev.poptartking.poptartcore.decoration.TapperDripShape.forming(
                dev.poptartking.poptartcore.decoration.TapperDripShape.FORM_TICKS);
        var falling = dev.poptartking.poptartcore.decoration.TapperDripShape.falling(0);
        helper.assertTrue(
                start.topOffset() - start.height() > 0, "Starts within the tip instead of floating beneath it");
        helper.assertTrue(gathered.topOffset() == 0 && gathered.height() > 0, "Gathers directly against the underside");
        helper.assertTrue(
                release.width() < gathered.width() && release.height() > gathered.height(),
                "Bead narrows and stretches before release");
        helper.assertTrue(
                release.equals(falling), "Release frame preserves width, height and position without popping");
        int visibleTicks = 0;
        for (int tick = 0; tick < dev.poptartking.poptartcore.decoration.TapperDripShape.FORM_TICKS; tick++) {
            var shape = dev.poptartking.poptartcore.decoration.TapperDripShape.forming(tick);
            if (shape.topOffset() - shape.height() < 0) visibleTicks++;
        }
        helper.assertTrue(visibleTicks >= 110 && visibleTicks <= 130, "Visible forming phase lasts about 6 seconds");
        helper.assertTrue(
                dev.poptartking.poptartcore.decoration.TapperDripShape.MAX_AGE
                        > dev.poptartking.poptartcore.decoration.TapperDripShape.FORM_TICKS + 20,
                "Particle lifetime covers the full formation and fall");
        var previous = start;
        for (int i = 1; i <= dev.poptartking.poptartcore.decoration.TapperDripShape.FORM_TICKS * 10; i++) {
            var shape = dev.poptartking.poptartcore.decoration.TapperDripShape.forming(i / 10F);
            helper.assertTrue(
                    shape.width() <= .125F && shape.width() > 0 && shape.height() > 0, "Remains within nozzle width");
            helper.assertTrue(
                    Math.abs(shape.topOffset() - previous.topOffset()) < .001F
                            && Math.abs(shape.width() - previous.width()) < .002F
                            && Math.abs(shape.height() - previous.height()) < .002F,
                    "Continuous shape between frames");
            previous = shape;
        }
        helper.succeed();
    }

    @GameTest(template = "tree_tapper_test", timeoutTicks = 40)
    public static void resinHarvestEmptiesFullTankInFiveClicksAndKeepsRemainder(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int amount : new int[] {90, 100, 199, 200, 250, 450, 1000}) {
            var tapper = filledTapper(helper, PoptartCoreFluids.RESIN.source().get(), amount);
            var pos = tapper.getBlockPos();
            var player = helper.makeMockPlayer(GameType.SURVIVAL);
            var state = level.getBlockState(pos);
            for (int click = 1; click <= amount / 200; click++) {
                var result = state.useItemOn(
                        player.getMainHandItem(), level, player, InteractionHand.MAIN_HAND, bucketHit(pos));
                helper.assertTrue(
                        result == ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION,
                        "Empty hand and held resin reach the bucket interaction");
                helper.assertTrue(
                        state.useWithoutItem(level, player, bucketHit(pos)).consumesAction(),
                        "Can keep harvesting after the first resin piece lands in the hand");
                helper.assertTrue(
                        player.getInventory().countItem(NMLItems.RESIN.get()) == click * 2,
                        "Each click gives two resin pieces, with ten pieces collected in five clicks");
                helper.assertTrue(
                        player.getMainHandItem().is(NMLItems.RESIN.get()), "Harvested resin can stay in the hand");
                helper.assertTrue(
                        tapper.storedFluid().getAmount() == amount - click * 200,
                        "Each click drains exactly 200 mB and leaves the rest");
            }
            helper.assertTrue(tapper.storedFluid().getAmount() == amount % 200, "Partial resin stays in the tank");
            helper.assertTrue(
                    !level.getBlockState(pos).getValue(TreeTapperBlock.OPEN), "Harvest does not turn the tap");
            int before = player.getInventory().countItem(NMLItems.RESIN.get());
            state.useWithoutItem(level, player, bucketHit(pos));
            helper.assertTrue(
                    player.getInventory().countItem(NMLItems.RESIN.get()) == before
                            && tapper.storedFluid().getAmount() == amount % 200,
                    "Repeated clicks cannot harvest the same resin twice");
            var restored = new TreeTapperBlockEntity(pos, state);
            restored.loadWithComponents(tapper.getUpdateTag(level.registryAccess()), level.registryAccess());
            helper.assertTrue(
                    restored.storedFluid().getAmount() == amount % 200, "Harvested balance survives save/load");
        }
        helper.succeed();
    }

    @GameTest(template = "tree_tapper_test", timeoutTicks = 40)
    public static void syrupBottlesUseEitherHandAndLeavePartialFluid(GameTestHelper helper) {
        var level = helper.getLevel();
        for (InteractionHand hand : InteractionHand.values()) {
            var tapper =
                    filledTapper(helper, PoptartCoreFluids.MAPLE_SYRUP.source().get(), 450);
            var pos = tapper.getBlockPos();
            var player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setItemInHand(hand, new ItemStack(Items.GLASS_BOTTLE, 3));
            for (int bottle = 1; bottle <= 2; bottle++) {
                var result = level.getBlockState(pos)
                        .useItemOn(player.getItemInHand(hand), level, player, hand, bucketHit(pos));
                helper.assertTrue(result.consumesAction(), "Syrup can be bottled using " + hand);
                helper.assertTrue(
                        tapper.storedFluid().getAmount() == 450 - bottle * 200,
                        "Each syrup bottle drains exactly 200 mB");
                helper.assertTrue(
                        player.getItemInHand(hand).is(Items.GLASS_BOTTLE)
                                && player.getItemInHand(hand).getCount() == 3 - bottle
                                && player.getInventory().countItem(NMLItems.MAPLE_SYRUP_BOTTLE.get()) == bottle,
                        "One glass bottle becomes one syrup bottle");
            }
            var result =
                    level.getBlockState(pos).useItemOn(player.getItemInHand(hand), level, player, hand, bucketHit(pos));
            helper.assertTrue(
                    !result.consumesAction() && tapper.storedFluid().getAmount() == 50,
                    "Cannot bottle a partial serving");
            helper.assertTrue(player.getItemInHand(hand).is(Items.GLASS_BOTTLE), "Insufficient syrup keeps the bottle");
            helper.assertTrue(
                    !level.getBlockState(pos.above()).getValue(TreeTapperBlock.OPEN), "Bottling leaves tap closed");
            var tag = tapper.getUpdateTag(level.registryAccess());
            tag.put(
                    "Fluid",
                    new FluidStack(PoptartCoreFluids.MAPLE_SYRUP.source().get(), 200).save(level.registryAccess()));
            tapper.loadWithComponents(tag, level.registryAccess());
            level.getBlockState(pos).useItemOn(player.getItemInHand(hand), level, player, hand, bucketHit(pos));
            helper.assertTrue(
                    player.getItemInHand(hand).is(NMLItems.MAPLE_SYRUP_BOTTLE.get()),
                    "Last glass bottle is replaced in the same hand");
            helper.assertTrue(tapper.storedFluid().isEmpty(), "Exact serving empties the tank");
        }
        helper.succeed();
    }

    @GameTest(template = "tree_tapper_test", timeoutTicks = 40)
    public static void harvestingRejectsWrongItemsAndKeepsTapControlSeparate(GameTestHelper helper) {
        var level = helper.getLevel();
        var tapper = filledTapper(helper, PoptartCoreFluids.RESIN.source().get(), 300);
        var pos = tapper.getBlockPos();
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        for (var item : new net.minecraft.world.item.Item[] {Items.GLASS_BOTTLE, Items.STICK}) {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item));
            var result = level.getBlockState(pos)
                    .useItemOn(player.getMainHandItem(), level, player, InteractionHand.MAIN_HAND, bucketHit(pos));
            helper.assertTrue(
                    result == ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION, "Wrong items pass through");
            level.getBlockState(pos).useWithoutItem(level, player, bucketHit(pos));
            helper.assertTrue(tapper.storedFluid().getAmount() == 300, "Held items cannot accidentally harvest resin");
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        var upper = pos.above();
        level.getBlockState(upper).useWithoutItem(level, player, bucketHit(upper));
        helper.assertTrue(
                level.getBlockState(pos).getValue(TreeTapperBlock.OPEN)
                        && tapper.storedFluid().getAmount() == 300,
                "Upper tap turns on without harvesting");
        level.getBlockState(pos).useWithoutItem(level, player, bucketHit(pos));
        helper.assertTrue(
                tapper.storedFluid().getAmount() == 100
                        && level.getBlockState(upper).getValue(TreeTapperBlock.OPEN),
                "Bucket harvest leaves the tap on");
        level.getBlockState(upper).useWithoutItem(level, player, bucketHit(upper));
        helper.assertTrue(
                !level.getBlockState(pos).getValue(TreeTapperBlock.OPEN), "Tap can still turn off after harvesting");
        helper.succeed();
    }

    @GameTest(template = "tree_tapper_test", timeoutTicks = 40)
    public static void creativeBottlingKeepsGlassAndDrainsOneServing(GameTestHelper helper) {
        var level = helper.getLevel();
        var tapper = filledTapper(helper, PoptartCoreFluids.MAPLE_SYRUP.source().get(), 400);
        var pos = tapper.getBlockPos();
        var player = helper.makeMockPlayer(GameType.CREATIVE);
        GameType.CREATIVE.updatePlayerAbilities(player.getAbilities());
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE, 3));
        for (int bottle = 0; bottle < 2; bottle++) {
            level.getBlockState(pos)
                    .useItemOn(player.getMainHandItem(), level, player, InteractionHand.MAIN_HAND, bucketHit(pos));
        }
        helper.assertTrue(player.getMainHandItem().getCount() == 3, "Creative keeps the glass bottles");
        helper.assertTrue(tapper.storedFluid().isEmpty(), "Creative still drains the collected fluid");
        helper.assertTrue(
                player.getInventory().countItem(NMLItems.MAPLE_SYRUP_BOTTLE.get()) == 1,
                "Creative uses vanilla filled-container duplicate prevention");
        helper.succeed();
    }

    @GameTest(template = "tree_tapper_test", timeoutTicks = 40)
    public static void fullInventoryDropsBottledSyrupWithoutLosingIt(GameTestHelper helper) {
        var level = helper.getLevel();
        var tapper = filledTapper(helper, PoptartCoreFluids.MAPLE_SYRUP.source().get(), 200);
        var pos = tapper.getBlockPos();
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        for (int slot = 0; slot < player.getInventory().items.size(); slot++) {
            player.getInventory().setItem(slot, new ItemStack(Items.STONE, 64));
        }
        player.moveTo(Vec3.atCenterOf(pos.south(2)));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE, 2));
        level.getBlockState(pos)
                .useItemOn(player.getMainHandItem(), level, player, InteractionHand.MAIN_HAND, bucketHit(pos));
        int dropped = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(5)).stream()
                .filter(item -> item.getItem().is(NMLItems.MAPLE_SYRUP_BOTTLE.get()))
                .mapToInt(item -> item.getItem().getCount())
                .sum();
        helper.assertTrue(
                dropped == 1 && tapper.storedFluid().isEmpty(), "Full inventory drops exactly the harvested bottle");
        helper.assertTrue(player.getMainHandItem().getCount() == 1, "Exactly one glass bottle is consumed");
        helper.succeed();
    }

    private static TreeTapperBlockEntity filledTapper(GameTestHelper helper, Fluid fluid, int amount) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(4, 2, 4));
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(pos.north(), Blocks.OAK_LOG.defaultBlockState(), 3);
        level.setBlock(pos.north().above(), Blocks.OAK_LOG.defaultBlockState(), 3);
        helper.assertTrue(place(helper, pos, Direction.SOUTH), "Valid tapper mount");
        var tapper = (TreeTapperBlockEntity) level.getBlockEntity(pos);
        var tag = tapper.getUpdateTag(level.registryAccess());
        tag.put("Fluid", new FluidStack(fluid, amount).save(level.registryAccess()));
        tapper.loadWithComponents(tag, level.registryAccess());
        return tapper;
    }

    private static BlockHitResult bucketHit(BlockPos pos) {
        return new BlockHitResult(Vec3.atCenterOf(pos), Direction.SOUTH, pos, false);
    }

    private static boolean place(GameTestHelper helper, BlockPos pos, Direction face) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var stack = new ItemStack(PoptartWorkstationBlocks.TREE_TAPPER.item().get());
        var hit = new BlockHitResult(
                Vec3.atCenterOf(pos.relative(face.getOpposite())), face, pos.relative(face.getOpposite()), false);
        var context = new BlockPlaceContext(helper.getLevel(), player, InteractionHand.MAIN_HAND, stack, hit);
        return ((BlockItem) stack.getItem()).place(context).consumesAction();
    }
}
