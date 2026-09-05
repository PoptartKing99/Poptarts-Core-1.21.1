package dev.poptartking.poptartcore;

import com.mojang.logging.LogUtils;
import dev.poptartking.poptartcore.registry.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(PoptartCore.MOD_ID)
public class PoptartCore {

    public static final String MOD_ID = "poptartcore";
    public static final Logger LOGGER = LogUtils.getLogger();

    public PoptartCore(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);
        modContainer.registerConfig(ModConfig.Type.COMMON, PoptartCoreConfig.SPEC);

        PoptartCoreBlocks.register(modEventBus);
        PoptartCoreItems.register(modEventBus);
        PoptartCoreBlockEntities.register(modEventBus);
        PoptartCoreMenus.register(modEventBus);
        PoptartCoreFluids.register(modEventBus);
        PoptartCoreRecipes.register(modEventBus);
        PoptartCoreTabs.register(modEventBus);
    }

    private void commonSetup(FMLCommonSetupEvent event) {}

    private void addCreative(BuildCreativeModeTabContentsEvent event) {}

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {}

    public static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
