package dev.poptartking.poptartcore.client;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.client.model.MiningHelmetModel;
import dev.poptartking.poptartcore.client.model.PoptartCoreArmorModel;
import dev.poptartking.poptartcore.client.model.RawHideArmorModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PoptartCoreModelLayers {
    public static MiningHelmetModel MINING_HELMET_MODEL;
    public static RawHideArmorModel RAW_HIDE_ARMOR_MODEL;
    public static PoptartCoreArmorModel LEATHER_HELM_MODEL;
    public static PoptartCoreArmorModel LEATHER_TUNIC_SKIRTLESS_MODEL;
    public static PoptartCoreArmorModel LEATHER_PANTS_MODEL;
    public static PoptartCoreArmorModel LEATHER_BOOTS_MODEL;


    public static final ModelLayerLocation MINING_HELMET_LAYER = new ModelLayerLocation(PoptartCore.location("mining_helmet"), "main");
    public static final ModelLayerLocation RAW_HIDE_ARMOR_LAYER = new ModelLayerLocation(PoptartCore.location("raw_hide_armor"), "main");
    public static final ModelLayerLocation LEATHER_HELM_LAYER = new ModelLayerLocation(PoptartCore.location("leather_helm"), "main");
    public static final ModelLayerLocation LEATHER_TUNIC_SKIRTLESS_LAYER = new ModelLayerLocation(PoptartCore.location("leather_tunic_skirtless"), "main");
    public static final ModelLayerLocation LEATHER_PANTS_LAYER = new ModelLayerLocation(PoptartCore.location("leather_pants"), "main");
    public static final ModelLayerLocation LEATHER_BOOTS_LAYER = new ModelLayerLocation(PoptartCore.location("leather_boots"), "main");

}
