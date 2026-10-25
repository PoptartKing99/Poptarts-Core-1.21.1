package dev.poptartking.poptartcore.backpack;

import dev.poptartking.poptartcore.equipment.AccessoryInventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** The server keeps the actual backpack here; the menu sends only a visual copy to clients. */
public sealed interface BackpackColorTarget {
    boolean valid(Player player);

    ItemStack snapshot();

    void setColor(BackpackColors.Part part, int rgb);

    void reopen(ServerPlayer player);

    default boolean locks(ItemStack stack) {
        return false;
    }

    record Held(Player owner, ItemStack source) implements BackpackColorTarget {
        public boolean valid(Player player) {
            if (player != owner || !player.isAlive() || player.isSpectator() || !BackpackColors.supports(source))
                return false;
            if (AccessoryInventory.get(player).getStackInSlot(AccessoryInventory.BACK_SLOT) == source) return true;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++)
                if (player.getInventory().getItem(i) == source) return true;
            return false;
        }

        public ItemStack snapshot() {
            return BackpackColors.visual(source);
        }

        public void setColor(BackpackColors.Part part, int rgb) {
            BackpackColors.set(source, part, rgb);
            owner.getInventory().setChanged();
        }

        public boolean locks(ItemStack stack) {
            return stack == source;
        }

        public void reopen(ServerPlayer player) {
            BackpackMenu.open(player, source);
        }
    }

    record Placed(BackpackBlockEntity entity) implements BackpackColorTarget {
        public boolean valid(Player player) {
            return player.isAlive() && !player.isSpectator() && entity.stillValid(player);
        }

        public ItemStack snapshot() {
            var stack = new ItemStack(entity.getBlockState().getBlock());
            stack.applyComponents(entity.collectComponents());
            return BackpackColors.visual(stack);
        }

        public void setColor(BackpackColors.Part part, int rgb) {
            entity.setColor(part, rgb);
        }

        public void reopen(ServerPlayer player) {
            BackpackMenu.switchMenu(player, entity);
        }
    }
}
