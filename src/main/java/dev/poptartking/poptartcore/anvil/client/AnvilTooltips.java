package dev.poptartking.poptartcore.anvil.client;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.anvil.AnvilMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID, value = Dist.CLIENT)
public final class AnvilTooltips {
    private AnvilTooltips() {}

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent event) {
        var stack = event.getItemStack();
        int count = AnvilMenu.getUpgradeCount(stack);
        if (count > 0)
            event.getToolTip().add(1, Component.literal("+" + "I".repeat(count)).withStyle(ChatFormatting.GRAY));
        for (String modification : AnvilMenu.getModifications(stack))
            event.getToolTip()
                    .add(Component.translatable("modification.poptartcore." + modification)
                            .withStyle(ChatFormatting.GRAY));
        if (AnvilMenu.isWornOut(stack))
            event.getToolTip()
                    .add(Component.translatable("item.poptartcore.worn_out").withStyle(ChatFormatting.RED));
    }
}
