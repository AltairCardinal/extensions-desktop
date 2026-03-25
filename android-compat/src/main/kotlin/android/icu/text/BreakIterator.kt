package android.icu.text

import java.util.Locale

/** JVM stub mapping android.icu.text.BreakIterator to java.text.BreakIterator. */
open class BreakIterator protected constructor(private val delegate: java.text.BreakIterator) {
    var text: String
        get() = (delegate.text as? java.text.StringCharacterIterator)?.let {
            buildString { var c = it.first(); while (c != java.text.CharacterIterator.DONE) { append(c); c = it.next() } }
        } ?: ""
        set(value) { delegate.setText(value) }

    fun next(): Int = delegate.next()
    fun ruleStatus(): Int = 0

    companion object {
        const val DONE = java.text.BreakIterator.DONE
        @JvmStatic fun getWordInstance(): BreakIterator = BreakIterator(java.text.BreakIterator.getWordInstance())
        @JvmStatic fun getWordInstance(locale: Locale): BreakIterator = BreakIterator(java.text.BreakIterator.getWordInstance(locale))
        @JvmStatic fun getCharacterInstance(): BreakIterator = BreakIterator(java.text.BreakIterator.getCharacterInstance())
        @JvmStatic fun getSentenceInstance(): BreakIterator = BreakIterator(java.text.BreakIterator.getSentenceInstance())
    }
}
