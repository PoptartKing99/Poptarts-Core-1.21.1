package dev.poptartking.poptartcore.client;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.client.model.*;
import dev.poptartking.poptartcore.crucible.CrucibleScreen;
import dev.poptartking.poptartcore.registry.PoptartCoreItems;
import dev.poptartking.poptartcore.registry.PoptartCoreMenus;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID, value = {Dist.CLIENT})
public class ClientEvents {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        MiningHelmetParticles.tick(Minecraft.getInstance());
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(
                new ArmorClientItemExtensions(() -> PoptartCoreModelLayers.MINING_HELMET_MODEL),
                PoptartCoreItems.MINING_HELMET.get()
        );

        event.registerItem(
                new ArmorClientItemExtensions(() -> PoptartCoreModelLayers.RAW_HIDE_ARMOR_MODEL),
                PoptartCoreItems.RAW_HIDE_HELMET.get(),
                PoptartCoreItems.RAW_HIDE_CHESTPLATE.get(),
                PoptartCoreItems.RAW_HIDE_LEGGINGS.get()
        );
        event.registerItem(new LeatherArmorClientExtensions(), new Item[]{Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS});

    }

    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(PoptartCoreModelLayers.MINING_HELMET_LAYER, MiningHelmetModel::createBodyLayer);
        event.registerLayerDefinition(PoptartCoreModelLayers.RAW_HIDE_ARMOR_LAYER, RawHideArmorModel::createBodyLayer);
        event.registerLayerDefinition(PoptartCoreModelLayers.LEATHER_HELM_LAYER, LeatherArmorModels::helm);
        event.registerLayerDefinition(PoptartCoreModelLayers.LEATHER_TUNIC_SKIRTLESS_LAYER, LeatherArmorModels::tunicSkirtless);
        event.registerLayerDefinition(PoptartCoreModelLayers.LEATHER_PANTS_LAYER, LeatherArmorModels::pants);
        event.registerLayerDefinition(PoptartCoreModelLayers.LEATHER_BOOTS_LAYER, LeatherArmorModels::boots);


    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        PoptartCoreModelLayers.MINING_HELMET_MODEL = new MiningHelmetModel(event.getEntityModels().bakeLayer(PoptartCoreModelLayers.MINING_HELMET_LAYER));
        PoptartCoreModelLayers.RAW_HIDE_ARMOR_MODEL = new RawHideArmorModel(event.getEntityModels().bakeLayer(PoptartCoreModelLayers.RAW_HIDE_ARMOR_LAYER));
        PoptartCoreModelLayers.LEATHER_HELM_MODEL = new PoptartCoreArmorModel(event.getEntityModels().bakeLayer(PoptartCoreModelLayers.LEATHER_HELM_LAYER));
        PoptartCoreModelLayers.LEATHER_TUNIC_SKIRTLESS_MODEL = new PoptartCoreArmorModel(event.getEntityModels().bakeLayer(PoptartCoreModelLayers.LEATHER_TUNIC_SKIRTLESS_LAYER));
        PoptartCoreModelLayers.LEATHER_PANTS_MODEL = new PoptartCoreArmorModel(event.getEntityModels().bakeLayer(PoptartCoreModelLayers.LEATHER_PANTS_LAYER));
        PoptartCoreModelLayers.LEATHER_BOOTS_MODEL = new PoptartCoreArmorModel(event.getEntityModels().bakeLayer(PoptartCoreModelLayers.LEATHER_BOOTS_LAYER));
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(
                PoptartCoreMenus.CRUCIBLE.get(),
                CrucibleScreen::new
        );
    }
}
