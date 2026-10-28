package dev.poptartking.poptartcore.navigation.client.hud;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.mixin.navigation.NavigationTabOverlayAccessor;
import dev.poptartking.poptartcore.navigation.CompassSource;
import dev.poptartking.poptartcore.navigation.CompassWaypoints;
import dev.poptartking.poptartcore.navigation.Waypoint;
import dev.poptartking.poptartcore.navigation.Waypoints;
import dev.poptartking.poptartcore.navigation.client.ClientWaypoints;
import dev.poptartking.poptartcore.navigation.network.HideWaypoint;
import dev.poptartking.poptartcore.navigation.network.SelectWaypoint;
import dev.poptartking.poptartcore.navigation.network.ToggleNavigation;
import dev.poptartking.poptartcore.registry.PoptartCoreSounds;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public final class CompassHud {
    private static final ResourceLocation DEATH_MARKER = PoptartCore.location("waypoints/default");
    private static float open;
    private static float shown;
    private static CompassWaypoints shownData = CompassWaypoints.EMPTY;
    private static boolean shownCompass;
    private static boolean shownRecovery;

    @Nullable
    private static Boolean predictedHidden;

    private static long predictionExpiry;
    private static int highlighted = -1;
    private static int previousHighlighted = -1;
    private static int priorSelection = -1;
    private static int committed = -1;
    private static int usedTicks;
    private static boolean wasUsing;
    private static boolean wasActive;
    private static boolean sessionLocal;
    private static boolean pendingLocalUse;
    private static long localUseTime;
    private static CompassSource source = CompassSource.HELD;

    private CompassHud() {}

    public static void render(GuiGraphics graphics, HudTime time) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        boolean chatting = minecraft.screen instanceof ChatScreen;
        if (player != null && minecraft.level != null) {
            if (!minecraft.options.hideGui && (minecraft.screen == null || chatting) && !player.isSpectator()) {
                if (chatting) {
                    close();
                }

                ItemStack compass = carried(player, Items.COMPASS);
                ItemStack recovery = carried(player, Items.RECOVERY_COMPASS);
                boolean present = !compass.isEmpty() || !recovery.isEmpty();
                if (present) {
                    shownData = Waypoints.get(compass);
                    shownCompass = !compass.isEmpty();
                    shownRecovery = !recovery.isEmpty();
                } else {
                    close();
                    if (shown <= 0.01F) {
                        reset();
                        return;
                    }
                }

                CompassWaypoints data = shownData;
                float partialTick = time.partialTick();
                double x = Mth.lerp((double) partialTick, player.xo, player.getX());
                double z = Mth.lerp((double) partialTick, player.zo, player.getZ());
                float yaw = player.getViewYRot(partialTick);
                ResourceKey<Level> dimension = player.level().dimension();
                List<CompassHud.Entry> entries = new ArrayList<>();
                List<Waypoint> waypoints = data.waypoints();

                for (int index = 0; index < waypoints.size(); index++) {
                    Waypoint waypoint = waypoints.get(index);
                    if (waypoint.pos().dimension().equals(dimension)) {
                        Vec3 target = ClientWaypoints.locate(player.level(), waypoint.pos(), partialTick);
                        if (target != null) {
                            double dx = target.x - x;
                            double dz = target.z - z;
                            entries.add(new CompassHud.Entry(
                                    waypoint,
                                    index,
                                    relativeYaw(dx, dz, yaw),
                                    Math.sqrt(dx * dx + dz * dz),
                                    data.isHidden(index)));
                        }
                    }
                }

                Optional<GlobalPos> death =
                        dev.poptartking.poptartcore.navigation.CorpseDeathMarkers.deathTarget(player, shownRecovery);
                Float deathYaw = null;
                if (death.isPresent() && death.get().dimension().equals(dimension)) {
                    deathYaw = relativeYaw(
                            (double) death.get().pos().getX() + 0.5 - x,
                            (double) death.get().pos().getZ() + 0.5 - z,
                            yaw);
                }

                highlighted = -1;
                float bestYaw = Float.MAX_VALUE;

                for (CompassHud.Entry entry : entries) {
                    if (Math.abs(entry.relativeYaw()) < bestYaw) {
                        bestYaw = Math.abs(entry.relativeYaw());
                        highlighted = entry.index();
                    }
                }

                long gameTime = minecraft.level.getGameTime();
                boolean active = !chatting
                        && isHeld(player, compass)
                        && player.isUsingItem()
                        && player.getUseItem().is(Items.COMPASS);
                if (active && !wasActive) {
                    sessionLocal = pendingLocalUse && gameTime - localUseTime <= 2L;
                    pendingLocalUse = false;
                }

                wasActive = active;
                boolean using = active && sessionLocal;
                if (using && !wasUsing) {
                    previousHighlighted = -1;
                    priorSelection = -1;
                    source = sourceOf(player, compass);
                }

                if (using) {
                    usedTicks = player.getTicksUsingItem();
                }

                boolean selecting = using && usedTicks >= 4;
                if (selecting && wasUsing && highlighted >= 0 && highlighted != previousHighlighted) {
                    minecraft
                            .getSoundManager()
                            .play(SimpleSoundInstance.forUI(PoptartCoreSounds.WAYPOINT_SELECT.get(), 1.4F, 0.5F));
                }

                if (highlighted != previousHighlighted && previousHighlighted >= 0) {
                    priorSelection = previousHighlighted;
                }

                previousHighlighted = highlighted;
                committed = compass.isEmpty() ? -1 : data.getSelectedIndex();
                float previousOpen = open;
                open = Mth.approach(open, selecting ? 1.0F : 0.0F, time.frameTicks() * 0.45F);
                if (predictedHidden != null
                        && (predictedHidden == data.navigationHidden() || gameTime >= predictionExpiry)) {
                    predictedHidden = null;
                }

                boolean barHidden =
                        shownCompass && (predictedHidden != null ? predictedHidden : data.navigationHidden());
                boolean tabbing = ((NavigationTabOverlayAccessor) minecraft.gui.getTabList()).poptartcore$isVisible();
                shown = Mth.approach(shown, present && !barHidden && !tabbing ? 1.0F : 0.0F, time.frameTicks() * 0.45F);
                if (shown > 0.01F) {
                    renderBar(graphics, minecraft.font, entries, data, yaw, shownCompass, deathYaw, shown);
                }

                if (open > 0.01F) {
                    renderSelector(graphics, minecraft.font, entries);
                }

                if (wasUsing && !using && !chatting && usedTicks < 4) {
                    predictedHidden = !barHidden;
                    predictionExpiry = gameTime + 20L;
                    minecraft
                            .getSoundManager()
                            .play(SimpleSoundInstance.forUI(
                                    PoptartCoreSounds.NAVIGATION_TOGGLE.get(), predictedHidden ? 0.8F : 1.1F, 0.5F));
                    PacketDistributor.sendToServer(new ToggleNavigation(source));
                } else if (wasUsing
                        && !using
                        && previousOpen > 0.4F
                        && highlighted >= 0
                        && highlighted != data.getSelectedIndex()
                        && !data.isHidden(highlighted)) {
                    PacketDistributor.sendToServer(new SelectWaypoint(highlighted, source));
                }

                wasUsing = using;
            } else {
                close();
            }
        } else {
            reset();
        }
    }

    public static void markLocalUse(long gameTime) {
        pendingLocalUse = true;
        localUseTime = gameTime;
    }

    public static int getLiveHighlight(ItemStack stack) {
        if (!(open <= 0.01F) && highlighted >= 0) {
            LocalPlayer player = Minecraft.getInstance().player;
            return player != null && isHeld(player, stack) ? highlighted : -1;
        } else {
            return -1;
        }
    }

    public static boolean tryHide() {
        if (!(open <= 0.01F) && wasUsing && highlighted >= 0) {
            PacketDistributor.sendToServer(
                    new HideWaypoint(highlighted, highlighted == committed ? priorSelection : -1, source));
            return true;
        } else {
            return false;
        }
    }

    private static void close() {
        open = 0.0F;
        wasUsing = false;
        wasActive = false;
    }

    public static void reset() {
        close();
        shown = 0.0F;
        shownData = CompassWaypoints.EMPTY;
        shownCompass = false;
        shownRecovery = false;
        predictedHidden = null;
    }

    private static void renderBar(
            GuiGraphics graphics,
            Font font,
            List<CompassHud.Entry> entries,
            CompassWaypoints data,
            float yaw,
            boolean showCardinals,
            @Nullable Float deathYaw,
            float visibility) {
        float alpha = (1.0F - open) * visibility;
        if (!(alpha <= 0.01F)) {
            int centerX = graphics.guiWidth() / 2;
            float fit = Math.min(1.0F, (float) graphics.guiHeight() / 270.0F);
            float halfWidth = Math.min(100.0F, (float) graphics.guiWidth() * 0.45F);
            int marker = Math.round(12.0F * fit);
            int markerY = 4 + 9 + 1;

            for (float degrees = -70.0F; degrees <= 70.0F; degrees += 15.0F) {
                int tickX = centerX + Math.round(degrees / 70.0F * halfWidth);
                Draw.fill(graphics, tickX, 4, tickX + 1, 7, Draw.withAlpha(16777215, 0.4F * alpha));
            }

            if (showCardinals) {
                drawCardinal(graphics, font, "N", 180.0F, yaw, centerX, halfWidth, alpha);
                drawCardinal(graphics, font, "E", -90.0F, yaw, centerX, halfWidth, alpha);
                drawCardinal(graphics, font, "S", 0.0F, yaw, centerX, halfWidth, alpha);
                drawCardinal(graphics, font, "W", 90.0F, yaw, centerX, halfWidth, alpha);
            }

            int selected = data.getSelectedIndex();

            for (CompassHud.Entry entry : entries) {
                if (!entry.hidden() && !(Math.abs(entry.relativeYaw()) > 70.0F)) {
                    int markerX = centerX + Math.round(entry.relativeYaw() / 70.0F * halfWidth);
                    boolean isSelected = entry.index() == selected;
                    int size = isSelected ? marker + 2 : marker;
                    Draw.sprite(
                            graphics,
                            entry.waypoint().getSprite(isSelected),
                            markerX - size / 2,
                            markerY,
                            size,
                            size,
                            1.0F,
                            1.0F,
                            1.0F,
                            alpha * (isSelected ? 1.0F : 0.65F));
                }
            }

            if (deathYaw != null && Math.abs(deathYaw) <= 70.0F) {
                int deathX = centerX + Math.round(deathYaw / 70.0F * halfWidth);
                int size = marker + 2;
                Draw.sprite(graphics, DEATH_MARKER, deathX - size / 2, markerY, size, size, 1.0F, 0.35F, 0.35F, alpha);
            }
        }
    }

    private static void renderSelector(GuiGraphics graphics, Font font, List<CompassHud.Entry> entries) {
        int centerX = graphics.guiWidth() / 2;
        int centerY = graphics.guiHeight() / 2;
        float ease = Draw.easeOutCubic(open);
        float fit = Math.min(1.0F, (float) graphics.guiHeight() / 270.0F);
        float innerRadius = 42.0F * fit;
        float outerRadius = 96.0F * fit;
        int spriteBase = Math.round(15.0F * fit);
        int spriteHot = Math.round(20.0F * fit);
        double minDistance = Double.MAX_VALUE;
        double maxDistance = -Double.MAX_VALUE;

        for (CompassHud.Entry entry : entries) {
            minDistance = Math.min(minDistance, entry.distance());
            maxDistance = Math.max(maxDistance, entry.distance());
        }

        float midRadius = (innerRadius + outerRadius) * 0.5F;
        float halfRange = (outerRadius - innerRadius) * 0.5F;
        double anchor = Math.sqrt(Math.max(1.0, minDistance) * Math.max(1.0, maxDistance));
        CompassHud.Entry top = null;

        for (CompassHud.Entry entry : entries) {
            double offset = Mth.clamp(2.0 * Math.log10(Math.max(1.0, entry.distance()) / anchor), -1.0, 1.0);
            float radius = (midRadius + (float) offset * halfRange) * ease;
            float angle = (float) Math.toRadians((double) entry.relativeYaw());
            float spriteX = (float) centerX + Mth.sin(angle) * radius;
            float spriteY = (float) centerY - Mth.cos(angle) * radius;
            boolean isHighlighted = entry.index() == highlighted;
            if (isHighlighted) {
                top = entry;
            }

            int size = isHighlighted ? spriteHot : spriteBase;
            float gray = entry.hidden() ? 0.4F : 1.0F;
            float baseAlpha = entry.hidden() ? 0.55F : 0.8F;
            Draw.sprite(
                    graphics,
                    entry.waypoint().getSprite(isHighlighted),
                    Math.round(spriteX) - size / 2,
                    Math.round(spriteY) - size / 2,
                    size,
                    size,
                    gray,
                    gray,
                    gray,
                    ease * (isHighlighted ? 1.0F : baseAlpha));
        }

        if (top != null) {
            int textAlpha = (int) (ease * 255.0F) << 24;
            int distanceY = centerY - Math.round(outerRadius * ease) - 3 - 9;
            int nameY = distanceY - 9;
            Draw.centeredText(
                    graphics,
                    font,
                    top.waypoint().label(),
                    centerX,
                    nameY,
                    (top.hidden() ? 8421504 : 16777215) | textAlpha);
            Draw.centeredText(
                    graphics,
                    font,
                    Component.literal((int) top.distance() + "m"),
                    centerX,
                    distanceY,
                    10526880 | textAlpha);
        }
    }

    private static void drawCardinal(
            GuiGraphics graphics,
            Font font,
            String letter,
            float worldYaw,
            float yaw,
            int centerX,
            float halfWidth,
            float alpha) {
        float relative = Mth.wrapDegrees(worldYaw - yaw);
        if (!(Math.abs(relative) > 70.0F)) {
            int letterX = centerX + Math.round(relative / 70.0F * halfWidth);
            Draw.centeredText(graphics, font, Component.literal(letter), letterX, 4, Draw.withAlpha(16777215, alpha));
        }
    }

    private static float relativeYaw(double dx, double dz, float viewYaw) {
        return Mth.wrapDegrees((float) Math.toDegrees(Math.atan2(-dx, dz)) - viewYaw);
    }

    private static ItemStack carried(LocalPlayer player, Item item) {
        ItemStack main = player.getMainHandItem();
        if (main.is(item)) {
            return main;
        } else {
            ItemStack off = player.getOffhandItem();
            if (off.is(item)) {
                return off;
            } else {
                ItemStack slotted = dev.poptartking.poptartcore.navigation.CompassAccess.carried(player, item);
                return !slotted.isEmpty()
                        ? slotted
                        : dev.poptartking.poptartcore.navigation.CompassAccess.attached(player, item);
            }
        }
    }

    private static CompassSource sourceOf(LocalPlayer player, ItemStack compass) {
        if (dev.poptartking.poptartcore.equipment.UtilityWield.isAttachmentWielding(player)) {
            return CompassSource.ATTACHED;
        } else {
            return dev.poptartking.poptartcore.equipment.UtilityWield.isWielding(player)
                            || compass
                                    == dev.poptartking.poptartcore.navigation.CompassAccess.carried(
                                            player, Items.COMPASS)
                    ? CompassSource.CARRIED
                    : CompassSource.HELD;
        }
    }

    private static boolean isHeld(LocalPlayer player, ItemStack stack) {
        return !stack.isEmpty() && (player.getMainHandItem() == stack || player.getOffhandItem() == stack);
    }

    private static record Entry(Waypoint waypoint, int index, float relativeYaw, double distance, boolean hidden) {}
}
