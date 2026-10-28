# Verification ownership

This pass reviewed all 24 scripts in the curated feature-check list. Eight scripts were retired after their rules moved to runtime tests and shared packaged-resource verification. Four other scripts were narrowed to the specialized rules they still own. Production code, resources, IDs and gameplay are unchanged.

## Retired checks and their replacements

| Area | Current owner |
| --- | --- |
| Beekeeper armor | `BeekeeperArmorGameTests` checks protection, material, actual bee stings and wear, damage reduction and poison. The shared client suite checks the baked model, texture dimensions and both first-person sleeve hooks. Shared resource rules preserve textures, mixins and the absence of recipes. |
| Inventory automation | `AutomationGameTests` queries the registered capabilities, checks crucible side routing, quern insertion/extraction simulation and workbench inventory exposure. |
| Workbench | Runtime tests save and restore all 25 slots, craft through the actual menu, refresh results and consume ingredients. The client suite resolves its registered screen factory. Shared contracts preserve resources and optional reference textures. |
| Hammer item | `HammerItemGameTests` checks durability, repair and tag compatibility, and processes one iron bloom into nine nuggets through the real block callback. |
| Bloomery assets | Shared contracts preserve required classes, models, animation metadata, textures, and the eight bloomery and nine bloom-pile blockstate variants. |
| Backpack attachment assets | Shared contracts check all eight models, the spyglass geometry/head transform, attachment texture namespaces, panel images and preservation of attachment components in loot. |
| Rucksack assets | Shared contracts preserve required resources and all 32 blockstate variants, including grounded lower models and empty upper models. |
| Armor-stand cape packaging | Shared contracts require the implementation and renderer classes and the interaction/drop mixin. Existing armor-stand GameTests cover interaction, persistence and drops. The feature-check entry point now requires a successful server-test log. |

The historical rules live in `src/gameTest/resources/data/poptartcore/catalog/feature-assets.json`. Its JSON comparisons ignore object-key order. Rules that allow extra loot components continue to allow them. These fixtures are test data and are excluded from the published mod.

## Focused checks retained

| Check | Why it remains separate |
| --- | --- |
| Project and script layout | Package paths, directory ownership, script syntax, entry points and references. These are structural checks. |
| Ragdoll armor mappings | Model-part coverage, stale/duplicate mappings and transform overrides. |
| Backpack facing | Geometry direction and all facing/dye rotations. |
| Backpack dye layers | Exact pixel coverage, grayscale masks, tint indices and matching poses. |
| Wayward model geometry | Original geometry and UVs across both bags and their dye variants. |
| Bee behavior | Focused source audits of gravity, dimension/weather hooks, turtle eggs and death rotation. These do not prove the behaviors in a live client. |
| Bee smoker | Focused source audits of range, pump/effect timing, smoke refresh and client-use hooks. Stats and crafting now have runtime tests; resources, sounds and mixin packaging moved to shared checks. |
| Cape assets | Palette generation, masks, colors and packaged texture consistency. |
| Flesh | Model/texture conventions, geode placement, terrain-conversion settings and obsolete-asset checks. Creative-tab source parsing was removed because existing runtime tests cover it. |
| Hammer area mining and persistent damage | Mining workload, protection, replacement/invalidation and lifecycle-hook audits, alongside existing math unit tests. These source audits do not replace multiplayer mining tests. |
| Portable engine | Focused source audits of the Aeronautics integration, lighting, fuel and dye behavior. Shared checks now own recipe, assets, mixins and packaged dependency metadata. |
| Quern | Specialized geometry/UV rules, powder colors, grinding conversions and timing audits. Generic file/class presence and broad JSON parsing moved to shared checks. |
| Ore world generation | Vein sizes, targets, placement modifiers, height ranges and biome routing. |

These checks retain distinct coverage. The goal is one owner for each rule, rather than removing useful checks to reduce a script count.

## Commands

Run `./scripts/verify-mod.ps1` for unit tests, formatting, build, all server GameTests and the curated feature checks. Run `python scripts/verification/catalog/verify-catalog-checker.py` for altered-JAR regression cases. They run in parallel by default; use `--workers 1` for serial execution.

Run `./scripts/verification/catalog/verify-client.ps1 -RunClient` for the isolated client suite. It verifies crossbow properties/models, beekeeper geometry and sleeve rendering, and workbench screen registration, then closes the client and restores normal launch arguments. It uses a copied test world and locked dependencies under `build/catalog-client-run`.

Optional texture comparisons use `-ReferenceRingTextures`, `-ReferenceWayfarerSources` and `-ReferenceWorkbenchSources` on the shared packaged checker. See [catalog verification](../scripts/verification/catalog/README.md) for directory layouts. Server and client assertions remain distinct from screenshot comparisons and a real multiplayer session.
