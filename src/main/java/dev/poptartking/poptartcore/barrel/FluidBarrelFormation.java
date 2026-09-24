package dev.poptartking.poptartcore.barrel;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.fluids.FluidStack;

final class FluidBarrelFormation {
    private FluidBarrelFormation() {}

    static void onPlayerPlaced(ServerLevel level, BlockPos placedPos, Player player, InteractionHand hand,
            Item barrelItem) {
        for (FluidBarrelBuildPattern pattern : FluidBarrelBuildPattern.ORDERED) {
            PlanSelection selection = findPlan(level, placedPos, player, hand, pattern);
            if (selection.ambiguous()) return;
            if (selection.plan() != null) {
                BuildPlan plan = selection.plan();
                if (placeMissing(level, plan, placedPos, player, barrelItem))
                    commit(level, plan.origin(), plan.size());
                return;
            }
        }
        tryFormFromPlacement(level, placedPos);
    }

    private static PlanSelection findPlan(ServerLevel level, BlockPos placedPos, Player player,
            InteractionHand hand, FluidBarrelBuildPattern pattern) {
        BuildPlan found = null;
        for (int dx = 0; dx < pattern.size(); dx++) {
            for (int dz = 0; dz < pattern.size(); dz++) {
                BlockPos origin = placedPos.offset(-dx, -pattern.filledLayers(), -dz);
                BuildPlan candidate = inspectPlan(level, origin, placedPos, player, hand, pattern);
                if (candidate == null) continue;
                if (found != null) return new PlanSelection(null, true);
                found = candidate;
            }
        }
        return new PlanSelection(found, false);
    }

    private static BuildPlan inspectPlan(ServerLevel level, BlockPos origin, BlockPos placedPos, Player player,
            InteractionHand hand, FluidBarrelBuildPattern pattern) {
        BlockState barrelState = level.getBlockState(placedPos);
        if (!(level.getBlockEntity(placedPos) instanceof FluidBarrelBlockEntity placed)
                || placed.formationSize() != 1) return null;
        if (pattern.size() == 3 && pattern.filledLayers() == 1
                && hasBarrelLayer(level, origin.below(), barrelState)) return null;
        if (pattern.size() == 2 && hasAdjacentBarrel(level, origin, 2, barrelState)) return null;

        FluidStack fluid = placed.storedFluid();
        List<BlockPos> missing = new ArrayList<>(pattern.missingCount());
        for (int y = 0; y < pattern.size(); y++) {
            for (int x = 0; x < pattern.size(); x++) {
                for (int z = 0; z < pattern.size(); z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    if (pos.equals(placedPos)) continue;
                    if (y >= pattern.filledLayers()) {
                        if (!canFill(level, pos, barrelState, player, hand)) return null;
                        missing.add(pos);
                        continue;
                    }
                    if (!level.hasChunkAt(pos)
                            || !level.getBlockState(pos).is(barrelState.getBlock())
                            || !(level.getBlockEntity(pos) instanceof FluidBarrelBlockEntity member)
                            || member.formationSize() != 1) return null;
                    FluidStack stored = member.storedFluid();
                    if (!stored.isEmpty()) {
                        if (!fluid.isEmpty() && !FluidStack.isSameFluidSameComponents(fluid, stored)) return null;
                        fluid = stored;
                    }
                }
            }
        }
        return missing.size() == pattern.missingCount() ? new BuildPlan(origin, pattern.size(), List.copyOf(missing)) : null;
    }

    private static boolean placeMissing(ServerLevel level, BuildPlan plan, BlockPos placedPos, Player player,
            Item barrelItem) {
        if (!player.getAbilities().instabuild && itemCount(player, barrelItem) < plan.missing().size()) return false;

        List<BlockSnapshot> snapshots = new ArrayList<>(plan.missing().size());
        BlockState barrelState = level.getBlockState(placedPos);
        for (BlockPos pos : plan.missing()) {
            snapshots.add(BlockSnapshot.create(level.dimension(), level, pos));
            if (!level.setBlock(pos, barrelState, Block.UPDATE_ALL)) {
                restore(snapshots);
                return false;
            }
        }
        if (EventHooks.onMultiBlockPlace(player, snapshots, Direction.UP)) {
            restore(snapshots);
            return false;
        }
        if (!canForm(level, plan.origin(), plan.size())) {
            restore(snapshots);
            return false;
        }
        if (!player.getAbilities().instabuild) consumeItems(player, barrelItem, plan.missing().size());
        return true;
    }

