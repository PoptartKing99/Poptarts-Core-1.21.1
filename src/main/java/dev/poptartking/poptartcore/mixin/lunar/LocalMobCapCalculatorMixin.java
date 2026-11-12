package dev.poptartking.poptartcore.mixin.lunar;

import dev.poptartking.poptartcore.lunar.BloodMoonMobCap;
import java.util.List;
import java.util.Map;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LocalMobCapCalculator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LocalMobCapCalculator.class)
public abstract class LocalMobCapCalculatorMixin implements BloodMoonMobCap {
    @Shadow
    @Final
    private Map<ServerPlayer, ?> playerMobCounts;

    @Shadow
    private List<ServerPlayer> getPlayersNear(ChunkPos pos) {
        throw new AssertionError();
    }

    @Override
    public boolean poptartcore$canSpawnBloodMoonMonster(ChunkPos chunk, int localLimit) {
        for (ServerPlayer player : getPlayersNear(chunk)) {
            var counts = (MobCountsAccessor) playerMobCounts.get(player);
            if (counts == null || counts.poptartcore$getCounts().getInt(MobCategory.MONSTER) < localLimit) return true;
        }
        return false;
    }
}
