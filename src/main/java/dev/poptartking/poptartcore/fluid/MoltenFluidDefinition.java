package dev.poptartking.poptartcore.fluid;

import net.minecraft.world.level.material.FlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;

public record MoltenFluidDefinition(
        DeferredHolder<FluidType, FluidType> type,
        DeferredHolder<net.minecraft.world.level.material.Fluid, FlowingFluid> source,
        DeferredHolder<net.minecraft.world.level.material.Fluid, FlowingFluid> flowing
) {
}