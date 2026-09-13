package dev.poptartking.poptartcore.registry;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.milk.MilkRecharge;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import org.jetbrains.annotations.Nullable;

public final class PoptartCoreDataMaps {
    public static final DataMapType<EntityType<?>, MilkRecharge> MILK_RECHARGE = DataMapType.builder(
                    PoptartCore.location("milk_recharge"), Registries.ENTITY_TYPE, MilkRecharge.CODEC)
            .build();

    private PoptartCoreDataMaps() {}

    public static void register(IEventBus eventBus) {
        eventBus.addListener(PoptartCoreDataMaps::registerDataMaps);
    }

    private static void registerDataMaps(RegisterDataMapTypesEvent event) {
        event.register(MILK_RECHARGE);
    }

    @Nullable
    public static MilkRecharge milkRecharge(EntityType<?> type) {
        return BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(type).getData(MILK_RECHARGE);
    }
}
