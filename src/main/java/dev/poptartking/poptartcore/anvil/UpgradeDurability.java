package dev.poptartking.poptartcore.anvil;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** An item's maximum durability at a known upgrade level, including references migrated from older items. */
public record UpgradeDurability(int maxDamage, int upgrades) {
    public static final Codec<UpgradeDurability> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.intRange(1, Integer.MAX_VALUE).fieldOf("max_damage").forGetter(UpgradeDurability::maxDamage),
                    Codec.intRange(0, 3).fieldOf("upgrades").forGetter(UpgradeDurability::upgrades))
            .apply(instance, UpgradeDurability::new));
    public static final StreamCodec<ByteBuf, UpgradeDurability> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            UpgradeDurability::maxDamage,
            ByteBufCodecs.VAR_INT,
            UpgradeDurability::upgrades,
            UpgradeDurability::new);

    public UpgradeDurability {
        if (maxDamage < 1 || upgrades < 0 || upgrades > 3) {
            throw new IllegalArgumentException("Invalid upgrade durability reference");
        }
    }

    int atLevel(int level, boolean armor) {
        if (level == upgrades) return maxDamage;
        double rate = armor ? 0.15 : 0.2;
        double maximum = maxDamage * (1.0 + rate * level) / (1.0 + rate * upgrades);
        return (int) Math.min(Integer.MAX_VALUE, Math.max(1L, Math.round(maximum)));
    }
}
