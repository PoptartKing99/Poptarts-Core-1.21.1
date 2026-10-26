package dev.poptartking.poptartcore.mixin.navigation;

import net.minecraft.client.gui.components.PlayerTabOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerTabOverlay.class)
public interface NavigationTabOverlayAccessor {
    @Accessor("visible")
    boolean poptartcore$isVisible();
}
