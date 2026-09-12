package dev.poptartking.poptartcore.mixin.integration.simulated;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.poptartking.poptartcore.registry.PoptartCoreTags;
import dev.simulated_team.simulated.content.blocks.portable_engine.PortableEngineBlockEntity;
import dev.simulated_team.simulated.service.SimItemService;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PortableEngineBlockEntity.class)
public class PortableEngineFuelMixin {
    @WrapOperation(
            method = "tick",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Ldev/simulated_team/simulated/service/SimItemService;getBurnTime(Lnet/minecraft/world/item/ItemStack;)I",
                            ordinal = 0))
    private int poptartcore$onlyAllowedFuel(SimItemService instance, ItemStack stack, Operation<Integer> original) {
        return stack.is(PoptartCoreTags.PORTABLE_ENGINE_ALLOWED_FUEL) ? original.call(instance, stack) : 0;
    }
}
