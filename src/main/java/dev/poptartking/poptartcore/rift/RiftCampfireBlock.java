package dev.poptartking.poptartcore.rift;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.CampfireBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public class RiftCampfireBlock extends CampfireBlock {
    public RiftCampfireBlock(Properties properties) {
        super(false, 1, properties);
    }

    @SubscribeEvent
    public static void addCampfireBlockEntity(BlockEntityTypeAddBlocksEvent event) {
        event.modify(
                ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, ResourceLocation.withDefaultNamespace("campfire")),
                PoptartCoreBlocks.RIFT_CAMPFIRE.get());
    }
}
