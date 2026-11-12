package dev.poptartking.poptartcore.summoning;

import java.util.UUID;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public class AllayData extends PixieData {
    private int lifespan;

    @Nullable
    private UUID targetUuid;

    public int getLifespan() {
        return this.lifespan;
    }

    public void setLifespan(int lifespan) {
        this.lifespan = lifespan;
    }

    @Nullable
    public UUID getTargetUuid() {
        return this.targetUuid;
    }

    public void setTargetUuid(@Nullable UUID targetUuid) {
        this.targetUuid = targetUuid;
    }

    @Override
    public CompoundTag serializeNBT(Provider provider) {
        CompoundTag tag = super.serializeNBT(provider);
        tag.putInt("Lifespan", this.lifespan);
        if (this.targetUuid != null) {
            tag.putUUID("Target", this.targetUuid);
        }

        return tag;
    }

    @Override
    public void deserializeNBT(Provider provider, CompoundTag tag) {
        super.deserializeNBT(provider, tag);
        this.lifespan = tag.getInt("Lifespan");
        this.targetUuid = tag.hasUUID("Target") ? tag.getUUID("Target") : null;
    }
}
