# Audit follow-up, 27 October 2026

The remaining audit pass covers performance, maintainability, bug fixes and reproducible verification. Existing recipes, balance settings, navigation controls and update intervals are preserved.

## Completed in the unattended pass

- Backpack visual synchronization compares the relevant item-component changes before making a stripped snapshot. Storage edits do not create or synchronize a new visual; dyes, names, attachments, glint and removals still update it.
- Compass refresh returns early when the existing component already represents the current origin, respawn point and named lodestones. It still checks marker existence and metadata at the original interval, and retains the existing normalization of selections and visibility preferences.
- Full server verification includes every GameTest package. The older reduced profile is explicitly labelled as a compatibility smoke suite.
- Catalog tests use the current loot metadata API and retain checks for original content while allowing later additions. A new check verifies all declared block and item tags against the loaded game data.
- Development dependencies have pinned filenames and SHA-256 hashes. Setup validates the full source set before copying; compilation validates local integration binaries; test synchronization uses exactly the locked runtime.
- Synchronization detects extra stale files even when Gradle would otherwise consider the task up to date. This was checked with the configuration cache enabled.
- Packaged-resource verification reads metadata exported by the running catalog and checks model/texture references, loot-table presence and mining tags in the built JAR.
- Isolated data generation works with its own optional Gallery dependency. Regenerating the axe tag restored its 17 omitted entries: beehive support and all 16 roof colors. Existing tag entries were retained.

## Verification

`scripts/verify-mod.ps1` completed the full build and all 225 required server GameTests. Packaged-resource checks passed for 131 catalog blocks, 100 standalone items and 343 referenced models. The command now also runs all 33 curated read-only feature checks, which passed.

Negative checks verified that both setup and Gradle reject a changed dependency binary, and that synchronization removes a stray test mod with a reused configuration cache. Temporary dependency copies remain under the ignored `build/dev-mods-setup-check` directory because automatic approval review blocked their deletion.

## Redundancy cleanup completed

- Removed `PoptartBlockItems`, its initialization call, the obsolete organization checker and its historical registration snapshot. The tab icon reads the catalog item directly.
- Removed the 14 reviewed unused helpers, three pieces of unused stored state and two overrides which only forwarded to their parent. The unregistered leather-tunic variant was removed while retaining the current skirtless geometry.
- Connected nine useful named settings to their existing values in live code. Removed the other 109 unused constant declarations. A comparison of resolved JVM instructions found 4,208 unchanged method bodies. The changed bodies were limited to the reviewed dead-state/model cleanup and data-generation metadata/ownership changes.
- Removed four unused quartz/brazier images. Every remaining image and sound retains its original bytes. Six groups of identical active textures keep their separate paths for independent customization.
- Removed 35 duplicated English names and seven unused strings. All remaining merged English values are unchanged. Language generation respects names owned by the handwritten language file, preventing duplicates from returning.
- Updated all 18 remaining failing feature checks. Checks now use current registration owners, resolve handwritten/generated resources, allow legitimate additions to shared tags and preserve recipe/stat/asset assertions. Actual creative-tab membership and order remain covered by server GameTests.
- Reference-texture comparisons accept explicit optional paths. Four checks were verified with matching and deliberately changed reference fixtures; matching files passed and changed files were rejected.
- Removed the obsolete catalog-generation fallback and isolation shim. The bucket script retains its geometry generation and delegates catalog output to standard datagen.
- Moved the two authored tapper/rubber loot tables into the handwritten resource root without changing their contents. The hoe mining tag now has one generated owner with the same memberships. No resource path overlaps or orphan texture metadata remain.
- A fresh isolated data-generation run reproduces all 476 checked-in generated JSON files. `scripts/verification/catalog/verify-generated-resources.py` checks the output, ownership and language keys. It also rejected a deliberately changed generated name.

The built JAR retains all item IDs, active textures/audio, tag memberships and exact custom loot contents. Its English resource contains the intended string cleanup; the hoe tag has only an ordering change. The source-only catalog loot metadata for rubber now correctly identifies its authored loot table. No new client visual/audio or multiplayer session was run during this cleanup.

## Separate follow-ups

- The planned two-player shared-backpack test with your brother remains a manual gameplay check.
- This pass did not run new manual client visual/audio tests or measure FPS gains.
- A fresh checkout still needs the exact local integration JARs. Setup instructions are in [development.md](development.md); private binaries are not redistributed.
- Missing gameplay features, including standard fluid automation support for barrels, remain outside this audit pass.
