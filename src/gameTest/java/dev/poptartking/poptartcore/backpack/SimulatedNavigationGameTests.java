package dev.poptartking.poptartcore.backpack;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.navigation.CompassWaypoints;
import dev.poptartking.poptartcore.navigation.LodestoneMark;
import dev.poptartking.poptartcore.navigation.LodestoneRegistry;
import dev.poptartking.poptartcore.navigation.NavigationComponents;
import dev.poptartking.poptartcore.navigation.Waypoint;
import dev.poptartking.poptartcore.navigation.WaypointType;
import dev.simulated_team.simulated.content.blocks.nav_table.NavTableBlockEntity;
import dev.simulated_team.simulated.content.navigation_targets.CompassNavigationTarget;
import dev.simulated_team.simulated.index.SimBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SimulatedNavigationGameTests {
    private static NavTableBlockEntity table(GameTestHelper h) {
        var pos = new BlockPos(1, 2, 1);
        h.setBlock(pos, SimBlocks.NAVIGATION_TABLE.get());
        return (NavTableBlockEntity) h.getBlockEntity(pos);
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void tableUsesSelectedWaypointAndUpdatesWhenSelectionChanges(GameTestHelper h) {
        var table = table(h);
        var a = GlobalPos.of(h.getLevel().dimension(), h.absolutePos(new BlockPos(5, 2, 4)));
        var b = GlobalPos.of(h.getLevel().dimension(), h.absolutePos(new BlockPos(3, 2, 5)));
        var compass = new ItemStack(Items.COMPASS);
        NavigationComponents.write(
                compass,
                new CompassWaypoints(
                        List.of(new Waypoint(a, WaypointType.ORIGIN), new Waypoint(b, WaypointType.RESPAWN)),
                        1,
                        List.of(),
                        false));
        table.setHeldItem(compass);
        table.tick();
        h.assertTrue(
                Vec3.atCenterOf(b.pos()).equals(table.currentTarget),
                "Actual navigation table follows selected compass destination");
        NavigationComponents.write(
                table.getHeldItem(),
                new CompassWaypoints(
                        List.of(new Waypoint(a, WaypointType.ORIGIN), new Waypoint(b, WaypointType.RESPAWN)),
                        0,
                        List.of(),
                        false));
        table.tick();
        h.assertTrue(
                Vec3.atCenterOf(a.pos()).equals(table.currentTarget), "Table updates after compass selection changes");
        h.assertTrue(table.getHeldItem().getCount() == 1, "Table compass remains intact");
        h.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void tablePreservesFallbackRejectsOtherDimensionsAndResolvesRelocations(GameTestHelper h) {
        var table = table(h);
        var handler = new CompassNavigationTarget();
        h.assertTrue(
                Vec3.atCenterOf(h.getLevel().getSharedSpawnPos())
                        .equals(handler.getTarget(table, new ItemStack(Items.COMPASS))),
                "Uninitialized compass keeps Simulated spawn-target behavior");
        var compass = new ItemStack(Items.COMPASS);
        var elsewhere = GlobalPos.of(Level.NETHER, BlockPos.ZERO);
        NavigationComponents.write(
                compass,
                new CompassWaypoints(List.of(new Waypoint(elsewhere, WaypointType.RESPAWN)), 0, List.of(), false));
        h.assertTrue(
                handler.getTarget(table, compass) == null,
                "Cross-dimension target does not silently navigate to spawn");
        var from = GlobalPos.of(h.getLevel().dimension(), h.absolutePos(new BlockPos(2, 2, 2)));
        var to = GlobalPos.of(h.getLevel().dimension(), h.absolutePos(new BlockPos(4, 2, 4)));
        var registry = LodestoneRegistry.get(h.getLevel().getServer());
        registry.set(from, new LodestoneMark("Moving destination", "default"));
        registry.relocate(from, to);
        NavigationComponents.write(
                compass,
                new CompassWaypoints(List.of(new Waypoint(from, WaypointType.LODESTONE)), 0, List.of(), false));
        h.assertTrue(
                Vec3.atCenterOf(to.pos()).equals(handler.getTarget(table, compass)),
                "Table shares relocated lodestone lookup with compass HUD");
        h.succeed();
    }
}
