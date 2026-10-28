# Catalog definitions

## Where block definitions live

Block definitions live in focused files under `src/main/java/dev/poptartking/poptartcore/registry`, alongside the existing item groups:

| File | Contents |
| --- | --- |
| `PoptartBuildingBlocks.java` | Plated bricks, concrete, clinker, and rubber |
| `PoptartMaterialBlocks.java` | Ores, metal and ingredient storage blocks, and ingot piles |
| `PoptartWorkstationBlocks.java` | Processing stations, anvils, fluid barrels, and the tree tapper |
| `PoptartEquipmentBlocks.java` | Backpacks and rucksacks |
| `PoptartBeekeepingBlocks.java` | Hive supports and colored roofs |
| `PoptartFleshBlocks.java` | Flesh, tendrils, ulcers, and lifebuds |
| `PoptartRiftBlocks.java` | Rift sediment, fire, torches, sconces, and braziers |
| `PoptartCreateBlocks.java` | Casings, encased parts, and the portable engine |
| `PoptartSpiderBlocks.java` | Temporary spider webs |

Add a block to its owning group using `PoptartCatalog.block(...)`, with its item factory, creative position, mining tags, models, loot, and generated recipes on the same declaration. Callers reference that group directly, for example `PoptartWorkstationBlocks.CRUCIBLE`. There is no second list of block constants to maintain.

`PoptartCoreBlocks` only coordinates startup. Its `initialize()` loads every group before `PoptartCatalog.register()` freezes the definitions. Each group that has internal blocks without normal block items owns a private deferred register; the coordinator connects those registers to the event bus once. Catalog blocks continue to use the catalog's shared block and item registers. The `fresh_flesh` migration alias lives with the flesh definitions.

Adding a definition to an existing group needs no startup changes. A new group needs an `initialize()` call in the coordinator, plus a `register(eventBus)` call only if it owns a separate register for internal blocks. Keep related property helpers in the same group and defer references to other registered objects until the factory runs.

## Creative-tab placement

Set an item's category and position beside its catalog declaration. Blocks use the same methods as items:

```java
PoptartCatalog.simpleItem("example_material")
        .creative(CreativeCatalogGroup.MATERIALS, 25);
```

The single tab displays Blocks, Tools, Equipment, then Materials. Lower positions appear first within each category. Existing positions have gaps of ten, so an entry can be inserted between 20 and 30 without renumbering them.

Use `.creative(CreativeCatalogGroup.TOOLS)` to append an entry to a category. Equal positions retain catalog registration order, with standalone items before block items. An entry without an explicit setting uses its item type: armor, rings and backpacks go in Equipment; tiered tools and projectile weapons go in Tools; blocks go in Blocks; other items go in Materials. Workstations and other exceptions should declare their category explicitly.

`.hideFromCreativeTab()` still hides an entry regardless of its placement. Creative settings freeze with the rest of the definition during registration. There is no separate list of IDs to update.

`CatalogCreativeOrderGameTests` checks the actual tab against the sequence captured before this cleanup. It allows new entries while protecting the order and categories of existing entries. The historical fixture is only a test resource and does not control the runtime tab. `scripts/migrations/catalog/migrate-creative-placement.py --check` can verify the original migration.

## Block and item tags

On a block definition, `.tags(...)` configures the placed block's tags, while `.itemTags(...)` configures the inventory item's tags. Their `TagKey<Block>` and `TagKey<Item>` types keep the two registries separate.

For example, the backpack and rucksack definitions both use:

```java
.itemTags(PoptartCoreTags.BACKPACKS)
```

The item-tag generator writes both registered block items into `poptartcore:backpacks`. The handwritten `poptartcore:equipment/back` compatibility tag includes `#poptartcore:backpacks` alongside the existing other-mod items. This preserves back-slot compatibility while keeping ownership of the catalog entries on their definitions.

Tag additions are duplicate-free, and returned tag sets are immutable snapshots. Settings freeze during registration. Generate the resources after changing a declaration, then run `scripts/verify-mod.ps1`; server and packaged-resource checks verify the declared block-item memberships.

A tag resource path should have one owner. If a handwritten compatibility tag already exists, reference a generated child tag from it instead of generating another file at the same path.
