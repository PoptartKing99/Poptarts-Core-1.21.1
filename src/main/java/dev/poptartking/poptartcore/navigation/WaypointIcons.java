package dev.poptartking.poptartcore.navigation;

import dev.poptartking.poptartcore.PoptartCore;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import net.minecraft.resources.ResourceLocation;

public final class WaypointIcons {
    public static final String DEFAULT = "default";
    private static final Pattern NAME = Pattern.compile("[a-z0-9_]{1,64}");
    private static final Set<String> REMOVED = Set.of("alchemist", "friend");
    private static final Map<String, ResourceLocation[]> SPRITES = new HashMap<>();

    private WaypointIcons() {}

    public static boolean isValid(String icon) {
        return NAME.matcher(icon).matches() && !REMOVED.contains(icon);
    }

    public static String sanitize(String icon) {
        return isValid(icon) ? icon : DEFAULT;
    }

    public static ResourceLocation getSprite(String icon, boolean selected) {
        icon = sanitize(icon);
        ResourceLocation[] sprites = SPRITES.get(icon);
        if (sprites == null) {
            sprites = new ResourceLocation[] {
                PoptartCore.location("waypoints/" + icon), PoptartCore.location("waypoints/" + icon + "_selected")
            };
            SPRITES.put(icon, sprites);
        }

        return sprites[selected ? 1 : 0];
    }
}
