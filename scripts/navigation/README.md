# Navigation checks

Build and run the server regressions:

```powershell
.\gradlew.bat build runGameTestServer -PgameTests -I scripts/compatibility/nomansland-gametest.gradle --offline --no-configuration-cache --console=plain
python scripts/navigation/verify-navigation.py
```

Client checks after restarting:

- Place a lodestone, enter a name, and choose an icon. Reopen it with an empty hand to edit it.
- Naming a lodestone saves it to your inventory, offhand, utility-slot, and worn backpack compasses without selecting it. A compass used directly on the lodestone still selects it. World Origin and your respawn point remain available. Each compass remembers up to 12 lodestones.
- Tap use to toggle the navigation bar. Hold use and turn toward a marker, then release to select it.
- While holding use, left-click to hide or restore the highlighted marker.
- Put the compass in the primary backpack attachment slot and repeat with G. Repeat in the utility slot with Z.
- Install a compass in another attachment slot and verify its navigation bar appears while wearing the bag.
- Restart the world and verify names, selected targets, hidden markers, and navigation visibility remain saved.
- Rename or break a bound lodestone and verify the compass updates. Change dimensions and verify only markers in the current dimension appear.
- With Sable, assemble and move a named lodestone, then disassemble it. Verify tracking and the name survive both moves, including a world restart.

Server tests cover binding through the vanilla compass, server validation, save codecs, selection, hiding, target limits, mounted use, and relocation state. Client startup does not establish that the visuals or moving-structure gameplay are correct.

Run the live controls regression with Caverns & Chasms installed:

```powershell
& scripts/navigation/verify-controls.ps1 -RunClient
```

This uses an isolated copy of the server test world. It checks tapping, holding, hiding, restoring, and release-to-select with the held compass and the real G handler on both bags. The client test source set is excluded from release JARs.

Corpse marker lifecycle regression, in the same disposable instance:

```powershell
.\gradlew.bat runClient -PnavigationTests -PcorpseMarkerTests --offline --no-configuration-cache --console=plain
.\gradlew.bat prepareClientRun --offline --no-configuration-cache --console=plain
```

Look for `CORPSE_MARKER_CONTROLS_PASS`. This verifies server-to-client clearing after corpse removal and restoration for the next corpse, retaining vanilla death history. Removal IDs are saved independently of loaded chunks; deaths destroyed before this tracking was installed cannot be inferred retroactively.

For a marker left behind by a corpse destroyed before this fix, `/cleardeathmarker` dismisses only the caller's current corpse marker. It needs no operator permission, preserves vanilla death history, and a new death restores the marker.

The corpse client regression also evaluates Minecraft's actual recovery-compass angle property: the needle tracks before removal, switches to vanilla random spinning afterward, and tracks again for the next corpse.

## Simulated navigation table

Select a destination on the compass, then insert that compass into Simulated's navigation table. The table resolves the selected waypoint through the same dimension and moving-lodestone lookup as our compass. Compasses without waypoint data retain Simulated's existing target behavior. A selected waypoint in another dimension returns no destination.

Server regressions exercise an actual placed table, selection changes, the fallback path, cross-dimension handling, and relocated lodestones. Actual vehicle travel is not covered by these tests.
