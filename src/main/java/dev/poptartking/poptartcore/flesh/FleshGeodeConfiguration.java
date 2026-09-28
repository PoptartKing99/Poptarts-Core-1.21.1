package dev.poptartking.poptartcore.flesh;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record FleshGeodeConfiguration(
        BlockStateProvider shell,
        BlockStateProvider lining,
        BlockStateProvider air,
        Optional<BlockStateProvider> pool,
        Optional<BlockStateProvider> casing,
        Optional<BlockStateProvider> gem)
        implements FeatureConfiguration {
    public static final Codec<FleshGeodeConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    BlockStateProvider.CODEC.fieldOf("shell").forGetter(FleshGeodeConfiguration::shell),
                    BlockStateProvider.CODEC.fieldOf("lining").forGetter(FleshGeodeConfiguration::lining),
                    BlockStateProvider.CODEC
                            .optionalFieldOf("air", BlockStateProvider.simple(Blocks.AIR))
                            .forGetter(FleshGeodeConfiguration::air),
                    BlockStateProvider.CODEC.optionalFieldOf("pool").forGetter(FleshGeodeConfiguration::pool),
                    BlockStateProvider.CODEC.optionalFieldOf("casing").forGetter(FleshGeodeConfiguration::casing),
                    BlockStateProvider.CODEC.optionalFieldOf("gem").forGetter(FleshGeodeConfiguration::gem))
            .apply(instance, FleshGeodeConfiguration::new));
}
