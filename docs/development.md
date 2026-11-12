# Building and verifying

Use Java 21 and the checked-in Gradle wrapper. Build versions stay in `gradle.properties` and `build.gradle`.

See [the script guide](../scripts/README.md) for the main commands and the distinction between verification, asset tools, and migrations. Feature tools now live under those categories with lowercase names separated by hyphens.

Some integrations use locally supplied binaries, including patched/private APIs. `gradle/development/mods-lock.json` records the exact filenames and SHA-256 hashes tested together. These files are not bundled into the mod or downloaded by setup.

On a new checkout, supply the existing development binaries from your mod directory:

```powershell
./scripts/setup-dev-mods.ps1 -SourceDirectory 'C:/path/to/existing/mods'
```

Setup checks every source and existing destination before copying missing files. It refuses to overwrite a different binary. Compile-only integration files are verified before compilation; the complete locked runtime is verified before GameTests. An intentional dependency upgrade requires a compatibility review and an updated lock.

To keep the binaries elsewhere, use `-PdevModsDir=C:/path/to/mods` with Gradle, or `-DevModsDirectory` with the verification script.

Run all automated Java checks and the full server GameTest suite with:

```powershell
./scripts/verify-mod.ps1
# Equivalent:
./gradlew.bat -PgameTests build
```

Both commands include unit tests, formatting checks, the mod build, and every GameTest package. The script additionally verifies the built JAR's catalog models, textures, loot tables and declared mining tags using metadata exported by the running server, then runs the 16 curated read-only feature checks in `scripts/verification/feature-checks.json`. Their results are saved in `build/verification/feature-checks.json`. Run `scripts/verify-feature-checks.ps1` separately after resource-only changes when the built JAR is current. The server uses `build/game-test-run`, with its mod directory synchronized to exactly the locked set. It does not change the development instance's worlds or mod files. The run exits when tests finish.

Feature checks use `scripts/lib/resources.ps1` to resolve handwritten and generated resources and to merge their English names. Creative-tab membership and order are checked by server GameTests, so feature scripts do not require explicit entries in the tab source. Existing tag memberships are required while additional entries remain allowed. Ring, Wayfarer-addition and workbench checks can optionally compare supplied reference textures using their `-ReferenceTextures` or `-ReferenceSources` parameters; they have no hard-coded decompiled-directory requirement.

`./gradlew.bat build` remains the faster build with unit and formatting checks, without launching a game server. The older `-I scripts/verification/compatibility/nomansland-gametest.gradle` invocation explicitly selects a reduced smoke suite and is not full verification.

Server tests do not replace client visual/audio checks or a real multiplayer test. Those remain separate, including the planned shared-backpack test with two players.

Data generation also uses an isolated `build/data-run` instance. To inspect generated files before updating tracked resources, use `./gradlew.bat runData -PdatagenOutput=build/catalog-generated-check`. It normally writes to `src/generated/resources`. The generator alone includes Gallery 2.0.0, matching [Caverns & Chasms' declared generation dependencies](https://github.com/team-abnormals/caverns-and-chasms/blob/1.21.1/gradle.properties). This dependency is excluded from client, server-test and published-mod runtimes.

After an isolated generation run, compare all generated resources with the checked-in owners using: python scripts/verification/catalog/verify-generated-resources.py build/catalog-generated-check. The check rejects stale output, duplicate file ownership, duplicate English names, and changed generated content. Handwritten names take precedence during generation. Custom loot tables belong in src/main/resources; the tapper and rubber-block tables retain their exact authored contents there.

[Verification ownership](verification-cleanup.md) records the consolidated checks, retained specialized audits and the isolated client command.

## Optional Better Days development settings

The local development run can load `betterdays-1.21.1-3.3.6.3-NEOFORGE.jar` from `run/mods`. Its required WhiteNoise library is bundled inside that JAR. Better Days is optional; neither it nor its settings are included in the PoptartCore JAR.

The local `run/config/betterdays-common.toml` uses:

```toml
[time]
    speedMethod = "MINUTES"
    daySpeedMinutes = 90.0
    nightSpeedMinutes = 30.0
    dayStart = 24000.0
    nightStart = 12000.0

[sleep]
    enableSleepFeature = false
```

The speed boundaries use two equal 12,000-tick halves of the world clock, so the configured minutes give actual 90-minute days and 30-minute nights. Better Days' sleep acceleration is disabled; beds retain vanilla sleeping and skip to morning. No respawn-only bed restriction is added to PoptartCore. These local files are under the ignored `run` directory and must be supplied separately when sharing a modpack. Restart the game after changing the time profile.

Run the isolated compatibility check with:

```powershell
./scripts/verification/eclipse/verify-client.ps1 -RunClient -WithClientRenderMods -WithBetterDays
```
