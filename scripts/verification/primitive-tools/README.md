# Primitive tools and repeating crossbow verification

Run `./scripts/verify-mod.ps1` for the full server and packaged-resource checks. The old primitive-tool source-text script has been removed.

`PrimitiveToolGameTests` checks bone and flint tool stats, mining level, repair materials, and effective combat attributes. It uses the registered items to till dirt, reject blocked tilling, strip logs, create paths, and light campfires. These actions check their real durability costs; the firestarter must break after three successful uses and spend no durability on a blocked attempt.

`RepeatingCrossbowGameTests` checks the 15-tick charge, early release, arrow and firework consumption, automatic completion of held use, redstone-circuit repair, worn-out rejection, and the existing comparison with normal crossbow arrow spread.

The shared recipe tests use `src/gameTest/resources/data/poptartcore/catalog/item-behavior.json`. It records the ten metal-tool recipes and six primitive-tool/component recipes independently of their production files. Both server tests and the packaged-JAR check use those contracts. The catalog checks also validate the tool tags, model references, crossbow override predicates, textures, and JSON syntax. See [catalog verification](../catalog/README.md) for details.

## Client properties and models

Compile the isolated client test without opening Minecraft:

```powershell
./scripts/verification/catalog/verify-client.ps1
```

Run it after a successful server verification:

```powershell
./scripts/verification/catalog/verify-client.ps1 -RunClient
```

The launcher selects only `CatalogClientSmoke` and `BeekeeperClientChecks` from the existing opt-in client source set. It validates and copies the locked test dependencies and starts a copied GameTest world under `build/catalog-client-run`. It leaves the normal game instance alone and restores its launch arguments afterward. An unexpected dependency in the isolated client's mod folder is an error.

The test calls the actual registered `pull`, `pulling`, `charged`, and `firework` functions, then asks Minecraft to resolve the item model for idle, charging, arrow, and firework states. Crossbow checks use a detached actor and inspect the resolved sprites. The shared suite also checks the baked beekeeper model and actual sleeve rendering on both hands, temporarily equipping a tunic and restoring the original chest item, then resolves the workbench screen factory. The client closes itself and writes `CATALOG_CLIENT_PASS` or `CATALOG_CLIENT_FAIL` to its log. The test classes are excluded from the published mod.

This is an opt-in client assertion test, not a screenshot comparison or a real multiplayer test. The normal verification pipeline does not launch a client.
