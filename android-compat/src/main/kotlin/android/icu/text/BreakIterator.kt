package android.icu.text

import java.text.StringCharacterIterator
import java.util.Locale

/** JVM stub mapping android.icu.text.BreakIterator to java.text.BreakIterator. */
open class BreakIterator protected constructor(protected val delegate: java.text.BreakIterator) {
    var text: java.text.CharacterIterator
        get() = delegate.text
        set(value) { delegate.text = value }

    val ruleStatus: Int get() = 0

    fun first(): Int = delegate.first()
    fun next(): Int = delegate.next()
    fun previous(): Int = delegate.previous()
    fun last(): Int = delegate.last()
    fun current(): Int = delegate.current()

    companion object {
        const val DONE = java.text.BreakIterator.DONE
        const val WORD_NONE = 0
        const val WORD_NONE_LIMIT = 100
        const val WORD_NUMBER = 100
        const val WORD_NUMBER_LIMIT = 200
        const val WORD_LETTER = 200
        const val WORD_LETTER_LIMIT = 300
        const val WORD_KANA = 300
        const val WORD_KANA_LIMIT = 400
        const val WORD_IDEO = 400
        const val WORD_IDEO_LIMIT = 500

        @JvmStatic fun getWordInstance(): BreakIterator = BreakIterator(java.text.BreakIterator.getWordInstance())
        @JvmStatic fun getWordInstance(locale: Locale): BreakIterator = BreakIterator(java.text.BreakIterator.getWordInstance(locale))
        @JvmStatic fun getCharacterInstance(): BreakIterator = BreakIterator(java.text.BreakIterator.getCharacterInstance())
        @JvmStatic fun getCharacterInstance(locale: Locale): BreakIterator = BreakIterator(java.text.BreakIterator.getCharacterInstance(locale))
        @JvmStatic fun getSentenceInstance(): BreakIterator = BreakIterator(java.text.BreakIterator.getSentenceInstance())
    }
}
