pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        google()
        maven("https://jitpack.io")
        maven("https://maven.fabricmc.net")
        maven("https://maven.architectury.dev/")
        maven("https://maven.minecraftforge.net")
        maven("https://repo.essential.gg/repository/maven-public")
    }
    plugins {
        id("gg.essential.multi-version.root") version "0.7.2"
        id("com.gradleup.shadow") version "9.4.1"
    }
}

// Recap Mod targets Minecraft 1.21.1 - 26.4 (see README.md).
// The 1.21 build also covers 1.21.1 (bug-fix release, no code changes).
val supportedVersions = listOf(
        "1.21",     // also runs on 1.21.1
        "1.21.2",
        "1.21.4",
        "1.21.5",
        "1.21.7",
        "1.21.10",
        "1.21.11",
        "26.1",
        "26.2",
        "26.3",
        "26.4",     // experimental - see README.md
)
// The preprocessor compiles `src/main` against the "main project" (see versions/mainProject -> 1.16.4).
// That project has to exist in the build even though it is not part of the released version matrix.
val toolingVersions = listOf("1.16.4")
val jGuiVersions = listOf(
        "1.16.4",
        "1.21",
        "1.21.2",
        "1.21.4",
        "1.21.5",
        "1.21.7",
        "1.21.10",
        "1.21.11",
        "26.1",
        "26.2",
        "26.3",
)
val replayModVersions = supportedVersions + toolingVersions

rootProject.buildFileName = "root.gradle.kts"

includeBuild("libs/ReplayStudio")

include(":jGui")
project(":jGui").apply {
    projectDir = file("jGui")
    buildFileName = "root.gradle.kts"
}
jGuiVersions.forEach { version ->
    include(":jGui:$version")
    project(":jGui:$version").apply {
        projectDir = file("jGui/versions/$version")
        buildFileName = "../../build.gradle.kts"
    }
}

replayModVersions.forEach { version ->
    include(":$version")
    project(":$version").apply {
        projectDir = file("versions/$version")
        buildFileName = "../../build.gradle.kts"
    }
}
