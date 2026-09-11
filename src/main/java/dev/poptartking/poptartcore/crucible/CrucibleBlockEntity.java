package dev.poptartking.poptartcore.crucible;

import dev.poptartking.poptartcore.crucible.alloying.AlloyingRecipe;
import dev.poptartking.poptartcore.crucible.alloying.AlloyingRecipeInput;
import dev.poptartking.poptartcore.crucible.alloying.CrucibleIngredient;
import dev.poptartking.poptartcore.crucible.casting.CastingRecipe;
import dev.poptartking.poptartcore.crucible.casting.CastingRecipeInput;
import dev.poptartking.poptartcore.crucible.melting.MeltingRecipe;
import dev.poptartking.poptartcore.crucible.melting.MeltingRecipeInput;
import dev.poptartking.poptartcore.crucible.menu.CrucibleMenu;
import dev.poptartking.poptartcore.registry.PoptartCoreBlockEntities;
import dev.poptartking.poptartcore.registry.PoptartCoreItems;
import dev.poptartking.poptartcore.registry.PoptartCoreRecipes;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class CrucibleBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {

    public static final int INPUT_SLOT_1 = 0;
    public static final int INPUT_SLOT_2 = 1;
    public static final int INPUT_SLOT_3 = 2;
    public static final int FUEL_SLOT = 3;
    public static final int CONTAINER_SLOT = 4;
    public static final int RESULT_SLOT = 5;

    public static final int TANK_CAPACITY = 1000;
    public static final int CASTING_TIME = 100;

    protected final int inputCount;
    protected final int fuelSlot;
    protected final int containerSlot;
    protected final int resultSlot;
    protected final int tankCapacity;
    protected NonNullList<ItemStack> items;
    protected final FluidTank tank;

    private int burnTime;
    private int burnDuration;
    private int burnSpeed = 1;
    private int cookTime;
    private int cookingBatches;
    private int cookTimeTotal = 200;
    private int castingProgress;
    private int castingTimeTotal = CASTING_TIME;
    private ResourceLocation cookingRecipeId;
    private ResourceLocation castingRecipeId;

    protected final ContainerData dataAccess = new ContainerData() {

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
        this(PoptartCoreBlockEntities.CRUCIBLE.get(), pos, state, 3, TANK_CAPACITY);
    }

    protected CrucibleBlockEntity(
            BlockEntityType<?> type, BlockPos pos, BlockState state, int inputCount, int tankCapacity) {
        super(type, pos, state);
        this.inputCount = inputCount;
        this.fuelSlot = inputCount;
        this.containerSlot = inputCount + 1;
        this.resultSlot = inputCount + 2;
        this.tankCapacity = tankCapacity;
        this.items = NonNullList.withSize(inputCount + 3, ItemStack.EMPTY);
        this.tank = new FluidTank(tankCapacity) {
            @Override
            protected void onContentsChanged() {
                setChanged();
            }
        };
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CrucibleBlockEntity blockEntity) {
        boolean changed = false;

        RecipeHolder<AlloyingRecipe> alloyingHolder =
                blockEntity.getAlloyingRecipe(level).orElse(null);
        AlloyingRecipe alloyingRecipe = alloyingHolder == null ? null : alloyingHolder.value();
        int alloyBatchLimit = alloyingHolder == null ? 0 : blockEntity.activeBatches(alloyingHolder.id());
        AlloyPlan alloyPlan =
                alloyingRecipe == null ? null : blockEntity.planAlloying(level, alloyingRecipe, alloyBatchLimit);

        MeltingRecipe meltingRecipe = null;
        RecipeHolder<MeltingRecipe> meltingHolder = null;
        int meltingBatches = 0;

        if (alloyPlan == null) {
            meltingHolder = blockEntity.getMeltingRecipe(level).orElse(null);
            meltingRecipe = meltingHolder == null ? null : meltingHolder.value();
            meltingBatches = meltingRecipe == null ? 0 : blockEntity.meltBatches(meltingRecipe);
            int activeBatches = meltingHolder == null ? 0 : blockEntity.activeBatches(meltingHolder.id());
            if (activeBatches > 0) {
                meltingBatches = meltingBatches >= activeBatches ? activeBatches : 0;
            }
        }

        boolean canProcess = alloyPlan != null || meltingBatches > 0;

        if (blockEntity.burnTime > 0) {
            blockEntity.burnTime = Math.max(0, blockEntity.burnTime - blockEntity.burnSpeed);
            changed = true;
        }

        if (!blockEntity.isBurning() && canProcess) {
            ItemStack fuel = blockEntity.items.get(blockEntity.fuelSlot);
            int burnDuration = blockEntity.getBurnDuration(fuel);

            if (burnDuration > 0) {
                blockEntity.burnTime = burnDuration;
                blockEntity.burnDuration = burnDuration;
                blockEntity.burnSpeed = blockEntity.fuelSpeedMultiplier(fuel);

                if (fuel.hasCraftingRemainingItem()) {
                    blockEntity.items.set(blockEntity.fuelSlot, fuel.getCraftingRemainingItem());
                } else {
                    fuel.shrink(1);
                }
                changed = true;
            }
        }

        if (blockEntity.isBurning() && canProcess) {
            ResourceLocation recipeId = alloyPlan != null ? alloyingHolder.id() : meltingHolder.id();
            int cookTimeTotal = blockEntity.adjustCookTime(
                    alloyPlan != null
                            ? alloyingRecipe.getCookingTime() * alloyPlan.batches()
                            : meltingRecipe.getCookingTime() * meltingBatches);

            if (!Objects.equals(blockEntity.cookingRecipeId, recipeId) || blockEntity.cookTimeTotal != cookTimeTotal) {
                blockEntity.cookTime = 0;
            }

            blockEntity.cookingRecipeId = recipeId;
            blockEntity.cookingBatches = alloyPlan != null ? alloyPlan.batches() : meltingBatches;
            blockEntity.cookTimeTotal = cookTimeTotal;
            blockEntity.cookTime += blockEntity.burnSpeed;

            if (blockEntity.cookTime >= blockEntity.cookTimeTotal) {
                blockEntity.cookTime = 0;

                boolean completed = alloyPlan != null
                        ? blockEntity.performAlloying(alloyingRecipe, alloyPlan)
                        : blockEntity.performMelting(meltingRecipe, meltingBatches);
                blockEntity.cookingBatches = 0;

                if (completed) {
                    changed = true;
                }
            }

            changed = true;
        } else if (blockEntity.cookTime > 0) {
            blockEntity.cookTime = 0;
            blockEntity.cookingBatches = 0;
            changed = true;
        }

        RecipeHolder<CastingRecipe> castingHolder =
                blockEntity.getCastingRecipe(level).orElse(null);
        CastingRecipe castingRecipe = castingHolder == null ? null : castingHolder.value();

        boolean canCast = castingRecipe != null && blockEntity.canCast(level, castingRecipe);

        if (canCast) {
            if (!Objects.equals(blockEntity.castingRecipeId, castingHolder.id())) {
                blockEntity.castingProgress = 0;
            }
            blockEntity.castingRecipeId = castingHolder.id();
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

        boolean isLit = blockEntity.isBurning();
        if (blockEntity.updateBlockState(level, pos, isLit)) {
            changed = true;
        }

        if (changed) {
            blockEntity.setChanged();
        }
    }

    protected MeltingRecipeInput getMeltingInput() {
        return new MeltingRecipeInput(getInputStacks(), isBlastFurnace());
    }

    protected Optional<RecipeHolder<MeltingRecipe>> getMeltingRecipe(Level level) {
        return findMelting(getMeltingInput(), level);
    }

    protected Optional<RecipeHolder<MeltingRecipe>> findMelting(MeltingRecipeInput input, Level level) {
        return level.getRecipeManager().getRecipeFor(PoptartCoreRecipes.CRUCIBLE_MELTING_TYPE.get(), input, level);
    }

    protected AlloyingRecipeInput getAlloyingInput() {
        return new AlloyingRecipeInput(getInputStacks(), tank.getFluid(), isBlastFurnace());
    }

    protected Optional<RecipeHolder<AlloyingRecipe>> getAlloyingRecipe(Level level) {
        return findAlloying(getAlloyingInput(), level);
    }

    protected Optional<RecipeHolder<AlloyingRecipe>> findAlloying(AlloyingRecipeInput input, Level level) {
        return level.getRecipeManager().getRecipeFor(PoptartCoreRecipes.CRUCIBLE_ALLOYING_TYPE.get(), input, level);
    }

    private List<ItemStack> getInputStacks() {
        return List.copyOf(items.subList(0, inputCount));
    }

    private CastingRecipeInput getCastingInput() {
        return new CastingRecipeInput(items.get(containerSlot), tank.getFluid());
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

    protected boolean updateBlockState(Level level, BlockPos pos, boolean isLit) {
        BlockState currentState = level.getBlockState(pos);
        int fluidLevel = getFluidLevel();

        if (currentState.getValue(CrucibleBlock.LIT) == isLit
                && currentState.getValue(CrucibleBlock.FLUID_LEVEL) == fluidLevel) {
            return false;
        }

        level.setBlock(
                pos,
                currentState.setValue(CrucibleBlock.LIT, isLit).setValue(CrucibleBlock.FLUID_LEVEL, fluidLevel),
                3);
        return true;
    }

    protected int getBurnDuration(ItemStack fuel) {
        if (fuel.isEmpty()) {
            return 0;
        }

        return fuel.getBurnTime(RecipeType.SMELTING);
    }

    protected boolean isBlastFurnace() {
        return false;
    }

    protected int adjustCookTime(int base) {
        return base;
    }

    protected int fuelSpeedMultiplier(ItemStack fuel) {
        return 1;
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

        for (int slot = 0; slot < inputCount; slot++) {
            if (!items.get(slot).isEmpty()) {
                filledSlots++;
            }
        }

        return limitMeltingBatches(Math.min(filledSlots, maxBySpace));
    }

    protected int limitMeltingBatches(int availableBatches) {
        return availableBatches;
    }

    private boolean performMelting(MeltingRecipe recipe, int batches) {
        if (batches <= 0 || meltBatches(recipe) < batches) {
            return false;
        }

        FluidStack result = recipe.result();

        tank.fill(result.copyWithAmount(result.getAmount() * batches), IFluidHandler.FluidAction.EXECUTE);

        int melted = 0;

        for (int slot = 0; slot < inputCount && melted < batches; slot++) {

            ItemStack stack = items.get(slot);

            if (!stack.isEmpty()) {
                stack.shrink(1);
                melted++;
            }
        }

        return true;
    }

    // A running cycle keeps its batch size when automation adds items or frees output space.
    private int activeBatches(ResourceLocation recipeId) {
        return cookTime > 0 && Objects.equals(cookingRecipeId, recipeId) ? cookingBatches : 0;
    }

    private AlloyPlan planAlloying(Level level, AlloyingRecipe recipe, int requiredBatches) {
        FluidStack tankFluid = tank.getFluid();
        int availableBatches = recipe.batchCount(getAlloyingInput(), level);

        if (availableBatches <= 0) {
            return null;
        }

        int batches = limitAlloyingBatches(recipe, availableBatches);
        if (requiredBatches > 0) {
            if (batches < requiredBatches) return null;
            batches = requiredBatches;
        }

        ItemStack itemResult = recipe.itemResult();
        if (!itemResult.isEmpty()) {
            ItemStack resultSlot = items.get(this.resultSlot);
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
            if (batches < requiredBatches) return null;
        }

        int[] meltAmountPerSlot = new int[inputCount];
        FluidStack[] meltFluidPerSlot = new FluidStack[inputCount];

        for (int slot = 0; slot < inputCount; slot++) {
            ItemStack stack = items.get(slot);

            if (!stack.isEmpty() && !recipe.usesAsItem(stack)) {
                FluidStack melted = tryMelt(stack, level);

                if (!melted.isEmpty()) {
                    meltFluidPerSlot[slot] = melted;
                    meltAmountPerSlot[slot] = melted.getAmount();
                }
            }
        }

        while (batches >= Math.max(1, requiredBatches)) {
            int[] itemsToConsume = new int[inputCount];
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

    protected int limitAlloyingBatches(AlloyingRecipe recipe, int availableBatches) {
        return 1;
    }

    private FluidStack tryMelt(ItemStack stack, Level level) {
        ItemStack singleItem = stack.copyWithCount(1);
        MeltingRecipeInput input = new MeltingRecipeInput(List.of(singleItem), isBlastFurnace());

        return findMelting(input, level)
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
        int[] remainingItems = new int[inputCount];

        for (int slot = 0; slot < inputCount; slot++) {
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

                for (int slot = 0; slot < inputCount && needed > 0; slot++) {
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

                for (int slot = 0; slot < inputCount && needed > 0; slot++) {
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

        for (int slot = 0; slot < inputCount; slot++) {
            if (plan.itemsToConsume()[slot] > 0) {
                items.get(slot).shrink(plan.itemsToConsume()[slot]);
            }
        }

        if (!plan.produced().isEmpty()) {
            tank.fill(plan.produced(), IFluidHandler.FluidAction.EXECUTE);
        }

        ItemStack itemResult = recipe.itemResult();
        if (!itemResult.isEmpty()) {
            ItemStack resultSlot = items.get(this.resultSlot);
            int amountProduced = itemResult.getCount() * plan.batches();

            if (resultSlot.isEmpty()) {
                ItemStack output = itemResult.copy();
                output.setCount(amountProduced);
                items.set(this.resultSlot, output);
            } else {
                resultSlot.grow(amountProduced);
            }
        }

        return true;
    }

    private boolean performCasting(Level level, CastingRecipe recipe) {
        ItemStack mould = items.get(containerSlot);
        ItemStack result = items.get(resultSlot);

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
            items.set(resultSlot, castingResult);
        } else {
            result.grow(castingResult.getCount());
        }

        return true;
    }

    private boolean canCast(Level level, CastingRecipe recipe) {
        ItemStack castingResult = recipe.assemble(getCastingInput(), level.registryAccess());
        ItemStack result = items.get(resultSlot);

        return !castingResult.isEmpty()
                && (result.isEmpty()
                        || ItemStack.isSameItemSameComponents(result, castingResult)
                                && result.getCount() + castingResult.getCount() <= result.getMaxStackSize());
    }

    @Override
    public int getContainerSize() {
        return inputCount + 3;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.UP) {
            return new int[] {INPUT_SLOT_1, INPUT_SLOT_2, INPUT_SLOT_3};
        }
        if (side == Direction.DOWN) {
            return new int[] {resultSlot};
        }
        Direction front = getBlockState().getValue(CrucibleBlock.FACING);
        // Left and right are from the player's view while facing the front.
        if (side == front.getClockWise()) {
            return new int[] {fuelSlot};
        }
        if (side == front.getCounterClockWise()) {
            return new int[] {containerSlot};
        }
        return new int[0];
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (slot == fuelSlot) {
            return getBurnDuration(stack) > 0;
        }
        if (slot == containerSlot) {
            return PoptartCoreItems.isMould(stack);
        }
        if (slot < 0 || slot >= inputCount || level == null) {
            return false;
        }
        MeltingRecipeInput input = new MeltingRecipeInput(List.of(stack.copyWithCount(1)), isBlastFurnace());
        return findMelting(input, level).isPresent()
                || level.getRecipeManager().getAllRecipesFor(PoptartCoreRecipes.CRUCIBLE_ALLOYING_TYPE.get()).stream()
                        .anyMatch(recipe -> recipe.value().usesAsItem(stack));
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
        if (side == Direction.DOWN) {
            return false;
        }
        for (int exposed : getSlotsForFace(side)) {
            if (slot == exposed) {
                return canPlaceItem(slot, stack);
            }
        }
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return side == Direction.DOWN && slot == resultSlot;
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
        tag.putInt("BurnSpeed", burnSpeed);
        tag.putInt("CookTime", cookTime);
        tag.putInt("CookingBatches", cookingBatches);
        tag.putInt("CookTimeTotal", cookTimeTotal);
        tag.putInt("CastingProgress", castingProgress);
        tag.putInt("CastingTimeTotal", castingTimeTotal);
        if (cookingRecipeId != null) {
            tag.putString("CookingRecipe", cookingRecipeId.toString());
        }
        if (castingRecipeId != null) {
            tag.putString("CastingRecipe", castingRecipeId.toString());
        }

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
        burnSpeed = Math.max(1, tag.getInt("BurnSpeed"));
        cookTime = tag.getInt("CookTime");
        cookingBatches = Math.max(0, tag.getInt("CookingBatches"));

        if (tag.contains("CookTimeTotal")) {
            cookTimeTotal = tag.getInt("CookTimeTotal");
        }

        castingProgress = tag.getInt("CastingProgress");
        // Older saves have no recipe identity. Their partial progress safely restarts.
        cookingRecipeId = ResourceLocation.tryParse(tag.getString("CookingRecipe"));
        castingRecipeId = ResourceLocation.tryParse(tag.getString("CastingRecipe"));

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
