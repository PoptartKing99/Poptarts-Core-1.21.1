# Timekeeping verification

Run server coverage and inspect the distributable:

```powershell
.\gradlew.bat build runGameTestServer -PgameTests -I scripts/verification/compatibility/nomansland-gametest.gradle --offline --no-configuration-cache --console=plain
python scripts/verification/timekeeping/verify-timekeeping.py
```

The clock controls use the isolated navigation client-test instance prepared by `scripts/verification/navigation/verify-controls.ps1 -RunClient`:

```powershell
.\gradlew.bat runClient -PnavigationTests -PtimekeepingTests --offline --no-configuration-cache --console=plain
.\gradlew.bat prepareClientRun --offline --no-configuration-cache --console=plain
```

Look for `TIMEKEEPING_CONTROLS_PASS` in `build/navigation-test-run/logs/latest.log`. The test checks actual right-click, G on both bags, Z in the utility slot, timer synchronization, tooltips, and preserved hand items. HUD screenshots are written into that instance's screenshots folder. Server tests cover start and stop, the automatic limit, serialization, and precise hover text in the final chat result.
