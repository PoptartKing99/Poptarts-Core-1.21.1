package dev.poptartking.poptartcore.integration.create;

import com.farcr.ragdoll.api.inventory.CorpseSlots;
import com.simibubi.create.content.equipment.armor.BacktankItem;
import com.simibubi.create.content.equipment.armor.BacktankUtil;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.equipment.AccessoryInventory;
import dev.poptartking.poptartcore.integration.ragdoll.AccessoryCorpseProvider;
import dev.poptartking.poptartcore.registry.PoptartCoreAttachments;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public final class BackSlotBacktank {
    private BackSlotBacktank() {}

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> BacktankUtil.addBacktankSupplier(entity -> {
            ItemStack stack = get(entity);
            return stack.isEmpty() ? List.of() : List.of(stack);
        }));
    }

    public static ItemStack get(Entity entity) {
        if (!(entity instanceof Player player)) return ItemStack.EMPTY;
        ItemStack stack = player.level().isClientSide
                ? player.getData(PoptartCoreAttachments.BACKTANK_VISUAL)
                : AccessoryInventory.get(player).getStackInSlot(AccessoryInventory.BACK_SLOT);
        return stack.getItem() instanceof BacktankItem ? stack : ItemStack.EMPTY;
    }

    public static ItemStack corpseVisualStack(List<ItemStack> items) {
        int base = CorpseSlots.baseSlot(AccessoryCorpseProvider.ID);
        int index = base + AccessoryInventory.BACK_SLOT;
        if (base < 0 || index >= items.size()) return ItemStack.EMPTY;
        ItemStack stack = items.get(index);
        return stack.getItem() instanceof BacktankItem ? stack.copy() : ItemStack.EMPTY;
    }

    // Rendering only. Never substitutes the player's actual equipment or armor attributes.
    public static ItemStack visualChestStack(Entity entity, ItemStack chestStack) {
        return chestStack.isEmpty() ? get(entity) : chestStack;
    }

    public static boolean canEquipChest(Entity entity, ItemStack stack) {
        return !(stack.getItem() instanceof BacktankItem) || get(entity).isEmpty();
    }

    public static boolean canEquipBack(Player player, ItemStack stack) {
        return !(stack.getItem() instanceof BacktankItem)
                || !(player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof BacktankItem);
    }

    public static void updateArmor(Player player) {
        ItemStack tank = get(player);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        for (Holder<Attribute> attribute :
                List.of(Attributes.ARMOR, Attributes.ARMOR_TOUGHNESS, Attributes.KNOCKBACK_RESISTANCE)) {
            var instance = player.getAttribute(attribute);
            if (instance == null) continue;
            var id = PoptartCore.location(
                    "backtank/" + attribute.getRegisteredName().replace(':', '/'));
            double bonus = Math.max(0, additiveStat(tank, attribute) - additiveStat(chest, attribute));
            var previous = instance.getModifier(id);
            if (previous != null && previous.amount() == bonus) continue;
            instance.removeModifier(id);
            if (bonus > 0)
                instance.addTransientModifier(new AttributeModifier(id, bonus, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private static double additiveStat(ItemStack stack, Holder<Attribute> attribute) {
        double[] amount = {0};
        stack.forEachModifier(EquipmentSlot.CHEST, (type, modifier) -> {
            if (type.equals(attribute) && modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
                amount[0] += modifier.amount();
            }
        });
        return amount[0];
    }

    @SubscribeEvent
    public static void sync(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        // Repair old saves or externally forced equipment without deleting either tank.
        ItemStack back = get(player);
        if (!back.isEmpty() && !canEquipBack(player, back)) {
            AccessoryInventory.get(player).setStackInSlot(AccessoryInventory.BACK_SLOT, ItemStack.EMPTY);
            if (!player.getInventory().add(back)) player.drop(back, false);
        }
        updateArmor(player);
        ItemStack current = get(player);
        ItemStack previous = player.getData(PoptartCoreAttachments.BACKTANK_VISUAL);
        if (!ItemStack.matches(current, previous)) {
            player.setData(PoptartCoreAttachments.BACKTANK_VISUAL, current.copy());
        }
    }
}
