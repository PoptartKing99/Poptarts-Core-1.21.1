package dev.poptartking.poptartcore.waxgolem;

import java.util.Iterator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.state.BlockState;

/* JADX INFO: loaded from: wayfarer_core-0.1.0.jar:dev/tazer/wayfarer/waxgolem/DepositSites.class */
public final class DepositSites {
    private DepositSites() {}

    public static BlockPos best(WaxGolem golem, ItemStack result, int range) {
        int score;
        Level level = golem.level();
        BlockPos origin = golem.blockPosition();
        BlockPos best = null;
        int bestScore = Integer.MIN_VALUE;
        boolean honey = result.is(Items.HONEY_BOTTLE);
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-range, -4, -range), origin.offset(range, 4, range))) {
            if (honey
                    && !pos.equals(golem.drawnFrom())
                    && HoneyCauldrons.acceptsBottle(level, pos)
                    && !HoneyCauldrons.fedByHiveTap(level, pos)) {
                int score2 = (40 - ((int) Math.sqrt(pos.distSqr(origin))))
                        + (HoneyCauldrons.isHoney(level.getBlockState(pos)) ? 10 : 0);
                if (score2 > bestScore) {
                    bestScore = score2;
                    best = pos.immutable();
                }
            } else {
                Container container = ContainerAccess.container(level, pos);
                if (container != null
                        && accepts(container, result)
                        && ContainerAccess.reachable(golem, pos)
                        && (score = score(level, pos, origin, container, result)) > bestScore) {
                    bestScore = score;
                    best = pos.immutable();
                }
            }
        }
        return best;
    }

    public static BlockPos bestCauldron(WaxGolem golem, BlockPos exclude) {
        Level level = golem.level();
        BlockPos origin = golem.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-16, -4, -16), origin.offset(16, 4, 16))) {
            if (!pos.equals(exclude)
                    && HoneyCauldrons.acceptsBottle(level, pos)
                    && !HoneyCauldrons.fedByHiveTap(level, pos)) {
                return pos.immutable();
            }
        }
        return null;
    }

    private static boolean accepts(Container container, ItemStack result) {
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack existing = container.getItem(slot);
            if (!existing.isEmpty()) {
                if (ItemStack.isSameItemSameComponents(existing, result)
                        && existing.getCount() < existing.getMaxStackSize()) {
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }

    private static int score(Level level, BlockPos pos, BlockPos origin, Container container, ItemStack result) {
        int score = 32 - ((int) Math.sqrt(pos.distSqr(origin)));
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            if (ItemStack.isSameItem(container.getItem(slot), result)) {
                score += 8;
                break;
            }
        }
        Iterator it = BlockPos.betweenClosed(pos.offset(-4, -2, -4), pos.offset(4, 2, 4))
                .iterator();
        while (it.hasNext()) {
            BlockState state = level.getBlockState((BlockPos) it.next());
            if (state.getBlock() instanceof BeehiveBlock) {
                score += 6;
                break;
            }
        }
        for (BlockPos near : BlockPos.betweenClosed(pos.offset(-3, -1, -3), pos.offset(3, 1, 3))) {
            if (!near.equals(pos) && ContainerAccess.isContainer(level, near)) {
                score += 2;
            }
        }
        return score;
    }

    public static boolean insert(WaxGolem golem, BlockPos pos, ItemStack result) {
        if (result.is(Items.HONEY_BOTTLE)
                && HoneyCauldrons.acceptsBottle(golem.level(), pos)
                && HoneyCauldrons.pourBottle(golem.level(), pos)) {
            result.shrink(1);
            golem.giveBack(new ItemStack(Items.GLASS_BOTTLE));
            return true;
        }
        Container container = ContainerAccess.container(golem.level(), pos);
        if (container == null) {
            return false;
        }
        boolean moved = false;
        for (int slot = 0; slot < container.getContainerSize() && !result.isEmpty(); slot++) {
            ItemStack existing = container.getItem(slot);
            if (existing.isEmpty()) {
                container.setItem(slot, result.copy());
                result.setCount(0);
                moved = true;
            } else if (ItemStack.isSameItemSameComponents(existing, result)) {
                int room = existing.getMaxStackSize() - existing.getCount();
                int moving = Math.min(room, result.getCount());
                if (moving > 0) {
                    existing.grow(moving);
                    result.shrink(moving);
                    moved = true;
                }
            }
        }
        if (moved) {
            container.setChanged();
        }
        return moved;
    }
}
