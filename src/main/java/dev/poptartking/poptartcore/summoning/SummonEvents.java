package dev.poptartking.poptartcore.summoning;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreAttachments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public final class SummonEvents {
    private SummonEvents() {}

    @SubscribeEvent
    public static void onPixieDamage(LivingIncomingDamageEvent event) {
        if (PixieSummons.isSummoned(event.getEntity())
                && (event.getSource().is(DamageTypeTags.IS_FALL)
                        || event.getSource().is(DamageTypes.IN_WALL))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDeath(LivingDeathEvent event) {
        var member = event.getEntity();
        if (member.level() instanceof ServerLevel level && PixieSummons.isSummoned(member)) {
            PixieSummons.releaseCarriedItems(level, member);
            PixieSummons.forget(level, member);
        }
    }

    @SubscribeEvent
    public static void onLeave(EntityLeaveLevelEvent event) {
        var member = event.getEntity();
        var reason = member.getRemovalReason();
        if (event.getLevel() instanceof ServerLevel level
                && PixieSummons.isSummoned(member)
                && (reason == Entity.RemovalReason.KILLED || reason == Entity.RemovalReason.DISCARDED)) {
            PixieSummons.forget(level, member);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        var player = event.getEntity();
        if (player.tickCount % 10 != 0
                || !(player.level() instanceof ServerLevel level)
                || !player.hasData(PoptartCoreAttachments.PIXIE_SUMMONER_DATA)) return;
        for (var type : PixieType.values()) {
            if (PixieSummons.hasActive(level, player, type)
                    && !player.getInventory().contains(stack -> stack.is(type.bell()))) {
                PixieSummons.dismiss(level, player, type);
            }
        }
    }
}
