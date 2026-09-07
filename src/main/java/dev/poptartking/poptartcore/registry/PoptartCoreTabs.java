package dev.poptartking.poptartcore.registry;

import dev.poptartking.poptartcore.PoptartCore;
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
                    .icon(() -> new ItemStack(PoptartCoreItems.CRUCIBLE.get()))
                    .title(Component.translatable("creativetab.poptartcore.poptartcore_tab"))
                    .displayItems((parameters, output) -> {
                        output.accept(PoptartCoreItems.MINING_HELMET);
                        output.accept(PoptartCoreItems.RAW_HIDE_HELMET);
                        output.accept(PoptartCoreItems.RAW_HIDE_CHESTPLATE);
                        output.accept(PoptartCoreItems.RAW_HIDE_LEGGINGS);
                        output.accept(PoptartCoreItems.STEEL_HELMET);
                        output.accept(PoptartCoreItems.STEEL_CHESTPLATE);
                        output.accept(PoptartCoreItems.STEEL_LEGGINGS);
                        output.accept(PoptartCoreItems.STEEL_BOOTS);

                        output.accept(PoptartCoreItems.CRUCIBLE);
                        output.accept(PoptartCoreItems.INGOT_MOULD);
                        output.accept(PoptartCoreItems.UNFIRED_INGOT_MOULD);
                        output.accept(PoptartCoreItems.PLATE_MOULD);
                        output.accept(PoptartCoreItems.UNFIRED_PLATE_MOULD);

                        output.accept(PoptartCoreItems.RAW_TIN);
                        output.accept(PoptartCoreItems.TIN_INGOT);
                        output.accept(PoptartCoreItems.TIN_NUGGET);

                        output.accept(PoptartCoreItems.RAW_LEAD);
                        output.accept(PoptartCoreItems.LEAD_INGOT);
                        output.accept(PoptartCoreItems.LEAD_NUGGET);

                        output.accept(PoptartCoreItems.RAW_SILVER);
                        output.accept(PoptartCoreItems.SILVER_INGOT);
                        output.accept(PoptartCoreItems.SILVER_NUGGET);

                        output.accept(PoptartCoreItems.BRONZE_INGOT);
                        output.accept(PoptartCoreItems.BRONZE_NUGGET);
                        output.accept(PoptartCoreItems.BRONZE_PLATE);

                        output.accept(PoptartCoreItems.STEEL_INGOT);
                        output.accept(PoptartCoreItems.STEEL_NUGGET);
                        output.accept(PoptartCoreItems.STEEL_PLATE);
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
