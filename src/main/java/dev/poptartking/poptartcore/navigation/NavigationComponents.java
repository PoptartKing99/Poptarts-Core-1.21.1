package dev.poptartking.poptartcore.navigation;

import dev.poptartking.poptartcore.PoptartCore;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NavigationComponents {
    public static final int MAX_LODESTONES = 12;
    private static final DeferredRegister<DataComponentType<?>> TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, PoptartCore.MOD_ID);
    public static final Supplier<DataComponentType<CompassWaypoints>> WAYPOINTS =
            TYPES.register("compass_waypoints", () -> DataComponentType.<CompassWaypoints>builder()
                    .persistent(CompassWaypoints.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(CompassWaypoints.CODEC))
                    .build());

    private NavigationComponents() {}

    public static void register(IEventBus bus) {
        TYPES.register(bus);
    }

    public static void write(ItemStack compass, CompassWaypoints data) {
        compass.set(WAYPOINTS.get(), data);
    }
}
