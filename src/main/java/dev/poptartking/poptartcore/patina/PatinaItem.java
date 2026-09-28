package dev.poptartking.poptartcore.patina;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.level.BlockEvent.BlockToolModificationEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public final class PatinaItem extends Item {
    public PatinaItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof WeatheringCopper weathering)) {
            return InteractionResult.PASS;
        }

        return weathering
                .getNext(state)
                .map(next -> {
                    Player player = context.getPlayer();
                    if (!level.isClientSide()) {
                        level.setBlockAndUpdate(pos, next);
                        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                        if (player == null || !player.hasInfiniteMaterials()) {
                            context.getItemInHand().shrink(1);
                        }
                        if (player != null) {
                            player.awardStat(Stats.ITEM_USED.get(this));
                        }
                        if (level instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(
                                    ParticleTypes.HAPPY_VILLAGER,
                                    pos.getX() + 0.5,
                                    pos.getY() + 0.5,
                                    pos.getZ() + 0.5,
                                    12,
                                    0.3,
                                    0.3,
                                    0.3,
                                    0.0);
                        }
                    }

                    SoundType sound = state.getSoundType();
                    level.playSound(
                            player,
                            pos,
                            sound.getPlaceSound(),
                            SoundSource.BLOCKS,
                            (sound.getVolume() + 1.0F) / 2.0F,
                            sound.getPitch() * 0.8F);
                    return InteractionResult.sidedSuccess(level.isClientSide());
                })
                .orElse(InteractionResult.PASS);
    }

    @SubscribeEvent
    public static void onScrape(BlockToolModificationEvent event) {
        if (event.isSimulated() || event.getItemAbility() != ItemAbilities.AXE_SCRAPE) {
            return;
        }
        BlockState state = event.getState();
        if (state.getBlock() instanceof WeatheringCopper weathering && weathering.getAge() != WeatherState.UNAFFECTED) {
            UseOnContext context = event.getContext();
            if (!context.getLevel().isClientSide()) {
                Block.popResource(
                        context.getLevel(), context.getClickedPos(), new ItemStack(PoptartCoreItems.PATINA.get()));
            }
        }
    }
}
