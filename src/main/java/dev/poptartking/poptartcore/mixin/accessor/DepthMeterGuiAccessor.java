package dev.poptartking.poptartcore.mixin.accessor;

import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Gui.class)
public interface DepthMeterGuiAccessor {
    @Accessor("overlayMessageTime")
    int poptartcore$overlayMessageTime();
}
