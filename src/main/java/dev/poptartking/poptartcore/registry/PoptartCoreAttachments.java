package dev.poptartking.poptartcore.registry;

import com.mojang.serialization.Codec;
import dev.poptartking.poptartcore.PoptartCore;
import java.util.function.Supplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class PoptartCoreAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, PoptartCore.MOD_ID);

    public static final Supplier<AttachmentType<Long>> MILK_FULL_AT = ATTACHMENT_TYPES.register(
            "milk_full_at",
            () -> AttachmentType.builder(() -> 0L).serialize(Codec.LONG).build());

    private PoptartCoreAttachments() {}

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
