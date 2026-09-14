package dev.poptartking.poptartcore.spider;

import net.minecraft.world.entity.LivingEntity;

public interface WebShootingSpider {
    boolean poptartcore$isWebShooting();

    void poptartcore$setWebShooting(boolean shooting);

    void poptartcore$onWebCatch(LivingEntity target);

    boolean poptartcore$isPursuingWebbedTarget(LivingEntity target);
}
