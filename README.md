# Recap Mod

**Recap Mod** is a Minecraft mod that records your game sessions and lets you replay them afterwards from
any perspective — including free camera movement, rendering the replay to a video file, and uploading it
straight to YouTube.

Recap Mod is a fork of the excellent [Replay Mod](https://github.com/ReplayMod/ReplayMod) and is maintained by
**TurtleOfficials**. It keeps the full feature set of the upstream project (recording, replay viewer, camera
paths, video rendering, live streaming, simple pathing, …) while being built and versioned independently.

| | |
|---|---|
| Maintainer | TurtleOfficials |
| Repository | https://github.com/Endiq-jar/Recap |
| Minecraft support | **1.21.1 – 26.4** (see the table below) |
| Mod loader | Fabric |
| License | GNU GPL v3 or later (see [`LICENSE.md`](LICENSE.md)) |

## Supported Minecraft versions

Every supported Minecraft version gets its own jar, built from the same code base via the preprocessor.

| Minecraft | Status | Gradle project |
|---|---|---|
| 1.21 / **1.21.1** | ✅ supported | `:1.21` |
| 1.21.2 | ✅ supported | `:1.21.2` |
| 1.21.4 | ✅ supported | `:1.21.4` |
| 1.21.5 | ✅ supported | `:1.21.5` |
| 1.21.7 | ✅ supported | `:1.21.7` |
| 1.21.10 | ✅ supported | `:1.21.10` |
| 1.21.11 | ✅ supported | `:1.21.11` |
| 26.1 | ✅ supported | `:26.1` |
| 26.2 | ✅ supported | `:26.2` |
| 26.3 | ✅ supported | `:26.3` |
| 26.4 | 🧪 experimental | `:26.4` |

Notes:

* 1.21.1 is a bug-fix release of 1.21 without gameplay code changes, so the `1.21` jar runs on it as well.
* 26.4 is the upcoming 2026 game drop. The Gradle project, preprocessor node and dependency wiring are already
  in place (Fabric API `0.164.x+26.4`), so it starts building as soon as Minecraft 26.4 and the matching
  `jGui`/Loom toolchain are published. Until then CI builds it as an **experimental, non-blocking** target.

## Building

The project is a Gradle multi-project build, one subproject per Minecraft version.

```bash
git clone --recursive https://github.com/Endiq-jar/Recap.git
cd Recap

# build every version Gradle knows about - including the legacy projects which only exist as
# ancestors for the preprocessor chain (very slow: one Minecraft decompile per version)
./gradlew build

# build a single version (fast, recommended)
./gradlew :1.21.7:bundleJar
```

The finished mod jars end up in `versions/<mcversion>/build/libs/recapmod-<mcversion>-<version>.jar`
(one entry per row of the table above).

Requirements:

* JDK 21 (JDK 25 is picked up automatically for newer Minecraft versions when present)
* ~8 GB of RAM for the Gradle daemon (`org.gradle.jvmargs=-Xmx8G` in `gradle.properties`)
* Git submodules, see below

### Submodules

Two upstream components are consumed as Git submodules:

| Path | Repository |
|---|---|
| `jGui` | https://github.com/ReplayMod/jGui |
| `libs/ReplayStudio` | https://github.com/ReplayMod/ReplayStudio |

Fetch them with:

```bash
git submodule update --init --recursive
```

The translation files used to be a submodule as well; they are vendored under
`src/main/resources/assets/replaymod/lang` in this repository so that the Replay Mod → Recap Mod rename is
applied consistently to every language.

## Automatic builds (GitHub Actions)

`.github/workflows/build.yml` builds, on every push and pull request:

* one job per Minecraft version in a parallel matrix (`:1.21` … `:26.4`),
* each job uploads `recapmod-<mcversion>-<version>.jar` as a workflow artifact,
* a `collect` job bundles all jars into a single `recapmod-all` artifact,
* the 26.4 target is marked experimental and never fails the pipeline.

Pushing a tag like `v1.0.0` additionally creates a GitHub release and attaches every jar to it.
You can also start a manual build from the *Actions* tab (`workflow_dispatch`) and optionally restrict it to a
subset of Minecraft versions.

## Project layout

```
src/main/java            version independent sources (preprocessed per Minecraft version)
src/main/resources       mod metadata (fabric.mod.json, mcmod.info), mixins, assets, translations
versions/<mcversion>     version specific source overrides / mixins (inherited by newer versions)
versions/mainProject     root of the preprocessor tree
jGui                     GUI framework (submodule)
libs/ReplayStudio        replay file format library (submodule, included build)
docs/                    sources of the user documentation website
```

## What changed compared to Replay Mod?

* Renamed to **Recap Mod**, author/maintainer metadata set to **TurtleOfficials**.
* Jar artifacts are called `recapmod-<mcversion>-<version>.jar`.
* Build matrix re-focused on Minecraft 1.21.1 through 26.4; a 26.4 target has been added. The legacy
  `versions/` source trees are kept because the preprocessor inherits version specific sources along the
  version chain, but only 1.21+ are built.
* GitHub Actions workflow for automatic per-version builds and tag releases (replaces the old Jenkinsfile).
* Translations vendored into the repository instead of being a Git submodule.

The internal mod id (`replaymod`), the Java package (`com.replaymod.*`) and the resource namespace
(`assets/replaymod`) are intentionally unchanged: they are implementation details, and keeping them means
existing configs, key bindings and resource paths keep working after the rename.

## Credits

Recap Mod would not exist without the Replay Mod team and community:

* **CrushedPixel** and **johni0702** — original Replay Mod authors
* all [Replay Mod contributors](https://github.com/ReplayMod/ReplayMod/graphs/contributors)
* the translators of the language files

## License

Recap Mod is licensed under the **GNU General Public License v3.0 or later**, see [`LICENSE.md`](LICENSE.md).
It is a derivative work of Replay Mod, which is licensed under the same terms.
