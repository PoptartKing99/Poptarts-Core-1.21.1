# Poptart's Core

Poptart's Core is a NeoForge mod for Minecraft 1.21.1. It adds machines,
materials, equipment, creatures, and integration with the other mods in
PoptartKing's modpack.

## Build and run

Use Java 21 and the included Gradle wrapper. From the project root:

```powershell
.\gradlew.bat build
.\gradlew.bat runClient
```

The build reads several development dependency JARs from `run/mods/`. Check
`build.gradle` for the exact file names before building in a new checkout.
The finished mod JAR is written to `build/libs/`.

For the runtime dependency list and supported version ranges, see
`src/main/templates/META-INF/neoforge.mods.toml`.

## Project layout

- `src/main/java/dev/poptartking/poptartcore/` contains the mod code. Gameplay
  systems have their own packages, such as `barrel`, `crucible`, and `quern`.
- `registry/` registers blocks, items, fluids, recipes, and other mod content.
- `integration/` and `mixin/integration/` contain code for other mods.
- `src/main/resources/` contains hand-authored models, textures, recipes,
  tags, and mixin configuration.
- `src/generated/resources/` contains committed data-generator output. Edit
  the generator or source assets instead of changing generated files by hand.
- `scripts/` contains feature-specific verification and asset tools.

The encased cogwheel models deliberately use top-level models for textures and
`normal` and `large` subfolders for geometry. This lets each casing choose its
own textures without changing Create's models.

## Tests and checks

Run the unit tests and build with:

```powershell
.\gradlew.bat test
.\gradlew.bat build
```

GameTests in `src/gameTest/` are opt-in. The `gameTests` property adds that
source set to the test server without packaging it in the released mod JAR:

```powershell
.\gradlew.bat runGameTestServer -PgameTests
```

The test server uses `build/game-test-run/`. It needs the development mods
required by the tests in `build/game-test-run/mods/`. Feature scripts, such as
`scripts/millstone/verify-millstone.ps1` and
`scripts/wax_golem/verify_wax_golem.ps1`, prepare those dependencies when run
with `-RunServer`. The GameTest run executes all registered GameTests, not just
the tests for the feature named by the script.

To check the Java package and resource-folder layout without starting the game:

```powershell
pwsh -File scripts/verify-project-layout.ps1
```
