package dev.poptartking.poptartcore.backpack;

import com.mojang.authlib.GameProfile;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.equipment.AccessoryInventory;
import dev.poptartking.poptartcore.equipment.UtilityWield;
import dev.poptartking.poptartcore.navigation.*;
import dev.poptartking.poptartcore.navigation.network.*;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class NavigationGameTests {
    private static ServerPlayer player(GameTestHelper h) {
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "navigation-test"), false);
        var player = new ServerPlayer(
                h.getLevel().getServer(), h.getLevel(), cookie.gameProfile(), cookie.clientInformation());
        new ServerGamePacketListenerImpl(
                h.getLevel().getServer(), new Connection(PacketFlow.SERVERBOUND), player, cookie) {
            @Override
            public void send(Packet<?> packet) {}
        };
        var pos = h.absolutePos(new BlockPos(1, 2, 1));
        player.setPos(Vec3.atCenterOf(pos));
        return player;
    }

    private static GlobalPos stone(GameTestHelper h, BlockPos local, String name) {
        h.setBlock(local, Blocks.LODESTONE);
        var global = GlobalPos.of(h.getLevel().dimension(), h.absolutePos(local));
        LodestoneRegistry.get(h.getLevel().getServer()).set(global, new LodestoneMark(name, "default"));
        return global;
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void namingSavesToCarriedAndMountedCompassesWithoutSelecting(GameTestHelper h) {
        for (boolean ruck : new boolean[] {false, true}) {
            var player = player(h);
            var prior = stone(h, new BlockPos(1, 2, 1), "Previous");
            var next = stone(h, new BlockPos(2, 2, 1), "");
            java.util.function.Supplier<ItemStack> compass = () -> {
                var stack = new ItemStack(Items.COMPASS);
                Waypoints.refresh(stack, player);
                Waypoints.bindLodestone(stack, player, prior, new LodestoneMark("Previous", "default"));
                NavigationComponents.write(stack, Waypoints.get(stack).withNavigationHidden(true));
                return stack;
            };
            player.setItemInHand(InteractionHand.MAIN_HAND, compass.get());
            player.setItemInHand(InteractionHand.OFF_HAND, compass.get());
            player.getInventory().setItem(9, compass.get());
            var accessories = AccessoryInventory.get(player);
            accessories.setStackInSlot(AccessoryInventory.UTILITY_SLOT, compass.get());
            var bag = new ItemStack((ruck ? PoptartCoreBlocks.RUCKSACK : PoptartCoreBlocks.BACKPACK)
                    .get()
                    .asItem());
            for (int i = 1; i < BackpackAttachments.slots(ruck); i++) BackpackAttachments.set(bag, i, compass.get());
            accessories.setStackInSlot(AccessoryInventory.BACK_SLOT, bag);
            Lodestones.onUpdate(new UpdateLodestone(next.pos(), "New location", "default"), player);
            var actual = new java.util.ArrayList<ItemStack>();
            actual.add(player.getMainHandItem());
            actual.add(player.getOffhandItem());
            actual.add(player.getInventory().getItem(9));
            actual.add(accessories.getStackInSlot(AccessoryInventory.UTILITY_SLOT));
            for (int i = 1; i < BackpackAttachments.slots(ruck); i++) actual.add(BackpackAttachments.get(bag, i));
            for (var stack : actual) {
                var data = Waypoints.get(stack);
                h.assertTrue(
                        data.size() == 3 && data.get(2).pos().equals(next),
                        "Naming saves the marker to each carried or mounted compass");
                h.assertTrue(
                        data.getSelected().pos().equals(prior) && data.navigationHidden(),
                        "Existing selected red destination and display preferences preserved");
            }
            Lodestones.onUpdate(new UpdateLodestone(next.pos(), "Renamed", "default"), player);
            h.assertTrue(Waypoints.get(player.getMainHandItem()).size() == 3, "Renaming does not duplicate markers");
            h.assertTrue(
                    Waypoints.get(player.getMainHandItem())
                            .get(2)
                            .label()
                            .getString()
                            .equals("Renamed"),
                    "Rename refreshes saved label");
        }
        h.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void namingDuringMountedUseKeepsSelectionAndSession(GameTestHelper h) {
        var player = player(h);
        var next = stone(h, new BlockPos(2, 2, 1), "");
        var compass = new ItemStack(Items.COMPASS);
        Waypoints.refresh(compass, player);
        var bag = new ItemStack(PoptartCoreBlocks.RUCKSACK.get().asItem());
        BackpackAttachments.set(bag, 1, compass);
        AccessoryInventory.get(player).setStackInSlot(AccessoryInventory.BACK_SLOT, bag);
        h.assertTrue(UtilityWield.startAttachment(player), "Compass is in use");
        Lodestones.onUpdate(new UpdateLodestone(next.pos(), "Auto saved", "default"), player);
        UtilityWield.tick(player);
        var data = Waypoints.get(BackpackAttachments.get(bag, 1));
        h.assertTrue(
                UtilityWield.isAttachmentWielding(player) && data.size() == 2 && data.getSelectedIndex() == 0,
                "Active compass updated without stale data or selection change");
        UtilityWield.stop(player);
        h.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void lodestoneNamesValidateReachAndSave(GameTestHelper h) {
        var player = player(h);
        var key = stone(h, new BlockPos(1, 2, 1), "Before");
        Lodestones.onUpdate(new UpdateLodestone(key.pos(), "  Home\n\u00a7Base ", "../invalid"), player);
        var registry = LodestoneRegistry.get(h.getLevel().getServer());
        var mark = registry.get(key);
        h.assertTrue(
                mark.name().equals("HomeBase") && mark.icon().equals("default"),
                "Names and icons sanitized at network boundary");
        player.setPos(player.position().add(50, 0, 0));
        Lodestones.onUpdate(new UpdateLodestone(key.pos(), "Forbidden", "bed"), player);
        h.assertTrue(registry.get(key).equals(mark), "Far away edits rejected");
        var ops = h.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        var saved = LodestoneRegistry.CODEC.encodeStart(ops, registry).getOrThrow();
        h.assertTrue(
                LodestoneRegistry.CODEC.parse(ops, saved).getOrThrow().get(key).equals(mark),
                "Lodestone name and icon survive save codec");
        h.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void actualCompassBindingAndUseWork(GameTestHelper h) {
        var player = player(h);
        var local = new BlockPos(1, 2, 1);
        var key = stone(h, local, "Home");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.COMPASS));
        var hit = new BlockHitResult(Vec3.atCenterOf(key.pos()), Direction.UP, key.pos(), false);
        var result = Items.COMPASS.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
        var stack = player.getMainHandItem();
        var data = Waypoints.get(stack);
        h.assertTrue(
                result.consumesAction() && data.size() == 2,
                "Using vanilla compass binds without removing world origin");
        h.assertTrue(data.getSelected().pos().equals(key), "Bound lodestone is selected");
        Items.COMPASS.use(h.getLevel(), player, InteractionHand.MAIN_HAND);
        h.assertTrue(
                player.isUsingItem() && stack.getUseDuration(player) == 72000,
                "Vanilla compass starts held selector use");
        player.stopUsingItem();
        h.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void targetLimitSelectionVisibilityAndDestruction(GameTestHelper h) {
        var player = player(h);
        var stack = new ItemStack(Items.COMPASS);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        Waypoints.refresh(stack, player);
        GlobalPos last = null;
        for (int i = 0; i < 13; i++) {
            var key = stone(h, new BlockPos(i % 4, 2 + i / 4, 2), "Stone " + i);
            Waypoints.bindLodestone(stack, player, key, new LodestoneMark("Stone " + i, "default"));
            if (i == 11) last = key;
        }
        h.assertTrue(Waypoints.get(stack).size() == 13, "Exactly twelve lodestones plus origin");
        Waypoints.onSelect(new SelectWaypoint(1, CompassSource.HELD), player);
        h.assertTrue(Waypoints.get(stack).getSelectedIndex() == 1, "Server selects requested target");
        Waypoints.onHide(new HideWaypoint(1, 0, CompassSource.HELD), player);
        h.assertTrue(
                Waypoints.get(stack).isHidden(1) && Waypoints.get(stack).getSelectedIndex() == 0,
                "Hiding selected target falls back to origin");
        Waypoints.onSelect(new SelectWaypoint(1, CompassSource.HELD), player);
        h.assertTrue(Waypoints.get(stack).getSelectedIndex() == 0, "Hidden target cannot be selected");
        Waypoints.onHide(new HideWaypoint(1, 0, CompassSource.HELD), player);
        Waypoints.onToggleNavigation(new ToggleNavigation(CompassSource.HELD), player);
        h.assertTrue(
                !Waypoints.get(stack).isHidden(1) && Waypoints.get(stack).navigationHidden(),
                "Visibility settings independent");
        h.getLevel().setBlock(last.pos(), Blocks.AIR.defaultBlockState(), 3);
        Waypoints.refresh(stack, player);
        h.assertTrue(Waypoints.get(stack).size() == 12, "Destroyed lodestone removed on refresh");
        h.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void mountedCompassChangesCommitWithoutEndingUse(GameTestHelper h) {
        var player = player(h);
        var key = stone(h, new BlockPos(1, 2, 1), "Mount");
        for (boolean ruck : new boolean[] {false, true}) {
            var bag = new ItemStack((ruck ? PoptartCoreBlocks.RUCKSACK : PoptartCoreBlocks.BACKPACK)
                    .get()
                    .asItem());
            var compass = new ItemStack(Items.COMPASS);
            Waypoints.refresh(compass, player);
            Waypoints.bindLodestone(compass, player, key, new LodestoneMark("Mount", "default"));
            BackpackAttachments.set(bag, 1, compass);
            AccessoryInventory.get(player).setStackInSlot(AccessoryInventory.BACK_SLOT, bag);
            h.assertTrue(UtilityWield.startAttachment(player), "Mounted compass enters virtual hand");
            Items.COMPASS.use(h.getLevel(), player, InteractionHand.MAIN_HAND);
            Waypoints.onSelect(new SelectWaypoint(0, CompassSource.ATTACHED), player);
            Waypoints.onHide(new HideWaypoint(1, 0, CompassSource.ATTACHED), player);
            UtilityWield.tick(player);
            h.assertTrue(
                    UtilityWield.isAttachmentWielding(player) && player.isUsingItem(),
                    "Navigation edits keep attachment session active");
            var stored = Waypoints.get(BackpackAttachments.get(bag, 1));
            h.assertTrue(stored.getSelectedIndex() == 0 && stored.isHidden(1), "Changes committed to actual mount");
            UtilityWield.stop(player);
            var saved = bag.save(h.getLevel().registryAccess());
            var copy = ItemStack.parseOptional(h.getLevel().registryAccess(), (net.minecraft.nbt.CompoundTag) saved);
            h.assertTrue(
                    Waypoints.get(BackpackAttachments.get(copy, 1)).equals(stored),
                    "Nested compass target and visibility survive item save");
        }
        h.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void originRespawnAndRelocationsStayCurrent(GameTestHelper h) {
        var player = player(h);
        var compass = new ItemStack(Items.COMPASS);
        var bed = h.absolutePos(new BlockPos(2, 2, 1));
        player.setRespawnPosition(Level.OVERWORLD, bed, 0F, true, false);
        Waypoints.refresh(compass, player);
        h.assertTrue(
                Waypoints.get(compass).indexOf(WaypointType.ORIGIN) == 0
                        && Waypoints.get(compass).indexOf(WaypointType.RESPAWN) == 1,
                "Origin and respawn present");
        var from = stone(h, new BlockPos(1, 2, 1), "Moving");
        var to = GlobalPos.of(h.getLevel().dimension(), h.absolutePos(new BlockPos(2, 3, 1)));
        var registry = LodestoneRegistry.get(h.getLevel().getServer());
        h.assertTrue(
                registry.relocate(from, to)
                        && registry.resolve(from).equals(to)
                        && registry.get(to).name().equals("Moving"),
                "Moving lodestones retain names and forwarding");
        var ops = h.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        var loaded = LodestoneRegistry.CODEC
                .parse(ops, LodestoneRegistry.CODEC.encodeStart(ops, registry).getOrThrow())
                .getOrThrow();
        h.assertTrue(loaded.resolve(from).equals(to), "Relocation survives save");
        registry.claim(from);
        h.assertTrue(registry.resolve(from).equals(from), "New block at old address does not inherit old forwarding");
        h.assertTrue(WaypointAnchors.isTracking(), "Sable waypoint tracking initialized");
        h.succeed();
    }
}
