package dev.poptartking.poptartcore.catalog;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

final class CatalogVerificationData {
    private CatalogVerificationData() {}

    static JsonObject originalContent() throws IOException {
        return fixture("original-content.json");
    }

    static JsonObject itemBehavior() throws IOException {
        return fixture("item-behavior.json");
    }

    private static JsonObject fixture(String name) throws IOException {
        try (var stream = Objects.requireNonNull(
                        CatalogVerificationData.class.getResourceAsStream("/data/poptartcore/catalog/" + name),
                        "Catalog test fixture is missing: " + name);
                var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    static void writeManifest() throws IOException {
        var manifest = new JsonObject();
        manifest.addProperty("schemaVersion", 8);
        var items = new JsonArray();
        for (var definition : PoptartCatalog.items()) {
            var entry = base(
                    definition.id(),
                    definition.displayName(),
                    definition.get().getDescriptionId(),
                    definition.showInCreativeTab(),
                    definition.model().name(),
                    definition.fuelBurnTime());
            entry.add("itemTags", tags(definition.tags()));
            items.add(entry);
        }
        var blocks = new JsonArray();
        for (var definition : PoptartCatalog.blocks()) {
            var entry = base(
                    definition.id(),
                    definition.displayName(),
                    definition.get().getDescriptionId(),
                    definition.showInCreativeTab(),
                    definition.model().name(),
                    definition.fuelBurnTime());
            entry.addProperty("loot", definition.lootMode().name());
            entry.addProperty(
                    "lootTable", definition.get().getLootTable().location().toString());
            entry.addProperty("modelPath", definition.modelPath());
            entry.addProperty(
                    "texture",
                    definition.texture() == null
                            ? "poptartcore:block/" + definition.id()
                            : definition.texture().toString());
            entry.addProperty("variants", definition.variants());
            entry.add("tags", tags(definition.tags()));
            entry.add("itemTags", tags(definition.itemTags()));
            if (definition.oreDrop() != null)
                entry.addProperty(
                        "oreDrop",
                        BuiltInRegistries.ITEM
                                .getKey(definition.oreDrop().get().asItem())
                                .toString());
            if (definition.storageRecipes() != null) {
                var recipes = definition.storageRecipes();
                var storage = new JsonObject();
                storage.addProperty(
                        "ingredient",
                        BuiltInRegistries.ITEM
                                .getKey(recipes.ingredient().get().asItem())
                                .toString());
                storage.addProperty("packing", recipes.packingRecipeId());
                storage.addProperty("unpacking", recipes.unpackingRecipeId());
                entry.add("storage", storage);
            }
            blocks.add(entry);
        }
        var original = originalContent();
        var translations = original.getAsJsonObject("translations").deepCopy();
        for (String kind : new String[] {"items", "blocks"}) {
            String prefix = kind.equals("items") ? "item.poptartcore." : "block.poptartcore.";
            original.getAsJsonObject(kind)
                    .entrySet()
                    .forEach(entry -> translations.addProperty(
                            prefix + entry.getKey(),
                            entry.getValue().getAsJsonObject().get("name").getAsString()));
        }
        manifest.add("items", items);
        manifest.add("blocks", blocks);
        manifest.add("requiredTranslations", translations);
        var behavior = itemBehavior();
        manifest.add("craftingRecipes", behavior.getAsJsonArray("recipes"));
        manifest.add("itemModelOverrides", behavior.getAsJsonArray("modelOverrides"));
        manifest.add("requiredItemTags", original.getAsJsonObject("itemTags"));
        manifest.add("requiredBlockTags", original.getAsJsonObject("blockTags"));
        manifest.add("requiredMixins", original.getAsJsonArray("requiredMixins"));
        manifest.add("forbiddenResourceNamespaces", original.getAsJsonArray("forbiddenResourceNamespaces"));
        manifest.add("requiredResources", original.getAsJsonArray("requiredResources"));
        manifest.add("wayfarerReferenceTextures", original.getAsJsonObject("wayfarerReferenceTextures"));
        manifest.add("featureAssets", fixture("feature-assets.json"));
        var equipment = fixture("equipment-assets.json");
        manifest.add("equipmentAssets", equipment);
        var assetHashes = new JsonObject();
        for (var item : equipment.getAsJsonObject("items").entrySet()) {
            var contract = item.getValue().getAsJsonObject();
            if (!contract.has("matchPackagedBytes")
                    || !contract.get("matchPackagedBytes").getAsBoolean()) continue;
            for (String path : new String[] {
                "assets/poptartcore/models/item/" + item.getKey() + ".json",
                "assets/poptartcore/textures/item/" + item.getKey() + ".png"
            }) {
                try (var stream =
                        Objects.requireNonNull(CatalogVerificationData.class.getResourceAsStream("/" + path), path)) {
                    assetHashes.addProperty(
                            path,
                            HexFormat.of()
                                    .formatHex(
                                            MessageDigest.getInstance("SHA-256").digest(stream.readAllBytes())));
                } catch (NoSuchAlgorithmException impossible) {
                    throw new AssertionError(impossible);
                }
            }
        }
        manifest.add("requiredAssetHashes", assetHashes);
        var ringTagMembers = new JsonArray();
        BuiltInRegistries.ITEM
                .getTagOrEmpty(TagKey.create(Registries.ITEM, ResourceLocation.parse("poptartcore:equipment/rings")))
                .forEach(item -> ringTagMembers.add(
                        BuiltInRegistries.ITEM.getKey(item.value()).toString()));
        manifest.add("loadedRingTagMembers", ringTagMembers);
        Files.writeString(Path.of("catalog-manifest.json"), manifest.toString());
    }

    private static JsonObject base(
            String id, String name, String descriptionId, boolean visible, String model, int fuel) {
        var entry = new JsonObject();
        entry.addProperty("id", id);
        entry.addProperty("name", name);
        entry.addProperty("descriptionId", descriptionId);
        entry.addProperty("visible", visible);
        entry.addProperty("model", model);
        entry.addProperty("fuel", fuel);
        return entry;
    }

    private static JsonArray tags(Set<? extends TagKey<?>> tags) {
        var result = new JsonArray();
        tags.stream().map(tag -> tag.location().toString()).sorted().forEach(result::add);
        return result;
    }
}
