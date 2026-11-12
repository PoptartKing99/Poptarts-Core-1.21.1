package dev.poptartking.poptartcore.registry;

import com.mojang.serialization.Codec;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.anvil.UpgradeDurability;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AnvilComponents {
    private static final DeferredRegister<DataComponentType<?>> TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, PoptartCore.MOD_ID);
    public static final Supplier<DataComponentType<Integer>> UPGRADE_COUNT =
            TYPES.register("upgrade_count", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.intRange(1, 3))
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());
    public static final Supplier<DataComponentType<UpgradeDurability>> UPGRADE_DURABILITY =
            TYPES.register("upgrade_durability", () -> DataComponentType.<UpgradeDurability>builder()
                    .persistent(UpgradeDurability.CODEC)
                    .networkSynchronized(UpgradeDurability.STREAM_CODEC)
                    .build());

    private AnvilComponents() {}

    public static void register(IEventBus bus) {
        TYPES.register(bus);
    }
}
