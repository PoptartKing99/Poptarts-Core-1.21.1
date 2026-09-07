package dev.poptartking.poptartcore.client.model;

import dev.poptartking.poptartcore.client.PoptartCoreModelLayers;

public final class SteelArmorClientExtensions extends PlateArmorClientExtensions {

    public SteelArmorClientExtensions() {
        super(
                () -> PoptartCoreModelLayers.STEEL_PLATE_HELMET_MODEL,
                () -> PoptartCoreModelLayers.STEEL_PLATE_CHESTPLATE_MODEL,
                () -> PoptartCoreModelLayers.STEEL_PLATE_LEGGINGS_MODEL,
                () -> PoptartCoreModelLayers.STEEL_PLATE_BOOTS_MODEL);
    }
}
