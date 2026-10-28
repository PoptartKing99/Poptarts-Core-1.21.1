package dev.poptartking.poptartcore.backpack;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.equipment.AccessoryInventory;
import dev.poptartking.poptartcore.registry.PoptartCoreAttachments;
import java.util.Objects;
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

    private static boolean matchesVisual(ItemStack source, ItemStack visual) {
        if (!(source.getItem() instanceof BackpackItem)) return visual.isEmpty();
        return visual.getCount() == 1
                && ItemStack.isSameItem(source, visual)
                && !visual.has(DataComponents.CONTAINER)
                && !visual.has(DataComponents.BLOCK_ENTITY_DATA)
                && sameVisualChanges(source, visual)
                && sameVisualChanges(visual, source);
    }

    private static boolean sameVisualChanges(ItemStack source, ItemStack other) {
        // Equal item types share prototype components; compare only changes from those defaults.
        for (var entry : source.getComponentsPatch().entrySet()) {
            var type = entry.getKey();
            if (type != DataComponents.CONTAINER
                    && type != DataComponents.BLOCK_ENTITY_DATA
                    && !Objects.equals(source.get(type), other.get(type))) return false;
        }
        return true;
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
        if (!matchesVisual(back, player.getData(PoptartCoreAttachments.BASIC_BACKPACK_VISUAL))) {
            player.setData(PoptartCoreAttachments.BASIC_BACKPACK_VISUAL, renderStack(back));
        }
    }
}
