package dev.poptartking.poptartcore.catalog;

import com.mojang.logging.LogUtils;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartToolItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Opt-in client checks, excluded from the published mod. */
@EventBusSubscriber(modid = PoptartCore.MOD_ID, value = Dist.CLIENT)
public final class CatalogClientSmoke {
    private static int ticks;
    private static boolean done;

    private CatalogClientSmoke() {}

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (done) return;
        var mc = Minecraft.getInstance();
        if (++ticks > 2400) {
            done = true;
            LogUtils.getLogger().error("CATALOG_CLIENT_FAIL: test world did not become ready");
            mc.stop();
            return;
        }
        if (mc.level == null || mc.player == null || mc.screen != null) return;
        done = true;
        try {
            var weapon = new ItemStack(PoptartToolItems.REPEATING_CROSSBOW.get());
            var actor = new UsingEntity(mc.level);
            check(
                    property(weapon, null, "pull") == 0 && property(weapon, null, "pulling") == 0,
                    "Null entity must not show a charge");
            check(
                    property(weapon, actor, "charged") == 0 && property(weapon, actor, "firework") == 0,
                    "Empty crossbow must not show loaded projectiles");
            sprite(weapon, actor, "poptartcore:item/crossbows/repeating_crossbow_standby");
            for (int elapsed : new int[] {0, 8, 9, 15}) {
                actor.use(weapon, weapon, elapsed);
                check(
                        Math.abs(property(weapon, actor, "pull") - elapsed / 15.0F) < 0.00001F,
                        "Actual pull property must follow the 15-tick charge");
                check(property(weapon, actor, "pulling") == 1, "Actual pulling property must recognize the used stack");
                String frame = elapsed >= 15 ? "3" : elapsed >= 9 ? "2" : "1";
                sprite(weapon, actor, "poptartcore:item/crossbows/repeating_crossbow_" + frame);
            }
            actor.use(weapon, new ItemStack(Items.CROSSBOW), 9);
            check(property(weapon, actor, "pulling") == 0, "Charging another stack must not animate this crossbow");
            actor.stop();
            weapon.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(new ItemStack(Items.ARROW)));
            check(
                    property(weapon, actor, "charged") == 1
                            && property(weapon, actor, "pull") == 0
                            && property(weapon, actor, "pulling") == 0
                            && property(weapon, actor, "firework") == 0,
                    "Loaded-arrow properties must replace the charging state");
            sprite(weapon, actor, "poptartcore:item/crossbows/repeating_crossbow_arrow_base");
            weapon.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(new ItemStack(Items.FIREWORK_ROCKET)));
            check(
                    property(weapon, actor, "charged") == 1 && property(weapon, actor, "firework") == 1,
                    "Loaded firework must set both predicates");
            sprite(weapon, actor, "minecraft:item/crossbow_firework");
            weapon.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
            check(
                    property(weapon, actor, "charged") == 0 && property(weapon, actor, "firework") == 0,
                    "Clearing projectiles must reset the properties");
            sprite(weapon, actor, "poptartcore:item/crossbows/repeating_crossbow_standby");
            BeekeeperClientChecks.verify(mc);
            LogUtils.getLogger()
                    .info(
                            "CATALOG_CLIENT_PASS: crossbow models, beekeeper model/sleeves and workbench screen verified");
        } catch (Throwable failure) {
            LogUtils.getLogger().error("CATALOG_CLIENT_FAIL", failure);
        } finally {
            mc.stop();
        }
    }

    private static float property(ItemStack stack, LivingEntity entity, String name) {
        var function = ItemProperties.getProperty(stack, PoptartCore.location(name));
        check(function != null, "Missing registered item property: " + name);
        return function.call(stack, Minecraft.getInstance().level, entity, 0);
    }

    private static void sprite(ItemStack stack, LivingEntity entity, String expected) {
        var mc = Minecraft.getInstance();
        var model = mc.getItemRenderer().getModel(stack, mc.level, entity, 0);
        var actual = model.getParticleIcon().contents().name();
        check(
                actual.equals(ResourceLocation.parse(expected)),
                "Wrong resolved model sprite: expected " + expected + ", got " + actual);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    /** Supplies item-use state to the real client functions without changing the player's inventory. */
    private static final class UsingEntity extends ArmorStand {
        UsingEntity(ClientLevel level) {
            super(EntityType.ARMOR_STAND, level);
        }

        void use(ItemStack held, ItemStack using, int elapsed) {
            setItemInHand(InteractionHand.MAIN_HAND, held);
            useItem = using;
            useItemRemaining = using.getUseDuration(this) - elapsed;
            setLivingEntityFlag(1, true);
        }

        void stop() {
            useItem = ItemStack.EMPTY;
            useItemRemaining = 0;
            setLivingEntityFlag(1, false);
        }
    }
}
