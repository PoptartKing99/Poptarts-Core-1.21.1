package dev.poptartking.poptartcore.equipment;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartMaterialItems;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public final class EquipmentRing extends Item {
    public enum Kind {
        BULWARK("bulwark_ring", 927, () -> PoptartMaterialItems.LEAD_INGOT.get(), Attributes.KNOCKBACK_RESISTANCE, .05),
        RAT("ring_of_the_rat", 527, null, null, 0),
        SULFUR(
                "sulfur_gemstone_ring",
                927,
                () -> com.farcr.nomansland.common.registry.items.NMLItems.SULFUR.get(),
                null,
                0),
        SILENCE("ring_of_silence", 213, null, null, 0),
        MAGNET("magnet_ring", 547, () -> PoptartMaterialItems.MAGNETITE.get(), null, 0),
        RUSTED_IRON("rusted_iron_ring", 327, () -> Items.IRON_INGOT, Attributes.ARMOR, 4),
        DULL_IRON("dull_iron_ring", 327, () -> Items.IRON_INGOT, Attributes.ARMOR_TOUGHNESS, 3),
        MOONLIGHT("moonlight_ring", 327, () -> PoptartMaterialItems.SILVER_INGOT.get(), Attributes.MAX_HEALTH, 4),
        WARMTH("ring_of_warmth", 527, () -> PoptartMaterialItems.BRONZE_INGOT.get(), null, 0),
        FIREWARD("fireward_ring", 927, () -> Items.GOLD_INGOT, null, 0),
        SILVERCAT("silvercat_ring", 527, () -> PoptartMaterialItems.SILVER_INGOT.get(), null, 0),
        TINY_HORSESHOE("tiny_horseshoe", 527, () -> Items.IRON_INGOT, null, 0),
        THORN("thorn_ring", 547, () -> PoptartMaterialItems.SILVER_INGOT.get(), null, 0),
        AFFLICTED("ring_of_the_afflicted", 927, () -> PoptartMaterialItems.BONE_ASH.get(), null, 0),
        MERCURY("mercury_gemstone_ring", 927, null, null, 0),
        ANCIENT_BLESSING("ancient_blessing", 0, null, null, 0),
        SUNLIGHT("sunlight_ring", 327, () -> Items.GOLD_INGOT, null, 0),
        MALEVOLENT(
                "malevolent_ring", 527, () -> PoptartMaterialItems.CHTHONIC_CALCULUS.get(), Attributes.MAX_HEALTH, -4);

        private final String id;
        private final int durability;
        private final Supplier<Item> repair;
        private final Holder<Attribute> attribute;
        private final double bonus;

        Kind(String id, int durability, Supplier<Item> repair, Holder<Attribute> attribute, double bonus) {
            this.id = id;
            this.durability = durability;
            this.repair = repair;
            this.attribute = attribute;
            this.bonus = bonus;
        }
    }

    private final Kind kind;

    public EquipmentRing(Kind kind) {
        super(kind.durability > 0 ? new Properties().durability(kind.durability) : new Properties().stacksTo(1));
        this.kind = kind;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repair) {
        return kind.repair != null && repair.is(kind.repair.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.poptartcore." + kind.id + ".desc")
                .withStyle(net.minecraft.ChatFormatting.GRAY));
    }

    public static boolean canEquip(Player player, int slot, ItemStack stack) {
        if (!(stack.getItem() instanceof EquipmentRing) || slot > 1) return true;
        return healthy(stack)
                && !AccessoryInventory.get(player).getStackInSlot(1 - slot).is(stack.getItem());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var held = player.getItemInHand(hand);
        for (int slot = 0; slot < 2; slot++) {
            if (AccessoryInventory.get(player).getStackInSlot(slot).isEmpty() && canEquip(player, slot, held)) {
                if (!level.isClientSide) {
                    new PlayerAccessoryHandler(player).setStackInSlot(slot, held.copyWithCount(1));
                    held.shrink(1);
                    if (kind == Kind.THORN) player.hurt(player.damageSources().magic(), 1);
                }
                return InteractionResultHolder.sidedSuccess(held, level.isClientSide);
            }
        }
        return InteractionResultHolder.fail(held);
    }

    public static boolean has(Player player, Kind kind) {
        for (int slot = 0; slot < 2; slot++) {
            var stack = AccessoryInventory.get(player).getStackInSlot(slot);
            if (stack.getItem() instanceof EquipmentRing ring && ring.kind == kind && healthy(stack)) return true;
        }
        return false;
    }

    public static void reconcile(Player player) {
        if (player.level().isClientSide) return;
        var inventory = AccessoryInventory.get(player);
        var equipped = EnumSet.noneOf(Kind.class);
        for (int slot = 0; slot < 2; slot++) {
            var stack = inventory.getStackInSlot(slot);
            if (!(stack.getItem() instanceof EquipmentRing ring)) continue;
            if (!healthy(stack) || !equipped.add(ring.kind)) {
                inventory.setStackInSlot(slot, ItemStack.EMPTY);
                if (!player.getInventory().add(stack)) player.drop(stack, false);
            }
        }
        for (var kind : Kind.values()) {
            if (kind.attribute == null) continue;
            var attribute = player.getAttribute(kind.attribute);
            // Preserve the existing Rusted Iron modifier ID for players already wearing it.
            var id = PoptartCore.location(kind.id + (kind == Kind.RUSTED_IRON ? "_armor" : "_bonus"));
            if (equipped.contains(kind) && !attribute.hasModifier(id))
                attribute.addTransientModifier(
                        new AttributeModifier(id, kind.bonus, AttributeModifier.Operation.ADD_VALUE));
            else if (!equipped.contains(kind)) attribute.removeModifier(id);
        }
        if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
    }

    public static void hurt(Player player, DamageSource source, float damage) {
        if (player.level().isClientSide || damage <= 0) return;
        for (int slot = 0; slot < 2; slot++) {
            var stack = AccessoryInventory.get(player).getStackInSlot(slot);
            if (stack.getItem() instanceof EquipmentRing && stack.isDamageableItem() && stack.canBeHurtBy(source))
                stack.setDamageValue(
                        Math.min(stack.getMaxDamage(), stack.getDamageValue() + (int) Math.max(1, damage / 4)));
        }
        reconcile(player);
    }

    @SubscribeEvent
    public static void incomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) return;
        float amount = event.getAmount();
        if (SilenceRing.isIntangible(player)) {
            event.setCanceled(true);
            return;
        }
        if (event.getSource().is(DamageTypeTags.IS_EXPLOSION) && has(player, Kind.BULWARK)) amount *= .8F;
        if (event.getSource().is(com.farcr.nomansland.common.registry.NMLDamageTypes.CORROSION)
                && has(player, Kind.SULFUR)) amount *= .7F;
        if (event.getSource().is(DamageTypeTags.IS_FIRE) && has(player, Kind.FIREWARD)) amount *= .85F;
        if (event.getSource().is(DamageTypeTags.IS_FALL)
                && has(player, Kind.SILVERCAT)
                && amount < player.getHealth() + player.getAbsorptionAmount()) amount *= .5F;
        event.setAmount(amount);
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        reconcile(event.getEntity());
        RingEffects.tick(event.getEntity());
        SilenceRing.tick(event.getEntity());
    }

    private static boolean healthy(ItemStack stack) {
        return !stack.isDamageableItem() || stack.getDamageValue() < stack.getMaxDamage();
    }

    public static void removed(Player player, ItemStack stack) {
        if (!player.level().isClientSide && stack.getItem() instanceof EquipmentRing ring && ring.kind == Kind.THORN)
            player.hurt(player.damageSources().magic(), 1);
    }
}
