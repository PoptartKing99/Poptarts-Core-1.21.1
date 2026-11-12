package dev.poptartking.poptartcore.summoning;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

/** Saved with the helper so a reload during phasing cannot leave it invisible or invulnerable. */
public final class PhaseState implements INBTSerializable<CompoundTag> {
    public static final int IDLE = 0;
    public static final int ENTERING = 1;
    public static final int PHASING = 2;
    private int state;
    private int delay;

    @Nullable
    private Vec3 entry;

    @Nullable
    private Vec3 exit;

    private boolean wasInvisible;
    private boolean wasInvulnerable;
    private boolean wasNoGravity;

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    @Nullable
    public Vec3 getEntry() {
        return entry;
    }

    public void setEntry(@Nullable Vec3 entry) {
        this.entry = entry;
    }

    @Nullable
    public Vec3 getExit() {
        return exit;
    }

    public void setExit(@Nullable Vec3 exit) {
        this.exit = exit;
    }

    public void remember(Mob mob) {
        wasInvisible = mob.isInvisible();
        wasInvulnerable = mob.isInvulnerable();
        wasNoGravity = mob.isNoGravity();
    }

    public void restore(Mob mob) {
        mob.noPhysics = false;
        mob.setInvisible(wasInvisible);
        mob.setInvulnerable(wasInvulnerable);
        mob.setNoGravity(wasNoGravity);
        state = IDLE;
        delay = 0;
        entry = null;
        exit = null;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var tag = new CompoundTag();
        tag.putInt("State", state);
        tag.putInt("Delay", delay);
        tag.putBoolean("WasInvisible", wasInvisible);
        tag.putBoolean("WasInvulnerable", wasInvulnerable);
        tag.putBoolean("WasNoGravity", wasNoGravity);
        if (entry != null && exit != null) {
            tag.putDouble("EntryX", entry.x);
            tag.putDouble("EntryY", entry.y);
            tag.putDouble("EntryZ", entry.z);
            tag.putDouble("ExitX", exit.x);
            tag.putDouble("ExitY", exit.y);
            tag.putDouble("ExitZ", exit.z);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        state = net.minecraft.util.Mth.clamp(tag.getInt("State"), IDLE, PHASING);
        delay = net.minecraft.util.Mth.clamp(tag.getInt("Delay"), 0, 6);
        wasInvisible = tag.getBoolean("WasInvisible");
        wasInvulnerable = tag.getBoolean("WasInvulnerable");
        wasNoGravity = tag.getBoolean("WasNoGravity");
        entry = tag.contains("EntryX")
                ? new Vec3(tag.getDouble("EntryX"), tag.getDouble("EntryY"), tag.getDouble("EntryZ"))
                : null;
        exit = tag.contains("ExitX")
                ? new Vec3(tag.getDouble("ExitX"), tag.getDouble("ExitY"), tag.getDouble("ExitZ"))
                : null;
        if (entry == null || exit == null) state = IDLE;
    }
}
