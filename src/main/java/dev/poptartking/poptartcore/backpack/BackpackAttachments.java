package dev.poptartking.poptartcore.backpack;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.equipment.AccessoryInventory;
import java.util.function.Supplier;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class BackpackAttachments {
    private static final DeferredRegister<DataComponentType<?>> TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, PoptartCore.MOD_ID);
    public static final Supplier<DataComponentType<ItemContainerContents>> CONTENTS =
            TYPES.register("backpack_attachments", () -> DataComponentType.<ItemContainerContents>builder()
                    .persistent(ItemContainerContents.CODEC)
                    .networkSynchronized(ItemContainerContents.STREAM_CODEC)
                    .build());
    public static final int PRIMARY = 1;

    private BackpackAttachments() {}

    public static void register(IEventBus bus) {
        TYPES.register(bus);
    }

    public static int slots(boolean rucksack) {
        return rucksack ? 5 : 3;
    }

    public static boolean rucksack(ItemStack stack) {
        return stack.getItem() instanceof BackpackItem item && ((BackpackBlock) item.getBlock()).rows() == 6;
    }

    public static String id(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    public static boolean fits(boolean rucksack, int slot, ItemStack stack) {
        if (stack.isEmpty() || slot < 0 || slot >= slots(rucksack)) return false;
        if (slot == 0) return rucksack && stack.getItem() instanceof BannerItem;
        return switch (id(stack)) {
            case "minecraft:compass",
                    "minecraft:clock",
                    "minecraft:recovery_compass",
                    "minecraft:spyglass",
                    "minecraft:heavy_core",
                    "minecraft:totem_of_undying",
                    "minecraft:bundle",
                    "caverns_and_chasms:packing_container",
                    "nomansland:warding_effigy",
                    "supplementaries:quiver",
                    "supplementaries:lunch_basket" -> true;
            default -> false;
        };
    }

    public static NonNullList<ItemStack> items(ItemContainerContents contents) {
        var items = NonNullList.withSize(5, ItemStack.EMPTY);
        contents.copyInto(items);
        return items;
    }

    public static ItemContainerContents contents(ItemStack bag) {
        return bag.getOrDefault(CONTENTS.get(), ItemContainerContents.EMPTY);
    }

    public static ItemStack get(ItemStack bag, int slot) {
        return items(contents(bag)).get(slot);
    }

    public static void set(ItemStack bag, int slot, ItemStack stack) {
        var items = items(contents(bag));
        items.set(slot, stack.copy());
        bag.set(CONTENTS.get(), ItemContainerContents.fromItems(items));
    }

    public static ItemStack worn(Player player) {
        var bag = AccessoryInventory.get(player).getStackInSlot(AccessoryInventory.BACK_SLOT);
        return bag.getItem() instanceof BackpackItem ? bag : ItemStack.EMPTY;
    }

    public static int find(ItemStack bag, String id) {
        var items = items(contents(bag));
        for (int i = 0; i < slots(rucksack(bag)); i++) if (id(items.get(i)).equals(id)) return i;
        return -1;
    }

    public static boolean has(Player player, String id) {
        return find(worn(player), id) >= 0;
    }

    public static boolean canUse(Player player) {
        var stack = get(worn(player), PRIMARY);
        return player.isAlive()
                && !player.isSpectator()
                && fits(rucksack(worn(player)), PRIMARY, stack)
                && !stack.is(Items.TOTEM_OF_UNDYING)
                && !stack.is(Items.HEAVY_CORE);
    }
}
