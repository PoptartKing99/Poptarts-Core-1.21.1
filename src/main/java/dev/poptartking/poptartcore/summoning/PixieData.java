package dev.poptartking.poptartcore.summoning;

import java.util.UUID;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

public class PixieData implements INBTSerializable<CompoundTag> {
    @Nullable
    private UUID ownerUuid;

    @Nullable
    private UUID summonId;

    private int idleTicks;

    public boolean hasOwner() {
        return this.ownerUuid != null;
    }

    public int getIdleTicks() {
        return this.idleTicks;
    }

    public void setIdleTicks(int idleTicks) {
        this.idleTicks = idleTicks;
    }

    @Nullable
    public UUID getOwnerUuid() {
        return this.ownerUuid;
    }

    public void setOwnerUuid(@Nullable UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    @Nullable
    public UUID getSummonId() {
        return this.summonId;
    }

    public void setSummonId(@Nullable UUID summonId) {
        this.summonId = summonId;
    }

    public CompoundTag serializeNBT(Provider provider) {
        CompoundTag tag = new CompoundTag();
        if (this.ownerUuid != null) {
            tag.putUUID("Owner", this.ownerUuid);
        }

        if (this.summonId != null) {
            tag.putUUID("Summon", this.summonId);
        }

        tag.putInt("IdleTicks", this.idleTicks);
        return tag;
    }

    public void deserializeNBT(Provider provider, CompoundTag tag) {
        this.ownerUuid = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        this.summonId = tag.hasUUID("Summon") ? tag.getUUID("Summon") : null;
        this.idleTicks = tag.getInt("IdleTicks");
    }
}
