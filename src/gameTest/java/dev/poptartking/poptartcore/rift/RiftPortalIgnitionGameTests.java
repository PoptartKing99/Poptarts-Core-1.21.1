package dev.poptartking.poptartcore.rift;

import dev.poptartking.poptartcore.PoptartCore;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.structures.BuriedTreasurePieces;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RiftPortalIgnitionGameTests {
    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void unchangedFramesKeepTheOriginalAnimation(GameTestHelper helper) {
        eachAxis(helper, portal -> {
            portal.tick(28);
            helper.assertTrue(portal.count() == 0, "Gathering does not place portal blocks early");
            portal.tick(1);
            helper.assertTrue(portal.count() == 16, "Outer ring appears first");
            portal.tick(3);
            helper.assertTrue(portal.count() == 24, "Second ring keeps the three-tick interval");
            portal.tick(3);
            helper.assertTrue(portal.count() == 25, "Center completes the portal");
            portal.tick(10);
            helper.assertTrue(portal.count() == 25, "Completed portal remains intact");
            for (int column = 0; column < 5; column++) {
                for (int row = 0; row < 5; row++) {
                    helper.assertTrue(
                            portal.level()
                                            .getBlockState(portal.pos(column, row))
                                            .getValue(BlockStateProperties.HORIZONTAL_AXIS)
                                    == portal.axis(),
                            "Portal keeps the frame's orientation");
                }
            }
        });
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void blocksPlacedDuringGatheringAreNotOverwritten(GameTestHelper helper) {
        eachAxis(helper, portal -> {
            portal.tick(10);
            var placed = portal.pos(2, 2);
            portal.level().setBlockAndUpdate(placed, Blocks.DIAMOND_BLOCK.defaultBlockState());
            portal.tick(50);
            helper.assertTrue(portal.level().getBlockState(placed).is(Blocks.DIAMOND_BLOCK), "Player's block survives");
            helper.assertTrue(portal.count() == 0, "Obstructed opening cancels ignition");
        });
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void breakingTheFrameDuringGatheringCancelsIgnition(GameTestHelper helper) {
        eachAxis(helper, portal -> {
            portal.tick(10);
            var broken = portal.pos(2, -1);
            portal.level().removeBlock(broken, false);
            portal.tick(50);
            helper.assertTrue(portal.level().getBlockState(broken).isAir(), "Broken frame is not restored");
            helper.assertTrue(portal.count() == 0, "Broken frame never receives portal blocks");
        });
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void cancellationKeepsNewChestsAndClearsPartialPortals(GameTestHelper helper) {
        for (boolean replaceCreatedPortal : new boolean[] {false, true}) {
            eachAxis(helper, portal -> {
                portal.tick(29);
                helper.assertTrue(portal.count() == 16, "Animation has placed its outer ring");
                var placed = portal.pos(replaceCreatedPortal ? 0 : 2, 2);
                portal.level().setBlockAndUpdate(placed, Blocks.CHEST.defaultBlockState());
                var chest = (ChestBlockEntity) portal.level().getBlockEntity(placed);
                chest.setItem(0, new ItemStack(Items.DIAMOND, 7));
                portal.tick(50);
                helper.assertTrue(
                        portal.level().getBlockEntity(placed) == chest
                                && chest.getItem(0).getCount() == 7,
                        "Cancellation preserves the new chest and its contents");
                helper.assertTrue(portal.count() == 0, "Cancellation removes partial portal blocks");
            });
        }
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void breakingTheFrameAfterTheFirstRingClearsThePortal(GameTestHelper helper) {
        eachAxis(helper, portal -> {
            portal.tick(29);
            helper.assertTrue(portal.count() == 16, "Outer ring exists before interruption");
            portal.level().removeBlock(portal.pos(2, -1), false);
            portal.tick(50);
            helper.assertTrue(portal.count() == 0, "Animation does not rebuild a portal in the broken frame");
        });
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void cancelledIgnitionRequiresANewStartAfterRepair(GameTestHelper helper) {
        eachAxis(helper, portal -> {
            var obstruction = portal.pos(2, 2);
            portal.level().setBlockAndUpdate(obstruction, Blocks.STONE.defaultBlockState());
            portal.tick(1);
            portal.level().removeBlock(obstruction, false);
            portal.tick(50);
            helper.assertTrue(portal.count() == 0, "Repair does not silently resume cancelled ignition");
            helper.assertTrue(
                    RiftPortalIgnition.start(portal.level(), portal.origin()), "Repaired frame can be ignited again");
            portal.tick(40);
            helper.assertTrue(portal.count() == 25, "Fresh ignition completes normally");
        });
        helper.succeed();
    }

    private static void eachAxis(GameTestHelper helper, Consumer<Portal> test) {
        for (Axis axis : new Axis[] {Axis.X, Axis.Z}) {
            var level = helper.getLevel();
            var origin = helper.absolutePos(new BlockPos(1, 1, 1));
            for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-1, -1, -1), origin.offset(5, 5, 5))) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
            var portal = new Portal(level, origin, axis);
            for (int column = -1; column <= 5; column++) {
                for (int row = -1; row <= 5; row++) {
                    if (column == -1 || column == 5 || row == -1 || row == 5) {
                        level.setBlock(
                                portal.pos(column, row), Blocks.OBSIDIAN.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                }
            }

            var structure = level.registryAccess()
                    .registryOrThrow(Registries.STRUCTURE)
                    .get(ResourceLocation.withDefaultNamespace("ruined_portal"));
            var chunk = level.getChunkAt(origin);
            var starts = new HashMap<>(chunk.getAllStarts());
            var references = new HashMap<>(chunk.getAllReferences());
            references.computeIfPresent(structure, (key, value) -> new LongOpenHashSet(value));
            // Supply only the natural-structure metadata required by the real ignition entry point.
            var start = new StructureStart(
                    structure,
                    chunk.getPos(),
                    0,
                    new PiecesContainer(List.of(new BuriedTreasurePieces.BuriedTreasurePiece(origin))));
            chunk.setStartForStructure(structure, start);
            chunk.addReferenceForStructure(structure, chunk.getPos().toLong());
            try {
                helper.assertTrue(
                        RiftPortalIgnition.start(level, origin), "Natural ruined portal starts its animation");
                test.accept(portal);
            } finally {
                RiftPortalIgnition.clear(level);
                chunk.setAllStarts(starts);
                chunk.setAllReferences(references);
            }
        }
    }

    private record Portal(ServerLevel level, BlockPos origin, Axis axis) {
        BlockPos pos(int column, int row) {
            return origin.relative(axis == Axis.X ? Direction.EAST : Direction.SOUTH, column)
                    .above(row);
        }

        void tick(int count) {
            for (int tick = 0; tick < count; tick++) {
                RiftPortalIgnition.onLevelTick(new LevelTickEvent.Post(() -> true, level));
            }
        }

        int count() {
            int count = 0;
            for (int column = 0; column < 5; column++) {
                for (int row = 0; row < 5; row++) {
                    if (level.getBlockState(pos(column, row)).is(Blocks.NETHER_PORTAL)) count++;
                }
            }
            return count;
        }
    }
}
