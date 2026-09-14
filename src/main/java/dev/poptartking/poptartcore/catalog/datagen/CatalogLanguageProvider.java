package dev.poptartking.poptartcore.catalog.datagen;

import dev.poptartking.poptartcore.catalog.CatalogBlockDefinition;
import dev.poptartking.poptartcore.catalog.CatalogItemDefinition;
import dev.poptartking.poptartcore.catalog.PoptartCatalog;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public final class CatalogLanguageProvider extends LanguageProvider {
    public CatalogLanguageProvider(PackOutput output) {
        super(output, PoptartCatalog.GENERATED_RESOURCE_NAMESPACE, "en_us");
    }

    @Override
    protected void addTranslations() {
        for (CatalogItemDefinition<?> definition : PoptartCatalog.items()) {
            add(definition.item().get(), definition.displayName());
        }
        for (CatalogBlockDefinition<?> definition : PoptartCatalog.blocks()) {
            add(definition.get(), definition.displayName());
        }
    }
}
