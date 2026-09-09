package dev.poptartking.poptartcore.hammer;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;

public final class BlockBreakProgress extends SavedData {
    private static final String FILE_NAME = "poptartcore_block_break_progress";
    private static final float NORMAL_DECAY_RATIO = 1.0F;
    private static final float HAMMER_DECAY_RATIO = 0.2F;
    private static final float MAX_DECAY_PER_TICK = 0.05F;
    private final Long2ObjectMap<Crack> cracks = new Long2ObjectOpenHashMap<>();
    private final Map<UUID, MiningAttempt> miningAttempts = new HashMap<>();
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

    public float fractionAt(BlockPos pos) {
        Crack crack = cracks.get(pos.asLong());
        if (crack != null && crack.block != level.getBlockState(pos).getBlock()) {
            clear(pos);
            return 0.0F;
        }
        return crack == null ? 0.0F : crack.fraction;
    }

    public void beginAttempt(UUID playerId, BlockPos pos, float savedFraction) {
        miningAttempts.put(playerId, new MiningAttempt(pos.immutable(), savedFraction > 0.0F, savedFraction));
    }

    public void updateAttempt(UUID playerId, BlockPos pos, float fraction) {
        MiningAttempt attempt = miningAttempts.get(playerId);
        if (attempt != null && attempt.pos.equals(pos)) {
            attempt.fraction = fraction;
        }
    }

    public boolean isCompletedResumedAttempt(UUID playerId, BlockPos pos) {
        MiningAttempt attempt = miningAttempts.get(playerId);
        return attempt != null && attempt.resumed && attempt.pos.equals(pos) && attempt.fraction >= 1.0F;
    }

    public void endAttempt(UUID playerId) {
        miningAttempts.remove(playerId);
    }

    public void record(BlockPos pos, float fraction, float rate, boolean hammered, long gameTime) {
        if (fraction <= 0.0F || rate <= 0.0F) {
            return;
        }

        Crack crack = crackForCurrentBlock(pos);
        crack.fraction = PersistentMiningMath.record(crack.fraction, fraction);
        crack.rate = rate;
        crack.decayRatio = hammered ? HAMMER_DECAY_RATIO : NORMAL_DECAY_RATIO;
        crack.touched = gameTime;
        crack.show(level);
        setDirty();
    }

    public void accrue(BlockPos pos, float amount, float rate, boolean hammered, long gameTime) {
        if (amount <= 0.0F || rate <= 0.0F) {
            return;
        }

        Crack crack = crackForCurrentBlock(pos);
        crack.fraction = PersistentMiningMath.accrue(crack.fraction, amount);
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

    private Crack crackForCurrentBlock(BlockPos pos) {
        Block block = level.getBlockState(pos).getBlock();
        Crack crack = cracks.get(pos.asLong());
        if (crack != null && crack.block != block) {
            crack.hide(level);
            cracks.remove(pos.asLong());
            crack = null;
        }
        if (crack == null) {
            crack = new Crack(pos.immutable(), block);
            cracks.put(pos.asLong(), crack);
        }
        return crack;
    }

    public void tick(long gameTime) {
        if (cracks.isEmpty()) {
            return;
        }

        boolean changed = cracks.values().removeIf(crack -> {
            if (crack.touched == gameTime) {
                return false;
            }

            crack.fraction =
                    PersistentMiningMath.decay(crack.fraction, crack.rate, crack.decayRatio, MAX_DECAY_PER_TICK);
            if (crack.fraction <= 0.0F) {
                crack.hide(level);
                return true;
            }
            if (!level.isLoaded(crack.pos)) {
                return false;
            }

            BlockState state = level.getBlockState(crack.pos);
            if (state.getBlock() == crack.block
                    && !state.isAir()
                    && state.getFluidState().isEmpty()) {
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
        return progress;
    }

    private static final class MiningAttempt {
        private final BlockPos pos;
        private final boolean resumed;
        private float fraction;

        private MiningAttempt(BlockPos pos, boolean resumed, float fraction) {
            this.pos = pos;
            this.resumed = resumed;
            this.fraction = fraction;
        }
    }

    private static final class Crack {
        private static final String BLOCK_KEY = "block";
        private final int id;
        private final BlockPos pos;
        private final Block block;
        private float decayRatio = NORMAL_DECAY_RATIO;
        private float fraction;
        private float rate;
        private int shownStage = -1;
        private long touched;

        private Crack(BlockPos pos, Block block) {
            this.pos = pos;
            this.block = block;
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
            tag.putString(BLOCK_KEY, BuiltInRegistries.BLOCK.getKey(block).toString());
            tag.putFloat("fraction", fraction);
            tag.putFloat("rate", rate);
            tag.putFloat("decay", decayRatio);
            return tag;
        }

        private static Crack load(CompoundTag tag) {
            if (!tag.contains(BLOCK_KEY, Tag.TAG_STRING)) {
                return null;
            }

            Optional<BlockPos> position = NbtUtils.readBlockPos(tag, "pos");
            ResourceLocation blockId = ResourceLocation.tryParse(tag.getString(BLOCK_KEY));
            Optional<Block> block = blockId == null ? Optional.empty() : BuiltInRegistries.BLOCK.getOptional(blockId);
            return position.flatMap(pos -> block.map(savedBlock -> {
                        Crack crack = new Crack(pos, savedBlock);
                        crack.fraction = tag.getFloat("fraction");
                        crack.rate = tag.getFloat("rate");
                        crack.decayRatio = tag.contains("decay") ? tag.getFloat("decay") : NORMAL_DECAY_RATIO;
                        return crack;
                    }))
                    .orElse(null);
        }
    }
}
