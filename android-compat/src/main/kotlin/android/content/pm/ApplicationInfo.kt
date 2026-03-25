package android.content.pm

class ApplicationInfo {
    var packageName: String = "eu.kanade.tachiyomi"
    var sourceDir: String = ""
    var dataDir: String = System.getProperty("user.home") ?: "."
    var nativeLibraryDir: String = ""
    var flags: Int = 0
}
