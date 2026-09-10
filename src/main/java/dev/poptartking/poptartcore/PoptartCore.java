package dev.poptartking.poptartcore;

import dev.poptartking.poptartcore.blastfurnace.BlastFurnaceCapabilities;
import dev.poptartking.poptartcore.registry.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(PoptartCore.MOD_ID)
public class PoptartCore {

    public static final String MOD_ID = "poptartcore";

    public PoptartCore(IEventBus modEventBus) {
        PoptartCoreBlocks.register(modEventBus);
        PoptartCoreItems.register(modEventBus);
        PoptartCoreBlockEntities.register(modEventBus);
        PoptartCoreMenus.register(modEventBus);
        PoptartCoreFluids.register(modEventBus);
        PoptartCoreRecipes.register(modEventBus);
        PoptartCoreSounds.register(modEventBus);
        PoptartCoreTabs.register(modEventBus);
        BlastFurnaceCapabilities.register(modEventBus);
    }

    public static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
