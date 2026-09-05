package dev.poptartking.poptartcore.registry;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.fluid.MoltenFluidDefinition;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class PoptartCoreFluids {

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, PoptartCore.MOD_ID);

    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, PoptartCore.MOD_ID);

    public static final MoltenFluidDefinition MOLTEN_COPPER = registerMoltenMetal("copper");

    private static MoltenFluidDefinition registerMoltenMetal(String name) {
        String fluidName = "molten_" + name;

        DeferredHolder<FluidType, FluidType> type = FLUID_TYPES.register(
                fluidName,
                () -> new FluidType(
                        FluidType.Properties.create().viscosity(2000).density(1400)));

        FluidReference sourceReference = new FluidReference();
        FluidReference flowingReference = new FluidReference();

        DeferredHolder<Fluid, FlowingFluid> source = FLUIDS.register(
                fluidName,
                () -> new BaseFlowingFluid.Source(
                        new BaseFlowingFluid.Properties(type, sourceReference, flowingReference)));

        DeferredHolder<Fluid, FlowingFluid> flowing = FLUIDS.register(
                "flowing_" + fluidName,
                () -> new BaseFlowingFluid.Flowing(
                        new BaseFlowingFluid.Properties(type, sourceReference, flowingReference)));

        sourceReference.set(source);
        flowingReference.set(flowing);

        return new MoltenFluidDefinition(type, source, flowing);
    }

    private static class FluidReference implements Supplier<FlowingFluid> {

        private Supplier<? extends FlowingFluid> supplier;

        public void set(Supplier<? extends FlowingFluid> supplier) {
            this.supplier = supplier;
        }

        @Override
        public FlowingFluid get() {
            return supplier.get();
        }
    }

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
        FLUIDS.register(eventBus);
    }
}
