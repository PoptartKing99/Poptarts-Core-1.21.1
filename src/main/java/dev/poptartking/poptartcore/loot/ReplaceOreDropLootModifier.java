package dev.poptartking.poptartcore.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public final class ReplaceOreDropLootModifier extends LootModifier {
    public static final MapCodec<ReplaceOreDropLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> codecStart(
                    instance)
            .and(instance.group(
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("original").forGetter(modifier -> modifier.original),
                    BuiltInRegistries.ITEM
                            .byNameCodec()
                            .fieldOf("replacement")
                            .forGetter(modifier -> modifier.replacement),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(modifier -> modifier.chance)))
            .apply(instance, ReplaceOreDropLootModifier::new));

    private final Item original;
    private final Item replacement;
    private final float chance;

    public ReplaceOreDropLootModifier(LootItemCondition[] conditions, Item original, Item replacement, float chance) {
        super(conditions);
        this.original = original;
        this.replacement = replacement;
        this.chance = chance;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        ItemStack originalStack = ItemStack.EMPTY;
        for (ItemStack stack : generatedLoot) {
            if (stack.is(original)) {
                originalStack = stack;
                break;
            }
        }
        if (!originalStack.isEmpty() && context.getRandom().nextFloat() < chance) {
            // Replace one item, preserving any extra drops from Fortune.
            originalStack.shrink(1);
            generatedLoot.removeIf(ItemStack::isEmpty);
            generatedLoot.add(new ItemStack(replacement));
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
