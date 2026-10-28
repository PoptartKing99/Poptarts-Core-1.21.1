package dev.poptartking.poptartcore.backpack.client;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.backpack.BackpackBlockEntity;
import dev.poptartking.poptartcore.backpack.BackpackColors;
import dev.poptartking.poptartcore.registry.PoptartEquipmentBlocks;
import net.minecraft.core.component.DataComponents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class BackpackDyeClient {
    private BackpackDyeClient() {}

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for (var block :
                    java.util.List.of(PoptartEquipmentBlocks.BACKPACK.get(), PoptartEquipmentBlocks.RUCKSACK.get())) {
                net.minecraft.client.renderer.item.ItemProperties.register(
                        block.asItem(),
                        PoptartCore.location("dyed"),
                        (stack, level, entity, seed) -> (stack.has(DataComponents.DYED_COLOR) ? 1 : 0)
                                + (stack.has(BackpackColors.BODY.get()) ? 2 : 0));
            }
        });
    }

    @SubscribeEvent
    public static void itemColors(RegisterColorHandlersEvent.Item event) {
        event.register(
                (stack, index) -> index == 0 || index == 1
                        ? 0xFF000000
                                | BackpackColors.color(
                                        stack, index == 1 ? BackpackColors.Part.BODY : BackpackColors.Part.FLAPS)
                        : -1,
                PoptartEquipmentBlocks.BACKPACK.get().asItem(),
                PoptartEquipmentBlocks.RUCKSACK.get().asItem());
    }

    @SubscribeEvent
    public static void blockColors(RegisterColorHandlersEvent.Block event) {
        event.register(
                (state, level, pos, index) -> level != null
                                && pos != null
                                && level.getBlockEntity(pos) instanceof BackpackBlockEntity backpack
                        ? backpack.dyeColor(index)
                        : 0xFFFFFF,
                PoptartEquipmentBlocks.BACKPACK.get(),
                PoptartEquipmentBlocks.RUCKSACK.get());
    }
}
