package dev.poptartking.poptartcore.equipment.client;

import dev.poptartking.poptartcore.equipment.AccessorySlotView;
import dev.poptartking.poptartcore.mixin.equipment.CreativeSlotWrapperAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public final class AccessoryPanel {
    private AccessoryPanel() {}

    public static AccessorySlotView view(Slot slot) {
        if (slot instanceof AccessorySlotView view) return view;
        if (slot instanceof CreativeSlotWrapperAccessor wrapper
                && wrapper.poptartcore$target() instanceof AccessorySlotView view) return view;
        return null;
    }

    public static boolean contains(AbstractContainerMenu menu, double x, double y) {
        for (Slot slot : menu.slots) {
            if (slot.isActive()
                    && view(slot) != null
                    && x >= slot.x - 3
                    && x < slot.x + 19
                    && y >= slot.y - 3
                    && y < slot.y + 19) return true;
        }
        return false;
    }

    public static void render(GuiGraphics graphics, AbstractContainerMenu menu, int left, int top) {
        for (Slot slot : menu.slots) {
            if (!slot.isActive()) continue;
            var view = view(slot);
            var texture = view != null ? view.accessoryType().emptySlotTexture() : corpseArmorTexture(slot);
            if (texture == null) continue;
            int x = left + slot.x, y = top + slot.y;
            if (!(menu instanceof net.minecraft.world.inventory.InventoryMenu)
                    && !(menu instanceof com.farcr.ragdoll.screen.CorpseMenu)) background(graphics, x, y);
            if (!slot.hasItem()) {
                com.mojang.blaze3d.systems.RenderSystem.enableBlend();
                graphics.blit(texture, x, y, 0, 0, 16, 16, 16, 16);
                com.mojang.blaze3d.systems.RenderSystem.disableBlend();
            }
        }
    }

    private static net.minecraft.resources.ResourceLocation corpseArmorTexture(Slot slot) {
        if (!(slot instanceof com.farcr.ragdoll.screen.CorpseSlot)) return null;
        String name =
                switch (slot.getSlotIndex()) {
                    case 36 -> "boots";
                    case 37 -> "leggings";
                    case 38 -> "chestplate";
                    case 39 -> "helmet";
                    case 40 -> "shield";
                    default -> null;
                };
        return name == null
                ? null
                : net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                        dev.poptartking.poptartcore.PoptartCore.MOD_ID,
                        "textures/gui/corpse/empty_armor_slot_" + name + ".png");
    }

    public static void background(GuiGraphics graphics, int x, int y) {
        graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xff373737);
        graphics.fill(x, y, x + 17, y + 17, 0xffffffff);
        graphics.fill(x, y, x + 16, y + 16, 0xff8b8b8b);
    }

    public static void tooltip(
            GuiGraphics graphics, AbstractContainerMenu menu, int left, int top, int mouseX, int mouseY) {
        for (Slot slot : menu.slots) {
            String translationKey = emptySlotTranslationKey(slot);
            if (slot.isActive()
                    && translationKey != null
                    && !slot.hasItem()
                    && mouseX >= left + slot.x
                    && mouseX < left + slot.x + 16
                    && mouseY >= top + slot.y
                    && mouseY < top + slot.y + 16) {
                graphics.renderTooltip(
                        Minecraft.getInstance().font, Component.translatable(translationKey), mouseX, mouseY);
                return;
            }
        }
    }

    private static String emptySlotTranslationKey(Slot slot) {
        var accessory = view(slot);
        if (accessory != null) return accessory.accessoryType().translationKey();
        Slot target = slot instanceof CreativeSlotWrapperAccessor wrapper ? wrapper.poptartcore$target() : slot;
        if (!(target.container instanceof net.minecraft.world.entity.player.Inventory)) return null;
        return switch (target.getSlotIndex()) {
            case 39 -> "slot.poptartcore.helmet";
            case 38 -> "slot.poptartcore.chestplate";
            case 37 -> "slot.poptartcore.leggings";
            case 36 -> "slot.poptartcore.boots";
            case 40 -> "slot.poptartcore.offhand";
            default -> null;
        };
    }
}
