package dev.poptartking.poptartcore.flesh;

import dev.poptartking.poptartcore.PoptartCore;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.LootTableLoadEvent;

/** Keep Wayfarer's innards chances without making Brewin' and Chewin' mandatory. */
@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public final class OptionalFleshDrops {
    private OptionalFleshDrops() {}

    @SubscribeEvent
    public static void onLootLoad(LootTableLoadEvent event) {
        boolean living = event.getName().equals(PoptartCore.location("blocks/living_flesh"));
        if (!living && !event.getName().equals(PoptartCore.location("blocks/flesh"))) return;
        var innards = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse("brewinandchewin:innards"));
        if (innards.isEmpty()) return;
        var pool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .when(ExplosionCondition.survivesExplosion())
                .add(LootItem.lootTableItem(innards.get()).setWeight(living ? 20 : 10))
                .add(EmptyLootItem.emptyItem().setWeight(living ? 80 : 50));
        if (!living) pool.add(LootItem.lootTableItem(Items.ROTTEN_FLESH).setWeight(40));
        event.setTable(LootTable.lootTable()
                .setParamSet(LootContextParamSets.BLOCK)
                .withPool(pool)
                .build());
    }
}
