package dev.poptartking.poptartcore.mixin.integration.ragdoll;

import com.farcr.ragdoll.client.render.entity.CorpseRenderer;
import com.farcr.ragdoll.content.entity.CorpseEntity;
import dev.poptartking.poptartcore.integration.create.BackSlotBacktank;
import dev.poptartking.poptartcore.integration.reliablebackpacks.BackSlotBackpack;
import dev.poptartking.poptartcore.registry.PoptartCoreAttachments;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CorpseRenderer.class)
public abstract class CorpseBacktankVisualMixin {
    @Inject(method = "getOrCreateRagdollPlayer", at = @At("RETURN"))
    private static void poptartcore$copyBacktank(
            CorpseEntity corpse, CallbackInfoReturnable<AbstractClientPlayer> cir) {
        var player = cir.getReturnValue();
        if (player == null) return;
        var capeItems = corpse.getEntityData().get(CorpseEntity.ITEMS);
        int capeBase = com.farcr.ragdoll.api.inventory.CorpseSlots.baseSlot(
                dev.poptartking.poptartcore.integration.ragdoll.AccessoryCorpseProvider.ID);
        int capeIndex = capeBase + dev.poptartking.poptartcore.equipment.AgilityCape.SLOT;
        var cape = capeBase >= 0
                        && capeIndex < capeItems.size()
                        && dev.poptartking.poptartcore.equipment.AgilityCape.texture(capeItems.get(capeIndex)) != null
                ? capeItems.get(capeIndex).copyWithCount(1)
                : ItemStack.EMPTY;
        if (!ItemStack.matches(cape, player.getData(PoptartCoreAttachments.CAPE_VISUAL)))
            player.setData(PoptartCoreAttachments.CAPE_VISUAL, cape);
        var stack = BackSlotBacktank.corpseVisualStack(corpse.getEntityData().get(CorpseEntity.ITEMS));
        if (!ItemStack.matches(stack, player.getData(PoptartCoreAttachments.BACKTANK_VISUAL))) {
            player.setData(PoptartCoreAttachments.BACKTANK_VISUAL, stack);
        }
        var items = corpse.getEntityData().get(CorpseEntity.ITEMS);
        int base = com.farcr.ragdoll.api.inventory.CorpseSlots.baseSlot(
                dev.poptartking.poptartcore.integration.ragdoll.AccessoryCorpseProvider.ID);
        int index = base + dev.poptartking.poptartcore.equipment.AccessoryInventory.BACK_SLOT;
        var backpack = base >= 0 && index < items.size() && BackSlotBackpack.isBackpack(items.get(index))
                ? BackSlotBackpack.renderStack(items.get(index))
                : ItemStack.EMPTY;
        if (!ItemStack.matches(backpack, player.getData(PoptartCoreAttachments.BACKPACK_VISUAL))) {
            player.setData(PoptartCoreAttachments.BACKPACK_VISUAL, backpack);
        }
    }
}
