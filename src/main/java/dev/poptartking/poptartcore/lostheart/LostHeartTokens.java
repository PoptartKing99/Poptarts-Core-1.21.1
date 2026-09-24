package dev.poptartking.poptartcore.lostheart;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

final class LostHeartTokens extends SavedData {
    private final Map<UUID, Integer> tokens = new HashMap<>();

    static LostHeartTokens of(MinecraftServer server) {
        return server.getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(
                new Factory<>(LostHeartTokens::new, LostHeartTokens::load), "poptartcore_lost_hearts");
    }

    private static LostHeartTokens load(CompoundTag tag, HolderLookup.Provider registries) {
        LostHeartTokens data = new LostHeartTokens();
        for (String key : tag.getAllKeys()) data.tokens.put(UUID.fromString(key), tag.getInt(key));
        return data;
    }

    int bump(UUID owner) {
        int next = tokenOf(owner) + 1;
        tokens.put(owner, next);
        setDirty();
        return next;
    }

    int tokenOf(UUID owner) {
        return tokens.getOrDefault(owner, 0);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tokens.forEach((owner, token) -> tag.putInt(owner.toString(), token));
        return tag;
    }
}
