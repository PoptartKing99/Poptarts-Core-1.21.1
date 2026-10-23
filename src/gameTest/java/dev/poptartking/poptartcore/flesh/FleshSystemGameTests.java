package dev.poptartking.poptartcore.flesh;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import dev.poptartking.poptartcore.registry.PoptartMaterialItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FleshSystemGameTests {
    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void oldBlockStatesAndItemsMigrateToLivingFlesh(GameTestHelper helper) {
        var old = PoptartCore.location("fresh_flesh");
        helper.assertTrue(
                BuiltInRegistries.BLOCK.get(old) == PoptartCoreBlocks.LIVING_FLESH.get(),
                "Old block registry name aliases living flesh");
        var state = new CompoundTag();
        state.putString("Name", old.toString());
        helper.assertTrue(
                NbtUtils.readBlockState(helper.getLevel().registryAccess().lookupOrThrow(Registries.BLOCK), state)
                        .is(PoptartCoreBlocks.LIVING_FLESH.get()),
                "Saved chunk block state migrates");
        var item = new CompoundTag();
        item.putString("id", old.toString());
        item.putInt("count", 12);
        var loaded = ItemStack.parseOptional(helper.getLevel().registryAccess(), item);
        helper.assertTrue(
                loaded.is(PoptartCoreBlocks.LIVING_FLESH.item().get()) && loaded.getCount() == 12,
                "Old inventory stacks migrate without losing count");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void connectedFleshIsSustainedAndDisconnectedFleshDecays(GameTestHelper helper) {
        var level = helper.getLevel();
        var bud = helper.absolutePos(new BlockPos(2, 3, 2));
        var flesh = bud.below();
        var diagonal = flesh.offset(1, -1, 1);
        var detached = flesh.offset(4, 0, 0);
        level.setBlockAndUpdate(flesh, PoptartCoreBlocks.LIVING_FLESH.get().defaultBlockState());
        level.setBlockAndUpdate(diagonal, PoptartCoreBlocks.LIVING_FLESH.get().defaultBlockState());
        level.setBlockAndUpdate(detached, PoptartCoreBlocks.LIVING_FLESH.get().defaultBlockState());
        level.setBlockAndUpdate(bud, PoptartCoreBlocks.LIFEBUD.get().defaultBlockState());
        var organism = FleshOrganisms.of(level, bud);
        helper.assertTrue(
                organism != null && organism.contains(diagonal) && !organism.contains(detached),
                "Diagonal connections count; nearby detached flesh does not");
        var random = RandomSource.create(42);
        for (int tick = 0; tick < 200; tick++) {
            PoptartCoreBlocks.LIVING_FLESH.get().randomTick(level.getBlockState(flesh), level, flesh, random);
            if (level.getBlockState(detached).is(PoptartCoreBlocks.LIVING_FLESH.get()))
                PoptartCoreBlocks.LIVING_FLESH.get().randomTick(level.getBlockState(detached), level, detached, random);
        }
        helper.assertTrue(
                level.getBlockState(flesh).is(PoptartCoreBlocks.LIVING_FLESH.get())
                        && level.getBlockState(detached).is(PoptartCoreBlocks.FLESH.get()),
                "Only connected flesh is preserved");
        level.setBlockAndUpdate(bud, Blocks.AIR.defaultBlockState());
        FleshOrganisms.changed(level, bud);
        helper.assertTrue(!FleshOrganisms.sustains(level, flesh), "Removing bud removes organism support");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void waterloggedTendrilsRetintAndLifebudsGrowThem(GameTestHelper helper) {
        var level = helper.getLevel();
        var bud = helper.absolutePos(new BlockPos(4, 3, 4));
        var support = bud.below();
        level.setBlockAndUpdate(support, PoptartCoreBlocks.LIVING_FLESH.get().defaultBlockState());
        level.setBlockAndUpdate(
                bud,
                PoptartCoreBlocks.LIFEBUD.get().defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true));
        var side = support.east();
        level.setBlockAndUpdate(side, Blocks.WATER.defaultBlockState());
        helper.assertTrue(FleshGrowth.sprout(level, side, RandomSource.create(1), 3), "Tendrils sprout in water");
        helper.assertTrue(
                level.getBlockState(side).is(PoptartCoreBlocks.LIVING_FLESH_TENDRILS.get())
                        && level.getBlockState(side).getValue(BlockStateProperties.WATERLOGGED),
                "Living tendrils preserve water");
        level.setBlockAndUpdate(support, PoptartCoreBlocks.ROTTEN_FLESH.get().defaultBlockState());
        FleshGrowth.retintAround(level, support);
        helper.assertTrue(
                level.getBlockState(side).is(PoptartCoreBlocks.ROTTEN_FLESH_TENDRILS.get())
                        && level.getBlockState(side).getValue(BlockStateProperties.WATERLOGGED),
                "Retint retains faces and water");
        level.setBlockAndUpdate(side, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(support, PoptartCoreBlocks.LIVING_FLESH.get().defaultBlockState());
        FleshOrganisms.changed(level, bud);
        var random = RandomSource.create(99);
        for (int tick = 0; tick < 300; tick++)
            PoptartCoreBlocks.LIFEBUD.get().randomTick(level.getBlockState(bud), level, bud, random);
        boolean sprouted = false;
        for (var direction : Direction.values())
            sprouted |= level.getBlockState(support.relative(direction)).getBlock() instanceof FleshTendrilsBlock;
        helper.assertTrue(sprouted, "Lifebud random ticks grow tendrils passively");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void ulcersConsumeFoodAndRevivalWitheringWavesWork(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(3, 2, 3));
        level.setBlockAndUpdate(
                pos.below(), PoptartCoreBlocks.LIVING_FLESH.get().defaultBlockState());
        level.setBlockAndUpdate(pos, PoptartCoreBlocks.ULCER.get().defaultBlockState());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BEEF, 3));
        PoptartCoreBlocks.ULCER
                .get()
                .useItemOn(
                        player.getMainHandItem(),
                        level.getBlockState(pos),
                        level,
                        pos,
                        player,
                        InteractionHand.MAIN_HAND,
                        new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        helper.assertTrue(
                player.getMainHandItem().getCount() == 2, "Native ulcer interaction consumes exactly one food");
        var target = pos.east();
        level.setBlockAndUpdate(target, PoptartCoreBlocks.ROTTEN_FLESH.get().defaultBlockState());
        FleshWaves.start(level, pos, FleshWaves.Kind.REVIVE);
        for (int tick = 0; tick < 100; tick++) FleshWaves.onLevelTick(new LevelTickEvent.Post(() -> true, level));
        helper.assertTrue(
                level.getBlockState(target).is(PoptartCoreBlocks.FLESH.get()), "Revival wave restores rotten flesh");
        FleshWaves.start(level, pos, FleshWaves.Kind.WITHER);
        for (int tick = 0; tick < 100; tick++) FleshWaves.onLevelTick(new LevelTickEvent.Post(() -> true, level));
        helper.assertTrue(
                level.getBlockState(target).is(PoptartCoreBlocks.ROTTEN_FLESH.get()), "Withering wave rots flesh");
        helper.assertTrue(
                FleshGrowth.isUlcerFood(new ItemStack(PoptartMaterialItems.LIFESALT.get())), "Lifesalt recognized");
        helper.assertTrue(
                !Blocks.STONE
                        .defaultBlockState()
                        .is(dev.poptartking.poptartcore.registry.PoptartCoreTags.SUPPORTS_FLESH_GROWTH),
                "Terrain growth disabled by requested empty eligibility tag");
        helper.succeed();
    }
}
