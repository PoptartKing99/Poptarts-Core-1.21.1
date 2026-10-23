package dev.poptartking.poptartcore.equipment;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCapeItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** Fixed dye choices, not leather-style mixed colors. Textures supply the muted shades and untouched trim. */
@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public final class CapeColors {
    private CapeColors() {}

    public static boolean isCape(ItemStack stack) {
        return stack.is(PoptartCapeItems.AGILITY_CAPE.get());
    }

    public static DyeColor color(ItemStack stack) {
        var dyed = stack.get(DataComponents.DYED_COLOR);
        if (dyed == null) return DyeColor.WHITE;
        for (var color : DyeColor.values()) {
            if (color.getTextureDiffuseColor() == dyed.rgb()) return color;
        }
        // Only our fixed palette is produced by dyeing; unknown command-supplied colors use the default.
        return DyeColor.WHITE;
    }

    public static void setColor(ItemStack stack, DyeColor color) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color.getTextureDiffuseColor(), false));
    }

    public static float modelProperty(ItemStack stack) {
        return (color(stack).getId() + 1) / 16F;
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent event) {
        if (!isCape(event.getItemStack())) return;
        event.getToolTip()
                .add(Component.translatable(
                                "color.minecraft." + color(event.getItemStack()).getName())
                        .withStyle(ChatFormatting.GRAY));
        event.getToolTip()
                .add(Component.translatable("item.poptartcore.agility_cape.desc")
                        .withStyle(ChatFormatting.GRAY));
    }
}
