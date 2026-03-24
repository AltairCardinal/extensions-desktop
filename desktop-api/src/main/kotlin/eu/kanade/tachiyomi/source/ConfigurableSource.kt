package eu.kanade.tachiyomi.source

import android.content.JavaSharedPreferences
import android.content.SharedPreferences

interface ConfigurableSource : Source {
    fun getSourcePreferences(): SharedPreferences = JavaSharedPreferences(preferenceKey())

    fun setupPreferenceScreen(screen: androidx.preference.PreferenceScreen)
}

fun ConfigurableSource.preferenceKey(): String = "source_$id"

fun ConfigurableSource.sourcePreferences(): SharedPreferences = JavaSharedPreferences(preferenceKey())

fun sourcePreferences(key: String): SharedPreferences = JavaSharedPreferences(key)
