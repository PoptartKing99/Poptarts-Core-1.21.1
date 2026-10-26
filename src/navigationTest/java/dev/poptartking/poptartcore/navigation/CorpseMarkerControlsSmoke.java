package dev.poptartking.poptartcore.navigation;

import com.farcr.ragdoll.content.entity.CorpseEntity;
import com.farcr.ragdoll.util.RagdollDataHolder;
import com.mojang.logging.LogUtils;
import dev.poptartking.poptartcore.PoptartCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID, value = Dist.CLIENT)
public final class CorpseMarkerControlsSmoke {
    private static int ticks;
    private static boolean done;
    private static CorpseEntity corpse;

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static void expectNeedleSpinning(Minecraft mc, boolean expected) throws Exception {
        var stack = mc.player.getMainHandItem();
        var property = net.minecraft.client.renderer.item.ItemProperties.getProperty(
                stack, net.minecraft.resources.ResourceLocation.withDefaultNamespace("angle"));
        require(
                property instanceof net.minecraft.client.renderer.item.CompassItemPropertyFunction,
                "Testing actual vanilla recovery-compass angle property");
        property.call(stack, mc.level, mc.player, 42);
        var randomField =
                net.minecraft.client.renderer.item.CompassItemPropertyFunction.class.getDeclaredField("wobbleRandom");
        randomField.setAccessible(true);
        var random = randomField.get(property);
        var lastTick = random.getClass().getDeclaredField("lastUpdateTick");
        lastTick.setAccessible(true);
        boolean spinning = lastTick.getLong(random) == mc.level.getGameTime();
        require(spinning == expected, "Recovery needle spinning=" + expected + " matches corpse marker visibility");
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("poptartcore.corpseMarkerSmoke")) return;
        var mc = Minecraft.getInstance();
        if (done || mc.player == null || mc.level == null || mc.screen != null) return;
        try {
            mc.setWindowActive(true);
            mc.player.getInventory().selected = 0;
            mc.player.setYRot(-90);
            mc.player.setXRot(0);
            ticks++;
            if (ticks == 10 || ticks == 80)
                mc.getSingleplayerServer().execute(() -> {
                    var player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                    player.getInventory().clearContent();
                    dev.poptartking.poptartcore.equipment.AccessoryInventory.get(player)
                            .setStackInSlot(
                                    dev.poptartking.poptartcore.equipment.AccessoryInventory.BACK_SLOT,
                                    ItemStack.EMPTY);
                    dev.poptartking.poptartcore.equipment.AccessoryInventory.get(player)
                            .setStackInSlot(
                                    dev.poptartking.poptartcore.equipment.AccessoryInventory.UTILITY_SLOT,
                                    ItemStack.EMPTY);
                    player.getInventory().selected = 0;
                    corpse = CorpseEntity.createPlayerRagdoll(player, false);
                    ((RagdollDataHolder) player).setRagdoll$boundCorpse(corpse.getUUID());
                    var death = GlobalPos.of(
                            player.level().dimension(), player.blockPosition().offset(10, 0, 0));
                    player.setLastDeathLocation(java.util.Optional.of(death));
                    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.RECOVERY_COMPASS));
                    CorpseDeathMarkers.sync(player);
                    mc.execute(() -> {
                        mc.player.setLastDeathLocation(java.util.Optional.of(death));
                        mc.player.setYRot(-90);
                    });
                });
            if (ticks == 40) {
                expectNeedleSpinning(mc, false);
                require(CorpseDeathMarkers.deathTarget(mc.player, true).isPresent(), "Live corpse has death marker");
                Screenshot.grab(mc.gameDirectory, "corpse-marker-before.png", mc.getMainRenderTarget(), message -> {});
            }
            if (ticks == 50) mc.getSingleplayerServer().execute(() -> corpse.remove(Entity.RemovalReason.KILLED));
            if (ticks == 70) {
                expectNeedleSpinning(mc, true);
                require(
                        CorpseDeathMarkers.deathTarget(mc.player, true).isEmpty(),
                        "Server removal synchronized and marker cleared");
                require(mc.player.getLastDeathLocation().isPresent(), "Death history retained");
                Screenshot.grab(mc.gameDirectory, "corpse-marker-after.png", mc.getMainRenderTarget(), message -> {});
            }
            if (ticks == 100) {
                expectNeedleSpinning(mc, false);
                require(
                        CorpseDeathMarkers.deathTarget(mc.player, true).isPresent(),
                        "Next corpse restores marker at same death location");
            }
            if (ticks == 110) {
                LogUtils.getLogger().info("CORPSE_MARKER_CONTROLS_PASS");
                done = true;
                mc.stop();
            }
        } catch (Throwable e) {
            LogUtils.getLogger().error("CORPSE_MARKER_CONTROLS_FAIL tick={}", ticks, e);
            done = true;
            mc.stop();
        }
    }
}
