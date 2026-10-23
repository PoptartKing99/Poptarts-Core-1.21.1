package dev.poptartking.poptartcore.mixin.integration.ragdoll;

import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ElytraModel.class)
public interface CorpseElytraModelAccessor {
    @Accessor("leftWing")
    ModelPart poptartcore$leftWing();

    @Accessor("rightWing")
    ModelPart poptartcore$rightWing();
}
