package dev.poptartking.poptartcore.client;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.client.model.ArmorClientItemExtensions;
import dev.poptartking.poptartcore.client.model.MiningHelmetModel;
import dev.poptartking.poptartcore.registry.PoptartCoreItems;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID, value = {Dist.CLIENT})
public class ClientEvents {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        MiningHelmetParticles.tick(Minecraft.getInstance());
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new ArmorClientItemExtensions(() -> {
            return PoptartCoreModelLayers.MINING_HELMET_MODEL;
        }), new Item[]{(Item) PoptartCoreItems.MINING_HELMET.get()});

    }

    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(PoptartCoreModelLayers.MINING_HELMET_LAYER, MiningHelmetModel::createBodyLayer);

    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        PoptartCoreModelLayers.MINING_HELMET_MODEL = new MiningHelmetModel(event.getEntityModels().bakeLayer(PoptartCoreModelLayers.MINING_HELMET_LAYER));
    }

}
