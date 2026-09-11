package dev.poptartking.poptartcore.millstone;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public final class MillstoneGameTests {
    @SubscribeEvent
    public static void template(ServerAboutToStartEvent event) throws Exception {
        event.getServer()
                .getStructureManager()
                .getOrCreate(PoptartCore.location("millstone_test"))
                .load(
                        event.getServer()
                                .registryAccess()
                                .lookupOrThrow(net.minecraft.core.registries.Registries.BLOCK),
                        TagParser.parseTag("{size:[9,5,9],palette:[{Name:\"minecraft:air\"}],blocks:[],entities:[]}"));
    }

    @GameTest(template = "millstone_test", timeoutTicks = 100)
    public static void processingAutomationAndTeardown(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(3, 1, 3));
        var level = helper.getLevel();
        level.setBlockAndUpdate(pos, PoptartCoreBlocks.MILLSTONE.get().defaultBlockState());
        helper.runAfterDelay(4, () -> {
            for (BlockPos offset : MillstoneStructure.ALL_OFFSETS) {
                helper.assertTrue(
                        !level.getBlockState(pos.offset(offset)).isAir(), "Missing structural piece " + offset);
            }
            var mill = (MillstoneBlockEntity) level.getBlockEntity(pos);
            var rotor = (MillstoneRotorBlockEntity) level.getBlockEntity(pos.above());
            var handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos.east(), Direction.EAST);
            helper.assertTrue(handler != null, "Lower outer part must expose inventory");
            helper.assertTrue(
                    level.getCapability(
                                    Capabilities.ItemHandler.BLOCK, pos.east().above(), Direction.EAST)
                            == null,
                    "Upper part must not expose inventory");
            ItemStack wheat = new ItemStack(Items.WHEAT, 64);
            helper.assertTrue(
                    handler.insertItem(9, wheat, true).isEmpty() && mill.totalCount(true) == 0,
                    "Simulated insertion changed inventory");
            helper.assertTrue(
                    handler.insertItem(9, wheat, false).isEmpty() && mill.totalCount(true) == 64,
                    "Wheat insertion failed");
            helper.assertTrue(
                    !handler.insertItem(9, new ItemStack(Items.WHEAT), false).isEmpty(), "Input capacity exceeded");
            helper.assertTrue(
                    !handler.insertItem(9, new ItemStack(Items.POPPY), false).isEmpty(), "Poppy accepted");
            helper.assertTrue(!handler.insertItem(0, wheat, false).isEmpty(), "Output accepted insertion");
            helper.assertTrue(handler.extractItem(9, 1, false).isEmpty(), "Unprocessed input extracted");
            rotor.setSpeed(128);
            for (int i = 0; i < 100; i++) MillstoneBlockEntity.serverTick(level, pos, mill.getBlockState(), mill);
            helper.assertTrue(mill.totalCount(false) == 0, "Overspeed produced output");
            rotor.setSpeed(64);
            for (int i = 0; i < 79; i++) MillstoneBlockEntity.serverTick(level, pos, mill.getBlockState(), mill);
            helper.assertTrue(mill.totalCount(false) == 0, "Recipe finished early");
            MillstoneBlockEntity.serverTick(level, pos, mill.getBlockState(), mill);
            helper.assertTrue(mill.totalCount(false) == 1 && mill.totalCount(true) == 63, "Incorrect processing yield");
            var flour =
                    BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse("create:wheat_flour"));
            helper.assertTrue(
                    handler.extractItem(0, 1, true).is(flour) && mill.totalCount(false) == 1,
                    "Wrong flour or simulation removed output");
            var saved = mill.saveWithoutMetadata(level.registryAccess());
            var restored = new MillstoneBlockEntity(pos, mill.getBlockState());
            restored.loadWithComponents(saved, level.registryAccess());
            helper.assertTrue(
                    restored.totalCount(true) == 63 && restored.totalCount(false) == 1, "Inventory persistence failed");
            helper.assertTrue(
                    handler.extractItem(0, 1, false).is(flour) && mill.totalCount(false) == 0,
                    "Output extraction failed");
            level.destroyBlock(pos.east(), true);
            helper.assertTrue(level.getBlockState(pos).isAir(), "Outer break left controller behind");
            for (BlockPos offset : MillstoneStructure.ALL_OFFSETS) {
                helper.assertTrue(level.getBlockState(pos.offset(offset)).isAir(), "Outer break left structure behind");
            }
            helper.assertTrue(handler.extractItem(0, 64, false).isEmpty(), "Old handler extracted removed inventory");
            helper.assertTrue(
                    !handler.insertItem(9, wheat, false).isEmpty(), "Old handler accepted input after removal");
            var drops = level.getEntitiesOfClass(
                    net.minecraft.world.entity.item.ItemEntity.class,
                    new net.minecraft.world.phys.AABB(pos).inflate(2));
            int blockDrops = drops.stream()
                    .filter(drop ->
                            drop.getItem().is(PoptartCoreBlocks.MILLSTONE.get().asItem()))
                    .mapToInt(drop -> drop.getItem().getCount())
                    .sum();
            int wheatDrops = drops.stream()
                    .filter(drop -> drop.getItem().is(Items.WHEAT))
                    .mapToInt(drop -> drop.getItem().getCount())
                    .sum();
            helper.assertTrue(blockDrops == 1 && wheatDrops == 63, "Teardown duplicated or lost block/inventory drops");
            helper.succeed();
        });
    }

    @GameTest(template = "millstone_test", timeoutTicks = 180)
    public static void createMotorDrivesGrinding(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(3, 1, 3));
        var level = helper.getLevel();
        level.setBlockAndUpdate(pos, PoptartCoreBlocks.MILLSTONE.get().defaultBlockState());
        helper.runAfterDelay(4, () -> {
            var motorBlock = BuiltInRegistries.BLOCK.get(
                    net.minecraft.resources.ResourceLocation.parse("create:creative_motor"));
            level.setBlockAndUpdate(
                    pos.above(2),
                    motorBlock
                            .defaultBlockState()
                            .setValue(
                                    net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING,
                                    Direction.DOWN));
            var motor = (com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity)
                    level.getBlockEntity(pos.above(2));
            motor.generatedSpeed.setValue(64);
            var mill = (MillstoneBlockEntity) level.getBlockEntity(pos);
            mill.insertInput(new ItemStack(Items.WHEAT), false);
        });
        helper.runAfterDelay(130, () -> {
            var mill = (MillstoneBlockEntity) level.getBlockEntity(pos);
            var rotor = (MillstoneRotorBlockEntity) level.getBlockEntity(pos.above());
            helper.assertTrue(Math.abs(rotor.getSpeed()) == 64, "Create network did not drive rotor at 64 RPM");
            helper.assertTrue(
                    mill.totalCount(false) == 1 && mill.totalCount(true) == 0,
                    "Powered millstone failed to grind wheat");
            level.destroyBlock(pos.above(), true);
            helper.assertTrue(level.getBlockState(pos).isAir(), "Rotor break failed to remove controller");
            helper.succeed();
        });
    }
}
