package dev.poptartking.poptartcore.catalog;

import dev.poptartking.poptartcore.PoptartCore;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BlockRegistryCompatibilityGameTests {
    private BlockRegistryCompatibilityGameTests() {}

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void savedInternalBlockIdsRemainAvailable(GameTestHelper helper) {
        // These saved-world IDs are outside the catalog's block-and-item registrations.
        for (String id : Set.of(
                "ingot_pile",
                "temporary_cobweb",
                "rift_fire",
                "rift_wall_torch",
                "sconce_rift_wall_torch",
                "sconce_cupric_wall_torch",
                "industrial_encased_shaft",
                "industrial_encased_cogwheel",
                "industrial_encased_large_cogwheel",
                "treated_wood_encased_shaft",
                "treated_wood_encased_cogwheel",
                "treated_wood_encased_large_cogwheel",
                "millstone_structural",
                "millstone_rotor",
                "lifebud")) {
            helper.assertTrue(
                    BuiltInRegistries.BLOCK.containsKey(PoptartCore.location(id)), "Missing saved block: " + id);
        }
        helper.assertTrue(
                BuiltInRegistries.BLOCK.get(PoptartCore.location("fresh_flesh"))
                        == BuiltInRegistries.BLOCK.get(PoptartCore.location("living_flesh")),
                "The old fresh_flesh block ID must still resolve to living_flesh");
        helper.succeed();
    }
}
