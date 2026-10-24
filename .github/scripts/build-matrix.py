#!/usr/bin/env python3
"""Build the GitHub Actions matrix for the Recap Mod build.

The list of Minecraft versions is read from ``settings.gradle.kts`` (``supportedVersions``)
so that the workflow and the Gradle build can never drift apart.

Outputs a GitHub Actions matrix of the form::

    {"include": [{"version": "1.21", "experimental": false, "gradle_args": "",
                  "mc_range": ">=1.21 <1.21.2", "artifact": "recapmod-1.21"}, ...]}

Environment variables:

* ``REQUESTED_VERSIONS`` - optional space separated subset (from workflow_dispatch)
* ``GITHUB_OUTPUT``      - file to append ``matrix=<json>`` to (set by GitHub Actions)
"""

from __future__ import annotations

import json
import os
import pathlib
import re
import sys

SETTINGS = pathlib.Path("settings.gradle.kts")

# Builds which also run on their patch-level successors, and the Fabric version range they declare.
# Only ranges we are confident about are listed; everything else produces an exact-version jar.
MC_RANGES = {
    # 1.21.1 is a bug-fix release of 1.21 without code changes
    "1.21": ">=1.21 <1.21.2",
}


def _read_list(text: str, name: str) -> list[str]:
    match = re.search(rf"val\s+{name}\s*=\s*listOf\((.*?)\n\)", text, re.DOTALL)
    if not match:
        raise SystemExit(f"Could not find 'val {name} = listOf(...)' in settings.gradle.kts")
    return re.findall(r'"([^"]+)"', match.group(1))


def supported_versions() -> tuple[list[str], list[str]]:
    """Return (supported, experimental) Minecraft versions declared in settings.gradle.kts."""
    text = SETTINGS.read_text(encoding="utf-8")
    supported = _read_list(text, "supportedVersions")
    experimental = _read_list(text, "experimentalVersions")
    if not supported:
        raise SystemExit("No Minecraft versions found in settings.gradle.kts")
    return supported, experimental


def main() -> int:
    supported, experimental = supported_versions()
    versions = supported + experimental

    requested = os.environ.get("REQUESTED_VERSIONS", "").replace(",", " ").split()
    if requested:
        unknown = [v for v in requested if v not in versions]
        if unknown:
            raise SystemExit(
                "Unsupported Minecraft version(s) requested: "
                + ", ".join(unknown)
                + ". Supported: "
                + ", ".join(versions)
            )
        versions = [v for v in versions if v in requested]

    matrix = {
        "include": [
            {
                "version": version,
                "experimental": version in experimental,
                # Experimental targets have to be enabled explicitly in the Gradle build.
                # Args are "|" separated on purpose: some values contain spaces (version ranges) and
                # GitHub would otherwise split them into separate shell words.
                "gradle_args": "|".join(
                    args for args in [
                        "-Precap.experimental=true" if version in experimental else "",
                    ] if args
                ),
                "mc_range": MC_RANGES.get(version, version),
                "artifact": f"recapmod-{version}",
            }
            for version in versions
        ]
    }

    payload = json.dumps(matrix)
    output_file = os.environ.get("GITHUB_OUTPUT")
    if output_file:
        with open(output_file, "a", encoding="utf-8") as handle:
            handle.write(f"matrix={payload}\n")
            handle.write(f"count={len(versions)}\n")

    print(payload)
    return 0


if __name__ == "__main__":
    sys.exit(main())
