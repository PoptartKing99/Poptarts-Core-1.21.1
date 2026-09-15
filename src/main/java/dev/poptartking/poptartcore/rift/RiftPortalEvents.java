package dev.poptartking.poptartcore.rift;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public final class RiftPortalEvents {
    private RiftPortalEvents() {}

    @SubscribeEvent
    public static void preventOrdinaryNetherPortals(BlockEvent.PortalSpawnEvent event) {
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void scrapeCryingObsidian(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        ItemStack stack = event.getItemStack();
        if (!state.is(Blocks.CRYING_OBSIDIAN) || !stack.canPerformAction(ItemAbilities.PICKAXE_DIG)) {
            return;
        }

        Player player = event.getEntity();
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
        if (level instanceof ServerLevel serverLevel) {
            level.setBlockAndUpdate(pos, Blocks.OBSIDIAN.defaultBlockState());
            Block.popResource(
                    level,
                    pos,
                    new ItemStack(PoptartCoreBlocks.RIFT_SEDIMENT.item().get()));
            level.playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.7F, 1.2F);
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.35F, 0.7F);

            Direction face = event.getFace() == null ? Direction.UP : event.getFace();
            for (int index = 0; index < 12; index++) {
                serverLevel.sendParticles(
                        ParticleTypes.FALLING_OBSIDIAN_TEAR,
                        pos.getX() + 0.5 + face.getStepX() * 0.6,
                        pos.getY() + 0.5 + face.getStepY() * 0.6,
                        pos.getZ() + 0.5 + face.getStepZ() * 0.6,
                        1,
                        0.3,
                        0.3,
                        0.3,
                        0.0);
                if (index % 3 == 0) {
                    serverLevel.sendParticles(
                            ParticleTypes.PORTAL,
                            pos.getX() + 0.5 + face.getStepX() * 0.62,
                            pos.getY() + 0.5 + face.getStepY() * 0.62,
                            pos.getZ() + 0.5 + face.getStepZ() * 0.62,
                            1,
                            0.24,
                            0.24,
                            0.24,
                            0.015);
                }
            }

            if (!player.hasInfiniteMaterials()) {
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(event.getHand()));
            }
        }
        player.swing(event.getHand());
    }

    @SubscribeEvent
    public static void clearLevelState(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            RiftPortalIgnition.clear(level);
            RiftBearing.clear(level);
        }
    }
}
