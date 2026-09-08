package dev.poptartking.poptartcore.client;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.blastfurnace.BlastFurnaceScreen;
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
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(
        modid = PoptartCore.MOD_ID,
        value = {Dist.CLIENT})
public class ClientEvents {

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        if (event.getMultiPlayerGameMode() instanceof ClientHammerMiningCleanup cleanup) {
            cleanup.poptartcore$clearHammerMining();
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        MiningHelmetParticles.tick(Minecraft.getInstance());
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(
                new ArmorClientItemExtensions(() -> PoptartCoreModelLayers.MINING_HELMET_MODEL),
                PoptartCoreItems.MINING_HELMET.get());

        event.registerItem(
                new ArmorClientItemExtensions(() -> PoptartCoreModelLayers.RAW_HIDE_ARMOR_MODEL),
                PoptartCoreItems.RAW_HIDE_HELMET.get(),
                PoptartCoreItems.RAW_HIDE_CHESTPLATE.get(),
                PoptartCoreItems.RAW_HIDE_LEGGINGS.get());

        event.registerItem(new LeatherArmorClientExtensions(), new Item[] {
            Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS
        });

        event.registerItem(
                ArmorClientExtensions.iron(),
                new Item[] {Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS});

        event.registerItem(
                ArmorClientExtensions.steel(),
                PoptartCoreItems.STEEL_HELMET.get(),
                PoptartCoreItems.STEEL_CHESTPLATE.get(),
                PoptartCoreItems.STEEL_LEGGINGS.get(),
                PoptartCoreItems.STEEL_BOOTS.get());
    }

    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(PoptartCoreModelLayers.MINING_HELMET_LAYER, MiningHelmetModel::createBodyLayer);
        event.registerLayerDefinition(PoptartCoreModelLayers.RAW_HIDE_ARMOR_LAYER, RawHideArmorModel::createBodyLayer);
        event.registerLayerDefinition(PoptartCoreModelLayers.LEATHER_HELM_LAYER, LeatherArmorModels::helm);
        event.registerLayerDefinition(
                PoptartCoreModelLayers.LEATHER_TUNIC_SKIRTLESS_LAYER, LeatherArmorModels::tunicSkirtless);
        event.registerLayerDefinition(PoptartCoreModelLayers.LEATHER_PANTS_LAYER, LeatherArmorModels::pants);
        event.registerLayerDefinition(PoptartCoreModelLayers.LEATHER_BOOTS_LAYER, LeatherArmorModels::boots);
        event.registerLayerDefinition(PoptartCoreModelLayers.IRON_HELMET_LAYER, MetalArmorModels::helmet);
        event.registerLayerDefinition(PoptartCoreModelLayers.IRON_CHESTPLATE_LAYER, MetalArmorModels::chestplate);
        event.registerLayerDefinition(PoptartCoreModelLayers.IRON_LEGGINGS_LAYER, MetalArmorModels::leggings);
        event.registerLayerDefinition(PoptartCoreModelLayers.IRON_BOOTS_LAYER, MetalArmorModels::boots);
        event.registerLayerDefinition(PoptartCoreModelLayers.STEEL_HELMET_LAYER, MetalArmorModels::helmet);
        event.registerLayerDefinition(PoptartCoreModelLayers.STEEL_CHESTPLATE_LAYER, MetalArmorModels::chestplate);
        event.registerLayerDefinition(PoptartCoreModelLayers.STEEL_LEGGINGS_LAYER, MetalArmorModels::leggings);
        event.registerLayerDefinition(PoptartCoreModelLayers.STEEL_BOOTS_LAYER, MetalArmorModels::boots);
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        PoptartCoreModelLayers.MINING_HELMET_MODEL =
                new MiningHelmetModel(event.getEntityModels().bakeLayer(PoptartCoreModelLayers.MINING_HELMET_LAYER));

        PoptartCoreModelLayers.RAW_HIDE_ARMOR_MODEL =
                new RawHideArmorModel(event.getEntityModels().bakeLayer(PoptartCoreModelLayers.RAW_HIDE_ARMOR_LAYER));

        PoptartCoreModelLayers.LEATHER_HELM_MODEL =
                new PoptartCoreArmorModel(event.getEntityModels().bakeLayer(PoptartCoreModelLayers.LEATHER_HELM_LAYER));

        PoptartCoreModelLayers.LEATHER_TUNIC_SKIRTLESS_MODEL = new PoptartCoreArmorModel(
                event.getEntityModels().bakeLayer(PoptartCoreModelLayers.LEATHER_TUNIC_SKIRTLESS_LAYER));

        PoptartCoreModelLayers.LEATHER_PANTS_MODEL = new PoptartCoreArmorModel(
                event.getEntityModels().bakeLayer(PoptartCoreModelLayers.LEATHER_PANTS_LAYER));

        PoptartCoreModelLayers.LEATHER_BOOTS_MODEL = new PoptartCoreArmorModel(
                event.getEntityModels().bakeLayer(PoptartCoreModelLayers.LEATHER_BOOTS_LAYER));

        PoptartCoreModelLayers.IRON_HELMET_MODEL =
                new PoptartCoreArmorModel(event.getEntityModels().bakeLayer(PoptartCoreModelLayers.IRON_HELMET_LAYER));

        PoptartCoreModelLayers.IRON_CHESTPLATE_MODEL = new PoptartCoreArmorModel(
                event.getEntityModels().bakeLayer(PoptartCoreModelLayers.IRON_CHESTPLATE_LAYER));

        PoptartCoreModelLayers.IRON_LEGGINGS_MODEL = new PoptartCoreArmorModel(
                event.getEntityModels().bakeLayer(PoptartCoreModelLayers.IRON_LEGGINGS_LAYER));

        PoptartCoreModelLayers.IRON_BOOTS_MODEL =
                new PoptartCoreArmorModel(event.getEntityModels().bakeLayer(PoptartCoreModelLayers.IRON_BOOTS_LAYER));

        PoptartCoreModelLayers.STEEL_HELMET_MODEL =
                new PoptartCoreArmorModel(event.getEntityModels().bakeLayer(PoptartCoreModelLayers.STEEL_HELMET_LAYER));

        PoptartCoreModelLayers.STEEL_CHESTPLATE_MODEL = new PoptartCoreArmorModel(
                event.getEntityModels().bakeLayer(PoptartCoreModelLayers.STEEL_CHESTPLATE_LAYER));

        PoptartCoreModelLayers.STEEL_LEGGINGS_MODEL = new PoptartCoreArmorModel(
                event.getEntityModels().bakeLayer(PoptartCoreModelLayers.STEEL_LEGGINGS_LAYER));

        PoptartCoreModelLayers.STEEL_BOOTS_MODEL =
                new PoptartCoreArmorModel(event.getEntityModels().bakeLayer(PoptartCoreModelLayers.STEEL_BOOTS_LAYER));
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(PoptartCoreMenus.CRUCIBLE.get(), CrucibleScreen::new);
        event.register(PoptartCoreMenus.BLAST_FURNACE.get(), BlastFurnaceScreen::new);
    }
}
