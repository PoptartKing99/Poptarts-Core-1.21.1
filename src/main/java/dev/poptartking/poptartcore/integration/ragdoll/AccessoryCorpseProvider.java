package dev.poptartking.poptartcore.integration.ragdoll;

import com.farcr.ragdoll.api.inventory.CorpseSlotProvider;
import com.farcr.ragdoll.api.inventory.CorpseSlots;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.equipment.AccessoryInventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;

public final class AccessoryCorpseProvider implements CorpseSlotProvider {
    public static final ResourceLocation ID = PoptartCore.location("accessories");

    public static void register() {
        CorpseSlots.register(new AccessoryCorpseProvider());
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public int getSlotCount() {
        return AccessoryInventory.SIZE;
    }

    @Override
    public ItemStack getStack(Player player, int slot) {
        return player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)
                ? ItemStack.EMPTY
                : AccessoryInventory.get(player).getStackInSlot(slot);
    }

    @Override
    public void setStack(Player player, int slot, ItemStack stack) {
        if (stack.isEmpty() && player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) return;
        AccessoryInventory.get(player).setStackInSlot(slot, stack);
    }
}
