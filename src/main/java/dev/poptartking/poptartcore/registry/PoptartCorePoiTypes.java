package dev.poptartking.poptartcore.registry;

import dev.poptartking.poptartcore.PoptartCore;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PoptartCorePoiTypes {
    private static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, PoptartCore.MOD_ID);

    public static final DeferredHolder<PoiType, PoiType> LIFEBUD = POI_TYPES.register(
            "lifebud",
            () -> new PoiType(
                    Set.copyOf(
                            PoptartCoreBlocks.LIFEBUD.get().getStateDefinition().getPossibleStates()),
                    0,
                    1));

    private PoptartCorePoiTypes() {}

    public static void register(IEventBus eventBus) {
        POI_TYPES.register(eventBus);
    }
}
