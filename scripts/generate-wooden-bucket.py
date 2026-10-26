"""Regenerate the decorative bucket model from the supplied 32x32 atlas."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/poptartcore"

def write(path, value):
    target = ASSETS / path
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(json.dumps(value, indent=2) + "\n")

# The centered Blockbench model is the source of truth. Never overwrite it.
# Derive the log-flush version by translating all geometry three pixels north.
from copy import deepcopy
model = json.loads((ASSETS / "models/block/wooden_bucket.json").read_text())
flush = deepcopy(model)
for element in flush["elements"]:
    for corner in ("from", "to"):
        element[corner][2] -= 3
    if "rotation" in element:
        element["rotation"]["origin"][2] -= 3
write("models/block/wooden_bucket_flush.json", flush)
# No Man's Land tap geometry, rotated to mount against the north log.
# Keep its original texture reference and artist credit.
def tap_model(source):
    tap = json.loads((ROOT / "scripts/wooden-bucket" / source).read_text())
    upper = deepcopy(tap)
    upper["parent"] = "minecraft:block/block"
    upper.pop("display", None)
    swap = {"north": "south", "south": "north", "east": "west", "west": "east", "up": "up", "down": "down"}
    for element in upper["elements"]:
        low, high = element["from"], element["to"]
        element["from"] = [16-high[0], low[1]-3, 16-high[2]]
        element["to"] = [16-low[0], high[1]-3, 16-low[2]]
        if "rotation" in element:
            origin = element["rotation"]["origin"]
            element["rotation"]["origin"] = [16-origin[0], origin[1]-3, 16-origin[2]]
        element["faces"] = {swap[face]: data for face, data in element["faces"].items()}
        for face in ("up", "down"):
            if face in element["faces"]:
                data = element["faces"][face]
                data["rotation"] = (data.get("rotation", 0)+180)%360
    return upper

upper = tap_model("nomansland-tap.json")
closed = tap_model("nomansland-tap-closed.json")
write("models/block/wooden_bucket_tap.json", upper)
write("models/block/wooden_bucket_tap_closed.json", closed)
variants = {}
for direction, rotation in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
    for half, model_name in (("lower", "wooden_bucket_flush"), ("upper", "wooden_bucket_tap")):
        for opened in (False, True):
            current = model_name + ("_closed" if half == "upper" and not opened else "")
            variants[f"half={half},facing={direction},open={str(opened).lower()}"] = {"model": f"poptartcore:block/{current}", "y": rotation}
write("blockstates/wooden_bucket.json", {"variants": variants})
item = deepcopy(flush)
item["credit"] = "Bucket: Poptarts Core; tap: Farcr / No Man's Land"
item["render_type"] = "minecraft:cutout"
item["textures"].update(closed["textures"])
item["textures"]["particle"] = "poptartcore:block/wooden_bucket"
for element in deepcopy(closed["elements"]):
    for corner in ("from", "to"):
        element[corner][1] += 16
    if "rotation" in element:
        element["rotation"]["origin"][1] += 16
    item["elements"].append(element)
item["display"] = {"gui": {"rotation": [30,225,0], "translation": [0,-2,0], "scale": [.5,.5,.5]}}
write("models/item/wooden_bucket.json", item)

# Targeted catalog output, equivalent to the standard providers. Full datagen
# currently fails because Caverns & Chasms requires its unpublished Gallery API.
generated = ROOT / "src/generated/resources"
def generated_json(path, value):
    target = generated / path
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(json.dumps(value, indent=2) + "\n")

generated_json("data/poptartcore/loot_table/blocks/wooden_bucket.json", {
    "type": "minecraft:block", "pools": [{"rolls": 1,
    "entries": [{"type": "minecraft:item", "name": "poptartcore:wooden_bucket"}],
    "conditions": [{"condition": "minecraft:survives_explosion"}, {"condition": "minecraft:block_state_property", "block": "poptartcore:wooden_bucket", "properties": {"half": "lower"}}]}]})
lang_path = "assets/poptart_catalog/lang/en_us.json"
lang = json.loads((generated / lang_path).read_text())
lang["block.poptartcore.wooden_bucket"] = "Tree Tapper"
generated_json(lang_path, lang)
tag_path = "data/minecraft/tags/block/mineable/axe.json"
tag = json.loads((generated / tag_path).read_text())
if "poptartcore:wooden_bucket" not in tag["values"]:
    tag["values"].append("poptartcore:wooden_bucket")
generated_json(tag_path, tag)
