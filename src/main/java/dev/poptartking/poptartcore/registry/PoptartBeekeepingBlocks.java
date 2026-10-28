package dev.poptartking.poptartcore.registry;

import static dev.poptartking.poptartcore.catalog.CreativeCatalogGroup.TOOLS;

import dev.poptartking.poptartcore.beekeeping.BeehiveRoofBlock;
import dev.poptartking.poptartcore.beekeeping.BeehiveSupportBlock;
import dev.poptartking.poptartcore.catalog.CatalogBlockDefinition;
import dev.poptartking.poptartcore.catalog.PoptartCatalog;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class PoptartBeekeepingBlocks {
    public static final CatalogBlockDefinition<BeehiveSupportBlock> BEEHIVE_SUPPORT = PoptartCatalog.block(
                    "beehive_support",
                    () -> new BeehiveSupportBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BEEHIVE)
                            .noOcclusion()
                            .strength(0.8F)))
            .externalModel()
            .customLoot()
            .mineableWithAxe()
            .creative(TOOLS, 390);

    public static final CatalogBlockDefinition<BeehiveRoofBlock> BEEHIVE_WHITE_ROOF =
            beehiveRoof("white").creative(TOOLS, 400);

    public static final CatalogBlockDefinition<BeehiveRoofBlock> BEEHIVE_LIGHT_GRAY_ROOF =
            beehiveRoof("light_gray").creative(TOOLS, 410);

    public static final CatalogBlockDefinition<BeehiveRoofBlock> BEEHIVE_GRAY_ROOF =
            beehiveRoof("gray").creative(TOOLS, 420);

    public static final CatalogBlockDefinition<BeehiveRoofBlock> BEEHIVE_BLACK_ROOF =
            beehiveRoof("black").creative(TOOLS, 430);

    public static final CatalogBlockDefinition<BeehiveRoofBlock> BEEHIVE_BROWN_ROOF =
            beehiveRoof("brown").creative(TOOLS, 440);

    public static final CatalogBlockDefinition<BeehiveRoofBlock> BEEHIVE_RED_ROOF =
            beehiveRoof("red").creative(TOOLS, 450);

    public static final CatalogBlockDefinition<BeehiveRoofBlock> BEEHIVE_ORANGE_ROOF =
            beehiveRoof("orange").creative(TOOLS, 460);

    public static final CatalogBlockDefinition<BeehiveRoofBlock> BEEHIVE_YELLOW_ROOF =
            beehiveRoof("yellow").creative(TOOLS, 470);

    public static final CatalogBlockDefinition<BeehiveRoofBlock> BEEHIVE_LIME_ROOF =
            beehiveRoof("lime").creative(TOOLS, 480);

    public static final CatalogBlockDefinition<BeehiveRoofBlock> BEEHIVE_GREEN_ROOF =
            beehiveRoof("green").creative(TOOLS, 490);

    public static final CatalogBlockDefinition<BeehiveRoofBlock> BEEHIVE_CYAN_ROOF =
            beehiveRoof("cyan").creative(TOOLS, 500);

    public static final CatalogBlockDefinition<BeehiveRoofBlock> BEEHIVE_LIGHT_BLUE_ROOF =
            beehiveRoof("light_blue").creative(TOOLS, 510);

    public static final CatalogBlockDefinition<BeehiveRoofBlock> BEEHIVE_BLUE_ROOF =
            beehiveRoof("blue").creative(TOOLS, 520);

    public static final CatalogBlockDefinition<BeehiveRoofBlock> BEEHIVE_PURPLE_ROOF =
            beehiveRoof("purple").creative(TOOLS, 530);

    public static final CatalogBlockDefinition<BeehiveRoofBlock> BEEHIVE_MAGENTA_ROOF =
            beehiveRoof("magenta").creative(TOOLS, 540);

    public static final CatalogBlockDefinition<BeehiveRoofBlock> BEEHIVE_PINK_ROOF =
            beehiveRoof("pink").creative(TOOLS, 550);

    public static BeehiveRoofBlock beehiveRoofForDye(DyeColor color) {
        return switch (color) {
            case WHITE -> BEEHIVE_WHITE_ROOF.get();
            case LIGHT_GRAY -> BEEHIVE_LIGHT_GRAY_ROOF.get();
            case GRAY -> BEEHIVE_GRAY_ROOF.get();
            case BLACK -> BEEHIVE_BLACK_ROOF.get();
            case BROWN -> BEEHIVE_BROWN_ROOF.get();
            case RED -> BEEHIVE_RED_ROOF.get();
            case ORANGE -> BEEHIVE_ORANGE_ROOF.get();
            case YELLOW -> BEEHIVE_YELLOW_ROOF.get();
            case LIME -> BEEHIVE_LIME_ROOF.get();
            case GREEN -> BEEHIVE_GREEN_ROOF.get();
            case CYAN -> BEEHIVE_CYAN_ROOF.get();
            case LIGHT_BLUE -> BEEHIVE_LIGHT_BLUE_ROOF.get();
            case BLUE -> BEEHIVE_BLUE_ROOF.get();
            case PURPLE -> BEEHIVE_PURPLE_ROOF.get();
            case MAGENTA -> BEEHIVE_MAGENTA_ROOF.get();
            case PINK -> BEEHIVE_PINK_ROOF.get();
        };
    }

    private static CatalogBlockDefinition<BeehiveRoofBlock> beehiveRoof(String color) {
        return PoptartCatalog.block(
                        "beehive_" + color + "_roof",
                        () -> new BeehiveRoofBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BEEHIVE)
                                .noOcclusion()
                                .strength(0.8F)))
                .externalModel()
                .customLoot()
                .mineableWithAxe();
    }

    private PoptartBeekeepingBlocks() {}

    public static void initialize() {}
}
