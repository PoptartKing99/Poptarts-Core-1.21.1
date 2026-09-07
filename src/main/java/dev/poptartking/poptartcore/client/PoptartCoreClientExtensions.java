package dev.poptartking.poptartcore.client;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.fluid.MoltenFluidDefinition;
import dev.poptartking.poptartcore.registry.PoptartCoreFluids;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public class PoptartCoreClientExtensions {

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        registerMoltenFluid(event, "copper", PoptartCoreFluids.MOLTEN_COPPER);
        registerMoltenFluid(event, "tin", PoptartCoreFluids.MOLTEN_TIN);
        registerMoltenFluid(event, "bronze", PoptartCoreFluids.MOLTEN_BRONZE);
    }

    private static void registerMoltenFluid(
            RegisterClientExtensionsEvent event, String metal, MoltenFluidDefinition fluid) {
        ResourceLocation texture =
                ResourceLocation.fromNamespaceAndPath(PoptartCore.MOD_ID, "block/fluid/molten_" + metal);

        event.registerFluidType(
                new IClientFluidTypeExtensions() {

                    @Override
                    public ResourceLocation getStillTexture() {
                        return texture;
                    }

                    @Override
                    public ResourceLocation getFlowingTexture() {
                        return texture;
                    }
                },
                fluid.type().get());
    }
}
