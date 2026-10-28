package dev.poptartking.poptartcore.backpack;

import dev.poptartking.poptartcore.registry.PoptartCoreMenus;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;

public final class BackpackColorMenu extends AbstractContainerMenu {
    public static final int SELECT_BODY = 0, SELECT_FLAPS = 1, APPLY = 2, BACK = 3;
    private final SimpleContainer dyeInput = new SimpleContainer(1);
    private final SimpleContainer visual = new SimpleContainer(1);
    private final SimpleContainerData selection = new SimpleContainerData(1);
    private final BackpackColorTarget target;

    public BackpackColorMenu(int id, Inventory inventory) {
        this(id, inventory, null);
    }

    public BackpackColorMenu(int id, Inventory inventory, BackpackColorTarget target) {
        super(PoptartCoreMenus.BACKPACK_COLORS.get(), id);
        this.target = target;
        addSlot(new Slot(dyeInput, 0, 105, 87) {
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof DyeItem;
            }
        });
        addSlot(new Slot(visual, 0, 62, 115) {
            @Override
            public boolean isActive() {
                return false;
            }

            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            public boolean mayPickup(Player player) {
                return false;
            }
        });
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++) {
                addPlayerSlot(inventory, col + row * 9 + 9, 10 + col * 18, 126 + row * 18);
            }
        for (int col = 0; col < 9; col++) addPlayerSlot(inventory, col, 10 + col * 18, 184);
        addDataSlots(selection);
        refresh();
    }

    private void addPlayerSlot(Inventory inventory, int index, int x, int y) {
        addSlot(new Slot(inventory, index, x, y) {
            public boolean mayPickup(Player player) {
                return !locked(getItem());
            }

            public boolean mayPlace(ItemStack stack) {
                return !locked(getItem()) && !locked(stack);
            }
        });
    }

    public static void open(ServerPlayer player, BackpackColorTarget target) {
        if (!target.valid(player)) return;
        BackpackMenu.switchMenu(
                player,
                new SimpleMenuProvider(
                        (id, inventory, opener) -> new BackpackColorMenu(id, inventory, target),
                        Component.translatable(
                                target.snapshot()
                                                .is(dev.poptartking.poptartcore.registry.PoptartEquipmentBlocks.RUCKSACK
                                                        .get()
                                                        .asItem())
                                        ? "menu.poptartcore.rucksack_colors"
                                        : "menu.poptartcore.backpack_colors")));
    }

    public BackpackColors.Part selectedPart() {
        return selection.get(0) == SELECT_FLAPS ? BackpackColors.Part.FLAPS : BackpackColors.Part.BODY;
    }

    public ItemStack current() {
        return visual.getItem(0);
    }

    public ItemStack preview() {
        return dyeInput.getItem(0).getItem() instanceof DyeItem dye
                        && !current().isEmpty()
                ? BackpackColors.dyed(current(), selectedPart(), dye)
                : current();
    }

    private boolean locked(ItemStack stack) {
        return target != null && target.locks(stack);
    }

    private void refresh() {
        if (target == null) return;
        var snapshot = target.snapshot();
        if (!ItemStack.matches(snapshot, current())) visual.setItem(0, snapshot);
    }

    @Override
    public void broadcastChanges() {
        refresh();
        super.broadcastChanges();
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (target == null || !stillValid(player)) return false;
        if (button == SELECT_BODY || button == SELECT_FLAPS) selection.set(0, button);
        else if (button == APPLY && dyeInput.getItem(0).getItem() instanceof DyeItem dye) {
            var result = BackpackColors.dyed(target.snapshot(), selectedPart(), dye);
            target.setColor(selectedPart(), BackpackColors.color(result, selectedPart()));
            dyeInput.removeItem(0, 1);
        } else if (button == BACK && player instanceof ServerPlayer server) {
            target.reopen(server);
            return true;
        } else return false;
        broadcastChanges();
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return target == null || target.valid(player);
    }

    @Override
    public void clicked(int index, int button, ClickType click, Player player) {
        if (!stillValid(player) || index == 1) return;
        if (index >= 0 && index < slots.size() && locked(slots.get(index).getItem())) return;
        if (click == ClickType.SWAP
                && ((button >= 0 && button < 9 && locked(player.getInventory().getItem(button)))
                        || (button == 40 && locked(player.getOffhandItem())))) return;
        super.clicked(index, button, click, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (!stillValid(player) || index < 0 || index >= slots.size() || index == 1) return ItemStack.EMPTY;
        var slot = slots.get(index);
        var stack = slot.getItem();
        if (stack.isEmpty() || locked(stack)) return ItemStack.EMPTY;
        var copy = stack.copy();
        if (index == 0) {
            if (!moveItemStackTo(stack, 2, slots.size(), true)) return ItemStack.EMPTY;
        } else if (!(stack.getItem() instanceof DyeItem) || !moveItemStackTo(stack, 0, 1, false))
            return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) clearContainer(player, dyeInput);
    }
}
