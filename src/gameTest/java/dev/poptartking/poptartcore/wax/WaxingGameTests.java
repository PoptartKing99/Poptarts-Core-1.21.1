package dev.poptartking.poptartcore.wax;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartMaterialItems;
import net.mehvahdjukaar.moonlight.api.block.IWaxable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WaxingGameTests {
    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void waxReplacesHoneycombForEveryCopperAge(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(1, 1, 1));
        Block[] bare = {Blocks.COPPER_BLOCK, Blocks.EXPOSED_COPPER, Blocks.WEATHERED_COPPER, Blocks.OXIDIZED_COPPER};
        Block[] waxed = {
            Blocks.WAXED_COPPER_BLOCK,
            Blocks.WAXED_EXPOSED_COPPER,
            Blocks.WAXED_WEATHERED_COPPER,
            Blocks.WAXED_OXIDIZED_COPPER
        };
        for (int i = 0; i < bare.length; i++) {
            level.setBlockAndUpdate(pos, bare[i].defaultBlockState());
            var honeycomb = new ItemStack(Items.HONEYCOMB, 2);
            player.setItemInHand(InteractionHand.MAIN_HAND, honeycomb);
            helper.assertTrue(
                    !honeycomb.useOn(context(player, pos)).consumesAction()
                            && level.getBlockState(pos).is(bare[i])
                            && honeycomb.getCount() == 2,
                    "Honeycomb must not wax or be consumed at copper age " + i);

            var wax = new ItemStack(PoptartMaterialItems.WAX.get(), 2);
            player.setItemInHand(InteractionHand.MAIN_HAND, wax);
            helper.assertTrue(
                    wax.useOn(context(player, pos)).consumesAction()
                            && level.getBlockState(pos).is(waxed[i])
                            && wax.getCount() == 1,
                    "Wax must coat copper and consume one item at age " + i);
            helper.assertTrue(
                    !wax.useOn(context(player, pos)).consumesAction() && wax.getCount() == 1,
                    "Already waxed copper must not consume another item");
            level.setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
            helper.assertTrue(
                    !wax.useOn(context(player, pos)).consumesAction()
                            && level.getBlockState(pos).is(Blocks.STONE)
                            && wax.getCount() == 1,
                    "Unsupported blocks must remain unchanged without consuming wax");
        }
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void moonlightWaxingUsesWaxAndHonorsCreativeConsumption(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(1, 1, 1));
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var target = new WaxableTarget();
        for (var item : new net.minecraft.world.item.Item[] {Items.HONEYCOMB, Items.DIRT}) {
            var stack = new ItemStack(item, 2);
            helper.assertTrue(
                    target.tryWaxingWithItem(level, pos, player, stack)
                                    == ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
                            && !target.isWaxed()
                            && stack.getCount() == 2,
                    "Moonlight must reject honeycomb and unrelated items without consuming them");
        }
        var wax = new ItemStack(PoptartMaterialItems.WAX.get(), 2);
        helper.assertTrue(
                target.tryWaxingWithItem(level, pos, player, wax).consumesAction()
                        && target.isWaxed()
                        && wax.getCount() == 1,
                "The actual Moonlight handler must accept Wax and consume exactly one");
        helper.assertTrue(
                target.tryWaxingWithItem(level, pos, player, wax) == ItemInteractionResult.FAIL && wax.getCount() == 1,
                "Already waxed targets must reject repeat waxing without consuming Wax");
        var creative = helper.makeMockPlayer(GameType.CREATIVE);
        // GameTest's mock only overrides isCreative; item consumption uses the actual abilities.
        GameType.CREATIVE.updatePlayerAbilities(creative.getAbilities());
        var creativeWax = new ItemStack(PoptartMaterialItems.WAX.get(), 2);
        var creativeTarget = new WaxableTarget();
        helper.assertTrue(
                creativeTarget
                                .tryWaxingWithItem(level, pos, creative, creativeWax)
                                .consumesAction()
                        && creativeTarget.isWaxed()
                        && creativeWax.getCount() == 2,
                "Moonlight waxing in creative must preserve the held stack");
        helper.succeed();
    }

    private static UseOnContext context(Player player, BlockPos pos) {
        return new UseOnContext(
                player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
    }

    // Only the target's stored flag is test-owned. The default waxing method comes from Moonlight.
    private static final class WaxableTarget implements IWaxable {
        private boolean waxed;

        @Override
        public boolean isWaxed() {
            return waxed;
        }

        @Override
        public void setWaxed(boolean waxed) {
            this.waxed = waxed;
        }
    }
}
