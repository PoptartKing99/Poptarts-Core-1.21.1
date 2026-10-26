package dev.poptartking.poptartcore.timekeeping;

import com.mojang.serialization.Codec;
import dev.poptartking.poptartcore.PoptartCore;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TimekeepingComponents {
    private static final DeferredRegister<DataComponentType<?>> TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, PoptartCore.MOD_ID);
    public static final Supplier<DataComponentType<Long>> STOPWATCH =
            TYPES.register("stopwatch_start", () -> DataComponentType.<Long>builder()
                    .persistent(Codec.LONG)
                    .networkSynchronized(ByteBufCodecs.VAR_LONG)
                    .build());

    private TimekeepingComponents() {}

    public static void register(IEventBus bus) {
        TYPES.register(bus);
    }
}
