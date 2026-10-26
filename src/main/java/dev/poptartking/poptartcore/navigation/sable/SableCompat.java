package dev.poptartking.poptartcore.navigation.sable;

import dev.poptartking.poptartcore.navigation.Lodestones;
import dev.poptartking.poptartcore.navigation.Waypoint;
import dev.poptartking.poptartcore.navigation.WaypointAnchors;
import dev.poptartking.poptartcore.navigation.WaypointType;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper.AssemblyTransform;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.ClientSubLevelAccess;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.mixinterface.respawn_point.ServerPlayerRespawnExtension;
import dev.ryanhcode.sable.sublevel.storage.HoldingSubLevel;
import dev.ryanhcode.sable.sublevel.tracking_points.SubLevelTrackingPointSavedData;
import dev.ryanhcode.sable.sublevel.tracking_points.TrackingPoint;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent.Post;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

public final class SableCompat implements WaypointAnchors.Tracker {
    private static final int SWEEP_INTERVAL = 20;

    private SableCompat() {}

    public static void init() {
        WaypointAnchors.setTracker(new SableCompat());
        NeoForge.EVENT_BUS.addListener(SableCompat::onServerTick);
    }

    public static void moveBlocks(
            ServerLevel level, AssemblyTransform transform, Iterable<BlockPos> blocks, Runnable move) {
        Map<BlockPos, BlockPos> lodestones = new HashMap<>();

        for (BlockPos block : blocks) {
            if (Lodestones.is(level.getBlockState(block))) {
                lodestones.put(block.immutable(), transform.apply(block));
            }
        }

        if (lodestones.isEmpty()) {
            move.run();
        } else {
            ServerLevel destination = transform.getLevel();
            Lodestones.move(level, destination, lodestones, move);
            SableWaypointData data = SableWaypointData.get(level.getServer());

            for (Entry<BlockPos, BlockPos> lodestone : lodestones.entrySet()) {
                data.relocate(
                        GlobalPos.of(level.dimension(), lodestone.getKey()),
                        GlobalPos.of(destination.dimension(), lodestone.getValue()));
            }
        }
    }

    @Nullable
    public static Vec3 project(ServerLevel level, UUID uuid) {
        TrackingPoint point = SubLevelTrackingPointSavedData.getOrLoad(level).getTrackingPoint(uuid);
        if (point == null) {
            return null;
        } else {
            Vector3d local = point.point();
            if (!point.inSubLevel()) {
                return new Vec3(local.x, local.y, local.z);
            } else {
                SubLevelAccess loaded = SableCompanion.INSTANCE.getContaining(level, local);
                if (loaded != null) {
                    return toVec3(loaded.logicalPose().transformPosition(local, new Vector3d()));
                } else if (point.subLevelID() == null) {
                    return null;
                } else {
                    ServerSubLevelContainer container = SubLevelContainer.getContainer(level);
                    HoldingSubLevel holding = container == null
                            ? null
                            : container.getHoldingChunkMap().getHoldingSubLevel(point.subLevelID());
                    return holding == null
                            ? null
                            : toVec3(holding.data().pose().transformPosition(local, new Vector3d()));
                }
            }
        }
    }

    @Override
    public boolean isMovable(Level level, BlockPos anchor) {
        return SableCompanion.INSTANCE.isInPlotGrid(level, anchor);
    }

    @Nullable
    @Override
    public Vec3 locate(Level level, BlockPos anchor, float partialTick) {
        SubLevelAccess subLevel = SableCompanion.INSTANCE.getContaining(level, anchor);
        if (subLevel == null) {
            return null;
        } else {
            Pose3dc pose = subLevel instanceof ClientSubLevelAccess client
                    ? client.renderPose(partialTick)
                    : subLevel.logicalPose();
            return pose.transformPosition(Vec3.atCenterOf(anchor));
        }
    }

    @Nullable
    @Override
    public Vec3 locate(ServerPlayer player, Waypoint waypoint) {
        ServerLevel level = player.server.getLevel(waypoint.pos().dimension());
        if (level == null) {
            return null;
        } else {
            UUID respawn = waypoint.type() == WaypointType.RESPAWN ? getRespawnPoint(player) : null;
            return respawn != null
                    ? SableWaypointData.get(player.server).locate(level, respawn)
                    : this.locate(level, waypoint);
        }
    }

    @Nullable
    @Override
    public Vec3 locate(ServerLevel level, Waypoint waypoint) {
        SableWaypointData data = SableWaypointData.get(level.getServer());
        Vec3 live = this.locate(level, waypoint.pos().pos(), 1.0F);
        UUID point = waypoint.type() == WaypointType.LODESTONE ? data.track(level, waypoint.pos()) : null;
        if (live != null) {
            if (point != null) {
                data.remember(level, point, live);
            }

            return live;
        } else {
            return point == null ? null : data.locate(level, point);
        }
    }

    private static void onServerTick(Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % 20 == 0) {
            SableWaypointData data = SableWaypointData.get(server);
            data.sweep(server);

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                UUID respawn = getRespawnPoint(player);
                ServerLevel level = server.getLevel(player.getRespawnDimension());
                if (respawn != null && level != null) {
                    data.locate(level, respawn);
                }
            }
        }
    }

    @Nullable
    private static UUID getRespawnPoint(ServerPlayer player) {
        return ((ServerPlayerRespawnExtension) player).sable$getRespawnPoint();
    }

    private static Vec3 toVec3(Vector3d vector) {
        return new Vec3(vector.x, vector.y, vector.z);
    }
}
