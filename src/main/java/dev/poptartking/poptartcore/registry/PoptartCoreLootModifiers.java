package dev.poptartking.poptartcore.registry;

import com.mojang.serialization.MapCodec;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.loot.ReplaceOreDropLootModifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class PoptartCoreLootModifiers {
    private static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, PoptartCore.MOD_ID);

    static {
        MODIFIERS.register("replace_ore_drop", () -> ReplaceOreDropLootModifier.CODEC);
    }

    private PoptartCoreLootModifiers() {}

    public static void register(IEventBus eventBus) {
        MODIFIERS.register(eventBus);
    }
}