    private static boolean hasBarrelLayer(Level level, BlockPos origin, BlockState barrelState) {
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                BlockPos pos = origin.offset(x, 0, z);
                if (!level.hasChunkAt(pos) || !level.getBlockState(pos).is(barrelState.getBlock())) return false;
            }
        }
        return true;
    }

    private static boolean hasAdjacentBarrel(Level level, BlockPos origin, int size, BlockState barrelState) {
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                BlockPos bottom = origin.offset(x, 0, z);
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    BlockPos neighbor = bottom.relative(direction);
                    if (neighbor.getX() >= origin.getX() && neighbor.getX() < origin.getX() + size
                            && neighbor.getZ() >= origin.getZ() && neighbor.getZ() < origin.getZ() + size) continue;
                    if (level.hasChunkAt(neighbor) && level.getBlockState(neighbor).is(barrelState.getBlock()))
                        return true;
                }
            }
        }
        return false;
    }

    private static boolean canFill(ServerLevel level, BlockPos pos, BlockState barrelState, Player player,
            InteractionHand hand) {
        return level.hasChunkAt(pos) && level.getBlockState(pos).isAir()
                && level.isInWorldBounds(pos)
                && level.getWorldBorder().isWithinBounds(pos)
                && player.mayInteract(level, pos)
                && player.mayUseItemAt(pos, Direction.UP, player.getItemInHand(hand))
                && level.isUnobstructed(barrelState, pos, CollisionContext.of(player));
    }

    private static int itemCount(Player player, Item item) {
        int count = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(item)) count += stack.getCount();
        }
        return count;
    }

    private static void consumeItems(Player player, Item item, int amount) {
        for (int slot = 0; slot < player.getInventory().getContainerSize() && amount > 0; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.is(item)) continue;
            int taken = Math.min(amount, stack.getCount());
            stack.shrink(taken);
            amount -= taken;
        }
        player.getInventory().setChanged();
    }

    private static void restore(List<BlockSnapshot> snapshots) {
        for (int i = snapshots.size() - 1; i >= 0; i--) snapshots.get(i).restore();
    }

    static void onTick(ServerLevel level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof FluidBarrelBlockEntity barrel)) return;
        if (barrel.formationSize() > 1) {
            if (!regionLoaded(level, barrel.formationOrigin(), barrel.formationSize())) return;
            if (intact(level, barrel.formationOrigin(), barrel.formationSize())) return;
            dissolve(level, barrel.formationOrigin(), barrel.formationSize());
        }

        BlockState state = level.getBlockState(pos);
        if (state.hasProperty(FluidBarrelBlock.PART)
                && state.getValue(FluidBarrelBlock.PART) != FluidBarrelBlock.Part.SINGLE)
            level.setBlock(pos, state.setValue(FluidBarrelBlock.PART, FluidBarrelBlock.Part.SINGLE), 3);
    }

    static void tryFormFromPlacement(ServerLevel level, BlockPos pos) {
        for (int size = 3; size >= 2; size--) {
            for (int dx = 0; dx < size; dx++) {
                for (int dy = 0; dy < size; dy++) {
                    for (int dz = 0; dz < size; dz++) {
                        BlockPos origin = pos.offset(-dx, -dy, -dz);
                        if (canForm(level, origin, size)) {
                            commit(level, origin, size);
                            return;
                        }
                    }
                }
            }
        }
    }

    static void onRemoved(Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof FluidBarrelBlockEntity removed)
                || removed.formationSize() == 1) return;
        BlockPos origin = removed.formationOrigin();
        int size = removed.formationSize();
        for (BlockPos memberPos : positions(origin, size)) {
            if (memberPos.equals(pos) || !level.hasChunkAt(memberPos)) continue;
            if (level.getBlockEntity(memberPos) instanceof FluidBarrelBlockEntity member
                    && member.formationSize() == size
                    && member.formationOrigin().equals(origin)) {
                setFormation(level, memberPos, member, memberPos, 1, FluidBarrelBlock.Part.SINGLE);
                level.scheduleTick(memberPos, level.getBlockState(memberPos).getBlock(), 1);
            }
        }
    }

    private static boolean regionLoaded(Level level, BlockPos origin, int size) {
        for (BlockPos pos : positions(origin, size)) {
            if (!level.hasChunkAt(pos)) return false;
        }
        return true;
    }

    private static boolean intact(Level level, BlockPos origin, int size) {
        for (BlockPos pos : positions(origin, size)) {
            if (!(level.getBlockEntity(pos) instanceof FluidBarrelBlockEntity member)
                    || member.formationSize() != size
                    || !member.formationOrigin().equals(origin)
                    || !level.getBlockState(pos).hasProperty(FluidBarrelBlock.PART)
                    || level.getBlockState(pos).getValue(FluidBarrelBlock.PART) != expectedPart(pos, origin, size))
                return false;
        }
        return true;
    }

    private static boolean canForm(Level level, BlockPos origin, int size) {
        BlockState barrelState = level.getBlockState(origin);
        if (size == 2 && hasAdjacentBarrel(level, origin, size, barrelState)) return false;
        if (size == 3 && hasBarrelLayer(level, origin.below(), barrelState)) return false;
        FluidStack existing = FluidStack.EMPTY;
        for (BlockPos pos : positions(origin, size)) {
            if (!level.hasChunkAt(pos)
                    || !level.getBlockState(pos).hasProperty(FluidBarrelBlock.PART)
                    || !(level.getBlockEntity(pos) instanceof FluidBarrelBlockEntity member)) return false;
            if (!contained(member.formationOrigin(), member.formationSize(), origin, size)) return false;

            FluidStack fluid = member.storedFluid();
            if (!fluid.isEmpty()) {
                if (!existing.isEmpty() && !FluidStack.isSameFluidSameComponents(existing, fluid)) return false;
                existing = fluid;
            }
        }
        return true;
    }

    private static boolean contained(BlockPos oldOrigin, int oldSize, BlockPos newOrigin, int newSize) {
        return oldOrigin.getX() >= newOrigin.getX() && oldOrigin.getX() + oldSize <= newOrigin.getX() + newSize
                && oldOrigin.getY() >= newOrigin.getY() && oldOrigin.getY() + oldSize <= newOrigin.getY() + newSize
                && oldOrigin.getZ() >= newOrigin.getZ() && oldOrigin.getZ() + oldSize <= newOrigin.getZ() + newSize;
    }

    private static void commit(Level level, BlockPos origin, int size) {
        for (BlockPos pos : positions(origin, size)) {
            FluidBarrelBlockEntity member = (FluidBarrelBlockEntity) level.getBlockEntity(pos);
            setFormation(level, pos, member, origin, size, expectedPart(pos, origin, size));
        }
    }

    private static FluidBarrelBlock.Part expectedPart(BlockPos pos, BlockPos origin, int size) {
        int anchor = (size - 1) / 2;
        if (!pos.equals(origin.offset(anchor, anchor, anchor))) return FluidBarrelBlock.Part.HIDDEN;
        return size == 3 ? FluidBarrelBlock.Part.GIANT : FluidBarrelBlock.Part.BIG;
    }

    private static void dissolve(Level level, BlockPos origin, int size) {
        for (BlockPos pos : positions(origin, size)) {
            if (!level.hasChunkAt(pos)) continue;
            if (level.getBlockEntity(pos) instanceof FluidBarrelBlockEntity member
                    && member.formationSize() == size
                    && member.formationOrigin().equals(origin)) {
                setFormation(level, pos, member, pos, 1, FluidBarrelBlock.Part.SINGLE);
                level.scheduleTick(pos, level.getBlockState(pos).getBlock(), 1);
            }
        }
    }

    private static void setFormation(Level level, BlockPos pos, FluidBarrelBlockEntity member, BlockPos origin,
            int size, FluidBarrelBlock.Part part) {
        member.setFormation(origin, size);
        BlockState state = level.getBlockState(pos);
        if (state.getValue(FluidBarrelBlock.PART) != part)
            level.setBlock(pos, state.setValue(FluidBarrelBlock.PART, part), 3);
        BlockState updated = level.getBlockState(pos);
        level.sendBlockUpdated(pos, updated, updated, 3);
    }

    private static List<BlockPos> positions(BlockPos origin, int size) {
        List<BlockPos> result = new ArrayList<>(size * size * size);
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) result.add(origin.offset(x, y, z));
            }
        }
        return result;
    }

    private record BuildPlan(BlockPos origin, int size, List<BlockPos> missing) {}

    private record PlanSelection(BuildPlan plan, boolean ambiguous) {}
}
