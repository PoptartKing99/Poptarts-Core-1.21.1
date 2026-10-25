package dev.poptartking.poptartcore.backpack;

import dev.poptartking.poptartcore.equipment.AccessoryInventory;
import dev.poptartking.poptartcore.registry.PoptartCoreMenus;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public final class BackpackMenu extends ChestMenu {
    public static final int CAPACITY = 27;
    private final ItemStack source;
    private final Player owner;
    private final boolean portable;
    private final int capacity;
    private final net.minecraft.world.inventory.SimpleContainerData attachmentEditing =
            new net.minecraft.world.inventory.SimpleContainerData(1);

    public BackpackMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(CAPACITY), ItemStack.EMPTY);
    }

    public static BackpackMenu rucksack(int id, Inventory inventory) {
        return new BackpackMenu(id, inventory, new SimpleContainer(54), ItemStack.EMPTY);
    }

    public BackpackMenu(int id, Inventory inventory, Container contents, ItemStack source) {
        super(
                contents.getContainerSize() == 54 ? PoptartCoreMenus.RUCKSACK.get() : PoptartCoreMenus.BACKPACK.get(),
                id,
                inventory,
                contents,
                contents.getContainerSize() / 9);
        this.source = source;
        this.owner = inventory.player;
        this.portable = !source.isEmpty();
        this.capacity = contents.getContainerSize();
        attachmentEditing.set(0, contents instanceof BackpackBlockEntity ? 1 : 0);
        addDataSlots(attachmentEditing);
        for (int i = 0; i < capacity; i++) {
            Slot original = slots.get(i);
            Slot restricted = new Slot(contents, i, original.x, original.y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return canStore(stack);
                }
            };
            restricted.index = original.index;
            slots.set(i, restricted);
        }
        boolean rucksack = capacity == 54;
        var mounted = new SimpleContainer(BackpackAttachments.slots(rucksack));
        var installed = portable
                ? BackpackAttachments.contents(source)
                : contents instanceof BackpackBlockEntity entity ? entity.attachments() : ItemContainerContents.EMPTY;
        installed.copyInto(mounted.getItems());
        mounted.addListener(changed -> {
            if (!stillValid(owner) || !canEditAttachments()) return;
            var updated = ItemContainerContents.fromItems(mounted.getItems());
            if (portable) source.set(BackpackAttachments.CONTENTS.get(), updated);
            else if (contents instanceof BackpackBlockEntity entity) entity.setAttachments(updated);
        });
        for (int i = 0; i < mounted.getContainerSize(); i++) {
            final int mount = i;
            int x = -27;
            int mountTop = rucksack ? 36 : 18;
            int y = i == 0 ? mountTop - 22 : mountTop + (i - 1) * 18;
            addSlot(new Slot(mounted, i, x, y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return canEditAttachments() && BackpackAttachments.fits(rucksack, mount, stack);
                }

                @Override
                public boolean mayPickup(Player player) {
                    return canEditAttachments();
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }

                @Override
                public boolean isActive() {
                    return canEditAttachments() && (mount != 0 || rucksack);
                }
            });
        }
    }

    public static boolean canStore(ItemStack stack) {
        return stack.getItem().canFitInsideContainerItems();
    }

    public boolean canEditAttachments() {
        return attachmentEditing.get(0) == 1;
    }

    public static SimpleContainer contents(ItemStack stack) {
        var item = (BackpackItem) stack.getItem();
        var inventory = new SimpleContainer(((BackpackBlock) item.getBlock()).rows() * 9);
        stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
                .copyInto(inventory.getItems());
        inventory.addListener(
                changed -> stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(inventory.getItems())));
        return inventory;
    }

    public static void open(ServerPlayer player, ItemStack stack) {
        if (!(stack.getItem() instanceof BackpackItem) || player.isSpectator()) return;
        switchMenu(
                player,
                new SimpleMenuProvider(
                        (id, inventory, opener) -> new BackpackMenu(id, inventory, contents(stack), stack),
                        stack.getHoverName()));
    }

    public static void switchMenu(ServerPlayer player, net.minecraft.world.MenuProvider provider) {
        player.openMenu(new net.minecraft.world.MenuProvider() {
            @Override
            public net.minecraft.network.chat.Component getDisplayName() {
                return provider.getDisplayName();
            }

            @Override
            public net.minecraft.world.inventory.AbstractContainerMenu createMenu(
                    int id, Inventory inventory, Player opener) {
                return provider.createMenu(id, inventory, opener);
            }

            @Override
            public boolean shouldTriggerClientSideContainerClosingOnOpen() {
                return false;
            }
        });
    }

    private boolean isSourceSlot(int slot) {
        return !source.isEmpty()
                && slot >= 0
                && slot < slots.size()
                && slots.get(slot).getItem() == source;
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (button != 0 || !stillValid(player) || !(player instanceof ServerPlayer server)) return false;
        BackpackColorTarget target;
        if (portable && BackpackColors.supports(source)) target = new BackpackColorTarget.Held(player, source);
        else if (getContainer() instanceof BackpackBlockEntity entity) target = new BackpackColorTarget.Placed(entity);
        else return false;
        BackpackColorMenu.open(server, target);
        return true;
    }

    @Override
    public void clicked(int slot, int button, ClickType click, Player player) {
        if (isSourceSlot(slot)) return;
        if (click == ClickType.SWAP
                && !source.isEmpty()
                && ((button >= 0 && button < 9 && player.getInventory().getItem(button) == source)
                        || (button == 40 && player.getOffhandItem() == source))) return;
        if (!stillValid(player)) return;
        super.clicked(slot, button, click, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (isSourceSlot(index) || !stillValid(player) || index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        var slot = slots.get(index);
        var stack = slot.getItem();
        if (stack.isEmpty()) return ItemStack.EMPTY;
        var original = stack.copy();
        int playerEnd = capacity + 36;
        if (index >= playerEnd && !canEditAttachments()) return ItemStack.EMPTY;
        if (index < capacity || index >= playerEnd) {
            if (!moveItemStackTo(stack, capacity, playerEnd, true)) return ItemStack.EMPTY;
        } else {
            boolean moved = moveItemStackTo(stack, playerEnd, slots.size(), false);
            if (!stack.isEmpty() && canStore(stack)) moved |= moveItemStackTo(stack, 0, capacity, false);
            if (!moved) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        slot.onTake(player, stack);
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        if (!portable) return super.stillValid(player);
        if (source.isEmpty()) return false;
        if (owner != player || !player.isAlive()) return false;
        if (AccessoryInventory.get(player).getStackInSlot(AccessoryInventory.BACK_SLOT) == source) return true;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i) == source) return true;
        }
        return false;
    }
}
