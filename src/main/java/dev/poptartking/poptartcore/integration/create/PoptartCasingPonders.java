package dev.poptartking.poptartcore.integration.create;

import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogwheelBlock;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedShaftBlock;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public final class PoptartCasingPonders implements PonderPlugin {
    private static final ResourceLocation SHAFT_SCHEMATIC =
            ResourceLocation.fromNamespaceAndPath("create", "shaft/encasing");
    private static final ResourceLocation COG_SCHEMATIC =
            ResourceLocation.fromNamespaceAndPath("create", "cog/encasing");

    public static void register() {
        PonderIndex.addPlugin(new PoptartCasingPonders());
    }

    @Override
    public String getModId() {
        return PoptartCore.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        addFamily(helper, "industrial_plating", "industrial_plating", PoptartCoreBlocks.INDUSTRIAL_PLATING.get(),
                PoptartCoreBlocks.INDUSTRIAL_ENCASED_SHAFT.get(),
                PoptartCoreBlocks.INDUSTRIAL_ENCASED_COGWHEEL.get());
        addFamily(helper, "treated_wood", "treated_wood_casing", PoptartCoreBlocks.TREATED_WOOD_CASING.get(),
                PoptartCoreBlocks.TREATED_WOOD_ENCASED_SHAFT.get(),
                PoptartCoreBlocks.TREATED_WOOD_ENCASED_COGWHEEL.get());
    }

    private static void addFamily(PonderSceneRegistrationHelper<ResourceLocation> helper, String name, String itemPath,
                                  Block casing, Block encasedShaft, Block encasedCog) {
        ResourceLocation itemId = PoptartCore.location(itemPath);
        helper.addStoryBoard(itemId, SHAFT_SCHEMATIC,
                (scene, util) -> shaftScene(scene, util, name, casing, encasedShaft));
        helper.addStoryBoard(itemId, COG_SCHEMATIC,
                (scene, util) -> cogScene(scene, util, name, casing, encasedCog));
    }

    private static void shaftScene(SceneBuilder scene, SceneBuildingUtil util, String name,
                                   Block casing, Block encasedShaft) {
        scene.title(name + "_shaft_encasing", "Encasing Shafts");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(0, 1, 2, 4, 1, 2), Direction.DOWN);
        scene.idle(20);
        scene.overlay().showControls(util.vector().topOf(3, 1, 2), Pointing.DOWN, 50)
                .rightClick().withItem(casing.asItem().getDefaultInstance());
        scene.idle(7);
        scene.world().setBlock(util.grid().at(3, 1, 2), encasedShaft.defaultBlockState()
                .setValue(EncasedShaftBlock.AXIS, Direction.Axis.X), true);
        scene.overlay().showText(70).text("Use this casing on a shaft to encase it")
                .pointAt(util.vector().centerOf(3, 1, 2));
        scene.idle(80);
    }

    private static void cogScene(SceneBuilder scene, SceneBuildingUtil util, String name,
                                 Block casing, Block encasedCog) {
        scene.title(name + "_cog_encasing", "Encasing Cogwheels");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(1, 1, 2, 4, 2, 4), Direction.DOWN);
        scene.idle(20);
        scene.overlay().showControls(util.vector().topOf(2, 1, 2), Pointing.DOWN, 50)
                .rightClick().withItem(casing.asItem().getDefaultInstance());
        scene.idle(7);
        scene.world().setBlock(util.grid().at(2, 1, 2), encasedCog.defaultBlockState()
                .setValue(EncasedCogwheelBlock.AXIS, Direction.Axis.Y), true);
        scene.overlay().showText(70).text("Use this casing on a cogwheel to encase it")
                .pointAt(util.vector().centerOf(2, 1, 2));
        scene.idle(80);
    }
}
