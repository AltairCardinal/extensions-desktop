package android.icu.text

import java.text.BreakIterator as JBI
import java.util.Locale

/** JVM stub for android.icu.text.StringSearch. */
class StringSearch(
    private val pattern: String,
    private val target: CharSequence,
    private val locale: Locale = Locale.getDefault(),
) {
    private var pos = 0
    val matchedText: String get() = if (pos >= 0 && pos <= target.length) pattern else ""

    fun next(): Int {
        val start = if (pos < 0) 0 else pos + 1
        val idx = target.toString().indexOf(pattern, start, ignoreCase = true)
        pos = idx
        return if (idx >= 0) idx else BreakIterator.DONE
    }
}
