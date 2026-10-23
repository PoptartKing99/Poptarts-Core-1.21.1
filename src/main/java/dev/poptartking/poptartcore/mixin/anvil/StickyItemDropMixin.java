package dev.poptartking.poptartcore.mixin.anvil;

import dev.poptartking.poptartcore.anvil.AnvilMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.class)
public abstract class StickyItemDropMixin implements IItemExtension {
    @Override
    public boolean onDroppedByPlayer(ItemStack stack, Player player) {
        return !AnvilMenu.hasModification(stack, "sticky");
    }
}
