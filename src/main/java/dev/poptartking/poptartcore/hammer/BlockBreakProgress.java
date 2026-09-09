package dev.poptartking.poptartcore.hammer;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;

public final class BlockBreakProgress extends SavedData {
    private static final String FILE_NAME = "poptartcore_block_break_progress";
    private static final float NORMAL_DECAY_RATIO = 1.0F;
    private static final float HAMMER_DECAY_RATIO = 0.2F;
    private static final float MAX_DECAY_PER_TICK = 0.05F;
    private final Long2ObjectMap<Crack> cracks = new Long2ObjectOpenHashMap<>();
    private final Map<UUID, BlockPos> resumedAttempts = new HashMap<>();
    private final ServerLevel level;

    private BlockBreakProgress(ServerLevel level) {
        this.level = level;
    }

    public static BlockBreakProgress get(ServerLevel level) {
        return level.getDataStorage()
                .computeIfAbsent(
                        new Factory<>(() -> new BlockBreakProgress(level), (tag, registries) -> load(level, tag), null),
                        FILE_NAME);
    }

    public int resumedStart(BlockPos pos, float rate, int gameTicks, int currentStart) {
        Crack crack = cracks.get(pos.asLong());
        if (crack == null || crack.fraction <= 0.0F || rate <= 0.0F) {
            return currentStart;
        }

        int earnedTicks = Math.round(Math.min(crack.fraction, 1.0F) / rate);
        return gameTicks - Math.max(0, earnedTicks - 1);
    }

    public float fractionAt(BlockPos pos) {
        Crack crack = cracks.get(pos.asLong());
        return crack == null ? 0.0F : crack.fraction;
    }

    public void beginAttempt(UUID playerId, BlockPos pos) {
        if (fractionAt(pos) > 0.0F) {
            resumedAttempts.put(playerId, pos.immutable());
        } else {
            resumedAttempts.remove(playerId);
        }
    }

    public boolean isResumedAttempt(UUID playerId, BlockPos pos) {
        return pos.equals(resumedAttempts.get(playerId));
    }

    public void endAttempt(UUID playerId) {
        resumedAttempts.remove(playerId);
    }

    public void record(BlockPos pos, float fraction, float rate, boolean hammered, long gameTime) {
        if (fraction <= 0.0F || rate <= 0.0F) {
            return;
        }

        Crack crack = cracks.computeIfAbsent(pos.asLong(), packed -> new Crack(BlockPos.of(packed)));
        crack.fraction = Math.min(1.0F, Math.max(crack.fraction, fraction));
        crack.rate = rate;
        crack.decayRatio = hammered ? HAMMER_DECAY_RATIO : NORMAL_DECAY_RATIO;
        crack.touched = gameTime;
        crack.show(level);
        setDirty();
    }

    public void clear(BlockPos pos) {
        Crack crack = cracks.remove(pos.asLong());
        if (crack != null) {
            crack.hide(level);
            setDirty();
        }
    }

    public void tick(long gameTime) {
        if (cracks.isEmpty()) {
            return;
        }

        boolean changed = cracks.values().removeIf(crack -> {
            if (crack.touched == gameTime) {
                return false;
            }

            crack.fraction -= Math.min(crack.rate * crack.decayRatio, MAX_DECAY_PER_TICK * crack.decayRatio);
            BlockState state = level.getBlockState(crack.pos);
            if (crack.fraction > 0.0F && !state.isAir() && state.getFluidState().isEmpty()) {
                crack.show(level);
                return false;
            }

            crack.hide(level);
            return true;
        });
        if (changed || !cracks.isEmpty()) {
            setDirty();
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag entries = new ListTag();
        for (Crack crack : cracks.values()) {
            entries.add(crack.save());
        }
        tag.put("cracks", entries);
        return tag;
    }

    private static BlockBreakProgress load(ServerLevel level, CompoundTag tag) {
        BlockBreakProgress progress = new BlockBreakProgress(level);
        ListTag entries = tag.getList("cracks", Tag.TAG_COMPOUND);
        for (int index = 0; index < entries.size(); index++) {
            Crack crack = Crack.load(entries.getCompound(index));
            if (crack != null) {
                progress.cracks.put(crack.pos.asLong(), crack);
            }
        }
        for (Crack crack : progress.cracks.values()) {
            crack.show(level);
        }
        return progress;
    }

    private static final class Crack {
        private final int id;
        private final BlockPos pos;
        private float decayRatio = NORMAL_DECAY_RATIO;
        private float fraction;
        private float rate;
        private int shownStage = -1;
        private long touched;

        private Crack(BlockPos pos) {
            this.pos = pos;
            this.id = HammerMining.crackId(pos);
        }

        private void show(ServerLevel level) {
            int stage = Math.min(9, (int) (fraction * 10.0F));
            if (stage != shownStage) {
                shownStage = stage;
                level.destroyBlockProgress(id, pos, stage);
            }
        }

        private void hide(ServerLevel level) {
            shownStage = -1;
            level.destroyBlockProgress(id, pos, -1);
        }

        private CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.put("pos", NbtUtils.writeBlockPos(pos));
            tag.putFloat("fraction", fraction);
            tag.putFloat("rate", rate);
            tag.putFloat("decay", decayRatio);
            return tag;
        }

        private static Crack load(CompoundTag tag) {
            Optional<BlockPos> position = NbtUtils.readBlockPos(tag, "pos");
            return position.map(pos -> {
                        Crack crack = new Crack(pos);
                        crack.fraction = tag.getFloat("fraction");
                        crack.rate = tag.getFloat("rate");
                        crack.decayRatio = tag.contains("decay") ? tag.getFloat("decay") : NORMAL_DECAY_RATIO;
                        return crack;
                    })
                    .orElse(null);
        }
    }
}
