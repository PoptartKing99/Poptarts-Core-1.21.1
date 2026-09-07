package dev.poptartking.poptartcore;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class PoptartCoreConfig {

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLE_CASTING_TIMER;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        ENABLE_CASTING_TIMER = builder.comment("Whether crucible casting takes time instead of completing instantly.")
                .define("enableCastingTimer", true);

        SPEC = builder.build();
    }

    private PoptartCoreConfig() {}
}
