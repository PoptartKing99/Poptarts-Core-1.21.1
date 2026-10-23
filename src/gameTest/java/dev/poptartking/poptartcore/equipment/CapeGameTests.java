package dev.poptartking.poptartcore.equipment;

import com.mojang.authlib.GameProfile;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.integration.ragdoll.AccessoryCorpseProvider;
import dev.poptartking.poptartcore.registry.PoptartCapeItems;
import dev.poptartking.poptartcore.registry.PoptartCoreAttachments;
import dev.poptartking.poptartcore.registry.PoptartCoreTabs;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CapeGameTests {
    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void takingCraftedCapeConsumesExactlyOneDyeAndCape(GameTestHelper helper) {
        var cookie =
                CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "cape-crafting-test"), false);
        var player = new ServerPlayer(
                helper.getLevel().getServer(), helper.getLevel(), cookie.gameProfile(), cookie.clientInformation());
        // Crafting requires a ServerPlayer, but this server-side test has no network client or login handshake.
        new ServerGamePacketListenerImpl(
                helper.getLevel().getServer(), new Connection(PacketFlow.SERVERBOUND), player, cookie) {
            @Override
            public void send(Packet<?> packet) {}
        };
        {
            var menu = player.inventoryMenu;
            menu.getSlot(1).set(new ItemStack(PoptartCapeItems.AGILITY_CAPE.get()));
            menu.getSlot(2).set(new ItemStack(Items.PURPLE_DYE, 8));
            helper.assertTrue(
                    menu.getSlot(0).hasItem()
                            && CapeColors.color(menu.getSlot(0).getItem()) == DyeColor.PURPLE,
                    "Native inventory crafting grid previews selected dye");
            menu.clicked(0, 0, net.minecraft.world.inventory.ClickType.PICKUP, player);
            helper.assertTrue(
                    menu.getCarried().is(PoptartCapeItems.AGILITY_CAPE.get())
                            && menu.getCarried().getCount() == 1
                            && CapeColors.color(menu.getCarried()) == DyeColor.PURPLE,
                    "Taking result gives one correctly dyed cape");
            helper.assertTrue(
                    menu.getSlot(1).getItem().isEmpty()
                            && menu.getSlot(2).getItem().getCount() == 7,
                    "Actual result-slot transfer consumes one cape and one dye");
            helper.assertTrue(menu.getSlot(0).getItem().isEmpty(), "No second result without another cape");
            helper.succeed();
        }
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void allDyesWorkInBothCraftingGridsAndRedyeWithoutMixing(GameTestHelper helper) {
        var cape = new ItemStack(PoptartCapeItems.AGILITY_CAPE.get());
        cape.set(DataComponents.CUSTOM_NAME, Component.literal("My Cape"));
        var original = cape.copy();
        for (var color : DyeColor.values()) {
            for (int width : new int[] {2, 3}) {
                var items = new ArrayList<>(Collections.nCopies(width * width, ItemStack.EMPTY));
                items.set(0, cape);
                var dye = new ItemStack(DyeItem.byColor(color), 8);
                items.set(items.size() - 1, dye);
                var input = CraftingInput.of(width, width, items);
                var recipe = helper.getLevel()
                        .getRecipeManager()
                        .getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel())
                        .orElseThrow();
                helper.assertTrue(
                        recipe.id().equals(PoptartCore.location("cape_dye")) && recipe.value() instanceof CapeDyeRecipe,
                        "Loaded cape dye recipe must match both grids");
                var result = recipe.value().assemble(input, helper.getLevel().registryAccess());
                helper.assertTrue(
                        result.is(PoptartCapeItems.AGILITY_CAPE.get())
                                && result.getCount() == 1
                                && CapeColors.color(result) == color,
                        "Each dye selects its fixed cape color");
                helper.assertTrue(result.getHoverName().getString().equals("My Cape"), "Dyeing preserves custom name");
                helper.assertTrue(
                        ItemStack.matches(cape, original) && dye.getCount() == 8,
                        "Recipe preview must not consume or mutate input");
                helper.assertTrue(
                        recipe.value().getRemainingItems(input).stream().allMatch(ItemStack::isEmpty),
                        "No duplicated cape or dye remainder");
                helper.assertTrue(
                        CapeColors.modelProperty(result) == (color.getId() + 1) / 16F,
                        "Inventory model selects the same saved dye as worn cape");
                helper.assertTrue(
                        AgilityCape.texture(result)
                                .equals(PoptartCore.location("textures/entity/cape/" + color.getName() + ".png")),
                        "Worn, corpse and elytra use the selected texture");
                var recolor = CraftingInput.of(2, 1, List.of(result, new ItemStack(Items.RED_DYE)));
                var red = recipe.value().assemble(recolor, helper.getLevel().registryAccess());
                helper.assertTrue(
                        CapeColors.color(red) == DyeColor.RED, "Re-dyeing replaces the color, never mixes it");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void rejectsMissingExtraAndMixedIngredients(GameTestHelper helper) {
        var recipe = new CapeDyeRecipe(net.minecraft.world.item.crafting.CraftingBookCategory.EQUIPMENT);
        var cape = new ItemStack(PoptartCapeItems.AGILITY_CAPE.get());
        for (var items : List.of(
                List.of(cape),
                List.of(new ItemStack(Items.RED_DYE)),
                List.of(cape, new ItemStack(Items.RED_DYE), new ItemStack(Items.BLUE_DYE)),
                List.of(cape, cape.copy(), new ItemStack(Items.RED_DYE)),
                List.of(cape, new ItemStack(Items.RED_DYE), new ItemStack(Items.DIRT)),
                List.of(new ItemStack(Items.LEATHER_CHESTPLATE), new ItemStack(Items.RED_DYE)))) {
            var input = CraftingInput.of(items.size(), 1, items);
            helper.assertTrue(
                    !recipe.matches(input, helper.getLevel())
                            && recipe.assemble(input, helper.getLevel().registryAccess())
                                    .isEmpty(),
                    "Only exactly one cape plus one dye slot is valid");
        }
        helper.assertTrue(
                recipe.canCraftInDimensions(2, 2)
                        && recipe.canCraftInDimensions(3, 3)
                        && !recipe.canCraftInDimensions(1, 1),
                "Both grids fit, one slot does not");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void onlyOneCapeIsRegisteredAndShown(GameTestHelper helper) {
        var undyed = new ItemStack(PoptartCapeItems.AGILITY_CAPE.get());
        helper.assertTrue(
                CapeColors.color(undyed) == DyeColor.WHITE
                        && CapeColors.modelProperty(undyed) == 1 / 16F
                        && AgilityCape.texture(undyed).equals(PoptartCore.location("textures/entity/cape/white.png")),
                "Undyed cape defaults to white in inventory and when worn");
        helper.assertTrue(
                dev.poptartking.poptartcore.catalog.PoptartCatalog.items().stream()
                                .filter(definition -> definition.id().endsWith("_cape"))
                                .count()
                        == 1,
                "Exactly one cape item registered");
        var tab = PoptartCoreTabs.POPTARTCORE_TAB.get();
        tab.buildContents(new CreativeModeTab.ItemDisplayParameters(
                helper.getLevel().enabledFeatures(), true, helper.getLevel().registryAccess()));
        helper.assertTrue(
                tab.getDisplayItems().stream().filter(CapeColors::isCape).count() == 1,
                "Exactly one cape in creative tab");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void dyedCapeEquipsSyncsSavesAndTransfersToCorpse(GameTestHelper helper) {
        for (var color : DyeColor.values()) {
            var player = helper.makeMockPlayer(GameType.SURVIVAL);
            var cape = new ItemStack(PoptartCapeItems.AGILITY_CAPE.get());
            CapeColors.setColor(cape, color);
            var serialized = (CompoundTag) cape.save(helper.getLevel().registryAccess());
            var loaded = ItemStack.parseOptional(helper.getLevel().registryAccess(), serialized);
            var expectedVisual = loaded.copy();
            helper.assertTrue(CapeColors.color(loaded) == color, "Dye survives save/load");
            player.setItemInHand(InteractionHand.MAIN_HAND, loaded);
            AgilityCape.equipOnRightClick(new PlayerInteractEvent.RightClickItem(player, InteractionHand.MAIN_HAND));
            helper.assertTrue(
                    player.getMainHandItem().isEmpty()
                            && CapeColors.color(AccessoryInventory.get(player).getStackInSlot(AgilityCape.SLOT))
                                    == color,
                    "Right-click transfers dyed cape with color intact");
            var visual = player.getData(PoptartCoreAttachments.CAPE_VISUAL);
            helper.assertTrue(
                    CapeColors.color(visual) == color && ItemStack.matches(visual, expectedVisual),
                    "Appearance carries selected color independently of consumed held stack");
            var provider = new AccessoryCorpseProvider();
            var captured = provider.getStack(player, AgilityCape.SLOT).copy();
            helper.assertTrue(
                    CapeColors.color(captured) == color
                            && AgilityCape.texture(captured).equals(AgilityCape.texture(visual)),
                    "Corpse captured stack selects the same dye texture");
            provider.setStack(player, AgilityCape.SLOT, ItemStack.EMPTY);
            new PlayerAccessoryHandler(player).setStackInSlot(AgilityCape.SLOT, captured);
            AgilityCape.sync(new PlayerTickEvent.Post(player));
            helper.assertTrue(
                    CapeColors.color(player.getData(PoptartCoreAttachments.CAPE_VISUAL)) == color,
                    "Corpse recovery retains color");
        }
        helper.succeed();
    }
}
