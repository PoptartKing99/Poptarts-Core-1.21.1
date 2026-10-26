"""Verify depth meter asset references, source fidelity, and packaged resources.
Run from the repository root after building: python scripts/backpack/verify-depth-meter.py
"""
from pathlib import Path
import json
import zipfile

root = Path(__file__).resolve().parents[2]
assets = root / "src/main/resources/assets/poptartcore"
models = [assets / "models/item/depth_meter.json"]
models += sorted((assets / "models/item/depth_meter").glob("*.json"))
models += sorted((assets / "models/item/backpack_attachments").glob("depth_meter*.json"))
assert len(models) == 26, f"Expected 26 meter models, found {len(models)}"
textures = set()
for path in models:
    model = json.loads(path.read_text())
    assert "wayward:" not in path.read_text(), f"Unconverted namespace in {path}"
    for texture in model.get("textures", {}).values():
        if texture.startswith("poptartcore:"):
            image = assets / "textures" / (texture.split(":", 1)[1] + ".png")
            assert image.is_file(), f"Missing {image}"
            textures.add(image)
    for override in model.get("overrides", []):
        child = assets / "models" / (override["model"].split(":", 1)[1] + ".json")
        assert child.is_file(), f"Missing override {child}"
jar = max((p for p in (root / "build/libs").glob("*.jar") if "sources" not in p.name and "javadoc" not in p.name), key=lambda p: p.stat().st_mtime)
with zipfile.ZipFile(jar) as archive:
    for path in [*models, *textures]:
        entry = path.relative_to(root / "src/main/resources").as_posix()
        assert archive.read(entry) == path.read_bytes(), f"Stale packaged asset {entry}"
print(f"Verified {len(models)} models, {len(textures)} textures, all references, and packaged bytes.")
