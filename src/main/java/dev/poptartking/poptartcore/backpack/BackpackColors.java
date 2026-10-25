package dev.poptartking.poptartcore.backpack;

import com.mojang.serialization.Codec;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class BackpackColors {
    private static final DeferredRegister<DataComponentType<?>> TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, PoptartCore.MOD_ID);
    public static final Supplier<DataComponentType<Integer>> BODY =
            TYPES.register("backpack_body_dye", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.intRange(0, 0xFFFFFF))
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    public enum Part {
        BODY,
        FLAPS
    }

    private BackpackColors() {}

    public static void register(IEventBus bus) {
        TYPES.register(bus);
    }

    public static boolean supports(ItemStack stack) {
        return stack.is(PoptartCoreBlocks.BACKPACK.get().asItem())
                || stack.is(PoptartCoreBlocks.RUCKSACK.get().asItem());
    }

    public static boolean has(ItemStack stack, Part part) {
        return part == Part.BODY ? stack.has(BODY.get()) : stack.has(DataComponents.DYED_COLOR);
    }

    public static int color(ItemStack stack, Part part) {
        return part == Part.BODY
                ? stack.getOrDefault(BODY.get(), 0xFFFFFF)
                : DyedItemColor.getOrDefault(stack, 0xFFFFFF) & 0xFFFFFF;
    }

    public static void set(ItemStack stack, Part part, int color) {
        if (part == Part.BODY) stack.set(BODY.get(), color);
        else stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color, false));
    }

    /** Returns a preview copy. Storage, name and the other part are untouched. */
    public static ItemStack dyed(ItemStack stack, Part part, DyeItem dye) {
        var scratch = new ItemStack(Items.LEATHER_CHESTPLATE);
        if (has(stack, part)) scratch.set(DataComponents.DYED_COLOR, new DyedItemColor(color(stack, part), false));
        scratch = DyedItemColor.applyDyes(scratch, List.of(dye));
        var result = stack.copy();
        set(result, part, scratch.get(DataComponents.DYED_COLOR).rgb());
        return result;
    }

    public static ItemStack visual(ItemStack stack) {
        var copy = stack.copy();
        copy.remove(DataComponents.CONTAINER);
        copy.remove(DataComponents.BLOCK_ENTITY_DATA);
        return copy;
    }
}
