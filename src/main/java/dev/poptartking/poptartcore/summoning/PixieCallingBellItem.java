package dev.poptartking.poptartcore.summoning;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;

public abstract class PixieCallingBellItem extends Item {
    private final PixieType type;
    public final int cooldown;
    public final int count;

    public PixieCallingBellItem(Properties properties, PixieType type, int cooldown, int count) {
        super(properties);
        this.type = type;
        this.cooldown = cooldown;
        this.count = count;
    }

    protected abstract void summonPixies(ServerLevel var1, Player var2);

    protected void onSummon(ServerLevel level, Player player) {}

    protected float ringPitch() {
        return 1.0F;
    }

    public void playRing(ServerLevel level, Player player) {
        level.playSound(
                null, player.blockPosition(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.0F, this.ringPitch());
        level.gameEvent(GameEvent.INSTRUMENT_PLAY, player.position(), Context.of(player));
    }

    public void playRecall(ServerLevel level, Player player) {
        level.playSound(
                null,
                player.blockPosition(),
                SoundEvents.BELL_BLOCK,
                SoundSource.PLAYERS,
                0.8F,
                this.ringPitch() * 0.6F);
        level.gameEvent(GameEvent.INSTRUMENT_PLAY, player.position(), Context.of(player));
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (level instanceof ServerLevel serverLevel) {
            if (PixieSummons.hasActive(serverLevel, player, this.type)) {
                PixieSummons.dismiss(serverLevel, player, this.type);
                this.playRecall(serverLevel, player);
                player.getCooldowns().addCooldown(this, this.cooldown);
                return InteractionResultHolder.consume(stack);
            } else if (player.getCooldowns().isOnCooldown(this)) {
                return InteractionResultHolder.pass(stack);
            } else {
                this.onSummon(serverLevel, player);
                this.summonPixies(serverLevel, player);
                this.playRing(serverLevel, player);
                player.awardStat(Stats.ITEM_USED.get(this));
                return InteractionResultHolder.consume(stack);
            }
        } else {
            return InteractionResultHolder.success(stack);
        }
    }
}
