package dev.poptartking.poptartcore.backpack.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.backpack.OpenBackpackPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = PoptartCore.MOD_ID, value = Dist.CLIENT)
public final class BackpackClient {
    private static final KeyMapping OPEN = new KeyMapping(
            "key.poptartcore.open_backpack",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            "key.categories.gameplay");

    private BackpackClient() {}

    private static int progress;
    private static int cooldown;
    private static net.minecraft.core.BlockPos target;
    private static boolean pickUp;

    @SubscribeEvent
    public static void suppressPickupSwing(
            net.neoforged.neoforge.client.event.InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isUseItem() && resolve(Minecraft.getInstance()) && pickUp) {
            // The hold gesture sends its own packet; ordinary use would swing or use the offhand item.
            event.setSwingHand(false);
            event.setCanceled(true);
        }
    }

    private static boolean resolve(Minecraft minecraft) {
        var player = minecraft.player;
        if (player == null
                || player.isSpectator()
                || !player.isAlive()
                || minecraft.screen != null
                || !minecraft.isWindowActive()
                || !player.isShiftKeyDown()
                || !player.getMainHandItem().isEmpty()
                || !(minecraft.hitResult instanceof net.minecraft.world.phys.BlockHitResult hit)
                || hit.getType() != net.minecraft.world.phys.HitResult.Type.BLOCK) return false;
        var state = player.level().getBlockState(hit.getBlockPos());
        boolean wearing = !player.getData(
                        dev.poptartking.poptartcore.registry.PoptartCoreAttachments.BASIC_BACKPACK_VISUAL)
                .isEmpty();
        boolean backpack = state.getBlock() instanceof dev.poptartking.poptartcore.backpack.BackpackBlock;
        if (backpack) {
            pickUp = true;
            return !wearing
                    && dev.poptartking.poptartcore.equipment.AccessoryInventory.get(player)
                            .getStackInSlot(dev.poptartking.poptartcore.equipment.AccessoryInventory.BACK_SLOT)
                            .isEmpty();
        }
        if (!wearing
                || !player.getOffhandItem().isEmpty()
                || state.getMenuProvider(player.level(), hit.getBlockPos()) != null) return false;
        var pos = state.canBeReplaced() ? hit.getBlockPos() : hit.getBlockPos().relative(hit.getDirection());
        pickUp = false;
        return player.level().getBlockState(pos).canBeReplaced()
                && player.level()
                        .getBlockState(pos.below())
                        .isFaceSturdy(player.level(), pos.below(), net.minecraft.core.Direction.UP);
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        var minecraft = Minecraft.getInstance();
        if (cooldown > 0) cooldown--;
        if (!resolve(minecraft) || cooldown > 0) {
            progress = 0;
            target = null;
        } else {
            var hit = (net.minecraft.world.phys.BlockHitResult) minecraft.hitResult;
            if (!hit.getBlockPos().equals(target)) progress = 0;
            target = hit.getBlockPos();
            if (!minecraft.options.keyUse.isDown()) progress = 0;
            else if (++progress >= 10) {
                PacketDistributor.sendToServer(
                        new dev.poptartking.poptartcore.backpack.BackpackGesturePayload(target, pickUp));
                progress = 0;
                cooldown = 10;
            }
        }
        while (OPEN.consumeClick()) {
            if (minecraft.player != null && minecraft.screen == null && minecraft.isWindowActive()) {
                PacketDistributor.sendToServer(OpenBackpackPayload.INSTANCE);
            }
        }
    }

    @SubscribeEvent
    public static void render(net.neoforged.neoforge.client.event.RenderGuiEvent.Post event) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui || target == null || cooldown > 0 || !resolve(minecraft)) return;
        var graphics = event.getGuiGraphics();
        int x = graphics.guiWidth() / 2;
        int y = graphics.guiHeight() / 2 + 12;
        var label = net.minecraft.network.chat.Component.translatable(
                pickUp ? "gesture.poptartcore.pick_up_backpack" : "gesture.poptartcore.place_backpack");
        graphics.drawCenteredString(minecraft.font, label, x, y, 0xFFFFFFFF);
        if (progress > 0) {
            graphics.fill(x - 31, y + 12, x + 31, y + 19, 0xC0000000);
            graphics.fill(x - 30, y + 13, x - 30 + progress * 6, y + 18, 0xFFFFFFFF);
        }
    }

    @EventBusSubscriber(modid = PoptartCore.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        private Registration() {}

        @SubscribeEvent
        public static void keys(RegisterKeyMappingsEvent event) {
            event.register(OPEN);
        }
    }
}
