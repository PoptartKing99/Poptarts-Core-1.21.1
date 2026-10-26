# Adjustable spyglass zoom

Scroll up to zoom in and down to zoom out while looking through the spyglass. The four FOV multipliers match Wayward: 0.05, 0.1, 0.2, and 0.4. Scoping starts at vanilla's 0.1. Releasing the spyglass resets the zoom; scrolling normally changes hotbar slots outside the scope.

Build and inspect packaging:

```powershell
.\gradlew.bat build --offline --no-configuration-cache --console=plain
python scripts/spyglass/verify-zoom.py
```

Use the isolated client instance prepared by `scripts/navigation/verify-controls.ps1 -RunClient`:

```powershell
.\gradlew.bat runClient -PnavigationTests -PspyglassTests --offline --no-configuration-cache --console=plain
.\gradlew.bat prepareClientRun --offline --no-configuration-cache --console=plain
```

Look for `SPYGLASS_ZOOM_PASS`. The test invokes the actual mouse scroll handler and checks player FOV, the renderer's lower clamp, all four levels and bounds, hotbar behavior, reset on reuse, and preserved hand items. It covers held use, G on each bag, and Z in the utility slot.
