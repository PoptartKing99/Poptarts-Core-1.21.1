package dev.poptartking.poptartcore.client.model;

import dev.poptartking.poptartcore.client.PoptartCoreModelLayers;

public final class IronArmorClientExtensions extends ArmorClientExtensions {

    public IronArmorClientExtensions() {
        super(
                () -> PoptartCoreModelLayers.IRON_HELMET_MODEL,
                () -> PoptartCoreModelLayers.IRON_CHESTPLATE_MODEL,
                () -> PoptartCoreModelLayers.IRON_LEGGINGS_MODEL,
                () -> PoptartCoreModelLayers.IRON_BOOTS_MODEL);
    }
}
