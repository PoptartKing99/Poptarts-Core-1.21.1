package dev.poptartking.poptartcore.registry;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.catalog.PoptartCatalog;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PoptartCoreTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PoptartCore.MOD_ID);

    public static final Supplier<CreativeModeTab> POPTARTCORE_TAB =
            CREATIVE_MODE_TAB.register("poptartcore_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(
                            PoptartWorkstationBlocks.CRUCIBLE.item().get()))
                    .title(Component.translatable("creativetab.poptartcore.poptartcore_tab"))
                    .displayItems((parameters, output) -> PoptartCatalog.addCreativeTabItems(output))
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
