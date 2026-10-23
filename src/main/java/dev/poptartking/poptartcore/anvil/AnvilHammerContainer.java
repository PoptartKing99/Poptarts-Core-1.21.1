package dev.poptartking.poptartcore.anvil;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Resolve the current block entity because damage/repair replaces the anvil block. */
final class AnvilHammerContainer implements Container {
    private final Level level;
    private final BlockPos pos;

    AnvilHammerContainer(Level level, BlockPos pos) {
        this.level = level;
        this.pos = pos.immutable();
    }

    private AnvilBlockEntity anvil() {
        return level.getBlockEntity(pos) instanceof AnvilBlockEntity anvil ? anvil : null;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return getItem(0).isEmpty();
    }

    @Override
    public ItemStack getItem(int index) {
        var anvil = anvil();
        return anvil == null ? ItemStack.EMPTY : anvil.hammer();
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        var anvil = anvil();
        return anvil == null ? ItemStack.EMPTY : anvil.removeItem(0, count);
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        var anvil = anvil();
        return anvil == null ? ItemStack.EMPTY : anvil.removeItemNoUpdate(0);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        var anvil = anvil();
        if (anvil != null) anvil.setHammer(stack);
    }

    @Override
    public void setChanged() {
        var anvil = anvil();
        if (anvil != null) anvil.setHammer(anvil.hammer());
    }

    @Override
    public boolean stillValid(Player player) {
        var anvil = anvil();
        return anvil != null && anvil.stillValid(player);
    }

    @Override
    public void clearContent() {
        setItem(0, ItemStack.EMPTY);
    }
}
