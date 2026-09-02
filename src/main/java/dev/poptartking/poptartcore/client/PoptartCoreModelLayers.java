package dev.poptartking.poptartcore.client;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.client.model.MiningHelmetModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PoptartCoreModelLayers {
    public static MiningHelmetModel MINING_HELMET_MODEL;

    public static final ModelLayerLocation MINING_HELMET_LAYER = new ModelLayerLocation(PoptartCore.location("mining_helmet"), "main");

}
