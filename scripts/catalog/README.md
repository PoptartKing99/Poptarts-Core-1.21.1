# Catalog resource checks

After changing catalog entries or their assets, build the mod and inspect what the JAR actually contains:

```powershell
.\gradlew.bat jar
pwsh -NoProfile -File scripts/catalog/verify-packaged-catalog.ps1
```

The catalog scripts require PowerShell 7. This verifier checks catalog item and block models, local model and texture references, block loot declarations, and declared mining tags. It reads the built JAR, so compiling Java alone is not enough. A custom loot table must use `.customLoot()` and exist in the packaged resources. Blocks intentionally dropping nothing use `.noLoot()`.

The older `verify_catalog_blocks.ps1` and `verify_catalog_items.ps1` cover additional catalog-specific expectations; keep using them when changing generated materials.
