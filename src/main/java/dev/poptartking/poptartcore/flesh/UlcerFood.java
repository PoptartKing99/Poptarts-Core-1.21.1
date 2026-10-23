package dev.poptartking.poptartcore.flesh;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record UlcerFood(float closeChance) {
    public static final Codec<UlcerFood> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.floatRange(0.0F, 1.0F).fieldOf("close_chance").forGetter(UlcerFood::closeChance))
            .apply(instance, UlcerFood::new));
}
