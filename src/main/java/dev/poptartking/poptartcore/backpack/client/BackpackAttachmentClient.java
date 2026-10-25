package dev.poptartking.poptartcore.backpack.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.equipment.UtilityInputPayload;
import dev.poptartking.poptartcore.equipment.UtilityWield;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = PoptartCore.MOD_ID, value = Dist.CLIENT)
public final class BackpackAttachmentClient {
    private static final KeyMapping USE_ATTACHMENT = new KeyMapping(
            "key.poptartcore.use_attachment",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.gameplay");
    private static LocalPlayer owner;
    private static boolean down;
    private static boolean holding;
    private static int heartbeat;

    private BackpackAttachmentClient() {}

    @EventBusSubscriber(modid = PoptartCore.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static final class KeyRegistration {
        private KeyRegistration() {}

        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(USE_ATTACHMENT);
        }
    }

    public static boolean isHolding() {
        return holding;
    }

    private static void end(Minecraft minecraft) {
        if (owner != null) dev.poptartking.poptartcore.backpack.BackpackAttachmentEffects.clear(owner);
        if (owner != null && holding) {
            holding = false;
            if (minecraft.gameMode != null) minecraft.gameMode.releaseUsingItem(owner);
            UtilityWield.stop(owner);
            if (minecraft.getConnection() != null) PacketDistributor.sendToServer(new UtilityInputPayload(false, true));
        }
        heartbeat = 0;
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player != owner) {
            end(minecraft);
            owner = minecraft.player;
            down = false;
        }
        // Keep the working item alive while its own container screen is open.
        if (holding
                && minecraft.screen != null
                && owner != null
                && owner.isAlive()
                && UtilityWield.isAttachmentWielding(owner)) {
            UtilityWield.tick(owner);
            if (++heartbeat % 10 == 0) PacketDistributor.sendToServer(new UtilityInputPayload(true, true));
            return;
        }
        boolean pressed = USE_ATTACHMENT.isDown()
                && minecraft.screen == null
                && minecraft.isWindowActive()
                && owner != null
                && owner.isAlive()
                && !owner.isSpectator()
                && minecraft.gameMode != null;
        if (holding) {
            UtilityWield.tick(owner);
            if (!pressed || !UtilityWield.isWielding(owner)) end(minecraft);
            else if (++heartbeat % 10 == 0) PacketDistributor.sendToServer(new UtilityInputPayload(true, true));
        } else if (pressed && !down && UtilityWield.startAttachment(owner)) {
            holding = true;
            // This payload must precede the ordinary use packet so the server resolves the utility hand.
            PacketDistributor.sendToServer(new UtilityInputPayload(true, true));
            if (minecraft.gameMode.useItem(owner, InteractionHand.MAIN_HAND).shouldSwing())
                owner.swing(InteractionHand.MAIN_HAND);
        }
        down = pressed;
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        end(Minecraft.getInstance());
        owner = null;
        down = false;
    }
}
