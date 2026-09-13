package dev.poptartking.poptartcore.beekeeping;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.mixin.effect.MobEffectInstanceAccessor;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public final class BeekeeperEvents {
    private BeekeeperEvents() {}

    @SubscribeEvent
    public static void reduceArthropodDamage(LivingIncomingDamageEvent event) {
        int pieces = BeekeeperArmorItem.pieceCount(event.getEntity());
        if (pieces == 0 || !(event.getSource().getEntity() instanceof LivingEntity attacker)) {
            return;
        }

        if (attacker.getType().is(EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS)) {
            event.setAmount(event.getAmount() * (1.0F - 0.05F * pieces));
        }
    }

    @SubscribeEvent
    public static void shortenArthropodPoison(MobEffectEvent.Added event) {
        int pieces = BeekeeperArmorItem.pieceCount(event.getEntity());
        if (pieces == 0
                || !event.getEffectInstance().is(MobEffects.POISON)
                || !(event.getEffectSource() instanceof LivingEntity source)
                || !source.getType().is(EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS)) {
            return;
        }

        int shortenedDuration = (int) (event.getEffectInstance().getDuration() * (1.0F - 0.15F * pieces));
        ((MobEffectInstanceAccessor) (Object) event.getEffectInstance()).poptartcore$setDuration(shortenedDuration);
    }
}
