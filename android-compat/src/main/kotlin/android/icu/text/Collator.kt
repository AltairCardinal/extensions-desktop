package android.icu.text

import java.util.Locale

/** JVM stub for android.icu.text.Collator. */
abstract class Collator {
    open var strength: Int = TERTIARY
    open var decomposition: Int = CANONICAL_DECOMPOSITION
    abstract fun compare(source: String, target: String): Int

    companion object {
        const val NO_DECOMPOSITION = 0
        const val CANONICAL_DECOMPOSITION = 1
        const val FULL_DECOMPOSITION = 2
        const val PRIMARY = 0
        const val SECONDARY = 1
        const val TERTIARY = 2
        const val IDENTICAL = 3

        @JvmStatic fun getInstance(): Collator = RuleBasedCollator()
        @JvmStatic fun getInstance(locale: Locale): Collator = RuleBasedCollator()
    }
}
