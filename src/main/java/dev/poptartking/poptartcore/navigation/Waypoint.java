package dev.poptartking.poptartcore.navigation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;

public record Waypoint(GlobalPos pos, WaypointType type, Optional<Component> name, Optional<String> icon) {
    private static final Codec<Component> NAME_CODEC = ComponentSerialization.CODEC;
    public static final Codec<Waypoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    GlobalPos.CODEC.fieldOf("pos").forGetter(Waypoint::pos),
                    WaypointType.CODEC.fieldOf("type").forGetter(Waypoint::type),
                    NAME_CODEC.optionalFieldOf("name").forGetter(Waypoint::name),
                    Codec.STRING.optionalFieldOf("icon").forGetter(Waypoint::icon))
            .apply(instance, Waypoint::new));

    public Waypoint(GlobalPos pos, WaypointType type) {
        this(pos, type, Optional.empty(), Optional.empty());
    }

    public static Waypoint lodestone(GlobalPos pos, LodestoneMark mark) {
        return new Waypoint(
                pos, WaypointType.LODESTONE, Optional.of(Component.literal(mark.name())), Optional.of(mark.icon()));
    }

    public ResourceLocation getSprite(boolean selected) {
        return this.icon.isPresent()
                ? WaypointIcons.getSprite(this.icon.get(), selected)
                : this.type.getSprite(selected);
    }

    public Component label() {
        return this.name.orElseGet(() -> Component.translatable(this.type.getTranslationKey()));
    }

    public boolean isSameAs(Waypoint other) {
        return this.type == other.type && (this.type != WaypointType.LODESTONE || this.pos.equals(other.pos));
    }
}
