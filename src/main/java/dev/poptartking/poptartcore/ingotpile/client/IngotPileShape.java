package dev.poptartking.poptartcore.ingotpile.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import dev.poptartking.poptartcore.PoptartCore;
import java.io.IOException;
import java.io.Reader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

/** Visual geometry only. The pile's collision shape remains defined by its block state. */
public record IngotPileShape(
        Bounds bottom, Bounds top, float height, float columnSpacing, float rowSpacing,
        float layerSpacing, float alternateLayerRotation, UvMap uv) {
    public record Bounds(float minX, float maxX, float minZ, float maxZ) {}
    public record UvRect(float minU, float minV, float maxU, float maxV) {}
    public record UvMap(int width, int height, UvRect top, UvRect bottom, UvRect longSide, UvRect end) {}

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation FILE = PoptartCore.location("models/block/ingot_pile_shape.json");
    private static final IngotPileShape DEFAULT = new IngotPileShape(
            new Bounds(.25F, 3.5078125F, .25F, 7.5078125F),
            new Bounds(.75F, 3.0078125F, .75F, 7.0078125F),
            2, 4, 8, 2, 90,
            new UvMap(32, 32,
                    new UvRect(1, 1, 9, 17), new UvRect(11, 1, 19, 17),
                    new UvRect(1, 20, 17, 24), new UvRect(20, 20, 28, 24)));
    private static volatile IngotPileShape current = DEFAULT;

    public static IngotPileShape current() {
        return current;
    }

    public static void reload(ResourceManager resources) {
        try (Reader reader = resources.getResourceOrThrow(FILE).openAsReader()) {
            current = parse(JsonParser.parseReader(reader).getAsJsonObject());
        } catch (IOException | RuntimeException exception) {
            current = DEFAULT;
            LOGGER.error("Could not load ingot pile shape {}; using built-in dimensions", FILE, exception);
        }
    }

    static IngotPileShape parse(JsonObject json) {
        Bounds bottom = bounds(json.getAsJsonArray("bottom"));
        Bounds top = bounds(json.getAsJsonArray("top"));
        float height = number(json, "height");
        float columnSpacing = number(json, "column_spacing");
        float rowSpacing = number(json, "row_spacing");
        float layerSpacing = number(json, "layer_spacing");
        float rotation = number(json, "alternate_layer_rotation");
        JsonArray textureSize = json.getAsJsonArray("texture_size");
        if (textureSize == null || textureSize.size() != 2)
            throw new IllegalArgumentException("Expected texture_size [width, height]");
        int width = textureSize.get(0).getAsInt();
        int textureHeight = textureSize.get(1).getAsInt();
        if (width <= 0 || textureHeight <= 0) throw new IllegalArgumentException("Invalid texture size");
        JsonObject uv = json.getAsJsonObject("uv");
        UvMap uvMap = new UvMap(width, textureHeight,
                uvRect(uv.getAsJsonArray("top"), width, textureHeight),
                uvRect(uv.getAsJsonArray("bottom"), width, textureHeight),
                uvRect(uv.getAsJsonArray("long_side"), width, textureHeight),
                uvRect(uv.getAsJsonArray("end"), width, textureHeight));
        if (top.minX < bottom.minX || top.maxX > bottom.maxX
                || top.minZ < bottom.minZ || top.maxZ > bottom.maxZ
                || height <= 0 || height > 16 || columnSpacing <= 0 || rowSpacing <= 0
                || layerSpacing <= 0) {
            throw new IllegalArgumentException("Ingot pile shape has invalid bounds or spacing");
        }
        return new IngotPileShape(bottom, top, height, columnSpacing, rowSpacing, layerSpacing, rotation, uvMap);
    }

    private static UvRect uvRect(JsonArray array, int width, int height) {
        if (array == null || array.size() != 4) throw new IllegalArgumentException("Expected four UV values");
        float minU = finite(array.get(0).getAsFloat());
        float minV = finite(array.get(1).getAsFloat());
        float maxU = finite(array.get(2).getAsFloat());
        float maxV = finite(array.get(3).getAsFloat());
        if (minU < 0 || minV < 0 || minU >= maxU || minV >= maxV || maxU > width || maxV > height)
            throw new IllegalArgumentException("UV rectangle is outside the texture");
        return new UvRect(minU, minV, maxU, maxV);
    }

    private static Bounds bounds(JsonArray array) {
        if (array == null || array.size() != 4) throw new IllegalArgumentException("Expected four bounds values");
        float minX = finite(array.get(0).getAsFloat());
        float maxX = finite(array.get(1).getAsFloat());
        float minZ = finite(array.get(2).getAsFloat());
        float maxZ = finite(array.get(3).getAsFloat());
        if (minX < 0 || maxX > 16 || minZ < 0 || maxZ > 16 || minX >= maxX || minZ >= maxZ)
            throw new IllegalArgumentException("Ingot pile bounds must fit within 16 pixels");
        return new Bounds(minX, maxX, minZ, maxZ);
    }

    private static float number(JsonObject json, String key) {
        return finite(json.get(key).getAsFloat());
    }

    private static float finite(float value) {
        if (!Float.isFinite(value)) throw new IllegalArgumentException("Ingot pile shape values must be finite");
        return value;
    }
}
