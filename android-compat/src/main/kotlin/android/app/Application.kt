package android.app

import android.content.JavaSharedPreferences
import android.content.SharedPreferences

open class Application : android.content.Context() {
    override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences =
        JavaSharedPreferences(name ?: "default")
}
