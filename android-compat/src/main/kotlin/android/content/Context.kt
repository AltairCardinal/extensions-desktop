package android.content

import java.io.File

open class Context {
    open fun getSharedPreferences(name: String?, mode: Int): android.content.SharedPreferences? = null

    open val packageName: String get() = "eu.kanade.tachiyomi"
    open val cacheDir: File get() = File(System.getProperty("java.io.tmpdir") ?: "/tmp", "mihon-desktop-cache")
    open val externalCacheDir: File? get() = cacheDir
    open val filesDir: File get() = File(System.getProperty("user.home") ?: ".", ".mihon-desktop")
    open val applicationInfo: android.content.pm.ApplicationInfo get() = android.content.pm.ApplicationInfo()

    open fun getSystemService(name: String): Any? = null
    open fun getString(resId: Int): String = ""
    open fun getString(resId: Int, vararg formatArgs: Any?): String = ""

    companion object {
        const val MODE_PRIVATE = 0
        const val CONNECTIVITY_SERVICE = "connectivity"
        const val AUDIO_SERVICE = "audio"
        const val DOWNLOAD_SERVICE = "download"
    }
}
