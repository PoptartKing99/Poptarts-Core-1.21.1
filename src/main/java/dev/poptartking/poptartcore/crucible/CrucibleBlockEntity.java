package dev.poptartking.poptartcore.crucible;

import dev.poptartking.poptartcore.PoptartCoreConfig;
import dev.poptartking.poptartcore.crucible.alloying.AlloyingRecipe;
import dev.poptartking.poptartcore.crucible.alloying.AlloyingRecipeInput;
import dev.poptartking.poptartcore.crucible.alloying.CrucibleIngredient;
import dev.poptartking.poptartcore.crucible.casting.CastingRecipe;
import dev.poptartking.poptartcore.crucible.casting.CastingRecipeInput;
import dev.poptartking.poptartcore.crucible.melting.MeltingRecipe;
import dev.poptartking.poptartcore.crucible.melting.MeltingRecipeInput;
import dev.poptartking.poptartcore.crucible.menu.CrucibleMenu;
import dev.poptartking.poptartcore.registry.PoptartCoreBlockEntities;
import dev.poptartking.poptartcore.registry.PoptartCoreRecipes;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class CrucibleBlockEntity extends BaseContainerBlockEntity {

    public static final int INPUT_SLOT_1 = 0;
    public static final int INPUT_SLOT_2 = 1;
    public static final int INPUT_SLOT_3 = 2;
    public static final int FUEL_SLOT = 3;
    public static final int CONTAINER_SLOT = 4;
    public static final int RESULT_SLOT = 5;

    public static final int TANK_CAPACITY = 1000;
    public static final int CASTING_TIME = 100;

    private NonNullList<ItemStack> items = NonNullList.withSize(6, ItemStack.EMPTY);

    private final FluidTank tank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    private int burnTime;
    private int burnDuration;
    private int cookTime;
    private int cookTimeTotal = 200;
    private int castingProgress;
    private int castingTimeTotal = CASTING_TIME;

    private final ContainerData dataAccess = new ContainerData() {

        @Override
        public int get(int index) {
            return switch (index) {
                case 0 ->
                    burnDuration > Short.MAX_VALUE
                            ? Mth.floor((double) burnTime / burnDuration * Short.MAX_VALUE)
                            : burnTime;
                case 1 -> Math.min(burnDuration, Short.MAX_VALUE);
                case 2 -> cookTime;
                case 3 -> cookTimeTotal;
                case 4 -> BuiltInRegistries.FLUID.getId(tank.getFluid().getFluid());
                case 5 -> tank.getFluidAmount();
                case 6 -> castingProgress;
                case 7 -> castingTimeTotal;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> burnTime = value;
                case 1 -> burnDuration = value;
                case 2 -> cookTime = value;
                case 3 -> cookTimeTotal = value;
                case 6 -> castingProgress = value;
                case 7 -> castingTimeTotal = value;
            }
        }

        @Override
        public int getCount() {
            return 8;
        }
    };

    public CrucibleBlockEntity(BlockPos pos, BlockState state) {
        super(PoptartCoreBlockEntities.CRUCIBLE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CrucibleBlockEntity blockEntity) {
        boolean changed = false;

        AlloyingRecipe alloyingRecipe =
                blockEntity.getAlloyingRecipe(level).map(RecipeHolder::value).orElse(null);
        AlloyPlan alloyPlan = alloyingRecipe == null ? null : blockEntity.planAlloying(level, alloyingRecipe);

        MeltingRecipe meltingRecipe = null;
        int meltingBatches = 0;

        if (alloyPlan == null) {
            meltingRecipe =
                    blockEntity.getMeltingRecipe(level).map(RecipeHolder::value).orElse(null);
            meltingBatches = meltingRecipe == null ? 0 : blockEntity.meltBatches(meltingRecipe);
        }

        boolean canProcess = alloyPlan != null || meltingBatches > 0;

        if (blockEntity.burnTime > 0) {
            blockEntity.burnTime--;
            changed = true;
        }

        if (!blockEntity.isBurning() && canProcess) {
            ItemStack fuel = blockEntity.items.get(FUEL_SLOT);
            int burnDuration = blockEntity.getBurnDuration(fuel);

            if (burnDuration > 0) {
                blockEntity.burnTime = burnDuration;
                blockEntity.burnDuration = burnDuration;

                if (fuel.hasCraftingRemainingItem()) {
                    blockEntity.items.set(FUEL_SLOT, fuel.getCraftingRemainingItem());
                } else {
                    fuel.shrink(1);
                }
                changed = true;
            }
        }

        if (blockEntity.isBurning() && canProcess) {
            int cookTimeTotal = alloyPlan != null
                    ? alloyingRecipe.getCookingTime() * alloyPlan.batches()
                    : meltingRecipe.getCookingTime() * meltingBatches;

            if (blockEntity.cookTime > 0 && blockEntity.cookTimeTotal != cookTimeTotal) {
                blockEntity.cookTime = 0;
            }

            blockEntity.cookTimeTotal = cookTimeTotal;
            blockEntity.cookTime++;

            if (blockEntity.cookTime >= blockEntity.cookTimeTotal) {
                blockEntity.cookTime = 0;

                boolean completed = alloyPlan != null
                        ? blockEntity.performAlloying(alloyingRecipe, alloyPlan)
                        : blockEntity.performMelting(meltingRecipe);

                if (completed) {
                    changed = true;
                }
            }

            changed = true;
        } else if (blockEntity.cookTime > 0) {
            blockEntity.cookTime = 0;
            changed = true;
        }

        CastingRecipe castingRecipe =
                blockEntity.getCastingRecipe(level).map(RecipeHolder::value).orElse(null);

        boolean canCast = castingRecipe != null && blockEntity.canCast(level, castingRecipe);

        if (!PoptartCoreConfig.ENABLE_CASTING_TIMER.get()) {
            if (blockEntity.castingProgress != 0 || blockEntity.castingTimeTotal != 0) {
                changed = true;
            }

            blockEntity.castingProgress = 0;
            blockEntity.castingTimeTotal = 0;

            if (canCast && blockEntity.performCasting(level, castingRecipe)) {
                changed = true;
            }
        } else if (canCast) {
            blockEntity.castingTimeTotal = CASTING_TIME;
            blockEntity.castingProgress++;

            if (blockEntity.castingProgress >= blockEntity.castingTimeTotal) {
                blockEntity.castingProgress = 0;

                if (blockEntity.performCasting(level, castingRecipe)) {
                    changed = true;
                }
            } else {
                changed = true;
            }
        } else if (blockEntity.castingProgress > 0) {
            blockEntity.castingProgress = 0;
            changed = true;
        }

        int fluidLevel = blockEntity.getFluidLevel();
        boolean isLit = blockEntity.isBurning();
        BlockState currentState = level.getBlockState(pos);

        if (currentState.getValue(CrucibleBlock.LIT) != isLit
                || currentState.getValue(CrucibleBlock.FLUID_LEVEL) != fluidLevel) {
            level.setBlock(
                    pos,
                    currentState.setValue(CrucibleBlock.LIT, isLit).setValue(CrucibleBlock.FLUID_LEVEL, fluidLevel),
                    3);

            changed = true;
        }

        if (changed) {
            blockEntity.setChanged();
        }
    }

    private MeltingRecipeInput getMeltingInput() {
        return new MeltingRecipeInput(
                List.of(items.get(INPUT_SLOT_1), items.get(INPUT_SLOT_2), items.get(INPUT_SLOT_3)));
    }

    private Optional<RecipeHolder<MeltingRecipe>> getMeltingRecipe(Level level) {
        return level.getRecipeManager()
                .getRecipeFor(PoptartCoreRecipes.CRUCIBLE_MELTING_TYPE.get(), getMeltingInput(), level);
    }

    private AlloyingRecipeInput getAlloyingInput() {
        return new AlloyingRecipeInput(
                List.of(items.get(INPUT_SLOT_1), items.get(INPUT_SLOT_2), items.get(INPUT_SLOT_3)), tank.getFluid());
    }

    private Optional<RecipeHolder<AlloyingRecipe>> getAlloyingRecipe(Level level) {
        return level.getRecipeManager()
                .getRecipeFor(PoptartCoreRecipes.CRUCIBLE_ALLOYING_TYPE.get(), getAlloyingInput(), level);
    }

    private CastingRecipeInput getCastingInput() {
        return new CastingRecipeInput(items.get(CONTAINER_SLOT), tank.getFluid());
    }

    private Optional<RecipeHolder<CastingRecipe>> getCastingRecipe(Level level) {
        return level.getRecipeManager()
                .getRecipeFor(PoptartCoreRecipes.CRUCIBLE_CASTING_TYPE.get(), getCastingInput(), level);
    }

    private boolean isBurning() {
        return burnTime > 0;
    }

    private int getFluidLevel() {
        int amount = tank.getFluidAmount();

        if (amount <= 0) {
            return 0;
        }

        return Math.min(4, (amount + 249) / 250);
    }

    protected int getBurnDuration(ItemStack fuel) {
        if (fuel.isEmpty()) {
            return 0;
        }

        return fuel.getBurnTime(RecipeType.SMELTING);
    }

    private int meltBatches(MeltingRecipe recipe) {
        FluidStack result = recipe.result();

        if (result.isEmpty() || result.getAmount() <= 0) {
            return 0;
        }

        FluidStack tankFluid = tank.getFluid();

        if (!tankFluid.isEmpty() && !FluidStack.isSameFluidSameComponents(tankFluid, result)) {
            return 0;
        }

        int maxBySpace = tank.getSpace() / result.getAmount();

        if (maxBySpace <= 0) {
            return 0;
        }

        int filledSlots = 0;

        for (int slot = INPUT_SLOT_1; slot <= INPUT_SLOT_3; slot++) {
            if (!items.get(slot).isEmpty()) {
                filledSlots++;
            }
        }

        return Math.min(filledSlots, maxBySpace);
    }

    private boolean performMelting(MeltingRecipe recipe) {
        int batches = meltBatches(recipe);

        if (batches <= 0) {
            return false;
        }

        FluidStack result = recipe.result();

        tank.fill(result.copyWithAmount(result.getAmount() * batches), IFluidHandler.FluidAction.EXECUTE);

        int melted = 0;

        for (int slot = INPUT_SLOT_1; slot <= INPUT_SLOT_3 && melted < batches; slot++) {

            ItemStack stack = items.get(slot);

            if (!stack.isEmpty()) {
                stack.shrink(1);
                melted++;
            }
        }

        return true;
    }

    private AlloyPlan planAlloying(Level level, AlloyingRecipe recipe) {
        FluidStack tankFluid = tank.getFluid();
        int availableBatches = recipe.batchCount(getAlloyingInput(), level);

        if (availableBatches <= 0) {
            return null;
        }

        int batches = 1;

        ItemStack itemResult = recipe.itemResult();
        if (!itemResult.isEmpty()) {
            ItemStack resultSlot = items.get(RESULT_SLOT);
            int availableSpace;

            if (resultSlot.isEmpty()) {
                availableSpace = itemResult.getMaxStackSize();
            } else if (ItemStack.isSameItemSameComponents(resultSlot, itemResult)) {
                availableSpace = resultSlot.getMaxStackSize() - resultSlot.getCount();
            } else {
                return null;
            }

            int maxBatchesByItem = availableSpace / Math.max(1, itemResult.getCount());
            if (maxBatchesByItem <= 0) {
                return null;
            }

            batches = Math.min(batches, maxBatchesByItem);
        }

        int[] meltAmountPerSlot = new int[FUEL_SLOT];
        FluidStack[] meltFluidPerSlot = new FluidStack[FUEL_SLOT];

        for (int slot = INPUT_SLOT_1; slot <= INPUT_SLOT_3; slot++) {
            ItemStack stack = items.get(slot);

            if (!stack.isEmpty() && !recipe.usesAsItem(stack)) {
                FluidStack melted = tryMelt(stack, level);

                if (!melted.isEmpty()) {
                    meltFluidPerSlot[slot] = melted;
                    meltAmountPerSlot[slot] = melted.getAmount();
                }
            }
        }

        while (batches > 0) {
            int[] itemsToConsume = new int[FUEL_SLOT];
            int tankToDrain =
                    gatherIngredients(recipe, batches, meltFluidPerSlot, meltAmountPerSlot, tankFluid, itemsToConsume);

            FluidStack produced = recipe.result().isEmpty()
                    ? FluidStack.EMPTY
                    : recipe.result().copyWithAmount(recipe.result().getAmount() * batches);

            if (tankToDrain >= 0 && outputFits(produced, tankFluid, tankToDrain)) {
                return new AlloyPlan(batches, tankToDrain, itemsToConsume, produced);
            }

            batches--;
        }

        return null;
    }

    private FluidStack tryMelt(ItemStack stack, Level level) {
        ItemStack singleItem = stack.copyWithCount(1);
        MeltingRecipeInput input = new MeltingRecipeInput(List.of(singleItem));

        return level.getRecipeManager()
                .getRecipeFor(PoptartCoreRecipes.CRUCIBLE_MELTING_TYPE.get(), input, level)
                .map(RecipeHolder::value)
                .map(recipe -> recipe.assembleFluid(input))
                .orElse(FluidStack.EMPTY);
    }

    private int gatherIngredients(
            AlloyingRecipe recipe,
            int batches,
            FluidStack[] meltFluidPerSlot,
            int[] meltAmountPerSlot,
            FluidStack tankFluid,
            int[] itemsToConsume) {
        int[] remainingItems = new int[FUEL_SLOT];

        for (int slot = INPUT_SLOT_1; slot <= INPUT_SLOT_3; slot++) {
            remainingItems[slot] = items.get(slot).getCount();
        }

        int tankToDrain = 0;

        for (CrucibleIngredient ingredient : recipe.ingredients()) {
            if (!ingredient.fluid().isEmpty()) {
                int needed = ingredient.fluid().getAmount() * batches;

                if (FluidStack.isSameFluid(ingredient.fluid(), tankFluid)) {
                    int taken = Math.min(tankFluid.getAmount() - tankToDrain, needed);
                    tankToDrain += Math.max(0, taken);
                    needed -= Math.max(0, taken);
                }

                for (int slot = INPUT_SLOT_1; slot <= INPUT_SLOT_3 && needed > 0; slot++) {
                    FluidStack melted = meltFluidPerSlot[slot];

                    if (melted == null
                            || remainingItems[slot] <= 0
                            || !FluidStack.isSameFluid(melted, ingredient.fluid())) {
                        continue;
                    }

                    int amountPerItem = meltAmountPerSlot[slot];
                    int requiredItems = (needed + amountPerItem - 1) / amountPerItem;
                    int taken = Math.min(remainingItems[slot], requiredItems);

                    remainingItems[slot] -= taken;
                    itemsToConsume[slot] += taken;
                    needed -= taken * amountPerItem;
                }

                if (needed > 0) {
                    return -1;
                }
            } else if (!ingredient.item().isEmpty()) {
                int needed = batches;

                for (int slot = INPUT_SLOT_1; slot <= INPUT_SLOT_3 && needed > 0; slot++) {
                    if (meltFluidPerSlot[slot] != null || remainingItems[slot] <= 0) {
                        continue;
                    }

                    ItemStack stack = items.get(slot);
                    if (ingredient.item().test(stack)) {
                        int taken = Math.min(remainingItems[slot], needed);
                        remainingItems[slot] -= taken;
                        itemsToConsume[slot] += taken;
                        needed -= taken;
                    }
                }

                if (needed > 0) {
                    return -1;
                }
            }
        }

        return tankToDrain;
    }

    private boolean outputFits(FluidStack produced, FluidStack tankFluid, int tankToDrain) {
        if (produced.isEmpty()) {
            return true;
        }

        int fluidRemaining = tankFluid.getAmount() - tankToDrain;
        if (fluidRemaining > 0 && !FluidStack.isSameFluidSameComponents(tankFluid, produced)) {
            return false;
        }

        return produced.getAmount() + fluidRemaining <= tank.getCapacity();
    }

    private boolean performAlloying(AlloyingRecipe recipe, AlloyPlan plan) {
        if (plan.tankToDrain() > 0) {
            tank.drain(plan.tankToDrain(), IFluidHandler.FluidAction.EXECUTE);
        }

        for (int slot = INPUT_SLOT_1; slot <= INPUT_SLOT_3; slot++) {
            if (plan.itemsToConsume()[slot] > 0) {
                items.get(slot).shrink(plan.itemsToConsume()[slot]);
            }
        }

        if (!plan.produced().isEmpty()) {
            tank.fill(plan.produced(), IFluidHandler.FluidAction.EXECUTE);
        }

        ItemStack itemResult = recipe.itemResult();
        if (!itemResult.isEmpty()) {
            ItemStack resultSlot = items.get(RESULT_SLOT);
            int amountProduced = itemResult.getCount() * plan.batches();

            if (resultSlot.isEmpty()) {
                ItemStack output = itemResult.copy();
                output.setCount(amountProduced);
                items.set(RESULT_SLOT, output);
            } else {
                resultSlot.grow(amountProduced);
            }
        }

        return true;
    }

    private boolean performCasting(Level level, CastingRecipe recipe) {
        ItemStack mould = items.get(CONTAINER_SLOT);
        ItemStack result = items.get(RESULT_SLOT);

        CastingRecipeInput input = new CastingRecipeInput(mould, tank.getFluid());

        ItemStack castingResult = recipe.assemble(input, level.registryAccess());

        if (castingResult.isEmpty()) {
            return false;
        }

        boolean fits = result.isEmpty()
                || ItemStack.isSameItemSameComponents(result, castingResult)
                        && result.getCount() + castingResult.getCount() <= result.getMaxStackSize();

        if (!fits) {
            return false;
        }

        tank.drain(recipe.fluid().getAmount(), IFluidHandler.FluidAction.EXECUTE);

        if (mould.isDamageableItem()) {
            mould.hurtAndBreak(1, (ServerLevel) level, (ServerPlayer) null, item -> {});
        } else {
            mould.shrink(1);
        }

        if (result.isEmpty()) {
            items.set(RESULT_SLOT, castingResult);
        } else {
            result.grow(castingResult.getCount());
        }

        return true;
    }

    private boolean canCast(Level level, CastingRecipe recipe) {
        ItemStack castingResult = recipe.assemble(getCastingInput(), level.registryAccess());
        ItemStack result = items.get(RESULT_SLOT);

        return !castingResult.isEmpty()
                && (result.isEmpty()
                        || ItemStack.isSameItemSameComponents(result, castingResult)
                                && result.getCount() + castingResult.getCount() <= result.getMaxStackSize());
    }

    @Override
    public int getContainerSize() {
        return 6;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.poptartcore.crucible");
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new CrucibleMenu(containerId, inventory, this, dataAccess);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        ContainerHelper.saveAllItems(tag, items, registries);

        tag.putInt("BurnTime", burnTime);
        tag.putInt("BurnDuration", burnDuration);
        tag.putInt("CookTime", cookTime);
        tag.putInt("CookTimeTotal", cookTimeTotal);
        tag.putInt("CastingProgress", castingProgress);
        tag.putInt("CastingTimeTotal", castingTimeTotal);

        FluidStack fluid = tank.getFluid();

        if (!fluid.isEmpty()) {
            tag.put("Fluid", fluid.save(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);

        ContainerHelper.loadAllItems(tag, items, registries);

        burnTime = tag.getInt("BurnTime");
        burnDuration = tag.getInt("BurnDuration");
        cookTime = tag.getInt("CookTime");

        if (tag.contains("CookTimeTotal")) {
            cookTimeTotal = tag.getInt("CookTimeTotal");
        }

        castingProgress = tag.getInt("CastingProgress");

        if (tag.contains("CastingTimeTotal")) {
            castingTimeTotal = tag.getInt("CastingTimeTotal");
        }

        if (tag.contains("Fluid")) {
            tank.setFluid(FluidStack.parseOptional(registries, tag.getCompound("Fluid")));
        } else {
            tank.setFluid(FluidStack.EMPTY);
        }
    }

    private record AlloyPlan(int batches, int tankToDrain, int[] itemsToConsume, FluidStack produced) {}
}
