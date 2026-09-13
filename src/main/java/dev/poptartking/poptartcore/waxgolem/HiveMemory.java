package dev.poptartking.poptartcore.waxgolem;

import net.minecraft.core.BlockPos;

/* JADX INFO: loaded from: wayfarer_core-0.1.0.jar:dev/tazer/wayfarer/waxgolem/HiveMemory.class */
public final class HiveMemory {
    public static final int UNKNOWN = -1;
    public static final long SMOKE_MEMORY = 2400;
    private final BlockPos pos;
    private long smokeCheckedAt;
    private long checkedAt;
    private long nextCheck;
    private int honeyLevel = -1;
    private int smoke = -1;
    private boolean reachable = true;

    public HiveMemory(BlockPos pos) {
        this.pos = pos;
    }

    public BlockPos pos() {
        return this.pos;
    }

    public boolean reachable() {
        return this.reachable;
    }

    public void setReachable(boolean value) {
        this.reachable = value;
    }

    public int honeyLevel() {
        return this.honeyLevel;
    }

    public boolean ripe() {
        return this.honeyLevel >= 5;
    }

    public int smoke() {
        return this.smoke;
    }

    public void sawSmoke(boolean value, long time) {
        this.smoke = value ? 1 : 0;
        this.smokeCheckedAt = time;
    }

    public void restoreSmoke(int value, long time) {
        this.smoke = value;
        this.smokeCheckedAt = time;
    }

    public boolean unsmoked(long time) {
        return this.smoke == 0 && time - this.smokeCheckedAt < SMOKE_MEMORY;
    }

    public boolean harvestable(long time) {
        return this.reachable && due(time) && !unsmoked(time);
    }

    public boolean due(long time) {
        return ripe() || this.honeyLevel == -1 || time >= this.nextCheck;
    }

    public void seen(int level, long time) {
        int previous = this.honeyLevel;
        this.honeyLevel = level;
        this.checkedAt = time;
        this.nextCheck = time + interval(previous, level);
    }

    public void harvested(long time) {
        this.honeyLevel = 0;
        this.checkedAt = time;
        this.nextCheck = time + 1200;
    }

    private static long interval(int previous, int level) {
        if (level >= 4) {
            return 60L;
        }
        if (level == 3) {
            return 200L;
        }
        if (previous != -1 && level > previous) {
            return 300L;
        }
        return 600 - (((long) level) * 80);
    }

    public long checkedAt() {
        return this.checkedAt;
    }
}
