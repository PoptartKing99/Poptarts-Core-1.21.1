package dev.poptartking.poptartcore.equipment;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.mixin.effect.MobEffectInstanceAccessor;
import dev.poptartking.poptartcore.registry.PoptartRingItems;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public final class RingEffects {
    public static final float MAGNET_RANGE = 4F;
    private static final ResourceLocation SUNLIGHT = PoptartCore.location("sunlight_ring_absorption");
    private static final ResourceLocation HORSESHOE = PoptartCore.location("tiny_horseshoe_speed");
    private static final AttributeModifier RAT_SNEAK_SPEED = new AttributeModifier(
            PoptartCore.location("ring_of_the_rat_sneak"), .15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    private static final String CHARGED = "poptartcore:sunlight_charged";
    private static final Map<Player, ItemStack> WORN_SUNLIGHT = new WeakHashMap<>();
    private static final Map<Player, LivingEntity> MOUNTS = new WeakHashMap<>();
    private static final Map<Player, Integer> PASSIVE_TICKS = new WeakHashMap<>();

    private RingEffects() {}

    public static void tick(Player player) {
        if (player.level().isClientSide) return;
        sunlight(player);
        magnet(player);
        mount(player);
        var speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        var rat = speed.getModifier(RAT_SNEAK_SPEED.id());
        if (player.isCrouching() && EquipmentRing.has(player, EquipmentRing.Kind.RAT)) {
            if (!RAT_SNEAK_SPEED.equals(rat)) {
                if (rat != null) speed.removeModifier(RAT_SNEAK_SPEED.id());
                speed.addTransientModifier(RAT_SNEAK_SPEED);
            }
        } else if (rat != null) {
            speed.removeModifier(RAT_SNEAK_SPEED.id());
        }
        int ticks = (PASSIVE_TICKS.getOrDefault(player, 0) + 1) % 400;
        PASSIVE_TICKS.put(player, ticks);
        if (ticks == 0 && EquipmentRing.has(player, EquipmentRing.Kind.ANCIENT_BLESSING)) player.heal(1);
        if (ticks % 10 == 0 && EquipmentRing.has(player, EquipmentRing.Kind.MALEVOLENT)) {
            for (var animal : player.level()
                    .getEntitiesOfClass(Animal.class, player.getBoundingBox().inflate(8))) {
                Vec3 away = animal.position().subtract(player.position());
                if (away.lengthSqr() < .0001) away = new Vec3(1, 0, 0);
                Vec3 target = animal.position().add(away.normalize().scale(8));
                animal.getNavigation().moveTo(target.x, target.y, target.z, 1.4);
            }
        }
    }

    private static ItemStack sunlightStack(Player player) {
        if (!EquipmentRing.has(player, EquipmentRing.Kind.SUNLIGHT)) return ItemStack.EMPTY;
        for (int slot = 0; slot < 2; slot++) {
            var stack = AccessoryInventory.get(player).getStackInSlot(slot);
            if (stack.is(PoptartRingItems.SUNLIGHT_RING.get())) return stack;
        }
        return ItemStack.EMPTY;
    }

    private static void magnet(Player player) {
        if (player.isSpectator() || !EquipmentRing.has(player, EquipmentRing.Kind.MAGNET)) return;
        var centre = player.position().add(0, player.getBbHeight() * .5, 0);
        for (var item : player.level()
                .getEntitiesOfClass(
                        net.minecraft.world.entity.item.ItemEntity.class,
                        player.getBoundingBox().inflate(MAGNET_RANGE))) {
            if (item.hasPickUpDelay() || item.isRemoved()) continue;
            var offset = centre.subtract(item.position());
            if (offset.lengthSqr() > MAGNET_RANGE * MAGNET_RANGE || offset.lengthSqr() < .0001) continue;
            item.setDeltaMovement(item.getDeltaMovement().add(offset.normalize().scale(.02)));
            MagnetGlide.pull(item, offset);
            item.hasImpulse = true;
        }
    }

    private static void setCharged(ItemStack stack, boolean charged) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean(CHARGED, charged));
    }

    private static void sunlight(Player player) {
        var stack = sunlightStack(player);
        var attribute = player.getAttribute(Attributes.MAX_ABSORPTION);
        if (stack.isEmpty()) {
            var previous = WORN_SUNLIGHT.remove(player);
            if (previous != null) setCharged(previous, false);
            attribute.removeModifier(SUNLIGHT);
            player.setAbsorptionAmount(Math.min(player.getAbsorptionAmount(), player.getMaxAbsorption()));
            return;
        }
        WORN_SUNLIGHT.put(player, stack);
        var data =
                stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        boolean charged = !data.contains(CHARGED) || data.getBoolean(CHARGED);
        if (charged || player.getAbsorptionAmount() > 0) {
            if (!attribute.hasModifier(SUNLIGHT))
                attribute.addPermanentModifier(
                        new AttributeModifier(SUNLIGHT, 8, AttributeModifier.Operation.ADD_VALUE));
            if (charged) {
                player.setAbsorptionAmount(player.getMaxAbsorption());
                setCharged(stack, false);
            }
        } else {
            attribute.removeModifier(SUNLIGHT);
        }
    }

    private static void mount(Player player) {
        LivingEntity current = player.isAlive()
                        && EquipmentRing.has(player, EquipmentRing.Kind.TINY_HORSESHOE)
                        && player.getVehicle() instanceof LivingEntity living
                ? living
                : null;
        var previous = MOUNTS.get(player);
        if (previous != current && previous != null) removeMountSpeed(previous);
        if (current == null) {
            MOUNTS.remove(player);
            return;
        }
        MOUNTS.put(player, current);
        var speed = current.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null && !speed.hasModifier(HORSESHOE))
            speed.addTransientModifier(
                    new AttributeModifier(HORSESHOE, .2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        if (player.tickCount % 200 == 0) current.heal(1);
    }

    @SubscribeEvent
    public static void waking(PlayerWakeUpEvent event) {
        if (event.getEntity().level().isClientSide) return;
        var stack = sunlightStack(event.getEntity());
        if (!stack.isEmpty()) setCharged(stack, true);
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        var player = event.getEntity();
        var mount = MOUNTS.remove(player);
        if (mount != null) removeMountSpeed(mount);
        WORN_SUNLIGHT.remove(player);
        PASSIVE_TICKS.remove(player);
    }

    private static void removeMountSpeed(LivingEntity mount) {
        var speed = mount.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) speed.removeModifier(HORSESHOE);
    }

    @SubscribeEvent
    public static void visibility(net.neoforged.neoforge.event.entity.living.LivingEvent.LivingVisibilityEvent event) {
        if (event.getEntity() instanceof Player player && EquipmentRing.has(player, EquipmentRing.Kind.RAT))
            event.modifyVisibility(.8);
    }

    @SubscribeEvent
    public static void incoming(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)
                || player.level().isClientSide
                || !event.getSource().is(DamageTypes.WITHER)
                || !EquipmentRing.has(player, EquipmentRing.Kind.MERCURY)) return;
        float amount = event.getAmount() * .7F;
        float survivable = Math.max(0, player.getHealth() - 1);
        if (amount > survivable) {
            float excess = amount - survivable;
            var food = player.getFoodData();
            float absorbed = Math.min(excess, food.getFoodLevel());
            food.setFoodLevel(food.getFoodLevel() - Mth.ceil(absorbed));
            amount = survivable + excess - absorbed;
        }
        event.setAmount(amount);
    }

    @SubscribeEvent
    public static void effectAdded(MobEffectEvent.Added event) {
        if (!(event.getEntity() instanceof Player player)
                || player.level().isClientSide
                || !EquipmentRing.has(player, EquipmentRing.Kind.AFFLICTED)) return;
        var effect = event.getEffectInstance();
        if (effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL)
            ((MobEffectInstanceAccessor) effect)
                    .poptartcore$setDuration(effect.mapDuration(duration -> (int) (duration * .7F)));
    }

    @SubscribeEvent
    public static void damageDone(LivingDamageEvent.Post event) {
        if (event.getEntity().level().isClientSide) return;
        var source = event.getSource();
        if (source.getEntity() instanceof Player attacker && EquipmentRing.has(attacker, EquipmentRing.Kind.MALEVOLENT))
            attacker.heal(event.getNewDamage() * .2F);
        if (!(event.getEntity() instanceof Player victim)
                || !EquipmentRing.has(victim, EquipmentRing.Kind.THORN)
                || source.is(DamageTypes.THORNS)) return;
        var attacker = source.getEntity();
        if (attacker == null || attacker == victim || !attacker.isAlive()) return;
        boolean physical = source.getDirectEntity() == attacker
                && !source.is(DamageTypeTags.IS_EXPLOSION)
                && !source.is(DamageTypeTags.WITCH_RESISTANT_TO);
        attacker.hurt(victim.damageSources().thorns(victim), Math.min(physical ? 3 : 1.5F, event.getNewDamage()));
    }
}
