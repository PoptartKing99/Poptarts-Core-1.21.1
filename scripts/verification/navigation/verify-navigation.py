"""Run from anywhere after building: python scripts/verification/navigation/verify-navigation.py"""
from pathlib import Path
import json
import re
import zipfile
root=Path(__file__).resolve().parents[3]
java=root/"src/main/java/dev/poptartking/poptartcore"
resources=root/"src/main/resources"
config=json.loads((resources/"poptartcore.mixins.json").read_text())
classes=[]
for side in ("mixins","client"):
    for name in config[side]:
        if name.startswith("navigation."):
            path=java/"mixin"/Path(name.replace(".","/")).with_suffix(".java")
            source=path.read_text()
            expected="dev.poptartking.poptartcore.mixin."+name.rsplit(".",1)[0]
            assert f"package {expected};" in source,path
            classes.append("dev/poptartking/poptartcore/mixin/"+name.replace(".","/")+".class")
for path in (java/"navigation").rglob("*.java"):
    text=path.read_text()
    assert "dev.tazer" not in text and "WaywardNetwork" not in text,path
assert "integration.simulated.CompassNavigationTargetMixin" in config["mixins"]
classes.append("dev/poptartking/poptartcore/mixin/integration/simulated/CompassNavigationTargetMixin.class")
sprites=resources/"assets/poptartcore/textures/gui/sprites/waypoints"
files=sorted(sprites.glob("*.png"))
assert files,"Missing waypoint icons"
for path in files:
    if not path.stem.endswith("_selected"):
        assert path.with_name(path.stem+"_selected.png").is_file(),path
lang=json.loads((resources/"assets/poptartcore/lang/en_us.json").read_text())
for key in ("lodestone.title","lodestone.name","lodestone.unnamed","compass.full","waypoint.origin","waypoint.respawn","waypoint.lodestone"):
    assert "poptartcore."+key in lang,key
jar=max((p for p in (root/"build/libs").glob("*.jar") if "sources" not in p.name and "javadoc" not in p.name),key=lambda p:p.stat().st_mtime)
with zipfile.ZipFile(jar) as archive:
    for entry in classes:assert entry in archive.namelist(),entry
    for path in files:
        entry=path.relative_to(resources).as_posix()
        assert archive.read(entry)==path.read_bytes(),entry
print(f"Verified {len(classes)} navigation mixins and {len(files)} waypoint sprites, translations, and packaged files.")
