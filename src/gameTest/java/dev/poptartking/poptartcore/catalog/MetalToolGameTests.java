package dev.poptartking.poptartcore.catalog;

import static dev.poptartking.poptartcore.catalog.ToolTestSupport.effectiveAttribute;
import static dev.poptartking.poptartcore.catalog.ToolTestSupport.item;

import dev.poptartking.poptartcore.PoptartCore;
import java.util.List;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vectorwing.farmersdelight.common.item.KnifeItem;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MetalToolGameTests {
    private static final List<Metal> METALS = List.of(
            new Metal("bronze", 275, 4, 1, 10, BlockTags.INCORRECT_FOR_STONE_TOOL),
            new Metal("steel", 1250, 8, 3, 15, BlockTags.INCORRECT_FOR_DIAMOND_TOOL));
    private static final List<Tool> TOOLS = List.of(
            new Tool("sword", SwordItem.class, Blocks.COBWEB, 4, 6, 1.6),
            new Tool("pickaxe", PickaxeItem.class, Blocks.STONE, 2, 4, 1.2),
            new Tool("axe", AxeItem.class, Blocks.OAK_LOG, 7, 9, 0.9),
            new Tool("shovel", ShovelItem.class, Blocks.DIRT, 2.5, 4.5, 1),
            new Tool("knife", KnifeItem.class, Blocks.PUMPKIN, 3, 5, 2));

    private MetalToolGameTests() {}

    private record Metal(
            String id,
            int durability,
            float miningSpeed,
            float damageBonus,
            int enchantability,
            TagKey<Block> incorrectDrops) {
        Item ingot() {
            return item(id + "_ingot");
        }
    }

    private record Tool(
            String id,
            Class<? extends TieredItem> type,
            Block efficientBlock,
            double bronzeDamage,
            double steelDamage,
            double attackSpeed) {}

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void actualToolsKeepTheirStatsAndRepairMaterials(GameTestHelper helper) {
        for (var metal : METALS)
            for (var tool : TOOLS) {
                String id = metal.id() + "_" + tool.id();
                var stack = new ItemStack(item(id));
                helper.assertTrue(tool.type().isInstance(stack.getItem()), "Wrong tool type: " + id);
                var tiered = (TieredItem) stack.getItem();
                var tier = tiered.getTier();
                helper.assertTrue(
                        stack.getMaxDamage() == metal.durability() && stack.getMaxStackSize() == 1,
                        "Wrong durability or stack size: " + id);
                helper.assertTrue(
                        tier.getUses() == metal.durability()
                                && tier.getSpeed() == metal.miningSpeed()
                                && tier.getAttackDamageBonus() == metal.damageBonus()
                                && tier.getIncorrectBlocksForDrops().equals(metal.incorrectDrops())
                                && stack.getEnchantmentValue() == metal.enchantability(),
                        "Wrong effective tier settings: " + id);
                helper.assertTrue(
                        tiered.isValidRepairItem(stack, new ItemStack(metal.ingot())),
                        "Rejects its repair ingot: " + id);
                for (var wrong : List.of(
                        Items.IRON_INGOT,
                        Items.STICK,
                        Items.DIRT,
                        item(metal.id().equals("bronze") ? "steel_ingot" : "bronze_ingot"))) {
                    helper.assertTrue(
                            !tiered.isValidRepairItem(stack, new ItemStack(wrong)),
                            "Accepts a wrong repair ingredient: " + id);
                }
                double damage = metal.id().equals("bronze") ? tool.bronzeDamage() : tool.steelDamage();
                helper.assertTrue(
                        Math.abs(effectiveAttribute(stack, Attributes.ATTACK_DAMAGE, 1) - damage) < 0.00001,
                        "Wrong main-hand attack damage: " + id);
                helper.assertTrue(
                        Math.abs(effectiveAttribute(stack, Attributes.ATTACK_SPEED, 4) - tool.attackSpeed()) < 0.00001,
                        "Wrong main-hand attack speed: " + id);
            }
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void actualToolsMineTheAppropriateBlocks(GameTestHelper helper) {
        for (var metal : METALS) {
            for (var tool : TOOLS) {
                String id = metal.id() + "_" + tool.id();
                var stack = new ItemStack(item(id));
                float speed = tool.id().equals("sword") ? 15 : metal.miningSpeed();
                helper.assertTrue(
                        stack.getDestroySpeed(tool.efficientBlock().defaultBlockState()) == speed,
                        "Wrong mining speed on the appropriate block: " + id);
                var inefficient = tool.id().equals("pickaxe") ? Blocks.OAK_LOG : Blocks.STONE;
                helper.assertTrue(
                        stack.getDestroySpeed(inefficient.defaultBlockState()) == 1,
                        "Unexpected mining bonus on an inappropriate block: " + id);
                helper.assertTrue(
                        stack.isCorrectToolForDrops(tool.efficientBlock().defaultBlockState()),
                        "Cannot harvest its appropriate block: " + id);
            }
            var pickaxe = new ItemStack(item(metal.id() + "_pickaxe"));
            helper.assertTrue(
                    pickaxe.isCorrectToolForDrops(Blocks.IRON_ORE.defaultBlockState()),
                    "Pickaxe cannot harvest iron ore: " + metal.id());
            boolean highTier = metal.id().equals("steel");
            for (var block : List.of(Blocks.DIAMOND_ORE, Blocks.OBSIDIAN)) {
                helper.assertTrue(
                        pickaxe.isCorrectToolForDrops(block.defaultBlockState()) == highTier,
                        "Wrong mining level for " + block + ": " + metal.id());
            }
            helper.assertTrue(
                    !pickaxe.isCorrectToolForDrops(Blocks.DIRT.defaultBlockState()),
                    "Pickaxe incorrectly replaces a shovel: " + metal.id());
        }
        helper.succeed();
    }
}
