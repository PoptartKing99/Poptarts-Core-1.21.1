package dev.poptartking.poptartcore.blastfurnace;

import dev.poptartking.poptartcore.blastfurnace.menu.BlastFurnaceMenu;
import dev.poptartking.poptartcore.crucible.CrucibleBlockEntity;
import dev.poptartking.poptartcore.crucible.alloying.AlloyingRecipe;
import dev.poptartking.poptartcore.crucible.alloying.AlloyingRecipeInput;
import dev.poptartking.poptartcore.crucible.melting.MeltingRecipe;
import dev.poptartking.poptartcore.crucible.melting.MeltingRecipeInput;
import dev.poptartking.poptartcore.registry.PoptartCoreBlockEntities;
import dev.poptartking.poptartcore.registry.PoptartCoreRecipes;
import dev.poptartking.poptartcore.registry.PoptartCoreTags;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class BlastFurnaceBlockEntity extends CrucibleBlockEntity implements WorldlyContainer {
    public static final int INPUT_COUNT = 6;
    public static final int TANK_CAPACITY = 3000;

    public BlastFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(PoptartCoreBlockEntities.BLAST_FURNACE.get(), pos, state, INPUT_COUNT, TANK_CAPACITY);
    }

    @Override
    protected Optional<RecipeHolder<MeltingRecipe>> findMelting(MeltingRecipeInput input, Level level) {
        Optional<RecipeHolder<MeltingRecipe>> blast = level.getRecipeManager()
                .getRecipeFor(PoptartCoreRecipes.BLAST_FURNACE_MELTING_TYPE.get(), input, level);
        return blast.isPresent() ? blast : super.findMelting(input, level);
    }

    @Override
    protected Optional<RecipeHolder<AlloyingRecipe>> findAlloying(AlloyingRecipeInput input, Level level) {
        Optional<RecipeHolder<AlloyingRecipe>> blast = level.getRecipeManager()
                .getRecipeFor(PoptartCoreRecipes.BLAST_FURNACE_ALLOYING_TYPE.get(), input, level);
        return blast.isPresent() ? blast : super.findAlloying(input, level);
    }

    @Override
    protected boolean isBlastFurnace() {
        return true;
    }

    @Override
    protected int adjustCookTime(int base) {
        return Math.max(1, base / 2);
    }

    @Override
    protected int limitAlloyingBatches(AlloyingRecipe recipe, int availableBatches) {
        return availableBatches;
    }

    @Override
    protected int fuelSpeedMultiplier(ItemStack fuel) {
        return fuel.is(PoptartCoreTags.BLAST_FURNACE_EFFICIENT) ? 2 : 1;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == resultSlot) return false;
        if (slot == fuelSlot) return stack.is(PoptartCoreTags.BLAST_FURNACE_ALLOWED);
        return slot == containerSlot
                ? dev.poptartking.poptartcore.registry.PoptartCoreItems.isMould(stack)
                : slot < inputCount;
    }

    private int[] inputSlots() {
        int[] slots = new int[inputCount];
        for (int slot = 0; slot < inputCount; slot++) {
            slots[slot] = slot;
        }
        return slots;
    }

    private Direction facing() {
        return getBlockState().getValue(BlastFurnaceBlock.FACING);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.DOWN) return new int[] {resultSlot, containerSlot};
        if (side == facing().getClockWise()) return new int[] {fuelSlot, containerSlot};
        if (side == facing().getCounterClockWise()) return new int[] {containerSlot};
        if (side != facing().getOpposite()) return new int[] {containerSlot};

        int[] slots = new int[inputCount + 1];
        System.arraycopy(inputSlots(), 0, slots, 0, inputCount);
        slots[inputCount] = containerSlot;
        return slots;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
        for (int exposed : getSlotsForFace(side)) if (exposed == slot) return canPlaceItem(slot, stack);
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot == resultSlot && side == Direction.DOWN;
    }

    public WorldlyContainer topHalf() {
        return new TopHalf(this);
    }

    @Override
    protected boolean updateBlockState(Level level, BlockPos pos, boolean isLit) {
        BlockState lower = level.getBlockState(pos);
        boolean changed = lower.getValue(BlastFurnaceBlock.LIT) != isLit;
        if (changed) level.setBlock(pos, lower.setValue(BlastFurnaceBlock.LIT, isLit), 3);
        BlockState upper = level.getBlockState(pos.above());
        if (upper.is(lower.getBlock()) && upper.getValue(BlastFurnaceBlock.LIT) != isLit) {
            level.setBlock(pos.above(), upper.setValue(BlastFurnaceBlock.LIT, isLit), 3);
            changed = true;
        }
        return changed;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.poptartcore.blast_furnace");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new BlastFurnaceMenu(id, inventory, this, dataAccess);
    }

    private record TopHalf(BlastFurnaceBlockEntity furnace) implements WorldlyContainer {
        @Override
        public int[] getSlotsForFace(Direction side) {
            if (side == Direction.UP) return new int[] {furnace.containerSlot};

            int[] slots = new int[furnace.inputCount + 1];
            System.arraycopy(furnace.inputSlots(), 0, slots, 0, furnace.inputCount);
            slots[furnace.inputCount] = furnace.containerSlot;
            return slots;
        }

        @Override
        public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
            return slot == furnace.containerSlot
                    ? furnace.canPlaceItem(slot, stack)
                    : side != Direction.UP && slot < furnace.inputCount && furnace.canPlaceItem(slot, stack);
        }

        @Override
        public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
            return false;
        }

        @Override
        public int getContainerSize() {
            return furnace.getContainerSize();
        }

        @Override
        public boolean isEmpty() {
            return furnace.isEmpty();
        }

        @Override
        public ItemStack getItem(int slot) {
            return furnace.getItem(slot);
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            return furnace.removeItem(slot, amount);
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return furnace.removeItemNoUpdate(slot);
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            furnace.setItem(slot, stack);
        }

        @Override
        public int getMaxStackSize() {
            return furnace.getMaxStackSize();
        }

        @Override
        public void setChanged() {
            furnace.setChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            return furnace.stillValid(player);
        }

        @Override
        public void clearContent() {
            furnace.clearContent();
        }
    }
}
