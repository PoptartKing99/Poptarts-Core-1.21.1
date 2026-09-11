# Millstone verification

The millstone uses the Wayfarer implementation in `intellij_wayfarer/decompiled`
as its reference. It has a 3 by 3 footprint and occupies two block layers.
The bottom-center block owns both inventories and processing. The upper-center
rotor connects to a Create shaft from above. Structural blocks forward inventory
access to the controller. Only the lower layer accepts item transfers.

Its separate `poptartcore:milling` recipe type leaves quern recipes unchanged.
Currently one wheat produces one `create:wheat_flour`, without bonus output.
Duration 20 uses Wayfarer's speed calculation, so processing takes 80 game ticks
at 64 RPM, or 160 ticks at 32 RPM. Speeds above 64 RPM stop processing.
Each buffer holds 64 items total across up to nine stacks.

No millstone crafting recipe or EMI integration is included yet.
Get the block from the creative tab or `/give @s poptartcore:millstone`.

## Automated checks

From the project directory, run:

```powershell
pwsh -File scripts/millstone/verify-millstone.ps1
pwsh -File scripts/millstone/verify-millstone.ps1 -RunServer
.\gradlew.bat build
```

The optional server tests copy the installed Create, Supplementaries and Moonlight
dependencies to `build/millstone-test-run/mods`. They use a separate test world.
Test classes in `src/gameTest` are enabled only by `-PmillstoneTests` and are not
included in the released mod JAR.

The tests cover structure formation, inventory limits, rejected ingredients,
simulation, speed limits, exact yield/timing, saved inventories, teardown drops,
stale inventory adapters, and grinding through a real Create motor connection.

## In-game checks still needed

1. Place the millstone in a clear 3 by 3 area. Try an obstructed location too.
2. Connect a shaft above the center and power it at 32 or 64 RPM.
3. Insert wheat by right-clicking the base. Empty-hand right-click extracts flour.
4. Check rotation, lighting, sounds and particles. Stop the drive, then try 128 RPM.
5. Test hopper and Create funnel insertion/extraction on the lower blocks.
6. Break different parts in survival and creative. Check for leftover pieces or extra drops.
7. Save/reload while grinding, and test two players interacting with the same millstone.
8. If using Create contraptions, test assembly, movement and disassembly separately.

## Port safety adjustments

The port checks placement bounds and loaded chunks, blocks stale inventory
adapters after removal, resets progress when the working ingredient changes,
validates recipe values, splits oversized result stacks, and cleans sound tracking
when changing worlds. These preserve the intended processing behavior without
copying those reference-code edge cases.
