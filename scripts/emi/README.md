# Test EMI workstation support

Use PowerShell 7 from the project folder.

Run `pwsh -File scripts/emi/verify-emi.ps1` to build the mod and check its packaged plugin and translations.

For the isolated client test:

1. Run `pwsh -File scripts/millstone/verify-millstone.ps1 -RunServer` to generate the disposable world and check startup without EMI.
2. Run `pwsh -File scripts/emi/verify-emi.ps1 -RunClient`.
3. Look for `EMI_SMOKE_PASS`. The test client closes itself after checking registration, recipe output lookup, display bounds, and workbench slot mapping.

The client uses `build/emi-test-run`, not your normal `run` folder. Neither test source set is packaged in the mod JAR.

## Check the player-facing behavior

1. Launch your normal client with EMI installed. Enter a world and wait for its recipe index to finish loading.
2. View uses for raw copper. Check the crucible-style melting panel and both workstation icons. Shared recipes appear once, as in Wayfarer. Blast-furnace-only recipes have their own category.
3. View recipes for molten bronze, molten steel, and coal coke. Check ingredient amounts and fluid or item outputs.
4. View recipes for bronze ingots and plates. Check the fluid amount and mould. Hover the mould to see its remainder.
5. View uses for wheat and poppies. Check the input and output slots. Progress arrows and casting bars should not show informational hover panels. Normal item and fluid tooltips should still work.
6. Open the workbench. Use EMI's recipe-fill button for a shaped and a shapeless recipe. Test ingredients in player inventory and workbench storage. Craft once, then shift-craft; verify ingredients and output counts.
7. Test a crafting recipe with container remainders, such as cake. Check that buckets remain and items are not duplicated. Repeat with a second player sharing the workbench.
8. Reload recipes with `/reload`. Wait for EMI to rebuild its index, then repeat a recipe lookup.

Only the workbench has recipe transfer. Other stations provide recipe displays without automated filling. Fuel consumption is not estimated because it depends on batch size and fuel choice.
