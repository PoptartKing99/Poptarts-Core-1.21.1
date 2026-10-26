"""Check the release after building: python scripts/spyglass/verify-zoom.py"""
from pathlib import Path
import json,zipfile
root=Path(__file__).resolve().parents[2]
config=json.loads((root/"src/main/resources/poptartcore.mixins.json").read_text())
classes=["SpyglassFovMixin","SpyglassMinimumFovMixin","SpyglassCrosshairMixin","SpyglassScrollMixin"]
for name in classes:assert "spyglass."+name in config["client"],name
jar=max((p for p in (root/"build/libs").glob("*.jar") if "sources" not in p.name and "javadoc" not in p.name),key=lambda p:p.stat().st_mtime)
with zipfile.ZipFile(jar) as archive:
    names=set(archive.namelist())
    for name in classes:assert "dev/poptartking/poptartcore/mixin/spyglass/"+name+".class" in names,name
    assert "dev/poptartking/poptartcore/spyglass/client/SpyglassZoom.class" in names
    assert not any("SpyglassZoomSmoke" in name for name in names),"Client test leaked into release"
print("Verified four client zoom mixins, zoom controller, and excluded client test.")
