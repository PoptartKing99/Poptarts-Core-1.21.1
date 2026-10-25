package dev.poptartking.poptartcore.backpack;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.equipment.AccessoryInventory;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.ArmorDyeRecipe;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BackpackDyeGameTests {
    private static ItemStack pack() {
        var stack = new ItemStack(PoptartCoreBlocks.BACKPACK.get());
        stack.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Colored pack"));
        BackpackMenu.contents(stack).setItem(26, new ItemStack(Items.DIAMOND, 5));
        return stack;
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void bagsRejectCraftingDyesButColorScreenSupportsAllColors(GameTestHelper helper) {
        var recipe = new ArmorDyeRecipe(CraftingBookCategory.MISC);
        for (var block : List.of(PoptartCoreBlocks.BACKPACK.get(), PoptartCoreBlocks.RUCKSACK.get())) {
            var colors = new java.util.HashSet<Integer>();
            for (var dye : DyeColor.values()) {
                var stack = new ItemStack(block);
                var input = CraftingInput.of(2, 1, List.of(stack, new ItemStack(DyeItem.byColor(dye))));
                helper.assertTrue(!recipe.matches(input, helper.getLevel()), "Bag does not match vanilla dye recipe");
                helper.assertTrue(
                        helper.getLevel()
                                .getRecipeManager()
                                .getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel())
                                .isEmpty(),
                        "No loaded crafting dye recipe matches");
                var preview = BackpackColors.dyed(stack, BackpackColors.Part.FLAPS, DyeItem.byColor(dye));
                colors.add(BackpackColors.color(preview, BackpackColors.Part.FLAPS));
                helper.assertTrue(!stack.has(DataComponents.DYED_COLOR), "Screen preview leaves original unchanged");
            }
            helper.assertTrue(colors.size() == 16, "Color screen supports all vanilla dye colors");
        }
        var leather = CraftingInput.of(
                2, 1, List.of(new ItemStack(Items.LEATHER_CHESTPLATE), new ItemStack(Items.GREEN_DYE)));
        helper.assertTrue(recipe.matches(leather, helper.getLevel()), "Vanilla leather dye recipe is unchanged");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void mixedDyesAndRedyeingUseVanillaColorRules(GameTestHelper helper) {
        var original = pack();
        var blue = DyeItem.byColor(DyeColor.BLUE);
        var white = DyeItem.byColor(DyeColor.WHITE);
        var result = BackpackColors.dyed(
                BackpackColors.dyed(original, BackpackColors.Part.FLAPS, blue), BackpackColors.Part.FLAPS, white);
        var expected = DyedItemColor.applyDyes(new ItemStack(Items.LEATHER_CHESTPLATE), List.of(blue, white));
        helper.assertTrue(
                result.get(DataComponents.DYED_COLOR).rgb()
                        == expected.get(DataComponents.DYED_COLOR).rgb(),
                "Screen dye mixing follows vanilla rules");
        var redyed = BackpackColors.dyed(result, BackpackColors.Part.FLAPS, DyeItem.byColor(DyeColor.GREEN));
        helper.assertTrue(
                redyed.has(DataComponents.DYED_COLOR)
                        && BackpackMenu.contents(redyed).getItem(26).getCount() == 5,
                "Screen redyeing preserves storage");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void dyeSurvivesPlacementSaveLootPickupAndVisualSnapshot(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var ground = helper.absolutePos(new BlockPos(4, 1, 4));
        var pos = ground.above();
        player.setPos(Vec3.atCenterOf(pos).add(0, 0, 2));
        player.setShiftKeyDown(true);
        helper.getLevel().setBlockAndUpdate(ground, Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        var stack = BackpackColors.dyed(pack(), BackpackColors.Part.FLAPS, DyeItem.byColor(DyeColor.GREEN));
        BackpackColors.set(stack, BackpackColors.Part.BODY, 0x123456);
        int color = stack.get(DataComponents.DYED_COLOR).rgb();
        BackpackItem.equip(player, stack);
        var hit = new BlockHitResult(Vec3.atCenterOf(ground).add(0, .5, 0), Direction.UP, ground, false);
        helper.assertTrue(BackpackGestures.place(player, hit), "Place dyed backpack");
        var state = helper.getLevel().getBlockState(pos);
        helper.assertTrue(state.getValue(BackpackBlock.DYED), "Placed block selects dyed model");
        helper.assertTrue(state.getValue(BackpackBlock.BODY_DYED), "Placement selects independent body material");
        var entity = (BackpackBlockEntity) helper.getLevel().getBlockEntity(pos);
        helper.assertTrue(entity.dyeColor() == color, "Block entity receives dye");
        var restored = new BackpackBlockEntity(pos, state);
        restored.loadWithComponents(
                entity.saveWithFullMetadata(helper.getLevel().registryAccess()),
                helper.getLevel().registryAccess());
        helper.assertTrue(
                restored.dyeColor() == color && restored.getItem(26).getCount() == 5,
                "Color and inventory survive reload");
        var update = entity.getUpdateTag(helper.getLevel().registryAccess());
        helper.assertTrue(
                update.getInt("FlapDye") == color && !update.contains("Items"),
                "Client receives color without private inventory");
        var drops = Block.getDrops(state, helper.getLevel(), pos, entity);
        helper.assertTrue(
                drops.size() == 1
                        && drops.getFirst().get(DataComponents.DYED_COLOR).rgb() == color,
                "Block drops preserve dye");
        helper.assertTrue(BackpackGestures.pickUp(player, pos), "Pick up dyed backpack");
        var worn = AccessoryInventory.get(player).getStackInSlot(AccessoryInventory.BACK_SLOT);
        var visual = BackpackVisual.renderStack(worn);
        helper.assertTrue(
                visual.get(DataComponents.DYED_COLOR).rgb() == color
                        && !visual.has(DataComponents.CONTAINER)
                        && visual.get(BackpackColors.BODY.get()) == 0x123456,
                "Worn/corpse visual keeps color without copying inventory");
        helper.succeed();
    }
}
