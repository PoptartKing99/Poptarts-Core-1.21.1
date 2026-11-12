package dev.poptartking.poptartcore.summoning;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

public class AllayCallingBellItem extends PixieCallingBellItem {
    public AllayCallingBellItem(Properties properties, int cooldown, int count) {
        super(properties, PixieType.ALLAY, cooldown, count);
    }

    @Override
    protected float ringPitch() {
        return 1.5F;
    }

    @Override
    protected void summonPixies(ServerLevel level, Player player) {
        HitResult hit = player.pick(20.0, 1.0F, false);
        AllaySummonController.summon(level, player, hit.getLocation(), this.count);
    }

    public InteractionResult interactLivingEntity(
            ItemStack stack, Player player, LivingEntity target, InteractionHand usedHand) {
        if (player.level() instanceof ServerLevel level) {
            if (PixieSummons.hasActive(level, player, PixieType.ALLAY)) {
                PixieSummons.dismiss(level, player, PixieType.ALLAY);
                this.playRecall(level, player);
                player.getCooldowns().addCooldown(this, this.cooldown);
                return InteractionResult.CONSUME;
            } else if (player.getCooldowns().isOnCooldown(this)) {
                return InteractionResult.PASS;
            } else {
                AllaySummonController.summonLure(level, player, target.getType(), this.count);
                this.playRing(level, player);
                player.awardStat(Stats.ITEM_USED.get(this));
                return InteractionResult.CONSUME;
            }
        } else {
            return InteractionResult.sidedSuccess(player.level().isClientSide);
        }
    }
}
