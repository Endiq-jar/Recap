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

* 1.21.1 is a bug-fix release of 1.21 without code changes, so the `1.21` jar runs on it as well. That jar
  declares the Fabric range `>=1.21 <1.21.2` instead of a single version (`-Precap.mcRange=...`).
* 26.4 is the upcoming 2026 game drop. The Gradle project, preprocessor node and dependency wiring are already
  in place (Fabric API `0.164.x+26.4`), so it starts building as soon as Minecraft 26.4 and the matching
  `jGui`/Loom toolchain are published. Because Minecraft 26.4 does not exist yet, the target is opt-in and CI
  builds it as an **experimental, non-blocking** job:

  ```bash
  ./gradlew :26.4:bundleJar -Precap.experimental=true
  ```

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

* JDK 21 to run Gradle (JDK 25 is used for newer drops when it is installed)
* JDK 8, 16 and 17 installed as well: the preprocessor compiles the older projects of the version chain
  (ReplayStudio/1.16, 1.17-1.18, 1.19-1.20) with them. Point Gradle at them with
  `-Dorg.gradle.java.installations.paths=/path/to/jdk8,/path/to/jdk16,/path/to/jdk17` if they are not in a
  standard location. GitHub Actions does exactly that, see `.github/workflows/build.yml`.
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

## One download, every Minecraft version

Minecraft mods are not portable across game versions: Replay/Recap Mod compiles against the Minecraft
classes and mappings of each release, and Fabric applies the mixins of exactly one version. A single jar
that Minecraft itself loads on 1.21.1 *and* on 26.4 is therefore not possible — pretending otherwise
would just produce a jar that crashes.

What Recap Mod does instead is collapse everything into **one file to download**:

| Artifact | Contents | Use it when |
|---|---|---|
| `recapmod-universal-<version>.jar` | **every** supported Minecraft version in one file | you just want one download |
| `recapmod` | the same sets of jars, one per Minecraft version | you know your Minecraft version |
| `recapmod-<minecraft>-<version>.jar` | a single version, installable directly | you want a drop-in mod jar |

The universal jar is a **bundle** (a jar/zip container, exactly like a `.zip` that is named `.jar`):

```
recapmod-universal-1.0.0.jar
├── INSTALL.txt                # which jar to pick
├── VERSION.txt
└── META-INF/recap/
    ├── 1.21/recapmod-1.21-1.0.0.jar
    ├── 1.21.2/recapmod-1.21.2-1.0.0.jar
    ├── ...
    └── 26.3/recapmod-26.3-1.0.0.jar
```

Extract the jar matching your Minecraft version into `.minecraft/mods` (Fabric). Every contained jar is
listed above as its own row, so the universal bundle never contains versions that are not supported.

Every jar declares exactly which Minecraft versions it accepts in its `fabric.mod.json`
(`depends.minecraft`): builds with a dedicated range (the `1.21` jar also covers `1.21.1`) get that range,
all other builds declare their exact version, so Fabric refuses to load a jar into a game version it was
never compiled against.

CI verifies every produced jar before uploading it: mod id `replaymod`, name `Recap Mod` and the declared
Minecraft range must match the build matrix. To apply a range to a jar manually:

```bash
python3 .github/scripts/set-mc-range.py ">=1.21 <1.21.2" versions/1.21/build/libs/recapmod-1.21-*.jar
```

## Interface

The interface is a single design system instead of per-screen vanilla widgets, implemented in
`com.replaymod.core.gui`:

* `RecapTheme` — colours, spacing and drawing primitives (gradient background, cards, header rails, HUD
  bars, chamfered corners). One place to change the look of the whole mod.
* `RecapScreen` — the base screen: themed background, header with title/subtitle/version badge, footer hint.
* `RecapCard` — a titled card with settings rows that highlight on hover.
* `RecapButton` — flat primary (accent) and secondary buttons.

Applied to:

| Screen / overlay | Redesign |
|---|---|
| Settings | grouped into cards per category (General, Recording, Replay Viewer, Rendering, Camera Paths, Advanced) in a scrollable list instead of two bare columns |
| Recording indicator | compact HUD pill with status dot, accent rail and blinking dot instead of the old texture icon |
| Replay controls | themed HUD bar behind play/pause, speed and timeline plus a status bar |
| Replay recovery | themed card with header/footer copy and themed buttons |
| Mod compatibility warning | themed screen chrome, scrollable content, themed buttons |

## Automatic builds (GitHub Actions)

`.github/workflows/build.yml` builds, on every push and pull request:

* one job per Minecraft version in a parallel matrix (`:1.21` … `:26.4`),
* each job uploads `recapmod-<mcversion>-<version>.jar` as a workflow artifact,
* a `collect` job publishes one `recapmod` artifact with all jars and builds the single-file
  `recapmod-universal-<version>.jar` bundle,
* the 26.4 target is marked experimental and never fails the pipeline,
* the 1.21 jar declares the Fabric range `>=1.21 <1.21.2` so it also loads on 1.21.1.

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
* A single `recapmod-universal-<version>.jar` bundle with every supported Minecraft version, plus a single
  `recapmod` artifact, so a release never means hunting through eleven downloads.
* Redesigned interface: shared theme, themed settings screen grouped by category, recording HUD pill,
  replay control bar and rethemed recovery/compatibility screens.
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
