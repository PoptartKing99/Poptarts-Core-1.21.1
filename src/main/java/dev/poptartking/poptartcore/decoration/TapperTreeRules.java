package dev.poptartking.poptartcore.decoration;

import com.farcr.nomansland.common.block.tap.TapTrees;
import com.farcr.nomansland.common.registry.NMLRegistries;
import dev.poptartking.poptartcore.registry.PoptartCoreFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/** Use NML's live datapack source predicates, conditions, and natural-tree checks. */
public final class TapperTreeRules {
    private TapperTreeRules() {}

    public static Fluid fluidAt(Level level, BlockPos log) {
        var registry = level.registryAccess().registryOrThrow(NMLRegistries.TAP_INTERACTION_KEY);
        for (String id : new String[] {"maple", "resin"}) {
            var rule = registry.get(ResourceLocation.fromNamespaceAndPath("nomansland", id));
            var state = level.getBlockState(log);
            if (rule == null
                    || !rule.source().matches(state)
                    || !rule.conditions().test(level, log)) continue;
            if (rule.source().tree().isPresent()
                    && !TapTrees.isTree(level, log, state, rule.source().tree().get())) continue;
            return id.equals("maple")
                    ? PoptartCoreFluids.MAPLE_SYRUP.source().get()
                    : PoptartCoreFluids.RESIN.source().get();
        }
        return Fluids.EMPTY;
    }
}
