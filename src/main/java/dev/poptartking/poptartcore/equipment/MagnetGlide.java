package dev.poptartking.poptartcore.equipment;

import java.util.Map;
import java.util.TreeSet;
import java.util.WeakHashMap;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;

/** Collision-safe climbing using ordinary item movement, never a position jump. */
final class MagnetGlide {
    static final double SPEED = .12;
    private static final Map<ItemEntity, Climb> CLIMBS = new WeakHashMap<>();

    private record Climb(double top, int expiresAtTick) {}

    private MagnetGlide() {}

    static void pull(ItemEntity item, Vec3 offset) {
        if (offset.horizontalDistanceSqr() < .0001 || item.isInWater() || item.isInLava()) {
            CLIMBS.remove(item);
            return;
        }
        var forward = new Vec3(offset.x, 0, offset.z).normalize().scale(.4);
        var box = item.getBoundingBox();
        var level = item.level();
        var climb = CLIMBS.get(item);
        if (climb != null && item.tickCount > climb.expiresAtTick()) climb = null;
        if (climb == null && item.onGround() && !level.noCollision(item, box.expandTowards(forward))) {
            var heights = new TreeSet<Double>();
            for (var shape : level.getBlockCollisions(
                    item, box.expandTowards(forward).expandTowards(0, RingEffects.MAGNET_RANGE, 0))) {
                for (var obstacle : shape.toAabbs()) {
                    double rise = obstacle.maxY - box.minY;
                    if (rise > .001 && rise <= RingEffects.MAGNET_RANGE + .00001) heights.add(rise);
                }
            }
            for (double rise : heights) {
                if (level.noCollision(item, box.expandTowards(0, rise, 0).deflate(1e-7))
                        && level.noCollision(
                                item,
                                box.move(0, rise, 0).expandTowards(forward).deflate(1e-7))) {
                    climb = new Climb(box.minY + rise, item.tickCount + 1);
                    break;
                }
            }
        }
        if (climb == null) {
            CLIMBS.remove(item);
            return;
        }
        double remaining = climb.top() - box.minY;
        boolean supported = !level.noCollision(item, box.move(0, -.03, 0));
        if (remaining < -.001
                || (remaining <= .001 && supported)
                || (remaining > .001 && level.noCollision(item, box.expandTowards(forward)))) {
            CLIMBS.remove(item);
            var velocity = item.getDeltaMovement();
            item.setDeltaMovement(velocity.x, Math.min(0, velocity.y), velocity.z);
            return;
        }
        CLIMBS.put(item, new Climb(climb.top(), item.tickCount + 1));
        var velocity = item.getDeltaMovement();
        // ItemEntity applies its .04 gravity immediately before collision movement.
        double gravity = item.isNoGravity() ? 0 : .04;
        item.setDeltaMovement(velocity.x, Math.min(SPEED, Math.max(0, remaining)) + gravity, velocity.z);
    }
}
