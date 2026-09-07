package dev.poptartking.poptartcore.client.model;

import dev.poptartking.poptartcore.client.PoptartCoreModelLayers;

public final class SteelArmorClientExtensions extends ArmorClientExtensions {

    public SteelArmorClientExtensions() {
        super(
                () -> PoptartCoreModelLayers.STEEL_HELMET_MODEL,
                () -> PoptartCoreModelLayers.STEEL_CHESTPLATE_MODEL,
                () -> PoptartCoreModelLayers.STEEL_LEGGINGS_MODEL,
                () -> PoptartCoreModelLayers.STEEL_BOOTS_MODEL);
    }
}
