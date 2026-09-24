package dev.poptartking.poptartcore.mixin.integration.create;

import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity.CasingType;
import dev.poptartking.poptartcore.integration.create.PoptartBeltCasing;
import dev.poptartking.poptartcore.integration.create.PoptartBeltCasingAccess;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeltBlockEntity.class)
public abstract class BeltBlockEntityMixin implements PoptartBeltCasingAccess {
    @Unique private static final String POPTARTCORE_CASING_KEY = "poptartcore:BeltCasing";
    @Unique private static final String POPTARTCORE_LEGACY_CASING_KEY = "PoptartCasing";
    @Unique private PoptartBeltCasing poptartcore$casingBeforeRead;

    @Override
    public PoptartBeltCasing poptartcore$getCasing() {
        BeltBlockEntity belt = (BeltBlockEntity) (Object) this;
        CompoundTag data = belt.getPersistentData();
        String id = data.contains(POPTARTCORE_CASING_KEY)
                ? data.getString(POPTARTCORE_CASING_KEY)
                : data.getString(POPTARTCORE_LEGACY_CASING_KEY);
        return PoptartBeltCasing.fromId(id);
    }

    @Override
    public void poptartcore$setCasing(PoptartBeltCasing casing) {
        BeltBlockEntity belt = (BeltBlockEntity) (Object) this;
        if (poptartcore$getCasing() == casing) {
            return;
        }
        belt.setCasingType(CasingType.ANDESITE);
        BlockEntity current = belt.getLevel().getBlockEntity(belt.getBlockPos());
        if (current instanceof BeltBlockEntity currentBelt) {
            ((PoptartBeltCasingAccess) currentBelt).poptartcore$storeCasing(casing);
        }
    }

    @Override
    public void poptartcore$storeCasing(PoptartBeltCasing casing) {
        BeltBlockEntity belt = (BeltBlockEntity) (Object) this;
        belt.getPersistentData().putString(POPTARTCORE_CASING_KEY, casing.id());
        belt.getPersistentData().remove(POPTARTCORE_LEGACY_CASING_KEY);
        poptartcore$refresh(belt);
    }

    @Inject(method = "setCasingType", at = @At("HEAD"))
    private void poptartcore$clearOnVanillaCasing(CasingType type, CallbackInfo callback) {
        BeltBlockEntity belt = (BeltBlockEntity) (Object) this;
        if (belt.getPersistentData().contains(POPTARTCORE_CASING_KEY)
                || belt.getPersistentData().contains(POPTARTCORE_LEGACY_CASING_KEY)) {
            belt.getPersistentData().remove(POPTARTCORE_CASING_KEY);
            belt.getPersistentData().remove(POPTARTCORE_LEGACY_CASING_KEY);
            poptartcore$refresh(belt);
        }
    }

    @Inject(method = "read", at = @At("HEAD"))
    private void poptartcore$captureCasingBeforeRead(CompoundTag tag, HolderLookup.Provider registries,
                                                      boolean clientPacket, CallbackInfo callback) {
        poptartcore$casingBeforeRead = poptartcore$getCasing();
    }

    @Inject(method = "read", at = @At("TAIL"))
    private void poptartcore$readCasing(CompoundTag tag, HolderLookup.Provider registries,
                                        boolean clientPacket, CallbackInfo callback) {
        BeltBlockEntity belt = (BeltBlockEntity) (Object) this;

        // Migrate both earlier formats: a root tag and an unnamespaced NeoForgeData key.
        CompoundTag data = belt.getPersistentData();
        if (!data.contains(POPTARTCORE_CASING_KEY)) {
            String legacyId = data.contains(POPTARTCORE_LEGACY_CASING_KEY)
                    ? data.getString(POPTARTCORE_LEGACY_CASING_KEY)
                    : tag.getString(POPTARTCORE_LEGACY_CASING_KEY);
            PoptartBeltCasing legacyCasing = PoptartBeltCasing.fromId(legacyId);
            if (legacyCasing != null) {
                data.putString(POPTARTCORE_CASING_KEY, legacyCasing.id());
            }
        }
        data.remove(POPTARTCORE_LEGACY_CASING_KEY);

        PoptartBeltCasing casingAfterRead = poptartcore$getCasing();
        if (poptartcore$casingBeforeRead != casingAfterRead
                && belt.hasLevel() && belt.getLevel().isClientSide) {
            belt.requestModelDataUpdate();
            belt.getLevel().sendBlockUpdated(belt.getBlockPos(), belt.getBlockState(), belt.getBlockState(), 16);
        }
        poptartcore$casingBeforeRead = null;
    }

    @Unique
    private static void poptartcore$refresh(BeltBlockEntity belt) {
        belt.setChanged();
        if (belt.getLevel() != null && belt.getLevel().isClientSide) {
            belt.requestModelDataUpdate();
            belt.getLevel().sendBlockUpdated(belt.getBlockPos(), belt.getBlockState(), belt.getBlockState(), 16);
        } else {
            belt.sendData();
        }
    }
}
