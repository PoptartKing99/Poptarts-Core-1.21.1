package dev.poptartking.poptartcore.scribing;

import dev.poptartking.poptartcore.registry.PoptartCoreMenus;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ScribingTableMenu extends AbstractContainerMenu {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_TOOLS = 1;
    public static final int SLOT_RESULT = 2;
    private static final int INVENTORY_START = 3;
    private static final int INVENTORY_END = 39;
    public static final int LORE_LINES = 5;
    private final Container tools;
    private final Container input = new SimpleContainer(1);
    private final ResultContainer resultSlots = new ResultContainer();
    private String scribedName = "";
    private String scribedLore = "";
    private boolean editName;
    private boolean editLore;
    private ItemStack editingInput = ItemStack.EMPTY;

    public ScribingTableMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, playerInventory, resolve(playerInventory, buffer.readBlockPos()));
    }

    public ScribingTableMenu(int containerId, Inventory playerInventory, Container tools) {
        super(PoptartCoreMenus.SCRIBING_TABLE.get(), containerId);
        checkContainerSize(tools, 1);
        this.tools = tools;
        this.addSlot(new Slot(this.input, 0, 29, 18) {
            @Override
            public void setChanged() {
                super.setChanged();
                ScribingTableMenu.this.createResult();
            }
        });
        this.addSlot(new Slot(tools, 0, 65, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof ScribingToolsItem;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public void setChanged() {
                super.setChanged();
                ScribingTableMenu.this.createResult();
            }
        });
        this.addSlot(new Slot(this.resultSlots, 0, 124, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player picker) {
                return ScribingTableMenu.this.hasWorkingTools();
            }

            @Override
            public void onTake(Player taker, ItemStack stack) {
                ScribingTableMenu.this.onResultTaken();
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 139 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(playerInventory, column, 8 + column * 18, 197));
        }

        this.createResult();
    }

    private static Container resolve(Inventory playerInventory, BlockPos pos) {
        BlockEntity var3 = playerInventory.player.level().getBlockEntity(pos);
        return (Container)
                (var3 instanceof ScribingTableBlockEntity ? (ScribingTableBlockEntity) var3 : new SimpleContainer(1));
    }

    public String getScribedName() {
        return this.scribedName;
    }

    public String getScribedLore() {
        return this.scribedLore;
    }

    public void setScribedText(ItemStack expectedInput, String name, String lore, boolean editName, boolean editLore) {
        this.resetTextIfInputChanged();
        if (expectedInput.isEmpty() || !ItemStack.matches(expectedInput, this.input.getItem(0))) {
            return;
        }
        this.scribedName = name;
        this.scribedLore = lore;
        this.editName = editName;
        this.editLore = editLore;
        this.createResult();
    }

    public boolean hasWorkingTools() {
        ItemStack stack = this.tools.getItem(0);
        return stack.getItem() instanceof ScribingToolsItem && stack.getDamageValue() < stack.getMaxDamage();
    }

    public boolean hasResult() {
        return !this.resultSlots.getItem(0).isEmpty();
    }

    private static List<Component> loreLines(String lore) {
        List<Component> lines = new ArrayList<>();
        if (lore.isBlank()) {
            return lines;
        } else {
            for (String line : lore.split("\n", 5)) {
                lines.add(Component.literal(line));
            }

            return lines;
        }
    }

    public static String defaultNameOf(ItemStack stack) {
        ItemStack plain = stack.copy();
        plain.remove(DataComponents.CUSTOM_NAME);
        return plain.getHoverName().getString();
    }

    private ItemStack decorate(ItemStack source) {
        ItemStack result = source.copy();
        result.setCount(1);
        if (this.editName && !this.scribedName.equals(source.getHoverName().getString())) {
            if (!this.scribedName.isBlank() && !this.scribedName.equals(defaultNameOf(source))) {
                result.set(DataComponents.CUSTOM_NAME, Component.literal(this.scribedName));
            } else {
                result.remove(DataComponents.CUSTOM_NAME);
            }
        }

        if (this.editLore && !this.scribedLore.equals(plainLoreOf(source))) {
            List<Component> lines = loreLines(this.scribedLore);
            if (lines.isEmpty()) {
                result.remove(DataComponents.LORE);
            } else {
                result.set(DataComponents.LORE, new ItemLore(lines));
            }
        }

        return result;
    }

    private static String plainLoreOf(ItemStack stack) {
        ItemLore lore = stack.get(DataComponents.LORE);
        if (lore == null) {
            return "";
        }
        return String.join("\n", lore.lines().stream().map(Component::getString).toList());
    }

    public void createResult() {
        this.resetTextIfInputChanged();
        ItemStack source = this.input.getItem(0);
        if (source.isEmpty()) {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
            this.broadcastChanges();
        } else {
            ItemStack result = this.decorate(source);
            boolean changed =
                    !Objects.equals(result.get(DataComponents.CUSTOM_NAME), source.get(DataComponents.CUSTOM_NAME))
                            || !Objects.equals(result.get(DataComponents.LORE), source.get(DataComponents.LORE));
            this.resultSlots.setItem(0, changed ? result : ItemStack.EMPTY);
            this.broadcastChanges();
        }
    }

    private void resetTextIfInputChanged() {
        ItemStack current = this.input.getItem(0);
        if (!ItemStack.isSameItemSameComponents(current, this.editingInput)) {
            this.editingInput = current.copy();
            this.scribedName = "";
            this.scribedLore = "";
            this.editName = false;
            this.editLore = false;
        }
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        this.createResult();
    }

    private void onResultTaken() {
        ItemStack source = this.input.getItem(0);
        if (!source.isEmpty()) {
            source.shrink(1);
            this.input.setItem(0, source.isEmpty() ? ItemStack.EMPTY : source);
        }

        ItemStack stack = this.tools.getItem(0);
        if (stack.getItem() instanceof ScribingToolsItem) {
            this.tools.setItem(
                    0,
                    stack.getItem().hasCraftingRemainingItem(stack)
                            ? stack.getItem().getCraftingRemainingItem(stack)
                            : ItemStack.EMPTY);
        }

        this.tools.setChanged();
        if (this.tools instanceof ScribingTableBlockEntity table
                && table.getLevel() != null
                && !table.getLevel().isClientSide) {
            table.getLevel()
                    .playSound(
                            null,
                            table.getBlockPos(),
                            SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT,
                            SoundSource.BLOCKS,
                            1.0F,
                            1.0F);
        }

        this.createResult();
    }

    @Override
    public ItemStack quickMoveStack(Player quickPlayer, int index) {
        ItemStack remainder = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            remainder = stack.copy();
            if (index == 2) {
                if (!this.hasWorkingTools()) {
                    return ItemStack.EMPTY;
                }

                if (!this.moveItemStackTo(stack, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(stack, remainder);
            } else if (index != 0 && index != 1) {
                if (stack.getItem() instanceof ScribingToolsItem) {
                    if (!this.moveItemStackTo(stack, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(stack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 3, 39, true)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stack.getCount() == remainder.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(quickPlayer, stack);
        }

        return remainder;
    }

    @Override
    public void removed(Player removedPlayer) {
        super.removed(removedPlayer);
        this.resultSlots.setItem(0, ItemStack.EMPTY);
        this.clearContainer(removedPlayer, this.input);
    }

    @Override
    public boolean stillValid(Player validPlayer) {
        return this.tools.stillValid(validPlayer);
    }
}
