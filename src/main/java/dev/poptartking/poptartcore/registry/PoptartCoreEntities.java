package dev.poptartking.poptartcore.registry;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.waxgolem.WaxGolem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PoptartCoreEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, PoptartCore.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<WaxGolem>> WAX_GOLEM =
            ENTITY_TYPES.register("wax_golem", () -> EntityType.Builder.of(WaxGolem::new, MobCategory.MISC)
                    .sized(0.8F, 1.9F)
                    .eyeHeight(1.6F)
                    .clientTrackingRange(8)
                    .build("wax_golem"));

    private PoptartCoreEntities() {}

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
