package dev.poptartking.poptartcore.navigation;

import dev.poptartking.poptartcore.navigation.network.HideWaypoint;
import dev.poptartking.poptartcore.navigation.network.SelectWaypoint;
import dev.poptartking.poptartcore.navigation.network.ToggleNavigation;
import dev.poptartking.poptartcore.registry.PoptartCoreSounds;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class Waypoints {
    public static final int REFRESH_INTERVAL = 10;

    private Waypoints() {}

    public static CompassWaypoints get(ItemStack compass) {
        return compass.getOrDefault(NavigationComponents.WAYPOINTS.get(), CompassWaypoints.EMPTY);
    }

    @Nullable
    public static Vec3 locate(ServerLevel level, Waypoint waypoint) {
        GlobalPos pos = waypoint.type() == WaypointType.LODESTONE
                ? LodestoneRegistry.get(level.getServer()).resolve(waypoint.pos())
                : waypoint.pos();
        return !pos.dimension().equals(level.dimension())
                ? null
                : WaypointAnchors.locate(level, new Waypoint(pos, waypoint.type(), waypoint.name(), waypoint.icon()));
    }

    public static boolean refresh(ItemStack compass, ServerPlayer player) {
        CompassWaypoints current = get(compass);
        List<Waypoint> rebuilt = new ArrayList<>();
        ServerLevel level = player.serverLevel();
        rebuilt.add(new Waypoint(
                GlobalPos.of(Level.OVERWORLD, level.getServer().overworld().getSharedSpawnPos()), WaypointType.ORIGIN));
        BlockPos respawn = player.getRespawnPosition();
        if (respawn != null) {
            rebuilt.add(new Waypoint(GlobalPos.of(player.getRespawnDimension(), respawn), WaypointType.RESPAWN));
        }

        LodestoneRegistry lodestones = LodestoneRegistry.get(level.getServer());

        for (Waypoint waypoint : current.waypoints()) {
            if (waypoint.type() == WaypointType.LODESTONE) {
                GlobalPos pos = lodestones.resolve(waypoint.pos());
                LodestoneMark mark = lodestones.get(pos);
                if (mark.isNamed() && lodestoneExists(level, pos)) {
                    rebuilt.add(Waypoint.lodestone(pos, mark));
                }
            }
        }

        Waypoint previous = current.getSelected();
        int selected = 0;
        if (previous != null) {
            Waypoint target = previous.type() == WaypointType.LODESTONE
                    ? new Waypoint(lodestones.resolve(previous.pos()), WaypointType.LODESTONE)
                    : previous;

            for (int index = 0; index < rebuilt.size(); index++) {
                if (rebuilt.get(index).isSameAs(target)) {
                    selected = index;
                    break;
                }
            }
        }

        CompassWaypoints updated =
                new CompassWaypoints(List.copyOf(rebuilt), selected, current.hidden(), current.navigationHidden());
        if (updated.equals(current)) {
            return false;
        } else {
            NavigationComponents.write(compass, updated);
            return true;
        }
    }

    public static void onSelect(SelectWaypoint packet, ServerPlayer player) {
        if (!player.isSpectator()) {
            mutateCompass(player, packet.source(), compass -> {
                CompassWaypoints current = get(compass);
                if (!current.isEmpty()) {
                    int index = Math.floorMod(packet.index(), current.size());
                    if (index != current.getSelectedIndex() && !current.isHidden(index)) {
                        NavigationComponents.write(compass, current.withSelected(index));
                        play(player, player.blockPosition(), PoptartCoreSounds.WAYPOINT_SELECT.get(), 0.5F, 1.4F);
                    }
                }
            });
        }
    }

    public static void onHide(HideWaypoint packet, ServerPlayer player) {
        if (!player.isSpectator()) {
            mutateCompass(player, packet.source(), compass -> {
                CompassWaypoints current = get(compass);
                int index = packet.index();
                if (current.get(index) != null) {
                    CompassWaypoints toggled = current.toggleHidden(index);
                    if (toggled.isHidden(index) && current.getSelectedIndex() == index) {
                        int fallback = packet.fallback();
                        int origin = toggled.indexOf(WaypointType.ORIGIN);
                        int target = toggled.get(fallback) != null && !toggled.isHidden(fallback)
                                ? fallback
                                : (origin >= 0 ? origin : toggled.getFirstVisible());
                        toggled = toggled.withSelected(target);
                    }

                    NavigationComponents.write(compass, toggled);
                    play(player, player.blockPosition(), PoptartCoreSounds.WAYPOINT_HIDE.get(), 0.5F, 0.9F);
                }
            });
        }
    }

    public static void onToggleNavigation(ToggleNavigation packet, ServerPlayer player) {
        if (!player.isSpectator()) {
            mutateCompass(player, packet.source(), compass -> {
                CompassWaypoints current = get(compass);
                NavigationComponents.write(compass, current.withNavigationHidden(!current.navigationHidden()));
            });
        }
    }

    public static void refreshAttached(ServerPlayer player) {
        CompassAccess.refreshAttached(player);
    }

    public static void refreshCarried(ServerPlayer player) {
        CompassAccess.refreshCarried(player);
    }

    /** Remember a named stone without selecting it or changing hidden marker preferences. */
    public static void rememberLodestone(ItemStack compass, ServerPlayer player, GlobalPos pos, LodestoneMark mark) {
        if (!compass.is(net.minecraft.world.item.Items.COMPASS) || !mark.isNamed()) return;
        refresh(compass, player);
        CompassWaypoints current = get(compass);
        if (current.waypoints().stream().anyMatch(waypoint -> waypoint.pos().equals(pos))) return;
        if (current.waypoints().stream()
                        .filter(waypoint -> waypoint.type() == WaypointType.LODESTONE)
                        .count()
                >= NavigationComponents.MAX_LODESTONES) return;
        List<Waypoint> updated = new ArrayList<>(current.waypoints());
        updated.add(Waypoint.lodestone(pos, mark));
        NavigationComponents.write(
                compass,
                new CompassWaypoints(
                        updated, current.getSelectedIndex(), current.hidden(), current.navigationHidden()));
    }

    public static void bindLodestone(ItemStack compass, ServerPlayer player, GlobalPos pos, LodestoneMark mark) {
        CompassWaypoints current = get(compass);
        List<Waypoint> waypoints = new ArrayList<>(current.waypoints());

        for (int index = 0; index < waypoints.size(); index++) {
            if (waypoints.get(index).pos().equals(pos)) {
                NavigationComponents.write(compass, current.withSelected(index));
                play(player, pos.pos(), SoundEvents.LODESTONE_COMPASS_LOCK, 1.0F, 1.0F);
                return;
            }
        }

        int lodestones = 0;

        for (Waypoint waypoint : waypoints) {
            if (waypoint.type() == WaypointType.LODESTONE) {
                lodestones++;
            }
        }

        int limit = NavigationComponents.MAX_LODESTONES;
        if (lodestones >= limit) {
            play(player, pos.pos(), PoptartCoreSounds.COMPASS_FULL.get(), 0.8F, 1.0F);
            NavigationMessages.overlay(
                    player, Component.translatable("poptartcore.compass.full", new Object[] {limit}));
        } else {
            waypoints.add(Waypoint.lodestone(pos, mark));
            NavigationComponents.write(
                    compass,
                    new CompassWaypoints(
                            List.copyOf(waypoints),
                            waypoints.size() - 1,
                            current.hidden(),
                            current.navigationHidden()));
            play(player, pos.pos(), SoundEvents.LODESTONE_COMPASS_LOCK, 1.0F, 1.0F);
        }
    }

    private static void mutateCompass(ServerPlayer player, CompassSource source, Consumer<ItemStack> mutation) {
        CompassAccess.mutate(player, source, mutation);
    }

    private static void play(ServerPlayer player, BlockPos pos, SoundEvent sound, float volume, float pitch) {
        player.level().playSound(null, pos, sound, SoundSource.PLAYERS, volume, pitch);
    }

    private static boolean lodestoneExists(ServerLevel level, GlobalPos pos) {
        if (!pos.dimension().equals(level.dimension())) {
            return true;
        } else {
            BlockPos block = pos.pos();
            if (!level.isInWorldBounds(block)) {
                return false;
            } else {
                return !WaypointAnchors.getTracker().isMovable(level, block)
                        ? level.getPoiManager().existsAtPosition(PoiTypes.LODESTONE, block)
                        : !level.isLoaded(block) || Lodestones.is(level.getBlockState(block));
            }
        }
    }
}
