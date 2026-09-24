package dev.poptartking.poptartcore.integration.create;

import dev.poptartking.poptartcore.PoptartCore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.client.event.ModelEvent;

public final class PoptartBeltCasingClient {
    public static final ModelProperty<PoptartBeltCasing> CASING_PROPERTY = new ModelProperty<>();
    private static final String[] CASING_MODELS = {
            "diagonal_end", "diagonal_middle", "diagonal_pulley", "diagonal_start",
            "horizontal_end", "horizontal_middle", "horizontal_pulley", "horizontal_start",
            "sideways_end", "sideways_middle", "sideways_pulley", "sideways_start"
    };

    private PoptartBeltCasingClient() {}

    public static void registerModels(ModelEvent.RegisterAdditional event) {
        for (PoptartBeltCasing casing : PoptartBeltCasing.values()) {
            for (String model : CASING_MODELS) {
                event.register(ModelResourceLocation.standalone(casingModel(casing, model)));
            }
            event.register(ModelResourceLocation.standalone(coverModel(casing, "x")));
            event.register(ModelResourceLocation.standalone(coverModel(casing, "z")));
        }
    }

    public static ResourceLocation casingModel(PoptartBeltCasing casing, String model) {
        return PoptartCore.location("block/belt/" + casing.id() + "/casing/" + model);
    }

    public static ResourceLocation coverModel(PoptartBeltCasing casing, String axis) {
        return PoptartCore.location("block/belt/" + casing.id() + "/cover_" + axis);
    }
}
