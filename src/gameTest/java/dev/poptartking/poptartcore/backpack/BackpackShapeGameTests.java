package dev.poptartking.poptartcore.backpack;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartEquipmentBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BackpackShapeGameTests {
    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void outlinesAndCollisionMatchPouchesInEveryDirection(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(4, 2, 4));
        for (var block :
                java.util.List.of(PoptartEquipmentBlocks.BACKPACK.get(), PoptartEquipmentBlocks.RUCKSACK.get())) {
            for (var facing : Direction.Plane.HORIZONTAL) {
                var state = block.defaultBlockState().setValue(BackpackBlock.FACING, facing);
                var outline = state.getShape(level, pos);
                var collision = state.getCollisionShape(level, pos, CollisionContext.empty());
                helper.assertTrue(
                        !Shapes.joinIsNotEmpty(outline, collision, BooleanOp.NOT_SAME),
                        "Outline and collision agree for " + block + " " + facing);
                helper.assertTrue(
                        state.getRenderShape() == RenderShape.INVISIBLE, "Only custom renderer draws the bag");
                if (block instanceof RucksackBlock) {
                    var upper = state.setValue(RucksackBlock.HALF, DoubleBlockHalf.UPPER)
                            .getShape(level, pos.above())
                            .move(0, 1, 0);
                    helper.assertTrue(
                            !Shapes.joinIsNotEmpty(outline, upper, BooleanOp.NOT_SAME),
                            "Both rucksack halves select the complete same model");
                }
                // Sample the outer lip of the right lower pouch, then rotate with the model.
                double x = block instanceof RucksackBlock ? 16.5 / 16 : 14.5 / 16;
                double z = 9.0 / 16;
                for (int turn = 0; turn < facing.get2DDataValue(); turn++) {
                    double nextX = 1 - z;
                    z = x;
                    x = nextX;
                }
                double top = block instanceof RucksackBlock ? 10.0 / 16 : 8.0 / 16;
                var inside = Shapes.box(x - .001, top - .01, z - .001, x + .001, top, z + .001);
                var above = inside.move(0, .02, 0);
                helper.assertTrue(
                        Shapes.joinIsNotEmpty(collision, inside, BooleanOp.AND), "Pouch lip supports the player");
                helper.assertTrue(
                        !Shapes.joinIsNotEmpty(collision, above, BooleanOp.AND), "No rectangular wall above pouch lip");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void playerLandsOnRucksackSidePouch(GameTestHelper helper) {
        var pos = helper.absolutePos(new BlockPos(4, 2, 4));
        var state = PoptartEquipmentBlocks.RUCKSACK
                .get()
                .defaultBlockState()
                .setValue(BackpackBlock.FACING, Direction.SOUTH);
        helper.getLevel().setBlockAndUpdate(pos, state);
        helper.getLevel().setBlockAndUpdate(pos.above(), state.setValue(RucksackBlock.HALF, DoubleBlockHalf.UPPER));
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(pos.getX() + 21.0 / 16, pos.getY() + 1.5, pos.getZ() + 9.0 / 16);
        player.move(MoverType.SELF, new Vec3(0, -2, 0));
        helper.assertTrue(
                Math.abs(player.getY() - (pos.getY() + 10.0 / 16)) < .0001,
                "Player lands on the side pouch rather than a full rectangular block or the floor");
        helper.succeed();
    }
}
