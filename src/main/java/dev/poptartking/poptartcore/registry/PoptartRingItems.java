package dev.poptartking.poptartcore.registry;

import dev.poptartking.poptartcore.catalog.CatalogItemDefinition;
import dev.poptartking.poptartcore.catalog.PoptartCatalog;

public final class PoptartRingItems {
    private PoptartRingItems() {}

    private static CatalogItemDefinition<dev.poptartking.poptartcore.equipment.EquipmentRing> ring(
            String id, dev.poptartking.poptartcore.equipment.EquipmentRing.Kind kind) {
        return PoptartCatalog.item(id, () -> new dev.poptartking.poptartcore.equipment.EquipmentRing(kind))
                .withoutGeneratedModel();
    }

    public static final CatalogItemDefinition<dev.poptartking.poptartcore.equipment.EquipmentRing> DULL_IRON_RING =
            ring("dull_iron_ring", dev.poptartking.poptartcore.equipment.EquipmentRing.Kind.DULL_IRON);

    public static final CatalogItemDefinition<dev.poptartking.poptartcore.equipment.EquipmentRing> MOONLIGHT_RING =
            ring("moonlight_ring", dev.poptartking.poptartcore.equipment.EquipmentRing.Kind.MOONLIGHT);

    public static final CatalogItemDefinition<dev.poptartking.poptartcore.equipment.EquipmentRing> RING_OF_WARMTH =
            ring("ring_of_warmth", dev.poptartking.poptartcore.equipment.EquipmentRing.Kind.WARMTH);

    public static final CatalogItemDefinition<dev.poptartking.poptartcore.equipment.EquipmentRing> FIREWARD_RING =
            ring("fireward_ring", dev.poptartking.poptartcore.equipment.EquipmentRing.Kind.FIREWARD);

    public static final CatalogItemDefinition<dev.poptartking.poptartcore.equipment.EquipmentRing> SILVERCAT_RING =
            ring("silvercat_ring", dev.poptartking.poptartcore.equipment.EquipmentRing.Kind.SILVERCAT);

    public static final CatalogItemDefinition<dev.poptartking.poptartcore.equipment.EquipmentRing> RUSTED_IRON_RING =
            ring("rusted_iron_ring", dev.poptartking.poptartcore.equipment.EquipmentRing.Kind.RUSTED_IRON);

    public static final CatalogItemDefinition<dev.poptartking.poptartcore.equipment.EquipmentRing> TINY_HORSESHOE =
            ring("tiny_horseshoe", dev.poptartking.poptartcore.equipment.EquipmentRing.Kind.TINY_HORSESHOE);
    public static final CatalogItemDefinition<dev.poptartking.poptartcore.equipment.EquipmentRing> THORN_RING =
            ring("thorn_ring", dev.poptartking.poptartcore.equipment.EquipmentRing.Kind.THORN);
    public static final CatalogItemDefinition<dev.poptartking.poptartcore.equipment.EquipmentRing>
            RING_OF_THE_AFFLICTED =
                    ring("ring_of_the_afflicted", dev.poptartking.poptartcore.equipment.EquipmentRing.Kind.AFFLICTED);
    public static final CatalogItemDefinition<dev.poptartking.poptartcore.equipment.EquipmentRing>
            MERCURY_GEMSTONE_RING =
                    ring("mercury_gemstone_ring", dev.poptartking.poptartcore.equipment.EquipmentRing.Kind.MERCURY);
    public static final CatalogItemDefinition<dev.poptartking.poptartcore.equipment.EquipmentRing> ANCIENT_BLESSING =
            ring("ancient_blessing", dev.poptartking.poptartcore.equipment.EquipmentRing.Kind.ANCIENT_BLESSING);
    public static final CatalogItemDefinition<dev.poptartking.poptartcore.equipment.EquipmentRing> SUNLIGHT_RING =
            ring("sunlight_ring", dev.poptartking.poptartcore.equipment.EquipmentRing.Kind.SUNLIGHT);
    public static final CatalogItemDefinition<dev.poptartking.poptartcore.equipment.EquipmentRing> MALEVOLENT_RING =
            ring("malevolent_ring", dev.poptartking.poptartcore.equipment.EquipmentRing.Kind.MALEVOLENT);

    public static void initialize() {}

    public static final CatalogItemDefinition<dev.poptartking.poptartcore.equipment.EquipmentRing> BULWARK_RING =
            ring("bulwark_ring", dev.poptartking.poptartcore.equipment.EquipmentRing.Kind.BULWARK);
    public static final CatalogItemDefinition<dev.poptartking.poptartcore.equipment.EquipmentRing> RING_OF_THE_RAT =
            ring("ring_of_the_rat", dev.poptartking.poptartcore.equipment.EquipmentRing.Kind.RAT);
    public static final CatalogItemDefinition<dev.poptartking.poptartcore.equipment.EquipmentRing>
            SULFUR_GEMSTONE_RING =
                    ring("sulfur_gemstone_ring", dev.poptartking.poptartcore.equipment.EquipmentRing.Kind.SULFUR);
    public static final CatalogItemDefinition<dev.poptartking.poptartcore.equipment.EquipmentRing> RING_OF_SILENCE =
            ring("ring_of_silence", dev.poptartking.poptartcore.equipment.EquipmentRing.Kind.SILENCE);

    public static final CatalogItemDefinition<dev.poptartking.poptartcore.equipment.EquipmentRing> MAGNET_RING =
            ring("magnet_ring", dev.poptartking.poptartcore.equipment.EquipmentRing.Kind.MAGNET);
}
