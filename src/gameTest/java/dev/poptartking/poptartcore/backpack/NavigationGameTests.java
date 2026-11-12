package dev.poptartking.poptartcore.backpack;

import com.mojang.authlib.GameProfile;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.equipment.AccessoryInventory;
import dev.poptartking.poptartcore.equipment.UtilityWield;
import dev.poptartking.poptartcore.navigation.*;
import dev.poptartking.poptartcore.navigation.network.*;
import dev.poptartking.poptartcore.registry.PoptartEquipmentBlocks;
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
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class NavigationGameTests {
    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void removedIconsPreserveSavedLocationsWithDefaultSprites(GameTestHelper h) {
        var registry = new LodestoneRegistry();
        var icons = new String[] {"alchemist", "friend", "bed"};
        var legacyWaypoints = new java.util.ArrayList<Waypoint>();
        for (int index = 0; index < icons.length; index++) {
            var pos = GlobalPos.of(Level.OVERWORLD, new BlockPos(index, 70, 10));
            var mark = new LodestoneMark("Saved location " + index, icons[index]);
            registry.set(pos, mark);
            legacyWaypoints.add(Waypoint.lodestone(pos, mark));
        }
        var legacyCompass = new CompassWaypoints(
                legacyWaypoints, 1, java.util.List.of(legacyWaypoints.getFirst().pos()), true);
        var ops = h.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        var loadedRegistry = LodestoneRegistry.CODEC
                .parse(ops, LodestoneRegistry.CODEC.encodeStart(ops, registry).getOrThrow())
                .getOrThrow();
        var loadedCompass = CompassWaypoints.CODEC
                .parse(
                        ops,
                        CompassWaypoints.CODEC.encodeStart(ops, legacyCompass).getOrThrow())
                .getOrThrow();
        h.assertTrue(loadedCompass.equals(legacyCompass), "Saved locations, selection and hidden state stay intact");
        for (int index = 0; index < icons.length; index++) {
            var waypoint = loadedCompass.get(index);
            var mark = loadedRegistry.get(waypoint.pos());
            var expectedIcon = index < 2 ? WaypointIcons.DEFAULT : "bed";
            h.assertTrue(
                    mark.name().equals(waypoint.label().getString())
                            && mark.icon().equals(expectedIcon),
                    "Loaded lodestones retain their names and positions with supported icons");
            for (boolean selected : new boolean[] {false, true}) {
                h.assertTrue(
                        waypoint.getSprite(selected).equals(WaypointIcons.getSprite(expectedIcon, selected)),
                        "Legacy compasses resolve both sprite states without missing textures");
            }
        }
        h.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void unchangedNavigationStillDetectsRespawnNamesAndDestroyedStones(GameTestHelper h) {
        var player = player(h);
        var compass = new ItemStack(Items.COMPASS);
        var key = stone(h, new BlockPos(3, 2, 1), "First name");
        Waypoints.refresh(compass, player);
        Waypoints.bindLodestone(compass, player, key, new LodestoneMark("First name", "default"));
        var original = Waypoints.get(compass);
        for (int tick = 0; tick < 30; tick++)
            h.assertTrue(
                    !Waypoints.refresh(compass, player) && Waypoints.get(compass) == original,
                    "Unchanged polling leaves the existing waypoint component intact");
        var respawn = h.absolutePos(new BlockPos(4, 2, 1));
        player.setRespawnPosition(h.getLevel().dimension(), respawn, 0, true, false);
        h.assertTrue(
                Waypoints.refresh(compass, player) && Waypoints.get(compass).indexOf(WaypointType.RESPAWN) == 1,
                "A changed respawn target appears on the very next refresh");
        LodestoneRegistry.get(h.getLevel().getServer()).set(key, new LodestoneMark("Renamed", "bed"));
        h.assertTrue(
                Waypoints.refresh(compass, player)
                        && Waypoints.get(compass)
                                .getSelected()
                                .name()
                                .orElseThrow()
                                .getString()
                                .equals("Renamed")
                        && Waypoints.get(compass)
                                .getSelected()
                                .icon()
                                .orElseThrow()
                                .equals("bed"),
                "Renaming a selected marker updates its name and icon without losing selection");
        h.setBlock(new BlockPos(3, 2, 1), Blocks.AIR);
        h.assertTrue(
                Waypoints.refresh(compass, player)
                        && Waypoints.get(compass).waypoints().stream()
                                .noneMatch(waypoint -> waypoint.type() == WaypointType.LODESTONE),
                "Destroying a named stone is detected without waiting for any cache expiry");
        h.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void navigationRefreshStillNormalizesLegacySelectionsAndStyledNames(GameTestHelper h) {
        var player = player(h);
        var compass = new ItemStack(Items.COMPASS);
        var key = stone(h, new BlockPos(3, 2, 1), "Home");
        Waypoints.refresh(compass, player);
        var origin = Waypoints.get(compass).waypoints().getFirst();
        var styled = new Waypoint(
                key,
                WaypointType.LODESTONE,
                java.util.Optional.of(net.minecraft.network.chat.Component.literal("Home")
                        .withStyle(net.minecraft.ChatFormatting.RED)),
                java.util.Optional.of("default"));
        NavigationComponents.write(
                compass,
                new CompassWaypoints(java.util.List.of(origin, styled, styled), -1, java.util.List.of(key), true));
        h.assertTrue(Waypoints.refresh(compass, player), "Legacy selection and styled metadata are rebuilt");
        var normalized = Waypoints.get(compass);
        h.assertTrue(
                normalized.selected() == 1
                        && normalized.navigationHidden()
                        && normalized.hidden().contains(key)
                        && normalized
                                .waypoints()
                                .get(1)
                                .name()
                                .orElseThrow()
                                .getStyle()
                                .isEmpty(),
                "Duplicate targets select their first occurrence, with visibility preferences preserved");
        h.assertTrue(!Waypoints.refresh(compass, player), "The normalized component then remains unchanged");
        h.succeed();
    }

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
            var bag = new ItemStack((ruck ? PoptartEquipmentBlocks.RUCKSACK : PoptartEquipmentBlocks.BACKPACK)
                    .get()
                    .asItem());
            for (int i = 1; i < BackpackAttachments.slots(ruck); i++) BackpackAttachments.set(bag, i, compass.get());
            accessories.setStackInSlot(AccessoryInventory.BACK_SLOT, bag);
            Lodestones.onUpdate(new UpdateLodestonePayload(next.pos(), "New location", "default"), player);
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
            Lodestones.onUpdate(new UpdateLodestonePayload(next.pos(), "Renamed", "default"), player);
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
        var bag = new ItemStack(PoptartEquipmentBlocks.RUCKSACK.get().asItem());
        BackpackAttachments.set(bag, 1, compass);
        AccessoryInventory.get(player).setStackInSlot(AccessoryInventory.BACK_SLOT, bag);
        h.assertTrue(UtilityWield.startAttachment(player), "Compass is in use");
        Lodestones.onUpdate(new UpdateLodestonePayload(next.pos(), "Auto saved", "default"), player);
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
        Lodestones.onUpdate(new UpdateLodestonePayload(key.pos(), "  Home\n\u00a7Base ", "../invalid"), player);
        var registry = LodestoneRegistry.get(h.getLevel().getServer());
        var mark = registry.get(key);
        h.assertTrue(
                mark.name().equals("HomeBase") && mark.icon().equals("default"),
                "Names and icons sanitized at network boundary");
        player.setPos(player.position().add(50, 0, 0));
        Lodestones.onUpdate(new UpdateLodestonePayload(key.pos(), "Forbidden", "bed"), player);
        h.assertTrue(registry.get(key).equals(mark), "Far away edits rejected");
        var ops = h.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        var saved = LodestoneRegistry.CODEC.encodeStart(ops, registry).getOrThrow();
        h.assertTrue(
                LodestoneRegistry.CODEC.parse(ops, saved).getOrThrow().get(key).equals(mark),
                "Lodestone name and icon survive save codec");
        h.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void unloadedLodestoneUpdatesDoNotLoadChunks(GameTestHelper h) {
        var player = player(h);
        var level = h.getLevel();
        var target = h.absolutePos(new BlockPos(4096, 2, 4096));
        int chunkX = target.getX() >> 4;
        int chunkZ = target.getZ() >> 4;
        h.assertTrue(level.getChunkSource().getChunkNow(chunkX, chunkZ) == null, "Target chunk starts unloaded");

        Lodestones.onUpdate(new UpdateLodestonePayload(target, "Distant request", "default"), player);

        h.assertTrue(
                level.getChunkSource().getChunkNow(chunkX, chunkZ) == null,
                "Rejected lodestone update must not load or generate its target chunk");
        h.assertTrue(
                !LodestoneRegistry.get(level.getServer())
                        .get(GlobalPos.of(level.dimension(), target))
                        .isNamed(),
                "Rejected update does not create a saved marker");
        h.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void loadedLodestoneUpdatesRespectSpectatorAndReachLimits(GameTestHelper h) {
        var player = player(h);
        var key = stone(h, new BlockPos(2, 2, 1), "Before");
        var registry = LodestoneRegistry.get(h.getLevel().getServer());
        player.setPos(Vec3.atCenterOf(key.pos()));
        player.setGameMode(GameType.SPECTATOR);
        Lodestones.onUpdate(new UpdateLodestonePayload(key.pos(), "Forbidden", "bed"), player);
        h.assertTrue(registry.get(key).name().equals("Before"), "Spectators cannot edit lodestones");

        player.setGameMode(GameType.SURVIVAL);
        player.setPos(Vec3.atCenterOf(key.pos()).add(8, 0, 0));
        Lodestones.onUpdate(new UpdateLodestonePayload(key.pos(), "At reach", "bed"), player);
        h.assertTrue(registry.get(key).name().equals("At reach"), "Existing eight-block editing reach is inclusive");
        player.setPos(player.position().add(.01, 0, 0));
        Lodestones.onUpdate(new UpdateLodestonePayload(key.pos(), "Too far", "default"), player);
        h.assertTrue(registry.get(key).name().equals("At reach"), "Edits outside reach are rejected");
        h.setBlock(new BlockPos(2, 2, 1), Blocks.STONE);
        player.setPos(Vec3.atCenterOf(key.pos()));
        Lodestones.onUpdate(new UpdateLodestonePayload(key.pos(), "Not a lodestone", "default"), player);
        h.assertTrue(!registry.get(key).isNamed(), "Non-lodestone blocks cannot be named through this packet");
        h.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void movingLodestoneUpdatesUseWorldSpaceReach(GameTestHelper h) {
        var player = player(h);
        var key = stone(h, new BlockPos(2, 2, 1), "Before");
        player.setPos(Vec3.atCenterOf(key.pos()).add(128, 0, 0));
        var previous = WaypointAnchors.getTracker();
        WaypointAnchors.setTracker(new WaypointAnchors.Tracker() {
            @Override
            public boolean isMovable(Level level, BlockPos anchor) {
                return anchor.equals(key.pos());
            }

            @Override
            public Vec3 locate(Level level, BlockPos anchor, float partialTick) {
                return player.position().add(1, 0, 0);
            }
        });
        try {
            Lodestones.onUpdate(new UpdateLodestonePayload(key.pos(), "On deck", "default"), player);
            h.assertTrue(
                    LodestoneRegistry.get(h.getLevel().getServer())
                            .get(key)
                            .name()
                            .equals("On deck"),
                    "Loaded moving lodestone uses its nearby world position, not distant storage coordinates");
        } finally {
            WaypointAnchors.setTracker(previous);
        }
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
        Waypoints.onSelect(new SelectWaypointPayload(1, CompassSource.HELD), player);
        h.assertTrue(Waypoints.get(stack).getSelectedIndex() == 1, "Server selects requested target");
        Waypoints.onHide(new HideWaypointPayload(1, 0, CompassSource.HELD), player);
        h.assertTrue(
                Waypoints.get(stack).isHidden(1) && Waypoints.get(stack).getSelectedIndex() == 0,
                "Hiding selected target falls back to origin");
        Waypoints.onSelect(new SelectWaypointPayload(1, CompassSource.HELD), player);
        h.assertTrue(Waypoints.get(stack).getSelectedIndex() == 0, "Hidden target cannot be selected");
        Waypoints.onHide(new HideWaypointPayload(1, 0, CompassSource.HELD), player);
        Waypoints.onToggleNavigation(new ToggleNavigationPayload(CompassSource.HELD), player);
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
            var bag = new ItemStack((ruck ? PoptartEquipmentBlocks.RUCKSACK : PoptartEquipmentBlocks.BACKPACK)
                    .get()
                    .asItem());
            var compass = new ItemStack(Items.COMPASS);
            Waypoints.refresh(compass, player);
            Waypoints.bindLodestone(compass, player, key, new LodestoneMark("Mount", "default"));
            BackpackAttachments.set(bag, 1, compass);
            AccessoryInventory.get(player).setStackInSlot(AccessoryInventory.BACK_SLOT, bag);
            h.assertTrue(UtilityWield.startAttachment(player), "Mounted compass enters virtual hand");
            Items.COMPASS.use(h.getLevel(), player, InteractionHand.MAIN_HAND);
            Waypoints.onSelect(new SelectWaypointPayload(0, CompassSource.ATTACHED), player);
            Waypoints.onHide(new HideWaypointPayload(1, 0, CompassSource.ATTACHED), player);
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
