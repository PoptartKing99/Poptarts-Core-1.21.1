package dev.poptartking.poptartcore.decoration;

import net.minecraft.util.Mth;

/** Continuous geometry for fluid emerging, gathering, stretching, and detaching. Units are blocks. */
public record TapperDripShape(float width, float height, float topOffset) {
    // Includes the initial portion inside the tip; the exposed forming phase lasts about 6 seconds.
    public static final int FORM_TICKS = 140;
    public static final int MAX_AGE = FORM_TICKS + 80;
    private static final float[][] KEYS = {
        // Progress, width, height, top relative to the nozzle underside.
        {0F, .07F, .014F, .026F},
        {.3F, .10F, .018F, .004F},
        {.65F, .10F, .030F, 0F},
        {.85F, .075F, .058F, 0F},
        {1F, .055F, .080F, -.004F}
    };

    public static TapperDripShape forming(float time) {
        float progress = Mth.clamp(time / FORM_TICKS, 0F, 1F);
        for (int i = 1; i < KEYS.length; i++) {
            float[] a = KEYS[i - 1], b = KEYS[i];
            if (progress <= b[0]) {
                float t = (progress - a[0]) / (b[0] - a[0]);
                return new TapperDripShape(Mth.lerp(t, a[1], b[1]), Mth.lerp(t, a[2], b[2]), Mth.lerp(t, a[3], b[3]));
            }
        }
        throw new IllegalStateException("Unreachable drip progress");
    }

    public static TapperDripShape falling(float time) {
        var release = forming(FORM_TICKS);
        float t = Mth.clamp(time / 5F, 0F, 1F);
        return new TapperDripShape(
                Mth.lerp(t, release.width, .0625F), Mth.lerp(t, release.height, .0625F), release.topOffset);
    }
}
