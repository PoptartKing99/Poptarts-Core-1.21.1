package dev.poptartking.poptartcore.lostheart;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public final class LostHeartEmberType extends ParticleType<LostHeartEmberOptions> {
    public LostHeartEmberType() {
        super(false);
    }

    @Override
    public MapCodec<LostHeartEmberOptions> codec() {
        return LostHeartEmberOptions.CODEC;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, LostHeartEmberOptions> streamCodec() {
        return LostHeartEmberOptions.STREAM_CODEC;
    }
}
