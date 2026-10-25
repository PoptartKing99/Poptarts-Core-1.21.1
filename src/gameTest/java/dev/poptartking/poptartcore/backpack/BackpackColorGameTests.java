package dev.poptartking.poptartcore.backpack;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BackpackColorGameTests {
    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void rucksackColorsMenuKeepsAll54SlotsAndMixesBothParts(GameTestHelper helper) {
        var packets = new java.util.ArrayList<net.minecraft.network.protocol.Packet<?>>();
        var player = menuPlayer(helper, packets);
        var stack = new ItemStack(PoptartCoreBlocks.RUCKSACK.get());
        stack.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("My rucksack"));
        BackpackMenu.contents(stack).setItem(53, new ItemStack(Items.EMERALD, 12));
        player.getInventory().setItem(0, stack);
        BackpackMenu.open(player, stack);
        helper.assertTrue(((BackpackMenu) player.containerMenu).getRowCount() == 6, "Rucksack opens 54-slot storage");
        helper.assertTrue(player.containerMenu.clickMenuButton(player, 0), "Rucksack opens Colors");
        var menu = (BackpackColorMenu) player.containerMenu;
        menu.getSlot(0).set(new ItemStack(Items.BLUE_DYE));
        helper.assertTrue(menu.clickMenuButton(player, BackpackColorMenu.APPLY), "Body dye applied");
        int body = BackpackColors.color(stack, BackpackColors.Part.BODY);
        menu.clickMenuButton(player, BackpackColorMenu.SELECT_FLAPS);
        menu.getSlot(0).set(new ItemStack(Items.GREEN_DYE));
        menu.clickMenuButton(player, BackpackColorMenu.APPLY);
        int firstFlap = BackpackColors.color(stack, BackpackColors.Part.FLAPS);
        menu.getSlot(0).set(new ItemStack(Items.WHITE_DYE, 2));
        menu.clickMenuButton(player, BackpackColorMenu.APPLY);
        var expected = new ItemStack(Items.LEATHER_CHESTPLATE);
        expected.set(DataComponents.DYED_COLOR, new DyedItemColor(firstFlap, false));
        expected = DyedItemColor.applyDyes(expected, List.of((DyeItem) Items.WHITE_DYE));
        helper.assertTrue(
                BackpackColors.color(stack, BackpackColors.Part.FLAPS)
                                == expected.get(DataComponents.DYED_COLOR).rgb()
                        && BackpackColors.color(stack, BackpackColors.Part.BODY) == body,
                "Rucksack mixes selected part independently");
        menu.clickMenuButton(player, BackpackColorMenu.BACK);
        helper.assertTrue(
                ((BackpackMenu) player.containerMenu).getRowCount() == 6
                        && BackpackMenu.contents(stack).getItem(53).getCount() == 12
                        && stack.getHoverName().getString().equals("My rucksack"),
                "Back keeps full storage and name");
        helper.assertTrue(
                player.getInventory().countItem(Items.WHITE_DYE) == 1
                        && packets.stream()
                                .noneMatch(packet -> packet
                                        instanceof net.minecraft.network.protocol.game.ClientboundContainerClosePacket),
                "Unused dye returned without closing the client screen");
        player.closeContainer();
        var recipe = new net.minecraft.world.item.crafting.ArmorDyeRecipe(
                net.minecraft.world.item.crafting.CraftingBookCategory.MISC);
        for (var dye : net.minecraft.world.item.DyeColor.values()) {
            var input = net.minecraft.world.item.crafting.CraftingInput.of(
                    2, 1, List.of(stack, new ItemStack(DyeItem.byColor(dye))));
            helper.assertTrue(!recipe.matches(input, helper.getLevel()), "Rucksack rejects crafting-table dye " + dye);
            var result = BackpackColors.dyed(stack, BackpackColors.Part.FLAPS, DyeItem.byColor(dye));
            helper.assertTrue(
                    BackpackColors.color(result, BackpackColors.Part.BODY) == body
                            && BackpackMenu.contents(result).getItem(53).getCount() == 12,
                    "Screen dyeing keeps body and slot 54");
        }
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void rucksackBothColorsSurvivePlacementSaveLootAndUpperPickup(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var ground = helper.absolutePos(new BlockPos(4, 1, 4));
        var pos = ground.above();
        helper.getLevel().setBlockAndUpdate(ground, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
        helper.getLevel()
                .setBlockAndUpdate(pos.above(), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
        var stack = new ItemStack(PoptartCoreBlocks.RUCKSACK.get());
        BackpackMenu.contents(stack).setItem(53, new ItemStack(Items.DIAMOND, 9));
        BackpackColors.set(stack, BackpackColors.Part.BODY, 0x123456);
        BackpackColors.set(stack, BackpackColors.Part.FLAPS, 0x654321);
        player.setPos(net.minecraft.world.phys.Vec3.atCenterOf(pos).add(0, 0, 2));
        player.setShiftKeyDown(true);
        helper.assertTrue(BackpackItem.equip(player, stack), "Equip colored rucksack");
        var hit = new net.minecraft.world.phys.BlockHitResult(
                net.minecraft.world.phys.Vec3.atCenterOf(ground).add(0, .5, 0),
                net.minecraft.core.Direction.UP,
                ground,
                false);
        helper.assertTrue(BackpackGestures.place(player, hit), "Place colored rucksack");
        var state = helper.getLevel().getBlockState(pos);
        helper.assertTrue(
                state.getValue(BackpackBlock.DYED) && state.getValue(BackpackBlock.BODY_DYED),
                "Both materials selected");
        var entity = (BackpackBlockEntity) helper.getLevel().getBlockEntity(pos);
        var loaded = new BackpackBlockEntity(pos, state);
        loaded.loadWithComponents(
                entity.saveWithFullMetadata(helper.getLevel().registryAccess()),
                helper.getLevel().registryAccess());
        helper.assertTrue(
                loaded.dyeColor(1) == 0x123456
                        && loaded.dyeColor(0) == 0x654321
                        && loaded.getItem(53).getCount() == 9,
                "Both colors and all storage reload");
        var drop = Block.getDrops(state, helper.getLevel(), pos, entity).getFirst();
        helper.assertTrue(
                BackpackColors.color(drop, BackpackColors.Part.BODY) == 0x123456
                        && BackpackColors.color(drop, BackpackColors.Part.FLAPS) == 0x654321,
                "Block loot keeps both colors");
        var edit = new BackpackColorMenu(1, player.getInventory(), new BackpackColorTarget.Placed(entity));
        edit.getSlot(0).set(new ItemStack(Items.WHITE_DYE));
        helper.assertTrue(edit.clickMenuButton(player, BackpackColorMenu.APPLY), "Placed rucksack can be recolored");
        int body = entity.dyeColor(1);
        helper.assertTrue(BackpackGestures.pickUp(player, pos.above()), "Pick up through upper half");
        var picked = dev.poptartking.poptartcore.equipment.AccessoryInventory.get(player)
                .getStackInSlot(dev.poptartking.poptartcore.equipment.AccessoryInventory.BACK_SLOT);
        var visual = BackpackVisual.renderStack(picked);
        helper.assertTrue(
                BackpackColors.color(visual, BackpackColors.Part.BODY) == body
                        && BackpackColors.color(visual, BackpackColors.Part.FLAPS) == 0x654321
                        && !visual.has(DataComponents.CONTAINER)
                        && BackpackMenu.contents(picked).getItem(53).getCount() == 9,
                "Pickup and worn/corpse visual retain colors without exposing storage");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void colorPacketsRefreshBothColorsWithoutReloadingStorage(GameTestHelper helper) {
        var state = PoptartCoreBlocks.BACKPACK.get().defaultBlockState();
        var sender = new BackpackBlockEntity(BlockPos.ZERO, state);
        var receiver = new BackpackBlockEntity(BlockPos.ZERO, state);
        receiver.setItem(0, new ItemStack(Items.DIAMOND, 5));
        var registries = helper.getLevel().registryAccess();
        sender.setColor(BackpackColors.Part.BODY, 0x123456);
        sender.setColor(BackpackColors.Part.FLAPS, 0x654321);
        receiver.handleUpdateTag(sender.getUpdateTag(registries), registries);
        helper.assertTrue(
                receiver.dyeColor(1) == 0x123456 && receiver.dyeColor(0) == 0x654321,
                "Chunk update receives both colors");
        sender.setColor(BackpackColors.Part.BODY, 0xABCDEF);
        sender.setColor(BackpackColors.Part.FLAPS, 0xFEDCBA);
        sender.setLevel(helper.getLevel());
        receiver.onDataPacket(null, sender.getUpdatePacket(), registries);
        helper.assertTrue(
                receiver.dyeColor(1) == 0xABCDEF && receiver.dyeColor(0) == 0xFEDCBA,
                "Redye packet updates both colors without changing block flags");
        helper.assertTrue(receiver.getItem(0).getCount() == 5, "Color packets preserve storage");
        receiver.handleUpdateTag(new net.minecraft.nbt.CompoundTag(), registries);
        helper.assertTrue(
                receiver.dyeColor(1) == 0xFFFFFF && receiver.dyeColor(0) == 0xFFFFFF,
                "Empty color update clears stale colors");
        helper.succeed();
    }

    private static net.minecraft.server.level.ServerPlayer menuPlayer(
            GameTestHelper helper, java.util.List<net.minecraft.network.protocol.Packet<?>> packets) {
        var level = helper.getLevel();
        var server = level.getServer();
        var cookie = net.minecraft.server.network.CommonListenerCookie.createInitial(
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "backpack-menu-test"), false);
        var player = new net.minecraft.server.level.ServerPlayer(
                server, level, cookie.gameProfile(), cookie.clientInformation());
        var connection = new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        // Exercise vanilla server menus without logging into unrelated mods or requiring a client handshake.
        player.connection =
                new net.minecraft.server.network.ServerGamePacketListenerImpl(server, connection, player, cookie) {
                    @Override
                    public void send(net.minecraft.network.protocol.Packet<?> packet) {
                        packets.add(packet);
                    }

                    @Override
                    public void send(
                            net.minecraft.network.protocol.Packet<?> packet,
                            net.minecraft.network.PacketSendListener listener) {
                        packets.add(packet);
                    }
                };
        return player;
    }

    private static ItemStack pack() {
        var stack = new ItemStack(PoptartCoreBlocks.BACKPACK.get());
        stack.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("My pack"));
        BackpackMenu.contents(stack).setItem(26, new ItemStack(Items.DIAMOND, 5));
        BackpackColors.set(stack, BackpackColors.Part.FLAPS, 0xAE0000);
        return stack;
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void independentColorsMixLikeLeatherWithoutMutatingPreview(GameTestHelper helper) {
        var original = pack();
        var green = (DyeItem) Items.GREEN_DYE;
        var white = (DyeItem) Items.WHITE_DYE;
        var first = BackpackColors.dyed(original, BackpackColors.Part.BODY, green);
        var second = BackpackColors.dyed(first, BackpackColors.Part.BODY, white);
        var leather = DyedItemColor.applyDyes(new ItemStack(Items.LEATHER_CHESTPLATE), List.of(green));
        leather = DyedItemColor.applyDyes(leather, List.of(white));
        helper.assertTrue(
                BackpackColors.color(second, BackpackColors.Part.BODY)
                        == leather.get(DataComponents.DYED_COLOR).rgb(),
                "Repeated dyes use vanilla mixing");
        helper.assertTrue(
                !original.has(BackpackColors.BODY.get())
                        && BackpackColors.color(second, BackpackColors.Part.FLAPS) == 0xAE0000,
                "Preview leaves original and other part unchanged");
        helper.assertTrue(
                BackpackMenu.contents(second).getItem(26).getCount() == 5
                        && second.getHoverName().getString().equals("My pack"),
                "Storage and name survive");
        helper.assertTrue(
                BackpackColors.supports(new ItemStack(PoptartCoreBlocks.RUCKSACK.get())), "Rucksack supported");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void menuAppliesOneDyeLocksSourceAndReturnsUnusedDyes(GameTestHelper helper) {
        var packets = new java.util.ArrayList<net.minecraft.network.protocol.Packet<?>>();
        var player = menuPlayer(helper, packets);
        var stack = pack();
        player.getInventory().setItem(0, stack);
        BackpackMenu.open(player, stack);
        helper.assertTrue(player.containerMenu instanceof BackpackMenu, "Storage opens");
        helper.assertTrue(player.containerMenu.clickMenuButton(player, 0), "Colors opens from storage");
        helper.assertTrue(player.containerMenu instanceof BackpackColorMenu, "Color menu opens");
        var menu = (BackpackColorMenu) player.containerMenu;
        menu.getSlot(0).set(new ItemStack(Items.GREEN_DYE, 3));
        helper.assertTrue(!stack.has(BackpackColors.BODY.get()), "Preview consumes nothing");
        menu.clicked(29, 0, ClickType.PICKUP, player);
        menu.clicked(0, 0, ClickType.SWAP, player);
        menu.clicked(1, 0, ClickType.CLONE, player);
        helper.assertTrue(
                player.getInventory().getItem(0) == stack && menu.getCarried().isEmpty(),
                "Source and preview cannot be taken");
        helper.assertTrue(menu.clickMenuButton(player, BackpackColorMenu.APPLY), "Apply accepted");
        helper.assertTrue(
                stack.has(BackpackColors.BODY.get())
                        && menu.getSlot(0).getItem().getCount() == 2,
                "One application consumes exactly one dye");
        int body = BackpackColors.color(stack, BackpackColors.Part.BODY);
        menu.clickMenuButton(player, BackpackColorMenu.SELECT_FLAPS);
        menu.getSlot(0).set(new ItemStack(Items.BLUE_DYE, 2));
        menu.clickMenuButton(player, BackpackColorMenu.APPLY);
        helper.assertTrue(
                BackpackColors.color(stack, BackpackColors.Part.BODY) == body
                        && BackpackColors.color(stack, BackpackColors.Part.FLAPS) != 0xAE0000,
                "Switching part keeps body");
        helper.assertTrue(menu.clickMenuButton(player, BackpackColorMenu.BACK), "Back button accepted");
        helper.assertTrue(player.containerMenu instanceof BackpackMenu, "Back returns to storage");
        helper.assertTrue(
                packets.stream()
                        .noneMatch(packet ->
                                packet instanceof net.minecraft.network.protocol.game.ClientboundContainerClosePacket),
                "Colors and Back never close the client screen between menus");
        helper.assertTrue(
                menu.getSlot(0).getItem().isEmpty() && player.getInventory().countItem(Items.BLUE_DYE) == 1,
                "Unused dye returned on close");
        helper.assertTrue(BackpackMenu.contents(stack).getItem(26).getCount() == 5, "Editing never changes storage");
        player.closeContainer();
        player.getInventory().setItem(0, ItemStack.EMPTY);
        helper.assertTrue(
                !menu.stillValid(player) && !menu.clickMenuButton(player, BackpackColorMenu.APPLY),
                "Stale target rejected");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void placedColorsPersistAndDropsKeepBothParts(GameTestHelper helper) {
        var pos = helper.absolutePos(new BlockPos(4, 2, 4));
        helper.getLevel()
                .setBlockAndUpdate(pos, PoptartCoreBlocks.BACKPACK.get().defaultBlockState());
        var entity = (BackpackBlockEntity) helper.getLevel().getBlockEntity(pos);
        entity.applyComponentsFromItemStack(pack());
        entity.setColor(BackpackColors.Part.BODY, 0x123456);
        entity.setColor(BackpackColors.Part.FLAPS, 0x654321);
        var state = helper.getLevel().getBlockState(pos);
        helper.assertTrue(
                state.getValue(BackpackBlock.DYED) && state.getValue(BackpackBlock.BODY_DYED),
                "Both materials selected");
        var loaded = new BackpackBlockEntity(pos, state);
        loaded.loadWithComponents(
                entity.saveWithFullMetadata(helper.getLevel().registryAccess()),
                helper.getLevel().registryAccess());
        helper.assertTrue(
                loaded.dyeColor(1) == 0x123456
                        && loaded.dyeColor(0) == 0x654321
                        && loaded.getItem(26).getCount() == 5,
                "Both colors and storage reload");
        var update = entity.getUpdateTag(helper.getLevel().registryAccess());
        helper.assertTrue(
                update.getInt("BodyDye") == 0x123456 && !update.contains("Items"),
                "Update contains colors without storage");
        var drop = Block.getDrops(state, helper.getLevel(), pos, entity).getFirst();
        var visual = BackpackVisual.renderStack(drop);
        helper.assertTrue(
                BackpackColors.color(visual, BackpackColors.Part.BODY) == 0x123456
                        && BackpackColors.color(visual, BackpackColors.Part.FLAPS) == 0x654321
                        && !visual.has(DataComponents.CONTAINER),
                "Loot and worn/corpse snapshot retain both colors");
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(pos.getX() + .5, pos.getY(), pos.getZ() + 1);
        var menu = new BackpackColorMenu(2, player.getInventory(), new BackpackColorTarget.Placed(entity));
        menu.getSlot(0).set(new ItemStack(Items.WHITE_DYE));
        helper.assertTrue(menu.clickMenuButton(player, BackpackColorMenu.APPLY), "Placed target accepts dye");
        helper.assertTrue(entity.getItem(26).getCount() == 5, "Placed editing keeps contents");
        player.setPos(pos.getX() + 50, pos.getY(), pos.getZ());
        helper.assertTrue(!menu.stillValid(player), "Leaving placed backpack closes editing");
        menu.removed(player);
        helper.succeed();
    }
}
