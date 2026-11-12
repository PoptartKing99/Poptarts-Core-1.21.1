package dev.poptartking.poptartcore.fruit;

import com.mojang.serialization.MapCodec;
import dev.poptartking.poptartcore.registry.PoptartFruitBlocks;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public final class LemonFruitDecorator extends TreeDecorator {
    public static final MapCodec<LemonFruitDecorator> CODEC = MapCodec.unit(LemonFruitDecorator::new);

    private static final int MAX_FRUITING_SITES = 8;
    private static final int MAX_BLOSSOM_SITES = 2;

    @Override
    protected TreeDecoratorType<?> type() {
        return PoptartFruitBlocks.LEMON_FRUIT_DECORATOR.get();
    }

    @Override
    public void place(Context context) {
        var leaves = PoptartFruitBlocks.LEMON_LEAVES.get();
        var flowering = PoptartFruitBlocks.FLOWERING_LEMON_LEAVES.get().defaultBlockState();
        var youngFruit = PoptartFruitBlocks.LEMON_FRUIT.get().defaultBlockState();
        var candidates = Util.shuffledCopy(context.leaves(), context.random());
        candidates.removeIf(pos -> !context.level()
                .isStateAtPosition(pos, state -> state.is(leaves) && !state.getValue(LeavesBlock.WATERLOGGED)));
        List<BlockPos> hangingSites = candidates.stream()
                .filter(pos -> context.isAir(pos.below()))
                .sorted(Comparator.comparingInt(BlockPos::getY))
                .toList();
        List<BlockPos> fruitingSites = new ArrayList<>(MAX_FRUITING_SITES);
        if (!hangingSites.isEmpty()) {
            // Reserve a reachable lemon and use a higher overhang when it has enough room.
            BlockPos lowest = hangingSites.getFirst();
            fruitingSites.add(lowest);
            hangingSites.reversed().stream()
                    .filter(pos -> pos.getY() > lowest.getY() && !sameCanopyPatch(lowest, pos))
                    .findFirst()
                    .ifPresent(fruitingSites::add);
            fillFruitingSites(hangingSites, fruitingSites, false);
            fillFruitingSites(hangingSites, fruitingSites, true);
        }

        for (BlockPos pos : fruitingSites) {
            context.setBlock(pos, flowering);
            context.setBlock(pos.below(), youngFruit);
        }

        int blossomSites = 0;
        for (BlockPos pos : candidates) {
            boolean blockedByTree = context.level()
                    .isStateAtPosition(
                            pos.below(),
                            state -> state.is(leaves) || state.is(flowering.getBlock()) || state.is(BlockTags.LOGS));
            if (blockedByTree
                    && Direction.Plane.HORIZONTAL.stream().anyMatch(side -> context.isAir(pos.relative(side)))) {
                context.setBlock(pos, flowering);
                if (++blossomSites == MAX_BLOSSOM_SITES) break;
            }
        }
    }

    private static void fillFruitingSites(
            List<BlockPos> candidates, List<BlockPos> selected, boolean allowVerticalSpacing) {
        for (BlockPos pos : candidates) {
            if (selected.size() == MAX_FRUITING_SITES) return;
            boolean tooClose = selected.stream()
                    .anyMatch(existing -> sameCanopyPatch(existing, pos)
                            && (!allowVerticalSpacing || Math.abs(existing.getY() - pos.getY()) <= 1));
            if (!tooClose) selected.add(pos);
        }
    }

    private static boolean sameCanopyPatch(BlockPos first, BlockPos second) {
        return Math.abs(first.getX() - second.getX()) <= 1 && Math.abs(first.getZ() - second.getZ()) <= 1;
    }
}
