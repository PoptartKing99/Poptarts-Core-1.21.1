"""Run after building: python scripts/timekeeping/verify-timekeeping.py"""
from pathlib import Path
import json,zipfile
root=Path(__file__).resolve().parents[2]
resources=root/"src/main/resources"
lang=json.loads((resources/"assets/poptartcore/lang/en_us.json").read_text())
for key in ["clock.time","clock.readout","stopwatch.running","stopwatch.total","readout.combined"]:
    assert "poptartcore."+key in lang,key
sounds=json.loads((resources/"assets/poptartcore/sounds.json").read_text())
for name in ["stopwatch_start","stopwatch_stop","stopwatch_tick"]: assert sounds[name]["sounds"],name
jar=max((p for p in (root/"build/libs").glob("*.jar") if "sources" not in p.name and "javadoc" not in p.name),key=lambda p:p.stat().st_mtime)
with zipfile.ZipFile(jar) as archive:
    names=set(archive.namelist())
    assert not any("TimekeepingControlsSmoke" in name or "NavigationControlsSmoke" in name for name in names),"Client tests leaked into release"
    for path in ["dev/poptartking/poptartcore/timekeeping/Stopwatch.class","dev/poptartking/poptartcore/timekeeping/client/ClockHud.class","dev/poptartking/poptartcore/mixin/timekeeping/ClockUseMixin.class","dev/poptartking/poptartcore/mixin/timekeeping/SupplementariesClockUseMixin.class"]:assert path in names,path
    for icon in ["sun","moon"]:
        entry=f"assets/poptartcore/textures/gui/sprites/{icon}.png"
        assert archive.read(entry)==(resources/entry).read_bytes(),entry
print("Verified clock sprites, translations, stopwatch sounds, release classes, and excluded test classes.")
