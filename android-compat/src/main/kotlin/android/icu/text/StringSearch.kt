package android.icu.text

import java.text.StringCharacterIterator

/** JVM stub for android.icu.text.StringSearch. */
class StringSearch(
    pattern: String,
    target: StringCharacterIterator,
    @Suppress("UNUSED_PARAMETER") collator: Collator? = null,
) {
    var pattern: String = pattern
    var target: StringCharacterIterator = target
    var isOverlapping: Boolean = false
    var matchedText: String = ""
        private set

    private fun currentText(): String {
        val t = target
        return buildString {
            var c = t.first()
            while (c != java.text.CharacterIterator.DONE) { append(c); c = t.next() }
        }
    }

    private var searchPos = 0

    fun first(): Int {
        searchPos = 0
        return nextFrom(0)
    }

    fun next(): Int = nextFrom(searchPos + 1)

    private fun nextFrom(start: Int): Int {
        val text = currentText()
        val idx = text.indexOf(pattern, start, ignoreCase = true)
        return if (idx >= 0) {
            searchPos = idx
            matchedText = text.substring(idx, idx + pattern.length)
            idx
        } else {
            DONE
        }
    }

    companion object {
        const val DONE = BreakIterator.DONE
    }
}
