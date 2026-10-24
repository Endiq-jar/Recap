#!/usr/bin/env python3
"""Rewrite the Minecraft version range declared by a built Recap Mod jar.

Minecraft mods are compiled against one specific Minecraft version, but patch level releases are
usually binary compatible. This script updates the ``minecraft`` entry in the ``depends`` block of
``fabric.mod.json`` inside an already built jar, so the jar is accepted by that range.

Usage: ``set-mc-range.py <jar> "<version range>" [<jar> ...]``
"""

from __future__ import annotations

import json
import shutil
import sys
import tempfile
import zipfile
from pathlib import Path

MOD_JSON = "fabric.mod.json"


def rewrite(jar: Path, version_range: str) -> None:
    with zipfile.ZipFile(jar) as source:
        if MOD_JSON not in source.namelist():
            raise SystemExit(f"{jar}: does not contain {MOD_JSON}, is this a Fabric mod jar?")
        metadata = json.loads(source.read(MOD_JSON).decode("utf-8"))
        metadata.setdefault("depends", {})["minecraft"] = version_range

        with tempfile.NamedTemporaryFile(delete=False, suffix=".jar") as tmp:
            temp_path = Path(tmp.name)
        with zipfile.ZipFile(temp_path, "w", zipfile.ZIP_DEFLATED) as target:
            for item in source.infolist():
                data = source.read(item.filename)
                if item.filename == MOD_JSON:
                    data = json.dumps(metadata, indent=2).encode("utf-8")
                target.writestr(item, data)
    shutil.move(str(temp_path), str(jar))


def main(argv: list[str]) -> int:
    if len(argv) < 3:
        print(__doc__)
        return 2
    version_range = argv[1]
    for raw in argv[2:]:
        jar = Path(raw)
        rewrite(jar, version_range)
        print(f"{jar}: minecraft = {version_range}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
