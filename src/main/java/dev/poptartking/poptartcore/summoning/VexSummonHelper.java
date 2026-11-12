package dev.poptartking.poptartcore.summoning;

import dev.poptartking.poptartcore.registry.PoptartCoreAttachments;
import dev.poptartking.poptartcore.registry.PoptartCoreSounds;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gameevent.GameEvent;

public class VexSummonHelper {
    private static final int VEX_LIFE = Integer.MAX_VALUE;

    public static void summon(ServerLevel level, LivingEntity summoner, int count) {
        PixieSummon summon = new PixieSummon(UUID.randomUUID(), PixieType.VEX, null);
        List<UUID> members = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Vex vex = (Vex) EntityType.VEX.create(level);
            if (vex != null) {
                double ox = summoner.getX() + (summoner.getRandom().nextDouble() - 0.5) * 2.0;
                double oz = summoner.getZ() + (summoner.getRandom().nextDouble() - 0.5) * 2.0;
                vex.moveTo(ox, summoner.getEyeY() + 0.5, oz, summoner.getYRot(), 0.0F);
                PixieData vd = (PixieData) vex.getData(PoptartCoreAttachments.VEX_DATA);
                vd.setOwnerUuid(summoner.getUUID());
                vd.setSummonId(summon.getId());
                vex.setData(PoptartCoreAttachments.VEX_DATA, vd);
                equipSword(vex);
                vex.setLimitedLife(VEX_LIFE);
                vex.setPersistenceRequired();
                level.addFreshEntity(vex);
                level.gameEvent(summoner, GameEvent.ENTITY_PLACE, vex.position());
                members.add(vex.getUUID());
                PhasingMover.pixieBurst(level, vex.getX(), vex.getY() + 0.5, vex.getZ(), 10, PixieType.VEX);
            }
        }

        summon.setMembers(members);
        PixieSummons.register(summoner, summon);
        level.playSound(
                null, summoner.blockPosition(), PoptartCoreSounds.VEX_SUMMON.get(), SoundSource.PLAYERS, 1.0F, 1.2F);
    }

    public static void equipSword(Vex vex) {
        vex.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        vex.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }
}
