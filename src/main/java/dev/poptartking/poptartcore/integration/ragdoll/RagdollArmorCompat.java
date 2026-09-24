package dev.poptartking.poptartcore.integration.ragdoll;

import com.farcr.ragdoll.api.overrides.ModelElementOverrides;
import net.minecraft.resources.ResourceLocation;

/** Maps our armor model parts to the player limbs used by Ragdoll's renderer. */
public final class RagdollArmorCompat {
    private static final ResourceLocation PLAYER = ResourceLocation.withDefaultNamespace("player");
    private static final ThreadLocal<Boolean> RENDERING_OUR_ARMOR = ThreadLocal.withInitial(() -> false);

    private RagdollArmorCompat() {}

    public static boolean enterModelRender() {
        boolean previous = RENDERING_OUR_ARMOR.get();
        RENDERING_OUR_ARMOR.set(true);
        return previous;
    }

    public static void exitModelRender(boolean previous) {
        if (previous) {
            RENDERING_OUR_ARMOR.set(true);
        } else {
            RENDERING_OUR_ARMOR.remove();
        }
    }

    public static boolean isRenderingOurArmor() {
        return RENDERING_OUR_ARMOR.get();
    }

    public static void register() {
        map("metal_helmet", "head");
        map("metal_body", "body");
        map("metal_left_arm", "left_arm");
        map("metal_right_arm", "right_arm");
        map("metal_waist", "body");
        map("metal_left_leg", "left_leg");
        map("metal_right_leg", "right_leg");
        map("metal_left_boot", "left_leg");
        map("metal_right_boot", "right_leg");

        map("helm", "head");
        map("hood", "head");
        map("flap", "head");
        map("tunic", "body");
        map("jacket", "body");
        map("waist", "body");
        map("skirt", "body");
        map("belt", "body");
        map("strap", "body");
        map("right_sleeve", "right_arm");
        map("left_sleeve", "left_arm");
        map("right_wrap", "right_leg");
        map("left_wrap", "left_leg");
        map("right_boot", "right_leg");
        map("left_boot", "left_leg");

        map("helmet", "head");
        map("clip_right", "head");
        map("clip_left", "head");
    }

    private static void map(String armorPart, String playerPart) {
        ModelElementOverrides.registerPart(PLAYER, armorPart, playerPart);
    }
}
