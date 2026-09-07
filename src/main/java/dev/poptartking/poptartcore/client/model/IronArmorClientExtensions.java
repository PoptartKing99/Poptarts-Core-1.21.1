package dev.poptartking.poptartcore.client.model;

import dev.poptartking.poptartcore.client.PoptartCoreModelLayers;

public final class IronArmorClientExtensions extends PlateArmorClientExtensions {

    public IronArmorClientExtensions() {
        super(
                () -> PoptartCoreModelLayers.IRON_PLATE_HELMET_MODEL,
                () -> PoptartCoreModelLayers.IRON_PLATE_CHESTPLATE_MODEL,
                () -> PoptartCoreModelLayers.IRON_PLATE_LEGGINGS_MODEL,
                () -> PoptartCoreModelLayers.IRON_PLATE_BOOTS_MODEL);
    }
}
