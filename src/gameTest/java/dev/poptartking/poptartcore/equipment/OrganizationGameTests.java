package dev.poptartking.poptartcore.equipment;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.catalog.CreativeCatalogGroup;
import dev.poptartking.poptartcore.catalog.PoptartCatalog;
import dev.poptartking.poptartcore.registry.PoptartCoreTabs;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class OrganizationGameTests {
    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void singleTabCoversCatalogInGroupedOrder(GameTestHelper helper) {
        helper.assertTrue(
                java.util.Arrays.equals(CreativeCatalogGroup.values(), new CreativeCatalogGroup[] {
                    CreativeCatalogGroup.BUILDING, CreativeCatalogGroup.TOOLS,
                    CreativeCatalogGroup.EQUIPMENT, CreativeCatalogGroup.MATERIALS
                }),
                "Required order is Blocks, Tools, Equipment, Materials");
        var tab = PoptartCoreTabs.POPTARTCORE_TAB.get();
        helper.assertTrue(
                PoptartCoreTabs.CREATIVE_MODE_TAB.getEntries().size() == 1, "Only one mod tab should be registered");
        Set<ResourceLocation> expected = new HashSet<>();
        Map<ResourceLocation, CreativeCatalogGroup> groups = new HashMap<>();
        PoptartCatalog.items().stream().filter(def -> def.showInCreativeTab()).forEach(def -> {
            expected.add(PoptartCore.location(def.id()));
            groups.put(PoptartCore.location(def.id()), def.creativeGroup());
        });
        PoptartCatalog.blocks().stream().filter(def -> def.showInCreativeTab()).forEach(def -> {
            expected.add(PoptartCore.location(def.id()));
            groups.put(PoptartCore.location(def.id()), def.creativeGroup());
        });
        Set<ResourceLocation> shown = new HashSet<>();
        var parameters = new CreativeModeTab.ItemDisplayParameters(
                helper.getLevel().enabledFeatures(), true, helper.getLevel().registryAccess());
        tab.buildContents(parameters);
        int previousGroup = -1;
        for (var stack : tab.getDisplayItems()) {
            var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (!id.getNamespace().equals(PoptartCore.MOD_ID)) continue;
            helper.assertTrue(shown.add(id), "Creative categories must not duplicate " + id);
            helper.assertTrue(groups.containsKey(id), "Creative entries must have a catalog definition: " + id);
            int group = groups.get(id).ordinal();
            helper.assertTrue(group >= previousGroup, "Categories must remain together in the single tab");
            previousGroup = group;
        }
        helper.assertTrue(
                shown.equals(expected), "Every visible catalog entry must appear in exactly one creative tab");
        helper.assertTrue(
                dev.poptartking.poptartcore.registry.PoptartRingItems.RUSTED_IRON_RING.creativeGroup()
                        == CreativeCatalogGroup.EQUIPMENT,
                "Rings belong in Equipment");
        helper.assertTrue(
                dev.poptartking.poptartcore.registry.PoptartBuildingBlocks.CLINKER_BRICKS.creativeGroup()
                        == CreativeCatalogGroup.BUILDING,
                "Clinker belongs in Building");
        helper.assertTrue(
                dev.poptartking.poptartcore.registry.PoptartMaterialItems.BRONZE_INGOT.creativeGroup()
                        == CreativeCatalogGroup.MATERIALS,
                "Ingots belong in Materials");
        helper.assertTrue(
                dev.poptartking.poptartcore.registry.PoptartWorkstationBlocks.CRUCIBLE.creativeGroup()
                        == CreativeCatalogGroup.TOOLS,
                "Workstations belong in Tools and Utilities");
        helper.succeed();
    }
}
