package dev.poptartking.poptartcore.summoning;

import dev.poptartking.poptartcore.registry.PoptartToolItems;

public enum PixieType {
    ALLAY,
    VEX;

    public PixieCallingBellItem bell() {
        return this == ALLAY ? PoptartToolItems.ALLAY_CALLING_BELL.get() : PoptartToolItems.VEX_CALLING_BELL.get();
    }
}
