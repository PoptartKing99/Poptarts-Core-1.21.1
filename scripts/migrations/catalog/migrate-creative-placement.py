"""Move captured creative placements onto declarations; --check verifies without editing.

The fixture records the real tab before the cleanup, not a second runtime ordering table.
Ranks leave gaps so future entries can be inserted without renumbering their neighbours.
"""

import argparse
from collections import Counter
import json
from pathlib import Path
import re


ROOT = Path(__file__).resolve().parents[3]
FIXTURE = ROOT / "src/gameTest/resources/data/poptartcore/catalog/creative-order-before-cleanup.json"
REGISTRY = ROOT / "src/main/java/dev/poptartking/poptartcore/registry"


def mask_java(source):
    pattern = r'//[^\n]*|/\*[\s\S]*?\*/|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\''
    return re.sub(pattern, lambda match: re.sub(r"[^\r\n]", " ", match[0]), source)


def initializer_end(masked, start):
    depth = 0
    for index in range(start, len(masked)):
        char = masked[index]
        if char in "({[":
            depth += 1
        elif char in ")}]":
            depth -= 1
        elif char == ";" and depth == 0:
            return index
    raise ValueError("Unterminated catalog declaration")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", help="Verify existing placements without writing files")
    args = parser.parse_args()
    entries = json.loads(FIXTURE.read_text(encoding="utf-8-sig"))
    counts = Counter()
    placements = {}
    for entry in entries:
        counts[entry["group"]] += 10
        if entry["id"] in placements:
            raise ValueError(f"Duplicate captured entry: {entry['id']}")
        placements[entry["id"]] = (entry["group"], counts[entry["group"]])

    seen = set()
    updates = {}
    declaration = re.compile(r"public\s+static\s+final\s+Catalog(?:Block|Item)Definition<[^;=]+>\s+(\w+)\s*=")
    for path in sorted(REGISTRY.glob("*.java")):
        source = path.read_text(encoding="utf-8")
        masked = mask_java(source)
        insertions = []
        groups = set()
        for match in declaration.finditer(masked):
            item_id = match[1].lower()
            if item_id not in placements:
                continue
            if item_id in seen:
                raise ValueError(f"Duplicate catalog declaration: {item_id}")
            seen.add(item_id)
            group, rank = placements[item_id]
            end = initializer_end(masked, match.end())
            expression = source[match.end():end]
            expected = f".creative({group}, {rank})"
            if re.search(r"\.creative\s*\(", expression):
                if expected not in expression:
                    raise ValueError(f"Unexpected placement for {item_id} in {path}")
            elif args.check:
                raise ValueError(f"Missing {expected} for {item_id} in {path}")
            else:
                insertions.append((end, expected))
                groups.add(group)
        if insertions:
            for index, text in reversed(insertions):
                source = source[:index] + text + source[index:]
            imports = "".join(
                f"import static dev.poptartking.poptartcore.catalog.CreativeCatalogGroup.{group};\n"
                for group in sorted(groups)
                if f"import static dev.poptartking.poptartcore.catalog.CreativeCatalogGroup.{group};" not in source
            )
            source = re.sub(r"(package [^;]+;\n)", lambda match: match[0] + "\n" + imports, source, count=1)
            updates[path] = source

    missing = placements.keys() - seen
    if missing:
        raise ValueError(f"No catalog declarations found for: {sorted(missing)}")
    for path, source in updates.items():
        path.write_text(source, encoding="utf-8", newline="\n")
    print(f"Verified {len(seen)} captured creative placements; updated {len(updates)} registry files.")


if __name__ == "__main__":
    main()
