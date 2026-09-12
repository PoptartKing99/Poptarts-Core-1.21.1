package dev.poptartking.poptartcore.integration.simulated;

import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.api.stress.BlockStressValues.GeneratedRpm;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import java.util.function.DoubleSupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public final class PortableEngineSetup {
    private PortableEngineSetup() {}

    @SubscribeEvent
    public static void addValidBlocks(BlockEntityTypeAddBlocksEvent event) {
        ResourceKey<BlockEntityType<?>> key = ResourceKey.create(
                Registries.BLOCK_ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("simulated", "portable_engine"));
        event.modify(key, PoptartCoreBlocks.PORTABLE_ENGINE.get());
    }

    @SubscribeEvent
    public static void registerStress(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Block engine = PoptartCoreBlocks.PORTABLE_ENGINE.get();
            BlockStressValues.CAPACITIES.register(engine, (DoubleSupplier) () -> 64.0);
            BlockStressValues.RPM.register(engine, new GeneratedRpm(32, false));
            Item item = engine.asItem();
            TooltipModifier.REGISTRY.register(item, KineticStats.create(item));
        });
    }
}
