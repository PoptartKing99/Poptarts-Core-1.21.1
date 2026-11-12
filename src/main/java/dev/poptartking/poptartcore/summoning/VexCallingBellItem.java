package dev.poptartking.poptartcore.summoning;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.Properties;

public class VexCallingBellItem extends PixieCallingBellItem {
    private static final float HEALTH_COST = 3.0F;

    public VexCallingBellItem(Properties properties, int cooldown, int count) {
        super(properties, PixieType.VEX, cooldown, count);
    }

    @Override
    protected float ringPitch() {
        return 0.55F;
    }

    @Override
    protected void onSummon(ServerLevel level, Player player) {
        player.hurt(level.damageSources().magic(), HEALTH_COST);
    }

    @Override
    protected void summonPixies(ServerLevel level, Player player) {
        VexSummonHelper.summon(level, player, this.count);
    }
}
