package dev.poptartking.poptartcore.navigation.persistence;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

public final class PersistentState<T extends Persistent> extends SavedData {
    private final Codec<T> codec;
    private final T value;

    private PersistentState(Codec<T> codec, T value) {
        this.codec = codec;
        this.value = value;
        value.bindDirtyMarker(this::setDirty);
    }

    public static <T extends Persistent> T get(
            MinecraftServer server, String name, Codec<T> codec, Supplier<T> factory) {
        var type = new SavedData.Factory<PersistentState<T>>(
                () -> new PersistentState<>(codec, factory.get()),
                (tag, registries) -> new PersistentState<>(
                        codec,
                        codec.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag.get("value"))
                                .getOrThrow()));
        return server.overworld().getDataStorage().computeIfAbsent(type, "poptartcore_" + name).value;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put(
                "value",
                codec.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), value)
                        .getOrThrow());
        return tag;
    }
}
