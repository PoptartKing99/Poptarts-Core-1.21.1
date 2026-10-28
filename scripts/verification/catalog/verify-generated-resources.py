"""Compare a fresh isolated runData output with its checked-in owners, without editing files."""
import argparse
import json
from pathlib import Path


def normalized(value):
    if isinstance(value, dict):
        return {
            key: sorted(child)
            if key == "values" and isinstance(child, list) and all(isinstance(item, str) for item in child)
            else normalized(child)
            for key, child in value.items()
        }
    if isinstance(value, list):
        return [normalized(child) for child in value]
    return value


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("generated_directory", type=Path, help="Output from runData -PdatagenOutput=...")
    args = parser.parse_args()
    repo = Path(__file__).resolve().parents[3]
    actual = args.generated_directory.resolve()
    checked = repo / "src/generated/resources"
    handwritten = repo / "src/main/resources"
    errors = []
    expected = {path.relative_to(checked) for path in checked.rglob("*.json") if ".cache" not in path.parts}
    produced = {path.relative_to(actual) for path in actual.rglob("*.json") if ".cache" not in path.parts}
    if not expected or not produced:
        raise SystemExit("Run isolated data generation first; both output sets must be non-empty.")
    for relative in sorted(expected | produced):
        if relative not in expected:
            errors.append(f"Untracked generated output: {relative}")
        elif relative not in produced:
            errors.append(f"Handwritten or stale file stored as generated: {relative}")
        elif normalized(json.loads((checked / relative).read_text(encoding="utf-8"))) != normalized(
            json.loads((actual / relative).read_text(encoding="utf-8"))
        ):
            errors.append(f"Generated content differs: {relative}")
        if (handwritten / relative).is_file():
            errors.append(f"Resource has two owners: {relative}")
    manual_names = json.loads((handwritten / "assets/poptartcore/lang/en_us.json").read_text(encoding="utf-8"))
    names = json.loads((actual / "assets/poptart_catalog/lang/en_us.json").read_text(encoding="utf-8"))
    for key in sorted(manual_names.keys() & names.keys()):
        errors.append(f"English name has two owners: {key}")
    if errors:
        raise SystemExit("\n".join(errors))
    print(f"All {len(expected)} generated JSON resources reproduce their checked-in owners; English keys are disjoint.")


if __name__ == "__main__":
    main()
