package android.app

import android.content.JavaSharedPreferences
import android.content.SharedPreferences

open class Application {
    fun getSharedPreferences(name: String, @Suppress("UNUSED_PARAMETER") mode: Int): SharedPreferences =
        JavaSharedPreferences(name)
}
