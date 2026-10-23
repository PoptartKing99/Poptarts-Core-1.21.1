package dev.poptartking.poptartcore.registry;

import java.util.function.UnaryOperator;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Rarity;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

public final class PoptartRarities {
    public static final EnumProxy<Rarity> MALEVOLENT =
            new EnumProxy<>(Rarity.class, -1, "poptartcore:malevolent", (UnaryOperator<Style>)
                    style -> style.withColor(ChatFormatting.RED));

    private PoptartRarities() {}
}
