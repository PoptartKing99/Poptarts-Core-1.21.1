package dev.poptartking.poptartcore.waxgolem;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import dev.poptartking.poptartcore.registry.PoptartCoreEntities;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

/* JADX INFO: loaded from: wayfarer_core-0.1.0.jar:dev/tazer/wayfarer/waxgolem/WaxGolemConstruction.class */
@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public final class WaxGolemConstruction {
    private WaxGolemConstruction() {}

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            BlockState placed = event.getPlacedBlock();
            if (!placed.is(Blocks.JACK_O_LANTERN) && !placed.is(Blocks.CARVED_PUMPKIN)) {
                return;
            }
            BlockPos head = event.getPos();
            BlockPos upper = head.below();
            BlockPos lower = upper.below();
            if (!isWax(serverLevel, upper) || !isWax(serverLevel, lower)) {
                return;
            }
            float yaw = placed.hasProperty(CarvedPumpkinBlock.FACING)
                    ? placed.getValue(CarvedPumpkinBlock.FACING).toYRot()
                    : 0.0f;
            BlockPos[] pattern = {head, upper, lower};
            for (BlockPos pos : pattern) {
                BlockState consumed = serverLevel.getBlockState(pos);
                serverLevel.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                serverLevel.levelEvent(2001, pos, Block.getId(consumed));
            }
            WaxGolem golem = PoptartCoreEntities.WAX_GOLEM.get().create(serverLevel);
            if (golem == null) {
                return;
            }
            golem.moveTo(((double) lower.getX()) + 0.5d, lower.getY(), ((double) lower.getZ()) + 0.5d, yaw, 0.0f);
            golem.setYBodyRot(yaw);
            golem.setYHeadRot(yaw);
            golem.setHome(lower);
            golem.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(lower), MobSpawnType.TRIGGERED, null);
            serverLevel.addFreshEntity(golem);
            for (ServerPlayer nearby : serverLevel.getEntitiesOfClass(
                    ServerPlayer.class, golem.getBoundingBox().inflate(5.0d))) {
                CriteriaTriggers.SUMMONED_ENTITY.trigger(nearby, golem);
            }
            serverLevel.gameEvent((Entity) null, GameEvent.ENTITY_PLACE, lower);
            for (BlockPos blockPos : pattern) {
                serverLevel.blockUpdated(blockPos, Blocks.AIR);
            }
        }
    }

    private static boolean isWax(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(PoptartCoreBlocks.WAX_BLOCK.get());
    }
}
