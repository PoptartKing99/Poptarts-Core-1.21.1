package dev.poptartking.poptartcore.catalog;

import static dev.poptartking.poptartcore.catalog.ToolTestSupport.effectiveAttribute;
import static dev.poptartking.poptartcore.catalog.ToolTestSupport.item;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.tool.BonePickItem;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PrimitiveToolGameTests {
    private PrimitiveToolGameTests() {}

    private record Tool(
            String id,
            Class<? extends TieredItem> type,
            int durability,
            int enchantability,
            Item repair,
            Block efficient,
            double attackDamage,
            double attackSpeed) {}

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void primitiveToolsRetainTheirActualStatsAndMiningLevel(GameTestHelper helper) {
        for (var tool : List.of(
                new Tool("bone_pick", BonePickItem.class, 150, 3, Items.BONE, Blocks.STONE, 3, 1.2),
                new Tool("flint_axe", AxeItem.class, 100, 5, Items.FLINT, Blocks.OAK_LOG, 4, 0.9),
                new Tool("flint_shovel", ShovelItem.class, 100, 5, Items.FLINT, Blocks.DIRT, 1.5, 1))) {
            var stack = new ItemStack(item(tool.id()));
            helper.assertTrue(tool.type().isInstance(stack.getItem()), "Wrong primitive tool type: " + tool.id());
            var tiered = (TieredItem) stack.getItem();
            helper.assertTrue(
                    stack.getMaxDamage() == tool.durability()
                            && stack.getMaxStackSize() == 1
                            && stack.getEnchantmentValue() == tool.enchantability(),
                    "Wrong durability, stack size, or enchantability: " + tool.id());
            helper.assertTrue(
                    tiered.getTier().getIncorrectBlocksForDrops().equals(BlockTags.INCORRECT_FOR_WOODEN_TOOL)
                            && stack.getDestroySpeed(tool.efficient().defaultBlockState()) == 3
                            && stack.isCorrectToolForDrops(tool.efficient().defaultBlockState()),
                    "Wrong primitive mining behavior: " + tool.id());
            helper.assertTrue(
                    Math.abs(effectiveAttribute(stack, Attributes.ATTACK_DAMAGE, 1) - tool.attackDamage()) < 0.00001
                            && Math.abs(effectiveAttribute(stack, Attributes.ATTACK_SPEED, 4) - tool.attackSpeed())
                                    < 0.00001,
                    "Wrong primitive combat attributes: " + tool.id());
            helper.assertTrue(
                    tiered.isValidRepairItem(stack, new ItemStack(tool.repair())),
                    "Rejects its repair material: " + tool.id());
            for (var wrong :
                    List.of(Items.IRON_INGOT, Items.STICK, tool.repair() == Items.BONE ? Items.FLINT : Items.BONE))
                helper.assertTrue(
                        !tiered.isValidRepairItem(stack, new ItemStack(wrong)),
                        "Accepts an incorrect repair material: " + tool.id());
        }
        var pick = new ItemStack(item("bone_pick"));
        helper.assertTrue(
                !pick.isCorrectToolForDrops(Blocks.IRON_ORE.defaultBlockState())
                        && !pick.isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState()),
                "Bone pick exceeds its original mining level");
        helper.assertTrue(
                pick.canPerformAction(ItemAbilities.PICKAXE_DIG) && pick.canPerformAction(ItemAbilities.HOE_TILL),
                "Bone pick must retain both tool abilities");
        for (var ability : ItemAbilities.DEFAULT_HOE_ACTIONS)
            helper.assertTrue(pick.canPerformAction(ability), "Bone pick lost a hoe ability: " + ability);
        for (var ability : ItemAbilities.DEFAULT_PICKAXE_ACTIONS)
            helper.assertTrue(pick.canPerformAction(ability), "Bone pick lost a pickaxe ability: " + ability);
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void bonePickActuallyTillsAndRespectsObstructions(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var pick = new ItemStack(item("bone_pick"));
        player.setItemInHand(InteractionHand.MAIN_HAND, pick);
        var pos = helper.absolutePos(new BlockPos(1, 1, 1));
        helper.getLevel().setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
        helper.assertTrue(pick.useOn(context(player, pos)).consumesAction(), "Bone pick rejects tillable dirt");
        helper.assertTrue(
                helper.getLevel().getBlockState(pos).is(Blocks.FARMLAND) && pick.getDamageValue() == 1,
                "Bone pick must create farmland and spend one durability");
        helper.getLevel().setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(pos.above(), Blocks.STONE.defaultBlockState());
        helper.assertTrue(
                !pick.useOn(context(player, pos)).consumesAction()
                        && helper.getLevel().getBlockState(pos).is(Blocks.DIRT)
                        && pick.getDamageValue() == 1,
                "Obstructed tilling must leave the dirt and durability unchanged");
        helper.getLevel().setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
        helper.assertTrue(
                !pick.useOn(context(player, pos)).consumesAction() && pick.getDamageValue() == 1,
                "Bone pick must not till stone or spend durability trying");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void flintToolsActuallyStripLogsAndMakePaths(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var pos = helper.absolutePos(new BlockPos(1, 1, 1));
        helper.getLevel().setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
        var axe = new ItemStack(item("flint_axe"));
        player.setItemInHand(InteractionHand.MAIN_HAND, axe);
        helper.getLevel().setBlockAndUpdate(pos, Blocks.OAK_LOG.defaultBlockState());
        helper.assertTrue(
                axe.useOn(context(player, pos)).consumesAction()
                        && helper.getLevel().getBlockState(pos).is(Blocks.STRIPPED_OAK_LOG)
                        && axe.getDamageValue() == 1,
                "Flint axe must strip a log and spend one durability");
        var shovel = new ItemStack(item("flint_shovel"));
        player.setItemInHand(InteractionHand.MAIN_HAND, shovel);
        helper.getLevel().setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
        helper.assertTrue(
                shovel.useOn(context(player, pos)).consumesAction()
                        && helper.getLevel().getBlockState(pos).is(Blocks.DIRT_PATH)
                        && shovel.getDamageValue() == 1,
                "Flint shovel must make a path and spend one durability");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void firestarterLightsCampfiresAndLastsThreeUses(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var starter = new ItemStack(item("firestarter"));
        player.setItemInHand(InteractionHand.MAIN_HAND, starter);
        var pos = helper.absolutePos(new BlockPos(1, 1, 1));
        helper.getLevel().setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
        for (int use = 1; use <= 3; use++) {
            helper.getLevel()
                    .setBlockAndUpdate(pos, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false));
            helper.assertTrue(
                    starter.useOn(context(player, pos)).consumesAction()
                            && helper.getLevel().getBlockState(pos).getValue(CampfireBlock.LIT),
                    "Firestarter must light the campfire on use " + use);
            helper.assertTrue(
                    use == 3 ? starter.isEmpty() : starter.getDamageValue() == use,
                    "Firestarter must spend one durability and break after its third use");
        }
        var fresh = new ItemStack(item("firestarter"));
        player.setItemInHand(InteractionHand.MAIN_HAND, fresh);
        helper.getLevel().setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(pos.above(), Blocks.STONE.defaultBlockState());
        helper.assertTrue(
                !fresh.useOn(context(player, pos)).consumesAction() && fresh.getDamageValue() == 0,
                "Blocked ignition must not spend durability");
        helper.succeed();
    }

    private static UseOnContext context(Player player, BlockPos pos) {
        return new UseOnContext(
                player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
    }
}
