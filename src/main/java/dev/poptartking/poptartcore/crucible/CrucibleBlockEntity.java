package dev.poptartking.poptartcore.crucible;

import dev.poptartking.poptartcore.crucible.casting.CastingRecipe;
import dev.poptartking.poptartcore.crucible.casting.CastingRecipeInput;
import dev.poptartking.poptartcore.crucible.melting.MeltingRecipe;
import dev.poptartking.poptartcore.crucible.melting.MeltingRecipeInput;
import dev.poptartking.poptartcore.crucible.menu.CrucibleMenu;
import dev.poptartking.poptartcore.registry.PoptartCoreBlockEntities;
import dev.poptartking.poptartcore.registry.PoptartCoreRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

import java.util.List;
import java.util.Optional;

public class CrucibleBlockEntity extends BaseContainerBlockEntity {

    public static final int INPUT_SLOT_1 = 0;
    public static final int INPUT_SLOT_2 = 1;
    public static final int INPUT_SLOT_3 = 2;
    public static final int FUEL_SLOT = 3;
    public static final int CONTAINER_SLOT = 4;
    public static final int RESULT_SLOT = 5;

    public static final int TANK_CAPACITY = 1000;

    private NonNullList<ItemStack> items =
            NonNullList.withSize(6, ItemStack.EMPTY);

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

