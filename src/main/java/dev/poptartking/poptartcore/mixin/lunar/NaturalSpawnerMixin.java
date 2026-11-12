package dev.poptartking.poptartcore.mixin.lunar;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.poptartking.poptartcore.lunar.BloodMoonData;
import dev.poptartking.poptartcore.lunar.BloodMoonMobCap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NaturalSpawner.class)
public abstract class NaturalSpawnerMixin {
    @Shadow
    @Final
    private static int MAGIC_NUMBER;

    @WrapOperation(
            method = "spawnForChunk",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/level/NaturalSpawner$SpawnState;canSpawnForCategory(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/world/level/ChunkPos;)Z"))
    private static boolean poptartcore$bloodMoonMonsterCap(
            NaturalSpawner.SpawnState state,
            MobCategory category,
            ChunkPos chunk,
            Operation<Boolean> original,
            ServerLevel level) {
        if (category != MobCategory.MONSTER || !BloodMoonData.active(level))
            return original.call(state, category, chunk);
        int localLimit = (int) (category.getMaxInstancesPerChunk() * 1.4);
        int globalLimit = localLimit * state.getSpawnableChunkCount() / MAGIC_NUMBER;
        if (state.getMobCategoryCounts().getInt(category) >= globalLimit) return false;
        var localCap = ((SpawnStateAccessor) state).poptartcore$getLocalMobCapCalculator();
        return ((BloodMoonMobCap) localCap).poptartcore$canSpawnBloodMoonMonster(chunk, localLimit);
    }

    @WrapOperation(
            method = "spawnCategoryForChunk",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/level/NaturalSpawner;getRandomPosWithin(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/chunk/LevelChunk;)Lnet/minecraft/core/BlockPos;"))
    private static BlockPos poptartcore$bloodMoonSurface(
            Level level, LevelChunk chunk, Operation<BlockPos> original, MobCategory category) {
        BlockPos candidate = original.call(level, chunk);
        if (category != MobCategory.MONSTER || !BloodMoonData.active(level)) return candidate;
        var nearest =
                level.getNearestPlayer(candidate.getX() + 0.5, candidate.getY(), candidate.getZ() + 0.5, -1, false);
        if (nearest == null) return candidate;
        BlockPos player = nearest.blockPosition();
        var surface = Heightmap.Types.MOTION_BLOCKING_NO_LEAVES;
        if (player.getY() < level.getHeight(surface, player.getX(), player.getZ())) return candidate;
        return candidate.atY(level.getHeight(surface, candidate.getX(), candidate.getZ()));
    }

    @ModifyExpressionValue(
            method = "isValidPositionForMob",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/MobCategory;getDespawnDistance()I"))
    private static int poptartcore$bloodMoonMonsterRange(int original, ServerLevel level, Mob mob) {
        return mob.getType().getCategory() == MobCategory.MONSTER && BloodMoonData.active(level)
                ? Math.min(original, 64)
                : original;
    }
}
