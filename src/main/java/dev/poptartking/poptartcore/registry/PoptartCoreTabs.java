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
                        output.accept(PoptartCoreItems.REPEATING_CROSSBOW);
                        output.accept(PoptartCoreItems.BEE_SMOKER);
                        output.accept(PoptartCoreItems.BEEKEEPER_HELMET);
                        output.accept(PoptartCoreItems.BEEKEEPER_CHESTPLATE);
                        output.accept(PoptartCoreItems.BEEKEEPER_LEGGINGS);
                        output.accept(PoptartCoreItems.BEEKEEPER_BOOTS);
                        output.accept(PoptartCoreItems.STEEL_HELMET);
                        output.accept(PoptartCoreItems.STEEL_CHESTPLATE);
                        output.accept(PoptartCoreItems.STEEL_LEGGINGS);
                        output.accept(PoptartCoreItems.STEEL_BOOTS);

                        output.accept(PoptartCoreItems.CRUCIBLE);
                        output.accept(PoptartCoreItems.BLAST_FURNACE);
                        output.accept(PoptartCoreItems.BLOOMERY);
                        output.accept(PoptartCoreItems.WORKBENCH);
                        output.accept(PoptartCoreItems.QUERN);
                        output.accept(PoptartCoreItems.PORTABLE_ENGINE);
                        output.accept(PoptartCoreItems.MILLSTONE);
                        output.accept(PoptartCoreItems.IRON_BLOOM);
                        output.accept(PoptartCoreItems.HAMMER);
                        output.accept(PoptartCoreItems.BONE_PICK);
                        output.accept(PoptartCoreItems.FLINT_AXE);
                        output.accept(PoptartCoreItems.FLINT_SHOVEL);
                        output.accept(PoptartCoreItems.FIRESTARTER);
                        output.accept(PoptartCoreItems.CLINKER_BRICKS);
                        output.accept(PoptartCoreItems.CLINKER_BRICK_SLAB);
                        output.accept(PoptartCoreItems.CLINKER_BRICK_STAIRS);
                        output.accept(PoptartCoreItems.CLINKER_BRICK_WALL);
                        output.accept(PoptartCoreItems.CLINKER_TILE);
                        output.accept(PoptartCoreItems.CLINKER_TILE_SLAB);
                        output.accept(PoptartCoreItems.CLINKER_TILE_STAIRS);
                        output.accept(PoptartCoreItems.CLINKER_TILE_WALL);
                        output.accept(PoptartCoreItems.MOSAIC_CLINKER_TILE);
                        output.accept(PoptartCoreItems.CHISELED_CLINKER_TILE);
                        output.accept(PoptartCoreItems.CLINKER_PILLAR);
                        output.accept(PoptartCoreItems.INGOT_MOULD);
                        output.accept(PoptartCoreItems.UNFIRED_INGOT_MOULD);
                        output.accept(PoptartCoreItems.PLATE_MOULD);
                        output.accept(PoptartCoreItems.UNFIRED_PLATE_MOULD);

                        output.accept(PoptartCoreItems.TIN_ORE);
                        output.accept(PoptartCoreItems.DEEPSLATE_TIN_ORE);
                        output.accept(PoptartCoreItems.RAW_TIN);
                        output.accept(PoptartCoreItems.RAW_TIN_BLOCK);
                        output.accept(PoptartCoreItems.TIN_INGOT);
                        output.accept(PoptartCoreItems.TIN_NUGGET);
                        output.accept(PoptartCoreItems.TIN_BLOCK);

                        output.accept(PoptartCoreItems.LEAD_ORE);
                        output.accept(PoptartCoreItems.DEEPSLATE_LEAD_ORE);
                        output.accept(PoptartCoreItems.RAW_LEAD);
                        output.accept(PoptartCoreItems.RAW_LEAD_BLOCK);
                        output.accept(PoptartCoreItems.LEAD_INGOT);
                        output.accept(PoptartCoreItems.LEAD_NUGGET);
                        output.accept(PoptartCoreItems.LEAD_BLOCK);

                        output.accept(PoptartCoreItems.SILVER_ORE);
                        output.accept(PoptartCoreItems.DEEPSLATE_SILVER_ORE);
                        output.accept(PoptartCoreItems.RAW_SILVER);
                        output.accept(PoptartCoreItems.RAW_SILVER_BLOCK);
                        output.accept(PoptartCoreItems.SILVER_INGOT);
                        output.accept(PoptartCoreItems.SILVER_NUGGET);
                        output.accept(PoptartCoreItems.SILVER_BLOCK);

                        output.accept(PoptartCoreItems.BRONZE_INGOT);
                        output.accept(PoptartCoreItems.BRONZE_NUGGET);
                        output.accept(PoptartCoreItems.BRONZE_PLATE);
                        output.accept(PoptartCoreItems.BRONZE_BLOCK);
                        output.accept(PoptartCoreItems.BRONZE_SWORD);
                        output.accept(PoptartCoreItems.BRONZE_PICKAXE);
                        output.accept(PoptartCoreItems.BRONZE_AXE);
                        output.accept(PoptartCoreItems.BRONZE_SHOVEL);
                        output.accept(PoptartCoreItems.BRONZE_KNIFE);

                        output.accept(PoptartCoreItems.STEEL_INGOT);
                        output.accept(PoptartCoreItems.STEEL_NUGGET);
                        output.accept(PoptartCoreItems.STEEL_PLATE);
                        output.accept(PoptartCoreItems.STEEL_BLOCK);
                        output.accept(PoptartCoreItems.STEEL_SWORD);
                        output.accept(PoptartCoreItems.STEEL_PICKAXE);
                        output.accept(PoptartCoreItems.STEEL_AXE);
                        output.accept(PoptartCoreItems.STEEL_SHOVEL);
                        output.accept(PoptartCoreItems.STEEL_KNIFE);
                        output.accept(PoptartCoreItems.COAL_COKE);
                        output.accept(PoptartCoreItems.COAL_COKE_BLOCK);
                        output.accept(PoptartCoreItems.WAX);
                        output.accept(PoptartCoreItems.WAX_BLOCK);
                        output.accept(PoptartCoreItems.REDSTONE_CIRCUIT);
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
