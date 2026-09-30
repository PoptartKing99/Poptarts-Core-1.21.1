package dev.poptartking.poptartcore.treetap;

import dev.poptartking.poptartcore.registry.PoptartCoreFluids;
import dev.poptartking.poptartcore.registry.PoptartCoreItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

/** One latex bottle contains one fixed, indivisible serving of fluid. */
public final class LatexBottleFluidHandler implements IFluidHandlerItem {
    public static final int AMOUNT = 200;

    private ItemStack container;

    public LatexBottleFluidHandler(ItemStack container) {
        this.container = container;
    }

    @Override
    public ItemStack getContainer() {
        return container;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return tank == 0 && container.is(PoptartCoreItems.LATEX_BOTTLE.get())
                ? new FluidStack(PoptartCoreFluids.LATEX.source().get(), AMOUNT)
                : FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        return tank == 0 ? AMOUNT : 0;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return tank == 0 && stack.is(PoptartCoreFluids.LATEX.source().get());
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return 0;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return resource.is(PoptartCoreFluids.LATEX.source().get())
                ? drain(resource.getAmount(), action)
                : FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (maxDrain < AMOUNT || container.getCount() != 1
                || !container.is(PoptartCoreItems.LATEX_BOTTLE.get())) {
            return FluidStack.EMPTY;
        }
        FluidStack extracted = new FluidStack(PoptartCoreFluids.LATEX.source().get(), AMOUNT);
        if (action.execute()) container = new ItemStack(Items.GLASS_BOTTLE);
        return extracted;
    }
}
