package dev.poptartking.poptartcore.backpack;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.equipment.AccessoryInventory;
import dev.poptartking.poptartcore.registry.PoptartCoreAttachments;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public final class BackpackVisual {
    private BackpackVisual() {}

    public static ItemStack renderStack(ItemStack stack) {
        if (!(stack.getItem() instanceof BackpackItem)) return ItemStack.EMPTY;
        var visual = stack.copyWithCount(1);
        // Rendering never exposes or modifies the real backpack inventory.
        visual.remove(DataComponents.CONTAINER);
        visual.remove(DataComponents.BLOCK_ENTITY_DATA);
        return visual;
    }

    public static ItemStack corpseVisualStack(java.util.List<ItemStack> items) {
        int base = com.farcr.ragdoll.api.inventory.CorpseSlots.baseSlot(
                dev.poptartking.poptartcore.integration.ragdoll.AccessoryCorpseProvider.ID);
        int index = base + AccessoryInventory.BACK_SLOT;
        return base >= 0 && index < items.size() ? renderStack(items.get(index)) : ItemStack.EMPTY;
    }

    @SubscribeEvent
    public static void sync(PlayerTickEvent.Post event) {
        var player = event.getEntity();
        if (player.level().isClientSide) return;
        boolean wielding = dev.poptartking.poptartcore.equipment.UtilityWield.isAttachmentWielding(player);
        if (player.getData(PoptartCoreAttachments.BACKPACK_ATTACHMENT_WIELDING) != wielding)
            player.setData(PoptartCoreAttachments.BACKPACK_ATTACHMENT_WIELDING, wielding);
        var back = AccessoryInventory.get(player).getStackInSlot(AccessoryInventory.BACK_SLOT);
        var visual = renderStack(back);
        if (!ItemStack.matches(visual, player.getData(PoptartCoreAttachments.BASIC_BACKPACK_VISUAL))) {
            player.setData(PoptartCoreAttachments.BASIC_BACKPACK_VISUAL, visual);
        }
    }
}
