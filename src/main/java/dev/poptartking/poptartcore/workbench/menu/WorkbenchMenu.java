package dev.poptartking.poptartcore.workbench.menu;

import dev.poptartking.poptartcore.registry.PoptartCoreMenus;
import dev.poptartking.poptartcore.workbench.WorkbenchBlockEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class WorkbenchMenu extends AbstractContainerMenu {
    public static final int GRID_WIDTH = 3;
    public static final int GRID_HEIGHT = 3;
    public static final int GRID_SIZE = GRID_WIDTH * GRID_HEIGHT;
    private static final int RESULT_SLOT = 0;
    private static final int GRID_START = 1;
    private static final int STORAGE_START = GRID_START + GRID_SIZE;
    private static final int PLAYER_INVENTORY_START = STORAGE_START + WorkbenchBlockEntity.STORAGE_SIZE;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 36;

    private final Container container;
    private final WorkbenchCraftingContainer craftingContainer;
    private final ResultContainer resultContainer = new ResultContainer();
    private final NonNullList<ItemStack> lastGrid = NonNullList.withSize(GRID_SIZE, ItemStack.EMPTY);
    private final Player player;
    private final Level level;

    public WorkbenchMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(WorkbenchBlockEntity.CONTAINER_SIZE));
    }

    public WorkbenchMenu(int containerId, Inventory playerInventory, Container container) {
        super(PoptartCoreMenus.WORKBENCH.get(), containerId);
        checkContainerSize(container, WorkbenchBlockEntity.CONTAINER_SIZE);
        this.container = container;
        player = playerInventory.player;
        level = player.level();
        craftingContainer = new WorkbenchCraftingContainer(this, container);

        addSlot(new ResultSlot(player, craftingContainer, resultContainer, 0, 124, 35));
        for (int row = 0; row < GRID_HEIGHT; row++) {
            for (int column = 0; column < GRID_WIDTH; column++) {
                addSlot(new Slot(craftingContainer, column + row * GRID_WIDTH, 30 + column * 18, 17 + row * 18));
            }
        }
        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 8; column++) {
                addSlot(new Slot(container, GRID_SIZE + column + row * 8, 18 + column * 18, 84 + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 139 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 197));
        }

        if (!level.isClientSide) {
            resultContainer.setItem(RESULT_SLOT, computeResult());
        }
    }

    @Override
    public void slotsChanged(Container changedContainer) {
        if (!level.isClientSide && changedContainer == craftingContainer) {
            updateResult();
        }
    }

    private void updateResult() {
        if (player instanceof ServerPlayer serverPlayer) {
            ItemStack result = computeResult();
            for (int slot = 0; slot < GRID_SIZE; slot++) {
                lastGrid.set(slot, craftingContainer.getItem(slot).copy());
            }
            resultContainer.setItem(RESULT_SLOT, result);
            setRemoteSlot(RESULT_SLOT, result);
            serverPlayer.connection.send(
                    new ClientboundContainerSetSlotPacket(containerId, incrementStateId(), RESULT_SLOT, result));
        }
    }

    @Override
    public void broadcastChanges() {
        if (!level.isClientSide) {
            // Other menus and hoppers edit the backing inventory without notifying this menu.
            for (int slot = 0; slot < GRID_SIZE; slot++) {
                if (!ItemStack.matches(lastGrid.get(slot), craftingContainer.getItem(slot))) {
                    updateResult();
                    break;
                }
            }
        }
        super.broadcastChanges();
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        // Refresh before vanilla captures any result-stack references, including hotbar swaps and drops.
        if (!level.isClientSide) {
            updateResult();
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != resultContainer && super.canTakeItemForPickAll(stack, slot);
    }

    private ItemStack computeResult() {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return ItemStack.EMPTY;
        }

        CraftingInput input = craftingContainer.asCraftInput();
        return level.getServer()
                .getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, input, level)
                .filter(holder -> resultContainer.setRecipeUsed(level, serverPlayer, holder))
                .map(RecipeHolder::value)
                .map(recipe -> recipe.assemble(input, level.registryAccess()))
                .filter(result -> result.isItemEnabled(level.enabledFeatures()))
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index == RESULT_SLOT && !level.isClientSide) {
            updateResult();
        }
        ItemStack original = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return original;
        }

        ItemStack stack = slot.getItem();
        original = stack.copy();
        if (index == RESULT_SLOT) {
            stack.getItem().onCraftedBy(stack, player.level(), player);
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, original);
        } else if (index >= GRID_START && index < PLAYER_INVENTORY_START) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, STORAGE_START, PLAYER_INVENTORY_START, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        if (index == RESULT_SLOT) {
            player.drop(stack, false);
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }
}
