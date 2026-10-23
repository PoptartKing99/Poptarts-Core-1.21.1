package dev.poptartking.poptartcore.equipment;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreAttachments;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public final class SilenceRing {
    private static final Map<Player, Integer> STILL = new WeakHashMap<>();
    private static final Map<Player, Vec3> LAST = new WeakHashMap<>();

    private SilenceRing() {}

    public static boolean isIntangible(Player player) {
        return player.getData(PoptartCoreAttachments.SILENCED);
    }

    private static void setActive(Player player, boolean active) {
        boolean previous = isIntangible(player);
        if (previous != active) player.setData(PoptartCoreAttachments.SILENCED, active);
        if (active) player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 6, 0, false, false, false));
        else if (previous) player.removeEffect(MobEffects.INVISIBILITY);
    }

    public static void breakStillness(Player player) {
        STILL.put(player, 0);
        setActive(player, false);
    }

    public static void tick(Player player) {
        if (player.level().isClientSide) return;
        if (!EquipmentRing.has(player, EquipmentRing.Kind.SILENCE)) {
            STILL.remove(player);
            LAST.remove(player);
            setActive(player, false);
            return;
        }
        Vec3 position = player.position();
        Vec3 last = LAST.put(player, position);
        boolean moved = last == null || position.distanceToSqr(last) > 1.0E-6 || player.swinging;
        int still = moved ? 0 : STILL.getOrDefault(player, 0) + 1;
        STILL.put(player, still);
        int combat = Math.max(player.getLastHurtMobTimestamp(), player.getLastHurtByMobTimestamp());
        setActive(player, still >= 10 && player.tickCount - combat >= 60);
    }

    @SubscribeEvent
    public static void rightBlock(PlayerInteractEvent.RightClickBlock event) {
        dispel(event);
    }

    @SubscribeEvent
    public static void rightItem(PlayerInteractEvent.RightClickItem event) {
        dispel(event);
    }

    @SubscribeEvent
    public static void entity(PlayerInteractEvent.EntityInteract event) {
        dispel(event);
    }

    @SubscribeEvent
    public static void leftBlock(PlayerInteractEvent.LeftClickBlock event) {
        dispel(event);
    }

    private static void dispel(PlayerInteractEvent event) {
        if (!event.getEntity().level().isClientSide) breakStillness(event.getEntity());
    }
}
