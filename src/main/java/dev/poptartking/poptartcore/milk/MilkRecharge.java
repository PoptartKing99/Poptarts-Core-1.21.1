package dev.poptartking.poptartcore.milk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record MilkRecharge(float minutes, List<MilkProduct> products) {
    public static final Codec<MilkRecharge> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.FLOAT.fieldOf("minutes").forGetter(MilkRecharge::minutes),
                    MilkProduct.CODEC.listOf().fieldOf("products").forGetter(MilkRecharge::products))
            .apply(instance, MilkRecharge::new));

    public int rechargeTicks() {
        return Math.max(1, Math.round(minutes * 60.0F * 20.0F));
    }

    @Nullable
    public MilkProduct productFor(ItemStack held) {
        for (MilkProduct product : products) {
            if (held.is(product.container())) {
                return product;
            }
        }
        return null;
    }

    public float step() {
        float smallest = 1.0F;
        for (MilkProduct product : products) {
            if (product.value() > 0.0F && product.value() < smallest) {
                smallest = product.value();
            }
        }
        return smallest;
    }

    public record MilkProduct(Item container, Item result, float value) {
        public static final Codec<MilkProduct> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        BuiltInRegistries.ITEM
                                .byNameCodec()
                                .fieldOf("container")
                                .forGetter(MilkProduct::container),
                        BuiltInRegistries.ITEM.byNameCodec().fieldOf("result").forGetter(MilkProduct::result),
                        Codec.FLOAT.fieldOf("value").forGetter(MilkProduct::value))
                .apply(instance, MilkProduct::new));
    }
}
