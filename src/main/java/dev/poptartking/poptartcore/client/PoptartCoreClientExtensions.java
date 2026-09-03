package dev.poptartking.poptartcore.client;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreFluids;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(
        modid = PoptartCore.MOD_ID
)
public class PoptartCoreClientExtensions {

    @SubscribeEvent
    public static void registerClientExtensions(
            RegisterClientExtensionsEvent event
    ) {
        ResourceLocation moltenCopperTexture =
                ResourceLocation.fromNamespaceAndPath(
                        PoptartCore.MOD_ID,
                        "block/fluid/molten_copper"
                );

        event.registerFluidType(
                new IClientFluidTypeExtensions() {

                    @Override
                    public ResourceLocation getStillTexture() {
                        return moltenCopperTexture;
                    }

                    @Override
                    public ResourceLocation getFlowingTexture() {
                        return moltenCopperTexture;
                    }
                },
                PoptartCoreFluids.MOLTEN_COPPER.type().get()
        );
    }
}