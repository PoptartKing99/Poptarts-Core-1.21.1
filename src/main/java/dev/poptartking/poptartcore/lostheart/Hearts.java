package dev.poptartking.poptartcore.lostheart;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreAttachments;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public final class Hearts {
    public static final int MIN = 5;
    public static final int MAX = 10;
    private static final ResourceLocation MODIFIER = PoptartCore.location("lost_heart_health");

    private Hearts() {}

    public static int get(Player player) {
        return player.getData(PoptartCoreAttachments.MAX_HEARTS.get());
    }

    public static void set(Player player, int hearts) {
        player.setData(PoptartCoreAttachments.MAX_HEARTS.get(), Math.clamp(hearts, MIN, MAX));
        apply(player);
    }

    public static void apply(Player player) {
        AttributeInstance health = player.getAttribute(Attributes.MAX_HEALTH);
        if (health == null) return;
        health.addOrReplacePermanentModifier(new AttributeModifier(
                MODIFIER, 2.0 * get(player) - 20.0, AttributeModifier.Operation.ADD_VALUE));
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        int hearts = get(event.getOriginal());
        if (event.isWasDeath()) hearts = Math.max(MIN, hearts - 1);
        set(event.getEntity(), hearts);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        apply(event.getEntity());
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        apply(event.getEntity());
    }
}
