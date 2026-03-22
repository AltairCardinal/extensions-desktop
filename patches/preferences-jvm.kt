package keiyoushi.utils

import android.content.JavaSharedPreferences
import android.content.SharedPreferences
import eu.kanade.tachiyomi.source.online.HttpSource

/**
 * JVM replacement for core/src/main/kotlin/keiyoushi/utils/Preferences.kt.
 * Avoids android.app.Application / Injekt dependency — uses JavaSharedPreferences directly.
 */
inline fun HttpSource.getPreferences(
    migration: SharedPreferences.() -> Unit = {},
): SharedPreferences = getPreferences(id).also(migration)

inline fun HttpSource.getPreferencesLazy(
    crossinline migration: SharedPreferences.() -> Unit = {},
) = lazy { getPreferences(migration) }

@Suppress("NOTHING_TO_INLINE")
inline fun getPreferences(sourceId: Long): SharedPreferences =
    JavaSharedPreferences("source_$sourceId")
