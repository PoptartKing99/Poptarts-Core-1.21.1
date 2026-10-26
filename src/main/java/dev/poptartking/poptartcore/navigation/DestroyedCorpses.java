package dev.poptartking.poptartcore.navigation;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/** Removed or manually dismissed corpse-marker identities, independent of loaded chunks. */
public final class DestroyedCorpses extends SavedData {
    private final Set<UUID> destroyed = new HashSet<>();

    public static DestroyedCorpses get(MinecraftServer server) {
        return server.overworld()
                .getDataStorage()
                .computeIfAbsent(
                        new Factory<>(DestroyedCorpses::new, DestroyedCorpses::load), "poptartcore_destroyed_corpses");
    }

    public boolean contains(UUID corpse) {
        return corpse != null && destroyed.contains(corpse);
    }

    public void record(UUID corpse) {
        if (destroyed.add(corpse)) setDirty();
    }

    public static DestroyedCorpses load(CompoundTag tag, HolderLookup.Provider registries) {
        var data = new DestroyedCorpses();
        var entries = tag.getList("corpses", Tag.TAG_COMPOUND);
        for (int i = 0; i < entries.size(); i++)
            data.destroyed.add(entries.getCompound(i).getUUID("id"));
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        var list = new ListTag();
        for (var id : destroyed) {
            var entry = new CompoundTag();
            entry.putUUID("id", id);
            list.add(entry);
        }
        tag.put("corpses", list);
        return tag;
    }
}
