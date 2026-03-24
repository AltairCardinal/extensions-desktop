pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    // Share extensions-source's version catalog when building as standalone
    versionCatalogs {
        create("libs") {
            from(files("extensions-source/gradle/libs.versions.toml"))
        }
    }
    repositories {
        mavenCentral()
        google()
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "extensions-desktop"

// android-compat shim — always included
include(":android-compat")
include(":desktop-api")

// Extensions are included dynamically by the build script after patching
// (settings are re-evaluated after patch.sh runs)
val extSrcDir = file("extensions-source")
if (extSrcDir.exists()) {
    include(":core")
    project(":core").projectDir = file("extensions-source/core")

    // lib modules
    file("extensions-source/lib").listFiles()?.filter { it.isDirectory }?.forEach { lib ->
        include(":lib:${lib.name}")
        project(":lib:${lib.name}").projectDir = lib
    }

    // lib-multisrc modules
    file("extensions-source/lib-multisrc").listFiles()?.filter { it.isDirectory }?.forEach { lib ->
        include(":lib-multisrc:${lib.name}")
        project(":lib-multisrc:${lib.name}").projectDir = lib
    }

    // extension src modules
    file("extensions-source/src").listFiles()?.filter { it.isDirectory }?.forEach { lang ->
        lang.listFiles()?.filter { it.isDirectory }?.forEach { ext ->
            include(":src:${lang.name}:${ext.name}")
            project(":src:${lang.name}:${ext.name}").projectDir = ext
        }
    }
}
