package dev.poptartking.poptartcore.summoning;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

public class PixieSummonerData implements INBTSerializable<CompoundTag> {
    private final List<PixieSummon> summons = new ArrayList<>();

    public List<PixieSummon> getByType(PixieType type) {
        List<PixieSummon> result = new ArrayList<>();

        for (PixieSummon summon : this.summons) {
            if (summon.getType() == type) {
                result.add(summon);
            }
        }

        return result;
    }

    @Nullable
    public PixieSummon get(@Nullable UUID id) {
        if (id == null) {
            return null;
        } else {
            for (PixieSummon summon : this.summons) {
                if (summon.getId().equals(id)) {
                    return summon;
                }
            }

            return null;
        }
    }

    public void add(PixieSummon summon) {
        remove(summon.getId());
        this.summons.add(summon);
    }

    public void remove(UUID id) {
        this.summons.removeIf(summon -> summon.getId().equals(id));
    }

    public boolean isEmpty() {
        return this.summons.isEmpty();
    }

    public CompoundTag serializeNBT(Provider provider) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();

        for (PixieSummon summon : this.summons) {
            list.add(summon.serializeNBT(provider));
        }

        tag.put("Summons", list);
        return tag;
    }

    public void deserializeNBT(Provider provider, CompoundTag tag) {
        this.summons.clear();

        for (Tag entry : tag.getList("Summons", 10)) {
            CompoundTag saved = (CompoundTag) entry;
            if (!saved.hasUUID("Id")
                    || (!saved.getString("Type").equals("ALLAY")
                            && !saved.getString("Type").equals("VEX"))) continue;
            PixieSummon summon = new PixieSummon();
            summon.deserializeNBT(provider, saved);
            if (!summon.getMembers().isEmpty()) add(summon);
        }
    }
}
