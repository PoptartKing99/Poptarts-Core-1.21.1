package dev.poptartking.poptartcore.navigation;

import com.mojang.authlib.GameProfile;
import dev.poptartking.poptartcore.PoptartCore;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CompassMarkGameTests {
    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void sneakUseForgetsOnlyTheClickedStoneInEitherHand(GameTestHelper h) {
        var player = player(h);
        var first = stone(h, new BlockPos(2, 2, 1), "First");
        var second = stone(h, new BlockPos(3, 2, 1), "Second");
        for (var hand : InteractionHand.values()) {
            var compass = marked(h, player, first, second);
            var data = Waypoints.get(compass);
            NavigationComponents.write(compass, new CompassWaypoints(data.waypoints(), 2, List.of(first), true));
            var other = compass.copy();
            player.getInventory().setItem(9, other);
            player.setItemInHand(hand, compass);
            player.setShiftKeyDown(true);
            use(player, hand, first);
            var updated = Waypoints.get(compass);
            h.assertTrue(
                    updated.size() == 2
                            && updated.getSelected().pos().equals(second)
                            && updated.hidden().isEmpty()
                            && updated.navigationHidden(),
                    "Forgetting an earlier marker preserves the remaining selection and display setting");
            h.assertTrue(
                    Waypoints.get(other).size() == 3
                            && LodestoneRegistry.get(h.getLevel().getServer())
                                    .get(first)
                                    .name()
                                    .equals("First"),
                    "Other compasses and the named lodestone stay intact");
            use(player, hand, first);
            h.assertTrue(Waypoints.get(compass).equals(updated), "Forgetting an absent marker is a no-op");
            player.setShiftKeyDown(false);
            use(player, hand, first);
            h.assertTrue(
                    Waypoints.get(compass).size() == 3
                            && Waypoints.get(compass).getSelected().pos().equals(first),
                    "Normal use can bind the forgotten destination again");
        }
        h.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void forgettingMovedSelectedStoneFindsVisibleFallback(GameTestHelper h) {
        var player = player(h);
        var from = stone(h, new BlockPos(2, 2, 1), "Moving");
        var other = stone(h, new BlockPos(3, 2, 1), "Other");
        var to = stone(h, new BlockPos(4, 2, 1), "New location");
        var compass = marked(h, player, from, other);
        var data = Waypoints.get(compass);
        var spawn = data.get(0).pos();
        NavigationComponents.write(compass, new CompassWaypoints(data.waypoints(), 1, List.of(spawn, from, to), false));
        LodestoneRegistry.get(h.getLevel().getServer()).relocate(from, to);
        player.setItemInHand(InteractionHand.MAIN_HAND, compass);
        player.setShiftKeyDown(true);
        use(player, InteractionHand.MAIN_HAND, to);
        var updated = Waypoints.get(compass);
        h.assertTrue(
                updated.size() == 2
                        && updated.getSelected().pos().equals(other)
                        && updated.hidden().equals(List.of(spawn)),
                "Relocated markers and hidden aliases are removed while a visible destination is selected");
        h.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void grindstoneClearsEitherInputWithoutDuplicatingItemsOrExperience(GameTestHelper h) {
        var player = player(h);
        var stone = stone(h, new BlockPos(2, 2, 1), "Home");
        var original = marked(h, player, stone);
        original.set(DataComponents.CUSTOM_NAME, Component.literal("Traveller's compass"));
        original.enchant(
                h.getLevel()
                        .registryAccess()
                        .lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(Enchantments.UNBREAKING),
                1);
        var expected = original.copy();
        expected.remove(NavigationComponents.WAYPOINTS.get());
        var position = h.absolutePos(new BlockPos(1, 2, 1));
        var area = new AABB(position).inflate(3);
        h.setBlock(new BlockPos(1, 2, 1), Blocks.GRINDSTONE);
        for (int input : new int[] {0, 1}) {
            for (var click : List.of(ClickType.PICKUP, ClickType.QUICK_MOVE)) {
                player.getInventory().clearContent();
                var menu = new GrindstoneMenu(
                        1, player.getInventory(), ContainerLevelAccess.create(h.getLevel(), position));
                h.assertTrue(
                        menu.getSlot(input).mayPlace(original), "Either grindstone input accepts a marked compass");
                menu.getSlot(input).set(original.copy());
                h.assertTrue(
                        ItemStack.isSameItemSameComponents(menu.getSlot(2).getItem(), expected),
                        "Output removes only the navigation component, retaining name and enchantments");
                h.assertTrue(
                        Waypoints.isMarkedCompass(menu.getSlot(input).getItem()), "Preview leaves the input marked");
                int orbs = h.getLevel()
                        .getEntitiesOfClass(ExperienceOrb.class, area)
                        .size();
                menu.clicked(2, 0, click, player);
                var result = click == ClickType.PICKUP
                        ? menu.getCarried()
                        : player.getInventory().items.stream()
                                .filter(stack -> stack.is(Items.COMPASS))
                                .findFirst()
                                .orElse(ItemStack.EMPTY);
                h.assertTrue(
                        result.getCount() == 1
                                && ItemStack.isSameItemSameComponents(result, expected)
                                && !menu.getSlot(0).hasItem()
                                && !menu.getSlot(1).hasItem()
                                && !menu.getSlot(2).hasItem(),
                        "Taking the result returns exactly one cleared compass and consumes the input");
                h.assertTrue(
                        h.getLevel()
                                        .getEntitiesOfClass(ExperienceOrb.class, area)
                                        .size()
                                == orbs,
                        "Clearing retained enchantments never awards duplicate enchantment experience");
            }
        }
        h.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void grindstoneRejectsExtraInputsAndKeepsVanillaDisenchanting(GameTestHelper h) {
        var player = player(h);
        var compass = marked(h, player, stone(h, new BlockPos(2, 2, 1), "Saved"));
        var menu = new GrindstoneMenu(1, player.getInventory());
        h.assertTrue(
                !menu.getSlot(0).mayPlace(new ItemStack(Items.COMPASS))
                        && !menu.getSlot(1).mayPlace(new ItemStack(Items.RECOVERY_COMPASS)),
                "Unmarked and recovery compasses are not navigation-clearing inputs");
        menu.getSlot(0).set(compass.copy());
        // Normal insertion clamps to one; simulate a legacy stack already present in the input.
        menu.getSlot(0).getItem().setCount(2);
        menu.getSlot(0).setChanged();
        h.assertTrue(!menu.getSlot(2).hasItem(), "A legacy multi-compass stack cannot be consumed as one compass");
        menu.getSlot(0).set(compass.copy());
        menu.getSlot(1).set(compass.copy());
        h.assertTrue(!menu.getSlot(2).hasItem(), "Two marked compasses do not produce a clearing output");
        menu.getSlot(1).set(ItemStack.EMPTY);
        menu.getSlot(0).set(ItemStack.EMPTY);
        var tool = new ItemStack(Items.IRON_PICKAXE);
        tool.enchant(
                h.getLevel()
                        .registryAccess()
                        .lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(Enchantments.UNBREAKING),
                1);
        menu.getSlot(0).set(tool);
        h.assertTrue(
                menu.getSlot(2).getItem().is(Items.IRON_PICKAXE)
                        && !menu.getSlot(2).getItem().isEnchanted(),
                "Ordinary grindstone disenchanting still works after navigation inputs");
        h.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void clearingHiddenMarksRestoresOnlyAutomaticDestinations(GameTestHelper h) {
        var player = player(h);
        player.setRespawnPosition(h.getLevel().dimension(), h.absolutePos(new BlockPos(3, 2, 1)), 0, true, false);
        var compass = new ItemStack(Items.COMPASS);
        Waypoints.refresh(compass, player);
        var data = Waypoints.get(compass);
        NavigationComponents.write(
                compass,
                new CompassWaypoints(data.waypoints(), 1, List.of(data.get(0).pos()), true));
        var menu = new GrindstoneMenu(1, player.getInventory());
        h.assertTrue(menu.getSlot(0).mayPlace(compass), "A compass with only hidden-marker preferences can be cleared");
        menu.getSlot(0).set(compass);
        var result = menu.getSlot(2).getItem().copy();
        h.assertTrue(
                result.is(Items.COMPASS) && !result.has(NavigationComponents.WAYPOINTS.get()),
                "Clearing removes saved visibility preferences");
        Waypoints.refresh(result, player);
        var rebuilt = Waypoints.get(result);
        h.assertTrue(
                rebuilt.size() == 2
                        && rebuilt.indexOf(WaypointType.ORIGIN) == 0
                        && rebuilt.indexOf(WaypointType.RESPAWN) == 1
                        && rebuilt.hidden().isEmpty()
                        && !rebuilt.navigationHidden(),
                "Spawn and respawn are repopulated with default navigation preferences");
        h.succeed();
    }

    private static ItemStack marked(GameTestHelper h, ServerPlayer player, GlobalPos... stones) {
        var compass = new ItemStack(Items.COMPASS);
        Waypoints.refresh(compass, player);
        for (var stone : stones)
            Waypoints.bindLodestone(
                    compass,
                    player,
                    stone,
                    LodestoneRegistry.get(h.getLevel().getServer()).get(stone));
        return compass;
    }

    private static void use(ServerPlayer player, InteractionHand hand, GlobalPos pos) {
        var hit = new BlockHitResult(Vec3.atCenterOf(pos.pos()), Direction.UP, pos.pos(), false);
        Items.COMPASS.useOn(new UseOnContext(player, hand, hit));
    }

    private static GlobalPos stone(GameTestHelper h, BlockPos local, String name) {
        h.setBlock(local, Blocks.LODESTONE);
        var pos = GlobalPos.of(h.getLevel().dimension(), h.absolutePos(local));
        LodestoneRegistry.get(h.getLevel().getServer()).set(pos, new LodestoneMark(name, WaypointIcons.DEFAULT));
        return pos;
    }

    private static ServerPlayer player(GameTestHelper h) {
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "compass-mark-test"), false);
        var player = new ServerPlayer(
                h.getLevel().getServer(), h.getLevel(), cookie.gameProfile(), cookie.clientInformation());
        new ServerGamePacketListenerImpl(
                h.getLevel().getServer(), new Connection(PacketFlow.SERVERBOUND), player, cookie) {
            @Override
            public void send(Packet<?> packet) {}
        };
        player.setPos(Vec3.atCenterOf(h.absolutePos(new BlockPos(1, 2, 1))));
        return player;
    }
}
