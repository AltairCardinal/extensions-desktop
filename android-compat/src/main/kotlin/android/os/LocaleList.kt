package android.os

import java.util.Locale

/**
 * JVM stub for android.os.LocaleList.
 * Returns JVM default locale for desktop compatibility.
 */
class LocaleList private constructor(private val locales: List<Locale>) {

    fun getFirstMatch(supportedLocales: Array<String>): Locale? {
        val systemLang = Locale.getDefault().language
        return if (supportedLocales.any { it == systemLang }) Locale.getDefault() else null
    }

    companion object {
        @JvmStatic
        fun getDefault(): LocaleList = LocaleList(listOf(Locale.getDefault()))
    }
}
