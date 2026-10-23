package dev.poptartking.poptartcore.integration.reliablebackpacks;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.equipment.AccessoryInventory;
import dev.poptartking.poptartcore.registry.PoptartCoreAttachments;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public final class BackSlotBackpack {
    private static final ResourceKey<Item> BACKPACK = ResourceKey.create(
            Registries.ITEM, ResourceLocation.fromNamespaceAndPath("reliable_backpacks", "backpack"));

    private BackSlotBackpack() {}

    public static boolean isBackpack(ItemStack stack) {
        return stack.getItemHolder().is(BACKPACK);
    }

    public static ItemStack get(Entity entity) {
        if (!(entity instanceof Player player)) return ItemStack.EMPTY;
        ItemStack stack = player.level().isClientSide
                ? player.getData(PoptartCoreAttachments.BACKPACK_VISUAL)
                : AccessoryInventory.get(player).getStackInSlot(AccessoryInventory.BACK_SLOT);
        return isBackpack(stack) ? stack : ItemStack.EMPTY;
    }

    public static boolean canEquipChest(Entity entity, ItemStack stack) {
        return !isBackpack(stack) || get(entity).isEmpty();
    }

    public static boolean canEquipBack(Player player, ItemStack stack) {
        return !isBackpack(stack) || !isBackpack(player.getItemBySlot(EquipmentSlot.CHEST));
    }

    public static boolean hasEmptyBackSlot(Player player) {
        return AccessoryInventory.get(player)
                .getStackInSlot(AccessoryInventory.BACK_SLOT)
                .isEmpty();
    }

    public static void equip(Player player, ItemStack stack) {
        AccessoryInventory.get(player).setStackInSlot(AccessoryInventory.BACK_SLOT, stack);
    }

    public static ItemStack renderStack(ItemStack stack) {
        ItemStack visual = stack.copy();
        // Nearby players need the appearance, not the backpack's private inventory.
        visual.remove(DataComponents.CONTAINER);
        visual.remove(DataComponents.BLOCK_ENTITY_DATA);
        return visual;
    }

    @SubscribeEvent
    public static void sync(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        ItemStack back = get(player);
        if (!back.isEmpty() && !canEquipBack(player, back)) {
            equip(player, ItemStack.EMPTY);
            // Reliable Backpacks forbids carrying filled backpacks in the main inventory.
            player.drop(back, false);
        }
        ItemStack visual = renderStack(get(player));
        if (!ItemStack.matches(visual, player.getData(PoptartCoreAttachments.BACKPACK_VISUAL))) {
            player.setData(PoptartCoreAttachments.BACKPACK_VISUAL, visual);
        }
    }
}
