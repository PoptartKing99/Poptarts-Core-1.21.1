package dev.poptartking.poptartcore.registry;

import com.mojang.serialization.Codec;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.equipment.AccessoryInventory;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class PoptartCoreAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, PoptartCore.MOD_ID);

    public static final Supplier<AttachmentType<AccessoryInventory>> ACCESSORIES = ATTACHMENT_TYPES.register(
            "accessories", () -> AttachmentType.<CompoundTag, AccessoryInventory>serializable(AccessoryInventory::new)
                    .build());

    public static final Supplier<AttachmentType<Boolean>> CORPSE_DEATH_MARKER_REMOVED =
            ATTACHMENT_TYPES.register("corpse_death_marker_removed", () -> AttachmentType.builder(() -> false)
                    .sync(net.minecraft.network.codec.ByteBufCodecs.BOOL)
                    .build());

    // Visual/air snapshot only; authoritative storage remains in ACCESSORIES.
    public static final Supplier<AttachmentType<Boolean>> BACKPACK_ATTACHMENT_WIELDING =
            ATTACHMENT_TYPES.register("backpack_attachment_wielding", () -> AttachmentType.builder(() -> false)
                    .sync(net.minecraft.network.codec.ByteBufCodecs.BOOL)
                    .build());

    public static final Supplier<AttachmentType<net.minecraft.world.item.ItemStack>> BASIC_BACKPACK_VISUAL =
            ATTACHMENT_TYPES.register("basic_backpack_visual", () -> AttachmentType.builder(
                            () -> net.minecraft.world.item.ItemStack.EMPTY)
                    .sync(net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC)
                    .build());

    public static final Supplier<AttachmentType<net.minecraft.world.item.ItemStack>> BACKTANK_VISUAL =
            ATTACHMENT_TYPES.register(
                    "backtank_visual", () -> AttachmentType.builder(() -> net.minecraft.world.item.ItemStack.EMPTY)
                            .sync(net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC)
                            .build());

    public static final Supplier<AttachmentType<net.minecraft.world.item.ItemStack>> BACKPACK_VISUAL =
            ATTACHMENT_TYPES.register(
                    "backpack_visual", () -> AttachmentType.builder(() -> net.minecraft.world.item.ItemStack.EMPTY)
                            .sync(net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC)
                            .build());

    public static final Supplier<AttachmentType<Long>> MILK_FULL_AT = ATTACHMENT_TYPES.register(
            "milk_full_at",
            () -> AttachmentType.builder(() -> 0L).serialize(Codec.LONG).build());

    public static final Supplier<AttachmentType<net.minecraft.world.item.ItemStack>> CAPE_VISUAL =
            ATTACHMENT_TYPES.register(
                    "cape_visual", () -> AttachmentType.builder(() -> net.minecraft.world.item.ItemStack.EMPTY)
                            .sync(net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC)
                            .build());

    public static final Supplier<AttachmentType<Integer>> MAX_HEARTS = ATTACHMENT_TYPES.register(
            "max_hearts",
            () -> AttachmentType.builder(() -> 10).serialize(Codec.INT).build());

    public static final Supplier<AttachmentType<net.minecraft.world.item.ItemStack>> ARMOR_STAND_CAPE =
            ATTACHMENT_TYPES.register(
                    "armor_stand_cape", () -> AttachmentType.builder(() -> net.minecraft.world.item.ItemStack.EMPTY)
                            .serialize(net.minecraft.world.item.ItemStack.OPTIONAL_CODEC)
                            .sync(net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC)
                            .build());

    public static final Supplier<AttachmentType<Long>> TOMBSTONE_ANCHOR =
            ATTACHMENT_TYPES.register("tombstone_anchor", () -> AttachmentType.builder(() -> Long.MIN_VALUE)
                    .serialize(Codec.LONG)
                    .build());

    private PoptartCoreAttachments() {}

    public static final Supplier<AttachmentType<Boolean>> SILENCED =
            ATTACHMENT_TYPES.register("silenced", () -> AttachmentType.builder(() -> false)
                    .sync(net.minecraft.network.codec.ByteBufCodecs.BOOL)
                    .build());

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
