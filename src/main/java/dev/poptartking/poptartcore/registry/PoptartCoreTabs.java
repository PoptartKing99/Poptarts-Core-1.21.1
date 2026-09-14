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
                        output.accept(PoptartCoreItems.MINING_HELMET.get());
                        output.accept(PoptartCoreItems.RAW_HIDE_HELMET.get());
                        output.accept(PoptartCoreItems.RAW_HIDE_CHESTPLATE.get());
                        output.accept(PoptartCoreItems.RAW_HIDE_LEGGINGS.get());
                        output.accept(PoptartCoreItems.REPEATING_CROSSBOW.get());
                        output.accept(PoptartCoreItems.BEE_SMOKER.get());
                        output.accept(PoptartCoreItems.BEEKEEPER_HELMET.get());
                        output.accept(PoptartCoreItems.BEEKEEPER_CHESTPLATE.get());
                        output.accept(PoptartCoreItems.BEEKEEPER_LEGGINGS.get());
                        output.accept(PoptartCoreItems.BEEKEEPER_BOOTS.get());
                        output.accept(PoptartCoreItems.STEEL_HELMET.get());
                        output.accept(PoptartCoreItems.STEEL_CHESTPLATE.get());
                        output.accept(PoptartCoreItems.STEEL_LEGGINGS.get());
                        output.accept(PoptartCoreItems.STEEL_BOOTS.get());

                        output.accept(PoptartCoreItems.CRUCIBLE);
                        output.accept(PoptartCoreItems.BLAST_FURNACE);
                        output.accept(PoptartCoreItems.BLOOMERY);
                        output.accept(PoptartCoreItems.WORKBENCH);
                        output.accept(PoptartCoreItems.QUERN);
                        output.accept(PoptartCoreItems.PORTABLE_ENGINE);
                        output.accept(PoptartCoreItems.MILLSTONE);
                        output.accept(PoptartCoreItems.IRON_BLOOM);
                        output.accept(PoptartCoreItems.HAMMER.get());
                        output.accept(PoptartCoreItems.BONE_PICK.get());
                        output.accept(PoptartCoreItems.FLINT_AXE.get());
                        output.accept(PoptartCoreItems.FLINT_SHOVEL.get());
                        output.accept(PoptartCoreItems.FIRESTARTER.get());
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
                        output.accept(PoptartCoreItems.INGOT_MOULD.get());
                        output.accept(PoptartCoreItems.UNFIRED_INGOT_MOULD.get());
                        output.accept(PoptartCoreItems.PLATE_MOULD.get());
                        output.accept(PoptartCoreItems.UNFIRED_PLATE_MOULD.get());

                        output.accept(PoptartCoreItems.TIN_ORE);
                        output.accept(PoptartCoreItems.DEEPSLATE_TIN_ORE);
                        output.accept(PoptartCoreItems.RAW_TIN.get());
                        output.accept(PoptartCoreItems.RAW_TIN_BLOCK);
                        output.accept(PoptartCoreItems.TIN_INGOT.get());
                        output.accept(PoptartCoreItems.TIN_NUGGET.get());
                        output.accept(PoptartCoreBlocks.TIN_BLOCK.item().get());

                        output.accept(PoptartCoreItems.LEAD_ORE);
                        output.accept(PoptartCoreItems.DEEPSLATE_LEAD_ORE);
                        output.accept(PoptartCoreItems.RAW_LEAD.get());
                        output.accept(PoptartCoreItems.RAW_LEAD_BLOCK);
                        output.accept(PoptartCoreItems.LEAD_INGOT.get());
                        output.accept(PoptartCoreItems.LEAD_NUGGET.get());
                        output.accept(PoptartCoreBlocks.LEAD_BLOCK.item().get());

                        output.accept(PoptartCoreItems.SILVER_ORE);
                        output.accept(PoptartCoreItems.DEEPSLATE_SILVER_ORE);
                        output.accept(PoptartCoreItems.RAW_SILVER.get());
                        output.accept(PoptartCoreItems.RAW_SILVER_BLOCK);
                        output.accept(PoptartCoreItems.SILVER_INGOT.get());
                        output.accept(PoptartCoreItems.SILVER_NUGGET.get());
                        output.accept(PoptartCoreBlocks.SILVER_BLOCK.item().get());

                        output.accept(PoptartCoreItems.BRONZE_INGOT.get());
                        output.accept(PoptartCoreItems.BRONZE_NUGGET.get());
                        output.accept(PoptartCoreItems.BRONZE_PLATE.get());
                        output.accept(PoptartCoreBlocks.BRONZE_BLOCK.item().get());
                        output.accept(PoptartCoreItems.BRONZE_SWORD.get());
                        output.accept(PoptartCoreItems.BRONZE_PICKAXE.get());
                        output.accept(PoptartCoreItems.BRONZE_AXE.get());
                        output.accept(PoptartCoreItems.BRONZE_SHOVEL.get());
                        output.accept(PoptartCoreItems.BRONZE_KNIFE.get());

                        output.accept(PoptartCoreItems.STEEL_INGOT.get());
                        output.accept(PoptartCoreItems.STEEL_NUGGET.get());
                        output.accept(PoptartCoreItems.STEEL_PLATE.get());
                        output.accept(PoptartCoreBlocks.STEEL_BLOCK.item().get());
                        output.accept(PoptartCoreItems.STEEL_SWORD.get());
                        output.accept(PoptartCoreItems.STEEL_PICKAXE.get());
                        output.accept(PoptartCoreItems.STEEL_AXE.get());
                        output.accept(PoptartCoreItems.STEEL_SHOVEL.get());
                        output.accept(PoptartCoreItems.STEEL_KNIFE.get());
                        output.accept(PoptartCoreItems.COAL_COKE.get());
                        output.accept(PoptartCoreBlocks.COAL_COKE_BLOCK.item().get());
                        output.accept(PoptartCoreItems.WAX.get());
                        output.accept(PoptartCoreBlocks.WAX_BLOCK.item().get());
                        output.accept(PoptartCoreItems.REDSTONE_CIRCUIT.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
