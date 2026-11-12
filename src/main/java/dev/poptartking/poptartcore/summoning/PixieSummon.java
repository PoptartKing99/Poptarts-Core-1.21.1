package dev.poptartking.poptartcore.summoning;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

public class PixieSummon implements INBTSerializable<CompoundTag> {
    private UUID id;
    private PixieType type;
    private final List<UUID> members = new ArrayList<>();

    @Nullable
    private Vec3 aim;

    @Nullable
    private EntityType<?> lureType;

    public PixieSummon() {}

    public PixieSummon(UUID id, PixieType type, @Nullable Vec3 aim) {
        this.id = id;
        this.type = type;
        this.aim = aim;
    }

    @Nullable
    public EntityType<?> getLureType() {
        return this.lureType;
    }

    public void setLureType(@Nullable EntityType<?> lureType) {
        this.lureType = lureType;
    }

    public UUID getId() {
        return this.id;
    }

    public PixieType getType() {
        return this.type;
    }

    public List<UUID> getMembers() {
        return this.members;
    }

    public void setMembers(List<UUID> members) {
        this.members.clear();
        this.members.addAll(members);
    }

    @Nullable
    public Vec3 getAim() {
        return this.aim;
    }

    public CompoundTag serializeNBT(Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", this.id);
        tag.putString("Type", this.type.name());
        ListTag list = new ListTag();

        for (UUID member : this.members) {
            list.add(NbtUtils.createUUID(member));
        }

        tag.put("Members", list);
        if (this.aim != null) {
            tag.putDouble("AimX", this.aim.x);
            tag.putDouble("AimY", this.aim.y);
            tag.putDouble("AimZ", this.aim.z);
        }

        if (this.lureType != null) {
            tag.putString(
                    "Lure", BuiltInRegistries.ENTITY_TYPE.getKey(this.lureType).toString());
        }

        return tag;
    }

    public void deserializeNBT(Provider provider, CompoundTag tag) {
        this.id = tag.getUUID("Id");
        this.type = PixieType.valueOf(tag.getString("Type"));
        this.members.clear();

        for (Tag entry : tag.getList("Members", 11)) {
            if (entry instanceof net.minecraft.nbt.IntArrayTag array && array.getAsIntArray().length == 4)
                this.members.add(NbtUtils.loadUUID(entry));
        }

        this.aim = tag.contains("AimX")
                ? new Vec3(tag.getDouble("AimX"), tag.getDouble("AimY"), tag.getDouble("AimZ"))
                : null;
        ResourceLocation lure = ResourceLocation.tryParse(tag.getString("Lure"));
        this.lureType = lure != null && BuiltInRegistries.ENTITY_TYPE.containsKey(lure)
                ? BuiltInRegistries.ENTITY_TYPE.get(lure)
                : null;
    }
}
