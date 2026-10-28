package dev.poptartking.poptartcore.crucible;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.blastfurnace.BlastFurnaceBlock;
import dev.poptartking.poptartcore.blastfurnace.BlastFurnaceBlockEntity;
import dev.poptartking.poptartcore.crucible.alloying.AlloyingRecipe;
import dev.poptartking.poptartcore.crucible.alloying.CrucibleIngredient;
import dev.poptartking.poptartcore.crucible.melting.MeltingRecipe;
import dev.poptartking.poptartcore.registry.PoptartWorkstationBlocks;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CrucibleAlloyingCacheGameTests {
    private static final ResourceLocation ALLOY = PoptartCore.location("test/crucible_cache_alloy");
    private static final ResourceLocation MELTING = PoptartCore.location("test/crucible_cache_melting");

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void unchangedContentsReuseWorkAndCompletionRechecksMaterials(GameTestHelper helper) {
        withRecipes(helper, List.of(itemRecipe(80)), () -> {
            var crucible = crucible(helper);
            inputs(crucible, 3);
            tick(crucible, 40);
            helper.assertTrue(
                    crucible.recipeChecks == 2 && crucible.planChecks == 2 && crucible.progress() == 40,
                    "Unchanged contents reuse recipe resolution and planning after the running batch starts");
            tick(crucible, 40);
            helper.assertTrue(
                    crucible.recipeChecks == 2 && crucible.planChecks == 3,
                    "Completing a cycle performs a fresh consumption plan without repeating unchanged tick work");
            helper.assertTrue(
                    crucible.getItem(CrucibleBlockEntity.RESULT_SLOT).is(Items.FLINT)
                            && crucible.getItem(CrucibleBlockEntity.RESULT_SLOT).getCount() == 1
                            && crucible.getItem(0).getCount() == 2
                            && crucible.getItem(1).getCount() == 2,
                    "The original duration, output, and ingredient consumption are preserved");
        });
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void missingRecipesAreCachedAndNewInputsStartImmediately(GameTestHelper helper) {
        withRecipes(helper, List.of(itemRecipe(80)), () -> {
            var crucible = crucible(helper);
            tick(crucible, 20);
            helper.assertTrue(
                    crucible.recipeChecks == 1
                            && crucible.getItem(CrucibleBlockEntity.FUEL_SLOT).getCount() == 8,
                    "An unchanged empty crucible checks once and does not consume fuel");
            inputs(crucible, 1);
            tick(crucible, 1);
            helper.assertTrue(
                    crucible.recipeChecks == 2 && crucible.progress() == 1,
                    "Inserting ingredients invalidates a cached miss on the next tick");
        });
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void inPlaceInputAndOutputEditsInvalidateCachedPlans(GameTestHelper helper) {
        withRecipes(helper, List.of(itemRecipe(80)), () -> {
            var crucible = crucible(helper);
            inputs(crucible, 3);
            tick(crucible, 5);
            crucible.getItem(0).shrink(3);
            tick(crucible, 1);
            helper.assertTrue(
                    crucible.progress() == 0
                            && crucible.getItem(1).getCount() == 3
                            && crucible.getItem(CrucibleBlockEntity.RESULT_SLOT).isEmpty(),
                    "Removing ingredients through a live stack stops the cycle without consuming other materials");
            crucible.setItem(0, new ItemStack(Items.BRICK, 3));
            tick(crucible, 1);
            int checks = crucible.recipeChecks;
            crucible.getItem(0).set(DataComponents.CUSTOM_NAME, Component.literal("Changed in place"));
            tick(crucible, 1);
            helper.assertTrue(
                    crucible.recipeChecks == checks + 1 && crucible.progress() == 2,
                    "Component changes invalidate planning while preserving valid progress");
            crucible.setItem(CrucibleBlockEntity.RESULT_SLOT, new ItemStack(Items.FLINT, 64));
            tick(crucible, 10);
            helper.assertTrue(
                    crucible.progress() == 0 && crucible.getItem(0).getCount() == 3,
                    "A full output stops processing without consuming ingredients");
            crucible.getItem(CrucibleBlockEntity.RESULT_SLOT).shrink(1);
            tick(crucible, 80);
            helper.assertTrue(
                    crucible.getItem(CrucibleBlockEntity.RESULT_SLOT).getCount() == 64
                            && crucible.getItem(0).getCount() == 2,
                    "Freeing output space through a live stack resumes the original cycle");
        });
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void inPlaceFluidChangesRecheckIngredientsAndTankCompatibility(GameTestHelper helper) {
        var recipe = new AlloyingRecipe(
                List.of(fluidIngredient(), brickIngredient()),
                20,
                new FluidStack(Fluids.LAVA, 100),
                ItemStack.EMPTY,
                false);
        withRecipes(helper, List.of(new RecipeHolder<>(ALLOY, recipe)), () -> {
            var crucible = crucible(helper);
            crucible.setItem(0, new ItemStack(Items.BRICK, 3));
            crucible.tank.setFluid(new FluidStack(Fluids.WATER, 100));
            tick(crucible, 5);
            crucible.tank.getFluid().setAmount(90);
            tick(crucible, 1);
            helper.assertTrue(
                    crucible.progress() == 0 && crucible.getItem(0).getCount() == 3,
                    "Draining a required fluid in place immediately invalidates the running plan");
            crucible.tank.getFluid().setAmount(110);
            tick(crucible, 1);
            helper.assertTrue(
                    crucible.progress() == 0, "A plan cannot leave an incompatible input fluid mixed with the output");
            crucible.tank.setFluid(new FluidStack(Fluids.WATER, 100));
            tick(crucible, 5);
            crucible.tank.setCapacity(50);
            tick(crucible, 1);
            helper.assertTrue(
                    crucible.progress() == 0 && crucible.getItem(0).getCount() == 3,
                    "Changing tank capacity also invalidates a plan whose output no longer fits");
            crucible.tank.setCapacity(CrucibleBlockEntity.TANK_CAPACITY);
            tick(crucible, 20);
            helper.assertTrue(
                    crucible.tank.getFluid().is(Fluids.LAVA)
                            && crucible.tank.getFluidAmount() == 100
                            && crucible.getItem(0).getCount() == 2,
                    "Restoring compatible contents produces exactly the intended fluid and consumes one ingredient");
        });
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void recipeAndMeltingReloadsInvalidateUnchangedInputs(GameTestHelper helper) {
        var alloy = new RecipeHolder<>(
                ALLOY,
                new AlloyingRecipe(
                        List.of(fluidIngredient(), brickIngredient()),
                        80,
                        FluidStack.EMPTY,
                        new ItemStack(Items.FLINT),
                        false));
        withRecipes(helper, List.of(alloy, melting(100)), () -> {
            var manager = helper.getLevel().getRecipeManager();
            helper.assertTrue(
                    manager.getRecipes() == manager.getRecipes(),
                    "Recipe collection identity is stable between reloads");
            var crucible = crucible(helper);
            crucible.setItem(0, new ItemStack(Items.BRICK));
            crucible.setItem(1, new ItemStack(Items.CLAY_BALL));
            tick(crucible, 5);
            helper.assertTrue(crucible.progress() == 5, "Virtual melting satisfies the original alloy recipe");
            int checks = crucible.recipeChecks;
            replace(helper, melting(50));
            tick(crucible, 1);
            helper.assertTrue(
                    crucible.recipeChecks == checks + 1
                            && crucible.progress() == 0
                            && crucible.getItem(1).getCount() == 1,
                    "Replacing only the melting recipe invalidates a plan using the unchanged alloy recipe object");
            replace(helper, melting(100));
            replace(
                    helper,
                    new RecipeHolder<>(
                            ALLOY,
                            new AlloyingRecipe(
                                    List.of(fluidIngredient(), brickIngredient()),
                                    40,
                                    FluidStack.EMPTY,
                                    new ItemStack(Items.DIAMOND),
                                    false)));
            tick(crucible, 40);
            helper.assertTrue(
                    crucible.getItem(CrucibleBlockEntity.RESULT_SLOT).is(Items.DIAMOND)
                            && crucible.getItem(0).isEmpty()
                            && crucible.getItem(1).isEmpty(),
                    "Reloaded recipe duration and output replace cached work without extra ingredient consumption");
        });
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void tagReloadsInvalidatePlansButFuelChangesDoNot(GameTestHelper helper) {
        withRecipes(helper, List.of(itemRecipe(80)), () -> {
            var crucible = crucible(helper);
            inputs(crucible, 3);
            tick(crucible, 5);
            int checks = crucible.recipeChecks;
            crucible.getItem(CrucibleBlockEntity.FUEL_SLOT).shrink(1);
            tick(crucible, 1);
            helper.assertTrue(crucible.recipeChecks == checks, "Fuel edits do not rebuild an unchanged alloy plan");
            // A synthetic bus-wide reload would also run NeoForge's data-map loader without prepared reload data.
            CrucibleBlockEntity.tagsUpdated(
                    new TagsUpdatedEvent(helper.getLevel().registryAccess(), false, false));
            tick(crucible, 1);
            helper.assertTrue(
                    crucible.recipeChecks == checks + 1 && crucible.progress() == 7,
                    "Server tag reloads invalidate cached plans without resetting valid processing progress");
        });
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void runningBatchesAndProgressSurviveAutomationAndSaveLoad(GameTestHelper helper) {
        withRecipes(helper, List.of(itemRecipe(20)), () -> {
            for (boolean blast : new boolean[] {false, true}) {
                var level = helper.getLevel();
                var pos = helper.absolutePos(new BlockPos(blast ? 5 : 2, 2, 2));
                var state = (blast ? PoptartWorkstationBlocks.BLAST_FURNACE : PoptartWorkstationBlocks.CRUCIBLE)
                        .get()
                        .defaultBlockState();
                level.setBlock(pos, state, 2);
                if (blast)
                    level.setBlock(pos.above(), state.setValue(BlastFurnaceBlock.HALF, DoubleBlockHalf.UPPER), 2);
                var furnace = (CrucibleBlockEntity) level.getBlockEntity(pos);
                inputs(furnace, 3);
                tick(furnace, 4);
                furnace.getItem(0).grow(2);
                furnace.getItem(1).grow(2);
                tick(furnace, 1);
                int duration = blast ? 30 : 20;
                int batches = blast ? 3 : 1;
                var saved = furnace.saveWithFullMetadata(level.registryAccess());
                helper.assertTrue(
                        saved.getInt("CookTime") == 5
                                && saved.getInt("CookTimeTotal") == duration
                                && saved.getInt("CookingBatches") == batches,
                        "Automation preserves the running batch size and the existing processing duration");
                var loaded = blast
                        ? new BlastFurnaceBlockEntity(pos, level.getBlockState(pos))
                        : new CrucibleBlockEntity(pos, level.getBlockState(pos));
                loaded.setLevel(level);
                loaded.loadWithComponents(saved, level.registryAccess());
                level.setBlockEntity(loaded);
                tick(loaded, duration - 5);
                helper.assertTrue(
                        loaded.getItem(loaded.resultSlot).is(Items.FLINT)
                                && loaded.getItem(loaded.resultSlot).getCount() == batches
                                && loaded.getItem(0).getCount() == 5 - batches
                                && loaded.getItem(1).getCount() == 5 - batches,
                        "Both actual workstation types resume saved progress and complete only their original batches");
            }
        });
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void consumptionRecheckRejectsMaterialsChangedAfterPlanning(GameTestHelper helper) {
        withRecipes(helper, List.of(itemRecipe(1)), () -> {
            var crucible = crucible(helper);
            inputs(crucible, 1);
            crucible.beforeFuel = () -> crucible.getItem(0).setCount(0);
            tick(crucible, 1);
            helper.assertTrue(
                    crucible.getItem(CrucibleBlockEntity.RESULT_SLOT).isEmpty()
                            && crucible.getItem(1).getCount() == 1,
                    "A change after planning cannot produce output or consume remaining ingredients at completion");
        });
        helper.succeed();
    }

    private static RecipeHolder<AlloyingRecipe> itemRecipe(int duration) {
        return new RecipeHolder<>(
                ALLOY,
                new AlloyingRecipe(
                        List.of(
                                brickIngredient(),
                                new CrucibleIngredient(Ingredient.of(Items.STICK), FluidStack.EMPTY)),
                        duration,
                        FluidStack.EMPTY,
                        new ItemStack(Items.FLINT),
                        false));
    }

    private static CrucibleIngredient brickIngredient() {
        return new CrucibleIngredient(Ingredient.of(Items.BRICK), FluidStack.EMPTY);
    }

    private static CrucibleIngredient fluidIngredient() {
        return new CrucibleIngredient(Ingredient.EMPTY, new FluidStack(Fluids.WATER, 100));
    }

    private static RecipeHolder<MeltingRecipe> melting(int amount) {
        return new RecipeHolder<>(
                MELTING,
                new MeltingRecipe(Ingredient.of(Items.CLAY_BALL), 80, new FluidStack(Fluids.WATER, amount), false));
    }

    private static void withRecipes(GameTestHelper helper, List<RecipeHolder<?>> added, Runnable test) {
        var manager = helper.getLevel().getRecipeManager();
        var saved = List.copyOf(manager.getRecipes());
        var recipes = new ArrayList<>(saved);
        recipes.addAll(added);
        try {
            manager.replaceRecipes(recipes);
            test.run();
        } finally {
            manager.replaceRecipes(saved);
        }
    }

    private static void replace(GameTestHelper helper, RecipeHolder<?> replacement) {
        var manager = helper.getLevel().getRecipeManager();
        var recipes = new ArrayList<>(manager.getRecipes());
        recipes.removeIf(recipe -> recipe.id().equals(replacement.id()));
        recipes.add(replacement);
        manager.replaceRecipes(recipes);
    }

    private static CountingCrucible crucible(GameTestHelper helper) {
        var pos = helper.absolutePos(new BlockPos(2, 2, 2));
        var state = PoptartWorkstationBlocks.CRUCIBLE.get().defaultBlockState();
        helper.getLevel().setBlock(pos, state, 2);
        var crucible = new CountingCrucible(pos, state);
        crucible.setLevel(helper.getLevel());
        helper.getLevel().setBlockEntity(crucible);
        crucible.setItem(CrucibleBlockEntity.FUEL_SLOT, new ItemStack(Items.COAL, 8));
        return crucible;
    }

    private static void inputs(CrucibleBlockEntity furnace, int count) {
        furnace.setItem(0, new ItemStack(Items.BRICK, count));
        furnace.setItem(1, new ItemStack(Items.STICK, count));
        furnace.setItem(furnace.fuelSlot, new ItemStack(Items.COAL, 8));
    }

    private static void tick(CrucibleBlockEntity furnace, int ticks) {
        for (int tick = 0; tick < ticks; tick++)
            CrucibleBlockEntity.serverTick(furnace.getLevel(), furnace.getBlockPos(), furnace.getBlockState(), furnace);
    }

    private static final class CountingCrucible extends CrucibleBlockEntity {
        private int recipeChecks;
        private int planChecks;
        private Runnable beforeFuel;

        private CountingCrucible(BlockPos pos, BlockState state) {
            super(pos, state);
        }

        @Override
        protected Optional<RecipeHolder<AlloyingRecipe>> getAlloyingRecipe(Level level) {
            recipeChecks++;
            return super.getAlloyingRecipe(level);
        }

        @Override
        protected int limitAlloyingBatches(AlloyingRecipe recipe, int availableBatches) {
            planChecks++;
            return super.limitAlloyingBatches(recipe, availableBatches);
        }

        @Override
        protected int getBurnDuration(ItemStack fuel) {
            int duration = super.getBurnDuration(fuel);
            if (beforeFuel != null) {
                var action = beforeFuel;
                beforeFuel = null;
                action.run();
            }
            return duration;
        }

        private int progress() {
            return dataAccess.get(2);
        }
    }
}
