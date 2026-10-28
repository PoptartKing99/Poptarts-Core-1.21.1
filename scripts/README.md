# Development scripts

Use PowerShell 7 and run these commands from the project folder. Java builds use Java 21 and the checked-in Gradle wrapper.

| Task | Command | Effect |
| --- | --- | --- |
| Set up development dependencies | `./scripts/setup-dev-mods.ps1 -SourceDirectory 'C:/path/to/mods'` | Copies missing locked development binaries after checking filenames and hashes. |
| Verify the mod | `./scripts/verify-mod.ps1` | Builds, checks formatting, runs unit and server tests, checks packaged resources, and runs the curated feature checks. |
| Run the curated feature checks | `./scripts/verify-feature-checks.ps1` | Runs the read-only checks listed in `verification/feature-checks.json` against current sources and the existing built JAR. |
| Compare generated resources | `python scripts/verification/catalog/verify-generated-resources.py build/catalog-generated-check` | Compares an isolated data-generation run with the checked-in resources without editing them. |

Generate the isolated resource output with `./gradlew.bat runData -PdatagenOutput=build/catalog-generated-check`. See [development.md](../docs/development.md) for setup, build requirements, and test limitations.

## Folders

| Folder | Purpose |
| --- | --- |
| `verification/<feature>/` | Feature checks, optional smoke-test launchers, and their Gradle/client fixtures. |
| `verification/` | Shared checks and the curated feature-check list. |
| `assets/<feature>/` | Asset generators, importers, texture tools, and their input files. These can write mod assets or generated Java sources. |
| `migrations/<feature>/` | Tools for applying or checking a specific source/resource migration. |
| `lib/` | Helpers used by other scripts, such as resource lookup and built-JAR selection. |

Names use lowercase words separated by hyphens. Keep a tool's input images, JSON, and palettes beside that tool. Put a new script in its existing feature folder when possible. Shared checks belong directly in `verification/`.

## Choosing a check

`verify-mod.ps1` is the complete automated check. `verify-feature-checks.ps1` runs only the curated read-only checks, records every result in `build/verification/feature-checks.json`, and reports all failures together. It does not launch a client or run asset tools in write mode. Run it after building so packaged-resource checks inspect the current JAR.

The catalog check uses the built JAR and a fresh manifest exported by the server tests. After a rebuild, run `verify-mod.ps1` to refresh that manifest. See [catalog verification](verification/catalog/README.md) for its coverage and the optional altered-JAR tests for the checker itself.

The remaining feature scripts may build the mod, select a smaller test suite, or launch an isolated client when requested. Read their parameters and feature README before using switches such as `-Build`, `-RunServer`, or `-RunClient`. The equipment smoke launcher also applies Java formatting, as it did before this reorganization. Server checks do not confirm client visuals, audio, or real multiplayer behavior.

## Asset tools and migrations

These tools are explicit authoring operations. Imports and generators can overwrite existing models or textures, including manually edited artwork. Read the parameters and output paths before running one. Moving a tool does not mean its historical import is needed again.

Examples include `assets/wooden-bucket/generate-wooden-bucket.py`, `assets/backpack/build-dye-models.ps1`, and `assets/cape/build-cape-colors.ps1`. Cape generation supports `-VerifyOnly`; the regular cape check calls that read-only mode. Other tools have their own parameters and should not be assumed to have a preview mode.

`python scripts/migrations/catalog/migrate-creative-placement.py --check` verifies the historical creative-order migration without editing declarations. The beehive-model migration reorganizes existing resource paths and updates their references.

`verification/verify-script-layout.ps1` checks folder and naming rules, PowerShell syntax, the feature-check list, and concrete script references in source and documentation. The full verification command runs it automatically.
