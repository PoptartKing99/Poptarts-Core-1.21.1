package dev.poptartking.poptartcore.catalog.datagen;

import com.google.gson.JsonParser;
import dev.poptartking.poptartcore.catalog.CatalogBlockDefinition;
import dev.poptartking.poptartcore.catalog.CatalogItemDefinition;
import dev.poptartking.poptartcore.catalog.PoptartCatalog;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public final class CatalogLanguageProvider extends LanguageProvider {
    public CatalogLanguageProvider(PackOutput output) {
        super(output, PoptartCatalog.GENERATED_RESOURCE_NAMESPACE, "en_us");
    }

    @Override
    protected void addTranslations() {
        Set<String> handwritten = handwrittenKeys();
        for (CatalogItemDefinition<?> definition : PoptartCatalog.items()) {
            if (!handwritten.contains(definition.get().getDescriptionId()))
                add(definition.item().get(), definition.displayName());
        }
        for (CatalogBlockDefinition<?> definition : PoptartCatalog.blocks()) {
            if (!handwritten.contains(definition.get().getDescriptionId()))
                add(definition.get(), definition.displayName());
        }
    }

    private static Set<String> handwrittenKeys() {
        try (var stream = CatalogLanguageProvider.class.getResourceAsStream("/assets/poptartcore/lang/en_us.json")) {
            if (stream == null) throw new IllegalStateException("Handwritten English language resource is missing");
            try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return Set.copyOf(
                        JsonParser.parseReader(reader).getAsJsonObject().keySet());
            }
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not read handwritten English language resource", exception);
        }
    }
}