    private final ContainerData dataAccess = new ContainerData() {

        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> burnTime;
                case 1 -> burnDuration;
                case 2 -> cookTime;
                case 3 -> cookTimeTotal;
                case 4 -> BuiltInRegistries.FLUID.getId(
                        tank.getFluid().getFluid()
                );
                case 5 -> tank.getFluidAmount();
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
            }
        }

        @Override
        public int getCount() {
            return 6;
        }
    };

    public CrucibleBlockEntity(BlockPos pos, BlockState state) {
        super(PoptartCoreBlockEntities.CRUCIBLE.get(), pos, state);
    }

    public static void serverTick(
            Level level,
            BlockPos pos,
            BlockState state,
            CrucibleBlockEntity blockEntity
    ) {
        boolean wasLit = blockEntity.isBurning();
        boolean changed = false;

        Optional<RecipeHolder<MeltingRecipe>> meltingRecipe =
                blockEntity.getMeltingRecipe(level);

        boolean canMelt = meltingRecipe
                .map(recipe ->
                        blockEntity.meltBatches(recipe.value()) > 0
                )
                .orElse(false);

        if (blockEntity.burnTime > 0) {
            blockEntity.burnTime--;
            changed = true;
        }

        if (!blockEntity.isBurning() && canMelt) {
            ItemStack fuel = blockEntity.items.get(FUEL_SLOT);
            int burnDuration = blockEntity.getBurnDuration(fuel);

            if (burnDuration > 0) {
                blockEntity.burnTime = burnDuration;
                blockEntity.burnDuration = burnDuration;

                fuel.shrink(1);
                changed = true;
            }
        }

        if (blockEntity.isBurning() && canMelt) {
            MeltingRecipe recipe = meltingRecipe.get().value();
            int batches = blockEntity.meltBatches(recipe);

            int cookTimeTotal =
                    recipe.getCookingTime() * Math.max(1, batches);

            if (blockEntity.cookTime > 0
                    && blockEntity.cookTimeTotal != cookTimeTotal) {
                blockEntity.cookTime = 0;
            }

            blockEntity.cookTimeTotal = cookTimeTotal;
            blockEntity.cookTime++;

            if (blockEntity.cookTime >= blockEntity.cookTimeTotal) {
                blockEntity.cookTime = 0;

                if (blockEntity.performMelting(recipe)) {
                    changed = true;
                }
            }

            changed = true;
        } else if (blockEntity.cookTime > 0) {
            blockEntity.cookTime = 0;
            changed = true;
        }

        Optional<RecipeHolder<CastingRecipe>> castingRecipe =
                blockEntity.getCastingRecipe(level);

        if (castingRecipe.isPresent()) {
            if (blockEntity.performCasting(
                    level,
                    castingRecipe.get().value()
            )) {
                changed = true;
            }
        }

        boolean isLit = blockEntity.isBurning();

        if (wasLit != isLit) {
            level.setBlock(
                    pos,
                    state.setValue(CrucibleBlock.LIT, isLit),
                    3
            );

            changed = true;
        }

        if (changed) {
            blockEntity.setChanged();
        }
    }

    private MeltingRecipeInput getMeltingInput() {
        return new MeltingRecipeInput(
                List.of(
                        items.get(INPUT_SLOT_1),
                        items.get(INPUT_SLOT_2),
                        items.get(INPUT_SLOT_3)
                )
        );
    }

    private Optional<RecipeHolder<MeltingRecipe>> getMeltingRecipe(
            Level level
    ) {
        return level.getRecipeManager().getRecipeFor(
                PoptartCoreRecipes.CRUCIBLE_MELTING_TYPE.get(),
                getMeltingInput(),
                level
        );
    }

    private CastingRecipeInput getCastingInput() {
        return new CastingRecipeInput(
                items.get(CONTAINER_SLOT),
                tank.getFluid()
        );
    }

    private Optional<RecipeHolder<CastingRecipe>> getCastingRecipe(
            Level level
    ) {
        return level.getRecipeManager().getRecipeFor(
                PoptartCoreRecipes.CRUCIBLE_CASTING_TYPE.get(),
                getCastingInput(),
                level
        );
    }

    private boolean isBurning() {
        return burnTime > 0;
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

        if (!tankFluid.isEmpty()
                && !FluidStack.isSameFluidSameComponents(tankFluid, result)) {
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

        tank.fill(
                result.copyWithAmount(
                        result.getAmount() * batches
                ),
                IFluidHandler.FluidAction.EXECUTE
        );

        int melted = 0;

        for (int slot = INPUT_SLOT_1;
             slot <= INPUT_SLOT_3 && melted < batches;
             slot++) {

            ItemStack stack = items.get(slot);

            if (!stack.isEmpty()) {
                stack.shrink(1);
                melted++;
            }
        }

        return true;
    }

    private boolean performCasting(
            Level level,
            CastingRecipe recipe
    ) {
        ItemStack mould = items.get(CONTAINER_SLOT);
        ItemStack result = items.get(RESULT_SLOT);

        CastingRecipeInput input =
                new CastingRecipeInput(
                        mould,
                        tank.getFluid()
                );

        ItemStack castingResult =
                recipe.assemble(
                        input,
                        level.registryAccess()
                );

        if (castingResult.isEmpty()) {
            return false;
        }

        boolean fits =
                result.isEmpty()
                        || ItemStack.isSameItemSameComponents(
                        result,
                        castingResult
                )
                        && result.getCount()
                        + castingResult.getCount()
                        <= result.getMaxStackSize();

        if (!fits) {
            return false;
        }

        tank.drain(
                recipe.fluid().getAmount(),
                IFluidHandler.FluidAction.EXECUTE
        );

        if (mould.isDamageableItem()) {
            mould.hurtAndBreak(
                    1,
                    (ServerLevel) level,
                    (ServerPlayer) null,
                    item -> {}
            );
        } else {
            mould.shrink(1);
        }

        if (result.isEmpty()) {
            items.set(
                    RESULT_SLOT,
                    castingResult
            );
        } else {
            result.grow(
                    castingResult.getCount()
            );
        }

        return true;
    }

    @Override
    public int getContainerSize() {
        return 6;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable(
                "container.poptartcore.crucible"
        );
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
    protected AbstractContainerMenu createMenu(
            int containerId,
            Inventory inventory
    ) {
        return new CrucibleMenu(
                containerId,
                inventory,
                this,
                dataAccess
        );
    }

    @Override
    protected void saveAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        super.saveAdditional(tag, registries);

        ContainerHelper.saveAllItems(
                tag,
                items,
                registries
        );

        tag.putInt("BurnTime", burnTime);
        tag.putInt("BurnDuration", burnDuration);
        tag.putInt("CookTime", cookTime);
        tag.putInt("CookTimeTotal", cookTimeTotal);

        FluidStack fluid = tank.getFluid();

        if (!fluid.isEmpty()) {
            tag.put(
                    "Fluid",
                    fluid.save(registries)
            );
        }
    }

    @Override
    protected void loadAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        super.loadAdditional(tag, registries);

        items = NonNullList.withSize(
                getContainerSize(),
                ItemStack.EMPTY
        );

        ContainerHelper.loadAllItems(
                tag,
                items,
                registries
        );

        burnTime = tag.getInt("BurnTime");
        burnDuration = tag.getInt("BurnDuration");
        cookTime = tag.getInt("CookTime");

        if (tag.contains("CookTimeTotal")) {
            cookTimeTotal = tag.getInt("CookTimeTotal");
        }

        if (tag.contains("Fluid")) {
            tank.setFluid(
                    FluidStack.parseOptional(
                            registries,
                            tag.getCompound("Fluid")
                    )
            );
        } else {
            tank.setFluid(FluidStack.EMPTY);
        }
    }
}