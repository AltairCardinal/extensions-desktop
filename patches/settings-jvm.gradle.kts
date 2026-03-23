/**
 * JVM-aware settings.gradle.kts replacement for extensions-source.
 *
 * By default it includes all individual extensions.
 * When -Pdesktop.modules=src:lang:name[,src:lang:name] is provided,
 * only those extension modules are included, which lets the desktop build
 * skip unrelated Android-only failures and publish successful modules.
 */

val requestedModules = providers.gradleProperty("desktop.modules")
    .orNull
    ?.split(",")
    ?.map { it.trim() }
    ?.filter { it.isNotEmpty() }
    ?.toSet()
    .orEmpty()

if (requestedModules.isEmpty()) {
    loadAllIndividualExtensions()
} else {
    requestedModules.forEach(::include)
}

include(":core")
include(":android-compat")

File(rootDir, "lib").eachDir { include("lib:${it.name}") }
File(rootDir, "lib-multisrc").eachDir { include("lib-multisrc:${it.name}") }

fun loadAllIndividualExtensions() {
    File(rootDir, "src").eachDir { dir ->
        dir.eachDir { subdir ->
            include("src:${dir.name}:${subdir.name}")
        }
    }
}

fun File.eachDir(block: (File) -> Unit) {
    val files = listFiles() ?: return
    for (file in files) {
        if (file.isDirectory && file.name != ".gradle" && file.name != "build") {
            block(file)
        }
    }
}
