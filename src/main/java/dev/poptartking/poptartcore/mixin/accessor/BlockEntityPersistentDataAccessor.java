package dev.poptartking.poptartcore.mixin.accessor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockEntity.class)
public interface BlockEntityPersistentDataAccessor {
    @Accessor("customPersistentData")
    CompoundTag poptartcore$getExistingPersistentData();
}
