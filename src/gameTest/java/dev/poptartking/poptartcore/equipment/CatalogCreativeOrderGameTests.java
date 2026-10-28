package dev.poptartking.poptartcore.equipment;

import com.google.gson.JsonParser;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.catalog.CreativeCatalogGroup;
import dev.poptartking.poptartcore.catalog.PoptartCatalog;
import dev.poptartking.poptartcore.registry.PoptartCoreTabs;
import dev.poptartking.poptartcore.registry.PoptartRingItems;
import dev.poptartking.poptartcore.registry.PoptartWorkstationBlocks;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CatalogCreativeOrderGameTests {
    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void creativeTabPreservesExistingItemSequence(GameTestHelper helper) throws Exception {
        var expected = new ArrayList<String>();
        var groups = new HashMap<String, CreativeCatalogGroup>();
        PoptartCatalog.items().forEach(def -> groups.put(def.id(), def.creativeGroup()));
        PoptartCatalog.blocks().forEach(def -> groups.put(def.id(), def.creativeGroup()));
        try (var stream = CatalogCreativeOrderGameTests.class.getResourceAsStream(
                "/data/poptartcore/catalog/creative-order-before-cleanup.json")) {
            helper.assertTrue(stream != null, "The captured pre-cleanup sequence must be present");
            try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                for (var element : JsonParser.parseReader(reader).getAsJsonArray()) {
                    var entry = element.getAsJsonObject();
                    var id = entry.get("id").getAsString();
                    expected.add(id);
                    helper.assertTrue(
                            groups.get(id)
                                    == CreativeCatalogGroup.valueOf(
                                            entry.get("group").getAsString()),
                            "Category changed during cleanup: " + id);
                }
            }
        }
        var tab = PoptartCoreTabs.POPTARTCORE_TAB.get();
        tab.buildContents(new CreativeModeTab.ItemDisplayParameters(
                helper.getLevel().enabledFeatures(), true, helper.getLevel().registryAccess()));
        var actual = new ArrayList<String>();
        var existingIds = new HashSet<>(expected);
        for (var stack : tab.getDisplayItems()) {
            var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (!id.getNamespace().equals(PoptartCore.MOD_ID)) continue;
            if (existingIds.contains(id.getPath())) actual.add(id.getPath());
        }
        helper.assertTrue(actual.equals(expected), "All pre-existing entries must keep their exact sequence");
        helper.assertTrue(
                tab.getDisplayItems().stream()
                        .noneMatch(stack ->
                                BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(PoptartCore.location("lifebud"))),
                "Lifebud remains hidden");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void creativeMetadataCannotChangeAfterRegistration(GameTestHelper helper) {
        var ring = PoptartRingItems.RUSTED_IRON_RING;
        var block = PoptartWorkstationBlocks.CRUCIBLE;
        int ringOrder = ring.creativeOrder();
        int blockOrder = block.creativeOrder();
        boolean itemRejected = false;
        boolean blockRejected = false;
        try {
            ring.creative(CreativeCatalogGroup.MATERIALS, 0);
        } catch (IllegalStateException expected) {
            itemRejected = true;
        }
        try {
            block.creative(CreativeCatalogGroup.BUILDING, 0);
        } catch (IllegalStateException expected) {
            blockRejected = true;
        }
        helper.assertTrue(
                itemRejected
                        && blockRejected
                        && ring.creativeGroup() == CreativeCatalogGroup.EQUIPMENT
                        && block.creativeGroup() == CreativeCatalogGroup.TOOLS
                        && ring.creativeOrder() == ringOrder
                        && block.creativeOrder() == blockOrder,
                "Late edits must fail without partially changing registered creative metadata");
        helper.succeed();
    }
}
