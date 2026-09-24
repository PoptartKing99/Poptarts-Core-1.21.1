package dev.poptartking.poptartcore.lostheart;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.poptartking.poptartcore.registry.PoptartCoreParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record LostHeartEmberOptions(int owner, int stage) implements ParticleOptions {
    public static final MapCodec<LostHeartEmberOptions> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.INT.fieldOf("owner").forGetter(LostHeartEmberOptions::owner),
                    Codec.INT.fieldOf("stage").forGetter(LostHeartEmberOptions::stage))
                    .apply(instance, LostHeartEmberOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, LostHeartEmberOptions> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, LostHeartEmberOptions::owner,
                    ByteBufCodecs.VAR_INT, LostHeartEmberOptions::stage, LostHeartEmberOptions::new);

    @Override
    public ParticleType<?> getType() {
        return PoptartCoreParticles.LOST_HEART_EMBER.get();
    }
}
