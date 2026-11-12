package dev.poptartking.poptartcore.navigation;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.poptartking.poptartcore.navigation.persistence.Persistent;
import dev.poptartking.poptartcore.navigation.persistence.PersistentState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;

public final class LodestoneRegistry implements Persistent {
    private static final Codec<LodestoneRegistry.Entry> ENTRY_CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                            GlobalPos.CODEC.fieldOf("pos").forGetter(LodestoneRegistry.Entry::pos),
                            Codec.STRING.fieldOf("name").forGetter(LodestoneRegistry.Entry::name),
                            Codec.STRING.fieldOf("icon").forGetter(LodestoneRegistry.Entry::icon))
                    .apply(instance, LodestoneRegistry.Entry::new));
    private static final Codec<LodestoneRegistry.Relocation> RELOCATION_CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                            GlobalPos.CODEC.fieldOf("from").forGetter(LodestoneRegistry.Relocation::from),
                            GlobalPos.CODEC.fieldOf("to").forGetter(LodestoneRegistry.Relocation::to))
                    .apply(instance, LodestoneRegistry.Relocation::new));
    private static final Codec<LodestoneRegistry> STATE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ENTRY_CODEC.listOf().fieldOf("marks").forGetter(LodestoneRegistry::getEntries),
                    RELOCATION_CODEC
                            .listOf()
                            .optionalFieldOf("relocations", List.of())
                            .forGetter(LodestoneRegistry::getRelocations))
            .apply(instance, LodestoneRegistry::new));
    public static final Codec<LodestoneRegistry> CODEC = Codec.either(STATE_CODEC, ENTRY_CODEC.listOf())
            .xmap(
                    stored -> (LodestoneRegistry)
                            stored.map(registry -> registry, entries -> new LodestoneRegistry(entries, List.of())),
                    registry -> Either.left(registry));
    private final Map<GlobalPos, LodestoneMark> marks = new HashMap<>();
    private final Map<GlobalPos, GlobalPos> relocations = new HashMap<>();
    private Runnable dirty = () -> {};

    public LodestoneRegistry() {}

    private LodestoneRegistry(List<LodestoneRegistry.Entry> entries, List<LodestoneRegistry.Relocation> relocations) {
        for (LodestoneRegistry.Entry entry : entries) {
            this.marks.put(entry.pos(), new LodestoneMark(entry.name(), WaypointIcons.sanitize(entry.icon())));
        }

        for (LodestoneRegistry.Relocation relocation : relocations) {
            this.relocations.put(relocation.from(), relocation.to());
        }
    }

    public static LodestoneRegistry get(MinecraftServer server) {
        return PersistentState.get(server, "lodestones", CODEC, LodestoneRegistry::new);
    }

    public LodestoneMark get(GlobalPos pos) {
        LodestoneMark mark = this.marks.get(pos);
        return mark == null ? LodestoneMark.UNNAMED : mark;
    }

    public GlobalPos resolve(GlobalPos pos) {
        GlobalPos relocated = this.relocations.get(pos);
        return relocated == null ? pos : relocated;
    }

    public void set(GlobalPos pos, LodestoneMark mark) {
        this.marks.put(pos, mark);
        this.dirty.run();
    }

    public boolean remove(GlobalPos pos) {
        boolean forwarded = this.relocations.values().removeIf(pos::equals);
        boolean removed = this.marks.remove(pos) != null;
        if (removed || forwarded) {
            this.dirty.run();
        }

        return removed;
    }

    public boolean relocate(GlobalPos from, GlobalPos to) {
        LodestoneMark mark = this.marks.remove(from);
        if (mark == null) {
            return false;
        } else {
            this.marks.put(to, mark);
            this.relocations.replaceAll((source, target) -> target.equals(from) ? to : target);
            this.relocations.put(from, to);
            this.relocations.remove(to);
            this.dirty.run();
            return true;
        }
    }

    public void claim(GlobalPos pos) {
        if (this.relocations.remove(pos) != null) {
            this.dirty.run();
        }
    }

    @Override
    public void bindDirtyMarker(Runnable marker) {
        this.dirty = marker;
    }

    private List<LodestoneRegistry.Entry> getEntries() {
        List<LodestoneRegistry.Entry> entries = new ArrayList<>(this.marks.size());

        for (Map.Entry<GlobalPos, LodestoneMark> mark : this.marks.entrySet()) {
            entries.add(new LodestoneRegistry.Entry(
                    mark.getKey(), mark.getValue().name(), mark.getValue().icon()));
        }

        return entries;
    }

    private List<LodestoneRegistry.Relocation> getRelocations() {
        List<LodestoneRegistry.Relocation> relocations = new ArrayList<>(this.relocations.size());

        for (Map.Entry<GlobalPos, GlobalPos> relocation : this.relocations.entrySet()) {
            relocations.add(new LodestoneRegistry.Relocation(relocation.getKey(), relocation.getValue()));
        }

        return relocations;
    }

    private static record Entry(GlobalPos pos, String name, String icon) {}

    private static record Relocation(GlobalPos from, GlobalPos to) {}
}
