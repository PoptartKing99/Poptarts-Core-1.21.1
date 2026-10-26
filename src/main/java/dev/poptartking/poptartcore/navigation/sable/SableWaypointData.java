package dev.poptartking.poptartcore.navigation.sable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.poptartking.poptartcore.navigation.LodestoneRegistry;
import dev.poptartking.poptartcore.navigation.persistence.Persistent;
import dev.poptartking.poptartcore.navigation.persistence.PersistentState;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.tracking_points.SubLevelTrackingPointSavedData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class SableWaypointData implements Persistent {
    private static final Codec<SableWaypointData.Tracked> TRACKED_CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                            GlobalPos.CODEC.fieldOf("anchor").forGetter(SableWaypointData.Tracked::anchor),
                            UUIDUtil.CODEC.fieldOf("point").forGetter(SableWaypointData.Tracked::point))
                    .apply(instance, SableWaypointData.Tracked::new));
    private static final Codec<SableWaypointData.Known> KNOWN_CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                            UUIDUtil.CODEC.fieldOf("point").forGetter(SableWaypointData.Known::point),
                            GlobalPos.CODEC.fieldOf("at").forGetter(SableWaypointData.Known::at))
                    .apply(instance, SableWaypointData.Known::new));
    public static final Codec<SableWaypointData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    TRACKED_CODEC
                            .listOf()
                            .optionalFieldOf("tracked", List.of())
                            .forGetter(SableWaypointData::getTracked),
                    KNOWN_CODEC.listOf().optionalFieldOf("known", List.of()).forGetter(SableWaypointData::getKnown))
            .apply(instance, SableWaypointData::new));
    private final Map<GlobalPos, UUID> tracked = new HashMap<>();
    private final Map<UUID, GlobalPos> known = new HashMap<>();
    private Runnable dirty = () -> {};

    public SableWaypointData() {}

    private SableWaypointData(List<SableWaypointData.Tracked> tracked, List<SableWaypointData.Known> known) {
        for (SableWaypointData.Tracked entry : tracked) {
            this.tracked.put(entry.anchor(), entry.point());
        }

        for (SableWaypointData.Known entry : known) {
            this.known.put(entry.point(), entry.at());
        }
    }

    public static SableWaypointData get(MinecraftServer server) {
        return PersistentState.get(server, "sable_waypoints", CODEC, SableWaypointData::new);
    }

    @Nullable
    public UUID track(ServerLevel level, GlobalPos anchor) {
        SubLevelTrackingPointSavedData points = SubLevelTrackingPointSavedData.getOrLoad(level);
        UUID existing = this.tracked.get(anchor);
        if (existing != null && points.getTrackingPoint(existing) != null) {
            return existing;
        } else if (SableCompanion.INSTANCE.getContaining(level, anchor.pos()) instanceof ServerSubLevel subLevel) {
            UUID created = points.generateTrackingPoint(Vec3.atCenterOf(anchor.pos()), subLevel);
            if (created == null) {
                return existing;
            } else {
                if (existing != null) {
                    this.known.remove(existing);
                }

                this.tracked.put(anchor, created);
                this.dirty.run();
                return created;
            }
        } else {
            return existing;
        }
    }

    public void relocate(GlobalPos from, GlobalPos to) {
        UUID point = this.tracked.remove(from);
        if (point != null) {
            this.tracked.put(to, point);
            this.dirty.run();
        }
    }

    @Nullable
    public Vec3 locate(ServerLevel level, UUID point) {
        Vec3 projected = SableCompat.project(level, point);
        if (projected != null) {
            this.remember(level, point, projected);
            return projected;
        } else {
            GlobalPos at = this.known.get(point);
            return at == null ? null : Vec3.atCenterOf(at.pos());
        }
    }

    public void remember(ServerLevel level, UUID point, Vec3 position) {
        GlobalPos at = GlobalPos.of(level.dimension(), BlockPos.containing(position));
        if (!at.equals(this.known.put(point, at))) {
            this.dirty.run();
        }
    }

    public void sweep(MinecraftServer server) {
        LodestoneRegistry lodestones = LodestoneRegistry.get(server);

        for (GlobalPos anchor : List.copyOf(this.tracked.keySet())) {
            ServerLevel level = server.getLevel(anchor.dimension());
            if (level != null
                    && lodestones.get(anchor).isNamed()
                    && SableCompanion.INSTANCE.isInPlotGrid(level, anchor.pos())) {
                UUID point = this.track(level, anchor);
                if (point != null) {
                    this.locate(level, point);
                }
            } else {
                this.forget(level, anchor);
            }
        }

        if (this.known.entrySet().removeIf(entry -> !this.isTracked(server, entry.getKey(), entry.getValue()))) {
            this.dirty.run();
        }
    }

    @Override
    public void bindDirtyMarker(Runnable marker) {
        this.dirty = marker;
    }

    private void forget(@Nullable ServerLevel level, GlobalPos anchor) {
        UUID point = this.tracked.remove(anchor);
        if (point != null) {
            if (level != null) {
                SubLevelTrackingPointSavedData.getOrLoad(level).removeTrackingPoint(point);
            }

            this.known.remove(point);
            this.dirty.run();
        }
    }

    private boolean isTracked(MinecraftServer server, UUID point, GlobalPos at) {
        ServerLevel level = server.getLevel(at.dimension());
        return level != null && SubLevelTrackingPointSavedData.getOrLoad(level).getTrackingPoint(point) != null;
    }

    private List<SableWaypointData.Tracked> getTracked() {
        List<SableWaypointData.Tracked> entries = new ArrayList<>(this.tracked.size());

        for (Entry<GlobalPos, UUID> entry : this.tracked.entrySet()) {
            entries.add(new SableWaypointData.Tracked(entry.getKey(), entry.getValue()));
        }

        return entries;
    }

    private List<SableWaypointData.Known> getKnown() {
        List<SableWaypointData.Known> entries = new ArrayList<>(this.known.size());

        for (Entry<UUID, GlobalPos> entry : this.known.entrySet()) {
            entries.add(new SableWaypointData.Known(entry.getKey(), entry.getValue()));
        }

        return entries;
    }

    private static record Known(UUID point, GlobalPos at) {}

    private static record Tracked(GlobalPos anchor, UUID point) {}
}
