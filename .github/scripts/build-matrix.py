#!/usr/bin/env python3
"""Build the GitHub Actions matrix for the Recap Mod build.

The list of Minecraft versions is read from ``settings.gradle.kts`` (``supportedVersions``)
so that the workflow and the Gradle build can never drift apart.

Outputs a GitHub Actions matrix of the form::

    {"include": [{"version": "1.21", "experimental": false, "artifact": "recapmod-1.21"}, ...]}

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

# Versions whose build is allowed to fail without failing the workflow.
EXPERIMENTAL = {"26.4"}


def supported_versions() -> list[str]:
    text = SETTINGS.read_text(encoding="utf-8")
    match = re.search(r"val\s+supportedVersions\s*=\s*listOf\((.*?)\n\)", text, re.DOTALL)
    if not match:
        raise SystemExit("Could not find 'val supportedVersions = listOf(...)' in settings.gradle.kts")
    versions = re.findall(r'"([^"]+)"', match.group(1))
    if not versions:
        raise SystemExit("No Minecraft versions found in settings.gradle.kts")
    return versions


def main() -> int:
    versions = supported_versions()

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
                "experimental": version in EXPERIMENTAL,
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
